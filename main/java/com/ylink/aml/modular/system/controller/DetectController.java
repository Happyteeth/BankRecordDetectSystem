package com.ylink.aml.modular.system.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cfss.RuleProcess;
import com.cfss.util.StringUtil;
import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.core.common.exception.BaseException;
import com.ylink.aml.modular.system.dto.ModelResultChart;
import com.ylink.aml.modular.system.dto.ModelResultData;
import com.ylink.aml.modular.system.dto.RuleAutoCheckDto;
import com.ylink.aml.modular.system.dto.RuleRunDto;
import com.ylink.aml.modular.system.entity.*;
import com.ylink.aml.modular.system.model.ModelTreeNode;
import com.ylink.aml.modular.system.model.TreeNode;
import com.ylink.aml.modular.system.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : lida
 */

@Slf4j
@Controller
@RequestMapping("/detect")
public class DetectController extends BaseController {
    private static final String PREFIX = "/modular/detect/";
    private ModelService modelService;
    private RuleRunInsertService ruleRunInsertService;
    private RuleAutoCheckViewService ruleAutoCheckViewService;
    private RuleRunViewService ruleRunViewService;
    private RuleRunService ruleRunService;
    private IDetectService iDetectService;
    private TbsDictionaryValService tbsDictionaryValService;
    private RuleRunViewPayService ruleRunViewPayService;
    private GunsProperties gunsProperties;
    private RuleDefService ruleDefService;

    @Value("${aml.appType}")
    private String appType;


    /**
     * 模型 item_Id
     */
    private static final String MODEL_ITEM_ID = "P1001";
    /**
     * 模型默认分类ID
     */
    private static final String MODEL_DEFAULT_TYPE_ID = "999999999";

    private static final String MODEL_DEFAULT_TYPE_ID2 = "9999999990";

    public DetectController(ModelService modelService, RuleRunInsertService ruleRunInsertService, RuleAutoCheckViewService ruleAutoCheckViewService, RuleRunViewService ruleRunViewService, RuleRunService ruleRunService, IDetectService iDetectService, TbsDictionaryValService tbsDictionaryValService, RuleRunViewPayService ruleRunViewPayService, GunsProperties gunsProperties, RuleDefService ruleDefService) {
        this.modelService = modelService;
        this.ruleRunInsertService = ruleRunInsertService;
        this.ruleAutoCheckViewService = ruleAutoCheckViewService;
        this.ruleRunViewService = ruleRunViewService;
        this.ruleRunService = ruleRunService;
        this.iDetectService = iDetectService;
        this.tbsDictionaryValService = tbsDictionaryValService;
        this.ruleRunViewPayService = ruleRunViewPayService;
        this.gunsProperties = gunsProperties;
        this.ruleDefService = ruleDefService;
    }


    /**
     * 跳转到一键检测界面
     *
     * @return java.lang.String
     */
    @RequestMapping("")
    public String index(org.springframework.ui.Model model) {
        RuleAutoCheckDto dto = null;
        try {
            dto = this.getUpData();
        } catch (Exception e) {
            System.out.println("数据为空！");
            return PREFIX + "detect.html";
        }

        Integer checkProgress = dto.getCheckProgress();
        Integer autoCheckId = dto.getAutoCheckId();
        System.out.println(checkProgress);
        if (checkProgress == null) {
            checkProgress = 0;
        }
        if (checkProgress > 0) {
            model.addAttribute("autoCheckId", autoCheckId + "");
            return PREFIX + "result.html";
        } else {
            return PREFIX + "detect.html";
        }
    }


    /**
     * 跳转到检测结果界面界面
     *
     * @return java.lang.String
     */
    @RequestMapping("/autoCheckResult")
    public String result(org.springframework.ui.Model model, String autoCheckId) {

        model.addAttribute("autoCheckId", autoCheckId);
        return PREFIX + "result.html";
    }


    /**
     * 跳转到查看报告界面
     *
     * @return java.lang.String
     */
    @RequestMapping("/report")
    public String report() {
        return PREFIX + "report.html";
    }

    /**
     * 跳转到一件检测
     *
     * @return java.lang.String
     */
    @RequestMapping("/detec")
    public String detec() {
        return PREFIX + "detect.html";
    }

