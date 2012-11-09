package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.model.picture.Picture;
import com.pekall.pctool.model.picture.PictureUtil;
import com.pekall.pctool.model.picture.QueryPictureResult;
import com.pekall.pctool.util.Slog;

import java.util.List;

public class PictureUtilTestCase extends AndroidTestCase {
    
    public void testQueryPictures() throws Exception {
        List<Picture> pictures = PictureUtil.queryPictures(getContext());
        
        for (Picture picture : pictures) {
            Slog.d(picture.toString());
        }
    }
    
    public void testQueryPicturesWithOffsetLimit() throws Exception {
        QueryPictureResult result = PictureUtil.queryPicturesWithOffsetLimit(getContext(), 0, PictureUtil.QUERY_LIMIT_NULL);
        
        Slog.d("result count = " + result.getPictures().size());
        Slog.d("total count = " + result.getTotalCount());
    }
}
