package com.ylink.aml.modular.system.entity;

/**
 * @Description:
 * @Auther: lida
 * @Date: 2019/6/21 09:32
 */

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("DATA_FILE_IMPORT")
public class DataFileImport implements Serializable {

    private static final long serialVersionUID = 5066650388336978863L;
    /**
         * �����ļ�ID
         */
        @TableId(value = "FILE_ID", type = IdType.AUTO)
        private Integer fileId;
        /**
         * �����Ŀ�����
         */
        @TableField("TABLE_NAME")
        private String tableName;
        /**
         * �����ļ���ԭʼ·��������Ŀ¼���ļ���
         */
        @TableField("ORI_PATH")
        private String oriPath;
        /**
         * ԭʼ�ļ���С����λ�ֽ�
         */
        @TableField("ORI_SIZE")
        private Long oriSize;
        /**
         * �����ļ��ϴ���HDFS��WEB��·��������Ŀ¼���ļ�����H+FILE_ID
         */
        @TableField("HDFS_PATH")
        private String hdfsPath;
        /**
         * �Ƿ�������У�0 - ���������У�1 - ��������
         */
        @TableField("IF_TITLE")
        private String ifTitle;
        /**
         * �ļ���ʽ��1 - CSV��2 - TXT
         */
        @TableField("FILE_FORMAT")
        private String fileFormat;
        /**
         * �ֶηָ��
         */
        @TableField("DELIMIT_FIELD")
        private String delimitField;
        /**
         * �зָ��
         */
        @TableField("DELIMIT_LINE")
        private String delimitLine;
        /**
         * �����У��ṩ����Ԥ��ʹ��
         */
        @TableField("LINE_TITLE")
        private String lineTitle;
        /**
         * ǰN�У��ṩ����Ԥ��ʹ��
         */
        @TableField("LINE_N")
        private String lineN;
        /**
         * ����״̬��1 - ��¼�ѱ��棬���ϴ��ļ���WEB��2 - �ļ����ϴ���WEB��3 - �ļ����ϴ���HDFS��4 - �ļ��Ѿ����У�飻5 - �ļ������Ѿ���ɵ���
         */
        @TableField("STATUS")
        private String status;
        /**
         * ������
         */
        @TableField("LINE_TOTAL")
        private Long lineTotal;
        /**
         * ��ʽ���������
         */
        @TableField("LINE_ERROR")
        private Long lineError;
        /**
         * ��ʽ�����ǰN�У��ṩ����Ԥ��ʹ��
         */
        @TableField("LINE_N_ERROR")
        private String lineNError;
        /**
         * ��ʽ�����ļ�·��������Ŀ¼���ļ������ļ�����HDFS_PATH+'_ERR'
         */
        @TableField("ERROR_PATH")
        private String errorPath;
        /**
         * ����ɹ�������
         */
        @TableField("LINE_SUCC")
        private Long lineSucc;
        /**
         * ����ʱ�䣨������
         */
        @TableLogic
        private String cDelFlag;

        @TableField("D_INSERT")
        private LocalDateTime dInsert;
        /**
         * ����ʱ�䣨����޸ģ�
         */
        @TableField("D_UPDATE")
        private LocalDateTime dUpdate;
        /**
         * �����ˣ�������
         */
        @TableField("V_INSERT_USER")
        private String vInsertUser;
    }