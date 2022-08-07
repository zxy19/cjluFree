package cc.xypp.cjluFree;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private dataUtil data;
    private final HashSet<String> classNameSet = new HashSet<>();
    private boolean enableLog = false;
    private void XVdLog(String flg, String content) {
        Log.i("[AS_LOG_NORMAL]" + flg, content);
        if(enableLog){
            StringBuilder sb=new StringBuilder(data.get("logs"));
            if(sb.length()>10000){
                sb.delete(0,1000);
            }
            sb.append(flg).append("|").append(content).append("\r\n");
            data.set("logs",sb.toString());
        }
    }
    Activity activity;
    Context ctx;
    private boolean hasInj = false;
    private boolean started = false;
    private boolean autoed = false;
    private boolean toExit = false;
    private String getPassScript(){
        String res=data.get("pass_src");

        return res
                .replace("{{T:im1}}",data.get("im1").replace("\n",""))
                .replace("{{T:im2}}",data.get("im2").replace("\n",""))
                .replace("{{T:im3}}",data.get("im3").replace("\n",""))
                .replace("{{T:zoom}}",data.get("auto_zoom").equals("true")?"1.5":"1.0")
                .replace("{{T:goldCode}}",data.get("auto_gold"));
    }
    private String getSigScript(){
        String res=data.get("sig_src"),tmp = "";
        if(data.get("forceLocate").equals("true") && !data.get("autoLocation").equals("true")){
            tmp = data.get("tmLocate");
        }
        return res.replace("{{T:autoLocation}}",data.get("autoLocation")).replace("{{T:forceLocate}}",tmp);
    }
    /**
     * 处理Xposed注入事件
     *
     * @param lpparam
     * @throws Throwable
     */
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.wework")) return;
        if (lpparam.isFirstApplication) {
            if (hookCheck(lpparam.packageName)) {
                try {
                    hasInj = false;
                    started = false;
                    autoed= false;
                    toExit = false;
                    data=null;
                    this.hookSystemWebView(lpparam);
                    this.hookAutomation(lpparam);
                } catch (Exception e) {
                    XVdLog("初始化注入", e.toString() + "|" + Arrays.toString(e.getStackTrace()));
                }
            }
        }
    }

    /**
     * 自动化方法注入
     *
     * @param lpparam
     */
    private void hookAutomation(final XC_LoadPackage.LoadPackageParam lpparam) {
        XVdLog("初始化注入", "系统函数hook");
        AppHook_Context(lpparam);
        AppHook_functions(lpparam);
        AppHook_classLoader(lpparam);
    }
    private void updateCrs(Context ctx){
        if (data != null) return;
        try {
            Context applicationContext = ctx;
            if (applicationContext == null) return;
            ctx=applicationContext;
            XVdLog("取得Context", applicationContext.toString());
            ContentResolver rsv = applicationContext.getContentResolver();
            if (rsv == null) return;
            data = new dataUtil(rsv);
            enableLog=data.get("enable_log").equals("true");
            XVdLog("取得Data", data.get("auto") + "-" + data.get("inj") + "-" + data.get("once"));
        } catch (Exception e) {
            XVdLog("取Context错误", e.toString());
        }
    }
    /**
     * 全局上下文HOOK
     *
     * @param lpparam 参数
     */
    private void AppHook_Context(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    updateCrs((Context) param.getResult());
                }
            });
        } catch (Throwable t) {
            XVdLog("取Context错误", t.toString());
        }
    }

    /**
     * 关键方法HOOK
     *
     * @param lpparam 参数
     */
    private void AppHook_functions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.launch.WwMainActivity", lpparam.classLoader);
            if (tmp != null) {
                XVdLog("方法HOOK", "WwMainActivity");
                XposedHelpers.findAndHookMethod(tmp, "onStart", new WmMainHook());
            }
        } catch (Exception e) {
            XVdLog("HOOK方法错误:WwMainActivity", e.toString());
        }

        try {
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.common.web.JsWebActivity", lpparam.classLoader);
            if (tmp != null) {
                XVdLog("方法HOOK", "JsWebActivity");
                XposedHelpers.findAndHookMethod(tmp, "onStart", new JSStartHook());
            }
        } catch (Exception e) {
            XVdLog("HOOK方法错误:JsWebActivity", e.toString());
        }
    }

    /**
     * 类加载器HOOK，用于异步加载的类hook
     *
     * @param lpparam 参数
     */
    private void AppHook_classLoader(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            try {
                                Class<?> clazz = (Class<?>) param.getResult();
                                //Log.i("[AS_DBG]","ClassLoader Loaded:"+clazz.toString());
                                if (clazz == null) return;
                                String name = clazz.getName();

                                if (name.contains("com.tencent.wework.launch.WwMainActivity")) {
                                    XVdLog("_方法HOOK", "WwMainActivity");
                                    XposedHelpers.findAndHookMethod(clazz, "onStart", new WmMainHook());
                                }else if (name.contains("com.tencent.wework.common.web.JsWebActivity")) {
                                    XVdLog("_方法HOOK", "WwMainActivity");
                                    XposedHelpers.findAndHookMethod(clazz, "onStart", new JSStartHook());
                                }else if(name.equals("android.webkit.WebView")) {
                                    XVdLog("_方法HOOK", "WebView");
                                    XposedBridge.hookAllConstructors(clazz, new WebViewConstructorHooker());
                                    XposedBridge.hookMethod(findMethod(clazz, "setWebViewClient"), new WebviewHook());
                                }
                            } catch (Exception e) {
                                XVdLog("HOOK方法错误:LoadClass", e.toString());
                            }
                            ;
                        }
                    });
        } catch (Exception e) {
            XVdLog("HOOK方法错误:LoadClass", e.toString());
        }
    }

    /**
     * 打开网页浏览器健康打卡页面
     *
     * @param ctx 当前上下文对象
     */
    private void openWeb(Context ctx, String URL) {
        try {
            XVdLog("JSWeb", "启动JSWeb");
            Intent intent = new Intent();
            intent.setClassName(ctx, "com.tencent.wework.common.web.JsWebActivity");
            intent.putExtra("extra_web_title", "自动化页面中间页");
            intent.putExtra("extra_web_url", URL);
            ctx.startActivity(intent);
        } catch (Exception e) {
            XVdLog("JsWeb启动失败", e.toString());
        }
    }

    /**
     * 系统WebViewHook，用于注入脚本。需要将X5内核关闭
     *
     * @param lpparam 参数
     */
    private void hookSystemWebView(final XC_LoadPackage.LoadPackageParam lpparam) {
        XVdLog("初始化注入", "WebView相关");
        try {
            final Class<?> webViewClazz = XposedHelpers.findClass("android.webkit.WebView", lpparam.classLoader);
            XposedBridge.hookAllConstructors(webViewClazz, new WebViewConstructorHooker());
            XposedBridge.hookMethod(findMethod(webViewClazz, "setWebContentsDebuggingEnabled"), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = true;
                }
            });
            XposedBridge.hookMethod(findMethod(webViewClazz, "setWebViewClient"), new WebviewHook());
        } catch (Exception e) {
            XVdLog("WebView注入错误", e.toString());
        }
    }

    private Method findMethod(Class<?> clazz, String name) {
        for (Method method : clazz.getMethods()) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        XVdLog("METHOD_NOT_FOUND", clazz.toString()+":"+name);
        return null;
    }

    private boolean hookCheck(String className) {
        if (classNameSet.contains(className)) {
            return false;
        } else {
            classNameSet.add(className);
            return true;
        }
    }

    private class WmMainHook extends XC_MethodHook {
        @Override
        protected void afterHookedMethod(final XC_MethodHook.MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            updateCrs((Context) param.thisObject);
            XVdLog("主Activity", "OnStart函数");
            if (data == null) return;
            if(toExit){
                toExit=false;
                started=false;
                ((Activity)param.thisObject).finish();
                return;
            }
            if (started) {
                return;
            }
            started = true;

            hasInj=false;
            XVdLog("WwMainActivity类", "标记启动");
            autoed=true;
            if (data.get("quick").equals("pass")) {
                data.set("quick", "");
                openWeb((Context) param.thisObject, Constants.URL_PASS);
            }else if (data.get("quick").equals("x5")) {
                data.set("quick", "");
                openWeb((Context) param.thisObject, Constants.X5);
            }else if (data.get("auto").equals("true") || data.get("quick").equals("sig")) {
                data.set("quick", "");
                try {
                    XVdLog("主Activity", "OnStart执行");
                    openWeb((Context) param.thisObject, Constants.URL_SIG);
                    XVdLog("主Activity", "OnStart执行完成");
                } catch (Exception e) {
                    XVdLog("主Activity错误", e.toString());
                }
            } else started=autoed=false;

        }
    }

    private class JSStartHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if(autoed){
                toExit=true;
            }
            hasInj=false;
            activity=((Activity)param.thisObject);
            XVdLog("JSStart", "ENT");
        }
    }

    private void setBright(float bright) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = bright;
            window.setAttributes(layoutParams);
        }catch (Exception e){XVdLog("亮度设置错误", e.toString());}
    }

    private class WebviewHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (param.args[0] != null && hookCheck(param.args[0].getClass().getName())) {
                XVdLog("WebViewHook", param.args[0].getClass().getName());
                XposedBridge.hookMethod(findMethod(param.args[0].getClass(), "onPageFinished"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XVdLog("脚本注入", "ENT");
                        final WebView webview = (WebView) param.args[0];
                        String url=webview.getUrl();
                        if(url.contains("://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx") && data.get("auto_bright").equals("true")){
                            setBright(1.0f);
                        }
                        if(url.contains("://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx") && data.get("force_portait").equals("true")){
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    }
                });
                XposedBridge.hookMethod(findMethod(param.args[0].getClass(), "shouldInterceptRequest"),new WebviewResHook());
            }
        }
    }
    //shouldInterceptRequest
    private class WebviewResHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                WebResourceResponse ret = null;
                WebView targetWebView = (WebView) (param.args[0]);
                WebResourceRequest request = (WebResourceRequest) (param.args[1]);
                String url = String.valueOf(request.getUrl());
                String turl = request.getRequestHeaders().get("Referer");
                if(turl==null)turl="";
                XVdLog("WEBVIEW_RES", url + " (in:" + turl + ")");
                if (data.get("inj_pass").equals("true") && !data.get("im2").equals("") && url.contains("card.png")) {
                    ret = new WebResourceResponse("image/png", "", new ByteArrayInputStream(new byte[]{}));
                } else if (data.get("inj_pass").equals("true") && turl.contains("://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx") && url.contains("://qywx.cjlu.edu.cn/resource/js/jeasyui/jquery-1.11.1.min.js")) {
                    //脚本-通行码
                    String retStr = data.get("user_jqo");
                    retStr += getPassScript();
                    ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(
                            retStr.getBytes()
                    ));
                } else if ((data.get("inj").equals("true") || data.get("once_inj").equals("true")) && turl.contains("://qywx.cjlu.edu.cn/Pages/Detail") && url.contains("://qywx.cjlu.edu.cn/resource/js/jeasyui/jquery-1.11.1.min.js")) {
                    //脚本-打卡
                    String retStr = data.get("user_jqo");
                    retStr += getSigScript();
                    ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(
                            retStr.getBytes()
                    ));
                } else if(url.startsWith("http://cjlufree.xypp.cc/record/")){
                    data.set("lastSignTs",String.valueOf(new Date().getTime()));
                    ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("success".getBytes()));
                    XVdLog("SIG_FLG","已经点击打卡");
                    Intent intent = new Intent("cc.xypp.cjlufree.widgetUpdate");
                    intent.setClassName("cc.xypp.cjluFree","cc.xypp.cjluFree.widget_sign_reminder");
                    activity.runOnUiThread(()->{
                        Toast.makeText(activity,"【量大自由】打卡已完成~",Toast.LENGTH_SHORT).show();
                    });
                    activity.sendBroadcast(intent);
                } else if (data.get("use_cache").equals("true")) {
                    if (url.startsWith("http://cjlufree.xypp.cc/user/")) {
                        url = url.substring(29);
                        if (url.contains("?_=")) {
                            url = url.substring(0, url.indexOf("?_="));
                        }
                        url = URLDecoder.decode(url, "UTF-8");
                        XVdLog("GET_USER", url);
                        data.set("user_inf", url);
                        ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("success".getBytes()));
                    } else if (url.startsWith("http://cjlufree.xypp.cc/disable/")) {
                        data.set("use_cache", "false");
                        ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("<script>location.href='https://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx'</script>".getBytes()));
                    } else if (url.contains("://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx")) {
                        String[] userInf = data.get("user_inf").split("\\|");
                        if (userInf.length >= 4)
                            ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(
                                    data.get("user_page")
                                            .replace("{{T:uname}}", userInf[0])
                                            .replace("{{T:uxh}}", userInf[1])
                                            .replace("{{T:uxy}}", userInf[2])
                                            .replace("{{T:ubj}}", userInf[3])
                                            .replace("{{T:inj}}", data.get("inj_pass"))
                                            .getBytes()
                            ));
                    } else if (url.startsWith("http://qywx.cjlu.edu.cn/Pages/RuXiao/jq_qinjIf.js")) {
                        String retStr = data.get("user_jq");
                        if (data.get("inj_pass").equals("true")) retStr = getPassScript() + retStr;
                        ret = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(
                                retStr.getBytes()
                        ));
                    }
                }

                if (ret != null) param.setResult(ret);
            }catch (Exception e){
                XVdLog("WEBVIEWRESERR", e.toString());
            }
        }
    }
    private class WebViewConstructorHooker extends XC_MethodHook{
        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            XposedHelpers.callStaticMethod(param.thisObject.getClass(), "setWebContentsDebuggingEnabled", true);
            XVdLog("WebView注入", "构造函数");
            final WebView webview = (WebView) param.thisObject;
            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            if (hookCheck(webSettings.getClass().getName())) {
                XposedBridge.hookMethod(findMethod(webSettings.getClass(), "setJavaScriptEnabled"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = true;
                    }
                });
            }
        }
    }

}
