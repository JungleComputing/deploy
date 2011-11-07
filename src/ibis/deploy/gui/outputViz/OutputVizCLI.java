package ibis.deploy.gui.outputViz;

import ibis.deploy.gui.outputViz.amuse.Hdf5TimedPlayer;

import java.io.File;

import javax.media.opengl.DefaultGLCapabilitiesChooser;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.GLProfile;

public class OutputVizCLI {
    private static String cmdlnfileName;
    private GLOffscreenWindow window;
    private GLContext offScreenContext;

    public Hdf5TimedPlayer timer;

    private String path, prefix;

    public OutputVizCLI() {
        initialize();
    }

    public void initialize() {
        GLProfile.initSingleton(true);

        GLProfile glp = GLProfile.getDefault();
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);

        // Make the offscreen context for screenshotting
        if (factory.canCreateGLPbuffer(factory.getDefaultDevice())) {
            GLCapabilities offScreenCapabilities = new GLCapabilities(glp);
            offScreenCapabilities.setHardwareAccelerated(true);
            offScreenCapabilities.setDoubleBuffered(false);

            // Anti-Aliasing
            offScreenCapabilities.setSampleBuffers(true);
            offScreenCapabilities.setNumSamples(4);

            GLPbuffer pbuffer = factory.createGLPbuffer(factory
                    .getDefaultDevice(), offScreenCapabilities,
                    new DefaultGLCapabilitiesChooser(), Settings
                            .getScreenshotScreenWidth(), Settings
                            .getScreenshotScreenHeight(), null);

            offScreenContext = pbuffer.createContext(null);
            offScreenContext.setSynchronized(true);

            if (pbuffer == null || offScreenContext == null) {
                System.err.println("PBuffer failed.");
            }
        } else {
            System.err.println("No offscreen rendering support.");
        }

        window = new GLOffscreenWindow(this, offScreenContext);
        window.init();
    }

    public void callback() {
        if (cmdlnfileName != null) {
            File cmdlnfile = new File(cmdlnfileName);
            if (cmdlnfile != null) {
                path = cmdlnfile.getPath().substring(
                        0,
                        cmdlnfile.getPath().length()
                                - cmdlnfile.getName().length());
                String name = cmdlnfile.getName();
                String fullPath = path + name;
                String[] ext = fullPath.split("[.]");
                if (!(ext[1].compareTo("evo") == 0
                        || ext[1].compareTo("grav") == 0
                        || ext[1].compareTo("add") == 0
                        || ext[1].compareTo("gas") == 0 || ext[1]
                        .compareTo("data") == 0)) {
                    System.out.println("Tried to open invalid file type.");
                } else {
                    prefix = ext[0].substring(0, ext[0].length() - 6);
                    window.stopAnimation();
                    timer = new Hdf5TimedPlayer(window);
                    timer.open(path, prefix);
                    window.startAnimation(timer);
                }
            }
        }
    }

    public static void main(String[] arguments) {
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-o")) {
                i++;
                cmdlnfileName = arguments[i];
            } else if (arguments[i].equals("-resume")) {
                i++;
                Settings.setInitial_simulation_frame(Integer
                        .parseInt(arguments[i]));
                i++;
                Settings.setInitial_rotation_x(Float.parseFloat(arguments[i]));
                i++;
                Settings.setInitial_rotation_y(Float.parseFloat(arguments[i]));
            } else {
                cmdlnfileName = null;
            }
        }

        @SuppressWarnings("unused")
        final OutputVizCLI bla = new OutputVizCLI();
    }

    public String getPath() {
        return path;
    }
}
