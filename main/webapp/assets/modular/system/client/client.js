
layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 系统管理--客户管理
     */
    var Client = {
        tableId: "clientTable",
        condition: {
            clientId: "",
        }
    };

    /**
     * 初始化表格的列
     */
    Client.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'clientId', hide: true, sort: true, title: 'id'},
            {field: 'wechatUserId', hide: true, sort: true, title: 'bwu_id'},
            {field: 'name', sort: true, title: '客户姓名',},
            {field: 'mobile', sort: true, title: '电话号码'},
            {field: 'nickName', sort: true, title: '微信昵称'},
            {field: 'gender', sort: true, title: '性别'},
            {field: 'address', sort: true, title: '地址'},
            {field: 'managerId', sort: true, title: '客户经理'},
            {field: 'remarks', sort: true, title: '备注'},
            {field: 'createTime', sort: true, title: '创建时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Client.search = function () {
        var queryData = {};
        queryData['condition'] = $("#name").val();
        queryData['clientId'] = Client.condition.clientId;
        table.reload(Client.tableId, {where: queryData});
    };

    /**
     * 弹出添加
     */
    Client.openAddClient = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加客户',
            area: ['500px', '300px'],
            content: Feng.ctxPath + '/client/client_add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Client.tableId);
            }
        });
    };

    /**
     * 导出excel按钮
     */
    Client.exportExcel = function () {
        var checkRows = table.checkStatus(Client.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };



    /**
     * 客户浏览记录
     * @param data
     */
    Client.browsersDetail = function (param) {
        if (param.wechatUserId == null) {
            top.layui.admin.open({
                title: '浏览详情',
                //area: ['1400px', '800px'],
                content: '该用户未与微信绑定'
            });
        } else {
            top.layui.admin.open({
                type: 2,
                title: '浏览详情',
                area: ['1400px', '800px'],
                content: Feng.ctxPath + '/browser/browser_detail?wechatUserId=' +  param.wechatUserId,
            });
        }
    };

    /**
     * 点击编辑客户
     *
     * @param data 点击按钮时候的行数据
     */
    Client.onEditClient = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '修改客户',
            area: ['500px', '300px'],
            content: Feng.ctxPath + '/client/client_update?clientId=' + data.clientId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Client.tableId);
            }
        });
    };

    /**
     * 点击删除客户
     *
     * @param data 点击按钮时候的行数据
     */
    Client.onDeleteClient = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/client/delete", function () {
                Feng.success("删除成功!");
                table.reload(Client.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("clientId", data.clientId);
            ajax.start();
        };
        Feng.confirm("是否删除客户 " + data.name + "?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Client.tableId,
        url: Feng.ctxPath + '/client/list',
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Client.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Client.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Client.openAddClient();
    });

    // 导出excel
    $('#btnExp').click(function () {
        Client.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + Client.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            Client.onEditClient(data);
        } else if (layEvent === 'delete') {
            Client.onDeleteClient(data);
        }else if (layEvent === 'detail') {
            Client.clientDetail(data);
        }else if (layEvent === 'browser') {
            Client.browsersDetail(data);
        }
    });
});
