package com.ylink.aml.modular.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author qy
 */
@Repository
public interface ExecuteManageMapper {

    /**
     * 分页查询列表
     * @param page 分页参数
     * @param params 参数集合
     * @return 结果
     */
    Page<Map<String, Object>> list(Page page, @Param("params") Map<String, Object> params);

}
