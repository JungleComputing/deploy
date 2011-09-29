package ibis.deploy.gui.outputViz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.hfd5reader.HDFTimer;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import com.jogamp.opengl.util.FPSAnimator;

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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OutputVizPanel extends JPanel {
	private static final long serialVersionUID = 4754345291079348455L;
	
	public static final int DEFAULT_SCREEN_WIDTH = 1024;

    public static final int DEFAULT_SCREEN_HEIGHT = 768;


	private GLWindow window;
	private GLCanvas glcanvas;
	
	public HDFTimer timer;
	public JSlider timeBar;
	public JFormattedTextField frameCounter;

	public OutputVizPanel(final GUI gui) {		
	    final JButton initButton = new JButton("Initialize 3D Visualization");
	    add(initButton);
	    
	    initButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    removeAll();
			}
	    });
	}
	
	public OutputVizPanel() {		
		initialize(null);
	}

	public void initialize(GUI gui) {
		setLayout(new BorderLayout(0, 0));
		
		// Make the GLEventListener
		window = new GLWindow();

		// Standard GL3 capabilities
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities glCapabilities = new GLCapabilities(glp);

		// glCapabilities.setDoubleBuffered(true);
		glCapabilities.setHardwareAccelerated(true);

		// Anti-Aliasing
		glCapabilities.setSampleBuffers(true);
		glCapabilities.setNumSamples(4);

		glcanvas = new GLCanvas(glCapabilities);
		glcanvas.setPreferredSize(new Dimension(800, 600));
		glcanvas.addGLEventListener(window);

		// Add Mouse event listener
		MouseHandler mouseHandler = new MouseHandler(window);
		glcanvas.addMouseListener(mouseHandler);
		glcanvas.addMouseMotionListener(mouseHandler);
		glcanvas.addMouseWheelListener(mouseHandler);

		// Add key event listener
		KeyHandler keyHandler = new KeyHandler(window);
		glcanvas.addKeyListener(keyHandler);

		// Set up animator
		final FPSAnimator animator = new FPSAnimator(glcanvas, 60);
		animator.start();	
		
		timeBar = new JSlider();
		timeBar.setValue(0);
	    timeBar.setMajorTickSpacing(5);
	    timeBar.setMinorTickSpacing(1);
	    timeBar.setMaximum(0);
	    timeBar.setMinimum(0);
	    timeBar.setPaintTicks(true);
	    timeBar.setSnapToTicks(true);
	    
		timer = new HDFTimer(timeBar, frameCounter);		
		
		//Make the menu bar
		JMenuBar menuBar = new JMenuBar();
			JMenu file = new JMenu("File");
				JMenuItem open = new JMenuItem("Open");
				open.addActionListener(new ActionListener() {					
					public void actionPerformed(ActionEvent arg0) {						
						File file = openFile();
						if (file != null) { 
							String path = file.getPath().substring(0, file.getPath().length()-file.getName().length());
							String name = file.getName();
							String fullPath = path + name;
							String[] ext = fullPath.split("[.]");
							if (!(ext[1].compareTo("evo") == 0 ||
								  ext[1].compareTo("grav") == 0 || 
								  ext[1].compareTo("add") == 0 || 
								  ext[1].compareTo("gas") == 0 || 
								  ext[1].compareTo("data") == 0)) {
								JOptionPane pane = new JOptionPane();
								pane.setMessage("Tried to open invalid file type.");
								JDialog dialog = pane.createDialog("Alert");
							    dialog.setVisible(true);							
							} else {
								String prefix = ext[0].substring(0, ext[0].length()-6);
								window.stopAnimation();
								timer = new HDFTimer(timeBar, frameCounter);
								timer.open(path, prefix);
								window.startAnimation(timer);
							}
						}
					}					
				});
				file.add(open);				
			menuBar.add(file);
			JMenu options = new JMenu("Options");
//				JMenuItem gas = new JMenuItem("Gas Toggle");
//				gas.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent arg0) {
//						GLWindow.setGas(!GLWindow.GAS_ON);
//					}					
//				});
//				options.add(gas);
//				
//				JMenuItem colormap = new JMenuItem("Gas Colormap Toggle");
//				colormap.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent arg0) {
//						GLWindow.setColormap(!GLWindow.GAS_COLORMAP);
//					}					
//				});
//				options.add(colormap);
//				
				JMenuItem postprocess = new JMenuItem("Post-Process (blur) Toggle");
				postprocess.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						GLWindow.setPostprocess(!GLWindow.POST_PROCESS);
					}					
				});
				options.add(postprocess);
				
				JMenu resolution = new JMenu("Gas cloud resolution.");
					JMenuItem five = new JMenuItem("5 elements per node.");
					five.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							GLWindow.setResolution(5);
						}					
					});
					resolution.add(five);
					JMenuItem ten = new JMenuItem("10 elements per node.");
					ten.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							GLWindow.setResolution(10);
						}					
					});
					resolution.add(ten);
					JMenuItem twentyfive = new JMenuItem("25 elements per node.");
					twentyfive.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							GLWindow.setResolution(25);
						}					
					});
					resolution.add(twentyfive);
					JMenuItem hundred = new JMenuItem("100 elements per node.");
					hundred.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							GLWindow.setResolution(100);
						}					
					});
					resolution.add(hundred);
					JMenuItem twohundred = new JMenuItem("200 elements per node.");
					twohundred.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							GLWindow.setResolution(200);
						}					
					});
					resolution.add(twohundred);
				options.add(resolution);
				
