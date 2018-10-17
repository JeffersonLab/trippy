# trippy
Trip Analysis Suite

## Install
```
git clone https://github.com/JeffersonLab/trippy.git
cd trippy
gradlew build
```

__Note__: If you are behind a firewall with a proxy server you might need to setup proxy settings.  For git clone to work at Jefferson Lab on an ACE Linux workstation with a tcsh shell you could execute:

```
setenv https_proxy jprox.jlab.org:8081
```

Now for gradlew build to work it executes Java, which needs to (1) connect to Internet and (2) trust SSL certificates.  At Jefferson Lab on a Linux workstation you could execute:

```
gradlew -Dhttps.proxyHost=jprox.jlab.org -Dhttps.proxyPort=8081 -Djavax.net.ssl.trustStore=/etc/pki/ca-trust/extracted/java/cacerts build
```

## Run

Currently trippy only supports computing the time and duration of hall restore after trips and dumps to an Excel file.   Run with:

```
gradlew restore -Parameters="2017-01-01T00:00:00,2019-01-01T00:00:00,trips.xlsx"
```