    /**
     * 一键检测
     * @return java.lang.Object
     *
     */
    @RequestMapping("/start")
    @ResponseBody
    public ResponseData startDetect() {
        long startTime = System.currentTimeMillis();
        if (Const.APP_TYPE_PAY.equalsIgnoreCase(appType)) {
            //查询出可执行的规则、模型
            List<RuleDef> modelList = ruleDefService.selectModels();
            if (modelList == null) {
                throw new BaseException("模型查询结果为空");
            }
            //一键检测表中checkId最大值
            Integer maxCheckId = ruleAutoCheckViewService.getMaxRuleAutoCheckId();
            int autoCheckId = 1;
            if (maxCheckId != null) {
                autoCheckId = maxCheckId + 1;
            }
            //生成模型执行实例，即像rule_run中插入数据
            for (RuleDef ruleDef : modelList) {
                ruleRunInsertService.createRuleRunPay(ruleDef, autoCheckId);
            }
            //执行模型
            Thread thead = new Thread(new RuleProcess());
            thead.start();

            return ResponseData.success(autoCheckId);
        } else {
            //查询出可执行的规则、模型
            List<Model> modelList = modelService.selectModels();
            if (modelList == null) {
                throw new BaseException("模型查询结果为空");
            }
            //一键检测视图中checkId最大值
            Integer maxCheckId = ruleAutoCheckViewService.getMaxRuleAutoCheckId();
            int autoCheckId = 1;
            if (maxCheckId != null) {
                autoCheckId = maxCheckId + 1;
            }
            //生成模型执行实例，即像rule_run中插入数据
            for (Model ruleDef : modelList) {
                ruleRunInsertService.createRuleRun(ruleDef, autoCheckId);
            }
            //单机版直接执行模型，大数据版由大数据程序执行模型
            if (Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
                log.info("当前版本是大数据版本，模型执行在大数据程序中管理");
            } else {
                Thread thead = new Thread(new RuleProcess());
                thead.start();
            }
            long endTime = System.currentTimeMillis();
            log.info("一键检测start时间： " + (endTime - startTime) + "ms");
            return ResponseData.success(autoCheckId);
        }
    }

    /**
     * 上次检测参数
     *
     * @param
     * @return
     */
    @RequestMapping("/getUpData")
    @ResponseBody
    public RuleAutoCheckDto getUpData() {
        RuleAutoCheckDto dto = new RuleAutoCheckDto();
        // String userName = ShiroKit.getUser().getAccount(); //当前账号
        //查询记录中最后一次提交时间
        RuleAutoCheckView autoCheckInfo = ruleAutoCheckViewService.getAutoCheckMax();
        if (autoCheckInfo == null) {
            return dto;
        }
        dto.setDInsert(autoCheckInfo.getDInsert());
        dto.setRunTime(autoCheckInfo.getRunTime());
        dto.setAutoCheckId(autoCheckInfo.getAutoCheckId());
        //执行模型总数量
        dto.setRuleCnt(autoCheckInfo.getCountTotal());
        //检查完成数量
        dto.setCheckEndNum(autoCheckInfo.getStatus8() + autoCheckInfo.getStatusSucc() + autoCheckInfo.getStatusA());
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 取整数
        numberFormat.setMaximumFractionDigits(0);
        String result = numberFormat.format((float) dto.getCheckEndNum() / (float) dto.getRuleCnt() * 100);
        //检查进度百分比数
        dto.setCheckProgress(Integer.valueOf(result));
        return dto;
    }

    /**
     * 取消检测
     *
     * @param
     * @return
     */
    @RequestMapping("/updateStart")
    @ResponseBody
    public ResponseData updateStart(@RequestParam(value = "autoCheckId") Integer autoCheckId) {

        if (autoCheckId == null) {
            throw new BaseException("数据为空");
        }
        ruleRunService.updateStart(autoCheckId);
        return ResponseData.success();
    }


