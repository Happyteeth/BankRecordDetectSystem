/**
 * 角色详情对话框
 */
var ProductInfoDlg = {
    data: {
        pid: "",
        pName: ""
    }
};

/*function prepare() {
    //第一步是先获取服务器传过来的图文信息值
    var E = window.wangEditor;
    var editor = new E('#info1');
    var content = document.getElementById("content").value;
    //var content = $('#content').val();
    alert("content:" + content)
    //alert("info1" + info1)
    //把图文信息的值通过innerHTML赋值给编辑器
    //document.getElementById("info2").innerHTML=content;
    editor.customConfig.uploadImgShowBase64 = true

    editor.create();
    editor.txt.html(content)
}*/


/*
layui.use(['layer', 'form', 'admin', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;

    // 让当前iframe弹层高度适应
    admin.iframeAuto();

    //获取部门信息
    /!*var ajax = new $ax(Feng.ctxPath + "/product/detail/" + Feng.getUrlParam("productId"));
    var result = ajax.start();
    form.val('productForm', result);
    prepare();*!/

    // 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/product/update", function (data) {
            Feng.success("修改成功！");

            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);

            //关掉对话框
            admin.closeThisDialog();
        }, function (data) {
            Feng.error("修改失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.start();
    });
});*/
