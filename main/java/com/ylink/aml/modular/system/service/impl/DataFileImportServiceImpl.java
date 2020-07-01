package com.ylink.aml.modular.system.service.impl;


import com.ylink.aml.core.common.page.LayuiPageFactory;
import com.ylink.aml.modular.system.entity.DataFileImport;
import com.ylink.aml.modular.system.mapper.DataFileImportMapper;
import com.ylink.aml.modular.system.mapper.DeleteInfoMapper;
import com.ylink.aml.modular.system.mapper.InsertBatchMapper;
import com.ylink.aml.modular.system.service.DataFileImportService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/21 15:43
 */
@AllArgsConstructor
@Service("DateFileImportService")
public class DataFileImportServiceImpl extends ServiceImpl<DataFileImportMapper, DataFileImport>  implements DataFileImportService {
    private DataFileImportMapper fileImportMapper;
    private InsertBatchMapper insertBatchMapper;
    private DeleteInfoMapper deleteInfoMapper;
    @Override
    public List <DataFileImport> selectFileByNameAndSize(String name, Long size) {
        List<DataFileImport> result = fileImportMapper.selectList(new QueryWrapper<DataFileImport>()
                .eq("ORI_PATH", name)
                .eq("ORI_SIZE", size)
        );
        return result;
    }

    @Override
    public int addDataInfo(DataFileImport fileImport) {
        return fileImportMapper.insert(fileImport);
    }

    @Override
    public boolean insertBatch(Map<String, String> map) {
        return insertBatchMapper.insertBatch(map);
    }

    //正确行插入目标对应表
    @Override
    public  boolean  insertBatch(String strSql){
        Map<String, String> map = new HashMap<>();
        map.put("sql",strSql);
       return   insertBatchMapper.insertBatch(map);
    }


    @Override
    public Page<Map<String, Object>> selectFileInfo(String tableName) {
        Page page = LayuiPageFactory.defaultPage();
        //Page<Map<String, Object>> p=fileImportMapper.selectFileInfo(page);

        return  fileImportMapper.selectFileInfo(page,tableName);
    }

    @Override
    public boolean deleteInfo(Map<String, String> map) {
        return deleteInfoMapper.deleteInfo(map);
    }
}
