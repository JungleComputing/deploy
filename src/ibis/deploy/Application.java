/*
 * Created on Mar 8, 2006 by rob
 */
package ibis.deploy;

import ibis.util.TypedProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class Application {
    private static Logger logger = Logger.getLogger(Application.class);

    private String name;

    private String javaMain;

    private String[] javaOptions;

    private String[] javaSystemProperties;

    private String[] javaArguments;

    private String[] preStageSet;

    private String[] postStageSet;

    /**
     * Creates a new {@link Application}.
     * 
     * @param name
     *            the name of the application
     * @param javaMain
     *            the java main class which should be executed
     * @param javaOptions
     *            the java options for this application
     * @param javaSystemProperties
     *            the java system properties for this application as an array
     *            {"key1=value1", "key2=value2", ..., "keyn=valuen"}
     * @param javaArguments
     *            the java arguments for this application
     * @param preStageSet
     *            the files to be pre staged
     * @param postStageSet
     *            the files to be post staged
     */
    public Application(String name, String javaMain, String[] javaOptions,
            String[] javaSystemProperties, String[] javaArguments,
            String[] preStageSet, String[] postStageSet) {
        this.name = name;
        this.javaMain = javaMain;
        this.javaOptions = javaOptions;
        this.javaSystemProperties = javaSystemProperties;
        this.javaArguments = javaArguments;
        this.preStageSet = preStageSet;
        this.postStageSet = postStageSet;
    }

    /**
     * Gets the name of the application
     * 
     * @return the name of the application
     */
    public String getName() {
        return name;
    }

    /**
     * Loads an application from a {@link TypedProperties} object. The following
     * properties can be set:
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="application
     * properties"> <CAPTION>application properties </CAPTION> <COLGROUP
     * align="left"> <COLGROUP align="center"> <COLGROUP align="left" > <THEAD
     * valign="top">
     * <TR>
     * <TH>Property
     * <TH>Example
     * <TH>Description<TBODY>
     * <TR>
     * <TD>applications
     * <TD>myApp1,myApp2
     * <TD>the names of the applications described in the properties
     * <TR>
     * <TD>[name.]java.main
     * <TD>mypackage.MyMainClass
     * <TD>the main class, which should be executed
     * <TR>
     * <TD>[name.]java.options
     * <TD>-server -Xms768M
     * <TD>the options for the jvm
     * <TR>
     * <TD>[name.]java.system.properties
     * <TD>satin.closed=true satin.alg=RS
     * <TD>the system properties for the jvm, the example results in
     * -Dsatin.closed=true -Dsatin.alg=RS
     * <TR>
     * <TD>[name.]java.arguments
     * <TD>myArg1 myArg2
     * <TD>the arguments for the main class
     * <TR>
     * <TD>[name.]prestage
     * <TD>file1 dir file2 path/to/otherfile path/to/otherdir
     * <TD>the files and directories that should be prestaged.
     * <TR>
     * <TD>[name.]poststage
     * <TD>file1 dir file2 path/to/otherfile path/to/otherdir
     * <TD>the files and directories that should be poststaged.
     * <TR> </TABLE>
     * <p>
     * The properties are hierarchical, an example of this is shown below:
     * <p>
     * applications=app1,app2,app3<br />
     * app1.main=MyMain<br />
     * app2.main=OtherMain<br />
     * app3.main=AnotherMain<br />
     * options=-server<br />
     * app3.options=-client<br />
     * <p>
     * There are three applications in this example, app1, app2 and app3. All
     * the applications have a different main class. In general the jvm option
     * is -server, but for app3 it's overwritten with -client. Note that the
     * order of the properties doesn't matter, specific properties overwrite
     * general properties.
     * 
     * @param applicationProps
     *            the application properties
     * @return a {@link Set} of {@link Application}s that were described by the
     *         application properties.
     */
    public static Set<Application> load(TypedProperties applicationProps) {
        if (logger.isInfoEnabled()) {
            logger.info("loading applications");
        }
        HashSet<Application> result = new HashSet<Application>();
        String[] apps = applicationProps.getStringList("applications");
        for (String app : apps) {
            String javaMain = TypedPropertiesUtility.getHierarchicalProperty(
                    applicationProps, app, "java.main", null);
            if (javaMain == null) {
                return null;
            }
            String[] javaOptions = TypedPropertiesUtility
                    .getHierarchicalStringList(applicationProps, app,
                            "java.options", null, " ");
            String[] javaSystemProperties = TypedPropertiesUtility
                    .getHierarchicalStringList(applicationProps, app,
                            "java.system.properties", null, " ");
            String[] javaArguments = TypedPropertiesUtility
                    .getHierarchicalStringList(applicationProps, app,
                            "java.arguments", null, " ");
            String[] preStageSet = TypedPropertiesUtility
                    .getHierarchicalStringList(applicationProps, app,
                            "prestage", null, " ");
            String[] postStageSet = TypedPropertiesUtility
                    .getHierarchicalStringList(applicationProps, app,
                            "poststage", null, " ");
            result.add(new Application(app, javaMain, javaOptions,
                    javaSystemProperties, javaArguments, preStageSet,
                    postStageSet));
        }
        return result;
    }

    /**
     * Gets the java arguments
     * 
     * @return the java arguments.
     */
    public String[] getJavaArguments() {
        return javaArguments;
    }

    protected String[] getServerPreStageSet() {
        String files = "";
        if (preStageSet != null) {
            for (String filename : preStageSet) {

                files += getFiles(new java.io.File(filename), "", ".jar");
                files += getFiles(new java.io.File(filename), "", ".properties");
            }
        }
        return files.split(":");
    }

    protected String getJavaClassPath(String[] filenames) {
        String classpath = "";
        if (filenames != null) {
            for (String filename : filenames) {
                classpath += getFiles(new java.io.File(filename), "", ".jar");
            }
        }
        return classpath;
    }

    private String getFiles(java.io.File file, String prefix, String postfix) {
        String result = "";
        if (file.isDirectory()) {
            for (java.io.File childfile : file.listFiles()) {
                result += getFiles(childfile, prefix + file.getName() + "/",
                        postfix);
            }
        } else if (file.getName().endsWith(postfix)) {
            result += prefix + file.getName() + ":";
        }
        return result;
    }

    /**
     * Gets the java main class
     * 
     * @return the java main class
     */
    public String getJavaMain() {
        return javaMain;
    }

    /**
     * Gets the java jvm options
     * 
     * @return the java jvm options
     */
    public String[] getJavaOptions() {
        return javaOptions;
    }

    /**
     * Gets the java system properties
     * 
     * @return the java system properties
     */
    public Map<String, String> getJavaSystemProperties() {
        if (javaSystemProperties == null) {
            return null;
        }
        Map<String, String> result = new HashMap<String, String>();
        for (String systemProperty : javaSystemProperties) {
            if (systemProperty.contains("=")) {
                result.put(systemProperty.substring(0, systemProperty
                        .indexOf("=")), systemProperty.substring(systemProperty
                        .indexOf("=") + 1));
            }
        }
        return result;
    }

    /**
     * Gets the post stage file set
     * 
     * @return the post stage file set.
     */
    public String[] getPostStageSet() {
        return postStageSet;
    }

    /**
     * Gets the pre stage file set
     * 
     * @return the pre stage file set
     */
    public String[] getPreStageSet() {
        return preStageSet;
    }
}