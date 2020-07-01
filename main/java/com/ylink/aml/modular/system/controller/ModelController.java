/**
 *
 */
package com.ylink.aml.modular.system.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cfss.RuleProcess;
import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.core.common.exception.BizExceptionEnum;
import com.ylink.aml.core.common.page.LayuiPageFactory;
import com.ylink.aml.core.common.page.LayuiPageInfo;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.dto.ModelResultChart;
import com.ylink.aml.modular.system.dto.ModelResultData;
import com.ylink.aml.modular.system.dto.RuleParaDefDto;
import com.ylink.aml.modular.system.entity.*;
import com.ylink.aml.modular.system.mapper.RuleParaDefMapper;
import com.ylink.aml.modular.system.model.*;
import com.ylink.aml.modular.system.service.*;
import com.ylink.aml.modular.system.util.ExcelImportUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

/**
 * 模型管理
 */
@Controller
@RequestMapping("/mod")
@Slf4j
public class ModelController extends BaseController {

    private static String PREFIX = "/modular/model/";

    //public static final Log LOG = LogFactory.getLog(ModelController.class.getName());

    //public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHHmmss");

    private GunsProperties gunsProperties;
    //private BeetlConfiguration beetlConfiguration;

    private RuleDefService ruleDefService;

    private ModelService modelService;

    private TbsDictionaryValService tbsDictionaryValService;

    private RuleParaDefService ruleParaDefService;
    private RuleRunViewPayService ruleRunViewPayService;

    private RuleRunInsertService ruleRunInsertService;

    private RuleRunViewService ruleRunViewService;

    private RuleParaDefMapper ruleParaDefMapper;

    //private RuleRunService ruleRunService;
    @Value("${aml.appType}")
    private String appType;

    public ModelController(RuleRunViewPayService ruleRunViewPayService, GunsProperties gunsProperties, ModelService modelService, TbsDictionaryValService tbsDictionaryValService, RuleParaDefService ruleParaDefService, RuleRunInsertService ruleRunInsertService, RuleRunViewService ruleRunViewService, RuleParaDefMapper ruleParaDefMapper, RuleDefService ruleDefService) {
        this.gunsProperties = gunsProperties;
        this.modelService = modelService;
        this.tbsDictionaryValService = tbsDictionaryValService;
        this.ruleParaDefService = ruleParaDefService;
        this.ruleRunInsertService = ruleRunInsertService;
        this.ruleRunViewService = ruleRunViewService;
        this.ruleParaDefMapper = ruleParaDefMapper;
        this.ruleDefService = ruleDefService;
        this.ruleRunViewPayService = ruleRunViewPayService;
    }
    /**
     * 跳转到查看管理员列表的页面
     */
    @RequestMapping("")
    public String index() {
        if (Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
            return PREFIX + "modelManagement_bd.html";
        } else if (Const.APP_TYPE_PAY.equalsIgnoreCase(appType)) {
            return PREFIX + "modelManagement_pay.html";
        } else {
            return PREFIX + "modelManagement.html";
        }
    }

    /**
     * 获取模型分类的ztree（银行版）
     */
    @RequestMapping(value = "/modelztree")
    @ResponseBody
    public List<TreeNode> modelztree(@RequestParam(value = "ruleName", required = false) String ruleName) {
        boolean filterEmpty = false;
        if (StrUtil.isNotBlank(ruleName)) {
            filterEmpty = true;
        }
        List<TreeNode> list = null;
        if (Const.APP_TYPE_PAY.equalsIgnoreCase(appType)) {
            list = modelService.selecttbsDictionary(ruleName, false, filterEmpty);
        } else {
           list = modelService.getModelTree(ruleName, false, filterEmpty);
            //list = modelService.selecttbsDictionary(ruleName, false, filterEmpty);
        }
        return list;
    }


