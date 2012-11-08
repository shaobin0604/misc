
package com.pekall.pctool.model.picture;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;

import com.pekall.pctool.util.Slog;

import java.util.ArrayList;
import java.util.List;

public class PictureUtil {

    public static List<Picture> queryPictures(Context context) {
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
            Slog.e("queryPictures cursor is null");
            return null;
        }

        Slog.d("cursor count = " + cursor.getCount());

        List<Picture> pictures = new ArrayList<Picture>();

        try {
            if (cursor.moveToNext()) {
                final int idxForId = cursor.getColumnIndex(ImageColumns._ID);
                final int idxForTitle = cursor.getColumnIndex(ImageColumns.TITLE);
                final int idxForDisplayName = cursor.getColumnIndex(ImageColumns.DISPLAY_NAME);
                final int idxForMimeType = cursor.getColumnIndex(ImageColumns.MIME_TYPE);
                final int idxForDateTaken = cursor.getColumnIndex(ImageColumns.DATE_TAKEN);
                final int idxForSize = cursor.getColumnIndex(ImageColumns.SIZE);
                final int idxForData = cursor.getColumnIndex(ImageColumns.DATA);
                final int idxforBucketDisplayName = cursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME);

                do {
                    pictures.add(
                            new Picture(cursor.getLong(idxForId),
                                    cursor.getString(idxForTitle),
                                    cursor.getString(idxForDisplayName),
                                    cursor.getString(idxForMimeType),
                                    cursor.getLong(idxForDateTaken),
                                    cursor.getLong(idxForSize),
                                    cursor.getString(idxForData),
                                    cursor.getString(idxforBucketDisplayName))
                            );
                } while (cursor.moveToNext());

            }
            return pictures;
        } finally {
            cursor.close();
        }
    }
}
