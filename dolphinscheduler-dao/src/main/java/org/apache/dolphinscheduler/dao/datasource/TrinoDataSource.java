package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;

/**
 * @author weijunhao
 * @date 2021/4/13 15:15
 */
public class TrinoDataSource extends BaseDataSource {
    @Override
    public String driverClassSelector() {
        return Constants.IO_PRESTOSQL_JDBC_PRESTODRIVER;
    }

    @Override
    public DbType dbTypeSelector() {
        return DbType.TRINO;
    }
}
