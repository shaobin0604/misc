package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.model.picture.Picture;
import com.pekall.pctool.model.picture.PictureUtil;
import com.pekall.pctool.util.Slog;

import java.util.List;

public class PictureUtilTestCase extends AndroidTestCase {
    
    public void testQueryPictures() throws Exception {
        List<Picture> pictures = PictureUtil.queryPictures(getContext());
        
        for (Picture picture : pictures) {
            Slog.d(picture.toString());
        }
        
    }
}
