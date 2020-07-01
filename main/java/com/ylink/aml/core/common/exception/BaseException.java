package com.ylink.aml.core.common.exception;

import cn.stylefeng.roses.kernel.model.exception.AbstractBaseExceptionEnum;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;

public class BaseException  extends ServiceException {

    public BaseException(Integer code, String errorMessage) {
        super(code, errorMessage);
    }

    public BaseException(String errorMessage) {
        super(400, errorMessage);
    }
}
