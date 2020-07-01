layui.use(['layer', 'form', 'admin', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;


    // 让当前iframe弹层高度适应
    admin.iframeAuto();

    //获取客户信息
    var ajax = new $ax(Feng.ctxPath + "/client/detail/" + Feng.getUrlParam("clientId"));
    var result = ajax.start();
    var avatarUrl = document.getElementById("avatarUrl");
    avatarUrl.src = result.avatarUrl;
    form.val('clientForm', result);

// 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/client/update", function (data) {
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
})
;