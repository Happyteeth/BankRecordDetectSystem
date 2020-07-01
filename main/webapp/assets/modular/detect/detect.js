layui.use(['table', 'admin', 'ax','element','AutoCheck','mods'], function () {
    var aa=''
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax
    element = layui.element, //Tab的切换功能，切换事件监听等，需要依赖element模块
        AutoCheck = layui.AutoCheck;
    var admin = layui.admin;
    var mods = layui.mods;

    var detect = {
        checkConfig:{
            // finalBtnText: '确定',
            // isShowFinalBtn: true,
            callback: function () {
                // clearInterval(detect.time);
            },
            isAutoRunCallback: false,
            checkList:[],
            timeGap: 1000
        },
        progress:0,
        time:1000,
        autoCheckId:'',
        num:'',
    }

    detect.start = function () {
        //var index = layer.load(2);
        setTimeout(function () {
            $.ajax({
                type: 'GET',
                url: Feng.ctxPath + '/detect/start',
                async:false,
                success: function (res) {
                    //layer.close(index);
                    if(res.code == 200){
                        // $('#detectCon').css({"display":'block'})
                        // $('#detect').css({"display":'none'})
                        // $('.proNum').html(detect.progress+'%')
                        // element.progress('demo', detect.progress+'%');
                        // detect.autoCheckId = Number(res)
                        // detect.config(detect.autoCheckId)
                        layui.sessionData('autoCheckId', {key: 'autoCheckId', value: res.data});
                        layui.sessionData('checkType', {key: 'checkType', value: 0});
                        window.location.href = Feng.ctxPath + "/detect/autoCheckResult";
                    }else {
                        layer.msg(res.message, {icon: 7, time: 2000});
                    }
                },
                error: function (e) {
                    console.log(e.status);
                    console.log(e.responseText);
                }
            });
        },500)

    }

    detect.config = function (id) {
        detect.time =setInterval(function () {
            $.ajax({
                type: 'POST',
                url: Feng.ctxPath + '/detect/checkResult',
                async:false,
                data:{autoCheckId:id},
                success: function (res) {
                    if (res) {
                        detect.progress = res.checkProgress;
                        detect.checkConfig.checkList = res.tree;
                        $('.proNum').html(detect.progress+'%')
                        element.progress('demo', detect.progress+'%');
                        if(detect.progress === 100){
                            clearInterval(detect.time);
                            $('#checkBtn').css({'display':'inline-block'});
                            $('#calcBtn').css({'display':'none'});
                            $('.prob').html('检测已完成');
                            detect.autoCheckId = res.autoCheckId
                        }
                        // AutoCheck.run(detect.checkConfig);
                        createHtml(detect.checkConfig);
                        var n = 34,m=0,length=0;
                        $.each(detect.checkConfig.checkList, function (i, d) {
                            if(d.status === 2){
                                length = n*i
                            }
                        });
                        $('.det-body').scrollTop(length)
                    }else{
                        clearInterval(detect.time);
                    }

                },
                error: function (e) {
                    console.log(e.status);
                    console.log(e.responseText);
                }
            });
        },detect.time)
    }

    detect.upData = function () { //查看历史
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/detect/getUpData',
            success: function (res) {
                console.log(res)
                if(res.autoCheckId !==''){
                    layui.sessionData('autoCheckId', {key: 'autoCheckId', value: res.autoCheckId});
                    var html = `<span >上次检测时间：${res.dInsert}</span>`
                    $('.t-2').append(html)
                    detect.autoCheckId=res.autoCheckId
                }
            },
            error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });

    }


    // 查看历史检测
    $(document).on('click', "#upBtn", function (autoCheckId) {
        layui.sessionData('checkType', {key: 'checkType', value: 1});
        if(detect.autoCheckId === ''){
            layer.msg('暂无历史检测记录', {icon: 7, time: 2000});
        }else{
            window.location.href = Feng.ctxPath + "/detect/autoCheckResult?autoCheckId="+detect.autoCheckId; //跳转检测结果页面
        }
    });


    // 开始检测
    $('#detectBtn').click(function () {
        layui.sessionData('checkType', {key: 'checkType', value: true});
        layui.sessionData('result', {key: 'result',remove: true});
        detect.start();
    });

    $(document).ready(function () {
        //获取用户详情
        var ajax = new $ax(Feng.ctxPath + "/system/currentUserInfo",function (res) {
            if(res.success){
                layui.sessionData('userInfo', {key: 'userInfo', value:res.data}); //存储查看检测结果判断字符
                detect.edition = layui.sessionData('userInfo').userInfo.edition;
            }
        });
        ajax.start();
        // var num = layui.sessionData('detect');
        // var id = layui.sessionData('autoCheckId');
        detect.upData()
        // if(num.detect === '1'){
        //     $('#detectCon').css({"display":'block'})
        //     $('#detect').css({"display":'none'})
        //     $('.proNum').html(detect.progress+'%')
        //     element.progress('demo', detect.progress+'%');
        //     detect.config(id.autoCheckId)
        // }
    });

    // 取消检测
    $('#calcBtn').click(function () {
        $('#detectCon').css({"display":'none'})
        $('#detect').css({"display":'block'}).removeClass("layui-btn-disabled");
        clearInterval(detect.time);
    });

    //查看报告
    $('#checkBtn').click(function () {
        layui.sessionData('autoCheckId', {key: 'autoCheckId', value: detect.autoCheckId});
        layui.sessionData('idList', {key: 'idList',remove: true});
    });

    //跳转文件导入
    $('#importBtn').click(function () {
        mods.tabChange({"menuId":"/file","menuPath":"/file","menuName":"数据导入"},1,'/file','数据导入')
    });

    createHtml = function(data){
        // var modal = `<ul class="check-list"></ul>`;
        // $('.detCon').append(modal);
        $('.check-list').html('')
        var ful = ''
        $.each(data.checkList, function (i,f) {
            var sul = ''
            $.each(f.children, function (i,s) {
                var tli = ''
                $.each(s.children.reverse(), function (i,mes) {
                    //初始状态1-check  运行中2-checking  成功8-success  错误9-fail
                    var n = Number(mes.status)
                    var name = mes.title?mes.title:'--'
                    var count = mes.resultCount?mes.resultCount:0
                    var num = count>0?'check-num':''
                    tli += `<li class="${n===1?'check':(n===2?'check':(n===8?'success':(n===9?'fail':'fail')))}"><span class="check-icon"></span><span class="check-text">${name}</span><span onclick="checkNum('${mes.id}')" class="check-r" title="查看报告">违规记录<b class="${num}">${count}</b>条</span></li>`;
                });
                sul += `<li><p>${s.title}</p><ul class="f-list">${tli}</ul></li>`
            })
            ful += `<li><p>${f.title}</p><ul class="f-list">${sul}</ul></li>`
        })
        $('.check-list').append(ful);

        // var modal = `<div id="autoCheckModal"><ul class="check-list"></ul></div>`;
        // $('.detCon').append(modal);
        // $('#autoCheckModal').find('.check-list').html('')
        // $.each(data.checkList.reverse(), function (i,mes) {
        //     //初始状态1-check  运行中2-checking  成功8-success  错误9-fail
        //     var n = Number(mes.status)
        //     var name = mes.ruleName?mes.ruleName:'--'
        //     var count = mes.resultCount?mes.resultCount:0
        //     var num = count>0?'check-num':''
        //     var li = `<li class="${n===1?'check':(n===2?'check':(n===8?'success':(n===9?'fail':'fail')))}"><span class="check-icon"></span><span class="check-text">${name}</span><span onclick="checkNum('${mes.ruleId}')" class="check-r" title="查看报告">已检测出<b class="${num}">${count}</b>条不合规记录</span></li>`;
        //     $('#autoCheckModal').find('.check-list').append(li);
        // });
    }

    checkNum = function (id) {
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/mod/modelselect',
            async:false,
            data:{ruleId:id},
            success: function (res) {
                console.log(res)
                if(res){
                    window.location.href = Feng.ctxPath+"/detect/report";
                    layui.sessionData('autoCheckId', {key: 'autoCheckId', value: detect.autoCheckId});
                    layui.sessionData('idList', {key: 'idList', value:{fid:res.modelType1ItemId?res.modelType1ItemId:'',sid:res.modelType2ItemId?res.modelType2ItemId:'',tid:id}});
                }
            },
            error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });
    }

    // 数据导入
    // $('#importBtn').click(function () {
    //     console.log(admin);
    //     layui.sessionData('file', {key: 'file', value: '/file'});
    // })

    // $('.det-body').bind('scroll',function () {
    //     var top = $(this).scrollTop();
    //     console.log(top)
    // })

});
