layui.use(['table', 'admin', 'ax', 'laydate'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var laydate = layui.laydate;

    /**
     * 系统管理--活动管理
     */
    var Activity = {
        tableId: "activityTable",
        condition: {
            activityId: "",
        }
    };

    /**
     * 初始化表格的列
     */
    Activity.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'activityId', hide: true, sort: true, title: 'id'},
            {field: 'title', sort: true, title: '活动名称'},
            {field: 'productId', sort: true, title: '所属产品'},
            {field: 'activityDescription', sort: true, title: '活动描述'},
            {field: 'marketingSpeech', sort: true, title: '营销话术'},
            {field: 'forwardTemplate', sort: true, title: '转发模板'},
            {field: 'startTime', sort: true, title: '开始时间'},
            {field: 'endTime', sort: true, title: '结束时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Activity.search = function () {
        var queryData = {};
        queryData['expire'] = $("#expire").val();
        queryData['condition'] = $("#name").val();
        table.reload(Activity.tableId, {where: queryData});
    };

    /**
     * 弹出添加
     */
    Activity.openAddActivity = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加活动',
            area: ['800px', '450px'],
            content: Feng.ctxPath + '/activity/activity_add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Activity.tableId);
            }
        });
    };

    /**
     * 导出excel按钮
     */
    Activity.exportExcel = function () {
        var checkRows = table.checkStatus(Activity.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };


    /**
     * 点击编辑活动
     *
     * @param data 点击按钮时候的行数据
     */
    Activity.onEditActivity = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '修改活动',
            area: ['800px', '450px'],
            content: Feng.ctxPath + '/activity/activity_update?activityId=' + data.activityId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Activity.tableId);
            }
        });
    };

    /**
     * 点击删除活动
     *
     * @param data 点击按钮时候的行数据
     */
    Activity.onDeleteActivity = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/activity/delete", function () {
                Feng.success("删除成功!");
                table.reload(Activity.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("activityId", data.activityId);
            ajax.start();
        };
        Feng.confirm("是否删除活动 " + data.title + "?", operation);
    };

    /*//渲染时间选择框
    laydate.render({
        elem: '#startTime'
    });

    //渲染时间选择框
    laydate.render({
        elem: '#endTime'
    });*/

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Activity.tableId,
        url: Feng.ctxPath + '/activity/list',
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Activity.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Activity.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Activity.openAddActivity();
    });

    // 导出excel
    $('#btnExp').click(function () {
        Activity.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + Activity.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            Activity.onEditActivity(data);
        } else if (layEvent === 'delete') {
            Activity.onDeleteActivity(data);
        }
    });
});
