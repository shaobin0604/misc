
package com.pekall.pctool.model.mms;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.mms.Mms.Attachment;
import com.pekall.pctool.model.mms.Mms.Slide;

public class MmsUtil {

    static final String TAG = "pekall android pc suite - mms functions";

    static final Uri MAIN_MMS_URI = Uri.parse("content://mms/");
    static final Uri INBOX_URI = Uri.parse("content://mms/inbox");
    static final Uri SENT_URI = Uri.parse("content://mms/sent");
    static final Uri OUTBOX_URI = Uri.parse("content://mms/outbox");
    static final Uri DRAFT_URI = Uri.parse("content://mms/draft");

	private static final String MMS_AUTHORITY = "mms";

    static Slide slideForParse;
    static ArrayList<String> slideTxtFileNames;


    public static ArrayList<Mms> query(Context cxt, MmsBox whichBox) {
        ArrayList<Mms> mmsList = new ArrayList<Mms>();
        Cursor cursor = cxt.getContentResolver().query(mapBoxUri(whichBox), new String[] {
                "_id", "sub", "date", "read"
        }, null, null, "_id desc");

        while (cursor.moveToNext()) {
            Mms mms = new Mms();

            mms.rowId = cursor.getLong(cursor.getColumnIndex("_id"));

            Cursor cursorAddress = cxt.getContentResolver().query(Uri.parse("content://mms/" + mms.rowId + "/addr"),
                    null, null, null, null);
            if (cursorAddress.moveToFirst())
                mms.phoneNum = cursorAddress.getString(cursorAddress.getColumnIndex("address"));
            cursorAddress.close();

            String str = cursor.getString(cursor.getColumnIndex("sub"));
            if (str != null) {
                try {
                    str = new String(str.getBytes("ISO-8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            mms.subject = str;

            mms.date = cursor.getLong(cursor.getColumnIndex("date"));
            mms.isReaded = cursor.getLong(cursor.getColumnIndex("read"));

            /** get smil.xml **/
            Cursor cursorSmil = cxt.getContentResolver().query(makeIdPartUri(mms.rowId), new String[] {
                "text"
            }, "ct='application/smil'", null, null);
            String smilContent = "";
            if (cursorSmil.moveToFirst()) {
                smilContent = cursorSmil.getString(cursorSmil.getColumnIndex("text"));
                // Log.e("", "wta = " + smilContent);
            } else {
                Log.e("", "this mms don't have smil.xml");
            }
            cursorSmil.close();

            /** parse smil.xml and fill slide contents **/
            parseSmilToSlide(cxt, smilContent, mms);

            /** get attachments **/
            Cursor cursorPart = cxt.getContentResolver().query(makeIdPartUri(mms.rowId), null, null, null, null);
            while (cursorPart.moveToNext()) {
                Attachment attachment = new Attachment();

                long partRowId = cursorPart.getLong(cursorPart.getColumnIndex("_id"));
                String name = cursorPart.getString(cursorPart.getColumnIndex("cl"));
                String type = cursorPart.getString(cursorPart.getColumnIndex("ct"));
                // TODO
                if (name == null)
                    name = "";

                if (!type.equalsIgnoreCase("application/smil")
                        && (name != null && !name.endsWith(".txt") || !slideTxtFileNames.contains(name))) {
                    attachment.fileBytes = readAttachments(cxt, partRowId);
                    if (type.startsWith("image/")) {
                        String imageSuffix = type.substring("image/".length());
                        int i = name.lastIndexOf(".");
                        /** if no suffix name, manually add one **/
                        attachment.name = (i == -1) ? (name + "." + imageSuffix)
                                : (name.substring(0, i) + "." + imageSuffix);
                    } else {
                        attachment.name = name;
                    }
                    mms.attachments.add(attachment);
                }

            }
            cursorPart.close();

            mmsList.add(mms);
        } // << end while >>

        cursor.close();

        return mmsList;
    }

    public static boolean delete(Context cxt, long rowId) {
        return cxt.getContentResolver().delete(MAIN_MMS_URI, "_id=" + rowId, null) == 1;
    }

    public static boolean delete(Context context, List<Long> rowIds) {
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (long rowId : rowIds) {
            ops.add(ContentProviderOperation.newDelete(Uri.parse("content://mms/" + rowId)).build());
        }
        try {
            context.getContentResolver().applyBatch(MMS_AUTHORITY, ops);
            return true;
        } catch (RemoteException e) {
            Slog.e("Error when deleteMms", e);
        } catch (OperationApplicationException e) {
            Slog.e("Error when deleteMms", e);
        }

        return false;
    }

    private static Uri mapBoxUri(MmsBox mmsBox) {
        switch (mmsBox) {
            case MAIN:
                return MAIN_MMS_URI;
            case INBOX:
                return INBOX_URI;
            case SENT:
                return SENT_URI;
            case OUTBOX:
                return OUTBOX_URI;
            case DRAFT:
                return DRAFT_URI;
            default:
                return null;
        }
    }

    private static Uri makeIdPartUri(long id) {
        return Uri.parse("content://mms/" + id + "/part");
    }

    private static Uri makePartIdUri(long id) {
        return Uri.parse("content://mms/part/" + id);
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private static void parseSmilToSlide(final Context cxt, String smilContent, final Mms mms) {
        slideTxtFileNames = new ArrayList<String>();

        RootElement root = new RootElement("smil");
        Element par = root.getChild("body").getChild("par");

        par.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attrs) {
                slideForParse = new Slide(parseDuration(attrs.getValue("dur")));
            }
        });
        par.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                mms.slides.add(slideForParse);
            }
        });