    /**
     * 增加分类
     */
    @RequestMapping(value = "/save")
    @ResponseBody
    public ResponseData save(@RequestBody Modelvo modelvo) {

        if (StrUtil.isBlank(modelvo.getName())) {
            return ResponseData.error("名称不能为空");
        }

        TbsDictionaryVal queryVal = new TbsDictionaryVal();
        queryVal.setVItemId(ModelService.MODEL_ITEM_ID);
        queryVal.setVValName(modelvo.getName());
        if (StrUtil.isBlank(modelvo.getPid())) {  //添加一级类型
            queryVal.setVItemValIdPar("0");
        } else {
            queryVal.setVItemValIdPar(modelvo.getPid());
        }
        QueryWrapper<TbsDictionaryVal> queryWrapper = new QueryWrapper<TbsDictionaryVal>(queryVal);
        if (tbsDictionaryValService.getOne(queryWrapper) != null) {
            return ResponseData.error("该名称已经存在");
        }

        TbsDictionaryVal dictionaryVal = new TbsDictionaryVal();
        dictionaryVal.setVItemId(ModelService.MODEL_ITEM_ID);
        DictValDto li = tbsDictionaryValService.selectvval(ModelService.MODEL_ITEM_ID);
        int sId = 10001;
        if (li != null) {
            sId = li.getValuId() + 1;
        }

        dictionaryVal.setVItemValId(StrUtil.toString(sId));

        dictionaryVal.setNOrderid("0");
        dictionaryVal.setVValName(modelvo.getName());
        dictionaryVal.setVItemIdPar(ModelService.MODEL_ITEM_ID);
        dictionaryVal.setVInsertUser(ShiroKit.getUser().getAccount());
        dictionaryVal.setDInsert(new Date());
        if (StrUtil.isBlank(modelvo.getPid())) {  //添加一级类型
            dictionaryVal.setVItemValIdPar("0");
            dictionaryVal.setLevel(1);
        } else {
            dictionaryVal.setVItemValIdPar(modelvo.getPid());
            dictionaryVal.setLevel(2);
        }
        tbsDictionaryValService.insert(dictionaryVal);
        return SUCCESS_TIP;


    }

    /**
     * 所有分类菜单
     */
    @RequestMapping(value = "/ModelType")
    @ResponseBody
    public List<TbsDictionaryVal> ModelType(@RequestParam(value = "vItemId", required = false) String vItemId,
                                            @RequestParam(value = "vItemValId", required = false) String vItemValId) {

        if (vItemValId == null || vItemValId == "") {
            List<TbsDictionaryVal> list = tbsDictionaryValService.selecttbsDictionaryVal(vItemId);
            return list;
        }
        TbsDictionaryVal tbsDictionaryVal = new TbsDictionaryVal();
        tbsDictionaryVal.setVItemValIdPar(vItemValId);
        List<TbsDictionaryVal> listt = tbsDictionaryValService.selecttbsVal(tbsDictionaryVal.getVItemValIdPar());
        return listt;
    }


    /**
     * 获取模型详细信息
     */
    @RequestMapping(value = "/modelselect")
    @ResponseBody
    public ModelDto modelselec(@RequestParam("ruleId") String ruleId) {
        ModelDto modelDto = modelService.selectModelV(ruleId);
        List<ModelvalDto> modelvalDto = modelService.selectval();
        for (ModelvalDto lvalDto : modelvalDto) {
            modelDto.setVItemValId1(lvalDto.getVItemValId1());
            modelDto.setVItemValId2(lvalDto.getVItemValId2());
        }
        String ruleprog = modelDto.getRuleProg();
        if (StringUtils.isEmpty(ruleprog)) {
            modelDto.setRuleProg("数据代码为空");
        } else {
            String rulepro = DigestUtils.md5Hex(ruleprog);
            modelDto.setRuleProg(rulepro);
        }
        String charprog = modelDto.getChartProg();
        if (StringUtils.isEmpty(charprog)) {
            modelDto.setChartProg("图表代码为空");
        } else {
            String charprogmd = DigestUtils.md5Hex(charprog);
            modelDto.setChartProg(charprogmd);
        }
        modelDto.setModelSql("数据代码：" + "\n" + modelDto.getRuleProg() + ";" + "\r\n" + "\r\n" + "图表代码：" + "\n" + modelDto.getChartProg());
        return modelDto;
    }

