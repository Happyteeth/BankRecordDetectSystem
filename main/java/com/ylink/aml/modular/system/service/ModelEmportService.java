package com.ylink.aml.modular.system.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylink.aml.core.common.exception.BizExceptionEnum;
import com.ylink.aml.core.shiro.ShiroKit;
import com.ylink.aml.modular.system.dto.ModelEmportDto;
import com.ylink.aml.modular.system.dto.ModelInfoEmportDto;
import com.ylink.aml.modular.system.dto.ModelParaEmportDto;
import com.ylink.aml.modular.system.entity.Model;
import com.ylink.aml.modular.system.entity.RuleParaDef;
import com.ylink.aml.modular.system.mapper.ModelMapper;
import com.ylink.aml.modular.system.mapper.RuleParaDefMapper;
import com.ylink.aml.modular.system.mapper.TbsDictionaryValMapper;
import com.ylink.aml.modular.system.util.YlCollUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型导出导入  《json格式
 */
@Service
@AllArgsConstructor
public class ModelEmportService extends ServiceImpl<ModelMapper, Model> {
    private RuleParaDefMapper ruleParaDefMapper;
    private TbsDictionaryValMapper tbsDictionaryValMapper;

    /**
     * 系统名称
     */
    private static final String  JSON_VALUE_SOURE ="ylaml";
    /**
     * 用途
     */
    private static final String  JSON_VALUE_PURPOSE ="model";
    /**
     * 版本
     */
    private static final String  JSON_VALUE_VERSION  ="1.00";



    /**
     * 获取表头 字段对应位置map
     * @param mapFields
     * @param listHead
     */
    private Map<Integer,String >getHeadFieldMatchMap(Map<String,String> mapFields, List<Object>  listHead){
        Map<String,String > mapModFiledDef = IterUtil.toMap(mapFields.values(),mapFields.keySet());
        Map<Integer,String > mapHeadFiled = MapUtil.newHashMap(); //表格字段对应 字段名
        int size = listHead.size();
        for (int i =0 ; i <size; i ++){
            String str = String.valueOf(listHead.get(i) );
            if( mapModFiledDef.containsKey( str) ){
                mapHeadFiled.put(i,mapModFiledDef.get(listHead.get(i)));
            }
        }
        return  mapHeadFiled;
    }


