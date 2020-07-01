package com.ylink.aml.modular.system.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.entity.*;
import com.ylink.aml.modular.system.mapper.*;
import com.ylink.aml.modular.system.model.ModelDto;
import com.ylink.aml.modular.system.model.ModelTreeNode;
import com.ylink.aml.modular.system.model.ModelvalDto;
import com.ylink.aml.modular.system.model.TreeNode;
import com.ylink.aml.modular.system.util.TreeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@AllArgsConstructor
public class RuleDefService extends ServiceImpl<RuleDefMapper, RuleDef> {
    private RuleParaDefMapper ruleParaDefMapper;
    private TbsDictionaryValMapper tbsDictionaryValMapper;
   private RuleTargetMappingMapper ruleTargetMappingMapper;
    /**
     * 模型 item_Id
     */
    public  static final  String MODEL_ITEM_ID ="P1001";

    //模型默认分类ID
    private static final  String MODEL_DEFAULT_TYPE_ID="999999999";
    private static final  String MODEL_DEFAULT_TYPE_ID2="9999999990";

    //数据分析和模型规则配置表  字段配置
    private static Map<String, String> modFieldMap = new LinkedHashMap();

    //数据分析和模型执行参数 字段配置
    private static Map<String, String> ruleParaFieldMap = new LinkedHashMap();

    private static Map<String, String> ruleTargetFieldMap = new LinkedHashMap();
    static {
        // 数据分析和模型规则配置表
        modFieldMap.put("ruleId", "规则id");
        modFieldMap.put("ruleName", "模型名称");
        modFieldMap.put("modelDesc", "模型描述");
        modFieldMap.put("ruleWeb", "页面配置项");
        modFieldMap.put("taskProgram", "规则检测执行语言");
        modFieldMap.put("ruleProg", "实际检测的SQL或程序");
        modFieldMap.put("chartType", "图标的样式");
        modFieldMap.put("chartProg", "图表SQL或程序");
        modFieldMap.put("ruleType", "规则分类");
        modFieldMap.put("modelSource", "模型来源");
        modFieldMap.put("modelType1ItemId", "模型分类第一级id");
        modFieldMap.put("modelType2ItemId", "模型分类第二级id");
        modFieldMap.put("cDelFlag", "删除标志");
        modFieldMap.put("dInsert", "操作时间（新增）");
        modFieldMap.put("dUpdate", "操作时间（最后修改）");
        modFieldMap.put("vInsertUser", "操作人（新增）");
        modFieldMap.put("vUpdateUser", "操作人（最后修改）");
        modFieldMap.put("modelType1ItemValId", "模型分类第一级值id");
        modFieldMap.put("modelType2ItemValId", "模型分类第二级值id");
        modFieldMap.put("modelType2ItemValId", "模型分类第二级值id");
        modFieldMap.put("ifAutoCheck", "是否参与一键检测");
        modFieldMap.put("ruleCount","检测的数据量SQL或程序");
        //数据分析和模型执行参数
        ruleParaFieldMap.put("ruleId", "规则id");
        ruleParaFieldMap.put("paraString", "参数名字符串");
        ruleParaFieldMap.put("paraDesc", "参数名说明");
        ruleParaFieldMap.put("paraValue", "参数值");
        ruleParaFieldMap.put("dInsert", "操作时间（新增）");
        ruleParaFieldMap.put("dUpdate", "操作时间（最后修改）");
        ruleParaFieldMap.put("vInsertUser", "操作人（新增）");
        ruleParaFieldMap.put("vUpdateUser", "操作人（最后修改）");

        //模型与目标关系信息
        ruleTargetFieldMap.put("ruleId","规则id");
        ruleTargetFieldMap.put("tableName","目标表名");
    }

    /**
     * 获取表头 字段对应位置map
     *
     * @param mapFields
     * @param listHead
     */
    private Map<Integer, String> getHeadFieldMatchMap(Map<String, String> mapFields, List<Object> listHead) {
        Map<String, String> mapModFiledDef = IterUtil.toMap(mapFields.values(), mapFields.keySet());
        Map<Integer, String> mapHeadFiled = MapUtil.newHashMap(); //表格字段对应 字段名
        int size = listHead.size();
        for (int i = 0; i < size; i++) {
            String str = String.valueOf(listHead.get(i));
            if (mapModFiledDef.containsKey(str)) {
                mapHeadFiled.put(i, mapModFiledDef.get(listHead.get(i)));
            }
        }
        return mapHeadFiled;
    }


