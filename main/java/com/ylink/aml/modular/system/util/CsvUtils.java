package com.ylink.aml.modular.system.util;

import java.io.*;
import java.util.List;

/**
 * @author qy
 */
public class CsvUtils {

    /**
     * 导出文件
     * @param file 文件
     * @param dataList 数据
     * @return 是否成功 true or false
     */
    public static boolean exportCsv(File file, List<String> dataList) {
        //BufferedWriter errWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errFilePath), "UTF-8"));
        boolean isSuccess;
        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out);
            //bw =new BufferedWriter(osw);
            bw =new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            if(dataList!=null && !dataList.isEmpty()){
                for(String data : dataList){
                    bw.append(data).append("\r");
                }
            }
            isSuccess=true;
        } catch (Exception e) {
            isSuccess=false;
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw!=null){
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return isSuccess;
    }
}
