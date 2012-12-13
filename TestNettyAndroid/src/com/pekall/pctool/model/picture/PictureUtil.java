
package com.pekall.pctool.model.picture;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;

import com.pekall.pctool.util.Settings;
import com.pekall.pctool.util.Slog;
import com.pekall.pctool.util.StorageUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class PictureUtil {

    public static class PictureNotExistException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String mPath;

        public PictureNotExistException(String path) {
            super();
            mPath = path;
        }

        @Override
        public String toString() {
            return "PictureNotExistException [mPath=" + mPath + "]";
        }
    }

    public static final int QUERY_LIMIT_NULL = 0;

    public static void scanPicture(Context context, String absolutePath) {
        // Uri uri = Uri.parse("file://" + absolutePath);

        String path = absolutePath;
        if (absolutePath.startsWith("/")) {
            path = absolutePath.substring(1);
        }

        Uri.Builder builder = new Uri.Builder();

        builder.scheme("file");
        builder.appendPath(path);

        Uri uri = builder.build();

        Slog.d("uri = " + uri);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    /**
     * Delete picture with the specified id
     * 
     * @param context
     * @param id
     * @return
     */
    public static boolean deletePicture(Context context, long id) {
        Uri deleteUri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id);
        int affectedRows = context.getContentResolver().delete(deleteUri, null, null);
        return affectedRows > 0;
    }

    public static boolean deletePictures(Context context, List<Long> ids) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        int count = 0;
        for (int i = 0; i < ids.size(); i++) {
            if (count == Settings.MAX_CONTENT_PROVIDER_OPERATION_COUNT) {
                try {
                    context.getContentResolver().applyBatch(MediaStore.AUTHORITY, ops);
                    count = 0;
                    ops.clear();
                } catch (RemoteException e) {
                    Slog.e("Error when deletePictures", e);
                    return false;
                } catch (OperationApplicationException e) {
                    Slog.e("Error when deletePictures", e);
                    return false;
                }
            }
            long id = ids.get(i);
            ops.add(ContentProviderOperation.newDelete(
                    ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id))
                    .withYieldAllowed(true).build());
            count++;
        }
        
        try {
            context.getContentResolver().applyBatch(MediaStore.AUTHORITY, ops);
            return true;
        } catch (RemoteException e) {
            Slog.e("Error when deletePictures", e);
            return false;
        } catch (OperationApplicationException e) {
            Slog.e("Error when deletePictures", e);
            return false;
        }
    }

    public static List<Picture> queryPictures(Context context) {
        QueryPictureResult result = queryPicturesWithRange(context, 0, QUERY_LIMIT_NULL);

        if (result == null) {
            return null;
        } else {
            return result.getPictures();
        }
    }

    public static InputStream getPictureStream(Context context, long id, StringBuilder /* out */outMimeType)
            throws PictureNotExistException {
        String path = queryPicturePath(context, id, outMimeType);

        if (TextUtils.isEmpty(path)) {
            throw new PictureNotExistException(path);
        }

        try {
            FileInputStream fis = new FileInputStream(path);
            return fis;
        } catch (FileNotFoundException e) {
            Slog.e("Error: file not found " + path, e);
            throw new PictureNotExistException(path);
        }
    }

    private static String queryPicturePath(Context context, long id, StringBuilder /* out */outMimeType) {
        String[] projection = {
                ImageColumns.DATA,
                ImageColumns.MIME_TYPE,
        };

        final Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);

        if (cursor == null) {
            Slog.e("Error: queryPicturePath cursor is null");
            return null;
        }

        if (cursor.moveToFirst()) {
            try {
                String path = cursor.getString(0); // ImageColumns.DATA
                String mimeType = cursor.getString(1); // ImageColumns.MIME_TYPE

                outMimeType.setLength(0);
                outMimeType.append(mimeType);

                return path;
            } finally {
                cursor.close();
            }
        } else {
            return null;
        }
    }

    /**
     * Query pictures with offset and limit
     * 
     * @param context
     * @param offset must larger than -1, if offset > total count, empty result
     *            is returned
     * @param limit the max count to query, or <b>0</b> if does not need limit
     * @return the {@link QueryPictureResult} or null if error occurred
     */
    public static QueryPictureResult queryPicturesWithRange(Context context, int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset should be greater than -1");
        }

        if (limit < 0) {
            throw new IllegalArgumentException("limit should be greater than -1");
        }

        String[] projection = {
                ImageColumns._ID,
                ImageColumns.TITLE,
                ImageColumns.DISPLAY_NAME,
                ImageColumns.MIME_TYPE,
                ImageColumns.DATE_TAKEN,
                ImageColumns.SIZE,
                ImageColumns.DATA,
                ImageColumns.BUCKET_DISPLAY_NAME,
        };

        // String selection = ImageColumns.MIME_TYPE + "= ?";
        // String[] selectionArgs = {
        // "image/jpeg"
        // };

        String selection = null;
        String[] selectionArgs = null;

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                selection, selectionArgs, null);

        if (cursor == null) {
            Slog.e("Error: queryPictures cursor is null");
            return null;
        }

        int resultCount = 0;
        int totalCount = cursor.getCount();
        
        Slog.d("total count = " + totalCount);

        QueryPictureResult queryResult = new QueryPictureResult(offset, limit, totalCount);

        try {
            if (cursor.moveToPosition(offset)) {
                final int idxForId = cursor.getColumnIndex(ImageColumns._ID);
                final int idxForTitle = cursor.getColumnIndex(ImageColumns.TITLE);
                final int idxForDisplayName = cursor.getColumnIndex(ImageColumns.DISPLAY_NAME);
                final int idxForMimeType = cursor.getColumnIndex(ImageColumns.MIME_TYPE);
                final int idxForDateTaken = cursor.getColumnIndex(ImageColumns.DATE_TAKEN);
                final int idxForSize = cursor.getColumnIndex(ImageColumns.SIZE);
                final int idxForData = cursor.getColumnIndex(ImageColumns.DATA);
                final int idxforBucketDisplayName = cursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME);

                do {
                    queryResult.addPicture(
                            new Picture(cursor.getLong(idxForId),
                                    cursor.getString(idxForTitle),
                                    cursor.getString(idxForDisplayName),
                                    cursor.getString(idxForMimeType),
                                    cursor.getLong(idxForDateTaken),
                                    cursor.getLong(idxForSize),
                                    StorageUtil.absolutePathToRelativePath(cursor.getString(idxForData)),
                                    cursor.getString(idxforBucketDisplayName))
                            );

                    resultCount++;

                    if (limit > 0 && resultCount >= limit) {
                        break;
                    }

                } while (cursor.moveToNext());

            }
            return queryResult;
        } finally {
            cursor.close();
        }
    }
}
