package com.vend.pulseautocolor;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public class Module extends XposedModule {

    private Context systemContext;
    private static final String TAG = "PulseAutoColor";

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        super.onModuleLoaded(param);
        log(Log.INFO, TAG, "framework: " + getFrameworkName() + "(" + getFrameworkVersionCode() + ") API " + getApiVersion());
    }

    private void logMsg(String msg) {
        Log.i(TAG, msg);
    }

    @Override
    public void onPackageLoaded(@NonNull XposedModuleInterface.PackageLoadedParam param) {
        if (param.getPackageName().equals("com.android.systemui")) {
            hookMedia(param.getDefaultClassLoader());
        }
    }

    private void hookMedia(ClassLoader classLoader) {
        try {
            Class<?> mediaManager = classLoader.loadClass("com.android.systemui.statusbar.NotificationMediaManager");
            for (Method method : mediaManager.getDeclaredMethods()) {
                if (method.getName().equals("dispatchUpdateMediaMetaData")) {
                    hook(method).intercept(new MediaHooker());
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Hook failed: ", t);
        }
    }

    class MediaHooker implements XposedInterface.Hooker {
        @Override
        public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable {
            Object result = chain.proceed();

            try {
                Object mediaManager = chain.getThisObject();
                if (mediaManager == null) return result;

                Bitmap bitmap = null;

                try {
                    Field field = mediaManager.getClass().getDeclaredField("mMediaMetadata");
                    field.setAccessible(true);
                    Object metadata = field.get(mediaManager);

                    if (metadata != null) {
                        Method getBitmap = metadata.getClass().getMethod("getBitmap", String.class);
                        bitmap = (Bitmap) getBitmap.invoke(metadata, "android.media.metadata.ART");
                        if (bitmap == null) {
                            bitmap = (Bitmap) getBitmap.invoke(metadata, "android.media.metadata.ALBUM_ART");
                        }
                    }
                } catch (Exception e) {
                    logMsg("Could not get bitmap from metadata: " + e.getMessage());
                }

                if (bitmap == null) {
                    logMsg("No bitmap found in metadata");
                    return result;
                }

                Context context = getSystemContext();
                if (context == null) {
                    logMsg("System context is null, cannot update color");
                    return result;
                }

                Palette palette = Palette.from(bitmap).generate();
                int color = palette.getDominantColor(0xFFFFFFFF);

                ContentResolver cr = context.getContentResolver();
                Settings.Secure.putInt(cr, "pulse_color_user", color);

                logMsg("Set color: #" + Integer.toHexString(color));

            } catch (Throwable t) {
                Log.e(TAG, "Error in hook: ", t);
            }
            return result;
        }
    }

    private Context getSystemContext() {
        if (systemContext == null) {
            try {
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                Method currentApplication = activityThread.getDeclaredMethod("currentApplication");
                systemContext = (Context) currentApplication.invoke(null);
            } catch (Exception e) {
                Log.i(TAG, "Failed to get system context", e);
            }
        }
        return systemContext;
    }
}
