layui.use(['table', 'admin', 'ax', 'laydate'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var laydate = layui.laydate;

    /**
     * 系统管理--活动管理
     */
    var TaskStatistics = {
        tableId: "taskStatisticsTable",
        condition: {
            taskStatisticsId: "",
        }
    };

    /**
     * 初始化表格的列
     */
    TaskStatistics.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'taskStatisticsId', hide: true, sort: true, title: 'id'},
            {field: 'title', sort: true, title: '活动名称'},
            {field: 'totalNumber', sort: true, title: '活动人数'},
            {field: 'contactNumber', sort: true, title: '已联系'},
            {field: 'failNumber', sort: true, title: '联系失败'},
            {field: 'unconnectNumber', sort: true, title: '未联系'},
            {field: 'startTime', sort: true, title: '开始时间'},
            {field: 'endTime', sort: true, title: '结束时间'},
        ]];
    };

    /**
     * 点击查询按钮
     */
    TaskStatistics.search = function () {
        var queryData = {};
        queryData['condition'] = $("#name").val();
        table.reload(TaskStatistics.tableId, {where: queryData});
    };
    
    /**
     * 导出excel按钮
     */
    TaskStatistics.exportExcel = function () {
        var checkRows = table.checkStatus(TaskStatistics.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };
    


    // 渲染表格
    var tableResult = table.render({
        elem: '#' + TaskStatistics.tableId,
        url: Feng.ctxPath + '/taskStatistics/list',
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: TaskStatistics.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        TaskStatistics.search();
    });



    // 导出excel
    $('#btnExp').click(function () {
        TaskStatistics.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + TaskStatistics.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            TaskStatistics.onEditTaskStatistics(data);
        } else if (layEvent === 'delete') {
            TaskStatistics.onDeleteTaskStatistics(data);
        }
    });
});