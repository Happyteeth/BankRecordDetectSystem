package com.ylink.aml.hive;

import cn.stylefeng.roses.core.reqres.response.ResponseData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.net.URI;
import java.sql.*;
import java.util.*;

import static com.ylink.aml.hive.DelUtil.closeResources;

/**
 * TODO: 当用户需要删除整条记录时（只有程序异常或程序执行完成才能删除），删除程序的临时文件以及结果文件，包括：
 * 1、web服务器上的文件
 * 2、HDFS上的临时文件（原始文件，校验成功的文件，校验失败的文件）
 * 3、hive上的表数据(目标表)
 *
 * @author jiang
 * @date 2019/7/25 11:35
 */
public class DeleteData implements  DeleteDataInner {

    private static Configuration conf = new Configuration();

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    static {
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
    }


    public static void main(String[] args) throws Exception {
        DeleteData getFilePath = new DeleteData();
        int file_id = 91;

        String drive = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://172.168.200.20/AMLBDTest";
        String username = "root";
        String password = "123456";
        String import_table = "DATA_FILE_IMPORT";
        Connection connection = DelUtil.getConnection(drive, url, username, password);

        Map<String, String> filePath = DelUtil.getFilePath(connection, file_id, import_table);
        String table_name = filePath.get("TABLE_NAME");
        String ori_path = filePath.get("ORI_PATH");

        Map<String, String> progMap = DelUtil.getFilePath(connection);
        // 文件上传到HDFS的目录
        String hdfs_dir = progMap.get("HDFS_DIR");
        // 格式校验成功的文件存放路径
        String check_suc_dir = progMap.get("CHECK_SUC_DIR");
        // 格式校验失败的文件存放路径
        String check_err_dir = progMap.get("CHECK_ERR_DIR");
        String hdfsUri = "hdfs://172.168.200.24:8020";
        String hive_url = "jdbc:hive2://172.168.200.24:10000/aml";
        getFilePath.clean(file_id, table_name, ori_path, hdfsUri, hdfs_dir, check_suc_dir, check_err_dir, hive_url);

    }

    /**
     * TODO: 清除所有的临时数据和目标文件
     *
     * @param file_id
     * @param table_name
     * @param ori_path
     * @param hdfsUri
     * @param hdfs_dir
     * @param check_suc_dir
     * @param check_err_dir
     * @param hive_url
     * @return
     */
    @Override
    public ResponseData clean(int file_id, String table_name, String ori_path, String hdfsUri, String hdfs_dir, String check_suc_dir, String check_err_dir, String hive_url) {
        /** 删除web上的数据 */
        boolean ifDeldeteWeb = deldeteWeb(ori_path);
        if (ifDeldeteWeb) {
            System.out.println("delete web data successful!");
        } else {
            System.out.println("delete web data failed!");
            return ResponseData.error("delete web data failed!");
        }

       /** 删除hdfs上的数据 */
        // 删除上传到hdfs上的文件
        String del_hdfs = hdfs_dir + "/H_" + file_id;
        boolean del_hdfs_dir = deleteHdfs(del_hdfs,hdfsUri);
        if (del_hdfs_dir) {
            System.out.println("delete hdfs_dir data successful!");
        } else {
            System.out.println("delete hdfs_dir data failed!");
            return ResponseData.error("delete hdfs_dir data failed!");
        }

       // 删除校验成功的文件
        String del_check_suc = check_suc_dir + "/H_" + file_id;
        boolean del_check_suc_dir = deleteHdfs(del_check_suc,hdfsUri);
        if (del_check_suc_dir) {
            System.out.println("delete check_suc_dir data successful!");
        } else {
            System.out.println("delete check_suc_dir data failed!");
            return ResponseData.error("delete check_suc_dir data failed!");
        }

        // 删除校验失败的文件
        String del_check_err = check_err_dir + "/" + file_id + "_ERR";
        boolean del_check_err_dir = deleteHdfs(del_check_err,hdfsUri);
        if (del_check_err_dir) {
            System.out.println("delete check_err_dir data successful!");
        } else {
            System.out.println("delete check_err_dir data failed!");
            return ResponseData.error("delete check_err_dir data failed!");
        }

        /** 删除hive上的数据*/
        String sql_target = "alter table " + table_name + " drop partition (file_id ='" + file_id + "')";
        boolean deleteHive= deldeteHive(driverName, hive_url, sql_target);
        if (deleteHive) {
            System.out.println("delete hive data successful!");
        } else {
            System.out.println("delete hive data failed!");
            return ResponseData.error("delete hive data failed!");
        }
        return ResponseData.success();
    }

    public boolean deldeteWeb(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("文件不存在！");
            return flag;
        }
        try {
            flag = file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    public boolean deleteHdfs(String hdfsPath, String hdfsUri) {
        FileSystem hdfs = null;
        boolean flag = false;
        try {
            hdfs = FileSystem.get(new URI(hdfsUri), conf, "hdfs");//加载文件系统实例
            Path path = new Path(hdfsPath);
            if (hdfs.exists(path)) {
                System.out.println("Begin to delete hdfs file -- " + hdfsPath);
                flag = hdfs.delete(path, true);
            }
            System.out.println("文件不存在！");
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
            return flag;
        } finally {
            DelUtil.closeSocket(hdfs);
        }
    }



    public boolean deldeteHive(String driver, String url, String sql) {
        System.out.println(sql);
        Connection conn = null;
        Statement statement = null;

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url,"hdfs","");
            statement = conn.createStatement();
            // <code>true</code> if the first result is a <code>ResultSet</code>
            // object; <code>false</code> if it is an update count or there are no results
            statement.execute(sql);
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, statement, null);
        }
    }
}