    /**
     * 获取参数详细信息
     */
    @RequestMapping(value = "/modelPara")
    @ResponseBody
    public LayuiPageInfo modelPara(@RequestParam(value = "ruleId", required = false) String ruleId) {


        if (StrUtil.isEmpty(ruleId)) {
            return LayuiPageFactory.success(null);
        }
        List<RuleParaDef> RuleParaDefList = ruleParaDefService.selectByIdl(ruleId);
        if (RuleParaDefList == null) {
            return LayuiPageFactory.success(null);
        }

        //List<RuleParaDefDto> RuleParaDefDtoList = YlCollUtil.copyList(RuleParaDefList,RuleParaDefDto.class);
        return LayuiPageFactory.success(RuleParaDefList);
    }

    /**
     * 修改参数值
     */
    @RequestMapping(value = "/updateModelParaVal")
    @ResponseBody
    public ResponseData updateModelParaVal(@RequestBody RuleParaDefDto RuleParaDefDto) {
        if (StrUtil.isBlank(RuleParaDefDto.getRuleId()) || StrUtil.isBlank(RuleParaDefDto.getParaString()) || StrUtil.isBlank(RuleParaDefDto.getParaValue())) {
            return ResponseData.error("传入参数有误");
        }

        //查询条件
        RuleParaDef ruleParaDefQuery = new RuleParaDef();
        ruleParaDefQuery.setRuleId(RuleParaDefDto.getRuleId());
        ruleParaDefQuery.setParaString(RuleParaDefDto.getParaString());

        //修改值
        RuleParaDef ruleParaDef = new RuleParaDef();
        ruleParaDef.setParaValue(RuleParaDefDto.getParaValue());
        ruleParaDef.setVUpdateUser(ShiroKit.getUser().getAccount()); //修改用户
        ruleParaDef.setDUpdate(new Timestamp(System.currentTimeMillis()));//修改时间

        int count = ruleParaDefMapper.update(ruleParaDef, new QueryWrapper<>(ruleParaDefQuery));
        if (count == 1) {

            return ResponseData.success();
        } else {
            return ResponseData.error("数据修改出错,count=" + count);
        }
    }


    /**
     * 修改模型字段
     */
    @RequestMapping("/UpdateModel")
    @ResponseBody
    public Object UpdateModel(@RequestBody ModelQueryvo modelQueryvo) {
        if (modelQueryvo.getRuleId() == null || modelQueryvo.getRuleId() == "") {
            return ResponseData.error("数据异常");
        }
        Model model = new Model();
        BeanUtil.copyProperties(modelQueryvo, model);
        if (modelQueryvo.getVItemValId1().equals("") || modelQueryvo.getVItemValId1() == null) {
            model.setModelType1ItemValId(model.getModelType1ItemValId());
        } else {
            model.setModelType1ItemValId(modelQueryvo.getVItemValId1());
        }
            model.setModelType2ItemValId(modelQueryvo.getVItemValId2());

        model.setIfAutoCheck(modelQueryvo.getIfAutoCheck());
        model.setModelType1ItemId(ModelService.MODEL_ITEM_ID);
        model.setModelType2ItemId(ModelService.MODEL_ITEM_ID);
        model.setModelDesc(modelQueryvo.getModelDesc());
        //修改用户
        model.setVUpdateUser(ShiroKit.getUser().getAccount());
        //修改时间
        model.setDUpdate(new Timestamp(System.currentTimeMillis()));
        modelService.updateById(model);
        return SUCCESS_TIP;
    }

