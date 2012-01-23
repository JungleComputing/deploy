package ibis.amuse.visualization.amuseAdaptor;

import ibis.amuse.visualization.GLOffscreenWindow;
import ibis.amuse.visualization.GLWindow;
import ibis.amuse.visualization.Settings;
import ibis.amuse.visualization.openglCommon.exceptions.FileOpeningException;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.scenegraph.OctreeNode;

import java.util.ArrayList;
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

    private ArrayList<Star> stars;
    private OctreeNode octreeRoot;

    private boolean running = true, cli = false;

    private String path = null;
    private String namePrefix = null;
    private final String gravNamePostfix = ".grav";

    private long startTime, stopTime;

    private JSlider timeBar;
    private JFormattedTextField frameCounter;

    private HashMap<Integer, Model> starModels;
    private HashMap<Integer, Model> cloudModels;

    private boolean initialized = false;
    private GLWindow glw;
    private GLOffscreenWindow glow;

    public Hdf5TimedPlayer(GLWindow glw, JSlider timeBar,
            JFormattedTextField frameCounter) {
        this.glw = glw;
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
    }

    public Hdf5TimedPlayer(GLOffscreenWindow glow) {
        cli = true;
        this.glow = glow;
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

        if (!cli) {
            timeBar.setMaximum(initialMaxBar);
        }

        try {
            updateFrame();
        } catch (FileOpeningException e) {
            System.err.println("Failed to open file.");
            System.exit(1);
        }

        initialized = true;
    }

    @Override
    public void run() {
        if (!initialized) {
            System.err.println("HDFTimer started while not initialized.");
            System.exit(1);
        }

        currentFrame = Settings.getInitialSimulationFrame();

        if (!cli) {
            glw.setRotation(new Vec3(Settings.getInitialRotationX(), Settings
                    .getInitialRotationY(), 0f));
            glw.setViewDist(Settings.getInitialZoom());

            try {
                updateFrame();
            } catch (FileOpeningException e) {
                System.err.println("Initial file not found");
                System.exit(1);
            }

            if (!cli) {
                timeBar.setValue(currentFrame);
                frameCounter.setValue(currentFrame);
            }

            currentState = states.STOPPED;
        } else {
            glow.setRotation(new Vec3(Settings.getInitialRotationX(), Settings
                    .getInitialRotationY(), 0f));
            glow.setViewDist(Settings.getInitialZoom());

            try {
                updateFrame();
            } catch (FileOpeningException e) {
                System.err.println("Initial file not found");
                System.exit(1);
            }

            currentState = states.MOVIEMAKING;
        }

        while (running) {
            if (currentState == states.PLAYING
                    || currentState == states.REDRAWING
                    || currentState == states.MOVIEMAKING) {
                try {
                    startTime = System.currentTimeMillis();

                    // System.out.println("stars: " + starModels.size() +
                    // " clouds: " + cloudModels.size());

                    try {
                        updateFrame();
                    } catch (FileOpeningException e) {
                        setFrame(currentFrame - 1);
                        currentState = states.WAITINGONFRAME;
                        System.err
                                .println("File not found, retrying from frame "
                                        + currentFrame + ".");
                        continue;
                    }

                    if (currentState == states.MOVIEMAKING) {
                        if (Settings.getMovieRotate()) {
                            if (!cli) {
                                Vec3 rotation = glw.getRotation();
                                System.out.println("Simulation frame: "
                                        + currentFrame + ", Rotation x: "
                                        + rotation.get(0) + " y: "
                                        + rotation.get(1));
                                glw.makeSnapshot(String.format("%05d",
                                        (currentFrame)));

                                rotation.set(1, rotation.get(1)
                                        + Settings.getMovieRotationSpeedDef());
                                glw.setRotation(rotation);

                                // glw.makeSnapshot(String.format("%05d",
                                // (currentFrame * 3 + 1)));
                                //
                                // rotation.set(1, rotation.get(1)
                                // + Settings.getPerFrameRotation());
                                // glw.setRotation(rotation);
                                //
                                // glw.makeSnapshot(String.format("%05d",
                                // (currentFrame * 3 + 2)));
                                //
                                // rotation.set(1, rotation.get(1)
                                // + Settings.getPerFrameRotation());
                                // glw.setRotation(rotation);
                            } else {
                                Vec3 rotation = glow.getRotation();
                                System.out.println("Simulation frame: "
                                        + currentFrame + ", Rotation x: "
                                        + rotation.get(0) + " y: "
                                        + rotation.get(1));
                                glow.makeSnapshot(String.format("%05d",
                                        (currentFrame) * 2 + 0));

                                rotation.set(1, rotation.get(1)
                                        + Settings.getMovieRotationSpeedDef());
                                glow.setRotation(rotation);

                                glow.makeSnapshot(String.format("%05d",
                                        (currentFrame * 2 + 1)));

                                rotation.set(1, rotation.get(1)
                                        + Settings.getMovieRotationSpeedDef());
                                glow.setRotation(rotation);

                                // glow.makeSnapshot(String.format("%05d",
                                // (currentFrame * 3 + 2)));
                                //
                                // rotation.set(1, rotation.get(1) +
                                // Settings.getPerFrameRotation());
                                // glow.setRotation(rotation);
                            }
                        } else {
                            if (!cli) {
                                glw.makeSnapshot(String.format("%05d",
                                        currentFrame));
                            } else {
                                glow.makeSnapshot(String.format("%05d",
                                        currentFrame));
                            }
                        }

                        // Thread.sleep(GLWindow.getWaittime());
                    }

                    if (currentState != states.REDRAWING) {
                        currentFrame++;
                    }

                    if (!cli) {
                        timeBar.setValue(currentFrame);
                        frameCounter.setValue(currentFrame);
                    }

                    stopTime = System.currentTimeMillis();
                    if (startTime - stopTime < GLWindow.getWaittime() && !cli
                            && currentState != states.MOVIEMAKING) {
                        Thread.sleep(GLWindow.getWaittime()
                                - (startTime - stopTime));
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
            } else if (currentState == states.REDRAWING) {
                currentState = states.STOPPED;
            } else if (currentState == states.WAITINGONFRAME) {
                try {
                    Thread.sleep(GLWindow.LONGWAITTIME);
                    currentState = states.PLAYING;
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting.");
                }
            }
            // System.gc();
        }
    }

    private void updateFrame() throws FileOpeningException {
        Hdf5Snapshotter snappy = new Hdf5Snapshotter();
        if (!cli) {
            snappy.open(namePrefix, currentFrame, GLWindow.getLOD(),
                    cloudModels);
        } else {
            snappy.open(namePrefix, currentFrame, GLOffscreenWindow.getLOD(),
                    cloudModels);
        }
        ArrayList<Star> newStars = snappy.getStars();
        OctreeNode newOctreeRoot = snappy.getOctreeRoot();

        synchronized (this) {
            this.stars = newStars;
            this.octreeRoot = newOctreeRoot;
        }
    }

    public void start() {
        currentState = states.PLAYING;
    }

    public void stop() {
        currentState = states.STOPPED;
    }

    public void rewind() {
        setFrame(0);
    }

    public void setFrame(int value) {
        // System.out.println("setValue?");
        currentState = states.STOPPED;
        currentFrame = value;

        timeBar.setValue(currentFrame);
        frameCounter.setValue(currentFrame);

        try {
            updateFrame();
        } catch (FileOpeningException e) {
            System.err.println("File not found, retrying from frame "
                    + currentFrame + ".");

            setFrame(value - 1);
            currentState = states.WAITINGONFRAME;
        } catch (Throwable t) {
            System.err.println("Got error in Hdf5TimedPlayer.setFrame!");
            t.printStackTrace(System.err);
        }
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

    public synchronized ArrayList<Star> getStars() {
        return stars;
    }

    public synchronized OctreeNode getOctreeRoot() {
        return octreeRoot;
    }

    public void oneBack() {
        stop();
        setFrame(currentFrame - 1);
    }

    public void oneForward() {
        stop();
        setFrame(currentFrame + 1);
    }

    public void redraw() {
        if (initialized) {
            setFrame(currentFrame);
            currentState = states.REDRAWING;
        }
    }

    public boolean isPlaying() {
        if (currentState == states.PLAYING
                || currentState == states.MOVIEMAKING)
            return true;

        return false;
    }
}
