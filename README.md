[![Build Status](https://travis-ci.org/mvysny/slf4j-handroid.svg?branch=master)](https://travis-ci.org/mvysny/slf4j-handroid)
[![GitHub tag](https://img.shields.io/github/tag/mvysny/slf4j-handroid.svg)](https://github.com/mvysny/slf4j-handroid/tags)

# slf4j-android fixed

Tired of slf4j-android not logging your debug messages? Tired of android logger hiding your `UnknownHostException`
or other exceptions not appearing? Use this fork of the standard `slf4j-android` logger.

Features

* Shows DEBUG messages during the development: http://jira.qos.ch/browse/SLF4J-314
* Does not hide any exceptions, even exceptions hidden by buggy Android Studio 1.5. Fixes https://code.google.com/p/android/issues/detail?id=195164 https://code.google.com/p/android/issues/detail?id=194446 http://stackoverflow.com/questions/28897239/log-e-does-not-print-the-stack-trace-of-unknownhostexception
* Supports Crashlytics; just call `HandroidLoggerAdapter.enableLoggingToCrashlytics();` AFTER Crashlytics has been initialized in your App's `onCreate()` method. slf4-handroid will then
  automatically log using `Crashlytics.log()` and `Crashlytics.logException()` instead of using plain Android logger.

## Using with your project

slf4j-handroid is now at jcenter, so all you have to do is to add this to your project dependencies:
```groovy
dependencies {
  compile 'sk.baka.slf4j:slf4j-handroid:x.y.z'
}
```

> Note: check the latest version of slf4j-handroid from Git tag stated above.

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
Just make sure that the app name is 10 character long tops (since the log name limit is only 23 characters),
otherwise your class names will get chopped.

Unfortunately I can't take advantage of the fact that the tag text is no longer constrained to 23 characters on Androids
24 and newer, because in reality that is not true: see [Issue #2](../../issues/2) for details.

## Crashlytics

slf4j-handroid supports Crashlytics. Make sure to call `HandroidLoggerAdapter.enableLoggingToCrashlytics();` after the Crashlytics is initialized in your App:
```java
@Override
public void onCreate() {
    super.onCreate();
    Fabric.with(this, new Crashlytics());
    HandroidLoggerAdapter.enableLoggingToCrashlytics();
    ...
}
```
Unfortunately automatic detection of Crashlytics does not work because of https://github.com/mvysny/slf4j-handroid/issues/5

## License

Licensed under the [MIT License](https://opensource.org/licenses/MIT).

Copyright (c) 2004-2018 QOS.ch and Martin Vysny

All rights reserved.

Permission is hereby granted, free  of charge, to any person obtaining
a  copy  of this  software  and  associated  documentation files  (the
"Software"), to  deal in  the Software without  restriction, including
without limitation  the rights to  use, copy, modify,  merge, publish,
distribute,  sublicense, and/or sell  copies of  the Software,  and to
permit persons to whom the Software  is furnished to do so, subject to
the following conditions:

The  above  copyright  notice  and  this permission  notice  shall  be
included in all copies or substantial portions of the Software.
THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
