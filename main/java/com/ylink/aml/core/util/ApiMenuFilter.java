
package com.ylink.aml.core.util;

import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.core.common.node.MenuNode;
import cn.stylefeng.roses.core.util.SpringContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * api接口文档显示过滤
 *
 * @author lida
 * @Date 2019-08-17 16:55
 */
public class ApiMenuFilter {

    public static List<MenuNode> build(List<MenuNode> nodes) {
        // 如果关闭了接口文档,则不显示接口文档菜单
        GunsProperties gunsProperties = SpringContextHolder.getBean(GunsProperties.class);
        if (!gunsProperties.getSwaggerOpen()) {
            nodes = filterMenuByName(nodes, Const.API_MENU_NAME);
        }
        return nodes;
    }

    private static List<MenuNode> filterMenuByName(List<MenuNode> nodes, String menuName) {
        List<MenuNode> menuNodesCopy = new ArrayList<>(nodes.size());
        for (MenuNode menuNode : nodes) {
            if (!menuName.equals(menuNode.getName())) {
                menuNodesCopy.add(menuNode);
            }
            List<MenuNode> childrenList = menuNode.getChildren();
            if (childrenList != null && childrenList.size() > 0) {
                menuNode.setChildren(filterMenuByName(childrenList, menuName));
            }
        }
        return menuNodesCopy;
    }

}
