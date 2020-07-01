package com.ylink.aml.modular.system.util;

import cn.hutool.core.util.StrUtil;
import com.ylink.aml.modular.system.dto.CheckFileDto;
import com.ylink.aml.modular.system.entity.TargetTabCol;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author jweicai
 * @date 2019/6/20 11:03
 * TODO: This class verifies that the data format of the data in the row matches the target table.
 */
@Slf4j
public class CheckLineUtils {

    /**
     * @param line              待校验数据行
     * @param listTargetTabCols 字段配置List
     * @param separator         分隔符
     * @return
     * @para hasQuotes  是否有数据引号
     */
    public static CheckFileDto checkOut(String line, List<TargetTabCol> listTargetTabCols, String separator, boolean hasQuotes, String ifCheckDict) {
        CheckFileDto dto =new CheckFileDto();
        dto.setBool(true);
        StringBuffer sb=new StringBuffer();
        sb.append("(");
        if (StringUtils.isNotEmpty(line) && StringUtils.isNotEmpty(separator)) {
            // 切分字段
            String[] fields = line.split(String.format("\\%s", separator), -1);

            StringBuilder argsBuilder = new StringBuilder();

            // 字段个数校验
            if (fields.length != listTargetTabCols.size()) {
                dto.setBool(false);
                sb.append("字段个数不匹配:目标表字段个数为"+listTargetTabCols.size()+",该行字段个数为"+fields.length+",");
                /*log.info("字段个数不匹配");
                dto.setBool(false);
                dto.setErrMess("(字段个数不匹配:目标表字段个数为"+listTargetTabCols.size()+",该行字段个数为"+fields.length+")");*/
                //return dto;
            } else {
                // 遍历每个字段
                for (int i = 0; i < fields.length; i++) {
                    // 取出字段
                    String field = fields[i];

                    //数据引号 "" 处理
                    if (field != null && hasQuotes) {
                        if (field.startsWith("\"")) {
                            field = field.substring(1);
                        }
                        if (field.endsWith("\"")) {
                            field = field.substring(0, field.length() - 1);
                        }
                    }

                    if (StringUtils.isNotEmpty(field)) {
                        TargetTabCol targetTabCol = listTargetTabCols.get(i);

                        String datatypes = targetTabCol.getDataType();// 数据类型匹配校验
                        if ("S".equals(datatypes)) {//字符类型校验

                        } else if ("N".equals(datatypes)) {// 数值类型校验
                            try {
                                // 调用方法str2Numble
                                BigDecimal bd = str2Numble(field, targetTabCol.getDataPrecision(), targetTabCol.getDataScale());
                                if (bd == null) {
                                    dto.setBool(false);
                                    sb.append("该行第"+(i + 1)+"字段:"+targetTabCol.getColumnName() + "数值型格式错误,");
                                    log.info("字段:"+targetTabCol.getColumnName() + "数值型格式错误");
                                    /*dto.setBool(false);
                                    dto.setErrMess("(该行字段:"+targetTabCol.getColumnName() + "数值型格式错误)");
                                    return dto;*/
                                }
                            } catch (NumberFormatException e) {
                                dto.setBool(false);
                                sb.append("该行第"+(i + 1)+"字段:"+targetTabCol.getColumnName() + "数值型格式错误,");
                                log.info("第" + (i + 1) + "个字段>>>>>>" + field + "<<<<<<数值型格式错误");
                                /*dto.setBool(false);
                                dto.setErrMess("(该行字段:"+targetTabCol.getColumnName() + "数值型格式错误)");
                                return dto;*/
                            }
                        } else {// 除了"S"和"N"之外的DATA类型"T"校验,其中还分为data和datatime
                            try {
                                String dateformat = targetTabCol.getDateFormat();
                                /*if (targetTabCol.getDateFormat().length() == 8) {
                                    dateformat = "yyyyMMdd";
                                } else if (dateformat.length() == 6) {
                                    dateformat = "HHmmss";
                                } else if (dateformat.length() == 14) {
                                    dateformat = "yyyyMMddHHmmss";
                                }*/
                                SimpleDateFormat format = new SimpleDateFormat(dateformat);
                                // String转Date
                                format.setLenient(false);
                                format.parse(field);
                            } catch (ParseException e) {
                                dto.setBool(false);
                                sb.append("该行第" + (i + 1) + "字段:"+targetTabCol.getColumnName() + "日期型格式错误,");
                                log.info("第" + (i + 1) + "个字段>>>"+targetTabCol.getColumnName() +">>>" + field + "<<<<<<日期型格式错误");
                               /* dto.setBool(false);
                                dto.setErrMess("(该行字段:"+targetTabCol.getColumnName() + "日期型格式错误)");
                                return dto;*/
                            }
                        }

                        //  字典校验
                        if (targetTabCol.getMapDictionaryVal() != null && ifCheckDict.equals("1")) {
                            if (!targetTabCol.getMapDictionaryVal().containsKey(field)) {
                                dto.setBool(false);
                                sb.append("该行第" + (i + 1) + "个字段:"+targetTabCol.getColumnName() + "字典型格式错误,");
                                log.info("第" + (i + 1) + "个字段>>>"+targetTabCol.getColumnName() +">>>" + field + "<<<<<<字典型格式错误");
                                /*dto.setBool(false);
                                dto.setErrMess("(该行字段:"+targetTabCol.getColumnName() + "字典型格式错误)");
                                return dto;*/
                            }
                        }
                    }

                }
            }

            //所有字段的格式都正确
            sb.append(")");
            //dto.setErrMess("该行字段:"+targetTabCol.getColumnName() + "字典型格式错误");
            dto.setErrMess(sb.toString());
            return dto;
        } else {
            //传入参数存在空值
            log.info("传入参数存在空值");
            dto.setBool(false);
            dto.setErrMess("(传入参数存在空值)");
            return dto;
        }
    }

    /**
     * TODO:数值类型校验
     *
     * @param value
     * @param length
     * @param scale
     * @return
     * @throws ParseException
     */
    private static BigDecimal str2Numble(String value, Integer length, Integer scale) throws NumberFormatException {
        // 构造以字符串内容为值的BigDecimal类型的变量bd
        BigDecimal bd = new BigDecimal(value);
        /*// 设置小数位数，第一个变量是小数位数，第二个变量是取舍方法(四舍五入)
        bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);*/
        // 取bd的精度

        // 与目标表中的字段长度进行对比
        if(length!=null && bd.precision()>length){
            return null;
        }

        if(scale!=null && bd.scale()>scale){
            return null;
        }

        return  bd;
    }

    /**
     * TODO:String to int.
     *
     * @param s
     * @return
     * @throws NumberFormatException
     */
    public static int str2Int(String s) {
        int num = 0;
        try {
            num = Integer.parseInt(s);
        } catch (NumberFormatException e) {

        }
        return num;
    }


    /**
     * TODO:集合是否为空
     *
     * @param collection 集合对象
     * @return 返回true or false
     */
    public static boolean isEmpty(Collection<?> collection) {
        if (collection == null || collection.size() == 0) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        BigDecimal bd = new BigDecimal("1000.0130");
        System.out.println(bd.scale());
    }
}
