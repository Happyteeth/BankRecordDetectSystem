layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 系统管理--产品管理
     */
    var Product = {
        tableId: "productTable",
        condition: {
            productId: ""
        }
    };

    /**
     * 初始化表格的列
     */
    Product.initColumn = function () {
        return [[
            {type: 'checkbox'},
            {field: 'productId', hide: true, sort: true, title: 'id'},
            {field: 'title', sort: true, title: '产品名称'},
            {field: 'introduce', sort: true, title: '产品简介'},
            {field: 'createUser', sort: true, title: '发布者'},
            {field: 'createTime', sort: true, title: '发布时间'},
            {field: 'status', sort: true, title: '备注'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Product.search = function () {
        var queryData = {};
        queryData['condition'] = $("#name").val();
        queryData['productId'] = Product.condition.productId;
        table.reload(Product.tableId, {where: queryData});
    };

    /**
     * 弹出添加
     */
    Product.openAddProduct = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            area:["1500px","1000px"],
            title: '添加产品',
            content: Feng.ctxPath + '/product/product_add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Product.tableId);
            }
        });
    };

    /**
     * 导出excel按钮
     */
    Product.exportExcel = function () {
        var checkRows = table.checkStatus(Product.tableId);
        if (checkRows.data.length === 0) {
            Feng.error("请选择要导出的数据");
        } else {
            table.exportFile(tableResult.config.id, checkRows.data, 'xls');
        }
    };

    /**
     * 点击编辑产品
     *
     * @param data 点击按钮时候的行数据
     */
    Product.onEditProduct = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            area:["1500px","1000px"],
            title: '修改产品',
            content: Feng.ctxPath + '/product/product_update?productId=' + data.productId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Product.tableId);
            }
        });
    };

    /**
     * 点击删除产品
     *
     * @param data 点击按钮时候的行数据
     */
    Product.onDeleteProduct = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/product/delete", function () {
                Feng.success("删除成功!");
                table.reload(Product.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("productId", data.productId);
            ajax.start();
        };
        Feng.confirm("是否删除产品 " + data.title + "?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Product.tableId,
        url: Feng.ctxPath + '/product/list',
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Product.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Product.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
    	Product.openAddProduct();
    });

    // 导出excel
    $('#btnExp').click(function () {
    	Product.exportExcel();
    });

    // 工具条点击事件
    table.on('tool(' + Product.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
        	Product.onEditProduct(data);
        } else if (layEvent === 'delete') {
        	Product.onDeleteProduct(data);
        }
    });
});
