package ibis.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
	
	//main class of application
	private String mainClass;
	
	//files and dirs which need to be in the classpath
	//automatically prestaged aswell.
	private List<File> libs;
	
	//arguments of the application
	private List<String> arguments;
	
	//additional input files (not jars)
	private List<File> inputFiles;
	
	//output files
	private List<File> outputFiles;
	
	//<NAME, VALUE> additional system properties
	private Map<String, String> systemProperties;
	
	//additional JVM options
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

}
