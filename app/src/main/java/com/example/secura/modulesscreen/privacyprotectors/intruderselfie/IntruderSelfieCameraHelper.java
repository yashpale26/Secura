package com.example.secura.modulesscreen.privacyprotectors.intruderselfie;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IntruderSelfieCameraHelper (Realme safe)
 * - Uses app-private file path for CameraX output (never fails on Realme)
 * - Normalizes rotation & mirror with ImageRotationUtils
 * - Optionally inserts corrected file into MediaStore
 * - Keeps legacy methods intact
 */
public class IntruderSelfieCameraHelper {

    private static final String TAG = "IntruderSelfieCamera";
    private final Context context;
    private ImageCapture imageCapture;
    private final ExecutorService cameraExecutor;
    private final LifecycleOwner lifecycleOwner;

    public IntruderSelfieCameraHelper(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    /** Public entry point */
    public void takeIntruderSelfie() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                int rotation = getSafeDisplayRotation();

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(rotation)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setJpegQuality(90)
                        .build();

                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture);

                Log.d(TAG, "Camera bound. Taking picture...");
                capturePhoto();

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "CameraProvider error", e);
                postToast("Selfie capture failed.");
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Capture photo → save to app-private file → normalize rotation → optionally add to MediaStore.
     */
    private void capturePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture is null");
            return;
        }

        File photoDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IntruderSelfies");
        if (!photoDir.exists()) photoDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File photoFile = new File(photoDir, "INTRUDER_SELFIE_" + timeStamp + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "Selfie saved: " + photoFile.getAbsolutePath());

                        // Fix orientation
                        fixImageRotation(photoFile);

                        // Insert into MediaStore (optional, for Gallery visibility)
                        addImageToGallery(photoFile);

                        postToast("Intruder selfie captured!");
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        postToast("Selfie capture failed.");
                    }
                }
        );
    }

    /** Apply orientation + mirror fix */
    private void fixImageRotation(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            if (bitmap == null) return;

            Bitmap rotatedBitmap = ImageRotationUtils.rotateBitmap(bitmap, orientation, true);

            FileOutputStream out = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            // Reset EXIF to normal
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
            exif.saveAttributes();

            Log.d(TAG, "Rotation fixed for selfie");
        } catch (Exception e) {
            Log.e(TAG, "Failed to fix rotation", e);
        }
    }

    /** Insert file into MediaStore so it shows in Gallery */
    private void addImageToGallery(File file) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ZSecurityIntruderSelfies");
            } else {
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            }

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bmp != null) bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert into MediaStore", e);
        }
    }

    // ------------- Legacy methods (kept intact) -------------

    /** Legacy: apply transform in memory */
    private Bitmap applyFrontCameraTransform(Bitmap input) {
        return ImageRotationUtils.rotateBitmap(input, ExifInterface.ORIENTATION_NORMAL, true);
    }

    /** Legacy: save a bitmap directly */
    @SuppressLint("NewApi")
    private void saveImage(Bitmap bitmap) {
        if (bitmap == null) return;
        File photoDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IntruderSelfies");
        if (!photoDir.exists()) photoDir.mkdirs();
        File file = new File(photoDir, "LEGACY_SELFIE_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (Exception e) {
            Log.e(TAG, "Legacy save failed", e);
        }
    }

    private int getSafeDisplayRotation() {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                @SuppressWarnings("deprecation")
                android.view.Display d = wm.getDefaultDisplay();
                if (d != null) return d.getRotation();
            }
        } catch (Exception ignore) {}
        return Surface.ROTATION_0;
    }

    private void postToast(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
    }
}
