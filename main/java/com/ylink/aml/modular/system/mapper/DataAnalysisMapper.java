package com.ylink.aml.modular.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataAnalysisMapper {

    Integer customCountSql(@Param("countSql") String countSql);

    List<Map<String, Object>> customListSql(@Param("listSql") String listSql);
}
