Ibis deploy

Summary:
Ibis deploy is a tool that makes it easier to deploy ibis applications. It's a library, but there's
also a standalone program that can be used to deploy your ibis jobs.

Using the library:
Get ibis deploy from the svn, note that ibis deploy uses JavaGAT and a JavaGAT version is included
in the ibis deploy version. Then build ibis deploy using ant

ant (builds ibis deploy with the included JavaGAT version)
ant build-external-gat (builds ibis deploy with JavaGAT located at $GAT_LOCATION)

Ok now you've the library. Look at the javadoc and the example application (test.SleepTest) for how
to use it. And finally when you've compiled your shiny application run it using this command:

bin/run-ibis-deploy-application <main> <args>

Using the standalone command line version:
Run a job like this:

bin/ibis-deploy <run properties file> (look at sleep-run.properties for an example of a run properties
file)

Known issues:
- if you want to use globus, you've got to run $GAT_LOCATION/bin/grid-proxy-init
- if you want to use ssh, you've got to setup your .ssh directory so that you can access the machine
  without having to type a password
