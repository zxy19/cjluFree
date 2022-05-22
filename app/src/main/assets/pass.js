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
"#bglayer-card{background: linear-gradient(45deg, black 4%,transparent 63%);position: absolute;left: 0px;top: 0px;right: 0;height: 150px;z-index: 2;border-radius: 11px;}" +
".card .bgimg{z-index:0;position: absolute;top: 0;left: 0;}"+
".card .bgimg.flayer{z-index:1;height:auto!important;}"+
".card div{z-index:3;}"+
"#qrcode{display:none;}"+
".qrcodebox div{background:transparent!important;transform:scale({{T:zoom}}) translateY(30px);}"+
".userinfo{top:13px!important;left:17px!important;width:calc(100% - 17px);}"+
".username{top:73px!important;left:17px!important;}"+
".userdpe{top:102px!important;left:17px!important;color:lightgray;font-size: 14px!important;}"+
".userbj{top:123px!important;left:17px!important;color:lightgray;font-size: 14px!important;}"+
"#qrCtr{width: 60px;position: absolute;left: calc(50% - 30px);padding: 0;top: calc(50% - 30px);border-radius: 20px;}"+
".loading .qr_imgs{opacity:0;} .qr_imgs{opacity:1;}"+
".vaccines{position: absolute;width: 100%;left: 0;bottom: 99px;background-color: #90ee9045;}"+
".qrcodebox.vaccinescomplete{background-color:none!important;}"
;
var useGoldCode="{{T:goldCode}}";
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
    var tmpImg="{{T:im2}}";
    if(tmpImg!=""){
        $(".bgimg").hide();
        $(".card").append($("<img id='card_bg_over' class='bgimg flayer'>").attr("src",tmpImg));
    }
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
    var vanc = (ymjzqk == '已完成');
    var canvas=document.createElement("canvas");
    canvas.style.display="none";
    canvas.setAttribute("height","721");
    canvas.setAttribute("width","721");
    document.body.append(canvas);
    var ctx=canvas.getContext('2d');
    ctx.drawImage(im,0,0);
    var imgData=ctx.getImageData(0,0,721,721);
    for (var i=0;i<imgData.data.length;i+=4)
    {
      if(imgData.data[i+2]>100||imgData.data[i+1]<100||imgData.data[i]>100)
      imgData.data[i+3]=0;
      if(vanc && useGoldCode=="true"){
        imgData.data[i+2]=0;
        imgData.data[i+1]=157;
        imgData.data[i]=207;
      }
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