package com.ylink.aml.modular.system.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.StrUtil;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylink.aml.config.properties.GunsProperties;
import com.ylink.aml.core.common.constant.Const;
import com.ylink.aml.core.common.exception.BaseException;
import com.ylink.aml.core.common.page.LayuiPageFactory;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.hive.DeleteData;
import com.ylink.aml.modular.system.dto.CheckFileDto;
import com.ylink.aml.modular.system.dto.FileDto;
import com.ylink.aml.modular.system.dto.FileViewDto;
import com.ylink.aml.modular.system.entity.DataFileImport;
import com.ylink.aml.modular.system.entity.TargetTab;
import com.ylink.aml.modular.system.entity.TargetTabCol;
import com.ylink.aml.modular.system.entity.TbsDictionaryVal;
import com.ylink.aml.modular.system.model.DataFileImportDto;
import com.ylink.aml.modular.system.service.*;
import com.ylink.aml.modular.system.util.CheckLineUtils;
import com.ylink.aml.modular.system.util.TimeString;
import com.ylink.aml.modular.system.warpper.FileWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 文件导入控制器
 * @Auther: lida
 * @Date: 2019/6/18 10:20
 */
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping("/file")
public class DataFileImportController extends BaseController {
    private static String PREFIX = "/modular/dataImport/";

    private DataFileImportService fileImportService;
    private TargetTabColService tabColService;
    private TbsDictionaryValService tbsDictionaryValService;
    private TbsProgParaService tbsProgParaService;
    private ITargetTabService iTargetTabService;

    private AsyncTask asyncTask;
    private final GunsProperties gunsProperties;
    /*
     * @Description：跳转到文件导入界面
     * @param: []
     * @return java.lang.String
     */
    @RequestMapping("")
    public String index() {
        if(Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
            return PREFIX + "fileImport_bd.html";
        }else{
            return PREFIX + "fileImport.html";
        }
    }

