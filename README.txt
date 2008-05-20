howto ibis deploy

1. get the files from /var/scratch/rkemp on the fs0
- lib.zip (containing the ibis deploy + javagat jars)
- javadoc.zip (containing the ibis deploy javadoc)
- sleep.zip (containing the application to run)
- test.zip (containing the sources of the test applications)
- log4j.properties
- das3.properties

2. suggested deployment of these files on your own computer:
your favourite dir contains:
 - lib (dir)
 - sleep (dir)
 - log4j.properties
 - das3.properties

NOTES: 
- You may want to use another deployment, in that case you've to change either the commandline as displayed in 6, or the source code of SleepTest application.
- Use the test.zip and javadoc.zip as references

5. make sure that you've a .ssh in you 'user.home' (on my Windows machine it's C:\Documents and Settings\rkemp\.ssh, you can print it in any Java program using: System.out.println(System.getProperty("user.home"));) containing the following files:
- authorized_keys
- id_dsa
- id_dsa.pub
- known_hosts

6. start the application from the directory containing the directory "sleep" using this commandline:

java -classpath lib\castor-1.1.1-xml.jar;lib\commons-logging-1.1.jar;lib\GAT-API.jar;lib\GAT-engine.jar;lib\ibis-deploy.jar;lib\ibis-server-2.1.1.jar;lib\ibis-util-2.1.jar;lib\log4j-1.2.13.jar;lib\xercesImpl.jar;lib\xmlParserAPIs.jar -Dgat.adaptor.path=lib\adaptors -Dlog4j.configuration=file:log4j.properties test.SleepTest <sleeptime> <number of nodes> <number of cores per node>

NOTES:
- this will execute the application test.SleepTest, the source SleepTest can be found in test.zip
- SleepTest will execute one (IbisDeploy) Job containing two (IbisDeploy) SubJobs, one executing at the VU cluster of the DAS-3, one executing at the UvA cluster of the DAS-3
- SleepTest executes the satin application called Sleep, which executes a distributed sleep, the sleep time (in seconds) can be provided as command line argument and will be divided over the total number of cores that join the distributed sleep. One of the cores will print the satin statistics.
- IbisDeploy doesn't start the Sleep application directly, because the source code of the SleepTest contains the lines with "setWrapperExecutable()" and "setWrapperArguments()". It does start the executable (/bin/sh) which gets some arguments: myscript.sh <number of nodes> <number of cores per node> <all the arguments that would lead to the execution of one Sleep application on one core>, this will look like: /path/to/java/bin/java -classpath ibisjar1.jar:ibisjar2.jar -Dlog4j.properties=file:log4j.properties -Dibis.pool.name=bla -Dibis.server.address=... -Dibis.server.hub.addresses=... -Dibis.location.automatic=true Sleep <sleeptime> 

7. The program will produce the following output in the CWD:
- server.err (the standard err of the server)
- VU.err (the standard err of the hub at the VU cluster)
- UvA.err (the standard err of the hub at the UvA cluster)
- VU.sleep.out (the standard out of the Sleep subjob at the VU cluster)
- VU.sleep.err (the standard err of the Sleep subjob at the VU cluster)
- UvA.sleep.out (the standard out of the Sleep subjob at the UvA cluster)
- UvA.sleep.err (the standard err of the Sleep subjob at the UvA cluster)

8. The logging output of the server, hubs and the Sleep application can be controlled using the log4j.properties in the directory "sleep" (it shouldn't write to stdout, because the hubs and servers use the stdout to communicate with ibis deploy, the current log4j.properties is properly set up.)

9. The logging output of the SleepTest application can be controlled using the log4j.properties in the CWD (or a log4j.properties at another location, but then you've to modify the example commandline).

