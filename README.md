# trippy
Trip Analysis Suite

## Install
```
git clone https://github.com/JeffersonLab/trippy.git
cd trippy
gradlew build
```
__Note__: Build script assumes you have access to build.acc.jlab.org.  If not, you can modify build.gradle to get jmyapi off jitpack.io or the like.  An example is commented out in the build.gradle file.  We can't use jitpack.io because our proxy blocks it.

__Note__: [JLab Proxy Settings](https://github.com/JeffersonLab/jmyapi/wiki/JLab-Proxy)

__Note__: You must configure jmyapi with config/credentials.properties and config/deployments.properties.

## Run

Currently trippy only supports computing the time and duration of hall restore after trips and dumps to an Excel file.   Run with:

```
gradlew restore -Parameters="2017-01-01T00:00:00,2019-01-01T00:00:00,trips.xlsx"
```
__Note__: The JLab Proxy can bite you even if you are simply running a task as Gradle can still attempt to connect to repos.  Your run command might actually look like:
```
gradlew -Dhttps.proxyHost=jprox.jlab.org -Dhttps.proxyPort=8081 -Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts restore -Parameters="2017-01-01T00:00:00,2019-01-01T00:00:00,trips.xlsx"
```

## See Also
[jmyapi](https://github.com/JeffersonLab/jmyapi)