    /**
     * 修改模型字段(pay)
     */
    @RequestMapping("/updatepay")
    @ResponseBody
    public Object Updatepay(@RequestBody Modelpayvo modelpayvo) {
        if (modelpayvo.getRuleId() == null || modelpayvo.getRuleId() == "") {
            return ResponseData.error("数据异常");
        }
        Model mo = modelService.selectById(modelpayvo.getRuleId());
        Model model = new Model();
        BeanUtil.copyProperties(modelpayvo, model);
        BeanUtil.copyProperties(mo, model);
        if (modelpayvo.getVItemValId1().equals("") || modelpayvo.getVItemValId1() == null) {
            model.setModelType1ItemValId(mo.getModelType1ItemValId());
        } else {
            model.setModelType1ItemValId(modelpayvo.getVItemValId1());
        }
        model.setIfAutoCheck(modelpayvo.getIfAutoCheck());
        model.setModelType1ItemId(ModelService.MODEL_ITEM_ID);
        model.setModelDesc(modelpayvo.getModelDesc());
        model.setVUpdateUser(ShiroKit.getUser().getAccount()); //修改用户
        model.setDUpdate(new Timestamp(System.currentTimeMillis()));//修改时间
        modelService.updateById(model);
        return SUCCESS_TIP;
    }


    /**
     * 导出
     *
     * @param response
     * @throws Exception
     */
    @RequestMapping("/exportExcel")
    @ApiOperation(value = "导出模型信息", notes = "")
    public void exportExcel(HttpServletResponse response) throws Exception {
        modelService.Excellist(response);
    }

    /**
     * 导出(pay)
     *
     * @param response
     * @throws Exception
     */
    @RequestMapping("/exportExcelPay")
    @ApiOperation(value = "导出模型信息", notes = "")
    public void exportExcelP(HttpServletResponse response) throws Exception {
        ruleDefService.Excellistpay(response);
    }


    /**
     * 导入
     *
     * @param file
     * @param request
     * @param response
     * @param session
     * @return
     * @throws IOException
     */

    @ResponseBody
    @RequestMapping(value = "batchImport")
    @ApiOperation(value = "导入模型信息", notes = "")
    public ResponseData batchImport(@RequestParam(value = "file") MultipartFile file,
                                    HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        //判断文件是否为空
        if (file == null) {
            session.setAttribute("msg", "文件不能为空！");
            return ResponseData.error("文件不能为空");
        }
        //获取文件名
        String fileName = file.getOriginalFilename();
        //验证文件名是否合格
        if (!ExcelImportUtils.validateExcel(fileName)) {
            session.setAttribute("msg", "文件必须是excel格式！");
            return ResponseData.error("文件必须是excel格式");
        }
        //进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
        long size = file.getSize();
        if (StringUtils.isEmpty(fileName) || size == 0) {
            session.setAttribute("msg", "文件不能为空！");
            return ResponseData.error("文件不能为空！");
        }

        // 获取文件后缀
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        final File excelFile = File.createTempFile(IdUtil.simpleUUID(), prefix);
        file.transferTo(excelFile);
        ExcelReader reader = ExcelUtil.getReader(excelFile);
        String msg = "";
        boolean rs = false;
        try {

            int count = modelService.batchImport(reader);
            msg = "成功导入 " + count + " 行数据";
            rs = true;
        } catch (Exception e) {
            log.error("导入出错", e);
            msg = e.getMessage();
        } finally {
            reader.close();
            deleteFile(excelFile);
        }
        if(rs){
            return ResponseData.success(msg);
        }else{
            return ResponseData.error(msg);
        }
    }

    /**
     * 导入(pay)
     *
     * @param file
     * @param request
     * @param response
     * @param session
     * @return
     * @throws IOException
     */

