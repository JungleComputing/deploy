package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {

    // group this application belongs to
    private final ApplicationGroup parent;

    // name of application
    private String name;

    // main class of application
    private String mainClass;

    // files and dirs which need to be in the classpath
    // automatically prestaged as well.
    private List<File> libs;

    // arguments of the application
    private List<String> arguments;

    // additional input files (not jars)
    private List<File> inputFiles;

    // output files
    private List<File> outputFiles;

    // <NAME, VALUE> additional system properties
    private Map<String, String> systemProperties;

    // additional JVM options
    private List<String> javaOptions;

    /**
     * Creates a new appplication with a given name. Applications cannot be
     * created directly, but are constructed by a parent ApplicationGroup
     * object.
     * 
     * @param name
     *            the name of the application
     * @throws Exception
     *             if the name given is <code>null</code>
     */

    Application(String name, ApplicationGroup parent) throws Exception {
        this.parent = parent;

        if (name == null) {
            throw new Exception("no name specified for application");
        }
        this.name = name;

        mainClass = null;
        libs = null;
        arguments = null;
        inputFiles = null;
        outputFiles = null;
        systemProperties = null;
        javaOptions = null;
    }

    /**
     * Load application from the given properties (usually loaded from an
     * application-group file)
     * 
     * @param properties
     *            properties to load application from
     * @param object
     *            name of this application, or null to load "defaults"
     *            application
     * @throws Exception
     *             if application cannot be read properly
     */
    Application(TypedProperties properties, String name, ApplicationGroup parent)
            throws Exception {
        this.parent = parent;
        this.name = name;

        String prefix;
        if (name == null) {
            prefix = "";
        } else {
            prefix = name + ".";
        }

        mainClass = properties.getProperty(prefix + "main.class");
        libs = Util.getFileListProperty(properties, prefix + "libs");
        arguments = Util
                .getStringListProperty(properties, prefix + "arguments");
        inputFiles = Util.getFileListProperty(properties, prefix
                + "input.files");
        outputFiles = Util.getFileListProperty(properties, prefix
                + "output.files");
        systemProperties = Util.getStringMapProperty(properties, prefix
                + "system.properties");
        javaOptions = Util.getStringListProperty(properties, prefix
                + "java.options");
    }

    public String getGroupName() {
        if (parent == null) {
            return null;
        }
        return parent.getName();
    }

    public String[] getArguments() {
        if (arguments == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getArguments();
        }
        return arguments.toArray(new String[0]);
    }

    public void setArguments(String[] arguments) {
        if (arguments == null) {
            this.arguments = null;
        } else {
            this.arguments = Arrays.asList(arguments.clone());
        }
    }

    public void addArgument(String argument) {
        if (arguments == null) {
            arguments = new ArrayList<String>();
        }
        arguments.add(argument);
    }

    public File[] getInputFiles() {
        if (inputFiles == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getInputFiles();
        }
        return inputFiles.toArray(new File[0]);
    }

    public void setInputFiles(File[] inputFiles) {
        if (inputFiles == null) {
            this.inputFiles = null;
        } else {
            this.inputFiles = Arrays.asList(inputFiles.clone());
        }
    }

    public void addInputFiles(String inputFile) {
        if (inputFiles == null) {
            inputFiles = new ArrayList<File>();
        }
        arguments.add(inputFile);
    }

    public String[] getJavaOptions() {
        if (javaOptions == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getJavaOptions();
        }
        return javaOptions.toArray(new String[0]);
    }

    public void setJavaOptions(String[] javaOptions) {
        if (javaOptions == null) {
            this.javaOptions = null;
        } else {
            this.javaOptions = Arrays.asList(javaOptions.clone());
        }
    }

    public void addJavaOptions(String javaOption) {
        if (javaOptions == null) {
            javaOptions = new ArrayList<String>();
        }
        javaOptions.add(javaOption);
    }

    public File[] getLibs() {
        if (libs == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getLibs();
        }
        return libs.toArray(new File[0]);
    }

    public void setLibs(File[] classpath) {
        if (classpath == null) {
            this.libs = null;
        } else {
            this.libs = Arrays.asList(classpath.clone());
        }
    }

    public void addLib(File lib) {
        if (libs == null) {
            libs = new ArrayList<File>();
        }
        libs.add(lib);
    }

    public String getMainClass() {
        if (mainClass == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getMainClass();
        }
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File[] getOutputFiles() {
        if (outputFiles == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getOutputFiles();
        }
        return outputFiles.toArray(new File[0]);
    }

    public void setOutputFiles(File[] outputFiles) {
        if (outputFiles == null) {
            this.outputFiles = null;
        } else {
            this.outputFiles = Arrays.asList(outputFiles.clone());
        }
    }

    public void addOutputFilesElement(File outputFile) {
        if (outputFiles == null) {
            outputFiles = new ArrayList<File>();
        }
        outputFiles.add(outputFile);
    }

    public Map<String, String> getSystemProperties() {
        if (systemProperties == null) {
            if (parent == null) {
                return null;
            }
            return parent.getDefaults().getSystemProperties();
        }
        return new HashMap<String, String>(systemProperties);
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        if (systemProperties == null) {
            this.systemProperties = null;
        } else {
            this.systemProperties = new HashMap<String, String>(
                    systemProperties);
        }
    }

    public void addSystemProperty(String key, String value) {
        if (systemProperties == null) {
            systemProperties = new HashMap<String, String>();
        }
        systemProperties.put(key, value);
    }

    /**
     * Print the settings of this application to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prependName
     *            if true, key/value lines prepended with the application name
     * @throws Exception
     *             if this application has no name
     */
    public void print(PrintWriter out, boolean prependName) throws Exception {
        String prefix;

        if (prependName) {
            if (name == null || name.length() == 0) {
                throw new Exception("cannot print application to file,"
                        + " name is not specified");
            }
            prefix = name + ".";
        } else {
            prefix = "";
        }

        if (mainClass == null) {
            out.println("#" + prefix + "main.class =");
        } else {
            out.println(prefix + "main.class = " + mainClass);
        }

        if (libs == null) {
            out.println("#" + prefix + "libs =");
        } else {
            out.println(prefix + "libs = " + Util.files2CSS(libs));
        }

        if (arguments == null) {
            out.println("#" + prefix + "arguments =");
        } else {
            out.println(prefix + "arguments = " + Util.strings2CSS(arguments));
        }

        if (inputFiles == null) {
            out.println("#" + prefix + "input.files =");
        } else {
            out.println(prefix + "input.files = " + Util.files2CSS(inputFiles));
        }

        if (outputFiles == null) {
            out.println("#" + prefix + "output.files =");
        } else {
            out.println(prefix + "output.files = "
                    + Util.files2CSS(outputFiles));
        }

        if (systemProperties == null) {
            out.println("#" + prefix + "system.properties =");
        } else {
            out.print(prefix + "system.properties = "
                    + Util.toCSString(systemProperties));
            out.println();
        }

        if (javaOptions == null) {
            out.println("#" + prefix + "java.options =");
        } else {
            out.println(prefix + "java.options = "
                    + Util.strings2CSS(javaOptions));
        }
    }

    public String toPrintString() {
        String result = "Application " + getName() + "\n";
        result += " Main class = " + getMainClass() + "\n";
        result += " Libs = " + Util.files2CSS(getLibs()) + "\n";
        result += " Arguments = " + Util.strings2CSS(getArguments()) + "\n";
        result += " Input Files = " + Util.files2CSS(getInputFiles()) + "\n";
        result += " Output Files = " + Util.files2CSS(getOutputFiles()) + "\n";
        result += " System properties = "
                + Util.toCSString(getSystemProperties()) + "\n";
        result += " Java Options = " + Util.strings2CSS(getJavaOptions())
                + "\n";

        return result;
    }

    public String toString() {
        return name + "@" + getGroupName();
    }

}