        par.getChild("text").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attrs) {
                String txtFileName = attrs.getValue("src");
                slideTxtFileNames.add(txtFileName);
                Cursor cursorPart = cxt.getContentResolver().query(makeIdPartUri(mms.rowId), new String[] {
                    "text"
                }, "cl='" + txtFileName + "'", null, null);
                cursorPart.moveToFirst();
                slideForParse.text = cursorPart.getString(cursorPart.getColumnIndex("text"));
                cursorPart.close();
            }
        });

        par.getChild("img").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attrs) {
                slideForParse.image = readToBytes(cxt, mms, attrs);
            }
        });

        par.getChild("audio").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attrs) {
                slideForParse.audio = readToBytes(cxt, mms, attrs);
            }
        });

        par.getChild("video").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attrs) {
                slideForParse.video = readToBytes(cxt, mms, attrs);
            }
        });

        try {
            Xml.parse(smilContent, root.getContentHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parseDuration(String str) {
        if (!Character.isDigit(str.charAt(0))) {
            Log.e("", "duration 1st char is not digit");
            return 0;
        } else {
            int i = 1;
            while (Character.isDigit(str.charAt(i++)))
                ;
            if (str.substring(i - 1).equalsIgnoreCase("ms")) {
                return Integer.parseInt(str.substring(0, i - 1));
            } else if (str.substring(i - 1).equalsIgnoreCase("s")) {
                return Integer.parseInt(str.substring(0, i - 1)) * 1000;
            } else {
                Log.e("", "invalid duration measurement");
                return 0;
            }
        }
    }

    private static byte[] readToBytes(Context cxt, Mms mms, Attributes attrs) {
        byte[] bytes = null;
        String fileName = attrs.getValue("src");
        Cursor cursorPart = cxt.getContentResolver().query(makeIdPartUri(mms.rowId), null,
                "cl='" + fileName + "' OR name='" + fileName + "'", null, null);
        if (cursorPart.moveToFirst()) {
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            try {
                is = cxt.getContentResolver().openInputStream(
                        makePartIdUri(cursorPart.getLong(cursorPart.getColumnIndex("_id"))));
                baos = new ByteArrayOutputStream();
                byte buf[] = new byte[1024];
                int numRead = 0;
                while ((numRead = is.read(buf)) != -1)
                    baos.write(buf, 0, numRead);
                bytes = baos.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                close(is);
                close(baos);
            }
        }
        cursorPart.close();
        return bytes;
    }

    private static byte[] readAttachments(Context cxt, long partRowId) {
        byte[] bytes = null;
        Uri partIdUri = makePartIdUri(partRowId);
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = cxt.getContentResolver().openInputStream(partIdUri);
            baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int numRead = 0;
            while ((numRead = is.read(buf)) != -1)
                baos.write(buf, 0, numRead);
            bytes = baos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            close(is);
            close(baos);
        }
        return bytes;
    }

    enum MmsBox {
        MAIN, INBOX, SENT, OUTBOX, DRAFT;
    }

}
