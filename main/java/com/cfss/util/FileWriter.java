package com.cfss.util;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class FileWriter {
    public static final Log LOG = LogFactory.getLog(FileWriter.class.getName());


    public String filePath;
    //public java.io.FileWriter writer;
    private FileOutputStream fileout = null; //new FileOutputStream(file)
    private OutputStreamWriter out = null;

    public FileWriter(String filePath){
        this.filePath = filePath;
//        this.init();

    }

    public boolean init() {
        File file = new File(this.filePath);
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            this.fileout = new FileOutputStream(file);
            //
            this.out = new OutputStreamWriter(this.fileout,"UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            LOG.error("Fail to init FileWrite. file path = " + this.filePath + ". Exception: " + e);
            return false;
        }
        return true;
    }

    /**
     * 用于写文件
     * @param content
     */
    public void writerFile(String content){
        try{
            this.out.write(content);
            this.out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void close() {
        this.close(this.fileout);
        this.close(this.out);
    }

    public void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
                closeable = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
