package ibis.deploy.monitoring.visualization.gridvision;

import java.awt.*;
import java.awt.event.*;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.gl2.GLUgl2;
import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Link;

public class JungleGoggles implements GLEventListener {	
	private final boolean STEREO = false;
	
	GL2 gl;
	GLUgl2 glu = new GLUgl2();
    GLUT glut = new GLUT();
    GLJPanel gljpanel;
    MouseHandler mouseHandler;
    KeyHandler keyHandler;
    
    //Perspective variables 
    private double fovy, aspect, width, height, zNear, zFar;
    
    //View variables
    private float viewDist; 
    private Float[] viewTranslation, viewRotation;
    
    //picking
    private boolean pickRequest, updateRequest, recenterRequest, resetRequest;
    private Point pickPoint;
    private int selectedItem;
    private HashMap<Integer, JGVisual> namesToVisuals;
    
    //Universe
    DisplayListBuilder listBuilder;
    private JGUniverse universe;
    private HashMap<Element, JGVisual> visualRegistry;
    private HashMap<Element, JGVisual> linkRegistry;
        
    //Viewer Location
    Mover m;
    
    //FPS counter
    private int framesPassed, fps;
    
    //Data interface
    Collector collector;    

	/* ------------------------------------------------------------------------------------------------ 
	 *  Initialization section 
	 * */    
    
