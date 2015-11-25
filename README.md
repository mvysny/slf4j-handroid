# slf4j-android upgraded

Tired of slf4j-android not logging your debug messages? Tired of android logger hiding your UnknownHostException? Use this instead of the built-in slf4j-android logger.

## Using with your project

You do not need to install anything, just check out the sources and run
```sh
$ ./gradlew
```

This will install the `slf4j-handroid` library into your local m2 repository. Then,
just add the following gradle dependency to your Android project:

```
compile 'org.slf4j:slf4j-handroid:1.7.13'
```

Then, just update the `HandroidLoggerAdapter.DEBUG` field to appropriate value. Good practice is to log debug during development,
while not logging debug messages during production. You can achieve this by adding a constructor to your android.app.Application and:

```
HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG;
```

