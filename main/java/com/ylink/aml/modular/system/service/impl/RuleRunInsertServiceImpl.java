package com.ylink.aml.modular.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.ylink.aml.core.common.exception.BaseException;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.entity.*;
import com.ylink.aml.modular.system.mapper.*;
import com.ylink.aml.modular.system.service.RuleRunInsertService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/7/10 11:12
 */

@Service
@Slf4j
@AllArgsConstructor
public class RuleRunInsertServiceImpl implements RuleRunInsertService {
    private RuleParaDefMapper ruleParaDefMapper;
    private RuleRunMapper ruleRunMapper;
    private RuleRunParaMapper ruleRunParaMapper;
    private RuleRunPayMapper ruleRunPayMapper;

    /**
     * 生成执行实例
     * @param model
     * @param autoCheckId
     * @return
     */
    @Override
    public String  createRuleRun(Model model, int autoCheckId) {
        //当前操作用户账号
        String userName = ShiroKit.getUser().getAccount();
        //模型执行ID
        String  ruleRunId = IdUtil.simpleUUID();
        //当前时间
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        RuleRunD ruleRun = new RuleRunD();
        //一件检测优先级
        if(autoCheckId!=0){
            ruleRun.setRulePriority(30);
        }else if(model.getRuleType().equals("1") && autoCheckId==0){
            // 数据分析设计优先级
            ruleRun.setRulePriority(10);
        }else if(model.getRuleType().equals("2") && autoCheckId==0){
            // 模型执行优先级
            ruleRun.setRulePriority(20);
        }
        //模型执行ID
        ruleRun.setRuleRunId(ruleRunId);
        //规则ID
        ruleRun.setRuleId(model.getRuleId());
        //一键检测批次ID
        ruleRun.setAutoCheckId(autoCheckId);
        //规则检测执行语言
        ruleRun.setTaskProgram(model.getTaskProgram());
        // 处理状态：1 - 初始提交；2 - 运行中；8 - 执行完成；9 - 执行异常；0 - 关联其他结果'
        ruleRun.setStatus("1");
        // 提交执行时间
        ruleRun.setSubmitTime(timestamp);
        //操作时间（新增）
        ruleRun.setDInsert(timestamp);
        //操作时间（最后修改）
        ruleRun.setDUpdate(timestamp);
        //操作人（新增）
        ruleRun.setVInsertUser(userName);
        //原始_数据SQL或程序
        String ruleProg = model.getRuleProg();
        //原始-图表SQL或程序
        String chartProg = model.getChartProg();
        //插入执行模型例子参数配置
        List<RuleParaDef> list = ruleParaDefMapper.selectByIdl(model.getRuleId());
        //每个rule_def不一定有rule_para_def
//        if(CollUtil.isEmpty(list)){
//            throw new BaseException("list is empty");
//        }
        for (RuleParaDef ruleParaDef : list) {
            if ((StrUtil.isEmpty(ruleParaDef.getParaString()) ) && (StrUtil.isEmpty(ruleParaDef.getParaValue()))) {
                throw new BaseException("数据参数配置不对");
            } else {
                String newRuleProg = ruleProg.replace(ruleParaDef.getParaString(), ruleParaDef.getParaValue());
                String  newChartProg = chartProg.replace(ruleParaDef.getParaString(), ruleParaDef.getParaValue());
                ruleProg= newRuleProg;
                chartProg=newChartProg;

                Rulerunpara rulerunpara = new Rulerunpara();
                rulerunpara.setDInsert(timestamp);
                rulerunpara.setRuleRunId( ruleRunId);
                rulerunpara.setParaString(ruleParaDef.getParaString());
                rulerunpara.setParaValue(ruleParaDef.getParaValue());
                ruleRunParaMapper.insert(rulerunpara);
            }
        }
        //数据SQL或程序
        ruleRun.setRuleProg(ruleProg);
        //图表SQL或程序
        ruleRun.setChartProg(chartProg);
        ruleRun.setRuleCount(model.getRuleCount());
        ruleRunMapper.insert(ruleRun);
        return ruleRunId;
    }

