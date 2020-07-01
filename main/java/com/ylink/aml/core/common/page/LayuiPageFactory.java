
package com.ylink.aml.core.common.page;

import cn.stylefeng.roses.core.util.HttpContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Layui Table默认的分页参数创建
 *
 * @author lida
 * @Date 2019-04-05 22:25
 */
public class LayuiPageFactory {

    /**
     * 获取layui table的分页参数
     *
     * @author lida
     * @Date 2019/1/25 22:13
     */
    public static Page defaultPage() {
        HttpServletRequest request = HttpContext.getRequest();

        //每页多少条数据
        int limit = Integer.valueOf(request.getParameter("limit"));

        //第几页
        int page = Integer.valueOf(request.getParameter("page"));

        return new Page(page, limit);
    }

    /**
     * 创建layui能识别的分页响应参数
     *
     * @author lida
     * @Date 2019/1/25 22:14
     */
    public static LayuiPageInfo createPageInfo(IPage page) {
        LayuiPageInfo result = new LayuiPageInfo();
        result.setCount(page.getTotal());
        result.setData(page.getRecords());
        return result;
    }

    /**
     * 创建layui能识别的分页响应参数
     *
     * @author lida
     * @Date 2019/1/25 22:14
     */
    public static LayuiPageInfo success(List list) {
        LayuiPageInfo result = new LayuiPageInfo();
        if(list!=null) {
            result.setCount(list.size());
        }else{
            result.setCount(0);
        }
        result.setData(list);
        return result;
    }

    /**
     * 失败返回
     * @param msg
     * @return
     */
    public static  LayuiPageInfo  error(String msg){
        LayuiPageInfo info =new LayuiPageInfo();
        info.setCode(10);
        info.setCount(0);
        info.setMsg(msg);
        return  info;
    }


}
