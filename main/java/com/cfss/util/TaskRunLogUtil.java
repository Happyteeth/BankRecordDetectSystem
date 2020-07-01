package com.cfss.util;

import java.sql.*;

/**
 * 日志工具
 * 	@author black
 *	@version 0.0.1
 */
public class TaskRunLogUtil {

	/**
	 * 写入SYM_TBS_TASK_RUN_LOG监控表工具
	 * @param taskId
	 * 			日志编号
	 * @param level
	 * 			日志的等级：0－正常；1－警告；2－遇到错误，程序继续运行；3－遇到错误，程序中止运行；
	 * @param logContent
	 * 			日志内容
	 * @param logAdds
	 * 			日志其他描述
	 * @param vSql
	 * 			跑数sql
	 */
	public static void saveLog(String taskId,String level, String logLevel,String logContent,String logAdds,String vSql) {
		Connection conn = null;
		try {
			conn = JDBCUtil.getConnection();
			saveLogWithConn(conn, taskId, level, logLevel, logContent, logAdds, vSql);
		}finally {
			JDBCUtil.closeResources(conn, null, null);
		}
	}
	
		public static  void saveLogWithConn(Connection conn, String taskId,String level, String logLevel,String logContent,String logAdds,String vSql){
		PreparedStatement pstmt = null;
		int t = 0;
		try {
			 t = executeInsert(conn, pstmt, taskId, level, logLevel, logContent, logAdds, vSql);
		} catch (Exception e) {
			System.out.println("日常异常");
			e.printStackTrace();
		}finally {
			JDBCUtil.closeResources(null, pstmt, null);
		}
		if( t != 1){
			System.out.println("Fail to insert log to SYM_TBS_TASK_RUN_LOG");
		}
	}

	private static int executeInsert(Connection conn,PreparedStatement pstmt , String taskId,String level, String logLevel,String logContent,String logAdds,String vSql) throws SQLException {
		Timestamp curr = new Timestamp(System.currentTimeMillis());
		pstmt = conn.prepareStatement(SQLStaicInfo.SQL_INSERT_TASK_LOG);
		pstmt.setString(1, taskId);
		pstmt.setTimestamp(2, curr);
		pstmt.setString(3, level);
		pstmt.setString(4, logLevel);
		pstmt.setString(5, logContent);
		pstmt.setString(6, logAdds);
		pstmt.setString(7, vSql);
		return pstmt.executeUpdate();
	}

}
