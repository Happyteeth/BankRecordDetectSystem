package com.cfss.util;

import com.cfss.rules.RuleRun;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * JDBC工具
 * @author Black
 *
 */
public class JDBCUtil {
	public static final Log LOG = LogFactory.getLog(JDBCUtil.class.getName());

	public static Properties  getProperties(){
		// 读取配置信息
		Properties properties = new Properties();
		InputStream inputStream=null;
		try {
			try {
				inputStream = new FileInputStream("config/jdbc.properties");
			}catch (Exception e){
				inputStream =null;
			}
			//inputStream = JDBCUtil.class.getClassLoader().getResourceAsStream("config/jdbc.properties");
			if(inputStream==null){
				LOG.info("JDBCUtil load   jdbc.properties");
				inputStream = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
			}else{
				LOG.info("JDBCUtil load  config/jdbc.properties");
			}
			properties.load(inputStream);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw new RuntimeException("获取数据库配置信息失败!" + e1.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("获取数据库配置信息失败!" + e.getMessage());
		}
		return  properties;
	}

	public static Connection getConnection() {
		
		// 读取配置信息
		Properties properties =getProperties();

		String jdbcDriver = properties.getProperty("jdbc.driver");
		String jdbcUsername = properties.getProperty("jdbc.username");
		String jdbcPassword = properties.getProperty("jdbc.password");
		String jdbcUrl = properties.getProperty("jdbc.url");
		
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
			// 向上抛出异常
			throw new RuntimeException("数据库链接创建失败!" + e.getMessage());
		}
	}
	
	public static Connection getOConnection() {
		
		// 读取配置信息
		Properties properties =getProperties();

		String jdbcDriver = properties.getProperty("jdbc.driver");
		String jdbcUsername = properties.getProperty("o.jdbc.username");
		String jdbcPassword = properties.getProperty("o.jdbc.password");
		String jdbcUrl = properties.getProperty("o.jdbc.url");
		
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
			// 向上抛出异常
			throw new RuntimeException("数据库链接创建失败!" + e.getMessage());
		}
	}

	public static void closeResources(Statement statement, ResultSet resultSet){
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
	}

	/**
	 * 从程序参数表中查询获取参数配置值，如果没有配置，会返回defaultValue.  TBS_PROG_PARA
	 * @param conn    ： 数据库的连接
	 * @param progId  ：《程序参数表》的V_PROG_ID的值
	 * @param paraId  ：《程序参数表》中的V_PARA_ID的值
	 * @param defaultValue ： 如果查询失败，或者不存对应的值，则返回此值。
	 * @return
	 */
	public static String getValueFromMapping(Connection conn, String progId, String paraId, String defaultValue){
		PreparedStatement statement = null;
		ResultSet rs = null;
		String result = defaultValue;
		try{
			statement = conn.prepareStatement(SQLStaicInfo.SQL_PARA_VALUE_IN_TBS_PROG_PARA);
			statement.setString(1, progId);
			statement.setString(2, paraId);
			rs = statement.executeQuery();
			while(rs.next()){
				//只获取第一个值
				result = rs.getString(1);
				break;
			}
		}catch (Exception e){
			e.printStackTrace();

		}finally {
			closeResources(statement, rs);
		}
		return result;
	}

	/**
	 * 用于rule_run中的表信息
	 * @param rule
	 * @param conn
	 * @param status
	 * @return
	 */
	public static boolean updateRuleRunDb(RuleRun rule, Connection conn, int status) {
		PreparedStatement statement = null;
		try {
			//"UPDATE RULE_RUN SET STATUS = ? , RUN_TIME = ?, RERULT_LINE_N = ?, RERULT_CHART_DATA = ?, RERULT_PATH = ?, D_UPDATE = ?  WHERE RULE_RUN_ID = ?";
			//RERULT_LINE_N
			//UPDATE RULE_RUN SET STATUS = ? , RUN_TIME = ?, RERULT_LINE_N = ?, RERULT_CHART_DATA = ?, RERULT_PATH = ?, RERULT_COUNT = ?, D_UPDATE = ?  WHERE RULE_RUN_ID = ?";
			statement = conn.prepareStatement(SQLStaicInfo.SQL_UPDATE_RULE_RUN_FINAL);
			statement.setInt(1, status);
			statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
//            statement.setBlob(3, new StringBufferInputStream(rule.getResultLineN().toString()));
			statement.setString(3, rule.getResultLineN().toString());
            System.out.println("******TTTT***************************** line_n length = " + rule.getResultLineN().length());

			statement.setString(4,rule.getResultChartData().toString());
			statement.setString(5, rule.getResultPath());
			statement.setLong(6, rule.getResultCount());
			statement.setTimestamp(7,new Timestamp(System.currentTimeMillis()));
			statement.setString(8, rule.getRuleRunId());
			int ret =  statement.executeUpdate();
			System.out.println("..... ret = " + ret);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			JDBCUtil.closeResources(statement, null);
		}
		return true;

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
	
	public static Map<String,String> getJdbcProperties(){
		
		// 读取配置信息
		Properties properties = getProperties();
		
		HashMap<String, String> jdbcPropertiesMap = new HashMap<String,String>();
		jdbcPropertiesMap.put("jdbcDriver", properties.getProperty("jdbc.driver"));
		jdbcPropertiesMap.put("jdbcUsername", properties.getProperty("jdbc.username"));
		jdbcPropertiesMap.put("jdbcPassword", properties.getProperty("jdbc.password"));
		jdbcPropertiesMap.put("jdbcUrl", properties.getProperty("jdbc.url"));
		jdbcPropertiesMap.put("oJdbcUsername", properties.getProperty("o.jdbc.username"));
		jdbcPropertiesMap.put("oJdbcPassword", properties.getProperty("o.jdbc.password"));
		jdbcPropertiesMap.put("oJdbcUrl", properties.getProperty("o.jdbc.url"));
		
		return jdbcPropertiesMap;
		
		
	}
	/**
	 * 单独把这一行列出来的原因是：在Rule_run中有result_line_n， 这一个结果会比较大。考虑减少数据传输，因此单独处理
	 *
	 * @param rule
	 * @param conn
	 * @param status
	 * @return
	 */
	public static boolean updateRuleRunPostExecute(RuleRun rule, Connection conn, String status) {
		PreparedStatement statement = null;
		try {
			//public static final String SQL_UPDATE_RULE_RUN_LOCAL_PATH = "UPDATE RULE_RUN SET STATUS = ? , RERULT_PATH = ?,  D_UPDATE = ?  WHERE RULE_RUN_ID = ?";
			statement = conn.prepareStatement(SQLStaicInfo.SQL_UPDATE_RULE_RUN_LOCAL_PATH);
			statement.setString(1, status);
			if(rule.getResultPath() == null){
				statement.setString(2, "");
			}else{
				statement.setString(2, rule.getResultPath());
			}
			statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			statement.setString(4, rule.getRuleRunId());
			int ret = statement.executeUpdate();
			System.out.println("Success to update rule_run.  ret = " + ret);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Fail to update . " + SQLStaicInfo.SQL_UPDATE_RULE_RUN_LOCAL_PATH + ". rule_run_id = " + rule.getRuleRunId(), e);
			return false;
		} finally {
			JDBCUtil.closeResources(statement, null);
		}
		return true;
	}


}