    /**
     * 删除
     *
     * @param files
     */
    private void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 导入模型数据
     * @param fileName
     * @param file
     * @return
     * @throws Exception
     */
    public int batchImport(String fileName, File file) throws Exception {
        int nSize =0;

            String userName = ShiroKit.getUser().getName(); //当前用户
            JSONObject jsonObject=JSONUtil.readJSONObject( file, Charset.forName("utf-8"));

             ModelEmportDto modelEmportDto = BeanUtil.toBean(jsonObject,ModelEmportDto.class);

        ////系统名称验证
            if( ! JSON_VALUE_SOURE.equals( modelEmportDto.getSource()) ){
                throw new ServiceException(BizExceptionEnum.MODEL_UPLOAD_SOURCE_ERROR);
            }

            //用途验证
            if( ! JSON_VALUE_PURPOSE.equals(modelEmportDto.getPurpose())){
                throw new ServiceException(BizExceptionEnum.MODEL_UPLOAD_PURPOSE_ERROR);
            }

            //数据校验
            if(modelEmportDto.getData()==null){
                throw new ServiceException(BizExceptionEnum.MODEL_UPLOAD_DATA_ERROR);
            }

             int rowSize= modelEmportDto.getData().size();
             Map<String,String> mapImpModel= MapUtil.newHashMap( rowSize); //导入模型记录

            //查询数据库现所有模型记录
            List<Model> modList =baseMapper.selectList(new QueryWrapper<>());
            Map<String,Model> mapModelLog = modList.stream().collect(Collectors.toMap(Model::getRuleId, a -> a,(k1, k2)->k1));

            for (ModelInfoEmportDto modelDto: modelEmportDto.getData() ){
                Model model =new Model();
                BeanUtil.copyProperties(modelDto ,model );

                long curTime =System.currentTimeMillis();
                model.setVUpdateUser(userName); //修改用户
                model.setDUpdate(new Timestamp(curTime));//修改时间

                //重复，则更新
                if( mapModelLog.containsKey(model.getRuleId())){
                        //throw new Exception("请修改数据中id，有id已经存在数据库中！");
                    baseMapper.updateById(model);
                }
               else{
                    model.setVInsertUser( userName);  //插入用户
                    model.setDInsert(new Timestamp(curTime)); //插入时间
                    baseMapper.insert(model);
                }
                
                RuleParaDef queryPara =new RuleParaDef();
                queryPara.setRuleId(model.getRuleId());
                ruleParaDefMapper.delete(new QueryWrapper<>(queryPara));
                for(ModelParaEmportDto paraDto:modelDto.getListPara()){
                    RuleParaDef ruleParaDef =new RuleParaDef();
                    BeanUtil.copyProperties(paraDto ,ruleParaDef );
                    ruleParaDef.setRuleId(model.getRuleId());

                    curTime =System.currentTimeMillis();
                    ruleParaDef.setVUpdateUser(userName); //修改用户
                    ruleParaDef.setDUpdate(new Timestamp(curTime));//修改时间
                    ruleParaDef.setVInsertUser( userName);  //插入用户
                    ruleParaDef.setDInsert(new Timestamp(curTime)); //插入时间

                    ruleParaDefMapper.insert(ruleParaDef);
                }




                mapImpModel.put(model.getRuleId(),model.getRuleId()); //添加到导入模型记录
            }


            //删除导入模型参数记录
            for (Map.Entry<String, String> entry : mapImpModel.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            }

            /*

            //第二个sheet  模型参数 导入
            for (int i=1;i<readLlist.size();i++){
                List<Object> ob=readLlist.get(i);
                RuleParaDef ruleParaDef =new RuleParaDef();
                List<RuleParaDef> ruledef=ruleParaDefMapper.selectList(new QueryWrapper<>());
                for (RuleParaDef ParaDef: ruledef) {
                    if(ParaDef.getRuleId()==ob.get(1)){
                        throw new Exception("请修改数据中id，有id已经存在数据库中！");
                    }
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                ruleParaDef.setVUpdateUser(String.valueOf(ob.get(1)));
                ruleParaDef.setParaValue(String.valueOf(ob.get(2)));
                ruleParaDef.setDInsert(new Timestamp(System.currentTimeMillis()));
                ruleParaDef.setDUpdate(new Timestamp(System.currentTimeMillis()));
                ruleParaDef.setVInsertUser(String.valueOf(ob.get(5)));
                ruleParaDef.setRuleId(String.valueOf(ob.get(6)));
                ruleParaDef.setParaString(String.valueOf(ob.get(7)));
                ruleParaDef.setParaDesc(String.valueOf(ob.get(8)));
                ruleParaDefMapper.insert(ruleParaDef);
            }
            **/

        return nSize;


    }


    public void Excellist(HttpServletResponse response) throws Exception {

        ModelEmportDto modelEmportDto =new ModelEmportDto();
        modelEmportDto.setSource(JSON_VALUE_SOURE);  //系统名称
        modelEmportDto.setPurpose(JSON_VALUE_PURPOSE); //用途
        modelEmportDto.setVersion(JSON_VALUE_VERSION);//版本

        modelEmportDto.setTime(DateUtil.now()); //导出时间
        modelEmportDto.setUser(ShiroKit.getUser().getName());  //用户

        //模型基本信息
        List<Model> sellerListP = baseMapper.findexcellist();

        List<ModelInfoEmportDto> listModelInfoEmport = new ArrayList<ModelInfoEmportDto>();
        for(Model model:sellerListP) {
            ModelInfoEmportDto  dto= new ModelInfoEmportDto();
            BeanUtil.copyProperties(model,dto);
            //模型参数信息

            RuleParaDef ruleParaDef =new RuleParaDef();
            ruleParaDef.setRuleId(model.getRuleId());
            List<RuleParaDef> sellerListE = ruleParaDefMapper.selectList(new QueryWrapper<>(ruleParaDef));


            dto.setListPara(YlCollUtil.copyList(sellerListE, ModelParaEmportDto.class));
            listModelInfoEmport.add(dto);
        }

        modelEmportDto.setData(listModelInfoEmport);


        response.setContentType("application/text ;charset=utf-8");
        String codedFileName = java.net.URLEncoder.encode("model_"+DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT), "UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+ codedFileName + ".mod");

        ServletOutputStream sos = response.getOutputStream();

        String jsonStr= JSONUtil.toJsonStr(modelEmportDto);
        sos.write(jsonStr.getBytes());

        sos.flush();
        sos.close();

    }



}