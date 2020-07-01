layui.use(['laydate', 'form', 'admin', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var laydate = layui.laydate;


    laydate.render({
        elem: '#communicationTime'
    });

    // 让当前iframe弹层高度适应
    admin.iframeAuto();



    //获取沟通纪要信息
    var ajax = new $ax(Feng.ctxPath + "/communication/detail/" + Feng.getUrlParam("communicationId"));
    var result = ajax.start();
    form.val('communicationForm', result);

    // 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/communication/update", function (data) {
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
});