# slf4j-android upgraded

Tired of slf4j-android not logging your debug messages? Tired of android logger hiding your UnknownHostException or other exceptions not appearing? Use this instead of the built-in slf4j-android logger.

## Using with your project

You do not need to install anything, just check out the sources and run
```sh
$ ./gradlew
```

This will install the `slf4j-handroid` library into your local m2 repository. Then,
just add the following gradle dependency to your Android project:

```
repositories {
  ...
  mavenLocal()
}
dependencies {
  compile 'org.slf4j:slf4j-handroid:1.7.13'
}
```

Then, just update the `HandroidLoggerAdapter.DEBUG` field to appropriate value. Good practice is to log debug during development,
while not logging debug messages during production. You can achieve this by adding a constructor to your android.app.Application and:

```
HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG;
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

## Which issues does this library solve?

* Shows DEBUG messages during the development
* Does not hide any exceptions, even exceptions hidden by buggy Android Studio 1.5. Fixes https://code.google.com/p/android/issues/detail?id=195164 https://code.google.com/p/android/issues/detail?id=194446 http://stackoverflow.com/questions/28897239/log-e-does-not-print-the-stack-trace-of-unknownhostexception

