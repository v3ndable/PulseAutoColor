package com.vend.pulseautocolor;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public class Module extends XposedModule {

    private static final String TAG = "PulseAutoColor";
    private static final String PACKAGE_SYSTEMUI = "com.android.systemui";
    private static final String CLASS_MEDIA_MANAGER = "com.android.systemui.statusbar.NotificationMediaManager";
    private static final String METHOD_UPDATE_METADATA = "dispatchUpdateMediaMetaData";

    private Context systemContext;

    void logInfo(String msg) {
        Log.i(TAG, msg);
    }

    void logException(String msg, Throwable e) {
        Log.e(TAG, msg, e);
    }

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        super.onModuleLoaded(param);
        logInfo("PulseAutoColor loaded on " + getFrameworkName() + " (" + getFrameworkVersionCode() + ")");
    }

    @Override
    public void onPackageLoaded(@NonNull XposedModuleInterface.PackageLoadedParam param) {
        if (PACKAGE_SYSTEMUI.equals(param.getPackageName())) {
            hookMedia(param.getDefaultClassLoader());
        }
    }

    private void hookMedia(ClassLoader classLoader) {
        try {
            Class<?> mediaManagerClass = classLoader.loadClass(CLASS_MEDIA_MANAGER);
            
            boolean hooked = false;
            for (Method method : mediaManagerClass.getDeclaredMethods()) {
                if (method.getName().equals(METHOD_UPDATE_METADATA)) {
                    hook(method).intercept(new MediaHooker(this));
                    hooked = true;
                }
            }
            
            if (hooked) {
                logInfo("Successfully hooked " + METHOD_UPDATE_METADATA);
            } else {
                logInfo("Method " + METHOD_UPDATE_METADATA + " not found in " + CLASS_MEDIA_MANAGER);
            }
            
        } catch (ClassNotFoundException e) {
            logException("SystemUI class not found: " + CLASS_MEDIA_MANAGER, e);
        } catch (Throwable t) {
            logException("Unexpected error during initialization", t);
        }
    }

    Context getSystemContext() {
        if (systemContext != null) return systemContext;

        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method currentApplication = activityThread.getDeclaredMethod("currentApplication");
            systemContext = (Context) currentApplication.invoke(null);
        } catch (Exception e) {
            logException("Failed to get system context via ActivityThread", e);
        }
        
        return systemContext;
    }
}
