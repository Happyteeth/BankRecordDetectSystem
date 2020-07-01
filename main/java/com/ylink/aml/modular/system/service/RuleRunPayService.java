package com.ylink.aml.modular.system.service;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.modular.system.dto.ModelResultChart;
import com.ylink.aml.modular.system.dto.ModelResultData;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleRunPay;
import com.ylink.aml.modular.system.entity.RuleRunViewEntity;
import com.ylink.aml.modular.system.mapper.RuleRunPayMapper;
import com.ylink.aml.modular.system.mapper.RuleRunViewMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 *
 * </p>
 *
 * @since 2019-06-07
 */
@Service
@Slf4j
public class RuleRunPayService extends ServiceImpl<RuleRunPayMapper, RuleRunPay> {
    @Autowired
    private ModelService modelService;



}