    /**
     * Constructor for Junglegoggles, this sets up the
     * window (Frame), creates a GLCanvas and starts the Animator
     */
    public JungleGoggles(Collector collector) {
    	//Standard GL2 capabilities
    	GLProfile glp = GLProfile.get(GLProfile.GL2); 
		GLCapabilities glCapabilities = new GLCapabilities(glp);
		
		//glCapabilities.setDoubleBuffered(true);
		glCapabilities.setHardwareAccelerated(true);
		
		//Anti-Aliasing
		glCapabilities.setSampleBuffers(true);
		glCapabilities.setNumSamples(4);
		
		//3d ?
		if (STEREO) {
			glCapabilities.setStereo(true);
		}
    	
    	gljpanel = new GLJPanel(glCapabilities);    	
		gljpanel.addGLEventListener(this);
		
		//Add Mouse event listener
		mouseHandler = new MouseHandler(this);
		gljpanel.addMouseListener(mouseHandler);
		gljpanel.addMouseMotionListener(mouseHandler);
		gljpanel.addMouseWheelListener(mouseHandler);
		
		//Add key event listener
		keyHandler = new KeyHandler(this);
		gljpanel.addKeyListener(keyHandler);
		
		//Set up animator
		final FPSAnimator animator = new FPSAnimator(gljpanel,60);
		
		//Set up the window
		final JFrame jframe = new JFrame("JungleGoggles");
		jframe.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				animator.stop();
				jframe.dispose();
				System.exit(0);
			}
		});
		jframe.getContentPane().add( gljpanel, BorderLayout.CENTER );
		jframe.setSize(1800, 1100);
		jframe.setVisible(true);
		
		//Initial perspective
		fovy = 45.0f; 
		aspect = (this.width / this.height); 
		zNear = 0.1f;
		zFar = 1500.0f;	
		
		//Initial view
		viewDist = -6;
		viewRotation = new Float[3];
		viewTranslation = new Float[3];
		for (int i=0; i<3; i++) {
			viewRotation[i] = 0.0f;
			viewTranslation[i] = 0.0f;
		}
				
		//Additional initializations
		pickRequest = false;
		updateRequest = true;
		recenterRequest = false;
		pickPoint = new Point();		
		new javax.swing.Timer(1000, fpsRecorder).start();				
		this.m = new Mover();
		
		//Data collector
		this.collector = collector;		
		
		//Universe initializers
		visualRegistry = new HashMap<Element, JGVisual>();
		linkRegistry = new HashMap<Element, JGVisual>();
		namesToVisuals = new HashMap<Integer, JGVisual>();
		
		//Visual updater definition
		UpdateTimer updater = new UpdateTimer(this);
		new Thread(updater).start();		
		
		//Start drawing
		animator.start();
		gljpanel.requestFocusInWindow(); 
    }
    
    /**
     * Init() will be called when Junglegoggles starts
     */
	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();
		
		//Shader Model
		gl.glShadeModel(GL2.GL_SMOOTH);
		
		//Anti-Aliasing
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glEnable(GL2.GL_POLYGON_SMOOTH); 
		gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
	    	    
	    //Depth testing
	    gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glClearDepth(1.0f);
		
		//Culling
		gl.glEnable(GL2.GL_CULL_FACE);
		
		//Enable Blending (needed for both Transparency and Anti-Aliasing
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);				
		gl.glEnable(GL2.GL_BLEND);
		
		//Lighting test
		//gl.glEnable(GL2.GL_LIGHT0);
	    //gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
				
		//General hint for optimum color quality
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		//Enable Vertical Sync
		gl.setSwapInterval(1);
		
		//Set black as background color
	    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	    
	    //3d ?
	    
	    
	    //Initialize display lists
	    listBuilder = new DisplayListBuilder(gl);
				
		//Universe initializers
	    initializeUniverse();	    
	    
	    //and set the matrix mode to the modelview matrix in the end
	    gl.glMatrixMode(GL2.GL_MODELVIEW);
	    gl.glLoadIdentity();
	}
	
	/**
	 * Function that is called when the canvas is resized. 
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		GL2 gl = drawable.getGL().getGL2();
		
		//Set the new viewport
		gl.glViewport(0, 0, w, h);

		//Change to the projection mode
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		//Calculate and set the new perspective
		this.width = (double) w;
		this.height = (double) h;
		
		aspect = this.width / this.height;
		
		glu.gluPerspective(fovy, aspect, zNear, zFar);
		
		//Return to normal mode
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}	
	
	
	/**
	 * Mandatory functions to complete the implementation of GLEventListener, but unneeded and therefore left blank.
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}	
	public void dispose(GLAutoDrawable arg0) {}
	
	/**
	 * Functions that register visual elements and GLNames during initialization, to enable picking later.
	 */
	public void registerVisual(Element element, JGVisual newVisual) {
		visualRegistry.put(element, newVisual);
	}
	
	public int registerGLName(JGVisual metric) {
		int key = namesToVisuals.size();
		namesToVisuals.put(key, metric);
		return key;		
	}
	
	/**
	 * Function used to speed up the display process by using pre-built display lists.
	 */
	public int[] getDisplayListPointer(DisplayListBuilder.DisplayList whichPointer) {
		return listBuilder.getPointer(whichPointer);
	}
		
	/**
	 * This function sets the current state to the original state.
	 */
	public void initializeUniverse() {
		visualRegistry.clear();
		
		universe = new JGUniverse(this, glu, collector.getRoot());
		universe.setCoordinates(m.getCurrentCoordinates());
		universe.init(gl);
		
		createLinks();	
	}
	
	/**
	 * Function to intinialize links
	 */
	private void createLinks() {
		linkRegistry.clear();
		
		for (Entry<Element, JGVisual> entry : visualRegistry.entrySet()) {
			Element data = entry.getKey();
			
			Link[] links = data.getLinks();
			for (Link link : links) {
				JGVisual v_source = visualRegistry.get(link.getSource());
				JGVisual v_dest = visualRegistry.get(link.getDestination());
				
				JGLink jglink = new JGLink(this, glu, v_source, v_dest, link);				
				jglink.init(gl);
				jglink.setCoordinates(m.getCurrentCoordinates());
				
				linkRegistry.put(link, jglink);
			}			
		}		
	}	
	
	/**
	 * Action listener for the fps counter 
	 */
	ActionListener fpsRecorder = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			fps = framesPassed;
			framesPassed = 0;
		}
	};
	
	
	/* ------------------------------------------------------------------------------------------------ 
	 *  Drawing section
	 * */	
	
	/**
	 * display() will be called repeatedly by the Animator
	 * when Animator is done it will swap buffers and update
	 * the display.
	 */
	public void display(GLAutoDrawable drawable) {
	    GL2 gl = drawable.getGL().getGL2();

	    //Added GL2.GL_DEPTH_BUFFER_BIT
	    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	    		
		//Draw the current state of the universe
		drawUniverse(gl, GL2.GL_RENDER);
		
		//Draw the Heads Up Display
		drawHud(gl);
		
		//Start the rendering process so that it runs in parallel with the 
		//computations we need to do for the NEXT frame
		gl.glFinish();
		
		
		//While we are rendering, update visuals for the next display cycle:
		
		//First, reset the universe if requested to do so
		if (resetRequest || collector.change()) {
			initializeUniverse();
			resetRequest = false;
		}
		
		//Then handle input, first determine where the user has clicked
		if (pickRequest) {
			selectedItem = pick(gl, pickPoint);
			pickRequest = false;
		}
		
		//And recenter (move) to that location
		if (recenterRequest) {
			if (namesToVisuals.get(selectedItem) != null) {
				Float[] newCenter = namesToVisuals.get(selectedItem).getCoordinates();
				m.moveTo(newCenter);
			}
			
			recenterRequest = false;
		}
		
		//Then, change the rotation according to input given.
		if (m.locationChanged()) {
			universe.setCoordinates(m.getCurrentCoordinates());
			for (JGVisual link : linkRegistry.values()) {
				link.setCoordinates(m.getCurrentCoordinates());
			}
		}
		
		//Lastly, update the current values for all of the visual elements if it is time to do so.
		if (updateRequest) {
			universe.update();	
			for (JGVisual link : linkRegistry.values()) {
				link.update();
			}
			updateRequest = false;
		}
	}
	
	private void drawUniverse(GL2 gl, int renderMode) {
		//Reset the modelview matrix
	    gl.glLoadIdentity();
	    
	    //Change the view according to mouse input
		gl.glTranslatef(viewTranslation[0], viewTranslation[1], viewDist);
		gl.glRotatef(viewRotation[0], 1,0,0);
		gl.glRotatef(viewRotation[1], 0,1,0);
		
		//Draw the universe (locations tree)
		universe.drawThis(gl, renderMode);
		
		//Draw all the links
		for (JGVisual link : linkRegistry.values()) {
			link.drawThis(gl, renderMode);
		}
	}
	
	private void drawHud(GL2 gl) {
		//Increase the counter
		framesPassed++;
		
		//Move to the hud location
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -1.0f);
		
		gl.glColor3f(1.0f,1.0f,1.0f);
		
		gl.glRasterPos2f(0.45f, -0.35f);
		
		//Draw the hud
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "FPS: " + fps);				
	}
	
	/**
	 * Function called by the updater thread to enable updating on the next cycle.
	 */
	public void doUpdateRequest() {
		updateRequest = true;
	}
	
	/* ------------------------------------------------------------------------------------------------ 
	 *  Interaction section
	 * */	
	
	/**
	 * Functions called by the MouseHandler class
	 */
	public void setRotation(Float[] newViewRotation) {
		viewRotation = newViewRotation;
	}
	
	public void setViewDist(float newViewDist) {
		viewDist = newViewDist;
	}
	
	public void doPickRequest(Point p) {
		pickRequest = true;
		pickPoint = p;
	}
	
	public PopupMenu menuRequest() {
		return new PopupMenu();
	}
	
	public GLJPanel getPanel() {
		return gljpanel;
	}
		
	/**
	 * Functions called by the KeyHandler class
	 */
	public void doRecenterRequest() {
		recenterRequest = true;
	}
	
	public void doReset() {
		resetRequest = true;
	}
		
	/**
	 * Functions to enable picking
	 */
	private int pick(GL2 gl, Point pickPoint) {
		final int BUFSIZE = 512;
		
		int[] selectBuf = new int[BUFSIZE];
	    IntBuffer selectBuffer = Buffers.newDirectIntBuffer(BUFSIZE);
	    int hits;
	    
	    //Save the current viewport
	    int viewport[] = new int[4];	    
	    gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

	    //Switch to selection mode
	    gl.glSelectBuffer(BUFSIZE, selectBuffer);
	    gl.glRenderMode(GL2.GL_SELECT);

	    gl.glInitNames();
	    gl.glPushName(-1);

	    //Switch to Projection mode and save the current projection matrix
	    gl.glMatrixMode(GL2.GL_PROJECTION);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    
	    // create 5x5 pixel picking region near cursor location
	    glu.gluPickMatrix((double) pickPoint.x,
	        (double) (viewport[3] - pickPoint.y), 
	        3.0, 3.0, viewport, 0);
	    	    
	    //Multiply by the perspective
	    glu.gluPerspective(fovy, aspect, zNear, zFar);	    
	    
	    //Draw the models in selection mode
	    gl.glMatrixMode(GL2.GL_MODELVIEW);	    
	    drawUniverse(gl, GL2.GL_SELECT);
	    
	    //Restore the original projection matrix
	    gl.glMatrixMode(GL2.GL_PROJECTION);
	    gl.glPopMatrix();
	    
	    //Switch back to modelview and make sure there are no stragglers
	    gl.glMatrixMode(GL2.GL_MODELVIEW);
	    gl.glFlush();
	    
	    //Process the hits
	    hits = gl.glRenderMode(GL2.GL_RENDER);
	    selectBuffer.get(selectBuf);
	    int selection = processHits(hits, selectBuf);	    
	    
	    return selection;
	}
		
	/**
	 * Internal function to handle picking requests, and determine the object that the 
	 * user wanted to select ( assumed to be the closest object to the user at the 
	 * picking location )
	 */
	private int processHits(int hits, int buffer[]) {	
		int selection = -1;
		int depth;
		
		if (hits > 0) {
			selection = buffer[3];
			depth = buffer[1];
			
			for (int i=0; i<hits;i++) {
				if (buffer[i*4+1] < depth) {
					selection = buffer[i*4+3];
					depth = buffer[i*4+1];
				}
			}
		}
	    
	    return selection;
	}

		
}