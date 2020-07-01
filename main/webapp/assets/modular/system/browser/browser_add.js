layui.use(['layer', 'form', 'admin', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;

    $(document).ready(function () {
        $.ajax({
            timeout: 3000,
            async: false,
            type: "POST",
            url: "/mgr/managerList",
            dataType: "json",
            data: {

            },
            success: function (data) {
                for (var i = 0; i < data.length; i++) {
                    $("#managerId").append("<option "+"value="+data[i].userId+">" + data[i].name + "</option>");
                }
            }
        });
    });


    // 让当前iframe弹层高度适应
    admin.iframeAuto();

    //获取客户信息
    var ajax = new $ax(Feng.ctxPath + "/client/transfer/" + Feng.getUrlParam("wechatUserId"));
    var result = ajax.start();
    var avatarUrl = document.getElementById("avatarUrl");
    avatarUrl.src = result.avatarUrl;
    form.val('browserForm', result);




    // 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/client/add", function () {
            Feng.success("添加成功！");

            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);

            //关掉对话框
            admin.closeThisDialog();
        }, function (data) {
            Feng.error("添加失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.start();
    });
});