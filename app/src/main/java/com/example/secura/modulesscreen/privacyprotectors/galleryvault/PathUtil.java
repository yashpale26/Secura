package com.example.secura.modulesscreen.privacyprotectors.galleryvault;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

public class PathUtil {

    private static final String TAG = "PathUtil";

    public static String getPath(final Context context, final Uri uri) {
        Log.d(TAG, "Attempting to get path for URI: " + uri.toString());

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    String path = Environment.getExternalStorageDirectory() + "/" + split[1];
                    Log.d(TAG, "ExternalStorageDocument primary path: " + path);
                    return path;
                } else {
                    Log.w(TAG, "ExternalStorageDocument non-primary type: " + type);
                    return null;
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (id != null && id.startsWith("raw:")) {
                    String rawPath = id.substring(4);
                    Log.d(TAG, "DownloadsDocument raw path: " + rawPath);
                    return rawPath;
                }

                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                String dataColumn = getDataColumn(context, contentUri, null, null);
                Log.d(TAG, "DownloadsDocument contentUri path: " + dataColumn);
                return dataColumn;
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                String dataColumn = getDataColumn(context, contentUri, selection, selectionArgs);
                Log.d(TAG, "MediaDocument path: " + dataColumn);
                return dataColumn;
            }
        }
        // MediaStore (and general content Uris)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                Log.d(TAG, "Google Photos URI - returning null as direct file path is not available: " + uri.getLastPathSegment());
                return null;
            }

            String dataColumn = getDataColumn(context, uri, null, null);
            Log.d(TAG, "Content URI path: " + dataColumn);
            return dataColumn;
        }
        // File URI
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            String path = uri.getPath();
            Log.d(TAG, "File URI path: " + path);
            return path;
        }

        Log.w(TAG, "Cannot get path for URI: " + uri.toString());
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException in getDataColumn: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}