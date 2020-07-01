package com.ylink.aml.modular.system.entity;

import java.util.Arrays;

public class ImgInfo {
    private Integer errno;
    private  String[] data;

    public Integer getErrno() {
        return errno;
    }

    public void setErrno(Integer errno) {
        this.errno = errno;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public ImgInfo(Integer errno, String[] data) {
        this.errno = errno;
        this.data = data;
    }

    public ImgInfo() {
    }

    @Override
    public String toString() {
        return "ImgInfo{" +
                "errno=" + errno +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
