package org.slf4j.impl;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author mvy
 */
public class HandroidLoggerAdapter extends AndroidLoggerAdapter {

    /**
     * Put this into your android.app.Application's constructor:
     * <pre>
     * HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG;
     * </pre>
     */
    public static boolean DEBUG = false;

    HandroidLoggerAdapter(String tag) {
        super(tag);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable. As opposed to Android, it logs even UnknownHostExceptions.
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
        Log.println(priority, name, message);
    }

    @NotNull
    private static String postprocessMessage(@NotNull String message) {
        // we need to do the following, to work around Android stupidity:
        // 1. remove all characters with code point 0..31 (for example \r) - if those characters are present in the message, the message is not simply logged at all by Android (!!!)
        final StringBuilder sb = new StringBuilder(message.length());
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c >= 32) {
                sb.append(c);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
