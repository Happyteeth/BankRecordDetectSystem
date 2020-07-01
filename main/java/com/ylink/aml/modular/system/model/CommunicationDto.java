package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommunicationDto implements Serializable {

    private Long communicationId;

    private Long activistId;

    private Integer contactStatus;

    private Integer followUpAgain;

    private String communicationInfo;

    private Date communicationTime;

    private Integer customerWishes;


}
