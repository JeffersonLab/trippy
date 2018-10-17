# trippy
Trip Analysis Suite

## Install
```
git clone https://github.com/JeffersonLab/trippy.git
cd trippy
gradlew build
```

__Note__: If you are behind a firewall you might need to setup a proxy.  At Jefferson Lab on a Linux workstation with a tcsh shell you could execute:

```
setenv https_proxy jprox.jlab.org:8081
```

## Run

Currently trippy only supports computing the time and duration of hall restore after trips and dumps to an Excel file.   Run with:

```
gradlew restore -Parameters="2017-01-01T00:00:00,2019-01-01T00:00:00,trips.xlsx"
```