    @ResponseBody
    @RequestMapping(value = "batchImportPay")
    @ApiOperation(value = "导入模型信息", notes = "")
    public ResponseData batchImportPay(@RequestParam(value = "file") MultipartFile file,
                                       HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        //判断文件是否为空
        if (file == null) {
            session.setAttribute("msg", "文件不能为空！");
            return ResponseData.error("文件不能为空");
        }
        //获取文件名
        String fileName = file.getOriginalFilename();
        //验证文件名是否合格
        if (!ExcelImportUtils.validateExcel(fileName)) {
            session.setAttribute("msg", "文件必须是excel格式！");
            return ResponseData.error("文件必须是excel格式");
        }
        //进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
        long size = file.getSize();
        if (StringUtils.isEmpty(fileName) || size == 0) {
            session.setAttribute("msg", "文件不能为空！");
            return ResponseData.error("文件不能为空！");
        }

        // 获取文件后缀
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        final File excelFile = File.createTempFile(IdUtil.simpleUUID(), prefix);
        file.transferTo(excelFile);
        ExcelReader reader = ExcelUtil.getReader(excelFile);
        String msg = "";
        boolean rs = false;
        try {
            int count = ruleDefService.batchImport(reader);
            msg = "成功导入 " + count + " 行数据";
            rs = true;
        } catch (Exception e) {
            log.error("导入出错", e);
            msg = e.getMessage();
        } finally {
            reader.close();
            deleteFile(excelFile);
        }
        if(rs){
            return ResponseData.success(msg);
        }else{
            return ResponseData.error(msg);
        }

    }

