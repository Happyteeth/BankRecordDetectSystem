package com.ylink.aml.hive;

import java.io.Closeable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author jiang
 * @date 2019/7/26 10:35
 */
public class DelUtil {
    public static void closeSocket(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
                closeable = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Connection getConnection(String jdbcDriver, String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        Connection connection = null;
        try {
            // 1.加载驱动
            Driver driver = (Driver) Class.forName(jdbcDriver).newInstance();
            // 2.得到连接
            Properties info = new Properties();
            info.setProperty("user", jdbcUsername);
            info.setProperty("password", jdbcPassword);
            connection = driver.connect(jdbcUrl, info);
            return connection;
        } catch (Exception e) {
            // 记录日志
            e.printStackTrace();
            System.out.println("Fail to create connection.");
            // 向上抛出异常
            throw new RuntimeException("数据库链接创建失败!" + e.getMessage());
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

    public static Map<String, String> getFilePath(Connection connection, int file_id, String import_table) throws SQLException {
        Map<String, String> map = new HashMap<>();
        String querySQL = "select * from " + import_table + " WHERE FILE_ID = " + file_id;
        PreparedStatement statement = connection.prepareStatement(querySQL);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {//只可能有一条数据
            String tableName = resultSet.getString("TABLE_NAME");
            String oriPath = resultSet.getString("ORI_PATH");
            map.put("TABLE_NAME", tableName);
            map.put("ORI_PATH", oriPath);
        }
        return map;
    }

    public static Map<String, String> getFilePath(Connection connection) {
        // 从程序参数表中读取数据
        Map<String, String> map = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT V_PARA_ID,V_PARA_VALUE FROM TBS_PROG_PARA WHERE V_PROG_ID = 'DATA_FILE_UPLOAD'");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String v_para_id = resultSet.getString("V_PARA_ID");
                String v_para_value = resultSet.getString("V_PARA_VALUE");
                map.put(v_para_id, v_para_value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DelUtil.closeResources(connection, statement, resultSet);
        }
        return map;
    }

}
