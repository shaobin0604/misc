package com.pekall.pctool.util;

import android.os.Environment;
import android.text.TextUtils;

import org.jboss.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageUtil {
    
    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static File getExternalPictureDir() {
        File externalDir = Environment.getExternalStorageDirectory();
        return new File(externalDir, "Pictures");
    }
    
    public static String absolutePathToRelativePath(String absolutePath) {
        File externalDir = Environment.getExternalStorageDirectory();
        return absolutePath.replace(externalDir.getAbsolutePath(), "");
    }
    
    /**
     * Generate upload filename, if target file exist, append timestamp
     * 
     * @param dir
     * @param filename
     * @return
     */
    public static File generateDstFileName(File dir, String filename) {
        File dst = new File(dir, filename);
        
        if (!dst.exists()) {
            return dst;
        }
        
        // file exist, append timestamp
        
        if (!TextUtils.isEmpty(filename)) {
            int idxForExt = filename.lastIndexOf('.');
            if (idxForExt > -1 && idxForExt < filename.length() - 1) {
                
                // has extension
                
                String ext = filename.substring(idxForExt + 1);
                String filenameWithoutExt = filename.substring(0, idxForExt);
                
                return new File(dir, String.format("%s-%d.%s", filenameWithoutExt, System.currentTimeMillis(), ext));
            } else {
                
                return new File(dir, String.format("%s-%d", filename, System.currentTimeMillis()));
            }
        } else {
            return new File(dir, filename);
        }
    }
    
    /**
     * Generate upload filename, if target file exist, append index 
     * 
     * @param dir
     * @param filename
     * @return
     */
    public static File generateDstFileNameExt(File dir, String filename) {
        File dst = new File(dir, filename);
        
        if (!dst.exists()) {
            return dst;
        }
        
        int appendIndex = 1;
        String newFilename = null;
        String[] filenameParts = splitFilenameAndExt(filename);
        boolean hasExt = (filenameParts != null);
        
        final ExistFilenameFilter existFilenameFilter = new ExistFilenameFilter(filename);
        String[] files = dir.list(existFilenameFilter);
        
        if (files != null && files.length > 0) {
            Arrays.sort(files);
            String lastFile = files[files.length - 1];
            
            Pattern pattern = existFilenameFilter.getPattern();
            
            Matcher matcher = pattern.matcher(lastFile);
            
            if (matcher.matches()) {
                String indexStr = matcher.group(1);
                
                appendIndex = Integer.valueOf(indexStr) + 1;
            }
        }
        
        if (hasExt) {
            newFilename = String.format("%s(%d).%s", filenameParts[0], appendIndex, filenameParts[1]); 
        } else {
            newFilename = String.format("%s(%d)", filename, appendIndex);
        }
        
        return new File(dir, newFilename);
    }
    
    /**
     * Split filename to filename without ext and ext
     * 
     * @param filename
     * @return two elements String array {filenameWithoutExt, ext}, or null if filename does not has ext
     */
    private static String[] splitFilenameAndExt(String filename) {
        int idxForExt = filename.lastIndexOf('.');
        if (idxForExt > -1 && idxForExt < filename.length() - 1) {
            // has extension
            String ext = filename.substring(idxForExt + 1);
            String filenameWithoutExt = filename.substring(0, idxForExt);
            
            return new String[] {filenameWithoutExt, ext};
        } else {
            return null;
        }
        
    }
    
    private static class ExistFilenameFilter implements FilenameFilter {
        
        private Pattern mFilenamePattern;
        
        public ExistFilenameFilter(String filename) {
            String[] filenameParts = splitFilenameAndExt(filename);
            
            if (filenameParts != null) {
                String filenameWithoutExt = filenameParts[0];
                String ext = filenameParts[1];
                
                mFilenamePattern = Pattern.compile(filenameWithoutExt + "\\((\\d+)\\)." + ext);
            } else {
                mFilenamePattern = Pattern.compile(filename + "\\((\\d+)\\)");
            }
        }

        @Override
        public boolean accept(File dir, String filename) {
            Matcher matcher = mFilenamePattern.matcher(filename);
            return matcher.matches();
        }
        
        public Pattern getPattern() {
            return mFilenamePattern;
        }
        
    }
    
    
}