    /**
     * 删除
     *
     * @param files
     */
    private void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }


    /**
     * 删除模型
     */


    @RequestMapping("/deletem")

    @ResponseBody
    public ResponseData deleteModel(@RequestParam(value = "ruleId", required = false) String ruleId) {
        if (ToolUtil.isEmpty(ruleId)) {
            throw new ServiceException(BizExceptionEnum.REQUEST_NULL);
        }
        this.modelService.delete(ruleId);
        return SUCCESS_TIP;
    }

    /**
     * 执行模型(单机/大数据)
     */

    @RequestMapping("/run")
    @ResponseBody
    @ApiOperation(value = "执行模型", notes = "")
    public ResponseData getNeedToRunRule(@RequestParam(value = "ruleId", required = false) String ruleId) {
        Model model = modelService.selectById(ruleId);
        if (model == null) {
            return ResponseData.error("数据库异常，查询数据为空！");
        }
        String runRunId = ruleRunInsertService.createRuleRun(model, 0);
        if (Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
            return ResponseData.success(runRunId);
        } else {
            RuleProcess ruleProcess = new RuleProcess();
            ruleProcess.runSignle(runRunId);
            return ResponseData.success(runRunId);
        }
    }


    /**
     * 执行模型(pay)
     */

    @RequestMapping("/runModel")
    @ResponseBody
    @ApiOperation(value = "执行模型", notes = "")
    public ResponseData runModel(@RequestParam(value = "ruleId", required = false) String ruleId) {
        RuleDef ruleDef = ruleDefService.selectById(ruleId);
        if (ruleDef == null) {
            return ResponseData.error("数据库异常，查询数据为空！");
        }
        String runRunId = ruleRunInsertService.createRuleRunPay(ruleDef, 0);
        RuleProcess ruleProcess = new RuleProcess();
        ruleProcess.runSignle(runRunId);
        return ResponseData.success(runRunId);
    }

    /**
     * 查询执行结果/数据
     */
    @RequestMapping("/getResultData")
    @ResponseBody
    @ApiOperation(value = "查询执行数据结果", notes = "")
    public ResponseData getResultData(@RequestParam(required = false, value = "ruleRunId") String ruleRunId) {
        if (StringUtils.isEmpty(ruleRunId)) {
            return ResponseData.error("参数为空");
        }
        RuleRunViewEntity ruleRunViewEntity = ruleRunViewService.getById(ruleRunId);
        if (ruleRunViewEntity == null) {
            return ResponseData.error("查询不到执行记录");
        }

        //处理状态：1 - 初始提交；2 - 运行中；8 - 执行完成；9 - 执行异常；0 - 关联其他结果
        if ("1".equals(ruleRunViewEntity.getStatus())) {
            return ResponseData.error("分析还未执行");
        }
        if ("2".equals(ruleRunViewEntity.getStatus())) {
            return ResponseData.error("分析还在运行中");
        }
        if (!"8".equals(ruleRunViewEntity.getStatus())) {
            return ResponseData.error("分析执行异常");
        }

        ModelResultData modelDataResult = new ModelResultData();
        ruleRunViewService.initFromResult(ruleRunViewEntity.getRerultLineN(), modelDataResult);
        RuleRunViewEntityPay ruleRun = ruleRunViewPayService.selectById(ruleRunId);
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        long checkCount = ruleRun.getCheckCount();
        if (checkCount == 0 || ruleRun.getRerultCount() == 0) {
            // 取整数
            numberFormat.setMaximumFractionDigits(4);
            modelDataResult.setProportion("0.0000");
        } else {
            // 取整数
            numberFormat.setMaximumFractionDigits(4);
            String sumCount = numberFormat.format((float) ruleRun.getRerultCount() / (float) checkCount * 100);
            modelDataResult.setProportion(sumCount);
        }
        modelDataResult.setResultCount(ruleRun.getRerultCount());
        return ResponseData.success(modelDataResult);
    }

    /**
     * 查询执行结果图表
     */
    @ApiOperation(value = "", notes = "")
    @RequestMapping("/getResultChart")
    @ResponseBody
    public ResponseData getResultChart(@RequestParam("ruleRunId") String ruleRunId) {
        RuleRunViewEntity ruleRunViewEntity = ruleRunViewService.getById(ruleRunId);
        if (ruleRunViewEntity == null) {
            return ResponseData.error("查询不到执行记录");
        }

        //处理状态：1 - 初始提交；2 - 运行中；8 - 执行完成；9 - 执行异常；0 - 关联其他结果
        if ("1".equals(ruleRunViewEntity.getStatus())) {
            return ResponseData.error("分析还未执行");
        }

        if ("2".equals(ruleRunViewEntity.getStatus())) {
            return ResponseData.error("分析还在运行中");
        }
        if (!"8".equals(ruleRunViewEntity.getStatus())) {
            return ResponseData.error("分析执行异常");
        }

        if (StrUtil.isBlank(ruleRunViewEntity.getRerultChartData())) {
            return ResponseData.error("结果没有图表数据");
        }
        String id = ruleRunViewEntity.getRuleId();
        Model model = modelService.selectById(ruleRunViewEntity.getRuleId());
        if (model == null) {
            return ResponseData.error("查询不到模型数据");
        }
        //图表的样式：1 - 柱状图（竖）；2 - 柱状图（横）；3 - 饼形图
        ModelResultChart modelDataResult = new ModelResultChart();
        ruleRunViewService.initFromResult(ruleRunViewEntity.getRerultChartData(), modelDataResult);
        ruleRunViewService.initChartValue(model, modelDataResult);
        return ResponseData.success(modelDataResult);
    }

    /**
     *導出執行結果數據
     */
    @RequestMapping("/uploadResultData")
    @ApiOperation(value = "导出", notes = "")
    public void uploadResultData(@RequestParam(required = false, value = "ruleRunId") String ruleRunId,
                                 HttpServletResponse response) throws Exception {
        if (ruleRunId == null || ruleRunId == "") {
            log.error("数据为空");
        }
        ruleRunViewService.exportExcel(response, ruleRunId);
    }

    /**
     * 判断文件是否存在
     */
    @RequestMapping("/ifAsNot")
    @ApiOperation(value = "判断文件是否存在", notes = "")
    @ResponseBody
    public ResponseData ifAsNot(@RequestParam(required = false, value = "ruleRunId") String ruleRunId) throws Exception {
        if (ruleRunId == null || ruleRunId == "") {
            return ResponseData.error("数据为空");
        }
        RuleRunViewEntity ruleRunViewEntity = ruleRunViewService.ifAsNot(ruleRunId);
        String rultpath = ruleRunViewEntity.getRerultPath();

        if (rultpath == null || rultpath == "") {
            return ResponseData.error("生成文件路径失败");
        }
        File file = new File(rultpath);
        if (!file.exists()) {
            // 让浏览器用UTF-8解析数据
            return ResponseData.error("文件不存在或已过期,请重新生成");
        } else {

            return ResponseData.success("文件成功");
        }

    }

}



