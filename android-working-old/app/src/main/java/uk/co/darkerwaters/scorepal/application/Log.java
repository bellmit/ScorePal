package uk.co.darkerwaters.scorepal.application;

import uk.co.darkerwaters.scorepal.settings.Settings;

public class Log {
    public static final String K_APPLICATION = "Scorepal";
    private static final String K_SEP = ": ";

    private static final Object lock = new Object();
    private static Log log = null;
    private final Application application;

    public static Log CreateLog(Application app) {
        synchronized (lock) {
            log = new Log(app);
        }
        return log;
    }

    private Log(Application application) {
        this.application = application;
    }

    private static boolean isLogging() {
        boolean isLogging = true;
        synchronized (lock) {
            if (null != log && null != log.application) {
                // have created the class, check the settings
                Settings settings = log.application.getSettings();
                if (null != settings) {
                    isLogging = log.application.getSettings().isLogging();
                }
            }
        }
        return isLogging;
    }

    public static void error(String explanation, Exception contents) {
        if (isLogging()) {
            android.util.Log.e(K_APPLICATION, explanation + K_SEP + contents.getMessage());
        }
    }

    public static void error(Exception contents) {
        if (isLogging()) {
            android.util.Log.e(K_APPLICATION, contents.getMessage());
        }
    }

    public static void error(String contents) {
        if (isLogging()) {
            android.util.Log.e(K_APPLICATION, contents);
        }
    }

    public static void debug(String explanation, Exception contents) {
        if (isLogging()) {
            android.util.Log.d(K_APPLICATION, explanation + K_SEP + contents.getMessage());
        }
    }

    public static void debug(Exception contents) {
        if (isLogging()) {
            android.util.Log.d(K_APPLICATION, contents.getMessage());
        }
    }

    public static void debug(String contents) {
        if (isLogging()) {
            android.util.Log.d(K_APPLICATION, contents);
        }
    }

    public static void info(String explanation, Exception contents) {
        if (isLogging()) {
            android.util.Log.i(K_APPLICATION, explanation + K_SEP + contents.getMessage());
        }
    }

    public static void info(Exception contents) {
        if (isLogging()) {
            android.util.Log.i(K_APPLICATION, contents.getMessage());
        }
    }

    public static void info(String contents) {
        if (isLogging()) {
            android.util.Log.i(K_APPLICATION, contents);
        }
    }

    public static void verbose(String explanation, Exception contents) {
        if (isLogging()) {
            android.util.Log.v(K_APPLICATION, explanation + K_SEP + contents.getMessage());
        }
    }

    public static void verbose(Exception contents) {
        if (isLogging()) {
            android.util.Log.v(K_APPLICATION, contents.getMessage());
        }
    }

    public static void verbose(String contents) {
        if (isLogging()) {
            android.util.Log.v(K_APPLICATION, contents);
        }
    }
}
