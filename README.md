# LogParser



## General info
Takes a file with JSON objects, parses it and saves into HSQLDB. 

## Setup

Build it first with Maven:

```
$ mvn install
```

Example of applicable file with JSON objects: logfile.txt


To run the app, you need to include path to the logfile. 

Example below:

```
$ java -jar .\target\LogParser-1.0-SNAPSHOT.jar "C:\logfile.txt"
```
