package com.ylink.aml.hive;


import cn.stylefeng.roses.core.reqres.response.ResponseData;

/**
 * @author jiang
 * @date 2019/7/25 11:34
 */
public interface DeleteDataInner {

    /**
     * TODO: 当用户需要删除整条记录时（只有程序异常或程序执行完成才能删除），删除程序的临时文件以及结果文件，包括：
     * 1、web服务器上的文件
     * 2、HDFS上的临时文件（原始文件，校验成功的文件，校验失败的文件）
     * 3、hive上的表数据(目标表)
     * @param file_id
     *          要删除的文件id
     * @param table_name
     *          要删除的文件导入的目标表名
     * @param ori_path
     *          web上的原始路径
     * @param hdfsUri （eg: 'hdfs://172.168.200.24:8020'）
     *          hdfs的ip端口
     * @param hdfs_dir
     *          上传到hdfs的路径
     * @param check_suc_dir
     *          校验成功的文件路径
     * @param check_err_dir
     *          校验失败的文件路径
     * @param url (eg:'jdbc:hive2://172.168.200.24:10000/aml')
     *          hive url
     * @return
     */
    ResponseData clean(int file_id, String table_name, String ori_path, String hdfsUri, String hdfs_dir, String check_suc_dir, String check_err_dir, String url);

    boolean deldeteWeb(String path);

    boolean deleteHdfs(String hdfsPath,String hdfsUri);

    boolean deldeteHive(String driver,String url, String sql);
}
