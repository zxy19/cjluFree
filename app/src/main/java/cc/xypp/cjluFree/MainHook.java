package cc.xypp.cjluFree;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.Arrays;
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
        return data.get("sig_src");
    }
    /**
     * ??????Xposed????????????
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
                    XVdLog("???????????????", e.toString() + "|" + Arrays.toString(e.getStackTrace()));
                }
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param lpparam
     */
    private void hookAutomation(final XC_LoadPackage.LoadPackageParam lpparam) {
        XVdLog("???????????????", "????????????hook");
        AppHook_Context(lpparam);
        AppHook_functions(lpparam);
        AppHook_classLoader(lpparam);
    }

    /**
     * ???????????????HOOK
     *
     * @param lpparam ??????
     */
    private void AppHook_Context(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (data != null) return;
                    try {
                        Context applicationContext = (Context) param.getResult();
                        if (applicationContext == null) return;
                        ctx=applicationContext;
                        XVdLog("??????Context", applicationContext.toString());
                        ContentResolver rsv = applicationContext.getContentResolver();
                        if (rsv == null) return;
                        data = new dataUtil(rsv);
                        enableLog=data.get("enable_log").equals("true");
                        XVdLog("??????Data", data.get("auto") + "-" + data.get("inj") + "-" + data.get("once"));
                    } catch (Exception e) {
                        XVdLog("???Context??????", e.toString());
                    }
                }
            });
        } catch (Throwable t) {
            XVdLog("???Context??????", t.toString());
        }
    }

    /**
     * ????????????HOOK
     *
     * @param lpparam ??????
     */
    private void AppHook_functions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.launch.WwMainActivity", lpparam.classLoader);
            if (tmp != null) {
                XVdLog("??????HOOK", "WwMainActivity");
                XposedHelpers.findAndHookMethod(tmp, "onStart", new WmMainHook());
            }
        } catch (Exception e) {
            XVdLog("HOOK????????????:WwMainActivity", e.toString());
        }

        try {
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.common.web.JsWebActivity", lpparam.classLoader);
            if (tmp != null) {
                XVdLog("??????HOOK", "JsWebActivity");
                XposedHelpers.findAndHookMethod(tmp, "onStart", new JSStartHook());
            }
        } catch (Exception e) {
            XVdLog("HOOK????????????:JsWebActivity", e.toString());
        }
    }

    /**
     * ????????????HOOK???????????????????????????hook
     *
     * @param lpparam ??????
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
                                    XVdLog("_??????HOOK", "WwMainActivity");
                                    XposedHelpers.findAndHookMethod(clazz, "onStart", new WmMainHook());
                                }else if (name.contains("com.tencent.wework.common.web.JsWebActivity")) {
                                    XVdLog("_??????HOOK", "WwMainActivity");
                                    XposedHelpers.findAndHookMethod(clazz, "onStart", new JSStartHook());
                                }else if(name.equals("android.webkit.WebView")) {
                                    XVdLog("_??????HOOK", "WebView");
                                    XposedBridge.hookAllConstructors(clazz, new WebViewConstructorHooker());
                                    XposedBridge.hookMethod(findMethod(clazz, "setWebViewClient"), new WebviewHook());
                                }else if(name.contains("com.tencent.wework.launch.LaunchSplashDefaultFragment")) {
                                    XposedHelpers.findAndHookMethod(clazz, "a",BitmapDrawable.class,boolean.class,String.class,String.class,String.class,new BitmapHooker());
                                    XVdLog("_??????HOOK", "LaunchSplashDefaultFragment");
                                }
                            } catch (Exception e) {
                                XVdLog("HOOK????????????:LoadClass", e.toString());
                            }
                            ;
                        }
                    });
        } catch (Exception e) {
            XVdLog("HOOK????????????:LoadClass", e.toString());
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param ctx ?????????????????????
     */
    private void openWeb(Context ctx, String URL) {
        try {
            XVdLog("JSWeb", "??????JSWeb");
            Intent intent = new Intent();
            intent.setClassName(ctx, "com.tencent.wework.common.web.JsWebActivity");
            intent.putExtra("extra_web_title", "????????????????????????");
            intent.putExtra("extra_web_url", URL);
            ctx.startActivity(intent);
        } catch (Exception e) {
            XVdLog("JsWeb????????????", e.toString());
        }
    }

    /**
     * ??????WebViewHook?????????????????????????????????X5????????????
     *
     * @param lpparam ??????
     */
    private void hookSystemWebView(final XC_LoadPackage.LoadPackageParam lpparam) {
        XVdLog("???????????????", "WebView??????");
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
            XVdLog("WebView????????????", e.toString());
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
            XVdLog("???Activity", "OnStart??????");
            if (data == null) return;
            if(toExit){
                toExit=false;
                started=false;
                ((Activity)param.thisObject).finish();
                return;
            }
            if (started) {
//                ((Activity)param.thisObject).finish();
                return;
            }
            started = true;

            hasInj=false;
            XVdLog("WwMainActivity???", "????????????");
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
                    XVdLog("???Activity", "OnStart??????");
                    openWeb((Context) param.thisObject, Constants.URL_SIG);
                    XVdLog("???Activity", "OnStart????????????");
                } catch (Exception e) {
                    XVdLog("???Activity??????", e.toString());
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
            activity=((Activity)param.thisObject);
        }
    }

    private void setBright(float bright) {
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = bright;
            window.setAttributes(layoutParams);
        }catch (Exception e){XVdLog("??????????????????", e.toString());}
    }

    private class WebviewHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (param.args[0] != null && hookCheck(param.args[0].getClass().getName())) {
                XVdLog("WebViewHook", param.args[0].getClass().getName());
                XposedBridge.hookMethod(findMethod(param.args[0].getClass(), "onPageFinished"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XVdLog("????????????", "ENT");
                        final WebView webview = (WebView) param.args[0];
                        String url=webview.getUrl();
                        XVdLog("??????????????????", webview.getUrl());
                        if (url.contains("https://qywx.cjlu.edu.cn/Pages/Detail") && !hasInj && (data.get("inj").equals("true") || data.get("once_inj").equals("true"))) {
                            data.set("once_inj","false");
                            XVdLog("????????????", "????????????");
                            webview.evaluateJavascript(getSigScript(), new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    hasInj = true;
                                    XVdLog("????????????", "??????");
                                }
                            });
                        }else if(url.contains("https://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx") && data.get("inj_pass").equals("true")){
                            XVdLog("????????????", "????????????");
                            webview.evaluateJavascript(getPassScript(), new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    XVdLog("????????????", "??????");
                                }
                            });
                        }
                        if(url.contains("https://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx") && data.get("auto_bright").equals("true")){
                            setBright(1.0f);
                        }
                    }
                });
                XVdLog("WebViewHookB", param.args[0].getClass().getName());
                XposedBridge.hookMethod(findMethod(param.args[0].getClass(), "onPageStarted"), new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XVdLog("????????????P", "ENT");
                        final WebView webview = (WebView) param.args[0];
                        String url= (String) param.args[1];
                        XVdLog("??????????????????P", url);
                        if(url.contains("https://qywx.cjlu.edu.cn/Pages/RuXiao/XSLXM.aspx") && data.get("inj_pass").equals("true")){
                            XVdLog("????????????P", "????????????");
                            webview.evaluateJavascript(getPassScript(), new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    XVdLog("????????????P", "??????");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private class WebViewConstructorHooker extends XC_MethodHook{
        @Override
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            XposedHelpers.callStaticMethod(param.thisObject.getClass(), "setWebContentsDebuggingEnabled", true);
            XVdLog("WebView??????", "????????????");
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
    private class BitmapHooker extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param){
            XVdLog("LaunchSplashDefaultFragment", "??????");
            try {
                String varContent = data.get("im_l");
                if (varContent.startsWith("data:image/")) {
                    XVdLog("LaunchSplashDefaultFragment", "??????");
                    varContent = varContent.substring(varContent.indexOf("base64,") + 7);
                    byte[] imgDat = Base64.decode(varContent, Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(imgDat, 0, imgDat.length);
                    param.args[0] = new BitmapDrawable(ctx.getResources(), bm);
                }
            }catch (Exception e){
                XVdLog("LaunchSplashDefaultFragment??????", e.toString());
            }
        }
    }

}
