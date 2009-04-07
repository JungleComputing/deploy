package ibis.deploy;

import java.util.ArrayList;
import java.util.Map;

import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JavaSoftwareDescription;
import org.gridlab.gat.resources.SoftwareDescription;

public class JythonSoftwareDescription extends JavaSoftwareDescription {

    private String jythonJar;

    private String pythonScript;

    private String pythonPath;

    public String getJythonJar() {
        return jythonJar;
    }

    public void setJythonJar(String jythonJar) {
        this.jythonJar = jythonJar;
    }

    public String getPythonScript() {
        return pythonScript;
    }

    public void setPythonScript(String pythonScript) {
        this.pythonScript = pythonScript;
    }

    public String getPythonPath() {
        return pythonPath;
    }

    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }

    /**
     * Constructs the command line arguments from the class path, the jvm
     * options, the system properties, the main and the java arguments of this
     * {@link SoftwareDescription}.
     * 
     * @return the command line arguments
     */
    public String[] getArguments() {
        ArrayList<String> result = new ArrayList<String>();
        if (getJavaOptions() != null) {
            for (String option : getJavaOptions()) {
                result.add(option);
            }
        }
        if (getJavaClassPath() != null) {
            result.add("-classpath");
            result.add(getJavaClassPath());
        }

        if (getJavaSystemProperties() != null) {
            Map<String, String> properties = getJavaSystemProperties();
            for (String key : properties.keySet()) {
                // null values ignored
                if (properties.get(key) != null) {
                    result.add("-D" + key + "=" + properties.get(key));
                }
            }
        }

        if (pythonPath != null) {
            result.add("-Dpython.path=" + pythonPath);
        }

        if (jythonJar != null) {
            result.add("-jar");
            result.add(jythonJar);
        } else {
            return null;
        }

        if (pythonScript != null) {
            result.add("lib" + File.separator + pythonScript);
        } else {
            return null;
        }

        if (getJavaArguments() != null) {
            for (String javaArgument : getJavaArguments()) {
                result.add(javaArgument);
            }
        }
        return result.toArray(new String[result.size()]);
    }

}
