PREREQUISITES FOR GENESEQUENCING

- you have to submit from a headnode (for the moment)
- $GAT_LOCATION is set to an up to date JavaGAT (can be $IBIS_DEPLOY_HOME/lib) 
- $IBIS_HOME is set to an up to date Ibis
- $IBIS_DEPLOY_HOME is set to an up to date ibis-deploy
- don't forget to do your grid-proxy-init
- make sure that you can ssh to each involved cluster without having to type your password
- have a dir called genesequencing in your $IBIS_DEPLOY_HOME
- this dir should contain: 
  * genesequencing/satin-2.0.jar (from satin) 
  * genesequencing/lrmc.jar (from satin)
  * genesequencing/testdata (from genesequencing)
  * genesequencing/geneSequencing.jar (from genesequencing)
  * genesequencing/neobio.jar (from genesequencing)
  you may chose to have another setup, but then rewrite the application property files!
- make sure that geneSequencing is compiled using ibisc (there's a rewrite.sh)
- submit your jobs from $IBIS_DEPLOY_HOME using:
  > bin/ibis_deploy run.properties <runtime in minutes>
- don't forget to set the runtime as the default runtime per job is 20 min.
- the default number of retries is 3, the default timeout between retries is 120 sec., these values can be changed in the run.properties
- the output of ibis-deploy can be controlled using the log4j.properties file
