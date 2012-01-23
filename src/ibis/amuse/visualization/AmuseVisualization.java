package ibis.amuse.visualization;

import ibis.amuse.visualization.amuseAdaptor.Hdf5TimedPlayer;
import ibis.amuse.visualization.openglCommon.GLProfileSelector;
import ibis.amuse.visualization.openglCommon.InputHandler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

public class AmuseVisualization extends JPanel {
    private static final long serialVersionUID = 4754345291079348455L;

    public static enum TweakState {
        NONE, VISUAL, MOVIE
    };

    private TweakState currentConfigState = TweakState.VISUAL;

    private static String cmdlnfileName;
    private GLWindow window;
    private GLCanvas glcanvas;
    private GLContext offScreenContext;

    public Hdf5TimedPlayer timer;
    public JSlider timeBar;
    public JFormattedTextField frameCounter;

    private JPanel configPanel;
    private JPanel visualConfig, movieConfig;

    private String path, prefix;

    // public OutputVizPanel(final GUI gui) {
    // final JButton initButton = new JButton("Initialize 3D Visualization");
    // add(initButton);
    //
    // initButton.addActionListener(new ActionListener() {
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // removeAll();
    // }
    // });
    // }

    public AmuseVisualization() {
        initialize();
    }

    public void initialize() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        setLayout(new BorderLayout(0, 0));

        GLProfileSelector.printAvailable();
        GLProfile glp = GLProfile.get(GLProfile.GL3);
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

        // JMenuItem postprocess = new JMenuItem("Post-Process (blur) Toggle");
        // postprocess.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setPostprocess(!GLWindow.isPostprocess());
        // }
        // });
        // options.add(postprocess);

        // JMenu lod = new JMenu("Level of detail.");
        // JMenuItem zero = new JMenuItem("Low.");
        // zero.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setLOD(0);
        // timer.redraw();
        // }
        // });
        // lod.add(zero);
        // JMenuItem one = new JMenuItem("Medium.");
        // one.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setLOD(1);
        // timer.redraw();
        // }
        // });
        // lod.add(one);
        // JMenuItem two = new JMenuItem("High.");
        // two.addActionListener(new ActionListener() {
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // GLWindow.setLOD(2);
        // timer.redraw();
        // }
        // });
        // lod.add(two);
        //
        // options.add(lod);

