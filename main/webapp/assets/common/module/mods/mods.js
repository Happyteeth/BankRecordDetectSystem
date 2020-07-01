/**
 * 方法封装
 *
 * @author  wangyuan
 * @time  2019-8-13
 */

layui.define(['jquery','element'], function (exports) {
    var $ = layui.jquery;
    element = layui.element;

    var mods = {}

    mods.tabChange = function(value,num,id,options){
        var openTab = layui.sessionData('openTab').openTab; //默认多页签关闭
        $(window.parent.document).find('#sideList li').eq(num).addClass('layui-this').siblings().removeClass('layui-this')
        if(openTab || openTab === undefined){
            var ss = $(window.parent.document).find(`#tabTitleList>li[lay-id='${id}']`)[0]
            var valueList = layui.sessionData('tempData').indexTabs;
            if(ss === undefined){
                valueList.push(value)
                parent.layui.element.tabAdd('admin-pagetabs', {
                    title: options
                    ,content: `<iframe lay-id="${id}" src="${id}" frameborder="0" class="admin-iframe" style="height: 100%;"></iframe>`
                    ,id: id
                })
            }
            layui.sessionData('tempData', {key: 'indexTabs', value:valueList});
            parent.layui.element.tabChange('admin-pagetabs', id);
        }else{
            window.location.href = Feng.ctxPath + id;
        }
        layui.sessionData('tempData', {key: 'tabPosition', value:id});
    }

    /**
     * 表格合并单元格
     * fieldName:table后
     * className:查找所要合并的表格DOM
     */
    mods.rowspan = function(fieldName, index,className) {
        var fixedNode = $(`${className} .layui-table-body`)[index - 1];
        if (!fixedNode) {
            return false;
        }
        var child = fixedNode.getElementsByTagName("td");
        var childFilterArr = [];
        // 获取data-field属性为fieldName的td
        for (var i = 0; i < child.length; i++) {
            if (child[i].getAttribute("data-field") === fieldName) {
                childFilterArr.push(child[i]);
            }
        }
        // 获取td的个数和种类
        var childFilterTextObj = {};
        for (var i = 0; i < childFilterArr.length; i++) {
            var childText = childFilterArr[i].textContent;
            if (childFilterTextObj[childText] === undefined) {
                childFilterTextObj[childText] = 1;
            } else {
                var num = childFilterTextObj[childText];
                childFilterTextObj[childText] = num * 1 + 1;
            }
        }
        // 给获取到的td设置合并单元格属性
        for (var key in childFilterTextObj) {
            var tdNum = childFilterTextObj[key];
            var canRowSpan = true;
            for (var i = 0; i < childFilterArr.length; i++) {
                if (childFilterArr[i].textContent === key) {
                    if (canRowSpan) {
                        childFilterArr[i].setAttribute("rowspan", tdNum);
                        childFilterArr[i].removeAttribute("style")
                        canRowSpan = false;
                    } else {
                        childFilterArr[i].style.display = "none";
                    }
                }
            }
        }
    }

    /**
     * 数字千位分隔符
     */
    mods.format = function(number) {
        return String(number).replace(/(\d)(?=(\d{3})+$)/g, '$1,');
    }

    exports('mods', mods);
})
