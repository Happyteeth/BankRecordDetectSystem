layui.use(['form', 'table', 'upload', 'ax', 'element', 'tree', 'admin', 'layer'], function () {
    var $ = layui.$;
    var form = layui.form;
    var table = layui.table;
    var upload = layui.upload;
    var $ax = layui.ax;
    var element = layui.element;
    var layer = layui.layer;
    var admin = layui.admin;
    var UPLOAD_FILES;
    var pathList = [];

    var File = {
        tableId: "fileImportTable",  //表格id
        formId: "import"
    };



    $("#fileChose").click(function () {
        clearFile();
        $('#fileChose').attr("value", "");
        $("#import")[0].reset();
        layui.form.render();
        $("#file").click();
    });

    $("#refresh").click(function () {
        table.reload(File.tableId);
    });
    /*
    初始化表格的列
     */
    File.initColumn = function () {
        return [[
            {field: 'fileId', hide: true, sort: true, title: '文件id'},
            {title: '序号', templet: '#xuhao', align: 'center', fixed: "left", width: 58},
            {field: 'dInsert', align: 'center', sort: true, title: '导入时间', width: 170},
            {field: 'oriPath', sort: true, title: '源文件名',templet: function (d) {
                    return File.getFileName(d.oriPath);
                }},
            {
                field: 'tableName', title: '导入表', width: 260
            },
            {
                field: 'status', align: 'center', title: '导入状态', templet: function (d) {
                    return File.getStatus(d.status);
                }, width: 90
            },
            {field: 'lineSucc', title: '成功行数', width: 90, align: 'center'},
            {
                field: 'lineError',
                title: '失败行数',
                style: 'color:#4361E3;cursor: pointer;',
                event: 'download',
                width: 90,
                align: 'center'
            },
            {align: 'center', fixed: "right", toolbar: '#tableBar', title: '操作', width: 120}
        ]];
    };

    // 渲染表格
    table.render({
        elem: '#' + File.tableId,
        url: Feng.ctxPath + '/file/list?tableName=' + $('#tableName option:selected').val(),
        page: true,
        limits: [20, 40, 60],
        limit: 20,
        skin: 'line',
        cols: File.initColumn(),
    });

    /*   tableName.click(function () {
           tableResult.reload();
       });*/

    /**
     * 点击删除按钮
     *
     * @param data 点击按钮时候的行数据
     */
    File.onDeleteFile = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/file/del", function (data) {
                Feng.success("删除成功!");
                table.reload(File.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("fileId", data.fileId);
            ajax.start();
        };
        Feng.confirm("是否删除文件 " + data.oriPath + "?", operation);
    };

    //大数据版选择文件
    $("#bdFileChose").click(function () {
        pathList =[];
        var filePathAndUploadFlag = new Array();
        $('#bdFileChose').attr("value", "");
        layui.form.render();
        layer.open({
            type: 2,
            title: "选择文件",
            id: "frameId",
            //skin: 'layui-layer-molv', //加上边框
            area: ['980px', '550px'], //宽高
            content: Feng.ctxPath + '/file/fileChose',
            btn:['我已选好','取消'],
            yes: function(index, layero){
                var body = layer.getChildFrame('body', index);
                var iframeWin = window[layero.find('iframe')[0]['name']];

                filePathAndUploadFlag=iframeWin.layui.selectFileAndUploadFlag(filePathAndUploadFlag);
                console.log(filePathAndUploadFlag);
                var uploaded = new Array();
                var f = judgeIfUpload(filePathAndUploadFlag,uploaded);
                console.log(pathList);
                console.log(f);
                if(!f){
                    layer.confirm("当前文件已导入成功，是否继续进行数据导入？", {
                        btn: ['继续', '取消'],
                        // btn2: function (index, layero) {
                        //     clearFile();
                        //     console.log("jij");
                        //     $('#bdFileChose').attr("value", "");
                        //     $("#import")[0].reset();
                        //     layui.form.render();
                        // }
                        yes:function (index1) {
                            pathList = iframeWin.layui.selectFile();
                            layer.close(index);
                            layer.close(index1);
                            console.log(pathList);
                        },
                       btn2:function (index, layero) {
                            console.log("取消");
                            pathList= new Array();
                            filePathAndUploadFlag = new Array();
                           console.log(pathList);
                           console.log(filePathAndUploadFlag);
                            $('#bdFileChose').attr("value", "");
                            $("#import")[0].reset();
                        }
                    }
                    )
                }else{
                    pathList = iframeWin.layui.selectFile();
                    console.log(pathList);
                    layer.close(index);
                }
            },cancel: function(){
                //do noting
            }
        });
    });

    //判断文件是否已上传
    var judgeIfUpload = function(filePathAndUploadFlag, uploaded){
        var f = true;
        for (let i = 0, len = filePathAndUploadFlag.length; i < len; i++) {
            if(filePathAndUploadFlag[i].get("uploadFlag")=="是") {
                f = false;
                uploaded.push(filePathAndUploadFlag[i]);
            }
        }
        return f;
        console.log(uploaded);
    }



    $('#bdCommit').click(function () {
        var info = {
            pathList: pathList,
            tableName: $('#tableName option:selected').val(),
            fileFormat: $('input[name="fileFormat"]:checked').val(),
            delimitField: $('input[name="delimitField"]:checked').val(),
            ifTitle: $('input[name="ifTitle"]:checked').val()
        }
        var disMatchPathList = new Array();

        $.ajax({
            url: Feng.ctxPath + '/file/bdImport',
            dataType: 'json',
            type: 'POST',
            contentType: 'application/json',
            data:JSON.stringify(info),
            success: function (data) {
                //添加提示信息，防止重复提交
                pathList=[];
                $('#bdFileChose').attr("value", "");
                $("#import")[0].reset();
                layui.form.render();
                table.reload(File.tableId);
                setTimeout(function(){//设置时间，多久可以改变状态为可以点击
                    this.disabled = '';
                    console.log("点击");
                },2000);//2秒内不可以重复点击，一秒等于1000毫秒
            }
        });
    });

    // //判断文件和表是否匹配
    // var matchPathListAndTableName = function(pathList, tableName, disMatchPathList){
    //     var f = true;
    //     for(let i = 0 ;i<pathList.length;i++){
    //         if(pathList[i].indexOf(tableName)<0){
    //             f = false;
    //             disMatchPathList.push(pathList[i]);
    //         }
    //     }
    //     console.log(disMatchPathList);
    //     return f;
    // }

    //文件预览
    File.onViewFile = function (param) {
        var attrListJson = '';
        var tableRes = '';
        var attrCols = [];   //存放表头数据
        layer.open({
            type: 1,
            title: "源文件预览",
            //skin: 'layui-layer-molv', //加上边框
            area: ['950px', '550px'], //宽高
            content: '<div><table class="layui-table" id="templateTable" lay-filter="templateTable">' +
                '<thead class="exportHeader">' +
                ' <tr class="exportTableHeader"></tr>' +
                '</thead>' +
                '<tbody class="exportTableList"></tbody>' +
                '</table>' +
                '</div>'
        });
        $.ajax({
            url: Feng.ctxPath + '/file/view',
            dataType: 'json',
            data: {
                fileId: param.fileId
            },
            type: 'get',
            success: function (redata) {
                console.log(redata);
                if (redata) {
                    attrListJson = redata.data.title;//不需要用$.parseJSON(data)转换格式，用了会报错
                    tableRes = redata.data.data;
                    console.log(tableRes);
                    initTable(attrListJson);//获取表头成功后初始化表格
                } else {
                    layer.msg('没有查询到数据，生成报表失败！', {icon: 5, time: 2000});
                }
            },
            error: function () {

            }
        });
        //初始化table
        //==========alen=========
        function initTable(theads) {
            //通过遍历结果List<Map>，动态拼接表头
            var arr = Object.keys(theads);
            var n = arr.length
            for (var thead in theads) {
                attrCols.push({
                    field: thead,
                    title: attrListJson[thead],
                    //sort: true,
                    align: 'center',
                    /*width: (100/n)+'%'*/
                });
            }
            //渲染table,假分页
            layui.use("table", function () {
                console.log(tableRes);
                var table = layui.table;
                table.render({
                    elem: "#templateTable",
                    // url: "",
                    data: tableRes,
                    page: true,// 开启分页
                    even: false,//隔行背景
                    cellMinWidth: 150,
                    cols: [attrCols], //表头
                    done: function () {
                        element.render();
                    }
                });
            });
        }
    }
    //错误文件下载
    File.errFileDownload = function (param) {
        window.open(Feng.ctxPath + "/file/download/" + param.fileId)
    };

    //获取原文件名
    File.getFileName = function (oriPath) {
        var idx = oriPath.lastIndexOf('/')
        idx = idx > -1 ? idx : oriPath.lastIndexOf('\\')
        if (idx < 0) {
            return oriPath
        }
        return oriPath.substring(idx + 1);
    }
/*
    //根据表名获取表中文描述
    File.getTableCName = function (tableName) {
        var tableCName = ''
        $.ajax({
            url: Feng.ctxPath + "/file/tableCName"
            , dataType: "json"
            , async: false
            , data: {tableName: tableName}
            , success: function (res) {
                console.log(res);
                tableCName = res;
            }
        });
        return tableCName;
    }*/

    //导入文件状态
    File.getStatus = function (status) {
        var str = '';
        if (status === '1' || status === '2') {
            str = "未处理";
            return str;
        } else if (status === '5' ) {
            str = "处理成功";
            return str;
        } else if ( status === '9') {
            str = "处理异常";
            return str;
        } else {
            str = "处理中"
            return str;
        }
    }

    //给目标表名下拉框赋值
    $.ajax({
        url: Feng.ctxPath + '/report/tableList',
        dataType: 'json',
        type: 'get',
        success: function (data) {
            var list = JSON.parse(JSON.stringify(data));
            $.each(list, function (key, value) {
                $('#tableName').append(new Option(value.table_name + "-" + value.table_desc, value.table_name));// 下拉菜单里添加元素
            });
            layui.form.render("select");
        }
    });

    //文件上传

    var uploadListIns = upload.render({
        elem: '#file',
        url: Feng.ctxPath + '/file/import',
        auto: false,//选择文件后不自动上传
        accept: 'file',
        multiple: true,
        size: 0,
        number: 0,
        bindAction: '#commit',
        //上传前的回调
        before: function () {
            var path = document.getElementById("file").value;
            this.data = {
                tableName: $('#tableName option:selected').val(),
                fileFormat: $('input[name="fileFormat"]:checked').val(),
                delimitField: $('input[name="delimitField"]:checked').val(),
                ifTitle: $('input[name="ifTitle"]:checked').val()
            }
            layer.load(); //上传loading
        },
        //选择文件后的回调
        choose: function (obj) {
            console.log(obj)
            UPLOAD_FILES = this.files = obj.pushFile(); //将每次选择的文件追加到文件队列
            clearFile(); //将所有文件先删除再说
            obj.preview(function (index, file, result) {
                console.log(file)
                $.ajax({
                    url: Feng.ctxPath + '/file/exit',
                    dataType: 'json',
                    data: {
                        fileName: file.name,
                        fileSize: parseInt(file.size)
                    },
                    success: function (res) {
                        if (res.code == 200) {
                        } else {
                            layer.confirm(res.message, {
                                    btn: ['继续', '取消'],
                                    btn2: function (index, layero) {
                                        // clearFile();
                                        // $('#fileChose').attr("value", "");
                                        // $("#import")[0].reset();
                                        // layui.form.render();
                                    }
                                }
                            );
                        }
                    }
                });
                $('#fileChose').attr("value", $('#fileChose').val() + file.name + ';');
                obj.pushFile();  //再把当前文件重新加入队列
            })
        }
        , allDone: function (obj) { //当文件全部被提交后，才触发
            console.log(obj);
            console.log(obj.total); //得到总文件数
            console.log(obj.successful); //请求成功的文件数
            console.log(obj.aborted); //请求失败的文件数
            clearFile();
            layui.form.render();
            table.reload(File.tableId);
        },
        done: function (res, index, upload) {
            layer.closeAll('loading'); //关闭loading
            if (res.code == 200) {
                layer.confirm("文件成功上传", {
                    btn: ['OK'] //按钮
                });
                clearFile();
                $('#fileChose').attr("value", "");
                $("#import")[0].reset();
                layui.form.render();
                table.reload(File.tableId);
            } else {
                layer.confirm(res.message, {
                    btn: ['确认'] //按钮
                });
                clearFile();
                $('#fileChose').attr("value", "");
                $("#import")[0].reset();
                layui.form.render();
                table.reload(File.tableId);

            }
        }
        , error: function (index, upload) {
            layer.closeAll('loading'); //关闭loading
            layer.confirm("文件上传出错", {btn: ['确认']});
            clearFile();
            $('#fileChose').attr("value", "");
            $("#import")[0].reset();
            layui.form.render();
            table.reload(File.tableId);
        }
    });

    //清空文件队列
    function clearFile() {
        for (let x in UPLOAD_FILES) {
            delete UPLOAD_FILES[x];
        }
    }


    // 工具条点击事件
    table.on('tool(' + File.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        console.log("data:" + data.status)
        if (layEvent === 'view') {
            File.onViewFile(data);
        } else if (layEvent === 'del') {
            File.onDeleteFile(data);
        } else if (layEvent === 'download') {
            File.errFileDownload(data);
        }
    });

});

