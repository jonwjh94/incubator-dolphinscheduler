package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;

/**
 * @author weijunhao
 * @date 2021/5/11 16:00
 */
public class SparkSQLDataSource extends BaseDataSource {
    @Override
    public String driverClassSelector() {
        return Constants.COM_SEASUN_RELEASE_JDBC_SPARKSQL_DRIVER;
    }

    @Override
    public DbType dbTypeSelector() {
        return DbType.SPARKSQL;
    }
}
