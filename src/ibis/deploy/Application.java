/*
 * Created on Mar 8, 2006 by rob
 */
package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class Application {
    private static Logger logger = Logger.getLogger(Application.class);
    private String main;

    private String[] javaFlags;

    private String[] parameters;

    private String name;
    private String classpath;

    private String[] preStaged;
    private String[] postStaged;

    public Application(String name, String main, String[] javaFlags,
            String[] parameters, String[] preStaged, String[] postStaged,
            String classpath) {
        this.name = name;
        this.main = main;
        this.javaFlags = javaFlags;
        this.parameters = parameters;
        this.preStaged = preStaged;
        this.postStaged = postStaged;
        this.classpath = classpath;
    }

    public Object clone() {
        return new Application(name, main, javaFlags, parameters, preStaged,
                postStaged, classpath);
    }

    public String getName() {
        return name;
    }

    public String getClasspath() {
        return classpath;
    }

    public String getExecutable() {
        return main;
    }

    public String[] getParameters() {
        return parameters;
    }

    public static HashSet<Application> loadApplications(String filename)
            throws FileNotFoundException, IOException {
        logger.info("loading applications: " + filename + " ...");
        HashSet<Application> result = new HashSet<Application>();
        TypedProperties applicationProps = new TypedProperties();
        applicationProps.load(new FileInputStream(filename));
        String[] apps = applicationProps.getStringList("applications");
        for (String app : apps) {
            String main = applicationProps.getProperty(app + ".main");
            if (main == null || main.equals("")) {
                main = applicationProps.getProperty("main");
            }

            String[] javaFlags = applicationProps.getStringList(app + ".flags",
                    " ");
            if (javaFlags.length == 0) {
                javaFlags = applicationProps.getStringList("flags", " ");
            }

            String[] parameters = applicationProps.getStringList(app
                    + ".parameters", " ");
            if (parameters.length == 0) {
                parameters = applicationProps.getStringList("parameters", " ");
            }

            String[] preStaged = applicationProps.getStringList(app
                    + ".prestage",  " ");
            if (preStaged.length == 0) {
                preStaged = applicationProps.getStringList("prestage", " ");
            }

            String[] postStaged = applicationProps.getStringList(app
                    + ".poststage", " ");
            if (postStaged.length == 0) {
                postStaged = applicationProps.getStringList("poststage", " ");
            }

            String classpath = applicationProps.getProperty(app + ".classpath");
            if (classpath == null || classpath.equals("")) {
                classpath = applicationProps.getProperty("classpath");
            }
            result.add(new Application(app, main, javaFlags, parameters,
                    preStaged, postStaged, classpath));
        }
        logger.info("loading application: " + filename + " DONE");
        return result;
    }

    public String toString() {
        String res = "Application " + name + "\n";
        res += "   executable: " + main + "\n";
        res += "   parameters:";
        for (int i = 0; i < parameters.length; i++) {
            res += " " + parameters[i];
        }
        res += "\n";

        res += "   java flags:";
        for (int i = 0; i < javaFlags.length; i++) {
            res += " " + javaFlags[i];
        }
        res += "\n";

        res += "   pre staged:";
        for (int i = 0; i < preStaged.length; i++) {
            res += " " + preStaged[i];
        }
        res += "\n";

        res += "   post staged:";
        for (int i = 0; i < postStaged.length; i++) {
            res += " " + postStaged[i];
        }
        res += "\n";

        return res;
    }

    public String[] getPreStaged() {
        return preStaged;
    }

    public String[] getPostStaged() {
        return postStaged;
    }

    public String[] getJavaFlags() {
        return javaFlags;
    }

    public String getJavaFlagsAsString() {
        String res = "";
        for (int i = 0; i < javaFlags.length; i++) {
            res += javaFlags[i];
            if (i != javaFlags.length - 1) {
                res += " ";
            }
        }
        return res;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public void setJavaFlags(String[] javaFlags) {
        this.javaFlags = javaFlags;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;

    }

    public void setPreStaged(String[] preStaged) {
        this.preStaged = preStaged;
    }

    public void setPostStaged(String[] postStaged) {
        this.postStaged = postStaged;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
}
