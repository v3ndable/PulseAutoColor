package com.vend.pulseautocolor;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

    public Module(@NonNull XposedInterface base, @NonNull XposedModuleInterface.ModuleLoadedParam param) {
        super();
        log(Log.INFO, TAG, "framework: $frameworkName($frameworkVersionCode) API $apiVersion");
        Log.i(TAG, "PulseAutoColor loaded");
    }

    private void logMsg(String msg) {
        Log.i(TAG, msg);
    }

    @Override
    public void onPackageLoaded(@NonNull XposedModuleInterface.PackageLoadedParam param) {
        Log.i(TAG, ">>> onPackageLoaded: " + param.getPackageName());
        super.onPackageLoaded(param);
        // We only care about SystemUI for this hook
        if (param.getPackageName().equals("com.android.systemui")) {
            logMsg("System UI loaded, hooking media...");
            // onPackageLoaded is only called on API 29+, so getDefaultClassLoader is available
            logMsg("1...");
            hookMedia(param.getDefaultClassLoader());
        }
    }

    private void hookMedia(ClassLoader classLoader) {
        try {
            logMsg("2...");
            Class<?> mediaManager = classLoader.loadClass("com.android.systemui.statusbar.NotificationMediaManager");
            logMsg("3...");
            // In LibXposed 100+, we find and hook methods individually
            for (Method method : mediaManager.getDeclaredMethods()) {
                logMsg("4...");
                if (method.getName().equals("dispatchUpdateMediaMetaData")) {
                    logMsg("5...");
                    hook(method).intercept(new MediaHooker());
                }
            }
        } catch (Throwable t) {
            Log.i(TAG, "Hook failed: " + t);
        }
    }

    class MediaHooker implements XposedInterface.Hooker {
        @Override
        public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable {
            // Execute the original method first (equivalent to AfterHooker)
            Object result = chain.proceed();
            logMsg("6...");

            try {
                // For NotificationMediaManager, we get the metadata from the instance
                Object mediaManager = chain.getThisObject();
                Bitmap bitmap = null;
                logMsg("7...");

                try {
                    // Try to get mMediaMetadata field which is common in NotificationMediaManager
                    Field field = mediaManager.getClass().getDeclaredField("mMediaMetadata");
                    field.setAccessible(true);
                    Object metadata = field.get(mediaManager);

                    if (metadata != null) {
                        // Extract bitmap from MediaMetadata using standard keys
                        Method getBitmap = metadata.getClass().getMethod("getBitmap", String.class);
                        bitmap = (Bitmap) getBitmap.invoke(metadata, "android.media.metadata.ART");
                        if (bitmap == null) {
                            bitmap = (Bitmap) getBitmap.invoke(metadata, "android.media.metadata.ALBUM_ART");
                        }
                    }
                } catch (Exception e) {
                    logMsg("Could not get bitmap from metadata: " + e);
                }

                if (bitmap == null) return result;

                Context context = getSystemContext();
                if (context == null) return result;

                // Extract dominant color using the Palette API
                Palette palette = Palette.from(bitmap).generate();
                int color = palette.getDominantColor(0xFFFFFFFF);

                logMsg("8...");

                // Update system settings with the new color
                ContentResolver cr = context.getContentResolver();
                Settings.Secure.putInt(cr, "pulse_color_user", color);

                logMsg("Set color: " + Integer.toHexString(color));

            } catch (Throwable t) {
                Log.i(TAG, "Error in hook: " + t);
            }
            return result;
        }
    }

    private Context getSystemContext() {
        if (systemContext == null) {
            try {
                // Standard reflection to get the system context
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                Method currentApplication = activityThread.getDeclaredMethod("currentApplication");
                systemContext = (Context) currentApplication.invoke(null);
            } catch (Exception e) {
                Log.i(TAG, "Failed to get system context", e);
            }
        }
        return systemContext;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } catch (Throwable t) {
            return null;
        }
    }
}