/*  //单独渲染字段个数列
  File.getFieldNum = function (param) {
      var num = '';
      if (fieldNumGroup != null && fieldNumGroup != undefined) {
          $.each(fieldNumGroup, function (index, value) {
              if (value.tableName == param) {
                  num = value.fieldNum;
                  return num;
              }
          })
      }
      return num;
  };*/
/*var html = '<div class ="layui-progress layui-progress-big" lay-showpercent="true" lay-filter="status">';
                    html += '<div class="layui-progress-bar "  lay-percent="' + File.progress(d.status) + '%"></div>';
                    html += '</div>';
                    return html;*/
/*    //状态栏进度条
    File.progress = function (param) {

        var num = '';
        if (param == '1') {
            return num = 25;
        } else if (param == '2') {
            return num = 50;
        } else if (param == '4') {
            return num = 75;
        } else if (param == '5') {
            return num = 100;
        }
        return num;
    };
    */
/*            {
                field: 'fieldNum', width: 80, title: '字段数', templet: function (d) {
                    return File.getFieldNum(d.tableName);
                }
            },//单独渲染*/
/*    //获取字段个数
    var fieldNumGroup;
    $.ajax({
        url: Feng.ctxPath + "/file/fieldNum"
        , method: 'GET'
        , dataType: "json"
        , async: false
        , success: function (res) {
            fieldNumGroup = JSON.parse(JSON.stringify(res));
            console.log(fieldNumGroup)
            return fieldNumGroup;
        }
    });*/
/*
 *  预览
 */
/*   File.onViewFile = function (param) {
       var ajax = new $ax(Feng.ctxPath + "/file/view/" + param.fileId, function (data) {
           console.log("前n行：" + data.lineN)
           Feng.infoDetail("文件预览", data.lineN);
       }, function (data) {
           Feng.error("预览文件失败!");
       });
       ajax.start();
   };*/