layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 系统管理--线索管理
     */
    var Browser = {
        tableId: "browserTable",
        condition: {
            id: ""
        }
    };

    /**
     * 初始化表格的列
     */
    Browser.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'id', hide: true, sort: true, title: 'id'},
            {field: 'wechatUserId', hide: true, sort: true, title: 'wechatUserId'},
            {field: 'nickName', sort: true, title: '微信昵称'},
            {field: 'totalScore', sort: true, title: '总分'},
            {field: 'views', sort: true, title: '浏览次数'},
            {field: 'totalMessage', sort: true, title: '留下信息'},
            {field: 'createTime', sort: true, title: '首次浏览'},
            {field: 'updateTime', sort: true, title: '最后浏览'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Browser.search = function () {
        var queryData = {};
        queryData['conditions'] = $("#name").val();
        queryData['id'] = Browser.condition.id;
        table.reload(Browser.tableId, {where: queryData});
    };


    /**
     * 导出excel按钮
     */
    Browser.exportExcel = function () {
        var checkRows = table.checkStatus(Browser.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };

    // 查看浏览详情
    Browser.browserDetail = function (param) {
        //admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '浏览详情',
            area: ['1400px', '800px'],
            content: Feng.ctxPath + '/browser/browser_detail?wechatUserId=' + param.wechatUserId,
        });
    };

    // 添加
    Browser.addClient = function (param) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加客户',
            area: ['500px', '300px'],
            content: Feng.ctxPath + '/browser/browser_add?wechatUserId=' + param.wechatUserId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Browser.tableId);
            }
        });
    };


    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Browser.tableId,
        url: Feng.ctxPath + '/browser/list',
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Browser.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Browser.search();
    });


    // 导出excel
    $('#btnExp').click(function () {
        Browser.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + Browser.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'detail') {
            Browser.browserDetail(data);
        } else if (layEvent === 'add') {
            Browser.addClient(data);
        }
    });
});