package com.ylink.aml.modular.system.model;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 *
 *
 * </p>
 *
 * @since 2018-12-07
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class DictValDto implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 值的中文名
     */

    private int valuId;

    private String vItemId;
}
