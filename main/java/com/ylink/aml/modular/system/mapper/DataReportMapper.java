package com.ylink.aml.modular.system.mapper;

import com.ylink.aml.modular.system.entity.DataReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author qy
 */
@Repository
public interface DataReportMapper extends BaseMapper<DataReport> {

    /**
     * 查询一个日期
     * @return
     */
    Date findOneDate();
}
