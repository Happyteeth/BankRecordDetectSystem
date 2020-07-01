package com.ylink.aml.system;

import com.cfss.util.JDBCUtil;
import com.ylink.aml.base.BaseJunit;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlTest extends BaseJunit {

    @Test
    public void testSql() throws SQLException {
        Connection connection = JDBCUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT RULE_RUN_ID, RULE_ID, SUBMIT_TIME , TASK_PROGRAM, RULE_PROG, CHART_PROG, STATUS FROM RULE_RUN WHERE RULE_RUN.STATUS =  1 ORDER BY SUBMIT_TIME ASC LIMIT 0,1");
        ResultSet resultSet = statement.executeQuery();
        System.out.println(resultSet.next());
    }

}
