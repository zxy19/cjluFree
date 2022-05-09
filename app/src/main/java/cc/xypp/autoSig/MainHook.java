package cc.xypp.autoSig;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private final String toInjScript = "document.body.appendChild(function(){var c=document.createElement(\"div\");c.style.position=\"fixed\";c.style.width=\"100px\";c.style.textAlign=\"center\";c.style.height=\"30px\";c.style.zIndez=\"1000000\";c.style.left=\"0px\";c.style.top=\"0px\";c.style.transition=\"all 1s\";c.style.opacity=\"0\";c.style.background=\"black\";c.style.color=\"white\";c.style.borderRadius=\"4px\";c.innerHTML=\"重新注入脚本\";c.onclick=function(e){e.currentTarget.style.opacity=\"0\";var head=document.getElementsByTagName(\"head\")[0],tmt;if(tmt=document.getElementById(\"__injected_atos\")){head.removeChild(tmt);}var cc=document.createElement(\"script\");cc.src=\"https://xypp.cc/xposed/\";cc.id=\"__injected_atos\";cc.onload=function(){c.style.opacity=\"1\";};setTimeout(function(head,cc){head.appendChild(cc);},500,head,cc)};setTimeout(function(c){c.onclick({currentTarget:c});},500,c);return c;}());";
    private dataUtil data;
    private final HashSet<String> classNameSet = new HashSet<>();
    private Class<?> JWClazz;
    private Context acontext;
    private boolean actvd=false;
    private void XVdLog(String flg,String content){
        Log.i("[AS_LOG]"+flg,content);
    }
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.equals("com.tencent.wework"))return;
        if(lpparam.isFirstApplication){
            if(hookCheck(lpparam.packageName)){
                try{
                    actvd=false;
                    JWClazz=null;
                    this.hookAutomation(lpparam);
                    this.hookSystemWebView(lpparam);
                }catch (Exception e){
                    XVdLog("初始化注入", e.toString()+"|"+ Arrays.toString(e.getStackTrace()));
                }
            }
        }
    }

    private void hookAutomation(final XC_LoadPackage.LoadPackageParam lpparam) {

        XVdLog("初始化注入", "系统函数hook");
        AppHook_Context(lpparam);
        AppHook_functions(lpparam);
        AppHook_classLoader(lpparam);
    }

    private void AppHook_Context(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if(data!=null)return;
                    try{
                        Context applicationContext = (Context) param.getResult();
                        if(applicationContext==null)return;
                        XVdLog("取得Context",applicationContext.toString());
                        acontext = applicationContext;
                        ContentResolver rsv = applicationContext.getContentResolver();
                        if(rsv==null)return;
                        data=new dataUtil(rsv);
                        XVdLog("取得Data", data.get("auto")+"-"+data.get("inj")+"-"+data.get("once"));
                    }catch (Exception e){
                        XVdLog("取Context错误",e.toString());
                    }
                }
            });
        } catch (Throwable t) {
            XVdLog("取Context错误", t.toString());
        }
    }

    private void AppHook_functions(XC_LoadPackage.LoadPackageParam lpparam) {
        try{
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.common.web.JsWebLauncher", lpparam.classLoader);
            if(tmp!=null){
                XVdLog("方法HOOK","JsWebLauncher");
                JWClazz=tmp;
            }
        }catch (Exception e){
            XVdLog("HOOK方法错误:JsWebLauncher", e.toString());
        }

        try{
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.launch.WwMainActivity", lpparam.classLoader);
            if(tmp!=null){
                XVdLog("方法HOOK","WwMainActivity");
                XposedHelpers.findAndHookMethod(tmp, "onStart", new WmMainHook());
            }
        }catch (Exception e){
            XVdLog("HOOK方法错误:WwMainActivity", e.toString());
        }

        try{
            Class<?> tmp = XposedHelpers.findClass("com.tencent.wework.msg.controller.MessageListFragment", lpparam.classLoader);
            if(tmp!=null){
                XVdLog("方法HOOK","MessageListFragment");
                XposedHelpers.findAndHookConstructor(tmp, lpparam.classLoader, new MsgListFrgHook());
            }
        }catch (Exception e){
            XVdLog("HOOK方法错误:MessageListFragment", e.toString());
        }
    }

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
                                if(clazz==null)return;
                                String name=clazz.getName();
                                if (name.contains("com.tencent.wework.common.web.JsWebLauncher")) {
                                    XVdLog("方法HOOK","JsWebLauncher");
                                    JWClazz = clazz;
                                } else if (name.contains("com.tencent.wework.msg.controller.MessageListFragment")) {
                                    XVdLog("方法HOOK","MessageListFragment");
                                    XposedHelpers.findAndHookConstructor(clazz, lpparam.classLoader, new MsgListFrgHook());
                                } else if (name.contains("com.tencent.wework.launch.WwMainActivity")) {
                                    XVdLog("方法HOOK","WwMainActivity");
                                    XposedHelpers.findAndHookMethod(clazz, "onStart", new WmMainHook());
                                }
                            } catch (Exception e) {
                                XVdLog("HOOK方法错误:LoadClass", e.toString());
                            };
                        }
                    });
        }catch (Exception e){
            XVdLog("HOOK方法错误:LoadClass", e.toString());
        }
    }

    private boolean JSWJdgCls(Class<?> clazz) {
        Method[] m =clazz.getMethods();
        for(Method tm:m){
            if(tm.getName().equals("N"))return true;
        }
        return false;
    }

    private void openWeb(Context ctx) {
        try {
            XVdLog("JSWeb","将启动JSWeb");
            XVdLog("JSWeb","Context:"+ctx+"\nJsWebClass:"+JWClazz.toString()+"\n");
            try {
                XposedHelpers.callStaticMethod(JWClazz,"N", new Class[]{Context.class, String.class, String.class}, ctx, "健康打卡-自动化", "https://qywx.cjlu.edu.cn/Pages/Detail.aspx?ID=5986121fe88949c283e1719f595d57da\n");
            } catch (Exception e) {
                XVdLog("JsWeb方法调用失败", e.toString());
            }
            XVdLog("JsWeb","启动过程结束");
            data.set("once", "false");
        }catch (Exception e){
            XVdLog("JsWeb启动失败",e.toString());
        }
    }

    private void hookSystemWebView(final XC_LoadPackage.LoadPackageParam lpparam){
        XVdLog("初始化注入", "WebView相关");
        try{
            final Class<?> webViewClazz = XposedHelpers.findClass("android.webkit.WebView",lpparam.classLoader);
            XposedBridge.hookAllConstructors(webViewClazz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callStaticMethod(webViewClazz, "setWebContentsDebuggingEnabled", true);
                    XVdLog("WebView注入", "构造函数");
                    final WebView webview = (WebView)param.thisObject;
                    WebSettings webSettings = webview.getSettings();
                    webSettings.setJavaScriptEnabled(true);
                    if(hookCheck(webSettings.getClass().getName())){
                        XposedBridge.hookMethod(findMethod(webSettings.getClass(),"setJavaScriptEnabled"),new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.args[0] = true;
                            }
                        });
                    }
                }
            });
            XposedBridge.hookMethod(findMethod(webViewClazz,"setWebContentsDebuggingEnabled"),new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = true;
                }
            });
            XposedBridge.hookMethod(findMethod(webViewClazz,"setWebViewClient"),new WebviewHook());
        }catch (Exception e){
            XVdLog("WebView注入错误", e.toString());
        }
    }

    private Method findMethod(Class<?> clazz,String name){
        for(Method method : clazz.getMethods()){
            if(name.equals(method.getName())){
                return method;
            }
        }
        return null;
    }

    private boolean hookCheck(String className){
        if(classNameSet.contains(className)){
            return false;
        }else{
            classNameSet.add(className);
            return true;
        }
    }

    private class WmMainHook extends XC_MethodHook {
        @Override
        protected void afterHookedMethod(final XC_MethodHook.MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            XVdLog("主Activity","OnStart函数");
            actvd=true;
            if (data == null || JWClazz == null) return;
            if (!data.get("auto").equals("true") && !data.get("once").equals("true"))
                return;
            actvd=false;

            try {
                XVdLog("主Activity","OnStart执行");
                openWeb((Context) param.thisObject);
                XVdLog("主Activity","OnStart执行完成");
            }catch (Exception e){
                XVdLog("主Activity错误",e.toString());
            }
        }
    }

    private class MsgListFrgHook extends XC_MethodHook{
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            //if(JWClazz!=null)return;
            XVdLog("聊天列表","构造器");

            try{
                XposedHelpers.callMethod(param.thisObject,"cu",101,"data:text/plain;base64,562J5b6F5rOo5YWl56iL5bqP5bel5L2c");
            }catch (Exception e){
                XVdLog("聊天列表",e.toString());
            }
        }
    }

    private class WebviewHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if(param.args[0] != null && hookCheck(param.args[0].getClass().getName())){
            XposedBridge.hookMethod(findMethod(param.args[0].getClass(),"onPageFinished"),new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(!data.get("inj").equals("true"))return;
                    XVdLog("脚本注入","请求");
                    final WebView webview = (WebView)param.args[0];
                    webview.evaluateJavascript(toInjScript,new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            XVdLog("脚本注入","结束");
                        }
                    });
                    }
                });
            }
        }
    }
}
