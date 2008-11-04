package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application belonging to some ApplicationGroup. Applications are Java
 * applications which use the IPL library to communicate
 * 
 * @author ndrost
 * 
 */
public class Application {

    // group this application belongs to
    private final ApplicationGroup parent;

    // name of application
    private String name;

    // main class of application
    private String mainClass;

    // files and directories which need to be in the classpath
    // automatically pre-staged as well.
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
    private List<String> jvmOptions;

    /**
     * Creates an empty application object, with no name or parent
     */
    Application() {
        parent = null;
        name = null;
        mainClass = null;
        libs = null;
        arguments = null;
        inputFiles = null;
        outputFiles = null;
        systemProperties = null;
        jvmOptions = null;
    }

    /**
     * Creates a new application with a given name and parent ApplicationGroup
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
        jvmOptions = null;
    }

    /**
     * Load application from the given properties (usually loaded from an
     * application-group file)
     * 
     * @param properties
     *            properties to load application from
     * @param name
     *            name of this application. Also used as prefix for all keys in
     *            property object
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
        jvmOptions = Util.getStringListProperty(properties, prefix
                + "java.options");
    }

    /**
     * Put all non-null values of given application into this application
     * 
     * @param other
     *            source application object
     */
    void overwrite(Application other) {
        if (other == null) {
            return;
        }

        if (other.mainClass != null) {
            this.mainClass = other.mainClass;
        }

        if (other.libs != null) {
            libs = new ArrayList<File>();
            libs.addAll(other.libs);
        }

        if (other.arguments != null) {
            arguments = new ArrayList<String>();
            arguments.addAll(other.arguments);
        }

        if (other.inputFiles != null) {
            inputFiles = new ArrayList<File>();
            inputFiles.addAll(other.inputFiles);
        }

        if (other.outputFiles != null) {
            outputFiles = new ArrayList<File>();
            outputFiles.addAll(other.outputFiles);
        }

        if (other.systemProperties != null) {
            for (Map.Entry<String, String> entry : other.systemProperties
                    .entrySet()) {
                setSystemProperty(entry.getKey(), entry.getValue());
            }
        }

        if (other.jvmOptions != null) {
            jvmOptions = new ArrayList<String>();
            jvmOptions.addAll(other.jvmOptions);
        }
    }

    /**
     * Returns application parameters of this application.
     * 
     * @return application parameters of this application.
     */
    public String[] getArguments() {
        if (arguments == null) {
            return null;
        }
        return arguments.toArray(new String[0]);
    }

    /**
     * Sets application parameters of this application.
     * 
     * @param arguments
     *            new application parameters of this application.
     */
    public void setArguments(String[] arguments) {
        if (arguments == null) {
            this.arguments = null;
        } else {
            this.arguments = Arrays.asList(arguments.clone());
        }
    }

    /**
     * Add addition arguments to list of parameters of this application. List
     * will be created if needed.
     * 
     * @param argument
     *            new application parameter of this application.
     */
    public void addArgument(String argument) {
        if (arguments == null) {
            arguments = new ArrayList<String>();
        }
        arguments.add(argument);
    }

    /**
     * Returns application input files.
     * 
     * @return list of input files
     */
    public File[] getInputFiles() {
        if (inputFiles == null) {
            return null;
        }
        return inputFiles.toArray(new File[0]);
    }

    /**
     * Sets application input files, overwriting any previous setting. There is
     * no need to add libraries to this list, as they are automatically added
     * 
     * @param inputFiles
     *            new list of input files
     */
    public void setInputFiles(File[] inputFiles) {
        if (inputFiles == null) {
            this.inputFiles = null;
        } else {
            this.inputFiles = Arrays.asList(inputFiles.clone());
        }
    }

    /**
     * Add a single input file to the list of input files. Automatically creates
     * list, if needed.
     * 
     * @param inputFile
     *            new input file
     */
    public void addInputFile(File inputFile) {
        if (inputFiles == null) {
            inputFiles = new ArrayList<File>();
        }
        inputFiles.add(inputFile);
    }

    /**
     * Returns any additional JVM options needed for this application (usually
     * memory settings and such)
     * 
     * @return The JVM options.
     */
    public String[] getJVMOptions() {
        if (jvmOptions == null) {
            return null;
        }
        return jvmOptions.toArray(new String[0]);
    }

    /**
     * Sets any addition JVM options needed for this application (usually memory
     * settings and such)
     * 
     * @param jvmOptions
     *            The new JVM options.
     */
    public void setJVMOptions(String[] jvmOptions) {
        if (jvmOptions == null) {
            this.jvmOptions = null;
        } else {
            this.jvmOptions = Arrays.asList(jvmOptions.clone());
        }
    }

    /**
     * Adds a single option to the list of JVM options. List is created if
     * needed.
     * 
     * @param jvmOption
     *            The new JVM option.
     */
    public void addJVMOption(String jvmOption) {
        if (jvmOptions == null) {
            jvmOptions = new ArrayList<String>();
        }
        jvmOptions.add(jvmOption);
    }

    public File[] getLibs() {
        if (libs == null) {
            return null;
        }
        return libs.toArray(new File[0]);
    }

    public void setLibs(File[] libs) {
        if (libs == null) {
            this.libs = null;
        } else {
            this.libs = Arrays.asList(libs.clone());
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

    public void addOutputFile(File outputFile) {
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

    public void setSystemProperty(String key, String value) {
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

        if (jvmOptions == null) {
            out.println("#" + prefix + "java.options =");
        } else {
            out.println(prefix + "java.options = "
                    + Util.strings2CSS(jvmOptions));
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
        result += " Java Options = " + Util.strings2CSS(getJVMOptions()) + "\n";

        return result;
    }

    public String toString() {
        return name;
    }

}
