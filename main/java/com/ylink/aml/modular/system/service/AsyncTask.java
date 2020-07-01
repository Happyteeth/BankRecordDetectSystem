package com.ylink.aml.modular.system.service;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.modular.system.service.TbsDictionaryValService;
import com.ylink.aml.modular.system.service.TbsProgParaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AsyncTask {
    private TbsProgParaService tbsProgParaService;
    private final GunsProperties gunsProperties;

    /**
     * 统计脚本执行 ，异步
     * @param tableName
     */
    @Async
    public void  execeStatic(String tableName){
        if(Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
            log.info("大数据版本，统计程序自动运行，跳过  execeStatic tableName={} ," ,tableName);
           return ;
        }
        
        String bacthNo= tbsProgParaService.selectParaValue("DATA_TABLE_STAT_BATCH_NO", tableName);
        if(StrUtil.isBlank(bacthNo)){
            log.error(" execeStatic tableName={} ,配置不存在" ,tableName);
            return  ;
        }
        String cmdStr= tbsProgParaService.selectParaValue("DATA_TABLE_STAT_SHELL", "shell");
        if(StrUtil.isBlank(bacthNo)){
            log.error("execeStatic  统计配置脚本不存在"  );
            return  ;
        }

        long curTime = System.currentTimeMillis();
        log.info("execeStatic {} {} statr..",cmdStr,bacthNo );
        try {
            String cmdRes = RuntimeUtil.execForStr(cmdStr + " " + bacthNo);
            log.info("cmdRss ={}", cmdRes);
        }catch(Exception e){
            log.error("execeStatic exption ",e);
        }

        log.info("execeStatic {} {} end cost:{}",cmdStr,bacthNo, System.currentTimeMillis()-curTime);

    }
}
