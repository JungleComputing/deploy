package ibis.deploy.gui.outputViz.amuse;

import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.scenegraph.OctreeNode;
import ibis.deploy.gui.outputViz.models.Model;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.media.opengl.GL3;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;

public class Hdf5TimedPlayer implements Runnable {
    public static enum states {
        UNOPENED, UNINITIALIZED, INITIALIZED, STOPPED, REDRAWING, SNAPSHOTTING, MOVIEMAKING, CLEANUP, WAITINGONFRAME, PLAYING
    };

    private states currentState = states.UNOPENED;
    private int currentFrame;

    private StarSGNode sgRoot;
    private OctreeNode octreeRoot;

    private boolean running = true;

    private String path = null;
    private String namePrefix = null;
    private final String gravNamePostfix = ".grav";

    private long startTime, stopTime;

    private final JSlider timeBar;
    private final JFormattedTextField frameCounter;

    private HashMap<Integer, Model> starModels;
    private HashMap<Integer, Model> cloudModels;

    private boolean initialized = false;
    private final GLWindow glw;

    public Hdf5TimedPlayer(GLWindow glw, JSlider timeBar, JFormattedTextField frameCounter) {
        this.glw = glw;
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
    }

    public void close() {
        running = false;
        initialized = false;
        currentFrame = 0;
        timeBar.setValue(0);
        frameCounter.setValue(0);
        timeBar.setMaximum(0);
    }

    public void delete(GL3 gl) {
        for (Entry<Integer, Model> e : starModels.entrySet()) {
            Model m = e.getValue();
            m.delete(gl);
        }
        for (Entry<Integer, Model> e : cloudModels.entrySet()) {
            Model m = e.getValue();
            m.delete(gl);
        }
    }

    public void open(String path, String namePrefix) {
        this.path = path;
        this.namePrefix = namePrefix;
    }

    public void init() {
        if (path == null) {
            System.err.println("HDFTimer initialized with no open file.");
            System.exit(1);
        }

        // The star and gas models can be re-used for efficiency, we therefore
        // store them in these central databases
        starModels = new HashMap<Integer, Model>();
        cloudModels = new HashMap<Integer, Model>();

        int initialMaxBar = Hdf5StarReader.getNumFiles(path, gravNamePostfix);
        timeBar.setMaximum(initialMaxBar);

        updateFrame();

        initialized = true;
    }

    @Override
    public void run() {
        if (!initialized) {
            System.err.println("HDFTimer started while not initialized.");
            System.exit(1);
        }

        currentState = states.PLAYING;

        while (running) {
            if (currentState == states.PLAYING || currentState == states.MOVIEMAKING) {
                try {
                    startTime = System.currentTimeMillis();

                    System.out.println("stars: " + starModels.size() + " clouds: " + cloudModels.size());

                    if (currentState == states.MOVIEMAKING) {
                        Vec3 rotation = glw.getRotation();
                        for (int i = 0; i < 5; i++) {
                            glw.makeSnapshot("" + (currentFrame * 5 + i));

                            rotation.set(1, rotation.get(1) + .5f);
                            glw.setRotation(rotation);

                            Thread.sleep(GLWindow.waittime);
                        }
                    }

                    currentFrame++;

                    timeBar.setValue(currentFrame);
                    frameCounter.setValue(currentFrame);

                    updateFrame();

                    stopTime = System.currentTimeMillis();
                    if (startTime - stopTime < GLWindow.waittime) {
                        Thread.sleep(GLWindow.waittime - (startTime - stopTime));
                    } else {
                        // Keep interactivity intact
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while playing.");
                }

            } else if (currentState == states.STOPPED) {
                try {
                    Thread.sleep(GLWindow.LONGWAITTIME);
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while stopped.");
                }
            } else if (currentState == states.WAITINGONFRAME) {
                try {
                    Thread.sleep(GLWindow.LONGWAITTIME);
                    currentState = states.PLAYING;
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting.");
                }
            }
            System.gc();
        }
    }

    private void updateFrame() {
        Hdf5Snapshotter snappy = new Hdf5Snapshotter();
        snappy.open(namePrefix, currentFrame, GLWindow.level_of_detail, starModels, cloudModels);
        StarSGNode newSgRoot = snappy.getSgRoot();
        OctreeNode newOctreeRoot = snappy.getOctreeRoot();

        synchronized (this) {
            sgRoot = newSgRoot;
            octreeRoot = newOctreeRoot;
        }
    }

    public void start() {
        GLWindow.axes = true;
        currentState = states.PLAYING;
    }

    public void stop() {
        currentState = states.STOPPED;
    }

    public void rewind() {
        setFrame(0);
    }

    public void setFrame(int value) {
        System.out.println("setValue?");
        currentState = states.STOPPED;
        currentFrame = value;

        timeBar.setValue(currentFrame);
        frameCounter.setValue(currentFrame);

        updateFrame();
    }

    public void movieMode() {
        currentState = states.MOVIEMAKING;
    }

    public states getState() {
        return currentState;
    }

    public int getFrame() {
        return currentFrame;
    }

    public StarSGNode getSgRoot() {
        synchronized (this) {
            return sgRoot;
        }
    }

    public OctreeNode getOctreeRoot() {
        synchronized (this) {
            return octreeRoot;
        }
    }
}