    @Override
    public String createRuleRunPay(RuleDef ruleDef, int autoCheckId) {
        long startTime=System.currentTimeMillis();
        String userName = ShiroKit.getUser().getAccount(); //当前账号
        String  ruleRunId = IdUtil.simpleUUID();  //执行ID
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());  //当前时间
        RuleRunPay ruleRunpay = new RuleRunPay();
        //一件检测优先级
        if(autoCheckId!=0){
            ruleRunpay.setRulePriority(30);
        }else if(ruleDef.getRuleType().equals("1") && autoCheckId==0 ){
            //数据分析设计优先级
            ruleRunpay.setRulePriority(10);
        }else if(ruleDef.getRuleType().equals("2") && autoCheckId==0){
            //模型执行优先级
            ruleRunpay.setRulePriority(20);
        }
        ruleRunpay.setRuleRunId(ruleRunId);  //执行ID
        ruleRunpay.setRuleId(ruleDef.getRuleId()); //规则ID
        ruleRunpay.setAutoCheckId(autoCheckId);//一键检测批次ID
        ruleRunpay.setTaskProgram(ruleDef.getTaskProgram());//规则检测执行语言
        ruleRunpay.setStatus("1"); // 处理状态：1 - 初始提交；2 - 运行中；8 - 执行完成；9 - 执行异常；0 - 关联其他结果'
        ruleRunpay.setSubmitTime(timestamp); // 提交执行时间

        ruleRunpay.setDInsert(timestamp);//操作时间（新增）'
        ruleRunpay.setDUpdate(timestamp);//操作时间（最后修改）
        ruleRunpay.setVInsertUser(userName);//操作人（新增）

        String ruleProg = ruleDef.getRuleProg(); //原始_数据SQL或程序
        String chartProg = ruleDef.getChartProg(); //原始-图表SQL或程序
        String ruleCount=ruleDef.getRuleCount();//检测的数据量SQL或程序
        //插入执行模型例子参数配置
        long getTime=System.currentTimeMillis();
        List<RuleParaDef> list = ruleParaDefMapper.selectByIdl(ruleDef.getRuleId());
        long getEndTime=System.currentTimeMillis();
        log.info("查询结束时间： "+(getEndTime-getTime)+"ms");
        for (RuleParaDef ruleParaDef : list) {
            if ((ruleParaDef.getParaString() == null || ruleParaDef.getParaString() == "") && (ruleParaDef.getParaValue() == null || ruleParaDef.getParaValue() == "")) {
                throw new BaseException("数据参数配置不对");
            } else {
                String newRuleProg = ruleProg.replace(ruleParaDef.getParaString(), ruleParaDef.getParaValue());
                String  newChartProg = chartProg.replace(ruleParaDef.getParaString(), ruleParaDef.getParaValue());
                String  newruleCount = ruleCount.replace(ruleParaDef.getParaString(), ruleParaDef.getParaValue());

                ruleProg= newRuleProg;
                chartProg=newChartProg;
                ruleCount=newruleCount;

                Rulerunpara rulerunpara = new Rulerunpara();
                rulerunpara.setDInsert(timestamp);
                rulerunpara.setRuleRunId(ruleRunId);
                rulerunpara.setParaString(ruleParaDef.getParaString());
                rulerunpara.setParaValue(ruleParaDef.getParaValue());
                ruleRunParaMapper.insert(rulerunpara);
            }
        }

        ruleRunpay.setRuleProg(ruleProg);  //数据SQL或程序
        ruleRunpay.setChartProg(chartProg);  //图表SQL或程序
        ruleRunpay.setRuleCount(ruleCount);//检测的数据量SQL或程序

        long insertTime=System.currentTimeMillis();
        ruleRunPayMapper.insert(ruleRunpay);
        long inserEndTime=System.currentTimeMillis();
        log.info("插入结束时间： "+(inserEndTime-insertTime)+"ms");

        long endTime=System.currentTimeMillis();
        log.info("循环结束时间： "+(endTime-startTime)+"ms");
        return ruleRunId;
    }
}