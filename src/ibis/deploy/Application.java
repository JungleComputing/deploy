package ibis.deploy;

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
        out.println("# libs               Files and directories which need to be in the classpath.");
        out.println("#                    Automatically pre-staged as well(*)");
        out.println("# input.files        Input files copied to root of sandbox(*)");
        out.println("# output.files       Output files copied from root of sandbox(*)");
        out.println("# system.properties  Additional system properties in the form of name=value(*)");
        out.println("# jvm.options        Additional JVM options, for instance memory options(*)");
        out.println("# log4j.file         Log4j properties file used for the application.");
        out.println("#                    Defaults to log4j of ibis-deploy itself.");
        out.println("# memory.size        Integer: amount of memory to use for this application (in Megabytes)");
        out.println("# (* = comma separated list of items)");

    }

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

    private File log4jFile;

    private int memorySize;

    /**
     * Creates an empty application object, with no name or parent
     */
    Application() {
        name = "anonymous";
        libs = null;
        mainClass = null;
        arguments = null;
        inputFiles = null;
        outputFiles = null;
        systemProperties = null;
        jvmOptions = null;
        log4jFile = null;
        memorySize = 0;
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
    public Application(String name) throws Exception {
        this();

        setName(name);
    }

    public Application(Application original) {
        this();

        this.name = original.name;

        if (original.libs != null) {
            this.libs = new ArrayList<File>(original.libs);
        }

        this.mainClass = original.mainClass;

        if (original.arguments != null) {
            this.arguments = new ArrayList<String>(original.arguments);
        }

        if (original.inputFiles != null) {
            this.inputFiles = new ArrayList<File>(original.inputFiles);
        }

        if (original.outputFiles != null) {
            this.outputFiles = new ArrayList<File>(original.outputFiles);
        }

        if (original.systemProperties != null) {
            this.systemProperties = new HashMap<String, String>(
                    original.systemProperties);
        }

        if (original.jvmOptions != null) {
            this.jvmOptions = new ArrayList<String>(original.jvmOptions);
        }

        log4jFile = original.log4jFile;

        memorySize = original.memorySize;
    }

    /**
     * Load application from the given properties (usually loaded from an
     * application-group file)
     * 
     * @param properties
     *            properties to load application from
     * @param prefix
     *            prefix used for loading application
     */
    public void setFromProperties(DeployProperties properties, String prefix) {
        prefix = prefix + ".";

        if (properties.getFileListProperty(prefix + "libs") != null) {
            libs = properties.getFileListProperty(prefix + "libs");
        }
        if (properties.getProperty(prefix + "main.class") != null) {
            mainClass = properties.getProperty(prefix + "main.class");
        }
        if (properties.getProperty(prefix + "arguments") != null) {
            arguments = properties.getStringListProperty(prefix + "arguments",
                    "\\s");
        }
        if (properties.getFileListProperty(prefix + "input.files") != null) {
            inputFiles = properties.getFileListProperty(prefix + "input.files");
        }
        if (properties.getFileListProperty(prefix + "output.files") != null) {
            outputFiles = properties.getFileListProperty(prefix
                    + "output.files");
        }
        if (properties.getStringMapProperty(prefix + "system.properties") != null) {
            systemProperties = properties.getStringMapProperty(prefix
                    + "system.properties");
        }
        if (properties.getStringListProperty(prefix + "jvm.options") != null) {
            jvmOptions = properties.getStringListProperty(prefix
                    + "jvm.options", "\\s");
        }
        if (properties.getFileProperty(prefix + "log4j.file") != null) {
            log4jFile = properties.getFileProperty(prefix + "log4j.file");
        }
        if (properties.getIntProperty(prefix + "memory.size", 0) != 0) {
            memorySize = properties.getIntProperty(prefix + "memory.size", 0);
        }
    }

    /**
     * Set any unset settings from the given other object
     * 
     * @param other
     *            source application object
     */
    void resolve(Application other) {
        if (other == null) {
            return;
        }

        if (other.name != null && name == null) {
            name = other.name;
        }

        if (other.mainClass != null && mainClass == null) {
            this.mainClass = other.mainClass;
        }

        if (other.arguments != null && arguments == null) {
            arguments = new ArrayList<String>();
            arguments.addAll(other.arguments);
        }

        if (other.libs != null && libs == null) {
            libs = new ArrayList<File>();
            libs.addAll(other.libs);
        }

        if (other.inputFiles != null && inputFiles == null) {
            inputFiles = new ArrayList<File>();
            inputFiles.addAll(other.inputFiles);
        }

        if (other.outputFiles != null && outputFiles == null) {
            outputFiles = new ArrayList<File>();
            outputFiles.addAll(other.outputFiles);
        }

        if (other.systemProperties != null && systemProperties == null) {
            for (Map.Entry<String, String> entry : other.systemProperties
                    .entrySet()) {
                setSystemProperty(entry.getKey(), entry.getValue());
            }
        }

        if (other.jvmOptions != null && jvmOptions == null) {
            jvmOptions = new ArrayList<String>();
            jvmOptions.addAll(other.jvmOptions);
        }

        if (other.log4jFile != null && log4jFile == null) {
            this.log4jFile = other.log4jFile;
        }

        if (other.memorySize != 0 && memorySize == 0) {
            this.memorySize = other.memorySize;
        }
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
     * @throws Exception
     *             if the name contains spaces or periods, or an application
     *             with the given name already exists in the applicationSet
     */
    public void setName(String name) throws Exception {
        if (name == null) {
            return;
        }

        if (name.equals(this.name)) {
            // name unchanged
            return;
        }

        if (name.contains(".")) {
            throw new Exception("application name cannot contain periods : \""
                    + name + "\"");
        }

        if (name.contains(" ")) {
            throw new Exception("application name cannot contain spaces : \""
                    + name + "\"");
        }

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
    public void setArguments(String... arguments) {
        if (arguments == null) {
            this.arguments = null;
        } else {
            this.arguments = new ArrayList<String>();
            this.arguments.addAll(Arrays.asList(arguments));
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
    public void setLibs(File... libs) {
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
    public void setInputFiles(File... inputFiles) {
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
    public void setOutputFiles(File... outputFiles) {
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
    public void setJVMOptions(String... jvmOptions) {
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
     * Log4j properties file used for application.
     * 
     * @return Log4j properties file used for application.
     */
    public File getLog4jFile() {
        return log4jFile;
    }

    /**
     * Sets Log4j properties file used for application.
     * 
     * @param log4jFile
     *            Log4j properties file used for application.
     */
    public void setLog4jFile(File log4jFile) {
        this.log4jFile = log4jFile;
    }

    /**
     * Returns the amount of memory used for the application, in Megabytes.
     * Defaults to 0 for "unset"
     * 
     * @return the amount of memory used for the application, in Megabytes.
     */
    public int getMemorySize() {
        return memorySize;
    }

    /**
     * Sets the amount of memory used by the application, in Megabytes.
     * 
     * @param memorySize
     *            the new amount of memory for this application, in Megabytes.
     */
    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
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
        boolean empty = true;

        if (prefix == null) {
            throw new Exception("cannot print application to file,"
                    + " prefix is not specified");
        }

        String dotPrefix = prefix + ".";

        if (mainClass != null) {
            out.println(dotPrefix + "main.class = " + mainClass);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "main.class =");
        }

        if (arguments != null) {
            out.println(dotPrefix + "arguments = "
                    + DeployProperties.strings2SSS(arguments));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "arguments =");
        }

        if (libs != null) {
            out.println(dotPrefix + "libs = "
                    + DeployProperties.files2CSS(libs));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "libs =");
        }

        if (inputFiles != null) {
            out.println(dotPrefix + "input.files = "
                    + DeployProperties.files2CSS(inputFiles));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "input.files =");
        }

        if (outputFiles != null) {
            out.println(dotPrefix + "output.files = "
                    + DeployProperties.files2CSS(outputFiles));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "output.files =");
        }

        if (systemProperties != null) {
            out.println(dotPrefix + "system.properties = "
                    + DeployProperties.toCSString(systemProperties));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "system.properties =");
        }

        if (jvmOptions != null) {
            out.println(dotPrefix + "jvm.options = "
                    + DeployProperties.strings2SSS(jvmOptions));
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "java.options =");
        }

        if (log4jFile != null) {
            out.println(dotPrefix + "log4j.file = " + log4jFile);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "log4j.file =");
        }

        if (memorySize != 0) {
            out.println(dotPrefix + "memory.size = " + memorySize);
            empty = false;
        } else if (printComments) {
            out.println("#" + dotPrefix + "memory.size =");
        }

        if (empty && printComments) {
            out.println("#Dummy property to make sure application is actually defined");
            out.println(dotPrefix);
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
        result += " Arguments = " + DeployProperties.strings2SSS(arguments)
                + "\n";
        result += " Libs = " + DeployProperties.files2CSS(libs) + "\n";
        result += " Input Files = " + DeployProperties.files2CSS(inputFiles)
                + "\n";
        result += " Output Files = " + DeployProperties.files2CSS(outputFiles)
                + "\n";
        result += " System properties = "
                + DeployProperties.toCSString(getSystemProperties()) + "\n";
        result += " JVM Options = " + DeployProperties.strings2SSS(jvmOptions)
                + "\n";
        result += " Log4j File = " + getLog4jFile() + "\n";

        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }

}
