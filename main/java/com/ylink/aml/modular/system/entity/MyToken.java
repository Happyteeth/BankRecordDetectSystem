package com.ylink.aml.modular.system.entity;

import org.apache.shiro.authc.UsernamePasswordToken;

public class MyToken extends UsernamePasswordToken {
    private String openId;
    private boolean hasAccount;

    public MyToken(String openId) {
        this.openId = openId;
        this.hasAccount = false;
    }

    public MyToken(String username, char[] password, String openId, boolean hasAccount) {
        super(username, password);
        this.openId = openId;
        this.hasAccount = hasAccount;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public boolean isHasAccount() {
        return hasAccount;
    }

    public void setHasAccount(boolean hasAccount) {
        this.hasAccount = hasAccount;
    }


}