package com.ylink.aml.modular.system.service;

import com.ylink.aml.modular.system.entity.DataFileImport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface DataFileImportService extends IService<DataFileImport> {

    //根据文件名和大小查找文件文件信息
    List<DataFileImport> selectFileByNameAndSize(String name, Long size);

    //将数据插入到文件数据导入表
    int addDataInfo(DataFileImport fileImport);

    //正确行插入目标对应表
    boolean insertBatch(Map<String, String> map);

    //正确行插入目标对应表
    boolean  insertBatch(String strSql);

    Page<Map<String, Object>> selectFileInfo(String tableName);

    //删除选择的文件
    boolean deleteInfo(Map<String, String> map);
}
