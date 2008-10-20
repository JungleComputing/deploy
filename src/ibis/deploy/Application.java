package ibis.deploy;

import ibis.util.TypedProperties;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {

	// name of application
	private String name;

	// main class of application
	private String mainClass;

	// files and dirs which need to be in the classpath
	// automatically prestaged aswell.
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

	public Application() {
		mainClass = null;
		libs = new ArrayList<File>();
		arguments = new ArrayList<String>();
		inputFiles = new ArrayList<File>();
		outputFiles = new ArrayList<File>();
		systemProperties = new HashMap<String, String>();
		javaOptions = new ArrayList<String>();
	}

	/**
	 * Load application from the given properties (usually loaded from an
	 * application file)
	 * 
	 * @param properties
	 *            properties to load application from
	 * @param object
	 *            name of this application, or null to load "defaults"
	 *            application
	 * @throws Exception
	 *             if application cannot be read properly
	 */
	public Application(TypedProperties properties, String name) throws Exception  {
		String prefix;
		if (name == null) {
			prefix = "";
		} else {
			prefix = name + ".";
		}
		
		mainClass = properties.getProperty(prefix + "main.class");
		libs = parseFileString(properties.getProperty(prefix + "libs"));
		arguments = parseStringList(properties.getProperty(prefix + "arguments"));
		inputFiles = parseFileString(properties.getProperty(prefix + "input.files"));
		outputFiles = parseFileString(properties.getProperty(prefix + "output.files"));
		
		systemProperties = new HashMap<String, String>();
		String[] propertyStrings = properties.getStringList(prefix + "system.properties");
		for(String string: propertyStrings) {
			String[] keyValue = string.split("=", 2);
			if (keyValue.length == 2) {
				systemProperties.put(keyValue[0], keyValue[1]);
			} else if (keyValue.length == 1) {
				systemProperties.put(keyValue[0], null);
			} else {
				throw new Exception("invalid system property specification: " + string);
			}
		}

		javaOptions = parseStringList(properties.getProperty(prefix + "java.options"));
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public List<File> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(List<File> inputFiles) {
		this.inputFiles = inputFiles;
	}

	public void addInputFile(File file) {
		inputFiles.add(file);
	}

	public List<String> getJavaOptions() {
		return javaOptions;
	}

	public void setJavaOptions(List<String> javaOptions) {
		this.javaOptions = javaOptions;
	}

	public void addJavaOption(String option) {
		javaOptions.add(option);
	}

	public List<File> getLibs() {
		return libs;
	}

	public void setLibs(List<File> libs) {
		this.libs = libs;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public List<File> getOutputFiles() {
		return outputFiles;
	}

	public void setOutputFiles(List<File> outputFiles) {
		this.outputFiles = outputFiles;
	}

	public void addOutputFile(File file) {
		outputFiles.add(file);
	}

	public Map<String, String> getSystemProperties() {
		return systemProperties;
	}

	public void setSystemProperties(Map<String, String> systemProperties) {
		this.systemProperties = systemProperties;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String printFileList(List<File> list) {
		if (list.size() == 0) {
			return "";
		}
		String result = "";
		for (File object : list) {
			result = result + object.toString() + ",";
		}
		return result.substring(0, result.length() - 1);
	}

	private List<File> parseFileString(String string) {
		List<File> result = new ArrayList<File>();
		
		if (string == null) {
			return result;
		}

		String[] list = string.split(",");
		for (String element : list) {
			if (element != null && element.length() > 0) {
				result.add(new File(element));
			}
		}
		return result;
	}

	private String printStringList(List<String> list) {
		if (list.size() == 0) {
			return "";
		}
		String result = "";
		for (String object : list) {
			result = result + object.toString() + ",";
		}
		return result.substring(0, result.length() - 1);
	}

	private List<String> parseStringList(String string) {
		List<String> result = new ArrayList<String>();

		if (string == null) {
			return result;
		}
		
		String[] list = string.split(",");
		for (String element : list) {
			if (element != null && element.length() > 0) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Print the settings of this application to a (properties) file
	 * 
	 * @param out
	 *            stream to write this file to
	 * @param if
	 *            true, key/value lines prepended with the application name
	 */
	public void print(PrintWriter out, boolean prependName) {
		String prefix;

		if (prependName) {
			prefix = name + ".";
		} else {
			prefix = "";
		}

		out.println(prefix + "main.class=" + mainClass);
		out.println(prefix + "libs=" + printFileList(libs));
		out.println(prefix + "arguments=" + printStringList(arguments));

		out.println(prefix + "input.files=" + printFileList(inputFiles));
		out.println(prefix + "output.files=" + printFileList(outputFiles));

		out.print(prefix + "system.properties=");
		for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
			out.print(entry.getKey() + "=" + entry.getValue() + ",");
		}
		out.println();

		out.println(prefix + "java.options=" + printStringList(javaOptions));

	}

}