//				JMenuItem axes = new JMenuItem("Axes Toggle");
//				axes.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent arg0) {
//						GLWindow.setAxes(!GLWindow.AXES);
//					}					
//				});
//				options.add(axes);
//				
//				JMenuItem prediction = new JMenuItem("Prediction Toggle");
//				prediction.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent arg0) {
//						GLWindow.setPrediction(!GLWindow.PREDICTION_ON);
//					}					
//				});
//				options.add(prediction);
			menuBar.add(options);
			
		//Make the "media player" panel
		JPanel bottomPanel = new JPanel();	
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
				JButton button0 = new JButton("Rewind");
				button0.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						timer.rewind();
					}
				});
				bottomPanel.add(button0);
				
				JButton button1 = new JButton("Pause");
				button1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						timer.stop();
					}
				});
				bottomPanel.add(button1);
				
				JButton button2 = new JButton("Snapshot!");
				button2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						timer.stop();
						timer.makeSnapshot();
					}
				});
				bottomPanel.add(button2);
				
				JButton button3 = new JButton("Play");
				button3.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						timer.start();
					}
				});
				bottomPanel.add(button3);
				
				frameCounter = new JFormattedTextField();
				frameCounter.setValue(new Integer(1));
				frameCounter.setColumns(4);
				frameCounter.setMaximumSize(new Dimension(40,20));
				frameCounter.setValue(0);
				frameCounter.addPropertyChangeListener(new PropertyChangeListener() {					
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField source = (JFormattedTextField) e.getSource();
						if (source.hasFocus()) {	
							if (source == frameCounter) {
								if (window.timerInitialized) timer.setFrame(((Number)frameCounter.getValue()).intValue());
							}
						}
					}
				});
				
				bottomPanel.add(frameCounter);				
				
				timeBar.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JSlider source = (JSlider)e.getSource();
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
		glcanvas.requestFocus();
	}
	
	public static void main(String[] args) {
		final JFrame frame = new JFrame("Ibis Deploy - OutputViz Testframe");
		frame.setPreferredSize(new Dimension(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT));
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try {
                final OutputVizPanel bla = new OutputVizPanel();
                frame.getContentPane().add(bla);
                
                frame.addWindowListener(new WindowAdapter() {
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

		fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		int result = fileChooser.showOpenDialog( this );

		// user clicked Cancel button on dialog
		if ( result == JFileChooser.CANCEL_OPTION )
			return null;
		else
			return fileChooser.getSelectedFile();
	}
}
