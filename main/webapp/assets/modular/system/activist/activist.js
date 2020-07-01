
layui.use(['table', 'admin', 'ax', 'laydate'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var laydate = layui.laydate;

    /**
     * 系统管理--活动客户管理
     */
    var Activist = {
        tableId: "activistTable",
        condition: {
            activistId: "",
        }
    };

    /**
     * 初始化表格的列
     */
    Activist.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'activistId', hide: true, sort: true, title: 'id'},
            {field: 'name', sort: true, title: '姓名'},
            {field: 'phone', sort: true, title: '电话号码'},
            {field: 'activityId', sort: true, title: '活动'},
            {field: 'gender', sort: true, title: '性别'},
            {field: 'contactStatus', sort: true, title: '联系状态'},
            {field: 'coreCustomer', sort: true, title: '核心客户'},
            {field: 'customerWishes', sort: true, title: '客户意愿'},
            {field: 'followUpAgain', sort: true, title: '再次跟进'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Activist.search = function () {
        var queryData = {};
        queryData['condition'] = $("#name").val();
        queryData['contactStatus'] = $("#contactStatus").val();
        queryData['coreCustomer'] = $("#coreCustomer").val();
        queryData['customerWishes'] = $("#customerWishes").val();
        queryData['followUpAgain'] = $("#followUpAgain").val();
        queryData['activityId'] = $("#activityId").val();
        table.reload(Activist.tableId, {where: queryData});
    };

    /**
     * 弹出沟通纪要表格
     *
     */
    Activist.openCommunication = function (data) {
        location.href=Feng.ctxPath + '/communication?activistId='+data.activistId;
        /*top.layui.admin.open({
            type: 2,
            title: '沟通纪要',
            content: Feng.ctxPath + '/communication?activistId='+data.activistId
        });*/
    };





    /**
     * 弹出添加
     */
    Activist.openAddActivist = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加活动客户',
            area: ['600px', '350px'],
            content: Feng.ctxPath + '/activist/activist_add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Activist.tableId);
            }
        });
    };

    /**
     * 导出excel按钮
     */
    Activist.exportExcel = function () {
        var checkRows = table.checkStatus(Activist.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };


    /**
     * 点击编辑活动客户
     *
     * @param data 点击按钮时候的行数据
     */
    Activist.onEditActivist = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '修改活动客户',
            area: ['600px', '350px'],
            content: Feng.ctxPath + '/activist/activist_update?activistId=' + data.activistId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Activist.tableId);
            }
        });
    };

    /**
     * 点击删除活动客户
     *
     * @param data 点击按钮时候的行数据
     */
    Activist.onDeleteActivist = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/activist/delete", function () {
                Feng.success("删除成功!");
                table.reload(Activist.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("activistId", data.activistId);
            ajax.start();
        };
        Feng.confirm("是否删除活动客户 " + data.name + "?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Activist.tableId,
        url: Feng.ctxPath + '/activist/list',
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Activist.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Activist.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Activist.openAddActivist();
    });

    // 导出excel
    $('#btnExp').click(function () {
        Activist.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + Activist.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            Activist.onEditActivist(data);
        } else if (layEvent === 'delete') {
            Activist.onDeleteActivist(data);
        }else if (layEvent === 'communication') {
            Activist.openCommunication(data);
        }
    });
});
