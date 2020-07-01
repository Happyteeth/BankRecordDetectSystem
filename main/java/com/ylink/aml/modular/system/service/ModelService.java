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
import com.cfss.util.StringUtil;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.entity.*;
import com.ylink.aml.modular.system.mapper.*;
import com.ylink.aml.modular.system.model.ModelDto;
import com.ylink.aml.modular.system.model.ModelTreeNode;
import com.ylink.aml.modular.system.model.ModelvalDto;
import com.ylink.aml.modular.system.model.TreeNode;
import com.ylink.aml.modular.system.util.StringUtils;
import com.ylink.aml.modular.system.util.TreeUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ModelService extends ServiceImpl<ModelMapper, Model> {
    private RuleParaDefMapper ruleParaDefMapper;
    private TbsDictionaryValMapper tbsDictionaryValMapper;
    private RuleTargetMappingMapper ruleTargetMappingMapper;
    private RuleDefService ruleDefService;
    /**
     * 模型 item_Id
     */
    public static final String MODEL_ITEM_ID = "P1001";

    //模型默认分类ID
    private static final String MODEL_DEFAULT_TYPE_ID = "999999999";
    private static final String MODEL_DEFAULT_TYPE_ID2 = "9999999990";

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
        ruleTargetFieldMap.put("ruleId", "规则id");
        ruleTargetFieldMap.put("tableName", "目标表名");
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
        List<Model> modList = baseMapper.selectList(new QueryWrapper<>());
        Map<String, Model> mapModelLog = modList.stream().collect(Collectors.toMap(Model::getRuleId, a -> a, (k1, k2) -> k1));

        int rowSize = readAll.size();
        Map<String, String> mapImpModel = MapUtil.newHashMap(rowSize); //导入模型记录
        for (int i = 1; i < rowSize; i++) {
            List<Object> ob = readAll.get(i);
            Model model = new Model();
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

        //删除将要导入模型参数记录
        for (Map.Entry<String, String> entry : mapImpModel.entrySet()) {
            RuleParaDef ruleParaDef = new RuleParaDef();
            ruleParaDef.setRuleId(entry.getKey());
            ruleParaDefMapper.delete(new QueryWrapper<>(ruleParaDef));
        }

        //第二个sheet  模型参数 导入
        reader.setSheet(1);
        List<List<Object>> readLlist = reader.read();
        //匹配第一行字段中文名称、英文名称
        List<Object> listParaHead = readLlist.get(0);
        Map<Integer, String> mapModParaFiled = getHeadFieldMatchMap(ruleParaFieldMap, listParaHead); //表格字段对应字段名

        for (int i = 1; i < readLlist.size(); i++) {
            List<Object> ob = readLlist.get(i);
            if (ob != null) {
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
            }
        }

        //第三个sheet  模型参数 导入
        reader.setSheet(2);
        List<List<Object>> reaLlist = reader.read();
        //匹配第一行字段中文名称、英文名称
        List<Object> listTargetHead = reaLlist.get(0);

        Map<Integer, String> mapModtargetFiled = getHeadFieldMatchMap(ruleTargetFieldMap, listTargetHead); //表格字段对应字段名

        for (int i = 1; i < reaLlist.size(); i++) {
            List<Object> ob = reaLlist.get(i);
            if (ob != null) {
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
            }
        }
        return mapImpModel.size();
    }


    /**
     * 模型导出
     *
     * @param response
     * @throws Exception
     */
    public void Excellist(HttpServletResponse response) throws Exception {
        //模型基本信息
        List<Model> sellerListP = baseMapper.findexcellist();

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

        List<RuleTargetMapping> listF = ruleTargetMappingMapper.selectList(new QueryWrapper<>());
        writer.setSheet("模型和目标关系信息");
        writer.setHeaderAlias(ruleTargetFieldMap);
        writer.write(listF, true);
        //设置所有列为自动宽度，不考虑合并单元格
        writer.autoSizeColumn((short) 2);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        String codedFileName = java.net.URLEncoder.encode("Model_" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT), "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + codedFileName + ".xls");
        writer.flush(response.getOutputStream());
        // 关闭writer，释放内存
        writer.close();
    }


    public Model seletById(Model model) {
        return baseMapper.selectById(model);
    }


    public void delete(String ruleId) {
        baseMapper.deleteById(ruleId);
    }


    public ModelDto selectModelV(String ruleId) {
        return baseMapper.selectModelV(ruleId);
    }

    public void updateModel(String modelType1ItemValId, String modelType1ItemId, String ruleId, String ruleName) {
        baseMapper.updateModel(modelType1ItemValId, modelType1ItemId, ruleId, ruleName);
    }


    public List<ModelDto> selectModelt(String ruleId) {
        return baseMapper.selectModelt(ruleId);
    }

    public Model selectById(String ruleId) {
        return baseMapper.selectById(ruleId);
    }

    public List<ModelvalDto> selectval() {
        return baseMapper.selectval();
    }


    public List<Model> selectModels() {
        List<Model> models = this.baseMapper.selectList(new QueryWrapper<Model>()
                .eq("IF_AUTO_CHECK", "1")
                .eq("RULE_TYPE", "2")
                .eq("C_DEL_FLAG", "0").isNotNull("RULE_NAME").orderByAsc("SHOW_ORDER"));
        return models;
    }


    /**
     * 构建模型基础树
     *
     * @param listDictVal 字典项List
     * @return
     */
    public List<TreeNode> bulidModelBaseTree(List<TbsDictionaryVal> listDictVal, Map<String, TreeNode> mapTreeNode) {
        if (listDictVal == null || listDictVal.size() == 0) {
            return null;
        }

        List<TreeNode> treeList = listDictVal.stream()
                .filter(val -> !val.getVItemValId().equals(val.getVItemValIdPar()))
                .map(val -> {
                    TreeNode node = new TreeNode();
                    node.setId(val.getVItemValId());
                    node.setTitle(val.getVValName());
                    node.setParentId(val.getVItemValIdPar());
                    if (StrUtil.isBlank(node.getParentId())) {
                        node.setParentId("0");
                    }
                    return node;
                }).collect(Collectors.toList());

        if (mapTreeNode != null) {
            for (TreeNode node : treeList) {
                mapTreeNode.put(node.getId(), node);
            }
        }

        return TreeUtil.bulid(treeList, "0");
    }


    /**
     * 获取模型树，基础树+ 模型
     *
     * @return
     */
    public List<TreeNode> getModelTree(String ruleName, boolean autoCheck, boolean filterEmpty) {
        TbsDictionaryVal dictVal = new TbsDictionaryVal();
        dictVal.setVItemId(MODEL_ITEM_ID);
        dictVal.setCDelFlag("0");

        List<TbsDictionaryVal> listDictVal = tbsDictionaryValMapper.selectList(new QueryWrapper<TbsDictionaryVal>(dictVal).orderByAsc("N_ORDERID"));

        Map<String, TreeNode> mapTreeNode = CollUtil.newHashMap();
        List<TreeNode> listTreeNode = bulidModelBaseTree(listDictVal, mapTreeNode);
        if (listTreeNode == null) {
            listTreeNode = new ArrayList<TreeNode>();
        }
        TreeNode defaultNode = null; //默认未归类的分类

        List<RuleDef> listModel = ruleDefService.selectList();
        //把模型插入目录树对应节点中
        for (RuleDef ruleDef : listModel) {
            if (StringUtil.isNull(ruleDef.getModelType2ItemValId())){
                //获取模型在模型目录位置
                TreeNode node = mapTreeNode.get(ruleDef.getModelType1ItemValId());
                if (node == null) {
                    // node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                    if (defaultNode == null) {
                   /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(newNode);
*/
                        defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(defaultNode);
                        // mapTreeNode.put(node.getId(), node);
                        // listTreeNode.add(node);
                    }
                    node = defaultNode;
                }
                TreeNode modelNode = new ModelTreeNode(ruleDef.getRuleId(), ruleDef.getModelType1ItemValId(), ruleDef.getRuleName());
                modelNode.setType("model");
                node.add(modelNode);
            } else {
                //获取模型在模型目录位置
                TreeNode node = mapTreeNode.get(ruleDef.getModelType2ItemValId());
                if (node == null) {
                    // node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                    if (defaultNode == null) {
                   /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(newNode);
*/
                        defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(defaultNode);
                        // mapTreeNode.put(node.getId(), node);
                        // listTreeNode.add(node);
                    }
                    node = defaultNode;
                }

                TreeNode modelNode = new ModelTreeNode(ruleDef.getRuleId(), ruleDef.getModelType2ItemValId(), ruleDef.getRuleName());
                modelNode.setType("model");
                node.add(modelNode);
            }
        }
        //名称过滤
        if (StrUtil.isNotBlank(ruleName)) {
            fiterTree(listTreeNode, ruleName);
        }

        //是否过滤空节点
        if (filterEmpty) {
            isValidTreeNode(listTreeNode, 0);
        }


        return listTreeNode;
    }


    /**
     * 是否有效节点
     *
     * @param listNodes
     * @param level
     * @return
     */
    public boolean isValidTreeNode(List<TreeNode> listNodes, int level) {
        if (listNodes == null) {
            return false;
        }
        int size = listNodes.size();
        if (size == 0) {
            return false;
        }
        for (int n = size - 1; n >= 0; n--) {
            if (!isValidTreeNode(listNodes.get(n), level + 1)) {
                listNodes.remove(n);
            }
        }
        if (listNodes.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 是否有效节点
     *
     * @param treeNode
     * @param level
     * @return
     */
    public boolean isValidTreeNode(TreeNode treeNode, int level) {
        if (level >= 3) {
            return true;
        }

        if (treeNode.isForceShow()) {
            return true;
        }

        List<TreeNode> listNodes = treeNode.getChildren();
        return isValidTreeNode(listNodes, level);
    }

    //过滤树形
    private boolean fiterTree(TreeNode treeNode, String fiterName) {
        String title = treeNode.getTitle();
        if (title != null && title.indexOf(fiterName) >= 0) {
            treeNode.setForceShow(true);
            return true;
        }

        List<TreeNode> listNodes = treeNode.getChildren();
        return fiterTree(listNodes, fiterName);
    }

    //过滤树形
    private boolean fiterTree(List<TreeNode> listNodes, String fiterName) {
        if (listNodes == null) {
            return false;
        }

        int size = listNodes.size();
        if (size == 0) {
            return false;
        }
        for (int n = size - 1; n >= 0; n--) {
            if (!fiterTree(listNodes.get(n), fiterName)) {
                listNodes.remove(n);
            }
        }
        if (listNodes.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public Model SelectById(String ruleId) {
        return baseMapper.selectById(ruleId);
    }

    public List<TreeNode> selecttbsDictionary(String ruleName, boolean autoCheck, boolean filterEmpty) {
        TbsDictionaryVal dictVal = new TbsDictionaryVal();
        dictVal.setVItemId(MODEL_ITEM_ID);
        dictVal.setCDelFlag("0");

        List<TbsDictionaryVal> listDictVal = tbsDictionaryValMapper.selectList(new QueryWrapper<TbsDictionaryVal>(dictVal).orderByAsc("N_ORDERID"));

        Map<String, TreeNode> mapTreeNode = CollUtil.newHashMap();
        List<TreeNode> listTreeNode = bulidModelBaseTree(listDictVal, mapTreeNode);
        if (listTreeNode == null) {
            listTreeNode = new ArrayList<TreeNode>();
        }
        TreeNode defaultNode = null; //默认未归类的分类

        List<RuleDef> listModel = ruleDefService.selectList();
        //把模型插入目录树对应节点中
        for (RuleDef ruleDef : listModel) {
            //if (ruleDef.getModelType2ItemValId().equals("")) {
            if (StringUtil.isNull(ruleDef.getModelType2ItemValId())){
                //获取模型在模型目录位置
                TreeNode node = mapTreeNode.get(ruleDef.getModelType1ItemValId());
                if (node == null) {
                    // node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                    if (defaultNode == null) {
                   /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(newNode);
*/
                        defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(defaultNode);
                        // mapTreeNode.put(node.getId(), node);
                        // listTreeNode.add(node);
                    }
                    node = defaultNode;
                }
                TreeNode modelNode = new ModelTreeNode(ruleDef.getRuleId(), ruleDef.getModelType1ItemValId(), ruleDef.getRuleName());
                modelNode.setType("model");
                node.add(modelNode);
            } else {
                //获取模型在模型目录位置
                TreeNode node = mapTreeNode.get(ruleDef.getModelType2ItemValId());
                if (node == null) {
                    // node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                    if (defaultNode == null) {
                   /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(newNode);
*/
                        defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(defaultNode);
                        // mapTreeNode.put(node.getId(), node);
                        // listTreeNode.add(node);
                    }
                    node = defaultNode;
                }

                TreeNode modelNode = new ModelTreeNode(ruleDef.getRuleId(), ruleDef.getModelType2ItemValId(), ruleDef.getRuleName());
                modelNode.setType("model");
                node.add(modelNode);
            }
        }
        //名称过滤
        if (StrUtil.isNotBlank(ruleName)) {
            fiterTree(listTreeNode, ruleName);
        }

        //是否过滤空节点
        if (filterEmpty) {
            isValidTreeNode(listTreeNode, 0);
        }


        return listTreeNode;
    }

   /* public List<TreeNode> selecttbsDictionary(String ruleName, boolean  autoCheck,boolean filterEmpty ) {
        TbsDictionaryVal dictVal = new TbsDictionaryVal();
        dictVal.setVItemId(MODEL_ITEM_ID);
        dictVal.setCDelFlag("0");
        dictVal.setLevel(1);
        List<TbsDictionaryVal> listDictVal = tbsDictionaryValMapper.selectList(new QueryWrapper<TbsDictionaryVal>(dictVal).orderByAsc("N_ORDERID"));

        Map<String, TreeNode> mapTreeNode = CollUtil.newHashMap();
        List<TreeNode> listTreeNode = bulidPayBaseTree(listDictVal, mapTreeNode);
        if (listTreeNode == null) {
            listTreeNode = new ArrayList<TreeNode>();
        }

        Model queryModel = new Model();
        queryModel.setCDelFlag("0");
        if (autoCheck) {
            queryModel.setIfAutoCheck("1");
        }
        queryModel.setRuleType("2");
        QueryWrapper queryWrapper = new QueryWrapper<Model>(queryModel);

        TreeNode defaultNode = null; //默认未归类的分类

        List<Model> listModel = this.baseMapper.selectList(queryWrapper);
        //把模型插入目录树对应节点中
        for (Model model : listModel) {
            //获取模型在模型目录位置
            TreeNode node = mapTreeNode.get(model.getModelType1ItemValId());
            if (node == null) {
                 node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                if (defaultNode == null) {
                  *//*  TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(newNode);
*//*
                    defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(defaultNode);
                    // mapTreeNode.put(node.getId(), node);
                    // listTreeNode.add(node);
                }
                node = defaultNode;
            }

            TreeNode modelNode = new ModelTreeNode(model.getRuleId(), model.getModelType1ItemValId(), model.getRuleName());
            modelNode.setType("model");
            node.add(modelNode);
        }

        //名称过滤
        if (StrUtil.isNotBlank(ruleName)) {
            fiterTree(listTreeNode, ruleName);
        }

        //是否过滤空节点
        if (filterEmpty) {
            isValidTreeNode(listTreeNode, 0);
        }


        return listTreeNode;
    }*/

    /**
     * 构建模型基础树
     *
     * @param listDictVal 字典项List
     * @return
     */
    public List<TreeNode> bulidPayBaseTree(List<TbsDictionaryVal> listDictVal, Map<String, TreeNode> mapTreeNode) {
        if (listDictVal == null || listDictVal.size() == 0) {
            return null;
        }

        List<TreeNode> treeList = listDictVal.stream()
                .map(val -> {
                    TreeNode node = new TreeNode();
                    node.setId(val.getVItemValId());
                    node.setTitle(val.getVValName());
                    node.setParentId("0");
                    return node;
                }).collect(Collectors.toList());

        if (mapTreeNode != null) {
            for (TreeNode node : treeList) {
                mapTreeNode.put(node.getId(), node);
            }
        }

        return TreeUtil.bulid(treeList, "0");
    }


}