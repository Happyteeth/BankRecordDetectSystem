layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 系统管理--线索管理
     */
    var BrowserDetail = {
        tableId: "browserDetailTable",
        condition: {
            id: ""
        }
    };

    /**
     * 初始化表格的列
     */
    BrowserDetail.initColumn = function () {
        return [[
            {field: 'id', hide: true, sort: true, title: 'id'},
            {field: 'createTime', sort: true, title: '时间',width:175},
            {field: 'browserType', sort: true, title: '类型',width:75},
            {field: 'shareObjectId', sort: true, title: '产品名称',width:275},
            {field: 'name', sort: true, title: '姓名'},
            {field: 'mobile', sort: true, title: '电话'},
            {field: 'message', sort: true, title: '信息',width:450}
        ]];
    };



    /**
     * 导出excel按钮
     */
    BrowserDetail.exportExcel = function () {
        var checkRows = table.checkStatus(BrowserDetail.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };


    // 渲染表格
    var wechatUserId = Feng.getUrlParam("wechatUserId")== null ? 0 :Feng.getUrlParam("wechatUserId");
    var tableResult = table.render({
        elem: '#' + BrowserDetail.tableId,
        url: Feng.ctxPath + '/browser/detail/'+wechatUserId,
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: BrowserDetail.initColumn()
    });

    // 导出excel
    $('#btnExp').click(function () {
        BrowserDetail.exportExcel();
    });
});

