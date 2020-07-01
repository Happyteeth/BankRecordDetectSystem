
package com.ylink.aml.modular.system.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 用户传输bean
 *
 * @author lida
 * @Date 2019/5/5 22:40
 */
@Data
public class UserDto {

    private Long userId;
    private String account;
    private String password;
    private String name;
    private String sex;
    /*private String email;
    private String phone;
    private String roleId;*/
    //private Long deptId;
    private String status;
    private String avatar;

}
