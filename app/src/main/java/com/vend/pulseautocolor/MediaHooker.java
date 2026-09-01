package com.vend.pulseautocolor;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;

public class MediaHooker implements XposedInterface.Hooker {
    private static final String METADATA_KEY_ART = "android.media.metadata.ART";
    private static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    private static final String SETTING_PULSE_COLOR = "pulse_color_user";

    private final Module module;

    public MediaHooker(Module module) {
        this.module = module;
    }

    @Override
    public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable {
        Object result = chain.proceed();
        
        try {
            processMediaUpdate(chain.getThisObject());
        } catch (Exception e) {
            module.logException("Error processing media update", e);
        }
        
        return result;
    }

    private void processMediaUpdate(@Nullable Object mediaManager) {
        if (mediaManager == null) return;

        Bitmap bitmap = extractBitmap(mediaManager);
        if (bitmap == null) {
            module.logInfo("No album art found in metadata");
            return;
        }

        Context context = module.getSystemContext();
        if (context == null) return;

        updatePulseColor(context, bitmap);
    }

    @Nullable
    private Bitmap extractBitmap(Object mediaManager) {
        try {
            Field field = mediaManager.getClass().getDeclaredField("mMediaMetadata");
            field.setAccessible(true);
            Object metadata = field.get(mediaManager);

            if (metadata == null) return null;

            Method getBitmap = metadata.getClass().getMethod("getBitmap", String.class);
            
            // Try ART first, then ALBUM_ART
            Bitmap bitmap = (Bitmap) getBitmap.invoke(metadata, METADATA_KEY_ART);
            if (bitmap == null) {
                bitmap = (Bitmap) getBitmap.invoke(metadata, METADATA_KEY_ALBUM_ART);
            }
            return bitmap;
        } catch (Exception e) {
            module.logException("Reflection failed to get bitmap", e);
            return null;
        }
    }

    private void updatePulseColor(Context context, Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        int color = palette.getDominantColor(0xFFFFFFFF);

        ContentResolver cr = context.getContentResolver();
        Settings.Secure.putInt(cr, SETTING_PULSE_COLOR, color);
        
        module.logInfo("Updated color to: #" + Integer.toHexString(color));
    }
}
