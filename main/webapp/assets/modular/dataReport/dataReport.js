layui.use(['echarts', 'form', 'FileSave', 'layer'], function () {
    let $ = layui.$;
    let form = layui.form;
    let layer = layui.layer;
    let eCharts = layui.echarts;
    let fileSave = layui.FileSave;
    let selects = ''; //全局变量
    let reportParentDiv = document.getElementById('data_report');
    let myCharts = [];
    //下拉框赋值
    $.ajax({
        url: Feng.ctxPath + '/report/hasReportTableList',
        dataType: 'json',
        type: 'get',
        success: function (res) {
            console.log(res);
            if (res != null && res != undefined && res.length != 0) {
                let dataArr = res.data;
                let reportDate = res.time;
                if (dataArr != null && dataArr != undefined && dataArr.length != 0) {
                    // let select = document.getElementsByName("table");
                    for (let i = 0; i < dataArr.length; i++) {
                        let data = dataArr[i];
                        selects += '<option value = "' + data.table_name + '">' + data.table_desc + '</option>'
                    }
                    $('#tableList').html(selects);
                    form.render('select');
                    //input
                    document.getElementById('report_time').innerHTML = '报表生成时间：' + reportDate;
                    createReport();
                } else {
                    document.getElementById('report_time').innerHTML = '数据报表生成中...';
                }
            }
        }
    });
    //生成报表
    form.on('select(createReport)', function () {
        createReport();
    })

    let createReport = function () {
        let selectVal = $('#tableList').val();
        reportParentDiv.innerHTML = '';
        myCharts = [];
        if (selectVal == undefined || selectVal == null || selectVal == "") {
            alert("请选择要生成报表的表");
            var p = "<p class='nothing'>暂无数据</p>";
            $('.dataReport-box').css({'height': '100vh'})
            $('#data_report').append(p);
        } else {
            console.log("获取到的表名为：" + selectVal)
            $.ajax({
                url: Feng.ctxPath + '/report/getReportByTable?tableName=' + selectVal,
                dataType: 'json',
                type: 'get',
                success: function (res) {
                    if (res != undefined && res.length >= 1) {
                        $('.dataReport-box').css({'height': 'auto'})
                        console.log(res);
                        let resLength = res.length;
                        for (let resKey in res) {
                            let data = res[resKey];
                            let divId = data.divId;
                            let chartType = data.type;
                            if (chartType == undefined || chartType == null || chartType == '') {
                                continue;
                            }
                            createDiv(divId, reportParentDiv, resLength);
                            if (chartType == 1) {
                                creatHistogram(data, divId);
                            } else if (chartType == 2) {
                                creatHistogram2(data, divId);
                            } else if (chartType == 3) {
                                serSeriesData(data);
                                createPieChart(data, divId);
                            } else if (chartType == 4) {
                                createLineChart(data);
                            }
                        }
                    }
                },
                error: function () {
                    alert("没有查询到数据，生成报表失败");
                }
            });
        }
    }

    //导出报表
    $(document).on('click', "#exportReport", function () {
        let params = {};
        let tableName = $('#tableList').val();
        let chTableName = $('#tableList option:selected').text();
        if (myCharts.length == 0) {
            Feng.error("请选择要导出的报表！！！")
            return;
        }
        let index = layer.load(0);
        let myChartBase64 = [];
        for (let myChart of myCharts) {
            let reportParam = {};
            reportParam.title = myChart.getModel().option.title[0].text;
            reportParam.chartCode = myChart.getDataURL({type: 'png'});
            myChartBase64.push(reportParam);
        }
        params.tableName = tableName;
        params.chartTypes = myChartBase64;
        params.chTableName = chTableName;
        let url = Feng.ctxPath + '/report/exportReport';
        let xhr = new XMLHttpRequest();//创建新的XHR对象
        xhr.open('post', url);//指定获取数据的方式和url地址
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8')
        xhr.responseType = 'blob';//以blob的形式接收数据，一般文件内容比较大
        xhr.onload = function (e) {
            var blob = this.response;//Blob数据
            if (this.status == 200) {
                if (blob && blob.size > 0) {
                    saveAs(blob, chTableName + ".xlsx");//处理二进制数据，让浏览器认识它
                    layer.close(index);
                }
            }
        };
        xhr.send(JSON.stringify(params)) //post请求传的参数
    })

    //创建eChartsDiv
    function createDiv(divId, parentDiv, resLength) {
        let childDiv = document.createElement('div');
        childDiv.id = divId
        childDiv.style.width = '30%';
        childDiv.style.height = '300px';
        childDiv.style.position = 'relative';
        childDiv.style.verticalAlign = 'top';
        childDiv.style.display = 'inline-block';
        childDiv.style.paddingTop = '10px';
        childDiv.style.paddingBottom = '15px';
        childDiv.style.paddingLeft = '15px';
        childDiv.style.paddingRight = '15px';
        parentDiv.appendChild(childDiv);
    }

    //创建柱状图图表 - 竖状
    let creatHistogram = (reData, divId) => {
        let myChart = eCharts.init(document.getElementById(divId));
        let sumValue = valSum(reData.val);
        let option = {
            title: {
                text: reData.title,
                x: 'center',
                // padding: [10, 10, 10, 100]  // 位置
                top: '5%'
            },
            tooltip: {},
            xAxis: {
                type: 'category',
                data: reData.xAxis,
                axisLabel: {
                    // interval: 0,//0：全部显示，1：间隔为1显示对应类目，2：依次类推，（简单试一下就明白了，这样说是不是有点抽象）
                    // rotate: -30,//倾斜显示，-：顺时针旋转，+或不写：逆时针旋转
                },
                nameTextStyle: {   // 坐标轴名称样式
                    color: 'red',
                    padding: [5, 0, 0, -5]
                },
            },
            grid: {
                top: '20%',
                bottom: '3%',
                left: '4%',
                right: '3%',
                containLabel: true
            },
            yAxis: {},
            series: [{
                type: 'bar',
                itemStyle: {
                    normal: {
                        color: "#30D9CF",
                        //以下为是否显示，显示位置和显示格式的设置了
                        label: {
                            show: true,
                            position: 'inside',
                            color: '#333',
                            formatter: function (params) {
                                let str = Number(params.data * 100 / sumValue).toFixed(1);
                                str += '%'
                                return str;
                            }
                        }
                    }
                },
                data: reData.val
            }]
        };
        myChart.setOption(option);
        window.addEventListener("resize", myChart.resize);
        myCharts.push(myChart);
    }
    //创建柱状图图表 - 横状
    let creatHistogram2 = (reData, divId) => {
        let myChart = eCharts.init(document.getElementById(divId));
        let sumValue = valSum(reData.val);
        let option = {
            title: {
                text: reData.title,
                x: 'center',
                top: '5%'
                // padding: [5% 10, 10, 0]  // 位置
            },
            grid: {
                left: 80
            },
            xAxis: {
                axisLabel: {
                    // interval: 0,//0：全部显示，1：间隔为1显示对应类目，2：依次类推，（简单试一下就明白了，这样说是不是有点抽象）
                    rotate: 30//倾斜显示，-：顺时针旋转，+或不写：逆时针旋转
                }
            },
            tooltip: {},
            yAxis: {
                type: 'category',
                inverse: true,
                data: reData.xAxis
            },
            grid: {
                top: '20%',
                bottom: '3%',
                left: '4%',
                right: '3%',
                containLabel: true
            },
            series: [{
                type: 'bar',
                data: reData.val,
                itemStyle: {
                    normal: {
                        //好，这里就是重头戏了，定义一个list，然后根据所以取得不同的值，这样就实现了，
                        color: "#30D9CF",
                        //以下为是否显示，显示位置和显示格式的设置了
                        label: {
                            show: true,
                            position: ['50%', '50%'],
                            color: '#333',
                            formatter: function (params) {
                                let str = Number(params.data * 100 / sumValue).toFixed(1);
                                str += '%'

                                return str;
                            }
                        }
                    }
                }
            }]
        };
        myChart.setOption(option);
        window.addEventListener("resize", myChart.resize);
        myCharts.push(myChart);
    }
    //创建饼状图
    let createPieChart = (reData, divId) => {
        let myChart = eCharts.init(document.getElementById(divId));
        let option = {
            title: {
                text: reData.title,
                x: 'center',
                top: '5%'
            },
            tooltip: {
                trigger: 'item',
                formatter: "{a} <br/>{b} : {c} ({d}%)"
            },
            legend: {
                // orient: 'horizontal', //图例列表的布局朝向。 horizontal - 横向 ， vertical - 竖向
                // x: 'center', // 居中显示
                // bottom: 10, //  距离底部距离
                type: 'scroll',
                // orient: 'vertical',
                // // right: 5,
                // x: 'right',
                // top: 40,
                bottom: 10,
                data: reData.xAxis,

                selected: reData.selected
            },
            grid: {
                top: '20%',
                bottom: '3%',
                left: '4%',
                right: '3%',
                containLabel: true
            },
            series: [{
                left: 'center',//离容器左侧的距离
                top: 'top',//距离容器上测的距离
                center: ['50%', '50%'],
                radius: '60%',
                type: 'pie',
                label: { //饼图图形上的文本标签
                    normal: {
                        show: true,
                        textStyle: {
                            fontWeight: 500,
                            fontSize: 12 //文字的字体大小
                        }
                    }
                },
                itemStyle: {
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    },
                    normal: {
                        color: function (params) {
                            //自定义颜色
                            let colorList = ["#2C86FD","#5EDFCD","#59CB74","#FCD14B","#E7A376"];
                            // let colorList = ["#B5FF91", "#94DBFF", "#FFBAFF", "#FFBD9D", "#C7A3ED", "#CC9898", "#8AC007", "#CCC007", "#FFAD5C"];
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
                name: '数据',
                data: reData.seriesData
            }]
        };
        myChart.setOption(option);
        window.addEventListener("resize", myChart.resize);
        myCharts.push(myChart);
    }
    //创建折线图
    let createLineChart = (reData) => {
        let myChart = eCharts.init(document.getElementById(reData.divId));
        let option = {
            title: {
                text: reData.title,
                x: 'center',
                top: '5%'
            },
            tooltip: {
                trigger: 'axis'
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: reData.xAxis
            },
            yAxis: {
                type: 'value'
            },
            grid: {
                top: '20%',
                bottom: '3%',
                left: '4%',
                right: '3%',
                containLabel: true
            },
            series: [{
                data: reData.val,
                type: 'line',
                color: '#5B9BD5',
                itemStyle: {
                    normal: {
                        label: {
                            show: true,
                            formatter: function (params) {
                                if (params.value > 0) {
                                    return params.value;
                                } else {
                                    return '';
                                }
                            },
                            textStyle: {
                                color: '#000'
                            }
                        },
                        textColor: '#000'
                    },
                    emphasis: {
                        label: {
                            show: true
                        }
                    }
                }
            }]
        };
        myChart.setOption(option);
        window.addEventListener("resize", myChart.resize);
        myCharts.push(myChart);
    }
    //整理饼状图的数据
    let serSeriesData = (data) => {
        let name = data.xAxis;
        let val = data.val;
        let seriesData = [];
        let selected = {};
        for (let nameKey in name) {
            seriesData.push({
                name: name[nameKey],
                value: val[nameKey]
            });
            selected[name[nameKey]] = nameKey < 6;
        }
        data.seriesData = seriesData;
        data.selected = selected;
    }
    let valSum = (val) => {
        let sVal = 0;
        if (val === undefined || val === null || val.length === 0 ) {
            return sVal;
        }
        for (let nameKey in val) {
            sVal += val[nameKey];
        }
        return sVal;
    }
})