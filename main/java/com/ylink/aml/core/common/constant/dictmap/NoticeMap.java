
package com.ylink.aml.core.common.constant.dictmap;

import com.ylink.aml.core.common.constant.dictmap.base.AbstractDictMap;

/**
 * 通知的映射
 *
 * @author lida
 * @Date 2019-05-06 15:01
 */
public class NoticeMap extends AbstractDictMap {

    @Override
    public void init() {
        put("noticeId", "标题id");
        put("title", "标题");
        put("content", "内容");
    }

    @Override
    protected void initBeWrapped() {
    }
}
