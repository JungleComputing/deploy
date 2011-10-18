package ibis.deploy.gui.outputViz;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.outputViz.amuse.Hdf5TimedPlayer;
import ibis.deploy.gui.outputViz.common.InputHandler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.media.opengl.DefaultGLCapabilitiesChooser;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.util.FPSAnimator;

public class OutputVizPanel extends JPanel {
    private static final long serialVersionUID = 4754345291079348455L;

    public static final int DEFAULT_SCREEN_WIDTH = 1024;
    public static final int DEFAULT_SCREEN_HEIGHT = 768;

    public static final int SCREENSHOT_SCREEN_WIDTH = 8080;
    public static final int SCREENSHOT_SCREEN_HEIGHT = 5200;

    private static String cmdlnfileName;
    private GLWindow window;
    private GLCanvas glcanvas;
    private GLContext offScreenContext;

    public Hdf5TimedPlayer timer;
    public JSlider timeBar;
    public JFormattedTextField frameCounter;

    private String path, prefix;

    public OutputVizPanel(final GUI gui) {
        final JButton initButton = new JButton("Initialize 3D Visualization");
        add(initButton);

        initButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeAll();
            }
        });
    }

    public OutputVizPanel() {
        initialize(null);
    }

    public void initialize(GUI gui) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        setLayout(new BorderLayout(0, 0));

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
                    new DefaultGLCapabilitiesChooser(),
                    SCREENSHOT_SCREEN_WIDTH, SCREENSHOT_SCREEN_HEIGHT, null);

            offScreenContext = pbuffer.createContext(null);
            offScreenContext.setSynchronized(true);

            if (pbuffer == null || offScreenContext == null) {
                System.err.println("PBuffer failed.");
            }
        } else {
            System.err.println("No offscreen rendering support.");
        }

        // Standard GL3 capabilities
        GLCapabilities glCapabilities = new GLCapabilities(glp);

        // glCapabilities.setDoubleBuffered(true);
        glCapabilities.setHardwareAccelerated(true);

        // Anti-Aliasing
        glCapabilities.setSampleBuffers(true);
        glCapabilities.setNumSamples(4);

        glcanvas = new GLCanvas(glCapabilities, offScreenContext);

        // Make the GLEventListener
        window = new GLWindow(this, offScreenContext);
        glcanvas.addGLEventListener(window);

        // Add Mouse event listener
        InputHandler inputHandler = new InputHandler(window);
        glcanvas.addMouseListener(inputHandler);
        glcanvas.addMouseMotionListener(inputHandler);
        glcanvas.addMouseWheelListener(inputHandler);

        // Add key event listener
        glcanvas.addKeyListener(inputHandler);

        // Set up animator
        final FPSAnimator animator = new FPSAnimator(glcanvas, 60);
        animator.start();

        timeBar = new CustomJSlider();
        timeBar.setValue(0);
        timeBar.setMajorTickSpacing(5);
        timeBar.setMinorTickSpacing(1);
        timeBar.setMaximum(0);
        timeBar.setMinimum(0);
        timeBar.setPaintTicks(true);
        timeBar.setSnapToTicks(true);

        timer = new Hdf5TimedPlayer(window, timeBar, frameCounter);

        // Make the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                File file = openFile();
                if (file != null) {
                    path = file.getPath().substring(0,
                            file.getPath().length() - file.getName().length());

                    String name = file.getName();
                    String fullPath = path + name;
                    String[] ext = fullPath.split("[.]");
                    if (!(ext[1].compareTo("evo") == 0
                            || ext[1].compareTo("grav") == 0
                            || ext[1].compareTo("add") == 0
                            || ext[1].compareTo("gas") == 0 || ext[1]
                            .compareTo("data") == 0)) {
                        JOptionPane pane = new JOptionPane();
                        pane.setMessage("Tried to open invalid file type.");
                        JDialog dialog = pane.createDialog("Alert");
                        dialog.setVisible(true);
                    } else {
                        prefix = ext[0].substring(0, ext[0].length() - 6);
                        window.stopAnimation();
                        timer = new Hdf5TimedPlayer(window, timeBar,
                                frameCounter);
                        timer.open(path, prefix);
                        window.startAnimation(timer);
                    }
                }
            }
        });
        file.add(open);
        menuBar.add(file);
        JMenu options = new JMenu("Options");
        // JMenuItem gas = new JMenuItem("Gas Toggle");
        // gas.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setGas(!GLWindow.GAS_ON);
        // }
        // });
        // options.add(gas);
        //
        // JMenuItem colormap = new JMenuItem("Gas Colormap Toggle");
        // colormap.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setColormap(!GLWindow.GAS_COLORMAP);
        // }
        // });
        // options.add(colormap);
        //
        JMenuItem postprocess = new JMenuItem("Post-Process (blur) Toggle");
        postprocess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                GLWindow.setPostprocess(!GLWindow.isPostprocess());
            }
        });
        options.add(postprocess);

        JMenu lod = new JMenu("Level of detail.");
        JMenuItem zero = new JMenuItem("Low.");
        zero.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                GLWindow.setLOD(0);
            }
        });
        lod.add(zero);
        JMenuItem one = new JMenuItem("Medium.");
        one.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                GLWindow.setLOD(1);
            }
        });
        lod.add(one);
        JMenuItem two = new JMenuItem("High.");
        two.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                GLWindow.setLOD(2);
            }
        });
        lod.add(two);
        // JMenuItem hundred = new JMenuItem("100 elements per node.");
        // hundred.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setLOD(100);
        // }
        // });
        // lod.add(hundred);
        // JMenuItem twohundred = new JMenuItem("200 elements per node.");
        // twohundred.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setLOD(200);
        // }
        // });
        // lod.add(twohundred);
        options.add(lod);

        JMenuItem makeMovie = new JMenuItem("Make movie from this angle.");
        makeMovie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                timer.movieMode();
            }
        });
        options.add(makeMovie);

        // JMenuItem axes = new JMenuItem("Axes Toggle");
        // axes.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setAxes(!GLWindow.AXES);
        // }
        // });
        // options.add(axes);
        //
        // JMenuItem prediction = new JMenuItem("Prediction Toggle");
        // prediction.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setPrediction(!GLWindow.PREDICTION_ON);
        // }
        // });
        // options.add(prediction);
        menuBar.add(options);

        // Make the "media player" panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        JButton button0 = new JButton("Rewind");
        button0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.rewind();
            }
        });
        bottomPanel.add(button0);

        JButton button1 = new JButton("Pause");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
            }
        });
        bottomPanel.add(button1);

        JButton button2 = new JButton("Snapshot!");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // timer.stop();
                String fileName = "" + timer.getFrame() + " {"
                        + window.getRotation().get(0) + ","
                        + window.getRotation().get(1) + " - "
                        + window.getViewDist() + "} ";
                window.makeSnapshot(fileName);
            }
        });
        bottomPanel.add(button2);

        JButton button3 = new JButton("Play");
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.start();
            }
        });
        bottomPanel.add(button3);

        frameCounter = new JFormattedTextField();
        frameCounter.setValue(new Integer(1));
        frameCounter.setColumns(4);
        frameCounter.setMaximumSize(new Dimension(40, 20));
        frameCounter.setValue(0);
        frameCounter.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                JFormattedTextField source = (JFormattedTextField) e
                        .getSource();
                if (source.hasFocus()) {
                    if (source == frameCounter) {
                        if (window.timerInitialized)
                            timer.setFrame(((Number) frameCounter.getValue())
                                    .intValue());
                    }
                }
            }
        });

        bottomPanel.add(frameCounter);

        timeBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    timer.setFrame(timeBar.getValue());
                }
            }
        });
        bottomPanel.add(timeBar);

        add(menuBar, BorderLayout.NORTH);
        add(glcanvas, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        glcanvas.setFocusable(true);
        glcanvas.requestFocusInWindow();
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
                    JOptionPane pane = new JOptionPane();
                    pane.setMessage("Tried to open invalid file type.");
                    JDialog dialog = pane.createDialog("Alert");
                    dialog.setVisible(true);
                } else {
                    prefix = ext[0].substring(0, ext[0].length() - 6);
                    window.stopAnimation();
                    timer = new Hdf5TimedPlayer(window, timeBar, frameCounter);
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
            } else {
                cmdlnfileName = null;
            }
        }

        final JFrame frame = new JFrame("Ibis Deploy - OutputViz Testframe");
        frame.setPreferredSize(new Dimension(DEFAULT_SCREEN_WIDTH,
                DEFAULT_SCREEN_HEIGHT));

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    final OutputVizPanel bla = new OutputVizPanel();
                    frame.getContentPane().add(bla);

                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            bla.close();
                            System.exit(0);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });

        // Display the window.
        frame.pack();

        // center on screen
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    private void close() {
        window.dispose(glcanvas);
    }

    private File openFile() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        // user clicked Cancel button on dialog
        if (result == JFileChooser.CANCEL_OPTION)
            return null;
        else
            return fileChooser.getSelectedFile();
    }

    public String getPath() {
        return path;
    }
}
