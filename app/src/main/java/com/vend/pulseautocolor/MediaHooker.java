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
    private static final String SETTING_BATTERY_BAR_COLOR = "statusbar_battery_bar_color";

    private final Module module;

    public MediaHooker(Module module) {
        this.module = module;
    }

    @Override
    public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable {
        Object result = chain.proceed();
        try {
            handleMediaMetadataChange(chain.getThisObject());
        } catch (Exception e) {
            module.logException("Failed to process media metadata change", e);
        }
        return result;
    }

    private void handleMediaMetadataChange(@Nullable Object mediaManager) {
        if (mediaManager == null) return;

        Bitmap albumArt = extractAlbumArt(mediaManager);
        if (albumArt == null) {
            module.logInfo("No album art found in metadata");
            return;
        }

        int dominantColor = extractDominantColor(albumArt);

        Context context = module.getSystemContext();
        if (context != null) {
            applyColorToSystemSettings(context, dominantColor);
        }
    }

    @Nullable
    private Bitmap extractAlbumArt(Object mediaManager) {
        try {
            Field field = mediaManager.getClass().getDeclaredField("mMediaMetadata");
            field.setAccessible(true);
            Object metadata = field.get(mediaManager);

            if (metadata == null) return null;

            Method getBitmap = metadata.getClass().getMethod("getBitmap", String.class);

            Bitmap bitmap = (Bitmap) getBitmap.invoke(metadata, METADATA_KEY_ART);
            if (bitmap == null) {
                bitmap = (Bitmap) getBitmap.invoke(metadata, METADATA_KEY_ALBUM_ART);
            }
            return bitmap;
        } catch (Exception e) {
            module.logException("Reflection failed to extract bitmap", e);
            return null;
        }
    }

    private int extractDominantColor(Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        return palette.getDominantColor(0xFFFFFFFF);
    }

    private void applyColorToSystemSettings(Context context, int color) {
        ContentResolver cr = context.getContentResolver();

        Settings.Secure.putInt(cr, SETTING_PULSE_COLOR, color);
        Settings.System.putInt(cr, SETTING_BATTERY_BAR_COLOR, color);

        module.logInfo("System colors updated to: #" + Integer.toHexString(color));
    }
}
