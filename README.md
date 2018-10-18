# trippy
Trip Analysis Suite

## Install
```
git clone https://github.com/JeffersonLab/trippy.git
cd trippy
gradlew build
```
__Note__: Build script assumes you have access to build.acc.jlab.org.  If not you can modify build.gradle to get jmyapi off jitpack.io or the like.  An example is commented out it the file.  We can't use jitpack.io because our proxy blocks it.
__Note__: [JLab Proxy Settings](https://github.com/JeffersonLab/jmyapi/wiki/JLab-Proxy)

## Run

Currently trippy only supports computing the time and duration of hall restore after trips and dumps to an Excel file.   Run with:

```
gradlew restore -Parameters="2017-01-01T00:00:00,2019-01-01T00:00:00,trips.xlsx"
```

## See Also
[jmyapi](https://github.com/JeffersonLab/jmyapi)
