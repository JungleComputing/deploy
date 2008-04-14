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

	private String javaClassPath;

	private String[] preStageSet;

	private String[] postStageSet;

	public Application(String name, String javaMain, String[] javaOptions,
			String[] javaSystemProperties, String[] javaArguments,
			String javaClassPath, String[] preStageSet, String[] postStageSet) {
		this.name = name;
		this.javaMain = javaMain;
		this.javaOptions = javaOptions;
		this.javaSystemProperties = javaSystemProperties;
		this.javaArguments = javaArguments;
		this.javaClassPath = javaClassPath;
		this.preStageSet = preStageSet;
		this.postStageSet = postStageSet;
	}

	// public Object clone() {
	// return new Application(name, main, javaFlags, parameters, preStaged,
	// postStaged, classpath);
	// }

	public String getName() {
		return name;
	}

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
			String javaClassPath = TypedPropertiesUtility
					.getHierarchicalProperty(applicationProps, app,
							"java.classpath", "");
			String[] preStageSet = TypedPropertiesUtility
					.getHierarchicalStringList(applicationProps, app,
							"prestage", null, " ");
			String[] postStageSet = TypedPropertiesUtility
					.getHierarchicalStringList(applicationProps, app,
							"poststage", null, " ");
			result.add(new Application(app, javaMain, javaOptions,
					javaSystemProperties, javaArguments, javaClassPath,
					preStageSet, postStageSet));
		}
		return result;
	}

	public String[] getJavaArguments() {
		return javaArguments;
	}

	public String getJavaClassPath() {
		return javaClassPath;
	}

	public String getJavaMain() {
		return javaMain;
	}

	public String[] getJavaOptions() {
		return javaOptions;
	}

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

	public String[] getPostStageSet() {
		return postStageSet;
	}

	public String[] getPreStageSet() {
		return preStageSet;
	}

	// public String toString() {
	// String res = "Application " + name + "\n";
	// res += " main: " + javaMain + "\n";
	// res += " arguments:";
	// for (int i = 0; i < javaArguments.length; i++) {
	// res += " " + javaArguments[i];
	// }
	// res += "\n";
	//
	// res += " java flags:";
	// for (int i = 0; i < javaFlags.length; i++) {
	// res += " " + javaFlags[i];
	// }
	// res += "\n";
	//
	// res += " pre staged:";
	// for (int i = 0; i < preStaged.length; i++) {
	// res += " " + preStaged[i];
	// }
	// res += "\n";
	//
	// res += " post staged:";
	// for (int i = 0; i < postStaged.length; i++) {
	// res += " " + postStaged[i];
	// }
	// res += "\n";
	//
	// return res;
	// }

}