        JMenuItem makeMovie = new JMenuItem("Make movie.");
        makeMovie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setTweakState(TweakState.MOVIE);
            }
        });
        options.add(makeMovie);

        JMenuItem showTweakPanel = new JMenuItem("Show configuration panel.");
        showTweakPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setTweakState(TweakState.VISUAL);
            }
        });
        options.add(showTweakPanel);
        menuBar.add(options);

        add(menuBar, BorderLayout.NORTH);

        // Make the "media player" panel
        JPanel bottomPanel = createBottomPanel();

        // Add the tweaks panels
        configPanel = new JPanel();
        add(configPanel, BorderLayout.WEST);
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setPreferredSize(new Dimension(200, 0));
        configPanel.setVisible(false);

        visualConfig = new JPanel();
        visualConfig.setLayout(new BoxLayout(visualConfig, BoxLayout.Y_AXIS));
        visualConfig.setMinimumSize(configPanel.getPreferredSize());
        createVisualTweakPanel();

        movieConfig = new JPanel();
        movieConfig.setLayout(new BoxLayout(movieConfig, BoxLayout.Y_AXIS));
        movieConfig.setMinimumSize(configPanel.getPreferredSize());
        createMovieTweakPanel();

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

    // Callback methods for the various ui actions and listeners
    public void setTweakState(TweakState newState) {
        configPanel.setVisible(false);
        configPanel.remove(visualConfig);
        configPanel.remove(movieConfig);

        currentConfigState = newState;

        if (currentConfigState == TweakState.NONE) {
        } else if (currentConfigState == TweakState.VISUAL) {
            configPanel.setVisible(true);
            configPanel.add(visualConfig, BorderLayout.WEST);
        } else if (currentConfigState == TweakState.MOVIE) {
            configPanel.setVisible(true);
            configPanel.add(movieConfig, BorderLayout.WEST);
        }
    }

    private void createVisualTweakPanel() {
        ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                setTweakState(TweakState.NONE);
            }
        };
        visualConfig
                .add(GoggleSwing.titleBox("Visual Configuration", listener));

        ItemListener checkBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Settings.setInvertGasColor(e.getStateChange());
                timer.redraw();
            }
        };
        visualConfig.add(GoggleSwing.checkboxBox("",
                new String[] { "Beamer mode" }, new boolean[] { Settings
                        .invertGasColor() },
                new ItemListener[] { checkBoxListener }));

        ChangeListener overallBrightnessSliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    Settings.setPostprocessingOverallBrightness(source
                            .getValue());
                }
            }
        };
        visualConfig
                .add(GoggleSwing
                        .sliderBox(
                                "Overall Brightness",
                                overallBrightnessSliderListener,
                                (int) (Settings
                                        .getPostprocessingOverallBrightnessMin()),
                                (int) (Settings
                                        .getPostprocessingOverallBrightnessMax()),
                                (int) (0.1f * 10), (int) (Settings
                                        .getPostprocessingOverallBrightness()),
                                new JLabel("")));

        visualConfig.add(GoggleSwing.verticalStrut(5));

        ChangeListener axesBrightnessSliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    Settings.setPostprocessingAxesBrightness(source.getValue());
                }
            }
        };
        visualConfig.add(GoggleSwing.sliderBox("Axes Brightness",
                axesBrightnessSliderListener, (int) (Settings
                        .getPostprocessingAxesBrightnessMin()), (int) (Settings
                        .getPostprocessingAxesBrightnessMax()),
                (int) (0.1f * 10), (int) (Settings
                        .getPostprocessingAxesBrightness()), new JLabel("")));

        visualConfig.add(GoggleSwing.verticalStrut(5));

        ChangeListener gasBrightnessSliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    Settings.setPostprocessingGasBrightness(source.getValue());
                }
            }
        };
        visualConfig.add(GoggleSwing.sliderBox("Gas Brightness",
                gasBrightnessSliderListener, (int) (Settings
                        .getPostprocessingGasBrightnessMin()), (int) (Settings
                        .getPostprocessingGasBrightnessMax()),
                (int) (0.1f * 10), (int) (Settings
                        .getPostprocessingGasBrightness()), new JLabel("")));

        visualConfig.add(GoggleSwing.verticalStrut(5));

        ChangeListener starHaloBrightnessSliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    Settings.setPostprocessingStarHaloBrightness(source
                            .getValue());
                }
            }
        };
        visualConfig.add(GoggleSwing
                .sliderBox("Star Halo Brightness",
                        starHaloBrightnessSliderListener, (int) (Settings
                                .getPostprocessingStarHaloBrightnessMin()),
                        (int) (Settings
                                .getPostprocessingStarHaloBrightnessMax()),
                        (int) (0.1f * 10), (int) (Settings
                                .getPostprocessingStarHaloBrightness()),
                        new JLabel("")));

        visualConfig.add(GoggleSwing.verticalStrut(5));

        ChangeListener starBrightnessSliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    Settings.setPostprocessingStarBrightness(source.getValue());
                }
            }
        };
        visualConfig.add(GoggleSwing.sliderBox("Star Brightness",
                starBrightnessSliderListener, (int) (Settings
                        .getPostprocessingStarBrightnessMin()), (int) (Settings
                        .getPostprocessingStarBrightnessMax()),
                (int) (0.1f * 10), (int) (Settings
                        .getPostprocessingStarBrightness()), new JLabel("")));

        visualConfig.add(GoggleSwing.radioBox("Level of Detail", new String[] {
                "Low", "Medium", "High" }, new ActionListener[] {
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        GLWindow.setLOD(0);
                        timer.redraw();
                    }
                }, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        GLWindow.setLOD(1);
                        timer.redraw();
                    }
                }, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        GLWindow.setLOD(2);
                        timer.redraw();
                    }
                } }));
    }

    private void createMovieTweakPanel() {
        ItemListener listener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                setTweakState(TweakState.NONE);
            }
        };
        movieConfig.add(GoggleSwing.titleBox("Movie Creator", listener));

        ItemListener checkBoxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Settings.setMovieRotate(e.getStateChange());
                timer.redraw();
            }
        };
        movieConfig.add(GoggleSwing.checkboxBox("",
                new String[] { "Rotation" }, new boolean[] { Settings
                        .getMovieRotate() },
                new ItemListener[] { checkBoxListener }));

        final JLabel rotationSetting = new JLabel(""
                + Settings.getMovieRotationSpeedDef());
        ChangeListener movieRotationSpeedListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    Settings.setMovieRotationSpeed(source.getValue() * .25f);
                    rotationSetting.setText(""
                            + Settings.getMovieRotationSpeedDef());
                }
            }
        };
        movieConfig.add(GoggleSwing.sliderBox("Rotation Speed",
                movieRotationSpeedListener, (int) (Settings
                        .getMovieRotationSpeedMin() * 4f), (int) (Settings
                        .getMovieRotationSpeedMax() * 4f), 1, (int) (Settings
                        .getMovieRotationSpeedDef() * 4f), rotationSetting));

        movieConfig.add(GoggleSwing.buttonBox("",
                new String[] { "Start Recording" },
                new ActionListener[] { new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        timer.movieMode();
                    }
                } }));
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

        final JFrame frame = new JFrame("Amuse Visualization");
        frame.setPreferredSize(new Dimension(Settings.getDefaultScreenWidth(),
                Settings.getDefaultScreenHeight()));

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    final AmuseVisualization bla = new AmuseVisualization();
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

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setFocusCycleRoot(true);
        bottomPanel.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            // No focus traversal here, as it makes stuff go bad (some things
            // react on focus).
            @Override
            public Component getLastComponent(Container aContainer) {
                return null;
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return null;
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return null;
            }

            @Override
            public Component getComponentBefore(Container aContainer,
                    Component aComponent) {
                return null;
            }

            @Override
            public Component getComponentAfter(Container aContainer,
                    Component aComponent) {
                return null;
            }
        });

        final JButton oneForwardButton = GoggleSwing.createImageButton(
                "images/media-playback-oneforward.png", "Next", null);
        final JButton oneBackButton = GoggleSwing.createImageButton(
                "images/media-playback-onebackward.png", "Previous", null);
        final JButton rewindButton = GoggleSwing.createImageButton(
                "images/media-playback-rewind.png", "Rewind", null);
        final JButton screenshotButton = GoggleSwing.createImageButton(
                "images/camera.png", "Screenshot", null);
        final JButton playButton = GoggleSwing.createImageButton(
                "images/media-playback-start.png", "Start", null);
        final ImageIcon playIcon = GoggleSwing.createImageIcon(
                "images/media-playback-start.png", "Start");
        final ImageIcon stopIcon = GoggleSwing.createImageIcon(
                "images/media-playback-stop.png", "Start");

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        screenshotButton.addActionListener(new ActionListener() {
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
        bottomPanel.add(screenshotButton);

        rewindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.rewind();
                playButton.setIcon(playIcon);
            }
        });
        bottomPanel.add(rewindButton);

        oneBackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.oneBack();
                playButton.setIcon(playIcon);
            }
        });
        bottomPanel.add(oneBackButton);

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isPlaying()) {
                    timer.stop();
                    playButton.setIcon(playIcon);
                } else {
                    timer.start();
                    playButton.setIcon(stopIcon);
                }
            }
        });
        bottomPanel.add(playButton);

        oneForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.oneForward();
                playButton.setIcon(playIcon);
            }
        });
        bottomPanel.add(oneForwardButton);

        timeBar.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (source.hasFocus()) {
                    timer.setFrame(timeBar.getValue());
                    playButton.setIcon(playIcon);
                }
            }
        });
        bottomPanel.add(timeBar);

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
                        if (window.isTimerInitialized())
                            timer.setFrame(((Number) frameCounter.getValue())
                                    .intValue());
                        playButton.setIcon(playIcon);
                    }
                }
            }
        });

        bottomPanel.add(frameCounter);

        return bottomPanel;
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
