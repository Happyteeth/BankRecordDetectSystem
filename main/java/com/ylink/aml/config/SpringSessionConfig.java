
package com.ylink.aml.config;

import com.ylink.aml.config.properties.GunsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * spring session配置
 *
 * @author lida
 * @Date 2019-07-13 21:05
 */
//@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)  //session过期时间  如果部署多机环境,需要打开注释
@ConditionalOnProperty(prefix = GunsProperties.PREFIX, name = "spring-session-open", havingValue = "true")
public class SpringSessionConfig {

}
