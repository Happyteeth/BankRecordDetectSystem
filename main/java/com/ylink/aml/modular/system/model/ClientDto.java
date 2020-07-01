package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientDto implements Serializable {
    private Long clientId;

    private Long wechatUserId;

    private String name;

    private String mobile;

    private String nickName;

    private Integer gender;

    private String address;

    private String avatarUrl;

    private Long managerId;

    private String remarks;



}
