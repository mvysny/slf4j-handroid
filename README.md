[![Build Status](https://travis-ci.org/mvysny/slf4j-handroid.svg?branch=master)](https://travis-ci.org/mvysny/slf4j-handroid)

# slf4j-android fixed

Tired of slf4j-android not logging your debug messages? Tired of android logger hiding your `UnknownHostException`
or other exceptions not appearing? Use this fork of the standard `slf4j-android` logger.

Features

* Shows DEBUG messages during the development: http://jira.qos.ch/browse/SLF4J-314
* Does not hide any exceptions, even exceptions hidden by buggy Android Studio 1.5. Fixes https://code.google.com/p/android/issues/detail?id=195164 https://code.google.com/p/android/issues/detail?id=194446 http://stackoverflow.com/questions/28897239/log-e-does-not-print-the-stack-trace-of-unknownhostexception
* Supports Crashlytics; when Crashlytics jar is included in your project then slf4-handroid will
  automatically log using `Crashlytics.log()` and `Crashlytics.logException()` instead of using plain Android logger.

## Using with your project

slf4j-handroid is now at jcenter, so all you have to do is to add this to your project dependencies:
```groovy
dependencies {
  compile 'sk.baka.slf4j:slf4j-handroid:1.7.25-2'
}
```

If this won't work, add the jcenter repo:
```groovy
repositories {
    jcenter()
}
```

Then, just update the `HandroidLoggerAdapter.DEBUG` field to appropriate value. Good practice is to log debug during development,
while not logging debug messages during production. You can achieve this by adding a constructor to your `android.app.Application` and:

```
HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG;
HandroidLoggerAdapter.ANDROID_API_LEVEL = Build.VERSION.SDK_INT;
HandroidLoggerAdapter.APP_NAME = "MyApp";
```

Then, replace all calls to Android built-in `Log` class by slf4j logging, for example:

```java
public class YourClass {
  private static final Logger log = LoggerFactory.getLogger(YourClass.class);
  public void foo() {
    log.error("Something failed", new RuntimeException("something"));
  }
}
```

Since you have configured the `APP_NAME`, the log messages will look like this:

```
06-18 13:05:35.937 17994-17994/sk.baka.aedictkanjidrawpractice I/MyApp:SodIndex: Parsed SOD header with 6576 kanjis in 18ms
06-18 13:05:36.011 17994-17994/sk.baka.aedictkanjidrawpractice I/MyApp:MainActivity: Launched MainActivity for kanji 政 with stroke count 9
```

(Except when you're running on phone with Android SDK 24 or higher, in that case the log name is not limited to 23 characters
and will show a full class name including the package, prefixed by `MyApp:`, as follows:

```
06-18 13:23:36.189 23900-23900/sk.baka.aedictkanjidrawpractice I/MyApp:sk.baka.aedictkanjidrawpractice.util.android.SodIndex: Parsed SOD header with 6576 kanjis in 24ms
06-18 13:23:36.237 23900-23900/sk.baka.aedictkanjidrawpractice I/MyApp:sk.baka.aedictkanjidrawpractice.MainActivity: Launched MainActivity for kanji 政 with stroke count 9
```

Thus, it is easy to filter out the log (just search for `"MyApp."` instead for that horrible `"s*.b*.h*.w*.Foo"`).

Just make sure that the app name is 10 character long tops (since the log name limit is only 23 characters),
otherwise your class names will get chopped.