    /**
     * 模型导入
     *
     * @param reader
     * @return
     * @throws Exception
     */
    public int batchImport(ExcelReader reader) throws Exception {
        String userName = ShiroKit.getUser().getName(); //当前用户

        //第一个sheet  模型列表
        reader.setSheet(0);
        List<List<Object>> readAll = reader.read();

        //匹配第一行字段中文名称、英文名称
        List<Object> listModHead = readAll.get(0);

        Map<Integer, String> mapModInfoFiled = getHeadFieldMatchMap(modFieldMap, listModHead); //表格字段对应字段名

        //查询数据库现所有模型记录
        List<RuleDef> modList = baseMapper.selectList(new QueryWrapper<>());
        Map<String, RuleDef> mapModelLog = modList.stream().collect(Collectors.toMap(RuleDef::getRuleId, a -> a, (k1, k2) -> k1));

        int rowSize = readAll.size();
        Map<String, String> mapImpModel = MapUtil.newHashMap(rowSize); //导入模型记录
        for (int i = 1; i < rowSize; i++) {
            List<Object> ob = readAll.get(i);
            RuleDef model = new RuleDef();
            Map<String, String> mapValue = MapUtil.newHashMap(mapModInfoFiled.size());
            for (Map.Entry<Integer, String> entry : mapModInfoFiled.entrySet()) {
                mapValue.put(entry.getValue(), String.valueOf(ob.get(entry.getKey())));
            }
            BeanUtil.copyProperties(mapValue, model);

            long curTime = System.currentTimeMillis();
            model.setVUpdateUser(userName); //修改用户
            model.setDUpdate(new Timestamp(curTime));//修改时间

            //重复，则更新
            if (mapModelLog.containsKey(model.getRuleId())) {
                //throw new Exception("请修改数据中id，有id已经存在数据库中！");
                baseMapper.updateById(model);
            } else {
                model.setVInsertUser(userName);  //插入用户
                model.setDInsert(new Timestamp(curTime)); //插入时间
                baseMapper.insert(model);
            }

            mapImpModel.put(model.getRuleId(), model.getRuleId()); //添加到导入模型记录
        }

        //第二个sheet  模型参数 导入
        reader.setSheet(1);
        List<List<Object>> readLlist = reader.read();
        //匹配第一行字段中文名称、英文名称
        List<Object> listParaHead = readLlist.get(0);
        Map<Integer, String> mapModParaFiled = getHeadFieldMatchMap(ruleParaFieldMap, listParaHead); //表格字段对应字段名

        for (int i = 1; i < readLlist.size(); i++) {
            List<Object> ob = readLlist.get(i);
            if(ob!=null){
                //删除将要导入模型参数记录
                for (Map.Entry<String, String> entry : mapImpModel.entrySet()) {
                    RuleParaDef ruleParaDef = new RuleParaDef();
                    ruleParaDef.setRuleId(entry.getKey());
                    ruleParaDefMapper.delete(new QueryWrapper<>(ruleParaDef));
                }
            RuleParaDef ruleParaDef = new RuleParaDef();

            Map<String, String> mapValue = MapUtil.newHashMap(mapModParaFiled.size());
            for (Map.Entry<Integer, String> entry : mapModParaFiled.entrySet()) {
                mapValue.put(entry.getValue(), String.valueOf(ob.get(entry.getKey())));
            }
            BeanUtil.copyProperties(mapValue, ruleParaDef);

            //判断是否存在
            if (!mapImpModel.containsKey(ruleParaDef.getRuleId())) {
                continue;
            }
            long curTime = System.currentTimeMillis();
            ruleParaDef.setVUpdateUser(userName); //修改用户
            ruleParaDef.setDUpdate(new Timestamp(curTime));//修改时间
            ruleParaDef.setVInsertUser(userName);  //插入用户
            ruleParaDef.setDInsert(new Timestamp(curTime)); //插入时间

            ruleParaDefMapper.insert(ruleParaDef);
        }}

        //第三个sheet  模型参数 导入
        reader.setSheet(2);
        List<List<Object>> reaLlist = reader.read();
        //匹配第一行字段中文名称、英文名称
        List<Object> listTargetHead = reaLlist.get(0);

        Map<Integer, String> mapModtargetFiled = getHeadFieldMatchMap(ruleTargetFieldMap, listTargetHead); //表格字段对应字段名

        for (int i = 1; i < reaLlist.size(); i++) {
            List<Object> ob = reaLlist.get(i);
            if(ob!=null){
                //删除关联表中信息
                for (Map.Entry<String, String> entry : mapImpModel.entrySet()) {
                    RuleTargetMapping ruleTarget = new RuleTargetMapping();
                    ruleTarget.setRuleId(entry.getKey());
                    ruleTargetMappingMapper.delete(new QueryWrapper<>(ruleTarget));
                }
            RuleTargetMapping ruleTargetMapping = new RuleTargetMapping();

            Map<String, String> mapValue = MapUtil.newHashMap(mapModtargetFiled.size());
            for (Map.Entry<Integer, String> entry : mapModtargetFiled.entrySet()) {
                mapValue.put(entry.getValue(), String.valueOf(ob.get(entry.getKey())));
            }
            BeanUtil.copyProperties(mapValue, ruleTargetMapping);

            //判断是否存在
            if (!mapImpModel.containsKey(ruleTargetMapping.getRuleId())) {
                continue;
            }

            ruleTargetMappingMapper.insert(ruleTargetMapping);
        }}
        return mapImpModel.size();
    }


