var i1,i2;var selfLocateSelecting=false;
function checkSt(){
    var autoLocation=("{{T:autoLocation}}"=="true");
    i2=null;
    $("#currentTip").text("正在等待完成定位。如果弹窗请允许");
    var tmLocate="{{T:forceLocate}}";
    if(tmLocate!="" && tmLocate.indexOf("forceLocate")==-1){
        window._____value = function (id, val) { document.getElementById(id).value = val; };
        _____value('SS', tmLocate);
        _____value('HQCS', tmLocate);
    }
    if((!document.body)||(!document.getElementById("SS"))||document.getElementById("SS").value=="")return i1=setTimeout(checkSt,1000);
    window.______click = function (id) { document.getElementById(id).checked = true; };
    if(selfLocateSelecting){
        $("#loadingTip").fadeIn();
    }
    $("#currentTip").text("正在处理");
    ______click('KQWD_0');
    ______click('SFWQZBLYYZDWQZ_1');
    ______click('SFWJZGL_0');
    ______click('TNSFZHBZZHTJHBYHB_1');
    ______click('SFJRDH_1');
    ______click('SFWJJGL_0');
    ______click('LQHZJKMQK_0');
    ______click('YSMQSTQK_0');
    ______click('YSMQSTQK_0');
    if(/浙江省.*杭州市.*钱塘区.*/.test(document.getElementById("SS").value) || !(autoLocation))
        ______click('JJLXDH_0');
    else
        ______click('JJLXDH_1');
    var _t = window.$.messager.confirm;
    $("#currentTip").text("准备提交");
    window.$.messager.confirm = function (a, b, c) { setTimeout(function(){
    c(true);$("#currentTip").text("提交完成");$("#loadingTip").fadeOut();
    var s = document.createElement("script");s.setAttribute("src","http://cjlufree.xypp.cc/record/");document.getElementsByTagName("head")[0].appendChild(s);
    },500); };
    m_submit_Event();
}
function locateSelf(){
    $("#loadingTip").fadeOut();
    selfLocateSelecting = true;
}
function stopAll(){
    if(i1)clearTimeout(i1);
    if(i2)clearTimeout(i2);
    $("#loadingTip").fadeOut();
}
function init__(){
    if(!document.body)return setTimeout(init__,400);
    $("body").append($("<div id='loadingTip' style='background:#00000077;position:fixed;left:0;right:0;top:0;bottom:0;text-align: center;z-index:10000;'></div>").html('<svg width="400" height="400" xmlns="http://www.w3.org/2000/svg" style="transform: scale(0.5);"><!-- Created with Method Draw - http://github.com/duopixel/Method-Draw/ --><g class="installPss"><title>theAnim</title><ellipse ry="185" rx="185" id="svg_1" cy="200" cx="200" stroke-width="30" stroke="#ffffff" fill="transparent" /></g><style>.installPss ellipse{animation: rott 1s infinite linear;stroke-dasharray:300,870;transform-origin:center;}.installPss path{stroke-dasharray:500;stroke-dashoffset: 500;}.installPss line{stroke-dasharray:500;stroke-dashoffset: 500;}@keyframes rott{0%{transform: rotate(0deg);}100%{transform: rotate(360deg);}}</style></svg><br><div id="currentTip" style="color:white;text-align: center;"></div><br><a onclick="stopAll()" style="color:#AED6F1;text-align: center;text-decoration: underline;font-weight:bolder;">停止脚本</a><br><br><a onclick="locateSelf()" style="color:#AED6F1;text-align: center;text-decoration: underline;font-weight:bolder;">手动定位</a>').hide());
    i2=setTimeout(checkSt,2000);
    setTimeout(()=>{$("#loadingTip").fadeIn();$("#currentTip").text("正在等待...");},200);
}
$(document).ready(init__);