    @RequestMapping("fileChose")
    public String fileChose() {
            return PREFIX + "fileChose.html";
    }
    //大数据版文件导入
    //获取固定目录下的所有文件
    @RequestMapping("/getFiles")
    @ResponseBody
    public Map getFiles(@RequestParam(value="filePath",required = false) String filePath,
                        @RequestParam(value="fileName",required = false) String fileName,
                        //@RequestParam(value="path",required = false)String path,
                        @RequestParam(value="uploadFlag",required = false)String uploadFlag,
                        @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "limit", required = false) Integer limit) {

        Map map = new HashMap();
        //获取服务器文件目录
        //String filePath = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "WEB_DIR");

        if (StrUtil.isEmpty(filePath)) {
            throw new BaseException("目录不存在");
        }
        ArrayList<FileDto> fileList = new ArrayList();
        this.getFiles(filePath,fileList);
        if(StrUtil.isNotEmpty(fileName)){
            for(int i=0;i<fileList.size();i++){
                if(!fileList.get(i).getFileName().contains(fileName)){
                    fileList.remove(i);
                    i--;
                }
            }
        }
/*        if(StrUtil.isNotEmpty(path)){
            for(int i=0;i<fileList.size();i++){
                if(!fileList.get(i).getPath().contains(path)){
                    fileList.remove(i);
                    i--;
                }
            }
        }*/
        if(StrUtil.isNotEmpty(uploadFlag)){
            for(int i=0;i<fileList.size();i++){
                if(!fileList.get(i).getUploadFlag().contains(uploadFlag)){
                    fileList.remove(i);
                    i--;
                }
            }
        }
        int total=fileList.size();
        List newList=fileList.subList(limit*(page-1), ((limit*page)>total?total:(limit*page)));

        map.put("code",0);
        map.put("count",fileList.size());
        map.put("data",newList);
        return map;
    }

    //大数据文件导入
    @RequestMapping("/filePath")
    @ResponseBody
    public ResponseData filePathList(){
        List<String> pathList = new ArrayList<>();
        //获取服务器文件目录
        String filePath = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "WEB_DIR");
        pathList.add(filePath);
        List<String> filePathName = this.getFilePathName(filePath, pathList);
        return ResponseData.success(filePathName);
    }


    //大数据文件导入
    @RequestMapping("/bdImport")
    @ResponseBody
    public ResponseData bdFileImport(@RequestBody DataFileImportDto dto){
        if(StrUtil.isEmpty(dto.getDelimitField()) || StrUtil.isEmpty(dto.getTableName())
            || StrUtil.isEmpty(dto.getFileFormat()) || StrUtil.isEmpty(dto.getIfTitle()) || dto.getPathList().size()<=0){
            return ResponseData.error("参数为空");
        }
        for(String filePath:dto.getPathList()) {
            File file = FileUtil.file(filePath);
            String fileName = FileUtil.getName(file);//文件名
            Long size = FileUtil.size(file);//文件大小

            //将数据插入到文件记录表中
            DataFileImport fileImport = new DataFileImport();
            BeanUtil.copyProperties(dto, fileImport);
            fileImport.setOriPath(filePath);
            fileImport.setOriSize(size);
            fileImport.setStatus("1");
            fileImport.setDelimitLine("\r\n");
            fileImport.setDInsert(LocalDateTime.now());
            fileImport.setVInsertUser(ShiroKit.getUser().getName());
            int flag = fileImportService.addDataInfo(fileImport);
            if (flag <= 0) {
                return ResponseData.error("导入记录插入异常");
            }
        }
        return ResponseData.success();
    }

    /*
     * @Description：单机版文件导入
     * @param: [dto]
     * @return java.lang.Object
     */
    @RequestMapping("/import")
    @ResponseBody
    public ResponseData fileImport(@RequestParam("file") MultipartFile file, DataFileImportDto dto
            , HttpServletRequest request) throws Exception {

        if (file == null || file.isEmpty()) {
            return ResponseData.error("上传文件为空");
        }
        if (StrUtil.hasEmpty(dto.getTableName()) || StrUtil.hasEmpty(dto.getFileFormat())
                || StrUtil.hasEmpty(dto.getDelimitField()) || StrUtil.hasEmpty(dto.getIfTitle())) {
            return ResponseData.error("请求参数为空");
        }

        String fileName = file.getOriginalFilename();//文件名
        Long size = file.getSize();//文件大小

       /* //根据文件名和大小判断文件是否已存在
        DataFileImport result = fileImportService.selectFileByNameAndSize(fileName, size);
        if (result != null) {
            return ResponseData.error(fileName + "同名文件已存在");
        }*/

        //将数据插入到文件记录表中
        DataFileImport fileImport = new DataFileImport();
        BeanUtil.copyProperties(dto, fileImport);
        fileImport.setOriPath(fileName);
        fileImport.setOriSize(size);
        fileImport.setStatus("1");
        fileImport.setDelimitLine("\r\n");
        fileImport.setDInsert(LocalDateTime.now());
        fileImport.setVInsertUser(ShiroKit.getUser().getName());
        int flag = fileImportService.addDataInfo(fileImport);
        if (flag <= 0) {
            return ResponseData.error("导入记录插入异常");
        }

        //插入成功,获取该文件在数据导入表中的信息
        DataFileImport fileInfo = new DataFileImport();
        fileInfo.setFileId(fileImport.getFileId());

        //将文件上传到web服务器,更新该文件在信息导入表中的状态
        String filePath = this.uploadFile(file, fileInfo.getFileId(), request);
        fileInfo.setStatus("2");
        fileInfo.setHdfsPath(filePath);
        fileInfo.setDUpdate(LocalDateTime.now());

        if (!fileImportService.updateById(fileInfo)) {
            return ResponseData.error("更新状态异常");
        }

        //文件校验
        //获取目标表的字段定义 字典
        //是否校验字典值
        String ifCheckDict= tbsProgParaService.selectParaValue("PUBLIC_PARA", "IF_CHECK_DICT");
        StringBuilder sqlPreBuf = new StringBuilder("insert into "); //sql插入语句前面固定部分
        sqlPreBuf.append(dto.getTableName());
        sqlPreBuf.append("( FILE_ID"); //文件id 字段

        List<TargetTabCol> targetTabCols = tabColService.selectTabColInfo(dto.getTableName());
        for (TargetTabCol col : targetTabCols) {
            sqlPreBuf.append(",").append(col.getColumnName()); //字段名

            //根据字段的字典项id查出字典项的值id
            if (col.getVItemId() != null) { //当字段对应的字典项id不为空时
                List<TbsDictionaryVal> dictionaryList = tbsDictionaryValService.list(new QueryWrapper<TbsDictionaryVal>()
                        .eq("V_ITEM_ID", col.getVItemId()));
                if (dictionaryList != null) {
                    /**  List -> Map   */
                    col.setMapDictionaryVal(dictionaryList.stream().collect(Collectors.toMap(TbsDictionaryVal::getVItemValId, a -> a, (k1, k2) -> k1)));
                }
            }
        }
        sqlPreBuf.append(")  values ");

        String sqlPre = sqlPreBuf.toString();  //sql插入语句前面固定部分

        //读取文件内容并校验
        String errFilePath = this.createFile(StrUtil.toString(fileInfo.getFileId()), dto.getFileFormat());//错误文件地址
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        BufferedWriter errWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errFilePath), "UTF-8"));
        //BufferedWriter rigthWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errFilePath), "UTF-8"));


        int N = Integer.parseInt(tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "ORI_FIRST_ROWS"));//获取参数值,导入文件的前n行
        long lineCount = 0;//文件总行数（不包含标题行）
        long errorLineCount = 0;//格式错误的行数
        long rightLineCount = 0;//格式正确的行数
        StringBuilder lineN = new StringBuilder();//前n行
        StringBuilder errLineN = new StringBuilder();//错误的前n行

        String lineTitle = null;//标题行
        if (dto.getIfTitle().equals("1")) {//文件存在标题行时
            lineTitle = reader.readLine();//标题行
            //lineN.append(lineTitle).append("\r\n");
            //如果字段分隔符为逗号时，统一换成短竖
            if (dto.getDelimitField().equals(",")) {
                lineTitle = lineTitle.replaceAll(dto.getDelimitField(), "|");
            }
        }

        List<String> listRightLine = new ArrayList<String>(); //正确行

        String fileId = fileInfo.getFileId() + "";
        boolean hasQuotes = true;// 是否有数据引号
        String line;
        while ((line = reader.readLine()) != null) {
            lineCount++;
            if (lineCount < N) {
                lineN.append(line).append("\r\n");  //预览前N行
            }


            //检验每一行内容
            // 切分字段,将日志格式中存在t的日期替换
            String[] fields = line.split(String.format("\\%s", dto.getDelimitField()), -1);
            if (fields.length != targetTabCols.size()) {
                log.info("字段个数不匹配");
                errorLineCount++;
                if (errorLineCount <= N) {
                    errLineN.append(line).append("\r\n");  //前N行错误行
                }
                errWrite.write(line + "（字段个数不匹配:目标表字段个数为"+targetTabCols.size()+",该行字段个数为"+fields.length+")\r\n");
                if (errorLineCount % 100 == 0) {
                    errWrite.flush();
                }
            } else {
                for (int i = 0; i < fields.length; i++) {
                    // 取出字段
                    String field = fields[i];
                    String newField = null;
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(field)) {
                        TargetTabCol targetTabCol = targetTabCols.get(i);
                        String datatypes = targetTabCol.getDataType();// 数据类型匹配校验
                        if ("T".equals(datatypes)) {//时间日期类型
                            if (field.contains("t")) {
                                line = line.replace(field, field.replaceAll("t", "0"));
                            }
                        }
                    }
                }

                CheckFileDto checkOut = CheckLineUtils.checkOut(line, targetTabCols, dto.getDelimitField(), hasQuotes, ifCheckDict);
                if (checkOut.getBool()) {//正确的行
                    //rigthWrite.write(line );
                    listRightLine.add(line);

                } else {//格式错误的行写入web文件
                    errorLineCount++;
                    if (errorLineCount <= N) {
                        errLineN.append(line).append("\r\n");  //前N行错误行
                    }
                    log.info(checkOut.getErrMess());
                    errWrite.write(line+ checkOut.getErrMess()+ "\r\n");
                    if (errorLineCount % 100 == 0) {
                        errWrite.flush();
                    }
                }
            }
        }
        log.info("格式正确行数:{},错误行数::{},总行数：{}", rightLineCount, errorLineCount, lineCount);
        reader.close();


        //文件格式检验完成更新数据文件导入记录表
        fileInfo.setStatus("4");
        fileInfo.setLineTitle(lineTitle);
        fileInfo.setLineN(lineN.toString());
        fileInfo.setLineTotal(lineCount);
        fileInfo.setLineError(errorLineCount);
        fileInfo.setLineNError(errLineN.toString());
        fileInfo.setErrorPath(errFilePath);
        fileInfo.setDUpdate(LocalDateTime.now());
        fileInfo.setVInsertUser(ShiroKit.getUser().getName());
        if (!fileImportService.updateById(fileInfo)) {
            return ResponseData.error("更新状态异常");
        }


        //数据导入数据库
        long lineSucc = 0;//统计插入成功的行数
        //批次一次插入数量500
        int batchSize = Integer.parseInt(tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "INSERT_NUM"));
        //int batchSize = 5;  //批次一次插入数量

        List<String> listImpTranLine = new ArrayList<String>(); //插入转换行
        List<String> listImpOldLine = new ArrayList<String>(); //插入原始行
        for (String row : listRightLine) {
            listImpTranLine.add(getInsertSqlValue(row, fileId, dto.getDelimitField(), hasQuotes)); //插入转换好的SQL
            listImpOldLine.add(row);

            if (listImpTranLine.size() == batchSize) {
                List<String> listErrLine = new ArrayList<String>(); //错误原始行
                //插入到数据库中
                int nCurOkCnt = importBase(sqlPre, listImpOldLine, listImpTranLine, listErrLine);
                if (nCurOkCnt != listImpTranLine.size()) {
                    errorLineCount = errorLineCount + listErrLine.size();
                    for (String str : listErrLine) {
                        if (errorLineCount <= N) {
                            errLineN.append(str).append("\r\n");  //前N行错误行
                        }
                        errWrite.write(str + "\r\n");
                    }
                    errWrite.flush();
                }
                lineSucc = lineSucc + nCurOkCnt;

                listImpTranLine.clear(); //清理，重新一批次
                listImpOldLine.clear();  //清理，重新一批次
            }
        }

        //剩余部分导入
        if (listImpTranLine.size() > 0) {
            List<String> listErrLine = new ArrayList<String>(); //错误原始行
            //插入到数据库中
            int nCurOkCnt = importBase(sqlPre, listImpOldLine, listImpTranLine, listErrLine);
            if (nCurOkCnt != listImpTranLine.size()) {
                errorLineCount = errorLineCount + listErrLine.size();
                for (String str : listErrLine) {
                    if (errorLineCount <= N) {
                        errLineN.append(str).append("\r\n");  //前N行错误行
                    }
                    errWrite.write(str + "\r\n");
                }
                errWrite.flush();
            }
            lineSucc = lineSucc + nCurOkCnt;
        }


        log.info("成功条数：{}，错误条数：{}", lineSucc, errorLineCount);
        errWrite.flush();
        errWrite.close();
        //文件格完成导入更新数据文件导入记录表
        fileInfo.setStatus("5");
        fileInfo.setLineSucc(lineSucc);
        fileInfo.setLineError(errorLineCount);
        fileInfo.setLineNError(errLineN.toString());
        fileInfo.setDUpdate(LocalDateTime.now());
        if (!fileImportService.updateById(fileInfo)) {
            return ResponseData.error("更新状态异常");
        }

        asyncTask.execeStatic(dto.getTableName()); //执行统计

        return SUCCESS_TIP;
    }


    /**
     * 获取插入SQL部分数据字符串
     *
     * @param line      数据行
     * @param fileId    文件id
     * @param separator 分隔符
     * @return
     * @para hasQuotes  是否有数据引号
     */
    private static String getInsertSqlValue(String line, String fileId, String separator, boolean hasQuotes) {
        // 切分字段
        String[] fields = line.split(String.format("\\%s", separator), -1);

        StringBuilder args = new StringBuilder();
        args.append("('" + fileId + "'");
        for (String f : fields) {
            if (f.startsWith("\"")) {
                f = f.substring(1);
            }
            if (f.endsWith("\"")) {
                f = f.substring(0, f.length() - 1);
            }
            if (StrUtil.hasEmpty(f)) {
                f = null;
                args.append(",").append(f);
            } else {
                if(f.contains("'")){
                    f=f.replaceAll("'","''");
                }
                args.append(",'").append(f.replace("\"", "")).append("'"); //插入字符转义
            }
        }
        args.append(")");
        return args.toString();
    }

    /**
     * 插入到数据库中
     *
     * @param sqlPre
     * @param listOldLine
     * @param listRightLine
     * @param listErrLine
     * @return
     */
    private int importBase(String sqlPre, List<String> listOldLine, List<String> listRightLine, List<String> listErrLine) {
        StringBuilder sqlBuf = new StringBuilder();
        sqlBuf.append(sqlPre);
        int nCur = 0;
        //批量插入
        for (String strRow : listRightLine) {
            if (nCur > 0) {
                sqlBuf.append(",");
            }
            sqlBuf.append(strRow);
            nCur++;
        }

        try {
            fileImportService.insertBatch(sqlBuf.toString());
        } catch (Exception e) {
            log.info("批量出错，转单笔插入，错误信息：{}", e.toString());

            //批量出错，转单笔插入
            int n = 0;
            for (String strRow : listRightLine) {
                StringBuilder sqlBuf2 = new StringBuilder();
                sqlBuf2.append(sqlPre);
                sqlBuf2.append(strRow);
                try {
                    fileImportService.insertBatch(sqlBuf2.toString());
                } catch (Exception e2) {
                    log.info("单笔插入错误信息：{}", e2.toString());
                    String err = listOldLine.get(n)+"("+e2.toString()+")";
                    listErrLine.add(err);
                }
                n++;
            }
        }

        return listRightLine.size() - listErrLine.size();  //成功插入行数
    }

    /*
     * @Description：获取文件导入信息列表
     * @param: []
     * @return java.lang.Object
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object list(@RequestParam(value = "tableName", required = false) String tableName) {
        Page<Map<String, Object>> fileInfo = fileImportService.selectFileInfo(tableName);

        for (Map<String, Object> m : fileInfo.getRecords()) {
            for (String k : m.keySet()) {
                if(k.equals("tableName")){
                    m.put(k,this.gettableCName(m.get(k).toString()));
                }
            }
        }
        Page<Map<String, Object>> wrap = new FileWrapper(fileInfo).wrap();
        return LayuiPageFactory.createPageInfo(wrap);
    }

    /*
     * @Description：删除文件
     * @param: []
     * @return java.lang.Object
     */
    @RequestMapping("/del")
    @ResponseBody
    public ResponseData delete(@RequestParam(value = "fileId") Integer fileId) {
        if (fileId == null) {
            return ResponseData.error("参数为空");
        }
        DataFileImport dataFileImport = fileImportService.getById(fileId);
        if (dataFileImport == null) {
            return ResponseData.error("文件不存在");
        }
        dataFileImport.setDUpdate(LocalDateTime.now());
        fileImportService.updateById(dataFileImport);
        //删除文件导入信息
        fileImportService.removeById(fileId);

        if(Const.APP_VERSION_BIGDATA.equalsIgnoreCase(gunsProperties.getAppVersion())) {
            DeleteData getFilePath = new DeleteData();
            String hdfsUri = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "HDFS_URL");
            String check_suc_dir = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "CHECK_SUC_DIR");
            String check_err_dir = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "CHECK_ERR_DIR");
            String hive_url = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "HIVE_URL");
            return getFilePath.clean(fileId, dataFileImport.getTableName(), dataFileImport.getOriPath(),
                    hdfsUri, dataFileImport.getHdfsPath(), check_suc_dir, check_err_dir, hive_url);
        }else {

            //删除插入到目标表的内容
            Map<String, String> sqlMap = new HashMap<>();
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append(dataFileImport.getTableName()).append(" where FILE_ID = ").append("'" + fileId + "' ;");
            sqlMap.put("sql", sql.toString());
            log.info(sql.toString());
            fileImportService.deleteInfo(sqlMap);

            asyncTask.execeStatic(dataFileImport.getTableName()); //执行统计
            return SUCCESS_TIP;
        }
    }

    /*
     * @Description：预览前n行
     * @param: []
     * @return java.lang.Object
     */
 /*   @RequestMapping("/view/{fileId}")
    @ResponseBody
    public Object viewLineN(@PathVariable Integer fileId) {
        DataFileImport dataFileImport = fileImportService.getById(fileId);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(dataFileImport);
        return super.warpObject(new FileWrapper(stringObjectMap));
    }*/

    @RequestMapping("/download/{fileId}")
    @ResponseBody
    public void download(@PathVariable Integer fileId, HttpServletResponse response) throws IOException {
        DataFileImport fileImport = fileImportService.getById(fileId);
        String errPath = fileImport.getErrorPath();
        File file = new File(errPath);
        String fileName = file.getName();
        if (!file.exists()) {
            //如果文件不存在就跳出
            return;
        }
        //下载的文件携带这个名称
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        //文件下载类型--二进制文件
        response.setContentType("application/octet-stream");
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            fis.close();

            ServletOutputStream sos = response.getOutputStream();
            sos.write(new   byte []{( byte ) 0xEF ,( byte ) 0xBB ,( byte ) 0xBF });
            sos.write(content);

            sos.flush();
            sos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * @Description：根据表名获取字段个数
     * @param: [tableName]
     * @return int
     */
    @RequestMapping("/fieldNum")
    @ResponseBody
    public List<Map<String, Integer>> getFieldNum() {
        List<Map<String, Integer>> mapList = tabColService.countFieldNum();
        log.info(mapList.toString());
        return mapList;
    }

    /*
     * @Description：获取表名对应中文描述
     * @param: [tableName]
     * @return java.lang.Object
     */
    @RequestMapping("/tableCName")
    @ResponseBody
    public Object gettableCName(@RequestParam("tableName") String tableName) {
        if (StrUtil.isEmpty(tableName)) {
            return ResponseData.error("参数为空");
        }
        QueryWrapper<TargetTab> wrapper = new QueryWrapper();
        wrapper.select("TABLE_DESC").eq("TABLE_NAME", tableName);
        TargetTab targetTab = iTargetTabService.getOne(wrapper);
        if (targetTab == null) {
            return ResponseData.error("查询结果为空");
        }
        return targetTab.getTableDesc();
    }

    /*
     * @Description：文件上传服务器
     * @param: [file, request]
     * @return void
     */
    public String uploadFile(MultipartFile file, Integer fileId, HttpServletRequest request) throws Exception {
        //获取上传到web服务器的路径
        String uploadFilePath = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "WEB_DIR");
        // String uploadFilePath = "/home/web/upload/";
        //获取读前缀路径
        String serverIPPort = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // 得到上传时的文件名
        String orgFilename = file.getOriginalFilename();
        // 获取扩展名
        String ext = StringUtils.getFilenameExtension(orgFilename);
        // 拼接新的filename
        TimeString ts = new TimeString();
        String newFilename = "W_" + fileId + "." + ext;
        File newFile = new File(uploadFilePath + newFilename);
        // 上传
        FileUtils.writeByteArrayToFile(newFile, file.getBytes());
        return this.getBackUrl(serverIPPort, uploadFilePath, newFilename);

    }

    /*
     * @Description：获取文件回调url
     * @param: [serverName, fileName]
     * @return java.lang.String
     */
    private String getBackUrl(String serverName, String uploadFilePath, String fileName) {
        StringBuffer backUrl = new StringBuffer();
        backUrl.append(uploadFilePath).append(fileName);
        return backUrl.toString();
    }

    /*
     * @Description：创建一个错误文件路径
     * @param: [fileName]
     * @return java.lang.String
     */
    private String createFile(String id, String roleId) throws Exception {
        //格式校验失败的文件存放路径
        String errorFilePath = tbsProgParaService.selectParaValue("DATA_FILE_UPLOAD", "CHECK_ERR_DIR");
        log.info("格式校验失败的文件存放路径"+errorFilePath);
        //String errorFilePath = "/home/web/errorFile/";
        String filenameTemp = errorFilePath + "ERR_" + id;
        //1 - CSV；2 - TXT
        if ("1".equals(roleId)) {
            filenameTemp = filenameTemp + ".csv";
        } else {
            filenameTemp = filenameTemp + ".txt";
        }

        File fileDir = new File(errorFilePath);
        //如果文件夹不存在，则创建新的文件夹
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file = new File(filenameTemp);
        if (!file.exists()) {
            file.createNewFile();
        }
        return filenameTemp;
    }


    /*
     * @Description：预览前n行
     * @param: []
     * @return java.lang.Object
     */
    @RequestMapping("/view")
    @ResponseBody
    public ResponseData view(@RequestParam(value = "fileId") Integer fileId) {
        if (fileId == null) {
            return ResponseData.error("参数为空");
        }
        DataFileImport dataFileImport = fileImportService.getById(fileId);
        if (dataFileImport == null) {
            return ResponseData.error("查询结果为空");
        }
        //根据表名获取字段中文描述
        QueryWrapper<TargetTabCol> wrapper = new QueryWrapper<>();
        wrapper.select("column_desc")
                .eq("table_name", dataFileImport.getTableName())
                .orderByAsc("column_seq");
        List<Object> columnList = tabColService.listObjs(wrapper);

        if (columnList == null || columnList.size() < 1) {
            return ResponseData.error("查询结果为空");
        }
        //存放字段中文名作为表头
        LinkedHashMap<String, String> tileMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < columnList.size(); i++) {
            tileMap.put("f" + i, columnList.get(i).toString());
        }

        //存放前n行数据
        List<Map<String, String>> lineNList = new ArrayList<Map<String, String>>();
        String[] rowArray = dataFileImport.getLineN().trim().split("\r\n", 0);
        for (int i = 0; i < rowArray.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            List<String> split = StrSpliter.split(rowArray[i], dataFileImport.getDelimitField(), 0, true, false);
            log.info("分隔：" + split.toString());
            int index = 0;
            for (String val : split) {
                map.put("f" + index, val);
                index++;
            }
            lineNList.add(map);
        }
        FileViewDto fileViewDto = new FileViewDto();
        fileViewDto.setCount(lineNList.size());
        fileViewDto.setData(lineNList);
        fileViewDto.setTitle(tileMap);
        return ResponseData.success(fileViewDto);
    }


    /*
     * @Description：根据路径返回文件list
     * @param: [filePath]
     * @return java.util.ArrayList
     */
    private ArrayList getFiles(String filePath,ArrayList<FileDto> fileList){

        File file = FileUtil.file(filePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File fileIndex : files) {
                //如果这个文件是目录，则进行递归搜索
                if (!fileIndex.isDirectory()) {
                    //getFiles(fileIndex.getPath(),fileList);
                    log.info("文件名："+fileIndex.getName());
                    log.info("文件路径："+fileIndex.toString());
                    FileDto dto = new FileDto();
                    dto.setFileName(FileUtil.getName(fileIndex));
                    dto.setSize(FileUtil.size(fileIndex));
                    dto.setType(StringUtils.getFilenameExtension(FileUtil.getName(fileIndex)));
                    dto.setTime(FileUtil.lastModifiedTime(fileIndex));
                    //String path = fileIndex.getParent().replace(filePath,"./");
                    dto.setPath(fileIndex.getParent());
                    //dto.setPath(fileIndex.getParentFile().getName());
                    //判断文件是否上传过
                    List<DataFileImport> result = fileImportService.selectFileByNameAndSize(fileIndex.toString(), FileUtil.size(fileIndex));
                    if(result.size()>0){
                        dto.setUploadFlag("是");
                    }else {
                        dto.setUploadFlag("否");
                    }
                    //如果文件是普通文件，则将文件句柄放入集合中
                    fileList.add(dto);
                }
            }
        }
        return fileList;
    }

    private List<String> getFilePathName(String filePath, List<String> pathList){
        File file = FileUtil.file(filePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File fileIndex : files) {
                //如果这个文件是目录，则进行递归搜索
                if (fileIndex.isDirectory()) {
                    String path = fileIndex.getPath().replace("\\", "/");
                    pathList.add(path);
                    getFilePathName(fileIndex.getPath(),pathList);
                }
            }
        }
        return pathList;
    }
    //根据文件名和大小来判断文件是否上传过
    @RequestMapping("/exit")
    @ResponseBody
    public ResponseData fileIsExist(@RequestParam("fileName") String fileName,@RequestParam("fileSize")Long fileSize){
        if (StrUtil.hasEmpty(fileName) || fileSize == null) {
            return ResponseData.error("请求参数为空");
        }
        //根据文件名和大小判断文件是否已存在
        List<DataFileImport> result = fileImportService.selectFileByNameAndSize(fileName, fileSize);
        if (result.size()>0) {
            return ResponseData.error(fileName + "同名文件已存在,是否继续上传");
        }
        return ResponseData.success();
    }
}


