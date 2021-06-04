package com.example.xposedxdebug;

import android.os.Debug;
import android.os.Process;
import android.text.Selection;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final int DEBUG_ENABLE_DEBUGGER = 0x1;
    private XC_MethodHook debugAppsHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param)
                throws Throwable {
            int id = 5;
            int flags = (Integer) param.args[id];
            if ((flags & DEBUG_ENABLE_DEBUGGER) == 0) {
                flags |= DEBUG_ENABLE_DEBUGGER;
            }
            param.args[id] = flags;
        }
    };

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        hookWheel(loadPackageParam);
        XposedHelpers.findAndHookMethod(Debug.class, "startMethodTracingDdms", int.class, int.class, boolean.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int size = (Integer) param.args[0];
                if (size == 8388608) {
                    param.args[0] = size * 10;
                }
            }
        });
    }

    private void hookWheel(XC_LoadPackage.LoadPackageParam app) {
        if (app.packageName.equals(HookConfig.wheel)) {
            Log.i(TAG, "hookWheel: hook我的项目");
            XposedHelpers.findAndHookMethod(TextView.class, "getSelectionStart", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (param != null && param.thisObject != null && (param.thisObject instanceof TextView)) {
                        TextView tv = (TextView) param.thisObject;
                        int start = Selection.getSelectionStart(tv.getText());
                        int end = Selection.getSelectionEnd(tv.getText());
                        if (start >= 0 && start != end) {
                            String content = tv.getText().toString();
                            Log.i(TAG, "beforeHookedMethod start: " + start);
                            Log.i(TAG, "beforeHookedMethod   end: " + end);
                            Log.d(TAG, "beforeHookedMethod: 获取完整文字： " + content);
                            int length = content.length();
                            if (length > 0 && end <= length) {
                                String substring = content.substring(start, end);
                                Log.e(TAG, "beforeHookedMethod: 选择的文字： " + substring);
                            }
                        }
                    }
                }
            });

        } else if ("com.tencent.mm".equals(app.packageName)) {
            Log.d(TAG, "hookWheel: 微信8.0.2");
            Class c = XposedHelpers.findClass("com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView", app.classLoader);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "setNewMoney", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        //修改钱包余额
                        param.args[0] = "4298920.10";
                    }
                });
            }
        }
    }

    private static final String TAG = "Hook =>>";

    @Override
    public void initZygote(final IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        Method[] methods = Process.class.getDeclaredMethods();
        XposedBridge.hookAllMethods(Process.class, "start", debugAppsHook);
    }
}
