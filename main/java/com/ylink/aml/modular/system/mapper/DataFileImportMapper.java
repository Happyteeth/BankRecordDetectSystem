package com.ylink.aml.modular.system.mapper;


import com.ylink.aml.modular.system.entity.DataFileImport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface DataFileImportMapper extends BaseMapper<DataFileImport> {
    Page<Map<String, Object>> selectFileInfo(Page page, @Param("tableName") String tableName);
}
