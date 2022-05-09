package cc.xypp.autoSig;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Method;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private final String toInjScript = "document.body.appendChild(function(){var c=document.createElement(\"div\");c.style.position=\"fixed\";c.style.width=\"100px\";c.style.textAlign=\"center\";c.style.height=\"30px\";c.style.zIndez=\"1000000\";c.style.left=\"0px\";c.style.top=\"0px\";c.style.transition=\"all 1s\";c.style.opacity=\"0\";c.style.background=\"black\";c.style.color=\"white\";c.style.borderRadius=\"4px\";c.innerHTML=\"重新注入脚本\";c.onclick=function(e){e.currentTarget.style.opacity=\"0\";var head=document.getElementsByTagName(\"head\")[0],tmt;if(tmt=document.getElementById(\"__injected_atos\")){head.removeChild(tmt);}var cc=document.createElement(\"script\");cc.src=\"https://xypp.cc/xposed/\";cc.id=\"__injected_atos\";cc.onload=function(){c.style.opacity=\"1\";};setTimeout(function(head,cc){head.appendChild(cc);},500,head,cc)};setTimeout(function(c){c.onclick({currentTarget:c});},500,c);return c;}());";
    private dataUtil data;
    private HashSet<String> classNameSet = new HashSet<>();
    private Class<?> JWClazz;
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.w("XPDE", "PKG:"+lpparam.packageName);
        if(!lpparam.packageName.equals("com.tencent.wework"))return;
        if(lpparam.isFirstApplication){
            Log.w("XPDET", "A");
            if(hookCheck(lpparam.packageName)){
                try{
                    Log.w("XPDET", "A");
                    this.hookSystemWebView(lpparam);
                    this.hookAutomation(lpparam);
                }catch (Exception e){
                    Log.w("XPDET", e.toString());
                }
            }
        }
    }

    private void hookAutomation(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Log.w("XPDET", "@@");
            Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if(data!=null)return;
                    try{
                        Context applicationContext = (Context) param.getResult();
                        Log.w("XPDET","((-"+applicationContext.toString());
                        ContentResolver rsv = applicationContext.getContentResolver();
                        Log.w("XPDET","<<-"+rsv.toString());
                        data=new dataUtil(rsv);
                        Log.w("XPDET", data.get("auto")+"-"+data.get("inj"));
                    }catch (Exception e){
                        Log.w("XPDET", e.toString());
                    }
                    XposedBridge.log("CSDN_LQR-->得到上下文");
                }
            });
        } catch (Throwable t) {
            Log.w("XPDET", t.toString());
        }
        try {
            XposedHelpers.findAndHookMethod(
                    ClassLoader.class,
                    "loadClass",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Class<?> clazz = (Class<?>) param.getResult();
                            if (clazz.getName().contains("com.tencent.wework.common.web.JsWebLauncher")) {
                                try {
                                    Log.w("XPDET", "A-Fnd");
                                    JWClazz = clazz;
                                } catch (Exception ignored) { };
                            }

                            else if (clazz.getName().contains("com.tencent.wework.launch.WwMainActivity")) {
                                XposedHelpers.findAndHookMethod(clazz, "onStart", new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        super.afterHookedMethod(param);
                                        if (data == null || JWClazz == null) return;
                                        if (!data.get("auto").equals("true") || data.get("once").equals("true"))
                                            return;
                                        Log.w("XPDET", "A-RES");
                                        final Context ctx = AndroidAppHelper.currentApplication();
                                        XposedHelpers.callStaticMethod(JWClazz, "N", ctx, "健康打卡-自动化", "https://qywx.cjlu.edu.cn/Pages/Detail.aspx?ID=5986121fe88949c283e1719f595d57da\n");
                                        Log.w("XPDET", "A-Called");
                                        data.set("once", "false");
                                    }
                                });
                            }
                        }
                    });
        }catch (Exception e){
            Log.w("XPDET","EE@"+e.toString());
        }
    }

    private void hookSystemWebView(final XC_LoadPackage.LoadPackageParam lpparam){
        try{
            Log.w("XPDET","HOOKPSS-"+lpparam.packageName);
            final Class<?> webViewClazz = XposedHelpers.findClass("android.webkit.WebView",lpparam.classLoader);
            XposedBridge.hookAllConstructors(webViewClazz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedHelpers.callStaticMethod(webViewClazz, "setWebContentsDebuggingEnabled", true);
                    Log.w("XPDET","HOOKPSSB");
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
            XposedBridge.log("[debug webview] setWebContentsDebuggingEnabled");
            XposedBridge.hookMethod(findMethod(webViewClazz,"setWebViewClient"),new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0] != null && hookCheck(param.args[0].getClass().getName())){
                        XposedBridge.hookMethod(findMethod(param.args[0].getClass(),"onPageFinished"),new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if(!data.get("inj").equals("true"))return;
                                Log.w("XPDET","ijd2");
                                final WebView webview = (WebView)param.args[0];
                                webview.evaluateJavascript(toInjScript,new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        Log.w("XPDET","ijd");
                                        XposedBridge.log("[debug webview] inject vconsole");
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }catch (Exception e){
            XposedBridge.log(e.getMessage());
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

    /**
     * 检查该类是否已经hook过，未hook的返回true
     * @param className
     * @return
     */
    private boolean hookCheck(String className){
        if(classNameSet.contains(className)){
            return false;
        }else{
            classNameSet.add(className);
            return true;
        }
    }
}