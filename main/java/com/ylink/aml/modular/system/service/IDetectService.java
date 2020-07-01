package com.ylink.aml.modular.system.service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author qy
 */
public interface IDetectService {

    /**
     * 一键检测批量导出
     * @param autoCheckId 批次ID
     * @param response response
     * @return
     */
    File batchExport(String autoCheckId, HttpServletResponse response);
}
