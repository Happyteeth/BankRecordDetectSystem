layui.use(['layer', 'form', 'table', 'tree', 'laydate', 'admin', 'ax', 'ztree', 'upload', 'element', 'echarts', 'FileSave','mods'], function () {
    var $ = layui.$;
    var layer = layui.layer;
    var form = layui.form;
    var table = layui.table;
    var tree = layui.tree
        , util = layui.util;
    var $ax = layui.ax;
    var laydate = layui.laydate;
    var admin = layui.admin;
    var $ZTree = layui.ztree;
    var upload = layui.upload;
    var element = layui.element;
    var eCharts = layui.echarts;
    var reportParentDiv = document.getElementById('data_report');
    var MgrUser = {}
   let fileSave = layui.FileSave;
    var mods = layui.mods;
    let createdChart = "";
    /**
     * 模型管理
     */
    var Model = {
        tableId: "modelTable",
        ruleId: "",
        vItemValId1: "",
        vItemValId2: "",
        vValName1: "",
        vValName2: "",
        vItemId: "",
        ruleName: "",
        ruleRunId:"",
        condition: {
            modelId: ""
        },
        url:'',  //树形图数据接口
        edition:'', //版本类型  pay支付版  model单机版和大数据版
        export:{
            tableCheck:false,
            chartCheck:false,
        }
    };

    var uploadInst = upload.render({
        elem: '#testListAction' //绑定元素
        , url: Feng.ctxPath + "/mod/batchImport" //上传接口
        , accept: 'file' //普通文件
        , done: function (res) {
           /* //上传完毕回调
          if( Model.edition === 'pay'){
                 Model.edition = 'pay'
                 Model.url = '/mod/payztree'
             }else{
                 Model.edition = 'model'
                 Model.url = '/mod/modelztree'
             }*/
              Model.edition = 'model'
              Model.url = '/mod/modelztree'
             Model.initTree();
             Feng.success("导入成功!");
        }
        , error: function () {
            //请求异常回调
             Feng.error("导入失败!");
        }
    });
    /**
     * 初始化树形目录
     */
    Model.initTree = function (ruleName,isSearch) {
        var index = layer.load(2);
        $.ajax({
            type: "POST",
            url: Feng.ctxPath + Model.url,
            data: {
                'ruleName': ruleName ? ruleName : ''
            },
            success: function (result) {
                if(isSearch && ruleName!==''){
                    for(var i of result){ //用于查询结果TREE展开
                        i.spread = true
                        var chd = i.children && i.children.length > 0;
                        if(chd){
                            var chdList = i.children
                            for(var j of chdList){
                                j.spread = true
                            }
                        }
                    }
                }
                console.log(result)
                layer.close(index);
                tree.render({
                    elem: '#modeTree'
                    , data: result
                    , showLine: false  //是否开启连接线
                    , onlyIconControl: false  //是否仅允许节点左侧图标控制展开收缩
                    , click: function (obj) {
                        var that = event.target.nodeName;
                        var htmlColor = obj.elem.children(":first").find('.layui-tree-txt')
                        var icon = obj.elem.children(":first").find($('.layui-tree-iconClick .layui-tree-iconArrow '))
                        if(obj.state === 'open' && that !== 'I' && obj.data.children.length>0){ //小图标转换
                            icon.addClass('add')
                            htmlColor.css({'color':'#0C79FD'})
                        }else if(obj.state === 'close' && that !== 'I'){
                            icon.removeClass('add')
                            htmlColor.css({'color':'#555'})
                        }
                        if (that === 'I') { //新增
                            event.stopPropagation();
                            if( Model.edition === 'pay'){
                                Model.openAddModel(obj,'添加模型名');
                            }else{
                                Model.openAddModel(obj,'添加检查项');
                            }
                            return
                        }
                        if (obj.data.type === "model") { //如为 model则查询详情
                            event.stopPropagation();
                            htmlColor.css({'color':'#555'})
                            Model.threeData(obj);
                            Model.ruleId = obj.data.id;
                            return
                        }
                        return false;
                    }
                });
                if(isSearch && ruleName!==''){
                    $('#modeTree').find($('.layui-tree-iconArrow')).addClass('add')
                }
                if(Model.edition === 'model' || Model.edition === 'pay'){ //添加二级
                    var p = "<i class=\"add-icon layui-icon layui-icon-add-circle\"></i>";
                    var html = $('.layui-tree>.layui-tree-set>div:first-child .layui-tree-txt');
                    html.append(p);
                }
                var elemList = $('.layui-tree-txt');
                $.each(elemList, function (i, mes) {
                    mes.title = mes.textContent
                });
            },
            error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });
    }


    $(document).ready(function () {
        //获取用户详情
        var ajax = new $ax(Feng.ctxPath + "/system/currentUserInfo",function (res) {
            console.log(res)
            if(res.success){
                layui.sessionData('userInfo', {key: 'userInfo', value:res.data}); //存储查看检测结果判断字符
                Model.edition = layui.sessionData('userInfo').userInfo.edition;
               /* if(res.data.edition === 'pay'){
                    Model.edition = 'pay'
                    Model.url = '/mod/payztree'
                }else{
                    Model.edition = 'model'
                    Model.url = '/mod/modelztree'
                }*/
                Model.url = '/mod/modelztree'
            }
        },function (res) {
            Model.edition = 'model'
            Model.url = '/mod/modelztree'
        });
        ajax.start();
        Model.initTree();
    });

    /**
     * 点击查询按钮
     */
    Model.search = function () {
        var name;
        name = $("#key").val();
        Model.initTree(name,true);
    };

    $('#key').on('keyup', function(event) {
        var name = $("#key").val();
        if (event.keyCode == "13") {
            Model.initTree(name,true);
        }
        else if(event.keyCode == "8" || event.keyCode == "46"){
            if(name === '' || name === undefined){
                Model.initTree(name,true);
            }
        }
    });
    $('#search').click(function () {
        Model.search()
    })

    // 搜索按钮事件
    // $('#key').blur(function () {
    //     Model.search();
    // });


    /**
     * 弹出添加树形目录
     */
    Model.openAddModel = function (obj,tit) {
        var type = $(this).data('type');
        layer.open({
            type: 1,
            title: tit,
            id: 'add' + type,
            area: ['40%', '37%'],
            content: $('#addCon'),
            shade: .2,//不显示遮罩
            success: function (layero, index) {
                $("#addFirst").val(obj ? obj.data.title : '')
                $("#vItemValId").val('')
                $("#addPid").val(obj ? obj.data.id : '')
            }
        });

    };


    /**
     * 查询三级菜单详情
     */
    Model.threeData = function (obj) {
        if (!obj.data.id) return;
        console.log(obj);

        Model.ruleId = obj.data.id;
        $.ajax({
            type: "POST",
            url: Feng.ctxPath + "/mod/modelselect?ruleId=" + Model.ruleId,
            success: function (data) {
                if (data) {
                    form.val('modelForm', {
                        "ruleName": data.ruleName
                        ,"ifAutoCheck": data.ifAutoCheck? data.ifAutoCheck:'1'
                        ,"modelDesc":data.modelDesc
                        ,"vValName": data.vValName
                        ,"secondList": data.secondList
                        ,"modelSql": data.modelSql
                        ,"firstList":data.firstList
                        ,"ruleId":data.ruleId
                    }); //同步数据
                    Model.firstData(data.modelType1ItemValId,data.modelType2ItemValId); //调用所在一级数据
                    Model.ruleName = data.ruleName;
                    Model.modelDesc = data.modelDesc;
                    Model.modelSql = data.modelSql;
                }
                console.log(data);
            },
            error: function () {
            }
        });

        table.reload("modeTable", {url: Feng.ctxPath + "/mod/modelPara?ruleId=" + Model.ruleId});
    }

    element.on('tab(modelBox)', function(elem){
        if($(this).attr('lay-id') === '1'){
            $('#ruleName').val(Model.ruleName)
            $('#mDes').val(Model.modelDesc)
            $('#modelSql').val(Model.modelSql)
        }
    });

    /**
     * 查询一级菜单详情
     */
    Model.firstData = function (obj,second) {
        $("#firstList").html('');
        $.ajax({
            type: "POST",
            url: Feng.ctxPath + "/mod/ModelType",
            data: {'vItemId': 'P1001'},
            success: function (data) {
                if (data) {
                    console.log(data);
                    var option = '<option value=""></option>';
                    $.each(data, function (i, mes) {
                        option += "<option value='" + mes.vItemValId + "'>" + mes.vValName + "</option>";
                    });
                    $("#firstList").append(option);
                    $(`#firstList option[value='${obj}']`).attr('selected',true) //一级目录同步显示
                    Model.secondData(obj,second); //调用所在二级数据
                    form.render('select');
                }
            },
            error: function () {
            }
        });
    }

    /**
     * 查询二级菜单详情
     */
    Model.secondData = function (obj,num) {
        $("#secondList").html('');
        $.ajax({
            type: "POST",
            url: Feng.ctxPath + "/mod/ModelType",
            data: {vItemId: 'P1001', vItemValId: obj},
            success: function (data) {
                if (data) {
                    console.log(data);
                    var option = '<option value=""></option>';
                    $.each(data, function (i, mes) {
                        option += "<option class='"+mes.vItemId+"' value='" + mes.vItemValId + "'>" + mes.vValName + "</option>";
                    });
                    $("#secondList").append(option);
                    $(`#secondList option[value='${num}']`).attr('selected',true) //二级目录同步显示
                    form.render('select');
                }
            },
            error: function () {
            }
        });
    }


    //选择一级目录
    form.on('select(firstList)', function (data) {
        var text = $('#firstList').find("option:selected").text();
        Model.vItemValId1 = data.value;
        Model.vValName1 = text;
        if(data.value !== ''){
            Model.secondData(data.value);
        }else{
            $("#secondList").html('')
            form.render();
        }

    });

    //选择二级目录
    form.on('select(secondList)', function (data) {
        var secondList = $('#secondList').find("option:selected")
        Model.vItemValId2 = data.value;
        Model.vValName2 = secondList.text();
        Model.vItemId2 = secondList.attr('class');

    });



    // 增加二级目录表单提交事件
    form.on('submit(modelAddForm)', function (data) {
        console.log(data.field);
        $.ajax({
            type: "POST",
            contentType: "application/json;charset=UTF-8",
            url: Feng.ctxPath + "/mod/save",
            data: JSON.stringify({
                name: data.field.name,
                pid: data.field.pid
            }),
            success: function (data) {
                if (data.code === 200) {
                    Feng.success("保存成功!");
                } else {
                    Feng.fail(data.message);
                }

            },
            error: function () {
                Feng.error("保存失败!");
            }
        });
    });


    // 增加一级目录表单提交事件
    form.on('submit(modelAddTypeForm)', function (data) {
        console.log(data.field);
        $.ajax({
            type: "POST",
            contentType: "application/json;charset=UTF-8",
            url: Feng.ctxPath + "/mod/save",
            data: JSON.stringify({
                name: data.field.name
            }),
            success: function (data) {
                if (data.code === 200) {
                    Feng.success("保存成功!");
                } else {
                    Feng.fail(data.message);
                }
            },
            error: function () {
                Feng.error("保存失败!");
            }
        });
    });


    // 模型表单保存事件
    form.on('submit(modelForm)', function (data) {
        console.log(data.field);
        $.ajax({
            type: "POST",
            contentType: "application/json;charset=UTF-8",
            url: Feng.ctxPath + "mod/UpdateModel",
            data: JSON.stringify({
                vItemValId1: Model.vItemValId1,
                vItemValId2: data.field.secondList,
                ruleId: data.field.ruleId,//当前模型ruleId
                ruleName: data.field.ruleName, //当前模型更改后的模型名称
                modelDesc:data.field.modelDesc,
                ifAutoCheck:$('input[name="ifAutoCheck"]:checked').val()
            }),
            success: function (data) {
                Feng.success("保存成功!");

            },
            error: function () {
                Feng.error("保存失败!");

            }
        });
        return false;
    });

    Model.deleteS = function (ruleId) {
        console.log(Model.ruleId);
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/mod/deletem", function () {
                $('#firstList,#secondList').find("option:selected").attr("selected", false);
                form.render();
                Model.initTree();
                Feng.success("删除成功!");
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("ruleId", Model.ruleId);
            ajax.start();
        };
        Feng.confirm("是否删除此模型？", operation);
    };

    Model.runCon = function (ruleId) {
        var index = layer.load(2);
        console.log(Model.ruleId);
        $(".tab-header>li").eq(1).addClass('layui-this').siblings().removeClass('layui-this')
        $(".tab-content>div").eq(1).addClass('layui-show').siblings().removeClass('layui-show')
        $.ajax({
            url: Feng.ctxPath + "/mod/run?ruleId=" + ruleId,
            type: 'post',
            dataType: 'json',
            success: function (data) {
                console.log(data)
                layer.close(index);
                if (data.code === 200) {
                    getTableTitle(data.data);
                    getprogtitle(data.data);
                    Model.ruleRunId=data.data;
                } else {
                    layer.msg(data.message, {icon: 5, time: 2000});
                }
            }
        })

        function getTableTitle(ruleRunId) {
            var attrListJson = '';
            var tableRes = '';
            $.ajax({
                type: "post",
                url: Feng.ctxPath + "/mod/getResultData", //获取表头URL
                data: {ruleRunId: ruleRunId},
                dataType: "json",//返回数据类型的格式
                success: function (data) {
                    if(data){
                        $('.nothing-tab').css({'display':'none'})
                        attrListJson = data.data.title;//不需要用$.parseJSON(data)转换格式，用了会报错
                        tableRes = data.data.data;
                        initTable(attrListJson,tableRes,attrListJson);//获取表头成功后初始化表格
                    }else{
                        $('.nothing-tab').css({'display':'block'})
                    }

                },
                error: function () {
                    layer.msg('获取列表表头信息失败', {icon: 5, time: 2000});
                }
            })
        }

        function initTable(theads,tableRes,attrListJson) {
            var attrCols = [];   //存放表头数据
            var arr = Object.keys(theads);
            var n = arr.length
            for (var thead in theads) {
                attrCols.push({
                    field: thead,
                    title: attrListJson[thead],
                    sort: true,
                    align: 'center',
                    width:(100/n)+'%'
                });
            }
            layui.use("table", function () {
                var table = layui.table;
                table.render({
                    elem: "#exportTable",
                    data: tableRes,
                    page: true,// 开启分页
                    cellMinWidth: 105,
                    even: false,//隔行背景
                    cols: [attrCols]  //表头
                });
            });
        }

        function getprogtitle(ruleRunId) {
            $.ajax({
                type: "post",
                url: Feng.ctxPath + "/mod/getResultChart?ruleRunId=" + ruleRunId, //获取表头URL
                dataType: "json",//返回数据类型的格式
                success: function (res) {
                    if (res.code === 200) {
                        $('.nothing-chart').css({'display':'none'})
                        console.log(res);
                        Model.eChart(res.data)
                    }else{
                        $('.nothing-chart').css({'display':'block'})
                    }
                },
                error: function () {
                    layer.msg('没有查询到数据，生成报表失败', {icon: 5, time: 2000});
                }
            });
        }
    };

    /**
     * 展示图表
     */
    Model.eChart = function (data) {
        console.log(data)
        var myChart = eCharts.init(document.getElementById('data_report'));
        var colorList = ["#FFFF99", "#B5FF91", "#94DBFF", "#FFBAFF", "#FFBD9D", "#C7A3ED", "#CC9898", "#8AC007", "#CCC007", "#FFAD5C"];
        var chartType = Model.getChartType(data.chartType);
        var seriesData = Model.seriesData(data);
        console.log(seriesData.aa)
        var option = {
            legend: {
                type: 'scroll',
                bottom: 10,
                data: seriesData.bb,
                selected: seriesData.cc
            },
            grid: {
                top: '20%',
                bottom: '3%',
                left: '4%',
                right: '3%',
                containLabel: true
            },
            series : [
                {
                    name: '',
                    radius : '55%',
                    center: ['50%', '60%'],
                    itemStyle: {
                        normal: {
                            color:function (params) {
                                //自定义颜色
                                let colorList = ["#2C86FD","#5EDFCD","#59CB74","#FCD14B","#E7A376"];
                                if (params.dataIndex > (colorList.length - 1)) {
                                    return colorList[Math.ceil(Math.random() * 9)]
                                } else {
                                    return colorList[params.dataIndex];
                                }
                            },
                            borderColor: '#FFFFFF',
                            borderWidth: '1'
                        },
                        lineStyle: {
                            color: function (params) {
                                //自定义颜色
                                let colorList = ["#2C86FD","#5EDFCD","#59CB74","#FCD14B","#E7A376"];
                                if (params.dataIndex > (colorList.length - 1)) {
                                    return colorList[Math.ceil(Math.random() * 9)]
                                } else {
                                    return colorList[params.dataIndex];
                                }
                            },
                            borderColor: '#FFFFFF',
                            borderWidth: '1'
                        }
                    },
                    type: chartType,
                    data: seriesData.aa
                }
            ]
        };
        if (chartType === 'pie') {
            delete option.xAxis;
            delete option.yAxis;

        } else {
            option.xAxis = {data: Model.xAxisData(data)};
            option.yAxis = {};
        }
        myChart.setOption(option);
        createdChart = myChart;
        // window.addEventListener("resize", myChart.resize);
    }

    Model.xAxisData = (data) => {
        var chartX = data.chartX;
        var chartData = data.data;
        var xAxisData = [];
        for (var chartDataKey in chartData) {
            xAxisData.push(chartData[chartDataKey][chartX]);
        }
        return xAxisData;
    }

    Model.getChartType = (chartType) => {
        if (chartType == undefined || chartType == null || chartType == "") {
            return 'bar';
        }
        if (chartType === '1' || chartType === '2') {
            return 'bar';
        }
        if (chartType === '3') {
            return 'pie';
        }
        if (chartType === '4') {
            return 'line';
        }
    }

    Model.seriesData = (data) => {
        var chartVal = data.chartVal;
        var chartX = data.chartX;
        var chartData = data.data;
        var seriesData = [],legendData = [],selected = {};
        for (var key in chartData) {
            if (data.chartType === '3') {
                var obj = {};
                obj.value = chartData[key][chartVal];
                obj.name = chartData[key][chartX];
                seriesData.push(obj);
                legendData.push(chartData[key][chartX])
            } else {
                seriesData.push(chartData[key][chartVal]);
                legendData.push(chartData[key][chartX])
            }
            selected[chartData[key]] = key < 6;
        }
        return {
            aa:seriesData,
            bb:legendData,
            cc:selected
        };
    }

    $('#delete').click(function () {
        if (Model.ruleId === undefined || Model.ruleId === '') {
            layer.msg('内容为空，无法删除哦！', {icon: 7, time: 2000});
            return false;
        }
        Model.deleteS(Model.ruleId);
    });

    $('#run').click(function () {
        if ($('.ruleName').val() === '') {
            layer.msg('内容为空，无法执行哦！', {icon: 7, time: 2000});
            return false;
        }
        $('#runTab').css({'display': 'inline-block'})
        Model.runCon(Model.ruleId);
    });

    // 添加按钮点击事件
    $('#add').click(function () {
        layer.open({
            type: 1,
            title: '添加检查类型',
            id: 'aModeTypeLayer2',
            area: ['40%', '30%'],
            content: $('#addModeType2'),
            shade: .2,//不显示遮罩
            success: function (layero, index) {

            }
        });

    });

    // 关闭弹框
    $('#close').click(function () {
        layer.closeAll();
    });
    // 关闭弹框
    $('#close2').click(function () {
        layer.closeAll();
    });

 // 导出列表数据
    $('#uploadResult').click(function () {
        Model.uploadResult(Model.ruleRunId);
    });

    closeDialog = function (e) {
        e.stopPropagation()
        $(".tab-header>li").eq(0).addClass('layui-this').siblings().removeClass('layui-this')
        $(".tab-content>div").eq(0).addClass('layui-show').siblings().removeClass('layui-show')
        $('#runTab').css({'display': 'none'})
        return false
    }

    layui.use('table', function () {
        var table = layui.table;
        //监听单元格编辑
        var modeTable = table.render({
            elem: '#modeTable'
            , height: 80
            , url: Feng.ctxPath + "/mod/modelPara?ruleId=" + Model.ruleId//数据接口
            , page: false //开启分页
            , cols: [[ //表头//后台数据封装为map
                {field: 'ruleId', title: 'ruleId', hide: true}
                , {field: 'paraString', title: '参数名称', width: "20%"}
                , {field: 'paraValue', title: '参数值', width: "25%", edit: 'text'}
                , {title: '操作', toolbar: '#barDemo', width: "15%"}
                , {field: 'paraDesc', title: '参数说明', width: "40%"}

            ]]
        });

        table.on('tool(modeTable)', function (obj) {
            $.ajax({
                type: "POST",
                url: Feng.ctxPath + "/mod/updateModelParaVal",
                data: JSON.stringify(obj.data),
                contentType: 'application/json',
                dataType: "json",//返回数据类型的格式
                success: function (data) {
                    Feng.success("保存成功!");
                },
                error: function () {
                    // $F.messager.warn(data.message);
                    Feng.error("保存失败!");
                }
            });
            //layer.msg('[ID: ' + data.id + '] ' + field + ' 字段更改为：' + value);
        });
    });

    // 导出excel
    $('#downLoad').click(function () {
        Model.exportExcel();
    });

    /**
     * 导出excel
     */
    Model.exportExcel = function () {
        window.location.href = Feng.ctxPath + "/mod/exportExcel";
    };

    /**
     * 导出列表数据
     */
      Model.uploadResult=function(ruleRunId){
          if(!Model.export.tableCheck){
              layer.msg('暂无列表信息，无法导出！', {icon: 5, time: 2000});
              return false
          }
          $.ajax({
                 type: "post",
                 url: Feng.ctxPath + "/mod/ifAsNot?ruleRunId=" +ruleRunId,
                 dataType: "json",//返回数据类型的格式
                 success: function (res) {
                     console.log(res)
                     if(res.success){
                         window.location.href = Feng.ctxPath + "/mod/uploadResultData?ruleRunId=" +ruleRunId;
                     }else{
                         layer.msg( res.message, {icon: 5, time: 2000});
                     }
                 },
                 error: function () {
                     layer.msg('接口通讯异常', {icon: 5, time: 2000});
                 }
          });
     }

    /**
     * 导出图表数据
     */
    $(document).on('click', "#uploadChart", function () {
        if(!Model.export.chartCheck){
            layer.msg('暂无图表信息，无法导出！', {icon: 5, time: 2000});
            return false
        }

            let index = layer.load(0);
             let param = {};
            param.ruleRunId = Model.ruleRunId;
            param.chartTypes = createdChart.getDataURL();
            console.log(param.chartTypes);
            let url = Feng.ctxPath + '/detect/downprog';
            let xhr = new XMLHttpRequest();//创建新的XHR对象
            xhr.open('post', url);//指定获取数据的方式和url地址
            xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8')
            xhr.responseType = 'blob';//以blob的形式接收数据，一般文件内容比较大
            xhr.onload = function (e) {
                var blob = this.response;//Blob数据
                if (this.status == 200) {
                    if (blob && blob.size > 0) {
                        saveAs(blob, "Chart" + ".xlsx");//处理二进制数据，让浏览器认识它
                        layer.close(index);
                    }
                }
            };
            xhr.send(JSON.stringify(param)); //post请求传的参数
    })











 /**
   * ***********************************************大数据版*******************************************************************************
*/


    /**
     * ************************执行
     */

    $('#tabUp').click(function () {
        if ($('.ruleName').val() === '') {
            layer.msg('内容为空，无法执行哦！', {icon: 7, time: 2000});
            return false;
        }
        var index = layer.load(2);
        $.ajax({
            url: Feng.ctxPath + "/mod/run?ruleId=" + Model.ruleId,
            type: 'post',
            dataType: 'json',
            success: function (data) {
                console.log(data)
                layer.close(index);
                if (data.code === 200) {
                    layer.confirm('当前操作已提交，正在等待执行。是否跳转至执行管理页面，查看执行结果?',  {icon: 3, title:'提示'},function(index){
                        mods.tabChange({"menuId":"/manage","menuPath":"/manage","menuName":"执行管理"},5,'/manage','执行管理')
                        layer.close(index);
                    });
                } else {
                    layer.msg(data.message, {icon: 5, time: 2000});
                }
            }
        })

    });

 /**
   * ***********************************************pay版*******************************************************************************
*/

 // 添加按钮点击事件
 $('#addPay').click(function () {
     layer.open({
         type: 1,
         title: '添加检查项',
         id: 'aModeTypeLayer2',
         area: ['40%', '30%'],
         content: $('#addModeType2'),
         shade: .2,//不显示遮罩
         success: function (layero, index) {

         }
     });

 });

    var uploadInstPay = upload.render({
        elem: '#testListActionPay' //绑定元素
        , url: Feng.ctxPath + "/mod/batchImportPay" //上传接口
        , accept: 'file' //普通文件
        , done: function (res) {
           /* //上传完毕回调
             if( Model.edition === 'pay'){
                 Model.edition = 'pay'
                 Model.url = '/mod/payztree'
             }else{
                 Model.edition = 'model'
                 Model.url = '/mod/modelztree'
             }*/
              Model.edition = 'model'
             Model.url = '/mod/modelztree'
             Model.initTree();
             Feng.success("导入成功!");
        }
        , error: function () {
            //请求异常回调
             Feng.error("导入失败!");
        }
    });

    //批量导出
    $('#downLoadPay').click(function () {
        Model.exportExcelPay();
    });

    Model.exportExcelPay = function () {
        window.location.href = Feng.ctxPath + "/mod/exportExcelPay";
    };

   $('#runPay').click(function () {
        if ($('.ruleName').val() === '') {
            layer.msg('内容为空，无法执行哦！', {icon: 7, time: 2000});
            return false;
        }
        $('#runTab').css({'display': 'inline-block'})
        Model.runConPay(Model.ruleId);
   });

    Model.runConPay = function (ruleId) {
            var index = layer.load(2);
            console.log(Model.ruleId);
            $(".tab-header>li").eq(1).addClass('layui-this').siblings().removeClass('layui-this')
            $(".tab-content>div").eq(1).addClass('layui-show').siblings().removeClass('layui-show')
            $.ajax({
                url: Feng.ctxPath + "/mod/runModel?ruleId=" + ruleId,
                type: 'post',
                dataType: 'json',
                success: function (data) {
                    console.log(data)
                    layer.close(index);
                    if (data.code === 200) {
                        getTableTitle(data.data);
                        getprogtitle(data.data);
                        Model.ruleRunId=data.data;
                    } else {
                        layer.msg(data.message, {icon: 5, time: 2000});
                    }
                }
            })

            function getTableTitle(ruleRunId) {
                var attrListJson = '';
                var tableRes = '';
                $('.export-count').html('');
                $.ajax({
                    type: "post",
                    url: Feng.ctxPath + "/mod/getResultData", //获取表头URL
                    data: {ruleRunId: ruleRunId},
                    dataType: "json",//返回数据类型的格式
                    success: function (data) {
                        if(data){
                            var p = '';
                            attrListJson = data.data.title;//不需要用$.parseJSON(data)转换格式，用了会报错
                            // tableRes = data.data.data;
                            tableRes = data.data;
                            p += `<p class="export-count">违规记录<b class="export-num">${data.data.resultCount || 0}</b> 条，违规比例 <b class="export-num">${data.data.proportion || '0.0000'}%</b></p>`
                            $(".exportTableBox>div").remove();
                            if(data.data.resultCount === 0){
                                Model.export.tableCheck = false
                                $('.nothing-tab').css({'display':'block'})
                                $('.exportTable').css({'display':'none'})
                            }else{
                                Model.export.tableCheck = true
                                $('.exportTableBox').prepend(p);
                                $('.nothing-tab').css({'display':'none'})
                                initTable(attrListJson,tableRes,attrListJson);//获取表头成功后初始化表格
                            }

                        }else{
                            Model.export.tableCheck = false
                            $('.nothing-tab').css({'display':'block'})
                        }

                    },
                    error: function () {
                        layer.msg('获取列表表头信息失败', {icon: 5, time: 2000});
                    }
                })
            }

            function initTable(theads,tableRes,attrListJson) {
                var attrCols = [];   //存放表头数据
                var arr = Object.keys(theads);
                var n = arr.length
                for (var thead in theads) {
                    attrCols.push({
                        field: thead,
                        title: attrListJson[thead],
                        sort: true,
                        align: 'center'/*,
                        width:(100/n)+'%'*/
                    });
                }
                layui.use("table", function () {
                    var table = layui.table;
                    table.render({
                        elem: "#exportTable",
                        data: tableRes.data,
                        page: true,// 开启分页
                        cellMinWidth: 150,
                        even: false,//隔行背景
                        cols: [attrCols]  //表头
                        ,done:function(res, curr, count){
                            var list = tableRes.coordinateArr;
                            var id = 'exportTable';
                            var color = '#F1440E';
                            var h = $(`div[lay-id= ${id}] .layui-table-body>table[class='layui-table']>tbody`)
                            $.each(list, function (i, l) {
                                h.children().eq(l[0]).children().eq(l[1]).find('div').css({'color':color})
                            });
                        }
                    });
                });
            }

            function getprogtitle(ruleRunId) {
                $.ajax({
                    type: "post",
                    url: Feng.ctxPath + "/mod/getResultChart?ruleRunId=" + ruleRunId, //获取表头URL
                    dataType: "json",//返回数据类型的格式
                    success: function (res) {
                        if (res.code === 200) {
                            Model.export.chartCheck = true
                            $('.nothing-chart').css({'display':'none'})
                            Model.eChart(res.data)
                        }else{
                            Model.export.chartCheck = false
                            $('.nothing-chart').css({'display':'block'})
                        }
                    },
                    error: function () {
                        layer.msg('没有查询到数据，生成报表失败', {icon: 5, time: 2000});
                    }
                });
            }
    };








        // 模型表单保存事件
            form.on('submit(modelFormpay)', function (data) {
                console.log(data.field);
               $.ajax({
                           type: "POST",
                           contentType: "application/json;charset=UTF-8",
                           url: Feng.ctxPath + "mod/UpdateModel",
                           data: JSON.stringify({
                               vItemValId1: Model.vItemValId1,
                               vItemValId2: data.field.secondList,
                               ruleId: data.field.ruleId,//当前模型ruleId
                               ruleName: data.field.ruleName, //当前模型更改后的模型名称
                               modelDesc:data.field.modelDesc,
                               ifAutoCheck:$('input[name="ifAutoCheck"]:checked').val()
                           }),
                    success: function (data) {
                        Feng.success("保存成功!");

                    },
                    error: function () {
                        Feng.error("保存失败!");

                    }
                });
                return false;
            });
});
