var i1,i2;
function checkSt(){
    i2=null;
    $("#currentTip").text("正在等待完成定位。如果弹窗请允许");
    if((!document.body)||(!document.getElementById(SS))||document.getElementById(SS).value=="")i1=setTimeout(checkSt,1000);
    $("#currentTip").text("正在处理");
    window.______click = function (id) { document.getElementById(id).checked = true; };
    window._____value = function (id, val) { document.getElementById(id).value = val; };
    _____value('SS', "浙江省 杭州市 钱塘区");
    _____value('HQCS', "浙江省 杭州市 钱塘区");
    ______click('KQWD_0');
    ______click('SFWQZBLYYZDWQZ_1');
    ______click('SFWJZGL_0');
    ______click('TNSFZHBZZHTJHBYHB_1');
    ______click('SFJRDH_1');
    ______click('SFWJJGL_0');
    ______click('LQHZJKMQK_0');
    ______click('YSMQSTQK_0');
    ______click('YSMQSTQK_0');
    ______click('JJLXDH_0');
    var _t = window.$.messager.confirm;
    window.$.messager.confirm = function (a, b, c) { c(true); }; m_submit_Event();
    $("#loadingTip").fadeOut();
}
function stopAll(){
    if(i1)clearTimeout(i1);
    if(i2)clearTimeout(i2);
    $("#loadingTip").fadeOut();
}
i2=setTimeout(checkSt,2000);
setTimeout(()=>{$("#loadingTip").fadeIn();$("#currentTip").text("正在等待...");},200);
$("body").append($("<div id='loadingTip' style='background:#00000077;position:fixed;left:0;right:0;top:0;bottom:0;text-align: center;'></div>").html('<svg width="400" height="400" xmlns="http://www.w3.org/2000/svg" style="transform: scale(0.5);"><!-- Created with Method Draw - http://github.com/duopixel/Method-Draw/ --><g class="installPss"><title>theAnim</title><ellipse ry="185" rx="185" id="svg_1" cy="200" cx="200" stroke-width="30" stroke="#ffffff" fill="transparent" /></g><style>.installPss ellipse{animation: rott 1s infinite linear;stroke-dasharray:300,870;transform-origin:center;}.installPss path{stroke-dasharray:500;stroke-dashoffset: 500;}.installPss line{stroke-dasharray:500;stroke-dashoffset: 500;}@keyframes rott{0%{transform: rotate(0deg);}100%{transform: rotate(360deg);}}</style></svg><br><div id="currentTip" style="color:white;text-align: center;"></div><br><a onclick="stopAll()" style="color:#AED6F1;text-align: center;text-decoration: underline;font-weight:bolder;">停止脚本</a>').hide());
