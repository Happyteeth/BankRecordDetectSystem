layui.use(['table', 'admin', 'ax','element','AutoCheck'], function () {
    var aa=''
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax
    element = layui.element, //Tab的切换功能，切换事件监听等，需要依赖element模块
        AutoCheck = layui.AutoCheck;
    var admin = layui.admin;

    var result = {
        checkConfig:{
            callback: function () {
                // clearInterval(detect.time);
            },
            isAutoRunCallback: false,
            checkList:[],
            timeGap: 1000
        },
        time:3000,
        progress:0,
        autoCheckId:'',
        num:''
    }


    result.start = function () {
        // var id= router.search.autoCheckId;
        // console.log(id);
        var index = layer.load(2);
        setTimeout(function () {
            $.ajax({
                type: 'GET',
                url: Feng.ctxPath + '/detect/start',
                async:false,
                success: function (res) {
                    layer.close(index);
                    if(res){
                        result.autoCheckId = res
                        layui.sessionData('autoCheckId', {key: 'autoCheckId', value: res});
                       /*if(result.edition === 'pay'){
                        result.getInfo(result.autoCheckId,'/detect/checkResultPay');
                        }else{*/
                         result.getInfo(result.autoCheckId,'/detect/checkResult');
                      /*  }*/
                    }else{
                        layer.msg('无法检测哦！', {icon: 7, time: 2000});
                    }
                },
                error: function (e) {
                    console.log(e.status);
                    console.log(e.responseText);
                }
            });
        },500)

    }

    result.getInfo = function (id,url) {
        result.time =setInterval(function () {
            $.ajax({
                type: 'POST',
                url: Feng.ctxPath + url,
                async:false,
                data:{autoCheckId:id},
                success: function (res) {
                    if (res) {
                        console.log(result.checkConfig);
                        result.checkConfig.checkList = res.tree;
                        $('.prob').html(`<span>共${res.ruleCnt || 0}个检查项，已检查${res.checkEndNum || 0}个</span>  <span>开始时间: ${res.dInsert || '----'}</span>`);
                        $('.proNum').html(`${res.checkProgress || 0}${res.checkProgress === 0?'':'%'}`)
                        element.progress('demo', `${res.checkProgress || 0}${res.checkProgress === 0?'':'%'}`);
                        $('.proImg').attr('src','../../../assets/common/images/checking.gif')
                        if(result.type === 1){
                            clearInterval(result.time);
                            $('#checkBtn,#reBtn').css({'display':'inline-block'});
                            $('#calcBtn').css({'display':'none'});
                            $('.proImg').attr('src','../../../assets/common/images/Bitmap.png')
                        }
                        if(res.checkProgress === 100){
                            result.progress = 100;
                            clearInterval(result.time);
                            $('#checkBtn,#reBtn').css({'display':'inline-block'});
                            $('#calcBtn').css({'display':'none'});
                            $('.proImg').attr('src','../../../assets/common/images/Bitmap.png')
                            result.autoCheckId = res.autoCheckId
                        }
                        createHtml(result.checkConfig);
                        var n = 34,m=0,length=0;
                        $.each(result.checkConfig.checkList, function (i, d) {
                            if(d.status === 2){
                                length = n*i
                            }
                        });
                        $('.det-body').scrollTop(length)
                    }else{
                        clearInterval(result.time);
                    }

                },
                error: function (e) {
                    // console.log(e.status);
                    // console.log(e.responseText);
                }
            });
        },result.time)
    }

    $(document).ready(function () {
        //获取用户详情
        var ajax = new $ax(Feng.ctxPath + "/system/currentUserInfo",function (res) {
            if(res.success){
                layui.sessionData('userInfo', {key: 'userInfo',
                    value:res.data}); //存储查看检测结果判断字符
                    result.edition = layui.sessionData('userInfo').userInfo.edition;
            }
        });
        ajax.start();
        $('.proNum').html('')
        element.progress('demo', '');
        $('.proImg').attr('src','../../../assets/common/images/Bitmap.png')
        result.num = layui.sessionData('result').result; //获取查看检测结果判断字符
        result.type = layui.sessionData('checkType').checkType; //查看类型 0一键检测 1历史查看 else登录后直接跳转页面显示后台获取id
        if(result.type === 0 || result.num === 1){
            result.autoCheckId = layui.sessionData('autoCheckId').autoCheckId;
        }
        else if(result.type === 1){
            result.autoCheckId = layui.sessionData('autoCheckId').autoCheckId;
            $('#checkBtn,#reBtn').css({'display':'inline-block'});
            $('#calcBtn').css({'display':'none'});
        }
        else{
            result.autoCheckId = $('#autoCheckId').val()
        }
        console.log(result.autoCheckId)
        if(result.autoCheckId){
            console.log('检测结果')
       /* if(result.edition === 'pay'){
            result.getInfo(result.autoCheckId,'/detect/checkResultPay');
            }else{*/
             result.getInfo(result.autoCheckId,'/detect/checkResult');
            /*}*/

        }
        layui.sessionData('autoCheckId', {key: 'autoCheckId', value: result.autoCheckId});
    });

    // 重新检测
    $('#reBtn').click(function () {
      /*  layer.confirm('确认重新检测?',  {icon: 3, title:'提示'},function(index){*/
            /*if(result.type === 0){
              //  layui.sessionData('result', {key: 'result',remove: true});
                clearInterval(result.time);
                result.num = '';
                result.progress = 0;
              // $('.proNum').html('0%')
                //element.progress('demo', '0%');
               // $('.prob').html('正在检测中，请耐心等待<span class="dot-ani"></span>');
                window.location.href = Feng.ctxPath + "/detect";
            }
            else if(result.type === 1){
                window.location.href = Feng.ctxPath + "/detect";
            }*/
           window.location.href = Feng.ctxPath+"/detect/detec";
            // layer.close(index);
       /* });*/

    });

    // 取消检测
    $('#calcBtn').click(function () {
        clearInterval(result.time);
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/detect/updateStart',
            // async:false,
            data:{autoCheckId: result.autoCheckId},
            success: function (res) {
                console.log(res)
                console.log('取消检测')
             if(res.success){
              window.location.href = Feng.ctxPath+"/detect/detec";
            }else{
               layer.msg( '通讯异常，无法取消', {icon: 5, time: 2000});
         }
         },
          error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });
    });

    //查看报告
    $('#checkBtn').click(function () {
         var index = layer.load(2);
        $('#checkTab').css({'display': 'inline-block'});
        $(".tab-header>li").eq(1).addClass('layui-this').siblings().removeClass('layui-this');
        $(".tab-content>div").eq(1).addClass('layui-show').siblings().removeClass('layui-show');
        layui.sessionData('autoCheckId', {key: 'autoCheckId', value: result.autoCheckId});
        layui.sessionData('idList', {key: 'idList',remove: true});
        document.getElementById("checkframe").src="/detect/report";
        layer.close(index);
    });

    closeDialog = function (e) {
        e.stopPropagation()
        $(".tab-header>li").eq(0).addClass('layui-this').siblings().removeClass('layui-this');
        $(".tab-content>div").eq(0).addClass('layui-show').siblings().removeClass('layui-show');
        $('#checkTab').css({'display': 'none'});
        layui.sessionData('autoCheckId', {key: 'autoCheckId',remove: true});
        return false;
    }






    createHtml = function(data){
        console.log('创建树');
        console.log(data);
        console.log(result.edition);
        // var modal = `<ul class="check-list"></ul>`;
        // $('.detCon').append(modal);
        $('.check-list').html('')
        var ful = ''
        $.each(data.checkList, function (i, f) {
            var sul = '',sli = '';
            $.each(f.children, function (i, s) {
                var chd = s.children && s.children.length > 0;
                if(chd){
                    var tli = ''
                    $.each(s.children, function (i, mes) {
                        //初始状态1-check  运行中2、0、7-checking  成功8-success  错误9-fail 取消A-cancel
                        var n = mes.status==='A'?'A':Number(mes.status);
                        var name = mes.title ? mes.title : '--'
                        var count = mes.resultCount ? mes.resultCount : 0
                        var num = count > 0 ? 'check-num' : '';
                        tli += `<li class="${n === 1 ? 'check' : (n === 2 || n === 0 || n === 7 ? 'check' : (n === 8 ? 'success' : (n === 9 ? 'fail' : (n === 'A' ? 'cancel' : 'check'))))}">
                                <span class="check-icon ${(n === 1 || n === 2 || n === 0 || n === 7) ? 'layui-anim layui-anim-rotate layui-anim-loop' : ''}"></span><span class="check-text">${name}</span>
                                <span onclick="checkNum('${mes.id}','${mes.status}')" class="check-r" title="查看报告">违规记录<b class="${num}">${count}</b>条</span>
                            </li>`;
                    });
                    sul += `<li><p>${s.title}</p><ul class="s-list">${tli}</ul></li>`
                }else{
                    //初始状态1-check  运行中2、0-checking  成功8-success  错误9-fail 取消A-cancel
                    var n = s.status==='A'?'A':Number(s.status);
                    var name = s.title ? s.title : '--'
                    var count = s.resultCount ? s.resultCount : 0
                    var num = count > 0 ? 'check-num' : '';
                    sli += `<li class="${n === 1 ? 'check' : (n === 2 || n === 0 || n === 7? 'check' : (n === 8 ? 'success' : (n === 9 ? 'fail' :  (n === 'A' ? 'cancel' : 'check'))))}">
                                <span class="check-icon ${(n === 1 || n === 2 || n === 0 || n === 7) ? 'layui-anim layui-anim-rotate layui-anim-loop' : ''}"></span><span class="check-text">${name}</span>
                                <span onclick="checkNum('${s.id}','${s.status}')" class="check-r" title="查看报告">违规记录<b class="${num}">${count}</b>条</span>
                            </li>`;
                }
            })
            ful += `<li><p>${f.title}</p><ul class="f-list ${sli===''?'s-li':'f-li'}">${sli===''?sul:sli}</ul></li>`
        })
        $('.check-list').append(ful);
    }

    checkNum = function (id,n) {
        //初始状态1-check  运行中2-checking  成功8-success  错误9-fail
        if(n !== '8'){
            layer.msg('暂时无法查看，请稍后再试！', {icon: 7, time: 2000});
            return false
        }
        $.ajax({
            type: 'GET',
            url: Feng.ctxPath + '/mod/modelselect',
            async:false,
            data:{ruleId:id},
            success: function (res) {
                console.log(res)
                if(res){
                    //window.location.href = Feng.ctxPath+"/detect/report";

                    layui.sessionData('autoCheckId', {key: 'autoCheckId', value: result.autoCheckId});
                    layui.sessionData('idList', {key: 'idList', value:{fid:res.modelType1ItemId?res.modelType1ItemId:'',sid:res.modelType2ItemId?res.modelType2ItemId:'',tid:id}});
                    document.getElementById("checkframe").src="/detect/report";
                    $('#checkTab').css({'display': 'inline-block'});
                    $(".tab-header>li").eq(1).addClass('layui-this').siblings().removeClass('layui-this');
                    $(".tab-content>div").eq(1).addClass('layui-show').siblings().removeClass('layui-show');
                }
            },
            error: function (e) {
                console.log(e.status);
                console.log(e.responseText);
            }
        });
    }

});
