/**
 * DateUtil.java
 * 创建:Black 2019-05-05
 */
package com.cfss.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间工具栏
 * 
 * @author Black
 * @version 0.0.1
 */
public class DateUtil {
	public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * 获取延迟后的日期--日表
	 * 
	 * @param delayDate
	 * @return
	 */
	public static String getDataDateByDelayDateDay(String delayDate) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date nowDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nowDate);

		Integer delayDateInt = Integer.valueOf("-" + delayDate);
		calendar.add(Calendar.DATE, delayDateInt);
		Date dataDate = calendar.getTime();

		return sdf.format(dataDate);

	}

	/**
	 * 获取延迟后的日期--月表
	 * 
	 * @param delayDate
	 * @return
	 */
	public static String getDataDateByDelayDateMonth(String delayDate) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date nowDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nowDate);

		Integer delayDateInt = Integer.valueOf("-" + delayDate);
		calendar.add(Calendar.DATE, delayDateInt);
		Date dataDate = calendar.getTime();

		Calendar calendarMonth = Calendar.getInstance();
		calendarMonth.setTime(dataDate);
		calendar.add(Calendar.MONTH, -1);
		Date dataDateMonth = calendar.getTime();

		return sdf.format(dataDateMonth) + "-01";

	}

	/**
	 * 日期不为空时获取月表日期--月表
	 * 
	 * @param dataDate
	 * @return
	 * @throws ParseException 
	 */
	public static String getDataDateByDataDateMonth(String dataDate) throws ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		Date date = simpleDateFormat.parse(dataDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, -1);
		Date dataDateMonth = calendar.getTime();
		return format.format(dataDateMonth) + "-01";
	}

	/**
	 * 比较两个日期大小
	 * @param dataDate
	 * @param dataRunLastDate
	 * @return
	 * @throws ParseException
	 */
	public static boolean compareDate(String dataDate, String dataRunLastDate) throws ParseException {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date dt1 = df.parse(dataDate);
		Date dt2 = df.parse(dataRunLastDate);

		if (dt1.getTime() > dt2.getTime()) {
			return true;
		} else {
			return false;

		}
	}
	
	/**
	 * 是否比当前时间小与differ个小时
	 * @param dataDate
	 * @param differ
	 * @return
	 * @throws ParseException
	 */
	public static boolean compareDateAndNowDiffer(String dataDate, int differ) throws ParseException {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date dt1 = new Date();
		Date dt2 = df.parse(dataDate);
		
		if ((dt1.getTime() - dt2.getTime())/(1000*60*60) > differ) {
			return true;
		} else {
			return false;
			
		}
	}

	/**
	 * 获取两个时间段内所有月份 包含起始
	 * @param minDate
	 * @param maxDate
	 * @return
	 * @throws ParseException
	 */
	public static List<String> getMonthBetween(String minDate, String maxDate) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化为年月

		Calendar min = Calendar.getInstance();
		Calendar max = Calendar.getInstance();

		min.setTime(sdf.parse(minDate));
		min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

		max.setTime(sdf.parse(maxDate));
		max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

		Calendar curr = min;
		while (curr.before(max)) {
			result.add(sdf.format(curr.getTime()));
			curr.add(Calendar.MONTH, 1);
		}

		return result;
	}

	/**
	 * 
	 * 获取当天yyyy-MM-dd格式字符串
	 * 
	 * @return yyyy-MM-dd当天的格式字符串
	 */
	public static String getNowLongStr() {
		return format(getNowDate(), "yyyy-MM-dd");
	}

	/**
	 * 
	 * 获取当天日期
	 * 
	 * @return 当天日期
	 */
	public static Date getNowDate() {
		return new Date();
	}

	/**
	 * 
	 * 获取指定日期格式的日期字符串
	 * 
	 * @param date
	 *            日期对象
	 * @param pattern
	 *            格式字符串
	 * @return 指定日期格式的日期字符串
	 */
	public static String format(Date date, String pattern) {
		if ((date == null) || (StringUtil.isNull(pattern)))
			return null;
		String dateStr = null;
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			dateStr = simpleDateFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateStr;
	}

	/**
	 * 
	 * 返回间隔时间，单位分钟
	 * 
	 * @param startDate
	 *            开始日期
	 * @return 间隔时间
	 */
	public static long getIntervalMin(Date startDate) {
		return (getNowDate().getTime() - startDate.getTime()) / (1000 * 60);
	}

	/**
	 * 
	 * 返回间隔时间，单位秒
	 * 
	 * @param startTime
	 *            开始时间,System.nanoTime()的结果
	 * @return 间隔时间
	 */
	public static long getIntervalSecond(long startTime) {
		return (System.nanoTime() - startTime) / 1000000000;
	}

	/**
	 * 
	 * 返回间隔时间，单位毫秒
	 * 
	 * @param startTime
	 *            开始时间,System.nanoTime()的结果
	 * @return 间隔时间
	 */
	public static long getIntervalMilliSecond(long startTime) {
		return (System.nanoTime() - startTime) / 1000000;
	}

	/**
	 * 
	 * 获取当前日期的所属field的第几天日期
	 * 
	 * @param date
	 *            当前日期
	 * @return 日期
	 */
	public static Date getFieldSetDate(Date date, int field, int value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(field, value);
		return calendar.getTime();
	}

	/**
	 * 
	 * 获取当前日期的所属field的相减日期
	 * 
	 * @param date
	 *            当前日期
	 * @return 日期
	 */
	public static Date getFieldAddDate(Date date, int field, int value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(field, value);
		return calendar.getTime();
	}

	public static long convertTimeStamp2Long(Timestamp t){
		if( t == null){
			return 0L;
		}
		return t.getTime();
	}

	public static String getTimeStampofString(Timestamp timestamp){
		if(timestamp == null){
			return "NULL";
		}
		return simpleDateFormat.format(timestamp);
	}
}
