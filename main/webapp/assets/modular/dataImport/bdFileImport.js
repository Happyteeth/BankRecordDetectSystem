layui.use(['form', 'table', 'upload', 'ax', 'element', 'tree', 'admin', 'layer'], function () {
    var $ = layui.$;
    var table = layui.table;
    var form = layui.form;
    let selects = ''; //全局变量
    var filePath ='';
    var BdFile = {
        tableId: "bdFileImportTable",  //表格id
        condition: {
            fileName: "",
            path: "",
            uploadFlag:""
        }
    };

    //给选择文件下拉框赋值
    $.ajax({
        url: Feng.ctxPath + '/file/filePath',
        dataType: 'json',
        type: 'get',
        success: function (data) {
            console.log(data.data);
            var list = data.data;
            $.each(list, function (key, value) {
                console.log(key);
                console.log(value);
                if(key==0){
                    selects += '<option value = "' + value + '" selected>' + value + '</option>'
                }else {
                    selects += '<option value = "' + value + '">' + value + '</option>'
                }
                console.log(selects);
                //$('#filePath').append(new Option(value, value));// 下拉菜单里添加元素
            });

            $('#filePath').html(selects);
            layui.form.render("select");
            tableRender();
        }
    });

    form.on('select(filePathFilter)', function () {
        // $(document).on('click', '#createReport', function () {
        tableRender();
    })
    // 大数据版选择文件时渲染表格
    function tableRender(){
        let selectVal = $('#filePath').val();
        filePath = selectVal;
        console.log(selectVal);
        table.render({
            elem: '#' + BdFile.tableId,
            url: Feng.ctxPath + '/file/getFiles?filePath='+selectVal,
            page: true,
            limits: [10, 20, 30],
            limit: 10,
            skin: 'line',
            //data:{filePath:$('#filePath').val()},
            cols: [[
                {type: 'checkbox'}
                , {field: 'fileName', width:250,title: '文件名称',sort: true}
                , {field: 'type', title: '类型'}
                , {field: 'size', title: '大小'}
                , {field: 'uploadFlag', title: '是否已导入', sort: true}
                , {field: 'time', title: '上传时间', align: "center",sort: true}
            ]],
            done: function(res, curr, count){
                //如果是异步请求数据方式，res即为你接口返回的信息。
                //如果是直接赋值的方式，res即为：{data: [], count: 99} data为当前页数据、count为数据总长度
                //console.log(res);
                //得到当前页码
                //console.log(curr);
                //得到数据总量
                //console.log(count);
                $("[data-field='size']").children().each(function(){
                    if($(this).text()=="大小"){

                    }else {

                        var decimal = parseFloat($(this).text());
                        console.log(decimal);
                        if(decimal<1024){
                            $(this).text(dealDecimal(decimal,2)+"B");
                        }else if(decimal>1024&&decimal<1024*1024){
                            $(this).text(dealDecimal(decimal/(1024),2)+"KB");
                        }
                        else if(decimal>1024*1024&&decimal<1024*1024*1024){
                            $(this).text(dealDecimal(decimal/(1024*1024),2)+"MB");
                        }else if(1024*1024*1024<decimal){
                            $(this).text(dealDecimal(decimal/(1024*1024*1024),2)+"GB");
                        }
                    }
                });
            }
        });
    }

    //四舍五入方法，num是要处理的数字  v为要保留的小数位数
    var dealDecimal = function(num,v){
        var vv = Math.pow(10,v);
        return Math.round(num*vv)/vv;
    }

    $('#btnSearch').click(function () {
        BdFile.search();
        form.render();
    });

    var pathList = new Array();

    layui.selectFile=function () {
        var fileRows = table.checkStatus(BdFile.tableId);
        console.log(parent.layui.$('#bdFileChose').val())
        for (var i = 0; i < fileRows.data.length; i++) {
            var fileName = fileRows.data[i].fileName;
            var filePath = fileRows.data[i].path;
            pathList.push(filePath + '/' + fileName);
            //子页面给父页面赋值
            parent.layui.$('#bdFileChose').attr("value",  parent.layui.$('#bdFileChose').val()+fileName + ';');
            //parent.layer.close(index);
        }
        return pathList;
    }

    var filePathAndUploadFlag = [];
    //选择文件路径和是否已上传标志
    layui.selectFileAndUploadFlag=function (filePathAndUploadFlag) {
        var fileRows = table.checkStatus(BdFile.tableId);

        console.log(parent.layui.$('#bdFileChose').val())
        for (var i = 0; i < fileRows.data.length; i++) {
            var fileMap = new Map();
            var fileName = fileRows.data[i].fileName;
            var filePath = fileRows.data[i].path;
            var uploadFlag = fileRows.data[i].uploadFlag;
            var filePathTemp = (filePath + '/' + fileName).toString().replace(/\\/g,"/");
            console.log(filePathTemp);

            fileMap.set("path", filePathTemp);
            fileMap.set("uploadFlag", uploadFlag);
            filePathAndUploadFlag.push(fileMap);
            //子页面给父页面赋值

            //parent.layer.close(index);
        }
        console.log(filePathAndUploadFlag);
        return filePathAndUploadFlag;
    }

    BdFile.search = function () {
        console.log($('#ifExit option:selected').val());
        var queryData = {};
        // queryData['deptId'] = MgrUser.condition.deptId;
        queryData['fileName'] = $("#fileName").val();
        queryData['path'] = $("#path").val();
        queryData['uploadFlag'] = $('#ifExit option:selected').val();
        table.reload(BdFile.tableId, {where: queryData, page: {
                curr: 1 //重新从第 1 页开始
            }});
    };

});