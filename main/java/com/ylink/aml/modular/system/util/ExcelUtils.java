package com.ylink.aml.modular.system.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

/**
 * @author qy
 */
@Slf4j
public class ExcelUtils  {

    public static boolean evaluateReportToExcel(Workbook workbook, Sheet sheet, String chartCode, String imgFilePath, Integer beginRows) {
        if (StringUtils.isEmpty(chartCode)) {
            log.error("报表base64编码为空，添加失败");
            return false;
        }
        //生成图片
        try {
            OutputStream outputStream = new FileOutputStream(new File(imgFilePath));
            String[] codes = chartCode.split(",");
            String baseCode = codes[1];
            //base64解码
            byte[] buffer = Base64.getDecoder().decode(baseCode);
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            log.error("生成图片失败，{}", e.getMessage());
            return false;
        }
        //开始将图片保存到Excel
        if (saveImgToExcel(workbook, sheet, imgFilePath, beginRows)) {
            return true;
        }
        return false;
    }

    public static boolean saveImgToExcel(Workbook workbook, Sheet sheet, String imgFilePath, Integer beginRows) {
        //将图片写入到流中
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage bImage;
        try {
            bImage = ImageIO.read(new File(imgFilePath));
            ImageIO.write(bImage, "PNG", outputStream);
            //利用HSSFPatriarch或XSSFDrawing将图片写入EXCEL
            Drawing drawing = sheet.createDrawingPatriarch();
            //偏移量  这个有点恶心， 这个单位直接以万 计， 10000以下 基本设了等于没设。原因不明 ，操作2003 的 HSSF 是正常的比例
            ClientAnchor anchor = new XSSFClientAnchor(5 * 10000, 0, 100,  100, 0, beginRows + 2, 10, beginRows + 24);
            drawing.createPicture(anchor, workbook.addPicture(outputStream.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG));
        } catch (Exception e) {
            log.error("添加图片到Excel失败，imgFilePath： {}", imgFilePath);
            return false;
        }
        return true;
    }
}
