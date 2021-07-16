package org.apache.dolphinscheduler.alert.feishu;

import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.spi.AlertPluginProvider;

/**
 * @author weijunhao
 * @date 2021/7/15 17:37
 */
public class FeishuAlertPluginProvider implements AlertPluginProvider {
    @Override
    public AlertPlugin createPlugin() {
        return new FeishuAlertPlugin();
    }
}
