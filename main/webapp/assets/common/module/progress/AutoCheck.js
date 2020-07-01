(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            window.layui && layui.define ? layui.define(function(exports){exports('AutoCheck',factory(exports))}) :
                (factory((global.AutoCheck = {})));
}(this, (function (exports) {
    'use strict';
    var $ = layui.$;
    var Check = function (checkConfig) {
        this.id = 'autoCheckModal';
        this.checkList = checkConfig['checkList'];
        this.callback = checkConfig['callback'];
        this.curStep = 0;
        this.isAutoRunCallback = checkConfig['isAutoRunCallback'] === 'undefined'?false:checkConfig['isAutoRunCallback'];
        this.timeGap = checkConfig['timeGap'];
    };
    Check.prototype.run = function () {
        if(this.checkList && this.checkList instanceof Array && this.checkList.length > 0){
            this.createHtml();
        }
    };
    Check.prototype.createHtml = function () {
        var modal = '<div id="'+this.id+'">'
            +'<ul class="check-list"></ul>';
        modal +=  '</div>';
        $('.detCon').append(modal);
        var that = this;
        $('#'+that.id).find('.check-list').html('')
        $.each(this.checkList.reverse(), function (i,mes) {
            //初始状态1-check  运行中2-checking  成功8-success  错误9-fail
            var n = Number(mes.status)
            var name = mes.ruleName?mes.ruleName:'--'
            var count = mes.resultCount?mes.resultCount:0
            var li = `<li class="${n===1?'check':(n===2?'check':(n===8?'success':(n===9?'fail':'fail')))}"><span class="check-icon"></span><span class="check-text">${name}</span><span class="check-r">已检测出<b onclick="checkNum('${mes.ruleId}')" class="check-num">${count}</b>条</span></li>`;
            // var li = '<li class="'+(n===1?'check':(n===2?'check':(n===8?'success':(n===9?'fail':'fail'))))+'"><span class="check-icon"></span><span class="check-text">'+name+'</span><span class="check-r">已检测出<b id="'+mes.ruleId+'" class="check-num">'+count+'</b>条</span></li>';
            $('#'+that.id).find('.check-list').append(li);
        });
    };
    Check.prototype.doCheck = function () {
        (function(that){
            var timeGap = 1000;
            if(that.timeGap && parseInt(that.timeGap) === parseInt(that.timeGap)){
                timeGap = that.timeGap;
            }
            function doCheck() {
                var curLi = $('#'+that.id).find('.check-list li').eq(that.curStep);
                curLi.find('.check-icon').html('');
                curLi.find('.check-text').html(that.checkList[that.curStep].ruleName);
                var curNum = curLi.find('.check-num');
                var status= that.checkList[that.curStep].status
                var num= that.checkList[that.curStep].resultCount
                switch (status) {  //初始状态1-check  运行中2-checking  成功8-success  错误9-fail
                    case '1':
                        curLi.attr('class','check');
                        break;
                    case '2':
                        curLi.attr('class','checking');
                        curNum.html(num)
                        break;
                    case '8':
                        curLi.attr('class','success');
                        curNum.html(num)
                        break;
                    case '9':
                        curLi.attr('class','fail');
                        curNum.html(num)
                        break;
                    case '0':
                        curLi.attr('class','fail');
                        curNum.html(num)
                        break;
                }
                that.curStep++;
                if(that.checkList[that.curStep]){
                    var nextLi = $('#'+that.id).find('.check-list li').eq(that.curStep);
                    nextLi.find('.check-text').html(that.checkList[that.curStep].ruleName);
                    nextLi.attr('class','checking');
                }else{
                    that.callback();
                    // if(that.isAutoRunCallback){
                    //     that.callback();
                    //     that.destroy();
                    // }else{
                    //     $('#'+that.id).find('.check-btn').addClass('active').click(function () {
                    //         if(that.callback && typeof that.callback === 'function'){
                    //             that.callback();
                    //             that.destroy();
                    //         }
                    //     });
                    // }
                    // window.clearInterval(doCheck);
                }
            }
            // var doCheck = setInterval(,timeGap);
        })(this);
    };
    Check.prototype.destroy = function () {
        $('#'+this.id).remove();
    };
    return {
        run: function (checkConfig) {
            var check = new Check(checkConfig);
            check.run();
        }
    };

})));