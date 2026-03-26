package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

public class ImageRotationUtils {
    /**
     * Rotate + optionally mirror a bitmap based on EXIF orientation
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation, boolean mirror) {
        if (bitmap == null) return null;
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.preScale(-1f, 1f); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.preScale(1f, -1f); break;
            default: break;
        }

        if (mirror) {
            matrix.postScale(-1f, 1f); // enforce selfie mirror
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
