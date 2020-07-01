package com.ylink.aml.modular.system.util.sqlParse;


import com.cfss.util.JDBCUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

/**
 * TODO:SQL解析
 *
 * @author jiang
 * @date 2019/7/11 15:38
 */
public class SQLParse /*implements SQLParseInner*/ {

    public static final Log LOG = LogFactory.getLog(SQLParse.class.getName());
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) {

        String sql1 = "select * from tb_acc";
        boolean mysql = parse(sql1, "hive");
        System.out.println(mysql);
    }

    /**
     * TODO:判断是哪种类型的数据库，调用不同的方法
     *
     * @param sql
     * @param dbType
     * @return
     */
    public static boolean parse(String sql, String dbType) {
        boolean res = false;
        if ("mysql".equals(dbType.toLowerCase())) {
            res = mysqlSQLParse(sql);
        } else if ("hive".equals(dbType.toLowerCase())) {
            String url = "jdbc:hive2://172.168.200.24:10000/aml";
            res = hiveSQLParse(url, driverName, sql);
        }
        return res;
    }

    /**
     * 生成hiveSQL执行计划，能够生成返回true，否则返回false
     *
     * @param url
     * @param driver
     * @param sql
     * @return
     */
    public static boolean hiveSQLParse(String url, String driver, String sql) {
        Connection conn = null;
        Statement statement = null;
        try {
            Class.forName(driver);
             conn = DriverManager.getConnection(url,"hdfs","");
             statement = conn.createStatement();
            String explainSql = " explain " + sql;
            return statement.execute(explainSql);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("There is a problem with the SQL syntax.");
            return false;
        }finally {
            closeResources(conn,statement,null);
        }
    }

    /**
     * 生成mysqlSQL执行计划，能够生成返回true，否则返回false
     *
     * @param sql
     * @return
     */
    public static boolean mysqlSQLParse(String sql) {
        try {
            Connection connection = JDBCUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            return statement.execute();
        } catch (Exception e) {
            LOG.error("There is a problem with the SQL syntax.");
            return false;
        }

    }

    public static void closeResources(Connection conn, Statement statement, ResultSet resultSet) {

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                resultSet = null;
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                statement = null;
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                conn = null;
            }
        }
    }
}