package android.util;

public class Log {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    
    
    public static boolean isLoggable(String tag, int level) {
        return true;
    }

    public static void v(String tag, String msg) {
        System.out.println("[" + tag + "] " + msg);
    }
    
    public static void d(String tag, String msg) {
        System.out.println("[" + tag + "] " + msg);
    }

    public static void i(String tag, String msg) {
        System.out.println("[" + tag + "] " + msg);
    }
    
    public static void w(String tag, String msg) {
        System.out.println("[" + tag + "] " + msg);
    }
    
    public static void w(String tag, String msg, Exception e) {
        System.out.println("[" + tag + "] " + msg + "e: " + e.toString());
    }
    
    public static void e(String tag, String msg) {
        System.out.println("[" + tag + "] " + msg);
    }
    
    public static void e(String tag, String msg, Exception e) {
        System.out.println("[" + tag + "] " + msg + "e: " + e.toString());
    }
}
