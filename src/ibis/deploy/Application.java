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
 * Ibis-based Java Application Description. Applications are usually part of
 * (and created by) an ApplicationSet.
 * 
 * @author Niels Drost
 * 
 */
public class Application {

    /**
     * Print a table of valid keys and some explanations to the given stream
     * 
     * @param out
     *            stream used for printing
     */
    public static void printTableOfKeys(PrintWriter out) {
        out.println("# Valid keys for applications:");
        out.println("# KEY                COMMENT");
        out.println("# main.class         Main class of application");
        out.println("# arguments          Arguments of the application(*)");
        out
                .println("# libs               Files and directories which need to be in the classpath.");
        out.println("#                    Automatically pre-staged as well(*)");
        out
                .println("# input.files        Input files copied to root of sandbox(*)");
        out
                .println("# output.files       Output files copied from root of sandbox(*)");
        out
                .println("# system.properties  Additional system properties in the form of name=value(*)");
        out
                .println("# jvm.options        Additional JVM options, for instance memory options(*)");
        out.println("# (* = comma separated list of items)");
    }

    // group this application belongs to
    private final ApplicationSet parent;

    // name of application
    private String name;

    // main class of application
    private String mainClass;

    // arguments of the application
    private List<String> arguments;

    // files and directories which need to be in the classpath
    // automatically pre-staged as well.
    private List<File> libs;

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
        libs = null;
        mainClass = null;
        arguments = null;
        inputFiles = null;
        outputFiles = null;
        systemProperties = null;
        jvmOptions = null;
    }

    /**
     * Creates a new application with a given name and parent ApplicationSet
     * object.
     * 
     * @param name
     *            the name of the application
     * @throws Exception
     *             if name is null or contains periods and/or spaces
     */
    Application(String name, ApplicationSet parent) throws Exception {
        this.parent = parent;
        this.name = name;

        if (name != null && name.contains(".")) {
            throw new Exception("application name cannot contain periods : \""
                    + name + "\"");
        }

        if (name != null && name.contains(" ")) {
            throw new Exception("application name cannot contain spaces : \""
                    + name + "\"");
        }

        libs = null;
        mainClass = null;
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
     *            name of this application.
     * @param prefix
     *            prefix used for loading application
     * @throws Exception
     *             if application cannot be read properly
     */
    Application(TypedProperties properties, String name, String prefix,
            ApplicationSet parent) {
        this.parent = parent;
        this.name = name;

        // add separator to prefix
        prefix = prefix + ".";

        libs = Util.getFileListProperty(properties, prefix + "libs");
        mainClass = properties.getProperty(prefix + "main.class");
        arguments = Util
                .getStringListProperty(properties, prefix + "arguments");
        inputFiles = Util.getFileListProperty(properties, prefix
                + "input.files");
        outputFiles = Util.getFileListProperty(properties, prefix
                + "output.files");
        systemProperties = Util.getStringMapProperty(properties, prefix
                + "system.properties");
        jvmOptions = Util.getStringListProperty(properties, prefix
                + "jvm.options");
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

        if (other.name != null) {
            name = other.name;
        }

        if (other.mainClass != null) {
            this.mainClass = other.mainClass;
        }

        if (other.arguments != null) {
            arguments = new ArrayList<String>();
            arguments.addAll(other.arguments);
        }

        if (other.libs != null) {
            libs = new ArrayList<File>();
            libs.addAll(other.libs);
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
     * Returns application group this application belongs to.
     * 
     * @return group this application belongs to (possibly null).
     */
    public ApplicationSet getApplicationSet() {
        return parent;
    }

    /**
     * Returns name of this application.
     * 
     * @return name of this application.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of this application.
     * 
     * @param name
     *            name of this application.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns main class of this application.
     * 
     * @return main class of this application.
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * Set main class of this application.
     * 
     * @param mainClass
     *            main class of this application.
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
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
     * Returns list of library (jar) files required to run this application. May
     * include directories and non-jar files.
     * 
     * @return list of library (jar) files.
     */
    public File[] getLibs() {
        if (libs == null) {
            return null;
        }
        return libs.toArray(new File[0]);
    }

    /**
     * Sets list of library (jar) files required to run this application. May
     * include directories and non-jar files.
     * 
     * @param libs
     *            new list of library files and directories.
     */
    public void setLibs(File[] libs) {
        if (libs == null) {
            this.libs = null;
        } else {
            this.libs = Arrays.asList(libs.clone());
        }
    }

    /**
     * Adds file or directory to list of library (jar) files required to run
     * this application.
     * 
     * @param lib
     *            new library file or directory.
     */
    public void addLib(File lib) {
        if (libs == null) {
            libs = new ArrayList<File>();
        }
        libs.add(lib);
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
     * no need to add libraries to this list, as they are automatically
     * pre-staged.
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
     * Returns list of output files. Files are copied from the "root" directory
     * of the application, to the local file specified.
     * 
     * @return list of output files.
     */
    public File[] getOutputFiles() {
        if (outputFiles == null) {
            return null;
        }
        return outputFiles.toArray(new File[0]);
    }

    /**
     * Sets list of output files. Files are copied from the "root" directory of
     * the application, to the local file specified.
     * 
     * @param outputFiles
     *            new list of output files.
     */
    public void setOutputFiles(File[] outputFiles) {
        if (outputFiles == null) {
            this.outputFiles = null;
        } else {
            this.outputFiles = Arrays.asList(outputFiles.clone());
        }
    }

    /**
     * Add file to list of output files. File with name "outputFile.getName()"
     * is copied from the root of the application sandbox to the file specified.
     * 
     * @param outputFile
     *            new output file.
     */
    public void addOutputFile(File outputFile) {
        if (outputFiles == null) {
            outputFiles = new ArrayList<File>();
        }
        outputFiles.add(outputFile);
    }

    /**
     * Returns (copy of) map of all system properties.
     * 
     * @return all system properties, or null if unset.
     */
    public Map<String, String> getSystemProperties() {
        if (systemProperties == null) {
            return null;
        }
        return new HashMap<String, String>(systemProperties);
    }

    /**
     * Sets map of all system properties.
     * 
     * @param systemProperties
     *            new system properties, or null to unset.
     */
    public void setSystemProperties(Map<String, String> systemProperties) {
        if (systemProperties == null) {
            this.systemProperties = null;
        } else {
            this.systemProperties = new HashMap<String, String>(
                    systemProperties);
        }
    }

    /**
     * Sets a single system property. Map will be created if needed.
     * 
     * @param name
     *            name of new property
     * @param value
     *            value of new property.
     */
    public void setSystemProperty(String name, String value) {
        if (systemProperties == null) {
            systemProperties = new HashMap<String, String>();
        }
        systemProperties.put(name, value);
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

    /**
     * Checks if this application is suitable for deploying. If not, throws an
     * exception.
     * 
     * @param jobName
     *            name of job
     * 
     * @throws Exception
     *             if this application is incomplete or incorrect.
     */
    public void checkSettings(String jobName) throws Exception {
        String prefix = "Cannot run job \"" + jobName + "\": Application ";

        if (name == null) {
            throw new Exception(prefix + "name not specified");
        }
        if (mainClass == null) {
            throw new Exception(prefix + "main class not specified");
        }

        if (libs == null || libs.size() == 0) {
            throw new Exception(prefix + "libraries not specified");
        }

        for (File file : libs) {
            if (!file.exists()) {
                throw new Exception(prefix + "library file or dir \"" + file
                        + "\" does not exist");
            }
        }

        if (inputFiles != null) {
            for (File file : inputFiles) {
                if (!file.exists()) {
                    throw new Exception(prefix + " input file \"" + file
                            + "\" does not exist");
                }
            }
        }
    }

    /**
     * Print the settings of this application to a (properties) file
     * 
     * @param out
     *            stream to write this file to
     * @param prefix
     *            prefix to add to all keys, or null to use name of application.
     * @param printComments
     *            if true, comments are added for all null values
     * @throws Exception
     *             if this application has no name
     */
    void save(PrintWriter out, String prefix, boolean printComments)
            throws Exception {
        if (prefix == null) {
            throw new Exception("cannot print application to file,"
                    + " prefix is not specified");
        }

        prefix = prefix + ".";

        if (mainClass != null) {
            out.println(prefix + "main.class = " + mainClass);
        } else if (printComments) {
            out.println("#" + prefix + "main.class =");
        }

        if (arguments != null) {
            out.println(prefix + "arguments = " + Util.strings2CSS(arguments));
        } else if (printComments) {
            out.println("#" + prefix + "arguments =");
        }

        if (libs != null) {
            out.println(prefix + "libs = " + Util.files2CSS(libs));
        } else if (printComments) {
            out.println("#" + prefix + "libs =");
        }

        if (inputFiles != null) {
            out.println(prefix + "input.files = " + Util.files2CSS(inputFiles));
        } else if (printComments) {
            out.println("#" + prefix + "input.files =");
        }

        if (outputFiles != null) {
            out.println(prefix + "output.files = "
                    + Util.files2CSS(outputFiles));
        } else if (printComments) {
            out.println("#" + prefix + "output.files =");
        }

        if (systemProperties != null) {
            out.println(prefix + "system.properties = "
                    + Util.toCSString(systemProperties));
        } else if (printComments) {
            out.println("#" + prefix + "system.properties =");
        }

        if (jvmOptions != null) {
            out.println(prefix + "jvm.options = "
                    + Util.strings2CSS(jvmOptions));
        } else if (printComments) {
            out.println("#" + prefix + "java.options =");
        }
    }

    /**
     * Returns a newline separated string useful for printing.
     * 
     * @return a newline separated string useful for printing.
     */
    public String toPrintString() {
        String result;
        if (name == null) {
            result = "Application Settings:\n";
        } else {
            result = "Application Settings for \"" + getName() + "\":\n";
        }

        result += " Main class = " + getMainClass() + "\n";
        result += " Arguments = " + Util.strings2CSS(getArguments()) + "\n";
        result += " Libs = " + Util.files2CSS(getLibs()) + "\n";
        result += " Input Files = " + Util.files2CSS(getInputFiles()) + "\n";
        result += " Output Files = " + Util.files2CSS(getOutputFiles()) + "\n";
        result += " System properties = "
                + Util.toCSString(getSystemProperties()) + "\n";
        result += " JVM Options = " + Util.strings2CSS(getJVMOptions()) + "\n";

        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }

}