    /*
     * @Description：检测结果
     * @param: [autoCheckId]
     * @return java.lang.Object
     */
    @RequestMapping("/checkResult")
    @ResponseBody
    public Object getAutoCheckResult(@RequestParam(value = "autoCheckId", required = false) Integer autoCheckId) {
        if (autoCheckId == null) {
            return ResponseData.error("参数为空");
        }
        //查询出每个模型执行结果
        //获取当前一键执行批次信息
        /*        Integer autoCheckId = 0;*/
        RuleAutoCheckView autoCheckInfo = ruleAutoCheckViewService.getAutoCheckInfo(autoCheckId);
        if (autoCheckInfo == null) {
            return ResponseData.error("查询结果为空");
        }
        RuleAutoCheckDto dto = new RuleAutoCheckDto();
        dto.setAutoCheckId(autoCheckId);
        dto.setDInsert(autoCheckInfo.getDInsert());
        dto.setRunTime(autoCheckInfo.getRunTime());
        dto.setAutoCheckId(autoCheckInfo.getAutoCheckId());
        //执行模型总数量
        dto.setRuleCnt(autoCheckInfo.getCountTotal());
        //检查完成数量
        dto.setCheckEndNum(autoCheckInfo.getStatus8() + autoCheckInfo.getStatusSucc() + autoCheckInfo.getStatusA());
        log.info(String.valueOf(dto.getCheckEndNum()));
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 取整数
        numberFormat.setMaximumFractionDigits(0);
        String result = numberFormat.format((float) dto.getCheckEndNum() / (float) dto.getRuleCnt() * 100);
        log.info(result);
        //检查进度百分比数
        dto.setCheckProgress(Integer.valueOf(result));

        TbsDictionaryVal dictVal = new TbsDictionaryVal();
        dictVal.setVItemId(MODEL_ITEM_ID);
        dictVal.setCDelFlag("0");
        List<TbsDictionaryVal> listDictVal = tbsDictionaryValService.list(new QueryWrapper<TbsDictionaryVal>(dictVal).orderByAsc("N_ORDERID"));
        Map<String, TreeNode> mapTreeNode = CollUtil.newHashMap();
        List<TreeNode> listTreeNode = modelService.bulidModelBaseTree(listDictVal, mapTreeNode);
        if (listTreeNode == null) {
            listTreeNode = new ArrayList<TreeNode>();
        }
        if (Const.APP_TYPE_PAY.equalsIgnoreCase(appType) || Const.APP_VERSION_BIGDATA.equalsIgnoreCase(appType)) {
            List<RuleRunViewEntityPay> ruleRunList = ruleRunViewPayService.getRuleRunList(autoCheckId);
            if (ruleRunList == null || ruleRunList.size() < 1) {
                return ResponseData.error("查询结果为空");
            }
            //默认未归类的分类
            TreeNode defaultNode = null;
            for (RuleRunViewEntityPay ruleRun : ruleRunList) {

                //通过规则id查询出规则信息
                RuleDef model = ruleDefService.selectById(ruleRun.getRuleId());
                if (StrUtil.isEmpty(model.getModelType2ItemValId())) {

                    //获取模型在模型目录位置
                    TreeNode node = mapTreeNode.get(model.getModelType1ItemValId());
                    if (node == null) {
                        //node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                        if (defaultNode == null) {
                       /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(newNode);*/
                            defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                            listTreeNode.add(defaultNode);
                            // mapTreeNode.put(node.getId(), node);
                            // listTreeNode.add(node);
                        }
                        node = defaultNode;
                    }
                    RuleRunDto ruleRunDto = new RuleRunDto();
                    long checkCount = ruleRun.getCheckCount();
                    if (checkCount == 0 || ruleRun.getRerultCount() == 0) {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        ruleRunDto.setProportion("0.0000");
                    } else {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        String sumCount = numberFormat.format((float) ruleRun.getRerultCount() / (float) checkCount * 100);
                        ruleRunDto.setProportion(sumCount);
                    }
                    BeanUtil.copyProperties(ruleRun, ruleRunDto);
                    TreeNode modelNode = new ModelTreeNode(ruleRun.getRuleId(), model.getModelType1ItemValId(), model.getRuleName(), ruleRun.getRerultCount(),
                            ruleRun.getCheckCount(), ruleRun.getStatus(), ruleRunDto.getProportion());
                    modelNode.setType("model");
                    node.add(modelNode);
                } else {
                    //获取模型在模型目录位置
                    TreeNode node = mapTreeNode.get(model.getModelType2ItemValId());
                    if (node == null) {
                        //node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                        if (defaultNode == null) {
                       /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(newNode);*/
                            defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                            listTreeNode.add(defaultNode);
                            // mapTreeNode.put(node.getId(), node);
                            // listTreeNode.add(node);
                        }
                        node = defaultNode;
                    }
                    RuleRunDto ruleRunDto = new RuleRunDto();
                    long checkCount = ruleRun.getCheckCount();
                    if (checkCount == 0 || ruleRun.getRerultCount() == 0) {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        ruleRunDto.setProportion("0.0000");
                    } else {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        String sumCount = numberFormat.format((float) ruleRun.getRerultCount() / (float) checkCount * 100);
                        ruleRunDto.setProportion(sumCount);
                    }
                    BeanUtil.copyProperties(ruleRun, ruleRunDto);
                    TreeNode modelNode = new ModelTreeNode(ruleRun.getRuleId(), model.getModelType2ItemValId(), model.getRuleName(), ruleRun.getRerultCount(),
                            ruleRun.getCheckCount(),ruleRun.getStatus(), ruleRunDto.getProportion());
                    modelNode.setType("model");
                    node.add(modelNode);
                }
            }
            modelService.isValidTreeNode(listTreeNode, 1);
            dto.setTree(listTreeNode);
            return dto;
        } else {

            List<RuleRunViewEntityPay> ruleRunList = ruleRunViewPayService.getRuleRunList(autoCheckId);
            if (ruleRunList == null || ruleRunList.size() < 1) {
                return ResponseData.error("查询结果为空");
            }
            TreeNode defaultNode = null; //默认未归类的分类
            for (RuleRunViewEntityPay ruleRun : ruleRunList) {

                //通过规则id查询出规则信息
                RuleDef model = ruleDefService.selectById(ruleRun.getRuleId());
                if (StringUtil.isNull(model.getModelType2ItemValId())) {
                    //获取模型在模型目录位置
                    TreeNode node = mapTreeNode.get(model.getModelType1ItemValId());
                    if (node == null) {
                        //node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                        if (defaultNode == null) {
                       /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(newNode);*/
                            defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                            listTreeNode.add(defaultNode);
                            // mapTreeNode.put(node.getId(), node);
                            // listTreeNode.add(node);
                        }
                        node = defaultNode;
                    }
                    RuleRunDto ruleRunDto = new RuleRunDto();
                    long checkCount = ruleRun.getCheckCount();
                    if (checkCount == 0 || ruleRun.getRerultCount() == 0) {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        ruleRunDto.setProportion("0.0000");
                    } else {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        String sumCount = numberFormat.format((float) ruleRun.getRerultCount() / (float) checkCount * 100);
                        ruleRunDto.setProportion(sumCount);
                    }
                    BeanUtil.copyProperties(ruleRun, ruleRunDto);
                    TreeNode modelNode = new ModelTreeNode(ruleRun.getRuleId(), model.getModelType1ItemValId(), model.getRuleName(), ruleRun.getRerultCount(),ruleRun.getCheckCount(), ruleRun.getStatus(), ruleRunDto.getProportion());
                    modelNode.setType("model");
                    node.add(modelNode);
                } else {
                    //获取模型在模型目录位置
                    TreeNode node = mapTreeNode.get(model.getModelType2ItemValId());
                    if (node == null) {
                        //node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                        if (defaultNode == null) {
                       /* TreeNode newNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                        listTreeNode.add(newNode);*/
                            defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                            listTreeNode.add(defaultNode);
                            // mapTreeNode.put(node.getId(), node);
                            // listTreeNode.add(node);
                        }
                        node = defaultNode;
                    }
                    RuleRunDto ruleRunDto = new RuleRunDto();
                    long checkCount = ruleRun.getCheckCount();
                    if (checkCount == 0 || ruleRun.getRerultCount() == 0) {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        ruleRunDto.setProportion("0.0000");
                    } else {
                        // 取整数
                        numberFormat.setMaximumFractionDigits(4);
                        String sumCount = numberFormat.format((float) ruleRun.getRerultCount() / (float) checkCount * 100);
                        ruleRunDto.setProportion(sumCount);
                    }
                    BeanUtil.copyProperties(ruleRun, ruleRunDto);
                    TreeNode modelNode = new ModelTreeNode(ruleRun.getRuleId(), model.getModelType2ItemValId(), model.getRuleName(), ruleRun.getRerultCount(),
                            ruleRun.getCheckCount(), ruleRun.getStatus(), ruleRunDto.getProportion());
                    modelNode.setType("model");
                    node.add(modelNode);
                }
            }
            modelService.isValidTreeNode(listTreeNode, 1);
            dto.setTree(listTreeNode);
            return dto;
        }
    }

    /*
     * @Description：检测结果(Pay)
     * @param: [autoCheckId]
     * @return java.lang.Object
     */
    @RequestMapping("/checkResultPay")
    @ResponseBody
    public Object getAutoCheckResultPay(@RequestParam(value = "autoCheckId", required = false) Integer autoCheckId) {
        if (autoCheckId == null) {
            return ResponseData.error("参数为空");
        }
        //查询出每个模型执行结果
        //获取当前一键执行批次信息
        /*        Integer autoCheckId = 0;*/
        RuleAutoCheckView autoCheckInfo = ruleAutoCheckViewService.getAutoCheckInfo(autoCheckId);
        if (autoCheckInfo == null) {
            return ResponseData.error("查询结果为空");
        }
        RuleAutoCheckDto dto = new RuleAutoCheckDto();
        dto.setAutoCheckId(autoCheckId);
        dto.setDInsert(autoCheckInfo.getDInsert());
        dto.setRunTime(autoCheckInfo.getRunTime());
        dto.setAutoCheckId(autoCheckInfo.getAutoCheckId());
        //执行模型总数量
        dto.setRuleCnt(autoCheckInfo.getCountTotal());
        //检查完成数量
        dto.setCheckEndNum(autoCheckInfo.getStatus8() + autoCheckInfo.getStatusSucc() + autoCheckInfo.getStatusA());
        log.info(String.valueOf(dto.getCheckEndNum()));
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 取整数
        numberFormat.setMaximumFractionDigits(0);
        String result = numberFormat.format((float) dto.getCheckEndNum() / (float) dto.getRuleCnt() * 100);
        log.info(result);
        //检查进度百分比数
        dto.setCheckProgress(Integer.valueOf(result));

        TbsDictionaryVal dictVal = new TbsDictionaryVal();
        dictVal.setVItemId(MODEL_ITEM_ID);
        dictVal.setCDelFlag("0");
        dictVal.setLevel(1);
        List<TbsDictionaryVal> listDictVal = tbsDictionaryValService.list(new QueryWrapper<TbsDictionaryVal>(dictVal).orderByAsc("N_ORDERID"));
        Map<String, TreeNode> mapTreeNode = CollUtil.newHashMap();
        List<TreeNode> listTreeNode = modelService.bulidPayBaseTree(listDictVal, mapTreeNode);
        if (listTreeNode == null) {
            listTreeNode = new ArrayList<TreeNode>();
        }

        List<RuleRunViewEntityPay> ruleRunList = ruleRunViewPayService.getRuleRunList(autoCheckId);
        if (ruleRunList == null || ruleRunList.size() < 1) {
            return ResponseData.error("查询结果为空");
        }
        TreeNode defaultNode = null; //默认未归类的分类
        for (RuleRunViewEntityPay ruleRun : ruleRunList) {

            //通过规则id查询出规则信息
            RuleDef model = ruleDefService.selectById(ruleRun.getRuleId());
            //获取模型在模型目录位置
            TreeNode node = mapTreeNode.get(model.getModelType1ItemValId());
            if (node == null) {
                node = mapTreeNode.get(MODEL_DEFAULT_TYPE_ID);  //获取默认分类，没有，则创建一个
                if (defaultNode == null) {

                    defaultNode = new TreeNode(MODEL_DEFAULT_TYPE_ID, "0", "未归类检查项");
                    listTreeNode.add(defaultNode);
                }
                node = defaultNode;
            }
            RuleRunDto ruleRunDto = new RuleRunDto();
            long checkCount = ruleRun.getCheckCount();
            if (checkCount == 0 || ruleRun.getRerultCount() == 0) {
                // 取整数
                numberFormat.setMaximumFractionDigits(4);
                ruleRunDto.setProportion("0.0000");
            } else {
                // 取整数
                numberFormat.setMaximumFractionDigits(4);
                String sumCount = numberFormat.format((float) ruleRun.getRerultCount() / (float) checkCount);
                ruleRunDto.setProportion(sumCount);
            }
            BeanUtil.copyProperties(ruleRun, ruleRunDto);
            TreeNode modelNode = new ModelTreeNode(ruleRun.getRuleId(), model.getModelType2ItemValId(), model.getRuleName(), ruleRun.getRerultCount(),
                    ruleRun.getCheckCount(),ruleRun.getStatus(), ruleRunDto.getProportion());
            modelNode.setType("model");
            node.add(modelNode);
        }
        modelService.isValidTreeNode(listTreeNode, 0);
        dto.setTree(listTreeNode);
        return dto;
    }


    /*
     * @Description：模型树
     * @param: []
     * @return java.util.List<com.ylink.aml.modular.system.model.TreeNode>
     */
    @RequestMapping("/tree")
    @ResponseBody
    public List<TreeNode> viewReport() {
        return modelService.getModelTree(null, true, true);
    }

    /*
     * @Description：模型执行结果
     * @param: [autoCheckId, ruleId]
     * @return cn.stylefeng.roses.core.reqres.response.ResponseData
     */
    @RequestMapping("/result")
    @ResponseBody
    public ResponseData getRuleRunResult(@RequestParam(value = "autoCheckId") Integer autoCheckId,
                                         @RequestParam(value = "ruleId") String ruleId) {
        if (autoCheckId == null && StringUtils.isEmpty(ruleId)) {
            return ResponseData.error("参数为空");
        }
        List<Object> ruleRunIdList = ruleRunViewService.getRuleRunId(autoCheckId, ruleId);
        if (ruleRunIdList == null || ruleRunIdList.size() < 1) {
            return ResponseData.error("查询结果为空");
        }
        String ruleRunId = ruleRunIdList.get(0).toString();
        //List<Map<String ,ModelResultData>> resultList = new ArrayList<>();
        Map<String, ModelResultData> map = new HashMap<>();
        //List<ModelResultData> resultList = new ArrayList<>();


        //查询出执行结果/数据
        RuleRunViewEntity ruleRunViewEntity = ruleRunViewService.getById(ruleRunId);
        ModelResultData modelDataResult = new ModelResultData();
        modelDataResult.setRuleRunId(ruleRunId);
        ruleRunViewService.initFromResult(ruleRunViewEntity.getRerultLineN(), modelDataResult);
        map.put("table", modelDataResult);

        //查询出执行结果图表
        Model model = modelService.selectById(ruleRunViewEntity.getRuleId());
        if (model == null) {
            return ResponseData.error("查询不到模型数据");
        }
        //图表的样式：1 - 柱状图（竖）；2 - 柱状图（横）；3 - 饼形图
        ModelResultChart modelChartResult = new ModelResultChart();
        ruleRunViewService.initFromResult(ruleRunViewEntity.getRerultChartData(), modelChartResult);
        ruleRunViewService.initChartValue(model, modelChartResult);
        if (modelDataResult.getData() == null || modelDataResult.getData().size() <= 0) {
            modelChartResult.setData(null);
            map.put("echart", modelChartResult);
            return ResponseData.success(200, "模型执行结果", map);
        }
        map.put("echart", modelChartResult);
        return ResponseData.success(200, "模型执行结果", map);
    }


    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        log.info("size" + modelService.selectModels().size());
        return ruleAutoCheckViewService.getAutoCheckInfo(0).toString();
    }


    /**
     * 导出
     *
     * @param
     * @throws Exception
     */
    @RequestMapping("/downprog")
    @ApiOperation(value = "导出", notes = "")
    public ResponseData downprog(@RequestBody Param param,
                                 HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (param == null) {
            return ResponseData.error("param is empty");
        }
        String ruleRunId = param.ruleRunId;
        if (ruleRunId == null || ruleRunId == "") {
            log.error("数据为空");
            return ResponseData.error("ruleRunId is empty");
        }
        String chartTypes = param.chartTypes;
        if (chartTypes == null || chartTypes == "") {
            log.error("数据为空");
            return ResponseData.error("chartTypes is empty");
        }
        ruleRunViewService.downprog(response, request, ruleRunId, chartTypes);
        return SUCCESS_TIP;
    }

    @Data
    static class Param {
        private String ruleRunId;
        private String chartTypes;
    }


    @RequestMapping(value = "/batchExport")
    @ResponseBody
    public void batchExport(@RequestParam(required = false, value = "autoCheckId") String autoCheckId,
                            HttpServletResponse response) {
        if (StringUtils.isEmpty(autoCheckId)) {
            log.error("参数不能为空");
            return;
        }
        File file = iDetectService.batchExport(autoCheckId, response);
        if (file == null || !file.exists() || !file.isFile()) {
            log.error("导出失败");
            return;
        }
        try {
            response.setContentType("application/zip;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(file.getName(), "UTF-8"));
            byte[] buffer = new byte[1024];
            OutputStream outputStream = response.getOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            int i;
            while ((i = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, i);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            log.error("响应到前端失败，{}", e.getMessage());
            e.printStackTrace();
        } finally {
            file.delete();
        }
    }

}