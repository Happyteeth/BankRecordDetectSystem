layui.use(['form', 'element', 'echarts', 'FileSave','mods'], function () {  //如果只加载一个模块，可以不填数组。如：layui.use('form')
    var $ = layui.$;
    var form = layui.form; //获取form模块
    var element = layui.element; //获取element模块
    var eCharts = layui.echarts;
    var reportParentDiv = document.getElementById('data_report');
    var layer = layui.layer;
    let fileSave = layui.FileSave;
    var downPdf = document.getElementById("renderPdf");
    var mods = layui.mods;
    let myChartBase64 = "";
    var report = {
        autoCheckId:'',
        idList:{},
        export:{
            tableCheck:false,
            chartCheck:false,
        }
    }

    report.fList = function (id,url,type) {
        $.ajax({
            url: Feng.ctxPath + url,
            dataType: 'json',
            // data:{
            //     ruleRunId: '016676b6edfd4184915962d90c5d3465',
            //     autoCheckId: '0'
            // },
            type: 'POST',
            data:{autoCheckId:id},
            success: function (res) {
                if(res.tree){
                    var html = '';
                    for (var item of res.tree) {
                        var iitemContent = '',iitemCard = '';
                        for (var iitem of item.children) {
                            var chd = iitem.children && iitem.children.length > 0;
                            if(chd){
                                var card = ''
                                for (var iiitem of iitem.children) {
                                    card += `<div class="layui-colla-item">
                                                   <h2 class="layui-colla-title" id="${iiitem.id}">${iiitem.title?iiitem.title:'------'} &nbsp;&nbsp; 
                                                   <i class="resCount">(违规记录 <b class="${iiitem.resultCount?'resred':''}">${iiitem.resultCount?mods.format(iiitem.resultCount):0}</b>条</i>
                                                   <i class="resCount">,总计 <b class="${iiitem.checkCount?'resred':''}">${iiitem.checkCount?mods.format(iiitem.checkCount):0}</b>条</i> 
                                                   <i class="resCount">,违规占比 <b class="${iiitem.proportion?'resred':''}">${iiitem.resultCount === 0?0:iiitem.proportion}</b> %)</i>
                                                   </h2>
                                                     <div class="layui-colla-content">
                                                         <div class="exportTableBox">
                                                            <p class="text-right">
                                                                <button id="uploadProg"  class="layui-btn layui-btn-sm" >导出表格</button>
                                                                <span class="id${iiitem.id}" style="display: none"></span>
                                                            </p>
                                                            <table class="layui-table" id="exportTable${iiitem.id}" lay-filter="exportTable">
                                                                <thead class="exportHeader">
                                                                <tr class="exportTableHeader"></tr>
                                                                </thead>
                                                                <tbody class="exportTableList"></tbody>
                                                            </table>
                                                         </div>
                                                         <p class="text-right">
                                                            <button  id="progLoad"   class="layui-btn layui-btn-sm" >导出图表</button>
                                                            <span class="id${iiitem.id}" style="display: none"></span>
                                                         </p>
                                                         <div class="echartBox${iiitem.id}">
                                                            <div id="data_report${iiitem.id}" class="layui-card-body" style="width: 600px;height:400px;margin: auto"></div>
                                                         </div>
                                                    </div>
                                        </div>`
                                }
                                iitemContent += `<div class="layui-collapse" >
                                                <div class="layui-colla-item">
                                                    <h2 id="${iitem.id}" class="layui-colla-title">${iitem.title}</h2>
                                                    <div class="layui-colla-content layui-show">
                                                        <div class="layui-collapse" lay-filter="second">${card}</div>
                                                    </div>
                                                </div>
                                             </div>`
                            }else{
                                iitemCard += `<div class="layui-colla-item">
                                                   <h2 class="layui-colla-title" id="${iitem.id}">${iitem.title?iitem.title:'------'} &nbsp;&nbsp; 
                                                   <i class="resCount">(违规记录 <b class="${iitem.resultCount?'resred':''}">${iitem.resultCount?mods.format(iitem.resultCount):0}</b>条</i> 
                                                   <i class="resCount">,总计 <b class="${iitem.checkCount?'resred':''}">${iitem.checkCount?mods.format(iitem.checkCount):0}</b>条</i> 
                                                   <i class="resCount">,违规占比 <b class="${iitem.proportion?'resred':''}">${iitem.resultCount === 0?0:iitem.proportion}</b> %)</i></h2>
                                                     <div class="layui-colla-content">
                                                         <div class="exportTableBox">
                                                            <p class="text-right">
                                                                <button id="uploadProg"  class="layui-btn layui-btn-sm" >导出表格</button>
                                                                <span class="id${iitem.id}" style="display: none"></span>
                                                            </p>
                                                            <table class="layui-table" id="exportTable${iitem.id}" lay-filter="exportTable">
                                                                <thead class="exportHeader">
                                                                <tr class="exportTableHeader"></tr>
                                                                </thead>
                                                                <tbody class="exportTableList"></tbody>
                                                            </table>
                                                         </div>
                                                         <p class="text-right">
                                                            <button  id="progLoad"   class="layui-btn layui-btn-sm" >导出图表</button>
                                                            <span class="id${iitem.id}" style="display: none"></span>
                                                         </p>
                                                         <div class="echartBox${iitem.id}">
                                                            <div id="data_report${iitem.id}" class="layui-card-body" style="width: 600px;height:400px;margin: auto"></div>
                                                         </div>
                                                    </div>
                                        </div>`
                            }

                        }
                        html += `<div class="layui-colla-item">
                                    <h2 id="${item.id}" class="layui-colla-title">${item.title}</h2>
                                    <div class="layui-colla-content layui-show" style="${iitemCard === '' ? 'display:block' : 'display:none;padding: 0;'};">${iitemContent}</div>
                                    <div class="layui-colla-content layui-show" style="${iitemCard === '' ? 'display:none;padding: 0;' : 'block'};"><div class="layui-collapse" lay-filter="second">${iitemCard}</div></div>
                                 </div>`
                    }
                    $("#modelList").append(html);

                    element.render('collapse');
                    if(report.idList){
                        report.openBox(report.idList.fid,report.idList.sid,report.idList.tid)
                        var div_top = $('#'+report.idList.tid).offset().top ;
                        scrollTo(0, div_top);
                    }
                }
            }
        });
    }

    $(document).ready(function () {
        var localTest = layui.sessionData('autoCheckId');
        var idList = layui.sessionData('idList');
        report.autoCheckId = localTest.autoCheckId
        report.edition = layui.sessionData('userInfo').userInfo.edition;
        if(idList){
            report.idList = idList.idList
        }
        if(report.edition === 'pay'){
            report.fList(report.autoCheckId,'/detect/checkResult','pay')
        }else{
            report.fList(report.autoCheckId,'/detect/checkResult','other')
         }

        $('.report_dialog').css({'display':'none'})
        toTop()
    });

    report.openBox = function(f,s,t){
        $('#'+f).next().addClass('layui-show')
        $('#'+s).next().addClass('layui-show')
        $('#'+t).next().addClass('layui-show')
        $('#'+t).css({'color':'#0C79FD'})
        $('#'+f).children('.layui-colla-icon').html('&#xe61a')
        $('#'+s).children('.layui-colla-icon').html('&#xe61a')
        $('#'+t).children('.layui-colla-icon').html('&#xe61a')   // "&#xe602;" : "&#xe61a;"
        getTable(t)
    }


    //监听折叠
    element.on('collapse(second)', function (data) {
        $('.layui-colla-title').css({'color':'#333'})
        if(data.show){
            data.title.css({'color':'#0C79FD'})
            getTable(data.title[0].id)
        }else{
            data.title.css({'color':'#333'})
        }
    });

    $('.historyBtn').click(function () {
        layui.sessionData('result', {key: 'result', value:1}); //存储查看检测结果判断字符
        // layui.sessionData('autoCheckId', {key: 'autoCheckId',remove: true});
        window.location.href = Feng.ctxPath + "/detect/autoCheckResult";
    })

    //监听提交按钮
    form.on('submit(test)', function (data) {
        console.log(data);
    });

    element.on('tab(demo)', function (data) {
        console.log(data);
    });

    $(".goTop").click(function() {
        $('html,body').animate({
            scrollTop: 0
        }, 600);
    });

    function toTop() {
        $(window).scroll(function() {
            var s = $(window).scrollTop();
            if (s > 300) {
                $(".goTop").fadeIn(100);
            } else {
                $(".goTop").fadeOut(200);
            }
        });
    }

    getTable = function (id) {
        var attrListJson = '';
        var tableRes = '';
        var attrCols = [];   //存放表头数据
        var index = layer.load(2);
        $.ajax({
            url: Feng.ctxPath + '/detect/result',
            dataType: 'json',
            data: {
                autoCheckId:report.autoCheckId,
                ruleId: id
            },
            type: 'get',
            success: function (redata) {
                layer.close(index);
                if (redata) {
                    attrListJson = redata.data.table.title;//不需要用$.parseJSON(data)转换格式，用了会报错
                    tableRes = redata.data.table.data;
                    initTable(attrListJson,id);//获取表头成功后初始化表格
                    report.eChart(redata.data.echart,id);
                    $('.id'+id).html(redata.data.table.ruleRunId)
                    if(redata.data.table.count === '0'){
                        report.export.tableCheck = false
                    }else{
                        report.export.tableCheck = true
                    }
                    toTop()
                }else{
                    report.export.tableCheck = false
                    layer.msg('没有查询到数据，生成报表失败！', {icon: 5, time: 2000});
                }
            },
            error: function () {
                report.export.tableCheck = false
            }
        });
        //初始化table
        //==========alen=========
        function initTable(theads,id) {
            //通过遍历结果List<Map>，动态拼接表头
            var arr = Object.keys(theads);
            var n = arr.length
            for (var thead in theads) {
                attrCols.push({
                    field: thead,
                    title: attrListJson[thead],
                    sort: true,
                    align: 'center',
                    width:200
                    // width: (100/n)+'%'
                });
            }
            //渲染table,假分页
            layui.use("table", function () {
                var table = layui.table;
                table.render({
                    elem: "#exportTable"+id,
                    // url: "",
                    data: tableRes,
                    page: true,// 开启分页
                    cellMinWidth: 105,
                    even: false,//隔行背景
                    cols: [attrCols]  //表头
                });
            });
        }

    }

    closeDialog = function(){
        $('.report_dialog').css({'display':'none'})
    }

    exportTable=function(){

    }
    /**
     * 展示图表
     */
    report.eChart = function (data,id) {
        if(!data.data){
            var p = `<h3 class="text-center" style="padding: 50px 0;border:1px solid #ddd;">暂无数据</h3>`
            $('.echartBox'+id).html(p)
            report.export.chartCheck = false
            return false
        }
        report.export.chartCheck = true
        var myChart = eCharts.init(document.getElementById('data_report'+id));
        var colorList = ["#2C86FD","#5EDFCD","#59CB74","#FCD14B","#E7A376"];
        var chartType = report.getChartType(data.chartType?data.chartType:'');
        var seriesData = report.seriesData(data);
        console.log(seriesData)
        var option = { // 指定图表的配置项和数据
            title: {
                text: ''
            },
            tooltip: {},
            legend: {
                data: ['']
            },
            animation: false,
            series: [{
                name: '',
                itemStyle: {
                    normal: {
                        color: function () { //随机颜色
                            var colorIndex = Math.floor(Math.random() * colorList.length);
                            var color = colorList[colorIndex];
                            colorList.splice(colorIndex, 1);
                            return color;
                        }
                    },
                    lineStyle: {
                        color: function () { //随机颜色
                            var colorIndex = Math.floor(Math.random() * colorList.length);
                            var color = colorList[colorIndex];
                            colorList.splice(colorIndex, 1);
                            return color;
                        }
                    }
                },
                type: chartType,
                data: seriesData
            }]
        };
        if (chartType === 'pie') {
            delete option.xAxis;
            delete option.yAxis;
        } else {
            option.xAxis = {data: report.xAxisData(data)};
            option.yAxis = {};
        }
        myChart.setOption(option);
        myChartBase64=myChart.getDataURL({type: 'png'});

    }

    report.xAxisData = (data) => {
        var chartX = data.chartX;
        var chartData = data.data;
        var xAxisData = [];
        for (var chartDataKey in chartData) {
            xAxisData.push(chartData[chartDataKey][chartX]);
        }
        return xAxisData;
    }

    report.getChartType = (chartType) => {
        if (chartType === undefined || chartType == null || chartType === "") {
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

    report.seriesData = (data) => {
        var chartVal = data.chartVal;
        var chartX = data.chartX;
        var chartData = data.data;
        var seriesData = [];
        for (var i in chartData) {
            if (data.chartType === '3') {
                var obj = {};
                obj.value = chartData[i][chartVal];
                obj.name = chartData[i][chartX];
                seriesData.push(obj);
            } else {
                seriesData.push(chartData[i][chartVal]);
            }
        }
        return seriesData;
    }
    // 创建a标签，设置属性，并触发点击下载
    // var $a = $("<a>");
    // $a.attr("href", redata.data.file);
    // $a.attr("download", redata.data.filename);
    // $("body").append($a);
    // $a[0].click();
    // $a.remove();

    //导出图表
    $(document).on('click', "#progLoad", function () {
        if(!report.export.chartCheck){
            layer.msg('暂无图表信息，无法导出！', {icon: 5, time: 2000});
            return false
        }
            let index = layer.load(0);
            let param = {};
            param.ruleRunId = $(this)[0].nextElementSibling.innerHTML;
            param.chartTypes = myChartBase64;
            let url = Feng.ctxPath + '/detect/downprog';
            let xhr = new XMLHttpRequest();//创建新的XHR对象
            xhr.open('post', url);//指定获取数据的方式和url地址
            xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8')
            xhr.responseType = 'blob';//以blob的形式接收数据，一般文件内容比较大
            xhr.onload = function (e) {
                var blob = this.response;//Blob数据
                if (this.status == 200) {
                    if (blob && blob.size > 0) {
                        saveAs(blob, "chart" + ".xlsx");//处理二进制数据，让浏览器认识它
                        layer.close(index);
                    }
                }
            };
            xhr.send(JSON.stringify(param)); //post请求传的参数
    })



    $(document).on('click', "#batchExport", function () {
        let autoCheckId = report.autoCheckId;
        if (autoCheckId == undefined || autoCheckId == null || autoCheckId == '') {
            layui.error("批次ID出错");
        }
        let index = layer.load(0);
        let url = Feng.ctxPath + '/detect/batchExport?autoCheckId=' + autoCheckId;
        let xhr = new XMLHttpRequest();//创建新的XHR对象
        xhr.open('get', url);//指定获取数据的方式和url地址
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8')
        xhr.responseType = 'blob';//以blob的形式接收数据，一般文件内容比较大
        xhr.onload = function (e) {
            var blob = this.response;//Blob数据
            if (this.status == 200) {
                layer.close(index);
                if (blob && blob.size > 0) {
                    saveAs(blob, "检测数据结果.zip");//处理二进制数据，让浏览器认识它
                }
            }
        };
        xhr.send();
    })

    check = function(){
        if($('.report_dialog').css('display') === 'none'){
            $('.report_dialog').css({'display':'block'})
            return false
        }
        return true
    }

    downPdf.onclick = function() {
        if(check()){
            var element = $("#export_content");// 这个dom元素是要导出pdf的div容器
            var target=document.getElementById("export_content");
            target.style.background='#ffffff';
            var w = element.width();    // 获得该容器的宽
            var h = element.height();    // 获得该容器的高
            var offsetTop = element.offset().top;    // 获得该容器到文档顶部的距离
            var offsetLeft = element.offset().left;    // 获得该容器到文档最左的距离
            var canvas = document.createElement("canvas");
            var abs = 0;
            var win_i = $(window).width();    // 获得当前可视窗口的宽度（不包含滚动条）
            var win_o = window.innerWidth;    // 获得当前窗口的宽度（包含滚动条）
            if(win_o>win_i){
                abs = (win_o - win_i)/2;    // 获得滚动条长度的一半
            }
            canvas.width = w * 2;    // 将画布宽&&高放大两倍
            canvas.height = h * 2;
            var context = canvas.getContext("2d");
            context.scale(2, 2);
            context.translate(-offsetLeft-abs,-offsetTop);
            // 这里默认横向没有滚动条的情况，因为offset.left(),有无滚动条的时候存在差值，因此
            // translate的时候，要把这个差值去掉
            html2canvas(element).then(function(canvas) {
                var contentWidth = canvas.width;
                var contentHeight = canvas.height;
                //一页pdf显示html页面生成的canvas高度;
                var pageHeight = contentWidth / 592.28 * 841.89;
                //未生成pdf的html页面高度
                var leftHeight = contentHeight;
                //页面偏移
                var position = 0;
                //a4纸的尺寸[595.28,841.89]，html页面生成的canvas在pdf中图片的宽高
                var imgWidth = 595.28;
                var imgHeight = 592.28/contentWidth * contentHeight;
                var pageData = canvas.toDataURL('image/jpeg', 1.0);
                var pdf = new jsPDF('', 'pt', 'a4');
                //有两个高度需要区分，一个是html页面的实际高度，和生成pdf的页面高度(841.89)
                //当内容未超过pdf一页显示的范围，无需分页
                if (leftHeight < pageHeight) {
                    pdf.addImage(pageData, 'JPEG', 0, 0, imgWidth, imgHeight);
                } else {    // 分页
                    while(leftHeight > 0) {
                        pdf.addImage(pageData, 'JPEG', 0, position, imgWidth, imgHeight)
                        leftHeight -= pageHeight;
                        position -= 841.89;
                        //避免添加空白页
                        if(leftHeight > 0) {
                            pdf.addPage();
                        }
                    }
                }
                pdf.save('REPORT.PDF');
            })
        }
    }

   /**
   * 导出列表数据
     */
   $(document).on('click', "#uploadProg", function (e) {
       if(!report.export.tableCheck){
           layer.msg('暂无列表信息，无法导出！', {icon: 5, time: 2000});
           return false
       }
       let ruleRunId = $(this)[0].nextElementSibling.innerHTML;
       window.location.href = Feng.ctxPath + "/mod/uploadResultData?ruleRunId=" + ruleRunId;
   });

});
