/*
 * Copyright (c) 2004-2013 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AndroidLoggerFactory is an implementation of {@link ILoggerFactory} returning
 * the appropriately named {@link AndroidLoggerFactory} instance.
 *
 * @author Andrey Korzhevskiy <a.korzhevskiy@gmail.com>
 */
class AndroidLoggerFactory implements ILoggerFactory {
    static final String ANONYMOUS_TAG = "null";

    /**
     * Returns maximum tag name for Android. Androids 25 and earlier would fail if the tag name was longer than 23 characters.
     * See https://github.com/mvysny/slf4j-handroid/issues/2 for more details.
     * @return 23 on Androids 25 and lower, 1000 on Androids 26 and higher.
     */
    private static int getTagMaxLength() {
        return HandroidLoggerAdapter.ANDROID_API_LEVEL >= 26 ? 1000 : 23;
    }

    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<String, Logger>();

    /**
     * Return an appropriate {@link AndroidLoggerAdapter} instance by name.
     */
    public Logger getLogger(String name) {
        String tag = loggerNameToTag(name);
        Logger logger = loggerMap.get(tag);
        if (logger == null) {
            Logger newInstance = new HandroidLoggerAdapter(tag);
            Logger oldInstance = loggerMap.putIfAbsent(tag, newInstance);
            logger = oldInstance == null ? newInstance : oldInstance;
        }
        return logger;
    }

    /**
     * Tag names cannot be longer than {@link #getTagMaxLength} characters on Android platform.
     *
     * Returns the short logger tag (up to {@link #getTagMaxLength} characters) for the given logger name.
     * Traditionally loggers are named by fully-qualified Java classes; this
     * method attempts to return a concise identifying part of such names.
     *
     * See also:
     * android/system/core/include/cutils/property.h
     * android/frameworks/base/core/jni/android_util_Log.cpp
     * dalvik.system.DalvikLogging
     *
     */
    static String loggerNameToTag(String loggerName) {
        // Anonymous logger
        if (loggerName == null) {
            return ANONYMOUS_TAG;
        }

        final int length = loggerName.length();
        final int tagMaxLength = getTagMaxLength();
        if (length <= tagMaxLength && HandroidLoggerAdapter.APP_NAME == null) {
            return loggerName;
        }

        if (HandroidLoggerAdapter.APP_NAME != null) {
            final int lastDot = loggerName.lastIndexOf('.');
            final String className = lastDot < 0 ? loggerName : loggerName.substring(lastDot + 1, length);
            String name = HandroidLoggerAdapter.APP_NAME + ":" + className;
            if (name.length() > tagMaxLength) {
                name = name.substring(0, tagMaxLength - 1) + '*';
            }
            return name;
        }

        int tagLength = 0;
        int lastTokenIndex = 0;
        int lastPeriodIndex;
        StringBuilder tagName = new StringBuilder(tagMaxLength + 3);
        while ((lastPeriodIndex = loggerName.indexOf('.', lastTokenIndex)) != -1) {
            tagName.append(loggerName.charAt(lastTokenIndex));
            // token of one character appended as is otherwise truncate it to one character
            int tokenLength = lastPeriodIndex - lastTokenIndex;
            if (tokenLength > 1) {
                tagName.append('*');
            }
            tagName.append('.');
            lastTokenIndex = lastPeriodIndex + 1;

            // check if name is already too long
            tagLength = tagName.length();
            if (tagLength > tagMaxLength) {
                return getSimpleName(loggerName);
            }
        }

        // Either we had no useful dot location at all
        // or last token would exceed TAG_MAX_LENGTH
        int tokenLength = length - lastTokenIndex;
        if (tagLength == 0 || (tagLength + tokenLength) > tagMaxLength) {
            return getSimpleName(loggerName);
        }

        // last token (usually class name) appended as is
        tagName.append(loggerName, lastTokenIndex, length);
        return tagName.toString();
    }

    private static String getSimpleName(String loggerName) {
        // Take leading part and append '*' to indicate that it was truncated
        int length = loggerName.length();
        int lastPeriodIndex = loggerName.lastIndexOf('.');
        return lastPeriodIndex != -1 && length - (lastPeriodIndex + 1) <= getTagMaxLength() ? loggerName.substring(lastPeriodIndex + 1) : '*' + loggerName
                        .substring(length - getTagMaxLength() + 1);
    }
}