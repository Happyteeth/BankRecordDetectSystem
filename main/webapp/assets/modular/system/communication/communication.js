layui.use(['table', 'admin', 'ax', 'laydate'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var laydate = layui.laydate;

    /**
     * 系统管理--沟通纪要管理
     */
    var Communication = {
        tableId: "communicationTable",
        condition: {
            communicationId: "",
        }
    };

    var activistId = Feng.getUrlParam("activistId");

    /**
     * 初始化表格的列
     */
    Communication.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'communicationId', hide: true, sort: true, title: 'id'},
            {field: 'activityId', sort: true, title: '所属活动'},
            {field: 'communicationInfo', sort: true, title: '详细纪要',width:800},
            {field: 'communicationTime', sort: true, title: '沟通时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Communication.search = function (data) {
        var queryData = {};
        queryData['condition'] = $("#name").val();
        queryData['activistId'] = activistId;
        table.reload(Communication.tableId, {where: queryData});
    };

    /**
     * 弹出添加
     */
    Communication.openAddCommunication = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加沟通纪要',
            area: ['800px', '450px'],
            content: Feng.ctxPath + '/communication/communication_add?activistId='+activistId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Communication.tableId);
            }
        });
    };

    /**
     * 导出excel按钮
     */
    Communication.exportExcel = function () {
        var checkRows = table.checkStatus(Communication.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };


    /**
     * 点击编辑沟通纪要
     *
     * @param data 点击按钮时候的行数据
     */
    Communication.onEditCommunication = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '修改沟通纪要',
            area: ['800px', '450px'],
            content: Feng.ctxPath + '/communication/communication_update?communicationId=' + data.communicationId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Communication.tableId);
            }
        });
    };

    /**
     * 点击删除沟通纪要
     *
     * @param data 点击按钮时候的行数据
     */
    Communication.onDeleteCommunication = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/communication/delete", function () {
                Feng.success("删除成功!");
                table.reload(Communication.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("communicationId", data.communicationId);
            ajax.start();
        };
        Feng.confirm("是否删除该沟通纪要?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Communication.tableId,
        url: Feng.ctxPath + '/communication/list?activistId='+activistId,
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Communication.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Communication.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Communication.openAddCommunication();
    });

    // 导出excel
    $('#btnExp').click(function () {
        Communication.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + Communication.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            Communication.onEditCommunication(data);
        } else if (layEvent === 'delete') {
            Communication.onDeleteCommunication(data);
        }
    });
});