    /**
     * 模型导出
     *
     * @param response
     * @throws Exception
     */
    public void Excellistpay(HttpServletResponse response) throws Exception {
        //模型基本信息
        List<RuleDef> sellerListP = baseMapper.findexcellist();

        ExcelWriter writer = new ExcelWriter(false, "模型基本信息");
        //自定义标题别名
        writer.setHeaderAlias(modFieldMap);

        writer.write(sellerListP, true);

        //设置所有列为自动宽度，不考虑合并单元格
        writer.autoSizeColumn((short) 0);

        //模型参数信息
        List<RuleParaDef> sellerListE = ruleParaDefMapper.findByexcel();
        writer.setSheet("模型参数信息");
        //自定义标题别名
        writer.setHeaderAlias(ruleParaFieldMap);
        writer.write(sellerListE, true);
        //设置所有列为自动宽度，不考虑合并单元格
        writer.autoSizeColumn((short) 1);

        List<RuleTargetMapping> listF=ruleTargetMappingMapper.selectList(new QueryWrapper<>());
        writer.setSheet("模型和目标关系信息");
        writer.setHeaderAlias(ruleTargetFieldMap);
        writer.write(listF, true);
        //设置所有列为自动宽度，不考虑合并单元格
        writer.autoSizeColumn((short) 2);
        if(sellerListP==null && sellerListE ==null && listF==null){
            log.error("数据为空");
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        String codedFileName = java.net.URLEncoder.encode("Model_" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT), "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        writer.flush(response.getOutputStream());
        // 关闭writer，释放内存
        writer.close();
    }


    public RuleDef selectById(String ruleId) {
        return baseMapper.selectById(ruleId);
    }

    public List<RuleDef> selectModels() {
            List<RuleDef> models = this.baseMapper.selectList(new QueryWrapper<RuleDef>()
                    .eq("IF_AUTO_CHECK", "1")
                    .eq("RULE_TYPE","2")
                    .eq("C_DEL_FLAG", "0").orderByAsc("SHOW_ORDER").isNotNull("RULE_NAME"));
            return models;

    }


    public List<RuleDef> selectList() {
        List<RuleDef> models = this.baseMapper.selectList(new QueryWrapper<RuleDef>()
                .eq("RULE_TYPE","2")
                .eq("C_DEL_FLAG", "0").orderByAsc("SHOW_ORDER"));
        return models;
    }
}