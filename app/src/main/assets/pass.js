var cssContent=
"#bglayer{background-image:url({{T:im1}})!important;background-size: cover;background-position: center;position:fixed;left:0;top:0;right:0;bottom:0;z-index:-1;}" +
"body{background:tranparent!important;}" +
".combg{background:#ffffffa3!important;}" +
".passcode{height:500px!important;position:relative;}" +
"#notice{position:absolute;bottom: 15px;left: 0;right: 0;}" +
".passstatus{right: -20px!important;top: -19px;}" +
".card{box-shadow: grey 1px 1px 5px;border-radius:11px;overflow:hidden;position:relative;transfrom:translateY(-160px);}" +
".inited #card{transfrom:translateY(0);}"+
"*{transition:all 0.7s;}" +
"#rq{display: inline-block;padding-right: 12px;color:lightgray;}" +
"#sj{font-size: 30px;display: inline-block;color:gray;}" +
"#bglayer-card{background: linear-gradient(45deg, black 4%,transparent 63%);position: absolute;left: 0px;top: 0px;right: 0;height: 150px;z-index: 0;border-radius: 11px;}" +
".card div{z-index:1;}"+
"#qrcode{display:none;}"+
".qrcodebox div{background:transparent!important;}"+
".userinfo{top:13px!important;left:17px!important;width:calc(100% - 17px);}"+
".username{top:73px!important;left:17px!important;}"+
".userdpe{top:102px!important;left:17px!important;color:lightgray;font-size: 14px!important;}"+
".userbj{top:123px!important;left:17px!important;color:lightgray;font-size: 14px!important;}"+
"#qrCtr{width: 60px;position: absolute;left: calc(50% - 30px);padding: 0;top: 128px;border-radius: 20px;}"+
".loading .qr_imgs{opacity:0;} .qr_imgs{opacity:1;}"
;
function do_body(){
    if(!document.body)return setTimeout(do_body,100);
    document.body.classList.add("loading");
    document.body.classList.add("inited");
    document.body.appendChild(function () {
        var cs = document.createElement("style");
        cs.innerHTML = cssContent;
        return cs;
    }());
    document.body.appendChild(function () {
        var t = document.createElement("div");
        t.id = "bglayer";
        return t;
    }());
}
function do_card(){
    if(document.getElementsByClassName("card").length==0)return setTimeout(do_card,100);
    $(".card")[0].appendChild(function () {
        var t = document.createElement("div");
        t.id = "bglayer-card";
        return t;
    }());
    $(".card .bgimg").attr("src", "{{T:im2}}");
    $(".qrcodebox div").append($("<img id='qrReCalc' class='qr_imgs'>"));
    $(".qrcodebox div").append($("<img id='qrCtr' class='qr_imgs'>").attr("src","{{T:im3}}"));
    var of=document.getElementById("qrcode").setAttribute;
    document.getElementById("qrcode").setAttribute=function(...a){
        console.log("qr");
        of.apply(this,a);
        document.getElementById("qrcode").onload=()=>
        dealPic(document.getElementById("qrcode"));
    }
}

function dealPic(im){
    var canvas=document.createElement("canvas");
    canvas.style.display="none";
    canvas.setAttribute("height","721");
    canvas.setAttribute("width","721");
    document.body.append("canvas");
    var ctx=canvas.getContext('2d');
    ctx.drawImage(im,0,0);
    var imgData=ctx.getImageData(0,0,721,721);
    for (var i=0;i<imgData.data.length;i+=4)
    {
      if(imgData.data[i+2]>100||imgData.data[i+1]<100||imgData.data[i]>100)
      imgData.data[i+3]=0;
    }
    ctx.putImageData(imgData,0,0);
    document.getElementById("qrReCalc").setAttribute("src",canvas.toDataURL());
    global_finish();
}
function global_finish(){
    document.body.classList.remove("loading");
}
if(!window.___plugin_injected){
    window.___plugin_injected=true;
    do_body();
    do_card();
}else console.log("ignored");