package org.apache.dolphinscheduler.alert.feishu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.model.AlertData;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.apache.dolphinscheduler.plugin.model.PluginName;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author weijunhao
 * @date 2021/7/15 17:34
 */
public class FeishuAlertPlugin implements AlertPlugin {

    private static final Logger logger = LoggerFactory.getLogger(FeishuAlertPlugin.class);

    private static final Boolean FEISHU_ENABLE = PropertyUtils.getBoolean(Constants.FEISHU_ENABLE);
    private static final String ALGORITHM = "HmacSHA256";

    private PluginName pluginName;

    public FeishuAlertPlugin() {
        this.pluginName = new PluginName();
        this.pluginName.setEnglish(Constants.PLUGIN_FEISHU_EN);
        this.pluginName.setChinese(Constants.PLUGIN_FEISHU_CH);
    }

    @Override
    public String getId() {
        return Constants.PLUGIN_FEISHU;
    }

    @Override
    public PluginName getName() {
        return pluginName;
    }

    @Override
    public Map<String, Object> process(AlertInfo info) {
        AlertData alertData = info.getAlertData();
        String description = (String) info.getProp("description");
        try {
            JSONObject feishu = JSON.parseObject(description).getJSONObject("feishu");
            String webhook = feishu.getString("webhook");
            String secretKey = feishu.getString("secretKey");
            if (FEISHU_ENABLE&&webhook!=null&&secretKey!=null){
                long timestamp = System.currentTimeMillis() / 1000;
                SecretKeySpec keySpec = new SecretKeySpec((timestamp + "\n" + secretKey).getBytes(), ALGORITHM);
                Mac hmacSha256 = Mac.getInstance(ALGORITHM);
                hmacSha256.init(keySpec);
                byte[] bytes = hmacSha256.doFinal();
                String s = Base64.encodeBase64String(bytes);
                JSONObject json = new JSONObject();
                json.put("timestamp", timestamp + "");
                json.put("sign", s);
                json.put("msg_type", "text");
                json.put("content", new JSONObject() {{
                    put("text", "调度告警：\n" + alertData.getContent());
                }});
                sendRequest(webhook, json.toJSONString());
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("The encryption has failed.", e);
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void sendRequest(String url, String json) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClientBuilder.create().build();
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            HttpUriRequest post = RequestBuilder.post()
                    .setUri(url)
                    .setCharset(StandardCharsets.UTF_8)
                    .setEntity(entity)
                    .build();
            response = httpClient.execute(post);
        } catch (Exception e) {
            logger.error("send http request failed.", e);
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("close http client failed.", e);
            }
        }
    }

}
