package com.ylink.aml.modular.system.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=false)
public class Modelvo implements Serializable {
    String pid;
    String name;
}
