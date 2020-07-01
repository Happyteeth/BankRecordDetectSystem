package com.cfss.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 
 * @author Black
 * @version 0.0.1
 */
public class StringUtil {
	/**
	 * 
	 * 是否为空
	 * 
	 * @param str
	 *            String对象
	 * @return 返回true or false
	 */
	public static boolean isNull(String str) {
		if (str == null || str.equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 进行trim判断是否为空
	 * 
	 * @param str
	 *            String对象
	 * @return 返回true or false
	 */
	public static boolean isEmpty(String str) {
		boolean retFlag = isNull(str);
		if (!retFlag) {
			return str.trim().equals("");
		}
		return retFlag;
	}

	/**
	 * 获取32位全球唯一ID
	 */
	public static String getSID() {
		return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
	}

	/**
	 * 返回String对象的字节长度[GBK字符]
	 */
	public static int getGBKLen(String str) {
		try {
			if (!isNull(str)) {
				return str.getBytes("GBK").length;
			}
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
		}
		return 0;
	}

	/**
	 * 
	 * @description 截取指定字节长度字符
	 * @param str
	 *            原字符串
	 * @param len
	 *            长度
	 * @return 截取后的字符串
	 */
	public static String getFixStr(String str, int len) {
		if (len > 0 && len < getGBKLen(str)) {
			StringBuffer buff = new StringBuffer();
			char c;
			for (int i = 0; i < len; i++) {
				c = str.charAt(i);
				buff.append(c);
				if (getGBKLen(String.valueOf(c)) > 1) {
					--len;
				}
			}
			return buff.toString();
		}
		if (len == 0) {
			return "";
		}
		return str;
	}

	/**
	 * 
	 * @description 对字符串进行指定字节长度的格式化(不足长度右补空格左补0)
	 * 
	 * @param str
	 *            原字符串
	 * @param len
	 *            指定字节长度
	 * @param isZero
	 *            是否左补0
	 * @return 格式化后的字符串
	 */
	public static String formatStr(String str, int len, boolean isZero) {
		int curLen = getGBKLen(str);
		if (curLen > 0 && curLen > len) {
			return getFixStr(str, len);
		} else if (curLen >= 0 && curLen < len) {
			String fillStr = "";
			String flag = " ";
			if (isZero) {
				flag = "0";
			}
			int fillLen = len - curLen;
			for (int i = 0; i < fillLen; i++) {
				fillStr += flag;
			}
			if (curLen == 0) {
				return fillStr;
			}
			if (isZero) {
				return fillStr + str;
			} else {
				return str + fillStr;
			}
		} else {
			return str;
		}
	}

	/**
	 * 验证是否为数字
	 * 
	 * @param str
	 *            输入的字符串
	 * @return true:是，false:不是
	 */
	public static boolean isDigits(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher match = pattern.matcher(str);
		if (match.matches() == false) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 验证字符串是否为正整数或者带小数点的数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isFloatDigits(String str) {
		Pattern pattern = Pattern
				.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
		Matcher match = pattern.matcher(str);
		if (match.matches() == false) {
			return false;
		} else {
			return true;
		}
	}

	// 判断手机号
	public static boolean isMobileNo(String mobiles) {
		Pattern p = Pattern.compile("^[1]{1}\\d{10}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/**
	 * 
	 * 将长度转换成16进制
	 * 
	 * @param len
	 *            长度
	 * @return 16进制字符串
	 */
	public static String getLenHexStr(int len) {
		StringBuffer buf = new StringBuffer(2);
		if (((int) len & 0xff) < 0x10) {
			buf.append("0");
		}
		buf.append(Long.toString((int) len & 0xff, 16));
		return buf.toString();
	}

	/**
	 * 
	 * 将字节数组转换成16进制字符串
	 * 
	 * @param byteArray
	 *            字节数组
	 * @return 16进制字符串
	 */
	public static String byteToHexString(byte[] byteArray)
	{
		StringBuffer strBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				strBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else {
				strBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}
		}
		return strBuff.toString().toUpperCase();
	}
	
	/**
	 * 
	 * 将16进制字符串转换成字节数组
	 * 
	 * @param hexString
	 *            6进制字符串
	 * @return 字节数组
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String getExceptionToString(Throwable e) {
		if (e == null){
			return "";
		}
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
//    if(stringWriter.getBuffer().length() > 4000){
//       return stringWriter.getBuffer().substring(0, 3999);
//    }else{
		return stringWriter.getBuffer().toString();
//    }
	}
}
