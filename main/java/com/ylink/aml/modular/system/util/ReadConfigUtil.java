package com.ylink.aml.modular.system.util;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/8/26 15:53
 */
/**
 * 获取配置文件里面的内容
 * @param
 * @param
 * @return
 */
public class ReadConfigUtil {
    public static Object getCommonYml(String key, String proName) {
        Resource resource = new ClassPathResource(proName);
        Properties properties = null;
        try {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(resource);
            properties = yamlFactory.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return properties.get(key);
    }

}
