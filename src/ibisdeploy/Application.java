/*
 * Created on Mar 8, 2006 by rob
 */
package ibisdeploy;

import ibis.smartsockets.util.TypedProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Application {
    private String executable;

    private String[] javaFlags;
    
    private String[] arguments;

    private String directoryName;
    private String classpath;

    private String[] preStaged;
    private String[] postStaged;
    
    public Application(String command, String[] javaFlags, String[] parameters, String name, String[] preStaged, String[] postStaged, String classpath) {
        this.executable = command;
        this.javaFlags = javaFlags;
        this.arguments = parameters;
        directoryName = name;
        this.preStaged = preStaged;
        this.postStaged = postStaged;
        this.classpath = classpath;
    }
    
    public String getClasspath() {
        return classpath;
    }

    public String getExecutable() {
        return executable;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String[] getArguments() {
        return (String[]) arguments.clone();
    }

    private static String[] readStringArray(Input in) {
        ArrayList<String> res = new ArrayList<String>();
        while (!in.eoln()) {
            String p = in.readWord();
            in.skipWhiteSpace();
            res.add(p);
        }
        in.readln();
        
        String[] result = new String[res.size()];
        for(int i=0; i<result.length; i++) {
            result[i] = (String) res.get(i);
        }

        return result;
    }
    
    public static Application loadApplication(String filename) throws FileNotFoundException, IOException {
        System.err.println("loading application: " + filename + " ...");
        TypedProperties appprops = new TypedProperties();
        appprops.load(new FileInputStream(filename));
        String name = appprops.getProperty("ibis.deploy.application.name");
        String command = appprops.getProperty("ibis.deploy.application.java.class");
        String[] javaFlags = appprops.getStringList("ibis.deploy.application.java.flags", " ");
        String[] parameters = appprops.getStringList("ibis.deploy.application.parameters", " ");
        String[] preStaged = appprops.getStringList("ibis.deploy.application.files.prestage");
        String[] postStaged = appprops.getStringList("ibis.deploy.application.files.poststage");
        String classpath = appprops.getProperty("ibis.deploy.application.java.classpath");
        System.err.println("loading application: " + filename + " DONE");
        return new Application(command, javaFlags, parameters, name, preStaged, postStaged, classpath);
        
    }
    
    public String toString() {
        String res = "Application " + directoryName + "\n";
        res += "   executable: " + executable + "\n";
        res += "   parameters:";
        for(int i=0; i<arguments.length; i++) {
            res += " " + arguments[i];
        }
        res += "\n";
        
        res += "   java flags:";
        for(int i=0; i<javaFlags.length; i++) {
            res += " " + javaFlags[i];
        }
        res += "\n";
        
        res += "   pre staged:";
        for(int i=0; i<preStaged.length; i++) {
            res += " " + preStaged[i];
        }
        res += "\n";
        
        res += "   post staged:";
        for(int i=0; i<postStaged.length; i++) {
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
        for(int i=0; i<javaFlags.length; i++) {
            res += javaFlags[i];
            if(i != javaFlags.length-1) {
                res += " ";
            }
        }
        return res;
    }
}
