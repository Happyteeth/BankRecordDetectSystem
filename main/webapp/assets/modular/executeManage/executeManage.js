layui.use(['form', 'layer', 'element', 'echarts','table','form','laydate', 'FileSave','ax'], function () {
    var $ = layui.jquery
    var element = layui.element;
    var eCharts = layui.echarts;
    var table = layui.table;
    var form = layui.form;
    var laydate = layui.laydate;
    let fileSave = layui.FileSave;
    var $ax = layui.ax;


    element.on('tab(modelBox)', function(elem){
        if($(this).attr('lay-id') === '1'){

        }
    });

    var result = {
        fuzzy:'',
        type:'',
        sDate:'',
        eDate:'',
        orderBy:'',
        order:'',
        selectHtml:` <button type="button" class="layui-btn layui-btn-primary active" id="btn1" onclick="selectBtn('','btn1')">全部结果</button>
                     <button type="button" class="layui-btn layui-btn-primary" id="btn2" onclick="selectBtn('1','btn2')">数据分析</button>
                     <button type="button" class="layui-btn layui-btn-primary" id="btn3" onclick="selectBtn('2','btn3')">模型执行</button>`
    }

    /**
     * ************************tab1
     */

    /*
   初始化表格的列
    */
    result.initColumn = function () {
        return [[
            {field: 'submitTime',width:180, title: '提交时间', sort: true,align: 'center'},
            {field: 'ruleName',title: '执行名称', align: 'center'},
            {field: 'ruleType',width:160, title: '执行类型', sort: true , align: 'center'},
            // {field: 'rulePriority',width:120, title: '优先级', sort: true, align: 'center'},
            {field: 'status',width:160, title: '执行状态', sort: true},
            {field: 'vInsertUser',width:160, title: '用户名', align: 'center'},
            {align: 'left', fixed: "right", toolbar: '#barDemo', title: '操作', width: 180, align: 'center'}
        ]];
    };


    // 获取列表详情
    result.getInfo = function (fuzzy,type,sDate,eDate) {
        var url = `/manage/list?ruleType=${type?type:''}&&fuzzy=${sDate?'':fuzzy}&&sDate=${sDate?sDate:''}&&eDate=${eDate?eDate:''}`
        console.log(url)
        table.render({
            elem: '#resultTable',
            url: Feng.ctxPath + url,
            parseData: function(res){ //res 即为原始返回的数据
                var account = layui.sessionData('userInfo').userInfo.account;
                for(var i in res.data){
                    res.data[i].account = account?account:'admin';
                }
                return {
                    "code": res.code, //解析接口状态
                    "msg": res.msg, //解析提示文本
                    "count": res.count, //解析数据长度
                    "data": res.data //解析数据列表
                }
            },
            page: true,
            limits: [10, 20, 50,100],
            limit: 10,
            skin: 'line',
            cols: result.initColumn(),
            done: function(res, curr, count){
                element.render();
            }
        });

    }

    table.on('sort(resultTable)', function(obj){ //注：tool是工具条事件名，test是table原始容器的属性 lay-filter="对应的值"
        result.getInfo(result.fuzzy,result.type,result.sDate,result.eDate,obj.field,obj.type,obj.field,obj.type)
        table.reload('resultTable', {
            initSort: obj //记录初始排序，如果不设的话，将无法标记表头的排序状态。
            ,where: { //请求参数（注意：这里面的参数可任意定义，并非下面固定的格式）
                field: obj.field //排序字段
                ,order: obj.type //排序方式
            }
        });
    });

    // 搜索
    result.search = function () {
        result.getInfo(result.fuzzy,result.type,result.sDate,result.eDate)
    };

    $('#searchInput').on('keyup', function(event) {
        var name = $("#searchInput").val();
        if (event.keyCode == "13") {
            result.fuzzy = name
            result.search()
        }
        else if(event.keyCode == "8" || event.keyCode == "46"){
            if(name === '' || name === undefined){
                result.fuzzy = name
                if(name === ''){
                    result.sDate = ''
                    result.eDate = ''
                }
                result.search()
            }
        }
    });

    $('#search').click(function () {
        var name = $("#searchInput").val();
        result.fuzzy = name
        if(name === ''){
            result.sDate = ''
            result.eDate = ''
        }
        result.search()
    })


    laydate.render({
        elem: '#test28',
        btns: ['now'],
        done: function(value, date){
            $('#test28').text('')
            $('#searchInput').val(value)
            result.sDate = `${value} 00:00:00`
            result.eDate = `${value} 23:59:59`
            result.search()
        }
    });

    selectBtn = function(t,h) {
        $(`#${h}`).addClass('active').siblings().removeClass('active')
        result.type = t;
        result.search()
    }

    $("#refresh").click(function () {
        table.reload('resultTable');
    });

    // var max = "2019-12-31";
    // var min = "2019-1-1";
    // var start = laydate.render({
    //     elem: '#test19',
    //     type: 'datetime',
    //     max:max,
    //     done: function(value, date){
    //         result.sDate = value
    //         if(value){
    //             end.config.min = date;
    //             end.config.min.month = date.month -1;
    //         }
    //         result.search()
    //     }
    // });
    // var end = laydate.render({
    //     elem: '#test20',
    //     type: 'datetime',
    //     min:min,
    //     done: function(value, date){
    //         result.eDate = value
    //         if(value){
    //             start.config.max = date;
    //             start.config.max.month = date.month -1;
    //         }
    //         result.search()
    //     }
    // });

    $(document).ready(function () {
        $('.btnGroup').append(result.selectHtml)
        //获取用户详情
        var ajax = new $ax(Feng.ctxPath + "/system/currentUserInfo",function (res) {
            if(res.success){
                layui.sessionData('userInfo', {key: 'userInfo',
                    value:res.data}); //存储查看检测结果判断字符
            }
        });
        ajax.start();
        result.getInfo(result.fuzzy,result.type)
    });

    //取消暂停恢复
    result.stopCalc = function (id,s,callback) {
        $.ajax({
            type: "post",
            url: Feng.ctxPath + "/manage/cancel",
            data: {ruleRunId: id,status:s},
            dataType: "json",
            success: function (data) {
                if(data.success){
                    callback()
                }
            },
            error: function () {

            }
        })
    };

    result.saveModel = { //保存模型
        modelType1ItemValId:'',
        modelType2ItemValId:'',
    }

    //保存为模型
    result.sSaveModel = function (d,r,m,s,callback) {
        console.log(d)
        var index = layer.load(2);
        let info = {
            ruleRunId: d.ruleRunId,
            modelName: r,
            modelDesc: m?m:'',
            modelType1ItemId: s.modelType1ItemId,
            modelType1ItemValId: s.modelType1ItemValId,
            modelType2ItemId: s.modelType2ItemId,
            modelType2ItemValId: s.modelType2ItemValId,
        }
        $.ajax({
            type: "POST",
            url: Feng.ctxPath + "/manage/saveAsModel",
            dataType: "json",
            contentType: 'application/json',
            data: JSON.stringify(info),
            success: function (data) {
                layer.close(index);
                if(data.success){
                    layer.msg('保存模型成功！', {
                        icon:  7,
                        time: 2000
                    }, function(){
                        layer.closeAll();
                        callback();
                    });
                }else{
                    layer.msg('添加失败！', {icon: 5, time: 2000});
                }
            },
            error: function () {

            }
        })
    };

    result.modelConCheckBd = function(){
        if ($('#mName').val()==='') {
            layer.msg('模型名称不能为空', {icon: 7, time: 2000});
            return false;
        }
        return true
    }

    /**
     * 查询一级菜单详情
     */
    result.firstData =function(){
        $.ajax({
            type : "POST",
            url : Feng.ctxPath + "/mod/ModelType",
            data:{'vItemId':'P1001'},
            success: function (data) {
                if(data){
                    var option = '';
                    $.each(data, function (i, mes) {
                        option += `<option class='${mes.vItemId}' value='${mes.vItemValId}'>${mes.vValName}</option>`;
                    });
                    $("#mFirst").append(option);
                    form.render('select');
                }
            },
            error: function() {
            }
        });
    }

    /**
     * 查询二级菜单详情
     */
    result.secondData =function(obj){
        $("#mSecond").html('');
        $.ajax({
            type : "POST",
            url : Feng.ctxPath + "/mod/ModelType",
            data:{vItemId:'P1001',vItemValId:obj.value},
            success: function (data) {
                if(data){
                    var option = '<option value=""></option>';
                    $.each(data, function (i, mes) {
                        option += `<option class='${mes.vItemId}' value='${mes.vItemValId}'>${mes.vValName}</option>`;
                    });
                    $("#mSecond").append(option);
                    form.render('select');
                }
            },
            error: function() {
            }
        });
    }


    //选择一级目录
    form.on('select(mFirst)', function(data){
        var text = $('#mFirst').find("option:selected").attr('class');
        result.saveModel.modelType1ItemId = text;
        result.saveModel.modelType1ItemValId = data.value;
        if(data.value !== ''){
            result.secondData(data);
        }else{
            $("#mSecond").html('')
            form.render();
        }
    });

    //选择二级目录
    form.on('select(mSecond)', function(data){
        var text = $('#mSecond').find("option:selected").attr('class');
        result.saveModel.modelType2ItemId = text;
        result.saveModel.modelType2ItemValId = data.value;
    });



    //监听工具条
    table.on('tool(resultTable)', function(obj){
        var data = obj.data;
        var tr = obj.tr;
        if(obj.event === 'detail'){
            $(".tab-header>li").eq(1).addClass('layui-this').siblings().removeClass('layui-this')
            $(".tab-content>div").eq(1).addClass('layui-show').siblings().removeClass('layui-show')
            // layer.msg('ID：'+ data.ruleRunId + ' 的查看操作');
            result.tabSecond(data.ruleRunId)
            result.ruleRunId = data.ruleRunId
        }
        else if(obj.event === 'noDetail'){
            if(data.status === '执行异常'){
                layer.msg('执行异常，无法查看结果哦！', {icon: 5, time: 2000});
            }else if(data.status === '运行中'){
                layer.msg('运行中，无法查看结果哦！', {icon: 5, time: 2000});
            }

        }
        else if(obj.event === 'saveModel'){
            console.log('模型')
            layer.open({
                type: 1,
                title: '模型参数',
                area: ['50%', '70%'],
                offset: '10px',
                content: $('#addCon'),
                shade: .2,//不显示遮罩
                btn: ['取消', '保存'],
                yes: function(index, layero){
                    layer.close(index)
                },
                btn2: function(index, layero){
                    var r = $('#mName').val();
                    var m = $('#mDes').val();
                    if(result.modelConCheckBd()){
                        result.sSaveModel(data,r,m,result.saveModel,function () {
                            obj.update({
                                ruleType: '模型'
                            });
                            var html = `<a class="layui-btn layui-btn-xs" lay-event="detail">查看结果</a>`;
                            var findHtml = $(".resultTableBox > .layui-border-box>.layui-table-box>.layui-table-body").find($('tbody .layui-table-click>td'))
                            var btnHtml = findHtml.last().find('div').html(html)
                            var nameHtml = findHtml.eq(1).find('div').html(r)
                        })
                    }
                    return false
                },
                success: function(layero, index){
                    $('#mName').val(data.ruleName || '')
                    $('#mDes').val(data.modelDesc || '')
                    result.firstData();
                },
                end:function () {
                }
            });
            // layer.confirm('确认保存为模型？', function(index){
            //     result.sSaveModel(data,function () {
            //         obj.update({
            //             ruleType: '模型'
            //         });
            //         var html = `<a class="layui-btn layui-btn-xs" lay-event="detail">查看结果</a>`;
            //         var btnHtml = $(".resultTableBox > .layui-border-box>.layui-table-box>.layui-table-body")
            //             .find($('tbody .layui-table-click>td')).last().find('div').html(html)
            //     })
            //     layer.close(index);
            // });

        }
        else if(obj.event === 'stop'){
            layer.confirm('确认暂停？', function(index){
                result.stopCalc(data.ruleRunId,'B',function () {
                    obj.update({
                        status: '已暂停'
                    });
                    var html = `<a class="layui-btn layui-btn-xs" lay-event="run">恢复</a>`;
                    var btnHtml = $(".resultTableBox > .layui-border-box>.layui-table-box>.layui-table-body")
                                    .find($('tbody .layui-table-click>td')).last().find('div').html(html)
                })
                layer.close(index);
            });
        }
        else if(obj.event === 'run'){
            layer.confirm('确认恢复？', function(index){
                result.stopCalc(data.ruleRunId,'1',function () {
                    obj.update({
                        status: '等待执行'
                    });
                    var html = `<a class="layui-btn layui-btn-xs" lay-event="stop">暂停</a>
                                <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="calc">取消</a>`;
                    var btnHtml = $(".resultTableBox > .layui-border-box>.layui-table-box>.layui-table-body")
                                    .find($('tbody .layui-table-click>td')).last().find('div').html(html)
                })
                layer.close(index);
            });
        }
        else if(obj.event === 'calc'){
            layer.confirm('确认取消？', function(index){
                result.stopCalc(data.ruleRunId,'A',function () {
                    obj.update({
                        status: '已取消'
                    });
                    var btnHtml = $(".resultTableBox > .layui-border-box>.layui-table-box>.layui-table-body")
                        .find($('tbody .layui-table-click>td')).last().find('div').html(`<a style="color: #666">——</a>`)
                })
                layer.close(index);
            });
        }
    });


    /**
     * ************************tab2
     */

    result.export = {
        tableCheck:false,
        chartCheck:false,
    }

    result.tabSecond = function (ruleRunId) {
        // getTableTitle(id);
        // function getTableTitle(ruleRunId) {
        //
        // }
        // var attrListJson = '';
        // var tableRes = [];
        $('.export-count').html('');
        $.ajax({
            type: "post",
            url: Feng.ctxPath + "/mod/getResultData", //获取表头URL
            data: {ruleRunId: ruleRunId},
            dataType: "json",//返回数据类型的格式
            success: function (data) {
                if(data){
                    console.log(data)
                    var p = '';
                    $('#exportTable').html('')
                    var tableRes = data.data?data.data:[];
                    p += `<p class="export-count">违规记录<b class="export-num">${data.data.resultCount || 0}</b> 条，违规比例 <b class="export-num">${data.data.proportion || '0.0000'}%</b></p>`
                    $(".exportTableBox>div").remove();
                    if(tableRes.length === 0){
                        $('#dataReport').html(`<h2 class="nothing-chart">暂无图表数据</h2>`)
                        result.export.chartCheck = false
                    }else{
                        result.export.tableCheck = true
                        $('.nothing-tab').css({'display':'none'})
                        $('#dataReport').html('')
                        $('.exportTableBox').prepend(p);
                        getprogtitle(ruleRunId);
                        initTable(data.data.title,tableRes,data.data.title);//获取表头成功后初始化表格
                    }

                }else{
                    $('.nothing-tab').css({'display':'block'})
                    $('#dataReport').html(`<h2 class="nothing-chart">暂无图表数据</h2>`)
                    result.export.tableCheck = false
                }

            },
            error: function () {
                layer.msg('获取列表表头信息失败', {icon: 5, time: 2000});
            }
        })
    };

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
                width:n<=5?(100/n)+'%':200
            });
        }
        layui.use("table", function () {
            var table = layui.table;
            table.render({
                elem: "#exportTable",
                data: tableRes.data,
                page: true,// 开启分页
                cellMinWidth: 105,
                even: false,//隔行背景
                cols: [attrCols],  //表头
                done:function(res, curr, count){
                    var list = tableRes.coordinateArr;
                    var id = 'exportTable';
                    var color = '#F1440E';
                    var h = $(`div[lay-id= ${id}] .layui-table-body>table[class='layui-table']>tbody`)
                    $.each(list, function (i, l) {
                        var div = h.children().eq(l[0]).children().eq(l[1]).find('div')
                        var checknum = div.text().length
                        if(checknum>0){
                            div.css({color:color})
                        }else{
                            div.html('--').css({color:color})
                        }
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
                    result.eChart(res.data)
                    result.export.chartCheck = true
                }else{
                    $('#dataReport').html(`<h2 class="nothing-chart">暂无图表数据</h2>`)
                    result.export.chartCheck = false
                }
            },
            error: function () {
                layer.msg('没有查询到数据，生成报表失败', {icon: 5, time: 2000});
            }
        });
    }

    /**
     * 展示图表
     */
    var myChart;
    result.eChart = function (data) {
        if ( myChart !== null && myChart !== "" && myChart !== undefined ) {
            myChart.dispose();
        }
        myChart = eCharts.init(document.getElementById('dataReport'));
        var colorList = ["#2C86FD","#5EDFCD","#59CB74","#FCD14B","#E7A376"];
        var chartType = result.getChartType(data.chartType);
        var seriesData = result.seriesData(data);
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
                    center: ['50%', '50%'],
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
            option.tooltip={
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            }
            delete option.xAxis;
            delete option.yAxis;
        } else {
            option.xAxis = {data: result.xAxisData(data)};
            option.yAxis = {};
            option.tooltip={trigger: 'axis'}
            delete option.legend;
        }
        myChart.setOption(option,true);
        createdChart = myChart;
        // window.addEventListener("resize", myChart.resize);
    }

    result.xAxisData = (data) => {
        var chartX = data.chartX;
        var chartData = data.data;
        var xAxisData = [];
        for (var chartDataKey in chartData) {
            xAxisData.push(chartData[chartDataKey][chartX]);
        }
        return xAxisData;
    }

    result.getChartType = (chartType) => {
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

    result.seriesData = (data) => {
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

    // 导出列表数据
    $('#uploadResult').click(function () {
        if(!result.export.tableCheck){
            layer.msg('暂无列表信息，无法导出！', {icon: 5, time: 2000});
            return false
        }
        $.ajax({
            type: "post",
            url: Feng.ctxPath + "/mod/ifAsNot?ruleRunId=" + result.ruleRunId,
            dataType: "json",//返回数据类型的格式
            success: function (res) {
                console.log(res)
                if(res.success){
                    window.location.href = Feng.ctxPath + "/mod/uploadResultData?ruleRunId=" + result.ruleRunId;
                }else{
                    layer.msg( res.message, {icon: 5, time: 2000});
                }
            },
            error: function () {
                layer.msg('接口通讯异常', {icon: 5, time: 2000});
            }
        });
        // window.location.href = Feng.ctxPath + "/mod/uploadResultData?ruleRunId=" + result.ruleRunId;
    });

    /**
     * 导出图表数据
     */
    $(document).on('click', "#uploadChart", function () {
        if(!result.export.chartCheck){
            layer.msg('暂无图表信息，无法导出！', {icon: 5, time: 2000});
            return false
        }
        let index = layer.load(0);
        let param = {};
        param.ruleRunId = result.ruleRunId;
        param.chartTypes = createdChart.getDataURL();
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






});
