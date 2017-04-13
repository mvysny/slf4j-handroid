package org.slf4j.impl;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class handles three issues:
 * <ul>
 *     <li>It allows you to log or suppress DEBUG messages, simply by setting {@link #DEBUG} appropriately. See the field doc for more info.</li>
 *     <li>Logs ALL exceptions, including UnknownHostException and all exceptions caused by this exception. Android filters out any exceptions which were caused by UnknownHostException.
 *     See http://stackoverflow.com/questions/28897239/log-e-does-not-print-the-stack-trace-of-unknownhostexception for details.
 *     </li>
 *     <li>Process the exception message to allow it to log on Android Studio 1.5. See https://code.google.com/p/android/issues/detail?id=194446 and https://code.google.com/p/android/issues/detail?id=194974</li>
 * </ul>
 * @author mvy
 */
public class HandroidLoggerAdapter extends AndroidLoggerAdapter {

    /**
     * True if the debug messages should be logged, false if not. Defaults to false.
     * <p></p>
     * Good practice is to log debug during development, while not logging debug messages during production.
     * Put this into your android.app.Application's constructor to achieve this:
     * <pre>
     * HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG;
     * </pre>
     */
    public static boolean DEBUG = false;

    /**
     * If true, the log messages are routed to the Crashlytics library. The value of this property is auto-detected - if
     * <code>com.crashlytics.android.Crashlytics</code> class is present, this is set to true. You can however override that as you see fit.
     * <p></p>
     * Warning: only exception stacktraces logged as WARNING or ERROR are logged into Crashlytics. See {@link #logInternal(int, String, Throwable)}
     * for details.
     */
    public static boolean LOG_TO_CRASHLYTICS;
    private static Method crashlyticsLog;
    private static Method crashlyticsLogException;
    static {
        try {
            final Class<?> crashlyticsClass = Class.forName("com.crashlytics.android.Crashlytics");
            try {
                // yes I know, reflection is slower. Yet crashlytics doesn't seem to provide jars, only aar which I can't link against.
                crashlyticsLog = crashlyticsClass.getDeclaredMethod("log", int.class, String.class, String.class);
                crashlyticsLogException = crashlyticsClass.getDeclaredMethod("logException", Throwable.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            LOG_TO_CRASHLYTICS = true;
            System.out.println("slf4j-handroid: enabling integration with Crashlytics, override by setting HandroidLoggerAdapter.LOG_TO_CRASHLYTICS to false");
        } catch (ClassNotFoundException e) {
            LOG_TO_CRASHLYTICS = false;
        }
    }

    HandroidLoggerAdapter(String tag) {
        super(tag);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable. As opposed to Android logging mechanism, it logs even UnknownHostExceptions.
     * @param tr An exception to log
     */
    @NotNull
    public static String getStackTraceString(@NotNull Throwable tr) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    @Override
    protected boolean isLoggable(int priority) {
        return DEBUG ? true : super.isLoggable(priority);
    }

    @Override
    protected void logInternal(int priority, String message, Throwable throwable) {
        if (throwable != null) {
            message += '\n' + getStackTraceString(throwable);
        }
        message = postprocessMessage(message).trim();
        if (LOG_TO_CRASHLYTICS) {
            // this also internally calls Log.println(), so no need to do it ourselves
            try {
                crashlyticsLog.invoke(null, priority, name, message);
                if (priority >= Log.WARN && throwable != null) {
                    crashlyticsLogException.invoke(null, throwable);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.println(priority, name, message);
        }
    }

    @NotNull
    private static String postprocessMessage(@NotNull String message) {
        // we need to do the following, to work around Android Studio 1.5 bugs:
        // 1. remove all characters with code point 0..31 (for example \r) - if those characters are present in the message, the message is not simply logged at all by Android (!!!)
        // see https://code.google.com/p/android/issues/detail?id=194446
        // see https://code.google.com/p/android/issues/detail?id=194974
        // 2. remove two or more consecutive \n: https://code.google.com/p/android/issues/detail?id=195164
        final StringBuilder sb = new StringBuilder(message.length());
        boolean lastCharWasNewLine = false;
        for (int i = 0; i < message.length(); i++) {
            final char c = message.charAt(i);
            if (c == '\n' && lastCharWasNewLine) {
                // do nothing
            } else if (c >= 32 || c == '\n') {
                sb.append(c);
            } else {
                sb.append(' ');
            }
            lastCharWasNewLine = c == '\n';
        }
        return sb.toString();
    }
}
