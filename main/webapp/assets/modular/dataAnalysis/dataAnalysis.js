layui.use(['layer', 'form', 'table', 'laydate', 'admin', 'ax','element','echarts','FileSave','mods'], function () {
    var $ = layui.$;
    var layer = layui.layer;
    var form = layui.form;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var element = layui.element;
    var eCharts = layui.echarts;
    let fileSave = layui.FileSave;
    var laydate = layui.laydate;
    var mods = layui.mods;

    /**
     * 数据分析
     */
    var Analysis = {
        dataNameArry:[], //存储初始字段数组
        data: {  //查询存储字段
            tableName: '',  //表名
            columnList: [] //表字段
        },
        local: { //本地存储中文
            tableName: "",
            columnList: []
        },


    };

    Analysis.clean = function(){
        Analysis.stepFirstClean();
        Analysis.stepTwoClean();
        Analysis.stepFourClean();
        Analysis.stepFiveClean();
        $('#cType').find("option:selected").attr("selected", false);
        $("#cDataNameList,#cDataName,#sYName,#qDataNameList,.exportTableHeader,.exportTableList,.queryTableList,.cTableList").html('');
        $('#sXName,#sTableName').val('');
        $('.dataNameBox,.dataTableBox,.sChartRow,.queryTableBox,.exportTableBox,.cTableBox').css({'display':'none'});
        $('.no-2,.no-3,.no-4,.no-5').css({'display':'block'})
        $("#dataNameList").scrollTop(0);
        form.render();
    }

    /**
     * ************************step1
     */

    Analysis.stepFirstClean = function(){
        Analysis.data={
            tableName:"",
            columnList:[]
        }
        Analysis.local = {
            tableName: "",
            columnList: [],
            columnType: []
        }
        $("#dataNameList").html('');
        $(".dataTableList").html('');
    }

    /**
     * 查询表
     */
    Analysis.dataList = function () {
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/report/tableList',
            success: function (data) {
                if (data) {
                    var option = '';
                    $.each(data, function (i, mes) {
                        option += "<option value='" + mes.table_name + "'>" + mes.table_desc + "</option>";
                    });
                    $("#dataList").append(option);
                    form.render('select');
                }
            },
            error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });
    }

    /**
     * 查询表字段
     */
    Analysis.dataNameList = function (name) {
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/analysis/findColByTable',
            dataType: 'json',
            data: {
                tableName: name
            },
            success: function (data) {
                if (data) {
                    Analysis.dataNameArry = data; //存储初始字段
                    $('.dataNameBox').css({'display': 'block'});
                    $('.dataTableBox').css({'display': 'block'});
                    var checkbox = '', option = "<option value=''></option>";
                    $.each(data, function (i, mes) {
                        checkbox += "<p><input type='checkbox' lay-skin='primary' lay-filter='dataName' title='" + mes.column_desc + "' value='" + mes.column_name + "' class='" + mes.data_type + "'></p>";
                        option += "<option data-time='"+mes.date_format+"' class='"+ mes.data_type +"' value='" + mes.column_name + "'>" + mes.column_desc + "</option>";
                    });
                    $("#dataNameList").append(checkbox);
                    form.render('checkbox');
                    $("#qDataNameList").append(option); //选择字段step2
                    form.render('select');
                }
            },
            error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });
    }

    /**
     * 表&字段显示table
     */
    Analysis.dataTable = function (data, mes) {
        if (data && mes) {
            var tr = '';
            tr += "<tr><td>" + data + "</td><td>" + mes[mes.length - 1] + "</td></tr>";
            $(".dataTableList").append(tr);
            table.init('dataTable', {
                height: 214 //设置高度
            });

        }
    }


    $(document).ready(function () {
        Analysis.dataList();
    });

    //选择表
    form.on('select(dataList)', function (data) {
        Analysis.clean()
        $(".dataTableBox > .layui-border-box>.layui-table-box>.layui-table-body").find($('tbody')).html('')
        Analysis.data.tableName = data.value;  //表插入
        Analysis.dataNameList(data.value); //查询表字段

        var text = $('#dataList').find("option:selected").text();
        Analysis.local.tableName = text;
    });

    //选择字段
    form.on('checkbox(dataName)', function (data) {
        var checked = data.othis[0].classList.length == 3 ? true : false;
        var text = data.othis[0].innerText;
        var type = data.elem.className;
        if (checked) {
            Analysis.data.columnList.push(data.value) //表字段插入
            Analysis.local.columnList.push(text)
            Analysis.local.columnType.push(type)
            Analysis.dataTable(Analysis.local.tableName, Analysis.local.columnList);
            if (Analysis.local.columnList.length > 0) {  //汇总选择字段step4
                var key = Analysis.data.columnList
                var value = Analysis.local.columnList
                var colType = Analysis.local.columnType
                var arr = []
                for (var i = 0; i < value.length; i++) {
                    var obj = {};
                    for (var j = 0; j < key.length; j++) {
                        if (i === j) {
                            obj.name = value[i];
                            obj.value = key[j];
                            obj.type = colType[j];
                            arr.push(obj);
                        }
                    }
                }
                Analysis.stepFour.arry = arr //保存计算字段
            }
        } else {
            for (var i = 0; i < Analysis.local.columnList.length; i++) {
                var index = Analysis.local.columnList.indexOf(text)
                if (index > -1) {
                    var children = $(".dataTableList").children('tr')
                    var children2 = $(".dataTableBox > .layui-border-box>.layui-table-box>.layui-table-body").find($('tbody')).children('tr')
                    var cChildren = $("#cDataNameList").children('option'); //汇总选择字段step4
                    Analysis.data.columnList.splice(index, 1);
                    Analysis.local.columnList.splice(index, 1);
                    Analysis.stepFour.arry.splice(index,1)
                    children[index].remove()
                    cChildren[index+1].remove()
                    children2[index].remove()
                    form.render('select');
                    if (children.length === 1) {
                        $(".exportTableHeader").html('');
                        $(".exportTableList").html('')
                        $('.exportTableBox').css({'display': 'none'});
                        $('.no-3').css({'display':'block'})
                    }
                }
            }

        }
        Analysis.computeDataList(Analysis.stepFour.arry); //调用汇总字段
        Analysis.computeDataName(Analysis.stepFour.arry); //调用汇总字段
    });


    /**
     * ************************step2
     */

    Analysis.stepTwo = {  //第二步内容
        query: {
            colName: '',
            list: {},
            conditionList: [],  //表查询条件
            relation: 'AND' //and&or
        },
        local: {
            colName: '',
            list: {},
            conditionList: [],  //表查询条件
        },
        qOperatArry:[   //查询条件数组
            {name:'>',value:1},
            {name:'<',value:2},
            {name:'=',value:3},
            {name:'>=',value:4},
            {name:'<=',value:5},
            {name:'模糊查询',value:6},
            {name:'空值查询',value:7},
            {name:'不包含',value:9},
        ],
    }

    Analysis.stepTwoClean = function () {
        $('#qOperat').find("option:selected").attr("selected", false);
        $("#qField,#qTimeSelect,#qTimeSelect2,#qTimeSelect3").val('');
        $("#fieldOption").html('');
       Analysis.stepTwo.query.list = {};
       Analysis.stepTwo.local.list = {};
        form.render('select');
    }

    /**
     *显示查询条件option
     */
    Analysis.queryOperat = function (data) {
        $("#qOperat").html('');
        if (data) {
            var option = "<option value=''></option>";
            $.each(data, function (i, mes) {
                option += "<option value='" + mes.value + "' class='" + mes.type + "'>" + mes.name + "</option>";
            });
            $("#qOperat").append(option);
            form.render('select');
        }
    }

    /**
     * 字段值/参数显示option
     */
    Analysis.queryData = function (tableName, colName,callback) {
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/analysis/findDicByCol',
            dataType: 'json',
            data: {
                tableName: tableName,
                colName: colName
            },
            success: function (data) {
                var option = '';
                if (data.length > 0) {
                    var idList = []
                    callback('1')
                    $.each(data, function (i, mes) {
                        idList.push(mes.v_item_val_id)
                        option += "<option data-value=" + mes.v_val_name + "  data-id=" + mes.v_item_val_id + " value='" + mes.v_val_name + "'/>";
                    });
                    var html= `<option data-value='所有字典值' data-id='${JSON.parse(JSON.stringify(idList))}' value="所有字典值"></option>`;

                } else {
                    callback('0')
                    option = ""
                }
                $("#fieldOption").append(option).prepend(html);
                form.render('select');
            },
            error: function (e) {
                // console.log(e.status);
                // console.log(e.responseText);
            }
        });

    }

    /**
     * 查询条件显示table
     */
    Analysis.queryTable = function (data) {
        if (data) {
            var tr = '';
            $('.queryTableBox').css({'display':'block'});
            $('.no-2').css({'display':'none'})
            tr += `<tr class='${data.length}'><td>${data[data.length - 1].column}</td><td>${data[data.length - 1].conditionType}</td><td>${data[data.length - 1].value}</td></tr>`;
            $(".queryTableList").append(tr);
            table.init('queryTable', {
                height: 200,//设置高度
            });
        }
    }


    //选择字段
    form.on('select(qDataNameList)', function (data) {
        Analysis.stepTwoClean()
        Analysis.stepTwo.query.list.column = data.value
        var qDataNameList = $('#qDataNameList').find("option:selected")
        Analysis.stepTwo.local.list.column =  qDataNameList.text();
        var type = qDataNameList.attr('class')
        var dateType = qDataNameList.attr('data-time')
        console.log(type)
        console.log(dateType)
        Analysis.stepTwo.query.list.data_type = type;
        // Analysis.stepTwo.query.list.data_time = dateType?dateType:'';

        // $("#cDataName").html('');
        if(Analysis.stepTwo.qOperatArry){
            var qTime = $('.qTime');  //年月日 时间
            var qTime2 = $('.qTime2'); //时间
            var qTime3 = $('.qTime3'); //年月日
            var qOther = $('.qOther');
            switch (type.toString()) {
                case 'S':
                    Analysis.stepTwo.qOperatArry = [   //查询条件数组
                        {name:'=',value:3},
                        {name:'模糊查询',value:6},
                        {name:'空值查询',value:7},
                        {name:'不包含',value:9}, //字符串的不包含
                    ]
                    qTime.css({'display':'none'})
                    qTime2.css({'display':'none'})
                    qTime3.css({'display':'none'})
                    qOther.css({'display':'block'})
                    break;
                case 'N':
                    Analysis.stepTwo.qOperatArry = [   //查询条件数组
                        {name:'>',value:1},
                        {name:'<',value:2},
                        {name:'=',value:3},
                        {name:'>=',value:4},
                        {name:'<=',value:5},
                        {name:'空值查询',value:7},
                    ]
                    qTime.css({'display':'none'})
                    qTime2.css({'display':'none'})
                    qTime3.css({'display':'none'})
                    qOther.css({'display':'block'})
                    break;
                case 'T':
                    Analysis.stepTwo.qOperatArry = [   //查询条件数组
                        {name:'>',value:1},
                        {name:'<',value:2},
                        {name:'=',value:3},
                        {name:'>=',value:4},
                        {name:'<=',value:5},
                        {name:'空值查询',value:7},
                    ]
                    if(dateType){
                        if(dateType === 'yyyyMMdd'){
                            qTime.css({'display':'none'})
                            qTime2.css({'display':'none'})
                            qTime3.css({'display':'block'})
                        }else if(dateType === 'HHmmss'){
                            qTime.css({'display':'none'})
                            qTime2.css({'display':'block'})
                            qTime3.css({'display':'none'})
                        }else if(dateType === 'yyyyMMddHHmmss'){
                            qTime.css({'display':'block'})
                            qTime2.css({'display':'none'})
                            qTime3.css({'display':'none'})
                        }
                    }
                    qOther.css({'display':'none'})
                    break;
                default:
                    Analysis.stepTwo.qOperatArry = [   //查询条件数组
                        {name:'>',value:1},
                        {name:'<',value:2},
                        {name:'=',value:3},
                        {name:'>=',value:4},
                        {name:'<=',value:5},
                        {name:'模糊查询',value:6},
                        {name:'空值查询',value:7},
                        {name:'不包含',value:9},
                    ]
                    qTime.css({'display':'none'})
                    qTime2.css({'display':'none'})
                    qTime3.css({'display':'none'})
                    qOther.css({'display':'block'})
                    break;
            }
        }
        Analysis.queryData(Analysis.data.tableName, data.value,function (r) {
            if(r === '1'){
                Analysis.stepTwo.qOperatArry = [
                    {name:'=',value:3},
                    {name:'空值查询',value:7},
                    {name:'不等于',value:8}, //字典值的不包含
                ]
                qTime.css({'display':'none'})
                qTime2.css({'display':'none'})
                qTime3.css({'display':'none'})
                qOther.css({'display':'block'})
            }
            Analysis.queryOperat(Analysis.stepTwo.qOperatArry)
        });
    });

    laydate.render({
        elem: '#qTimeSelect'
        ,type: 'datetime'
        ,done: function(value, date){
            Analysis.stepTwo.query.list.value = value;
            Analysis.stepTwo.local.list.value = value;
            // layer.alert('你选择的日期是：' + value + '<br>获得的对象是' + JSON.stringify(date));
        }
    });

    laydate.render({
        elem: '#qTimeSelect2'
        ,type: 'time'
        ,done: function(value, date){
            Analysis.stepTwo.query.list.value = value;
            Analysis.stepTwo.local.list.value = value;
            // layer.alert('你选择的日期是：' + value + '<br>获得的对象是' + JSON.stringify(date));
        }
    });

    laydate.render({
        elem: '#qTimeSelect3'
        ,done: function(value, date){
            Analysis.stepTwo.query.list.value = value;
            Analysis.stepTwo.local.list.value = value;
            // layer.alert('你选择的日期是：' + value + '<br>获得的对象是' + JSON.stringify(date));
        }
    });

    //选择查询条件
    form.on('select(qOperat)', function (data) {
        if(data.value === '7'){
            $('#qField').attr('disabled','disabled')
        }else{
            $('#qField').removeAttr('disabled')
        }
        Analysis.stepTwo.query.list.conditionType = data.value
        var text = $('#qOperat').find("option:selected").text();
        Analysis.stepTwo.local.list.conditionType = text;
        console.log(Analysis.stepTwo.query.list.conditionType)
    });

    //选择条件关系关联
    form.on('radio(relation)', function (data) {
        Analysis.stepTwo.query.relation = data.value;
    });

    //选择字段值/参数
    $('#qField').blur(function () {
        var name = $("#qField").val();
        var option = $("#fieldOption option");
        var option_length = option.length;
        var option_id = '',option_value = '';
        if($("#fieldOption").html()){
            for (var i = 0; i < option_length; i++) {
                option_value = option.eq(i).attr('data-value');
                if (name === option_value) {
                    option_id = option.eq(i).attr('data-id');
                    option_value = option.eq(i).attr('data-value');
                    break;
                }
            }
        }else{
            option_id = name;
            option_value = name;
        }
        Analysis.stepTwo.query.list.value = option_id;
        Analysis.stepTwo.local.list.value = option_value;
    });

    //新增查询条件
    $('#queryAdd').click(function () {
        var arr = Analysis.stepTwo.query.list;
        var arr2 = Analysis.stepTwo.local.list;
        var list = Analysis.stepTwo.local.conditionList
        if($(".queryTableList").html()){
            for (var i = 0; i < list.length; i++) {
                if (list[i].column === arr2.column && list[i].conditionType === arr2.conditionType && list[i].value === arr2.value) {
                    layer.msg('请勿重复添加', {icon: 7, time: 2000});
                    return false;
                }
            }
        }else{
            console.log('9999')
            Analysis.stepTwo.query.conditionList = []
            Analysis.stepTwo.local.conditionList = []
        }
        if (arr2.column === "" || arr2.conditionType === "" || arr2.value === "" || arr2.column === undefined || arr2.conditionType === undefined || arr2.value === undefined) {
            if(arr.conditionType !== '7'){
                layer.msg('请补充完整查询条件', {icon: 7, time: 2000});
                return false;
            }
        }
        Analysis.stepTwo.query.conditionList.push({
            column: arr.column,
            conditionType: arr.conditionType,
            value: arr.value?arr.value:'',
            data_type:arr.data_type,
        })
        Analysis.stepTwo.local.conditionList.push({
            column: arr2.column,
            conditionType: arr2.conditionType,
            value: arr2.value?arr2.value:'',
            data_type:arr.data_type,
        })
        Analysis.queryTable(Analysis.stepTwo.local.conditionList);
        Analysis.stepTwoClean()
        $('#qDataNameList').find("option:selected").attr("selected", false);
        $('.qTime,.qTime2,.qTime3').css({'display':'none'})
        $('.qOther').css({'display':'block'})
        form.render('select');
    });

    //删除查询条件
    table.on('tool(queryTable)', function(obj){
        var layEvent = obj.event;
        var tr = obj.tr;
        var zindex = obj.tr.parent().children().index(obj.tr)
        var num = tr.attr('class')
        var cTableList = $('.queryTableList')
        if(layEvent === 'del'){ //删除
            layer.confirm('确认要删除吗', function(index){
                obj.del();
                Analysis.stepTwo.query.conditionList.splice(num - 1, 1);
                Analysis.stepTwo.local.conditionList.splice(num - 1, 1);
                cTableList.children('tr')[zindex].remove()
                layer.close(index);
                if (!Analysis.stepTwo.local.conditionList.length > 0) {
                    $('.queryTableBox').css({'display':'none'});
                    $('.no-2').css({'display':'block'})
                    cTableList.html('');
                }
            });
        }
    })

    Analysis.queryCheck = function(){
        if(!Analysis.data.columnList.length > 0){
            layer.msg('请选择表单字段', {icon: 7, time: 2000});
            return false
        }
        return true
    }

    //点击查询
    $('#query').click(function () {
        if(Analysis.queryCheck()){
            if($(".queryTableList").html()){
                Analysis.queryResult(Analysis, Analysis.local, Analysis.stepTwo.query);
            }else{
                var arr = Analysis.stepTwo.query.list;
                var arr2 = Analysis.stepTwo.local.list;
                Analysis.stepTwo.query.conditionList = []
                Analysis.stepTwo.local.conditionList = []
                if (arr2.value !== "" && arr2.value !== undefined){
                    Analysis.stepTwo.query.conditionList[0] = {
                        column: arr.column,
                        conditionType: arr.conditionType,
                        value: arr.value,
                        data_type:arr.data_type,
                    }
                    Analysis.stepTwo.local.conditionList[0] = {
                        column: arr2.column,
                        conditionType: arr2.conditionType,
                        value: arr2.value,
                        data_type:arr.data_type,
                    }
                }

                Analysis.queryResult(Analysis, Analysis.local, Analysis.stepTwo.query);
            }
        }

    })


    /**
     * ************************step3
     */

    Analysis.stepThreeClean = function () {
        $('.exportTableBox').css({'display': 'none'});
        $('.no-3').css({'display':'block'})
        $(".exportTableList,.exportTableHeader").html('');
    }

    /**
     * 查询结果显示table
     */
    Analysis.queryResult = function (aa, local, data) {
        $(".exportTableHeader").html('');
        $(".exportTableList").html('');
        $('.export-count').html('');
        // var arr = [],list = aa.data.columnList,localList = local.columnList;
        // for(var i in list){
        //     var str = `${list[i]} AS ${localList[i]}`
        //     arr.push(str)
        //     i++
        // }
        var info = {
            tableName: aa.data.tableName,
            columnList: aa.data.columnList,
            conditionList: data.conditionList,
            relation: data.relation
        }
        var index = layer.load(2);
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: Feng.ctxPath + '/analysis/getListBySql',
            dataType: 'json',
            data: JSON.stringify(info),
            success: function (mes) {
                layer.close(index);
                if (mes.data) {
                    var message = mes.data;
                    var th = '', tr = '',p = '';
                    for (var i = 0; i < local.columnList.length; i++) {
                        th += "<th lay-data={field:'"+aa.data.columnList[i]+"',sort:true}>" + local.columnList[i] + "</th>"
                    }
                    $(".exportTableHeader").append(th);
                    for (var j = 0; j < message.length; j++) {
                        var td = '';
                        for (var key in message[j]) {
                            td += "<td>" + message[j][key] + "</td>"
                        }
                        tr += "<tr>" + td + "</tr>";
                    }
                    p += `<p class="export-count">共<b class="export-num">${mes.count || 0}</b> 条记录</p>`
                    $('.exportTableBox').css({'display': 'block'}).prepend(p);
                    $('.no-3').css({'display':'none'})
                    $(".exportTableList").append(tr);
                    if($(".exportTableHeader,.exportTableList").html() !== ''){
                        table.init('exportTable', {
                            height: 400, //设置高度
                            limit: 100
                        });
                    }

                }else{
                    layer.msg('没有查询到结果哦！', {icon: 5, time: 2000});
                    Analysis.stepThreeClean()
                }
            },
            error: function (e) {
                layer.close(index);
                if(e.status === 200){
                    layer.msg('没有查询到结果哦！', {icon: 5, time: 2000});
                    Analysis.stepThreeClean()
                }
            }
        });

    }

    /**
     * 文件导出
     */
    Analysis.exportFile = function (aa, local, data, filename) {
        var index = layer.load(2);
        let info = {
            tableName: aa.tableName,
            columnList: aa.columnList,
            chColumnList:local.columnList,
            conditionList: data.conditionList,
            relation: data.relation,
            fileName: filename
        };
        let url = Feng.ctxPath + "/analysis/exportData";
        let params = JSON.stringify(info);

        let xhr = new XMLHttpRequest();//创建新的XHR对象
        xhr.open('post', url);//指定获取数据的方式和url地址
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8')
        xhr.responseType = 'blob';//以blob的形式接收数据，一般文件内容比较大
        xhr.onload = function (e) {
            var blob = this.response;//Blob数据
            layer.close(index);
            if (this.status == 200) {
                if (blob && blob.size > 0) {
                    saveAs(blob, filename);//处理二进制数据，让浏览器认识它
                }
            }
        };
        xhr.send(params) //post请求传的参数

    }

    Analysis.exportCheck = function(){
        if (!Analysis.data.columnList.length > 0) {
            layer.msg('请选择表单字段', {icon: 7, time: 2000});
            return false;
        }else if($(".exportTableList").html()===""){
            layer.msg('内容为空，无法导出哦！', {icon: 7, time: 2000});
            return false;
        }
        return true
    }

    $('#export').click(function () {
        var date = new Date().toLocaleDateString();
        if(Analysis.exportCheck()){
            layer.prompt({
                formType: 2,
                value: 'test',
                title: '请输入文件名'
            }, function (value, index, elem) {
                Analysis.exportFile(Analysis.data, Analysis.local, Analysis.stepTwo.query, value+'.csv');
                layer.close(index);
            });
        }
    })

    /**
     * ************************step4
     */
    Analysis.stepFour = { //第四步内容
        arry:[], //过滤第一步选择的字段，用于计算字段显示
        compute:{
            list:[],
        },
        local:{
            list:[],
        }
    }
    Analysis.list = {
        compute:{},
        local:{}
    }

    Analysis.stepFourClean = function () {
        $('#cType').find("option:selected").attr("selected", false);
        $('#cDataNameList').find("option:selected").attr("selected", false);
        $("#cDataName").html('');
        Analysis.list.compute= {};
        Analysis.list.local= {};
        form.render('select');
    }


    /**
     * 汇总字段显示option
     */
    Analysis.computeDataList = function (data) {
        $("#cDataNameList").html('')
        if (data) {
            var option = "<option value=''></option>";
            $.each(data, function (i, mes) {
                option += "<option value='" + mes.value + "' class='" + mes.type + "'>" + mes.name + "</option>";
            });
            $("#cDataNameList").append(option);
            form.render('select');
        }
    }

    /**
     * 计算字段显示option
     */
    Analysis.computeDataName = function (data) {
        $("#cDataName").html('');
        if (data) {
            var option = "<option value=''></option>";
            $.each(data, function (i, mes) {
                option += "<option value='" + mes.value + "' class='" + mes.type + "'>" + mes.name + "</option>";
            });
            $("#cDataName").append(option);
            form.render('select');
        }
    }

    /**
     * 计算方式显示table
     */
    Analysis.computeTable = function (data,groupBy) {
        if (data) {
            var tr = '';
            $('.cTableBox').css({'display': 'block'});
            $('.no-4').css({'display':'none'})
            tr += `<tr class='${data.length}'><td>${groupBy}</td><td>${data[data.length - 1].cType}</td><td>${data[data.length - 1].cDataName}</td></tr>`;
            if($(".cTableList").html()!==""){
                var tdArr = $(".cTableBox > .layui-border-box>.layui-table-box>.layui-table-body").find($('tbody')).children('tr').find('td:first')
                var trList = $(".cTableList").find($('tr'))
                for(var i in tdArr){
                    if(tdArr[i].innerText === groupBy){
                        trList.eq(i).after(tr)
                        break
                    }else{
                        $(".cTableList").append(tr);
                        break
                    }
                }
            }else{
                $(".cTableList").append(tr);
            }
            table.init('cTable', {
                height: 200, //设置高度
                done : function(res, curr, count) {
                    mods.rowspan('groupBy',1,'.cTableBox');
                }
            });
        }
    }

    /**
     * 开始计算
     */
    Analysis.compute = function (tableName, compute, query) {
        var info = {
            tableName: tableName,
            list:[],
            conditionList: query.conditionList,
            relation: query.relation
        }
        if(compute.list.length>0){
            for(var i in compute.list){
                info.list.push({
                    groupBy:compute.list[i].groupBy,
                    calcMap:compute.list[i].calcMap,
                })
            }
        }
        var index = layer.load(2);
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: Feng.ctxPath + '/analysis/batch_calc',
            dataType: 'json',
            data: JSON.stringify(info),
            success: function (res) {
                layer.close(index);
                Analysis.responseObject = res
                if(res[compute.list[0].groupBy].length>0){
                    Analysis.computeResult(res,0,compute.list[0].groupBy); //保存计算结果，用于step5
                }else{
                    layer.msg('没有查询到结果哦！', {icon: 5, time: 2000});
                    $('.sChartRow').css({'display':'none'})
                    $('.no-5').css({'display':'block'})
                    $('#sChart').html('')
                }
            },
            error: function (e) {
                layer.close(index);
                if(e.status === 200){
                    layer.msg('没有查询到结果哦！', {icon: 5, time: 2000});
                }
            }
        });
    }

    //选择汇总字段
    form.on('select(cDataNameList)', function (data) {
        var text = $('#cDataNameList').find("option:selected").text();
        Analysis.list.compute.groupBy = data.value
        Analysis.list.local.groupBy = text
    });

    //选择计算方式
    form.on('select(cType)', function (data) {
        var Arry = [],ss = [];
        $("#cDataName").html('');
        Arry = Analysis.stepFour.arry
        if(Arry){
            switch (data.value.toString()) {
                case 'SUM':
                    for(let i in Arry){
                        if(Arry[i].type === 'N'){
                            ss.push(Arry[i])
                            i++
                        }
                    }
                     Arry = ss
                    break;
                case 'AVG':
                    for(let i in Arry){
                        if(Arry[i].type === 'N'){
                            ss.push(Arry[i])
                            i++
                        }
                    }
                     Arry = ss
                    break;
                case 'MAX':
                    for(let i in Arry){
                        if(Arry[i].type !== 'S'){
                            ss.push(Arry[i])
                            i++
                        }
                    }
                     Arry = ss
                    break;
                case 'MIN':
                    for(let i in Arry){
                        if(Arry[i].type !== 'S'){
                            ss.push(Arry[i])
                            i++
                        }
                    }
                    Arry = ss
                    break;
            }
        }
        Analysis.list.compute.cType = data.value
        var text = $('#cType').find("option:selected").text();
        Analysis.list.local.cType = text
        Analysis.computeDataName(Arry);

    });

    //选择计算字段
    form.on('select(cDataName)', function (data) {
        Analysis.list.compute.cDataName = data.value
        var text = $('#cDataName').find("option:selected").text();
        Analysis.list.local.cDataName = text
    });

    //新增
    $('#cAdd').click(function () {
        var arr = Analysis.list.compute;
        var arr2 = Analysis.list.local;
        var list = Analysis.stepFour.local.list
        var flag = false;
        if (arr2.cType === "" || arr2.cDataName === "" || arr2.cType === undefined || arr2.cDataName === undefined) {
            layer.msg('请补充完整计算方式', {icon: 7, time: 2000});
            return false;
        }
        if($(".cTableList").html()){
            for (var i = 0; i < list.length; i++) {
                if(list[i].groupBy === arr2.groupBy){
                    var calcMapList = list[i].calcMap
                    for(let j in calcMapList){
                        if (calcMapList[j].cType === arr2.cType && calcMapList[j].cDataName === arr2.cDataName) {
                            layer.msg('请勿重复添加', {icon: 7, time: 2000});
                            return false;
                        }
                    }
                    flag = true
                    break
                }else{
                    flag = false
                }
            }
        }else{
            Analysis.stepFour.compute.list = []
            Analysis.stepFour.local.list = []
        }

        var key = arr.cType + '(' + arr2.cDataName + ')';
        var value = arr.cDataName
        var obj = {}
        obj.key = key
        obj.value = value
        if($(".cTableList").html()===""){
            Analysis.stepFour.compute.list = []
        }
        if(flag){
            for(let m in list){
                if(list[m].groupBy === arr2.groupBy){
                    Analysis.stepFour.compute.list[m].calcMap.push(obj)
                    Analysis.stepFour.local.list[m].calcMap.push({
                        cType: arr2.cType,
                        cDataName: arr2.cDataName,
                    })
                }
            }
        }else{
            Analysis.stepFour.compute.list.push({
                calcMap:[obj],
                groupBy:arr.groupBy,
                chGroupBy:arr2.groupBy
            })
            Analysis.stepFour.local.list.push({
                calcMap:[{
                    cType: arr2.cType,
                    cDataName: arr2.cDataName,
                }],
                groupBy:arr2.groupBy
            })
        }
        Analysis.computeTable([{cType: arr2.cType, cDataName: arr2.cDataName,}],arr2.groupBy);
        Analysis.stepFourClean()
    });

    //删除计算方式
    table.on('tool(cTable)', function(obj){
        var layEvent = obj.event;
        var tr = obj.tr;
        var zindex = obj.tr.parent().children().index(obj.tr)
        var num = tr.attr('class')
        var cTableList = $('.cTableList')
        if(layEvent === 'del'){ //删除
            layer.confirm('确认要删除吗', function(index){
                var list = Analysis.stepFour.local.list
                for(let i in list){
                    for(let j in list[i].calcMap){
                        if(list[i].groupBy === obj.data.groupBy && list[i].calcMap[j].cType === obj.data.cType && list[i].calcMap[j].cDataName === obj.data.cDataName){
                            Analysis.stepFour.compute.list[i].calcMap.splice(j, 1);
                            Analysis.stepFour.local.list[i].calcMap.splice(j, 1);
                            if(list[i].calcMap.length === 0){
                                Analysis.stepFour.compute.list.splice(i, 1);
                                Analysis.stepFour.local.list.splice(i, 1);
                            }
                            break
                        }
                    }
                    break
                }
                console.log(Analysis.stepFour.local.list)
                obj.del();
                cTableList.children('tr')[zindex].remove()
                mods.rowspan('groupBy',1,'.cTableBox');
                layer.close(index);
                if (!Analysis.stepFour.compute.list.length > 0) {
                    $('.cTableBox').css({'display':'none'});
                    $('.no-4').css({'display':'block'})
                    cTableList.html('');
                }
            });
        }
    })

    Analysis.computeCheck = function(){
        if(!Analysis.data.columnList.length > 0){
            layer.msg('请选择表单字段', {icon: 7, time: 2000});
            return false
        }
        // else if(!Analysis.list.compute.groupBy){
        //     layer.msg('请选择汇总字段', {icon: 7, time: 2000});
        //     return false
        // }
        else if(!Analysis.stepFour.compute.list.length>0 && Analysis.list.compute.cType === undefined && Analysis.list.compute.cDataName === undefined){
            layer.msg('请选择计算方式', { icon: 7, time: 2000});
            return false;
        }
        return true
    }

    $('#compute').click(function () {
        if(Analysis.computeCheck()){
            if($(".cTableList").html()){
                Analysis.compute(Analysis.data.tableName,Analysis.stepFour.compute,Analysis.stepTwo.query)
                Analysis.sChartMesX(Analysis.stepFour.compute.list) //step5：X轴数据
                Analysis.sChartMesY(Analysis.stepFour.compute.list,0) //step5：Y轴数据
                // Analysis.sChartMes(Analysis.stepFour.local.groupBy,Analysis.stepFour.compute.calcMap) //step5：表名和Y轴数据
            }else{
                var aa = Analysis.list.compute;var arr2 = Analysis.list.local;
                if(aa.groupBy === '' || aa.groupBy === undefined ||aa.cType === '' || aa.cType === undefined || aa.cDataName === '' || aa.cDataName === undefined){
                    layer.msg('请补充完整计算方式', { icon: 7, time: 2000});
                    return false;
                }
                var cType = $('#cType').find("option:selected"); //计数方式
                var cDataName = $('#cDataName').find("option:selected"); //计算字段
                // var cChart = $('input[name=cChart]:checked'); //是否展示在图表
                var key = cType.val() + '(' + cDataName.text() + ')';
                var value = cDataName.val()
                var obj = {}
                obj.key = key
                obj.value = value
                Analysis.stepFour.compute.list = []
                Analysis.stepFour.compute.list.push({
                    calcMap:[obj],
                    groupBy:aa.groupBy,
                    chGroupBy:arr2.groupBy
                })
                Analysis.compute(Analysis.data.tableName,Analysis.stepFour.compute,Analysis.stepTwo.query)
                // Analysis.sChartMes(Analysis.stepFour.local.groupBy,Analysis.stepFour.compute.calcMap) //step5：表名和Y轴数据
                Analysis.sChartMesX(Analysis.stepFour.compute.list) //step5：X轴数据
                Analysis.sChartMesY(Analysis.stepFour.compute.list,0) //step5：Y轴数据
            }

        }

    })


    /**
     * ************************step5
     */


    Analysis.stepFive = {  //第五步内容
        sResult:[],
        eChartBase64:'',
        yName:'',
        xName:'',
        chartVal:'',
        xList:[],
        yList:[],
        responseObject:'', //计算结果存储
        listIndex:0 //被保存计算方式展示的list下标 默认为第一个
    }

    Analysis.stepFiveClean =function(){
        Analysis.stepFive.yList = []
    }

    /**
     * echart表头信息
     */
    Analysis.sChartMesX = function(listLocal){
        // $('#sXName').val(g);
        $("#sXName").html('');
        if(listLocal){
            var optionX = '';
            for(var i in listLocal){
                optionX += `<option class='${listLocal[i].groupBy}' value='${i}'>${listLocal[i].chGroupBy}</option>`;
            }
            $("#sXName").append(optionX);
            form.render('select');
        }
    }
    Analysis.sChartMesY = function(listCompute,num){
        // $('#sXName').val(g);
        $("#sYName").html('');
        if(listCompute){
            var optionY = '';
            $.each(listCompute[num].calcMap, function (j, mesY) {
                optionY += `<option class='f${j+1}' value='${mesY.key}'>${mesY.key}</option>`;
            });
            $("#sYName").append(optionY);
            form.render('select');
        }
    }

    /**
     * 计算结果初始化展示
     */
    Analysis.computeResult = function (res,num,groupBy) {
        Analysis.stepFive.xList = []
        Analysis.stepFive.yList = []
        $('.sChartRow').css({'display':'block'})
        $('.no-5').css({'display':'none'})
        Analysis.stepFive.sResult = res[groupBy];
        Analysis.stepFive.xName = groupBy
        Analysis.stepFive.yName = Analysis.stepFour.compute.list[num].calcMap[0].key
        Analysis.stepFive.chartVal = $('#sYName').find("option:selected").attr('class');
        for (var i = 0; i < res[groupBy].length; i++) {
            if(res[groupBy][i][Analysis.stepFive.xName]){
                Analysis.stepFive.xList.push(res[groupBy][i][Analysis.stepFive.xName])
            }
            if(res[groupBy][i][Analysis.stepFive.yName]){
                Analysis.stepFive.yList.push(Math.round((parseFloat(res[groupBy][i][Analysis.stepFive.yName]) /10000) * 100) / 100)
            }
        }
        console.log(Analysis.stepFive.xList)
        console.log(Analysis.stepFive.yList)
        Analysis.sEchart(Analysis.stepFive.xList,Analysis.stepFive.yList,'bar');
    }

    //选择X轴字段
    form.on('select(sXName)', function(d){
        var name = $('#sXName').find("option:selected").attr('class');
        Analysis.sChartMesY(Analysis.stepFour.compute.list,d.value) //step5：X轴数据
        Analysis.computeResult(Analysis.responseObject,d.value,name);
        Analysis.saveModel.listIndex = d.value
    })

    //选择Y轴字段
    form.on('select(sYName)', function(d){
        Analysis.stepFiveClean();
        Analysis.stepFive.yName = d.value
        Analysis.stepFive.chartVal = $('#sYName').find("option:selected").attr('class');
        var res = Analysis.stepFive.sResult;
        for(var i=0;i<res.length;i++){
            if(res[i][Analysis.stepFive.yName]){
                Analysis.stepFive.yList.push(Math.round((parseFloat(res[groupBy][i][Analysis.stepFive.yName]) /10000) * 100) / 100)
            }
        }
        Analysis.sEchart(Analysis.stepFive.xList,Analysis.stepFive.yList,'bar');
    });

    //选择展示样式
    form.on('radio(sType)', function(d){
        if(d.value === 'bar'){
            Analysis.saveModel.chartType = 1;
        }else if(d.value === 'pie'){
            Analysis.saveModel.chartType = 3;
        }else if(d.value === 'line'){
            Analysis.saveModel.chartType = 4;
        }
        Analysis.sEchart(Analysis.stepFive.xList,Analysis.stepFive.yList,d.value);
    });

    /**
     * 展示图表
     */
    Analysis.sEchart = function (x,y,t) {
        var myChart = eCharts.init(document.getElementById('sChart'));
        var yPie = [];
        var n=0;
        var colorList = ["#2C86FD","#5EDFCD","#59CB74","#FCD14B","#E7A376"];
        var option = { // 指定图表的配置项和数据
            title: {
                text: ''
            },
            tooltip: {},
            legend: {
                data:['']
            },
            animation:false,
            series: [{
                name: '',
                itemStyle:{
                    normal:{
                        color: function (){ //随机颜色
                            var colorIndex = Math.floor(Math.random()*colorList.length);
                            var color = colorList[colorIndex];
                            colorList.splice(colorIndex,1);
                            return color;
                        }
                    },
                    lineStyle:{
                        color: function (){ //随机颜色
                            var colorIndex = Math.floor(Math.random()*colorList.length);
                            var color = colorList[colorIndex];
                            colorList.splice(colorIndex,1);
                            return color;
                        }
                    }
                },
                type: t,
                data:t === 'pie'?yPie:y
            }]
        };

        if(t === 'pie'){
            var name = Analysis.stepFive.yName;
            for(var i=0;i<y.length;i++){
                var obj = {};
                obj.value = y[i]
                obj.name = x[i]
                yPie.push(obj);
            }

            delete option.xAxis;
            delete option.yAxis;

        }else{
            option.xAxis={data:x}
            option.yAxis = {name : '单位(万)'}
        }
        myChart.setOption(option,true);
        Analysis.stepFive.eChartBase64=myChart.getDataURL()//图片转码
    }

    /**
     * 保存图表
     */
    Analysis.saveEchart = function(tableName,list,query,filename){
        var index = layer.load(2);
        let info = {
            tableName:tableName,
            list:list,
            conditionList: query.conditionList,
            relation: query.relation,
            chartCode:Analysis.stepFive.eChartBase64
        }
        console.log(info)
        let url = Feng.ctxPath + "/analysis/exportCalc";
        let params = JSON.stringify(info);

        let xhr = new XMLHttpRequest();//创建新的XHR对象
        xhr.open('post', url);//指定获取数据的方式和url地址
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8')
        xhr.responseType = 'blob';//以blob的形式接收数据，一般文件内容比较大
        xhr.onload = function (e) {
            var blob = this.response;//Blob数据
            layer.close(index);
            if (this.status == 200) {
                if (blob && blob.size > 0) {
                    saveAs(blob, filename);//处理二进制数据，让浏览器认识它
                }
            }
        };
        xhr.send(params) //post请求传的参数
    }

    Analysis.saveCheck = function(){
        if (!Analysis.data.columnList.length > 0) {
            layer.msg('请选择表单字段', {icon: 7, time: 2000});
            return false;
        }else if(!Analysis.stepFour.compute.list.length>0){
            layer.msg('请先设置计算方式哦！', { icon: 7, time: 2000});
            return false;
        }else if($("#sXName").val() === ''){
            layer.msg('内容为空，无法保存哦！', { icon: 7, time: 2000});
            return false;
        }else if($('#sTableName').val() === ''){
            layer.msg('请填写图表名称', { icon: 7, time: 2000});
            return false;
        }
        return true
    }

    $("#save").click(function () {
        var filename = $('#sTableName').val()
        if(Analysis.saveCheck()){
            Analysis.saveEchart(Analysis.data.tableName,Analysis.stepFour.compute.list,Analysis.stepTwo.query,filename+'.xlsx')
        }
    })


    /**
     * ************************saveModel
     */

    Analysis.saveModel = { //保存模型
        modelType1ItemValId:'',
        modelType2ItemValId:'',
        chartType: 1,
    }

    /**
     * 查询一级菜单详情
     */
    Analysis.firstData =function(){
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
    Analysis.secondData =function(obj){
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

    Analysis.sSaveModel = function (a,f,q,s,r,m) {
        // var arr = [],charr = [],list = a.data.columnList,localList = a.local.columnList;
        // for(var i in list){
        //     var str = `${list[i]} AS ${localList[i]}`
        //     arr.push(str)
        //     i++
        // }
        var info = {
            tableName: a.data.tableName,
            columnList: a.data.columnList,
            chColumnList:a.local.columnList,
            conditionList: q.conditionList,
            relation: q.relation,
            groupBy: f.groupBy || '',
            calcMap: f.calcMap || [],
            ruleName: r,
            chartType: s.chartType || '',
            modelDesc: m,
            modelType1ItemId: s.modelType1ItemId,
            modelType1ItemValId: s.modelType1ItemValId,
            modelType2ItemId: s.modelType2ItemId,
            modelType2ItemValId: s.modelType2ItemValId,
            chartVal: Analysis.stepFive.chartVal || 'f1',
            chartX:'f0'
        }
        console.log(info)
        var index = layer.load(2);
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: Feng.ctxPath + '/analysis/saveAsModel',
            dataType: 'json',
            data: JSON.stringify(info),
            success: function (res) {
                layer.close(index);
                if(res.success){
                    layer.msg('添加成功！', {
                        icon: 6,
                        time: 2000
                    }, function(){
                        layer.closeAll();
                        scrollTo(0, 0);
                        $('#dataList').find("option:selected").attr("selected", false);
                        Analysis.clean()
                    });
                }else{
                    layer.msg('添加失败！', {icon: 5, time: 2000});
                }
            },
            error: function (e) {
                layer.close(index);
                layer.msg('添加失败！', {icon: 5, time: 2000});
            }
        });

    }

    //选择一级目录
    form.on('select(mFirst)', function(data){
        var text = $('#mFirst').find("option:selected").attr('class');
        Analysis.saveModel.modelType1ItemId = text;
        Analysis.saveModel.modelType1ItemValId = data.value;
        if(data.value !== ''){
            Analysis.secondData(data);
        }else{
            $("#mSecond").html('')
            form.render();
        }
    });

    //选择二级目录
    form.on('select(mSecond)', function(data){
        var text = $('#mSecond').find("option:selected").attr('class');
        Analysis.saveModel.modelType2ItemId = text;
        Analysis.saveModel.modelType2ItemValId = data.value;
    });

    //重新分析
    $('#tabAdd').click(function () {
        layer.confirm('是否清空当前页面录入内容,重新分析？',{icon: 3, title:'提示'}, function(index){
            scrollTo(0, 0);
            Analysis.clean();
            var dataList = $('#dataList')
            dataList.find("option:selected").attr("selected", false);
            dataList.next().find($('input')).val('')
            $(".dataTableBox > .layui-border-box>.layui-table-box>.layui-table-body").find($('tbody')).html('')
            layer.close(index);
        });
    });

    Analysis.modelCheck = function(){
        if (!Analysis.data.columnList.length > 0) {
            layer.msg('请选择表单字段', {icon: 7, time: 2000});
            return false;
        }
        return true
    }

    Analysis.modelConCheck = function(){
        if ($('#mName').val()==='') {
            layer.msg('模型名称不能为空', {icon: 7, time: 2000});
            return false;
        }else if($('#mFirst').find("option:selected").val()===''){
            layer.msg('请选择一级分类', {icon: 7, time: 2000});
            return false;
        }else if($('#mSecond').find("option:selected").val() ===''){
            layer.msg('请选择二级分类', {icon: 7, time: 2000});
            return false;
        }
        return true
    }

    //保存提交
    $('#tabSave').click(function () {
        if(Analysis.modelCheck()){
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
                    if(Analysis.modelConCheck()){
                        var num = Analysis.stepFive.listIndex
                        Analysis.sSaveModel(Analysis,Analysis.stepFour.compute.list[num] === undefined ? [] : Analysis.stepFour.compute.list[num],Analysis.stepTwo.query,Analysis.saveModel,r,m);
                    }
                    return false
                 },
                success: function(layero, index){
                    $('#mName').val(Analysis.analysisInfo.aName || '')
                    $('#mDes').val(Analysis.analysisInfo.aDesc || '')
                    Analysis.firstData();
                },
                end:function () {
                }
            });
        }
    });



    /**
     * ***********************************************大数据版*******************************************************************************
     */

    /**
     * ************************提交分析
     */

    Analysis.analysisInfo = {
        aName:'',
        aDesc:''
    }

    Analysis.upAnalysis = function(a,f,q,s,r,m){
        var info = {
            tableName: a.data.tableName,
            columnList: a.data.columnList,
            chColumnList:a.local.columnList,
            conditionList: q.conditionList,
            relation: q.relation,
            groupBy: f.compute.groupBy,
            calcMap: f.compute.calcMap,
            ruleName: r,
            chartType: s.chartType,
            modelDesc: m?m:'',
            // modelType1ItemId: s.modelType1ItemId,
            // modelType1ItemValId: s.modelType1ItemValId,
            // modelType2ItemId: s.modelType2ItemId,
            // modelType2ItemValId: s.modelType2ItemValId,
            chartVal: 'f1',
            chartX:'f0'
        }
        console.log(info)
        var index = layer.load(2);
        $.ajax({
            type: 'POST',
            contentType: 'application/json',
            url: Feng.ctxPath + '/analysis/commitAnalysis',
            dataType: 'json',
            data: JSON.stringify(info),
            success: function (res) {
                layer.close(index);
                console.log(res)
                if(res.success){
                    layer.confirm('当前操作已提交，正在等待执行。是否跳转至执行管理页面，查看执行结果?',  {icon: 3, title:'提示'},function(index){
                        mods.tabChange({"menuId":"/manage","menuPath":"/manage","menuName":"执行管理"},5,'/manage','执行管理')
                        layer.close(index);
                    });
                }
            },
            error: function (e) {
                layer.close(index);
                layer.msg('添加失败！', {icon: 5, time: 2000});
            }
        });
    }

    $('#aName').blur(function () {
        Analysis.analysisInfo.aName = $('#aName').val()
    })

    $('#aDesc').blur(function () {
        Analysis.analysisInfo.aDesc = $('#aDesc').val()
    })

    Analysis.upCheck = function(){
        if($('#aName').val()==='') {
            layer.msg('分析名称不能为空', {icon: 7, time: 2000});
            return false;
        }
        return true
    }


    $('#tabUp').click(function () {
        if(Analysis.queryCheck() && Analysis.upCheck()){
            // if($(".queryTableList").html()){
            //     layer.msg('请将查询条件新增到列表中哦！', {icon: 7, time: 2000});
            //     return false;
            // }
            // if(!$(".cTableList").html()){
            //     layer.msg('请将计算方式新增到列表中哦！', {icon: 7, time: 2000});
            //     return false;
            // }
            // var aa = Analysis.list.compute;
            // if(aa.cType === '' || aa.cType === undefined || aa.cDataName === '' || aa.cDataName === undefined){
            //     layer.msg('请补充完整计算方式', { icon: 7, time: 2000});
            //     return false;
            // }
            var cType = $('#cType').find("option:selected"); //计数方式
            var cDataName = $('#cDataName').find("option:selected"); //计算字段
            var key = cType.val() + '(' + cDataName.text() + ')';
            var value = cDataName.val()
            var obj = {}
            obj.key = key
            obj.value = value
            Analysis.stepFour.compute.calcMap = []
            Analysis.stepFour.compute.calcMap[0]=obj

            var aName = $('#aName').val()
            var aDesc = $('#aDesc').val()
            Analysis.upAnalysis(Analysis,Analysis.stepFour,Analysis.stepTwo.query,Analysis.saveModel,aName,aDesc);
        }
    });

    Analysis.modelConCheckBd = function(){
        if ($('#mName').val()==='') {
            layer.msg('模型名称不能为空', {icon: 7, time: 2000});
            return false;
        }
        return true
    }

    //保存提交
    $('#tabSave_bd').click(function () {
        if(Analysis.modelCheck()){
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
                    if(Analysis.modelConCheckBd()){
                        var num = Analysis.stepFive.listIndex
                        Analysis.sSaveModel(Analysis,Analysis.stepFour.compute.list[num] === undefined ? [] : Analysis.stepFour.compute.list[num],Analysis.stepTwo.query,Analysis.saveModel,r,m);
                    }
                    return false
                },
                success: function(layero, index){
                    $('#mName').val(Analysis.analysisInfo.aName || '')
                    $('#mDes').val(Analysis.analysisInfo.aDesc || '')
                    Analysis.firstData();
                },
                end:function () {
                }
            });
        }
    });








});
