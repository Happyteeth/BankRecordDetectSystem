
package com.ylink.aml;

import cn.stylefeng.roses.core.config.WebAutoConfiguration;
import com.cfss.RuleProcess;
import com.cfss.monitor.RuleMonitorProcess;
import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * SpringBoot方式启动类
 *
 * @author lida
 * @Date 2019/5/21 12:06
 */
@SpringBootApplication(exclude = {WebAutoConfiguration.class})
@EnableAsync
public class GunsApplication {

    private final static Logger logger = LoggerFactory.getLogger(GunsApplication.class);

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(GunsApplication.class, args);
        logger.info(GunsApplication.class.getSimpleName() + " is success!");

      if(true) {
            //ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            //executorService.scheduleAtFixedRate(new RuleProcess(), 1, 2, TimeUnit.SECONDS);
            //防止上次一键检测没有跑完
            GunsProperties  gunsProperties=applicationContext.getBean(GunsProperties.class);
            if(Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
                logger.info("当前版本是大数据版本，模型执行在大数据程序中管理");
            }else {
                //logger.info("上次应用一键检测没有跑完的任务，继续");
                Thread thead = new Thread(new RuleProcess());
                Thread thread2 = new Thread(new RuleMonitorProcess());
                thead.start();
                thread2.start();
            }
        }
    }
}
