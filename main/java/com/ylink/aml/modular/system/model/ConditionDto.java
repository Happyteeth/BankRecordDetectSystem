package com.ylink.aml.modular.system.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qy
 */
@Data
public class ConditionDto implements Serializable {

    private String column;

    private String conditionType;

    private String value;

    private String dataType;

}
