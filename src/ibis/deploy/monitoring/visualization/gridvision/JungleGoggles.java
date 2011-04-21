package ibis.deploy.monitoring.visualization.gridvision;

import java.awt.*;
import java.awt.event.*;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Element;
import ibis.deploy.monitoring.collection.Link;
import ibis.deploy.monitoring.collection.MetricDescription;
import ibis.deploy.monitoring.visualization.gridvision.JGVisual.MetricShape;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.AllInUseException;
import ibis.deploy.monitoring.visualization.gridvision.exceptions.MetricDescriptionNotAvailableException;
import ibis.deploy.monitoring.visualization.gridvision.swing.ContextSensitiveMenu;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

public class JungleGoggles implements GLEventListener {
	private static final long serialVersionUID = 1928258465842884618L;
	
	private static final float STANDARD_VIEWDIST = -10f;
	private static final int MAX_PARTICLES = 2000;

	GL2 gl;
	GLUgl2 glu = new GLUgl2();
	GLUT glut = new GLUT();
	GogglePanel panel;

	// Perspective variables
	private double fovy, aspect, width, height, zNear, zFar;

	// View variables
	private float viewDist;
	private Float[] viewTranslation, viewRotation;

	// picking
	private boolean pickRequest, updateRequest, recenterRequest, resetRequest, menuRequest, repositionRequest;
	private Point pickPoint;
	private int selectedItem, menuCoordX, menuCoordY;
	private HashMap<Integer, JGVisual> namesToVisuals;
	private HashMap<Integer, JGVisual> namesToParents;

	// Universe
	DisplayListBuilder listBuilder;
	private JGUniverse universe;
	private HashMap<Element, JGVisual> visualRegistry;
	private HashMap<Element, JGVisual> linkRegistry;

	// Viewer Location
	Mover m;

	// FPS counter
	private int framesPassed, fps;

	// Data interface
	Collector collector;
	
	//Particle stuff
	private Particle[] particles;
	private boolean[] particleInUse;
	private ParticleTimer ptimer;
	
	//Persistent Form storage
	MetricShape currentNWForm = MetricShape.PARTICLES;
	MetricShape currentMetricForm = MetricShape.BAR;

	/*
	 * --------------------------------------------------------------------------
	 * ---------------------- Initialization section
	 */

	/**
	 * Constructor for Junglegoggles, this sets up the window (Frame), creates a
	 * GLCanvas and starts the Animator
	 * @wbp.parser.entryPoint
	 */
	public JungleGoggles(Collector collector, GogglePanel panel) {
		this.panel = panel;
		
		// Initial perspective
		fovy = 45.0f;
		aspect = (this.width / this.height);
		zNear = 0.1f;
		zFar = 1500.0f;

		// Initial view
		viewDist = STANDARD_VIEWDIST;
		viewRotation = new Float[3];
		viewTranslation = new Float[3];
		for (int i = 0; i < 3; i++) {
			viewRotation[i] = 0.0f;
			viewTranslation[i] = 0.0f;
		}

		// Additional initializations
		pickRequest = false;
		updateRequest = true;
		recenterRequest = false;
		menuRequest = false;
		repositionRequest = false;
		pickPoint = new Point();
		selectedItem = -1;
		new javax.swing.Timer(1000, fpsRecorder).start();
		this.m = new Mover();

		// Data collector
		this.collector = collector;

		// Universe initializers
		visualRegistry = new HashMap<Element, JGVisual>();
		linkRegistry = new HashMap<Element, JGVisual>();
		namesToVisuals = new HashMap<Integer, JGVisual>();
		namesToParents = new HashMap<Integer, JGVisual>();
		
		//Particle stuff
		particles = new Particle[MAX_PARTICLES];
		particleInUse = new boolean[MAX_PARTICLES];
		for (int i = 0; i < MAX_PARTICLES; i++) {
			particles[i] = new Particle(i);
			particleInUse[i] = false; 
		}
		
		ptimer = new ParticleTimer(this);
		new Thread(ptimer).start();
		
		// Visual updater definition
		UpdateTimer updater = new UpdateTimer(this);
		new Thread(updater).start();
	}

	/**
	 * Init() will be called when Junglegoggles starts
	 */
	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();

		// Shader Model
		gl.glShadeModel(GL2.GL_SMOOTH);

		// Anti-Aliasing
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glEnable(GL2.GL_POLYGON_SMOOTH);
		gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);

		// Depth testing
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glClearDepth(1.0f);

		// Culling
		gl.glEnable(GL2.GL_CULL_FACE);

		// Enable Blending (needed for both Transparency and Anti-Aliasing
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_BLEND);

		// Lighting test
		// gl.glEnable(GL2.GL_LIGHT0);
		// gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);

		// General hint for optimum color quality
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		// Enable Vertical Sync
		gl.setSwapInterval(1);

		// Set black as background color
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Initialize display lists
		listBuilder = new DisplayListBuilder(gl, glu);

		// Universe initializers
		initializeUniverse();

		// and set the matrix mode to the modelview matrix in the end
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	/**
	 * Function that is called when the canvas is resized.
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		GL2 gl = drawable.getGL().getGL2();

		// Set the new viewport
		gl.glViewport(0, 0, w, h);

		// Change to the projection mode
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		// Calculate and set the new perspective
		this.width = (double) w;
		this.height = (double) h;

		aspect = this.width / this.height;

		glu.gluPerspective(fovy, aspect, zNear, zFar);

		// Return to normal mode
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	/**
	 * Mandatory functions to complete the implementation of GLEventListener,
	 * but unneeded and therefore left blank.
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void dispose(GLAutoDrawable arg0) {
	}

	/**
	 * Functions that register visual elements and GLNames during
	 * initialization, to enable picking later.
	 */
	public void registerVisual(Element element, JGVisual newVisual) {
		visualRegistry.put(element, newVisual);
	}

	public int registerGLName(JGVisual parent, JGVisual metric) {
		int key = namesToVisuals.size();
		namesToVisuals.put(key, metric);
		namesToParents.put(key, parent);
		return key;
	}

	/**
	 * Function used to speed up the display process by using pre-built display
	 * lists.
	 */
	public int[] getDisplayListPointer(
			DisplayListBuilder.DisplayList whichPointer) {
		return listBuilder.getPointer(whichPointer);
	}
	
	/**
	 * Functions used to handle the particle storage system.
	 */	
	public Particle getParticle() throws AllInUseException {
		synchronized(particles) {
			Particle result = null;
			for (int i = 0; i < MAX_PARTICLES; i++) {
				if (!particleInUse[i]) {
					result = particles[i];
					particleInUse[i] = true;
					return result;
				}
			}			
		}		
		throw new AllInUseException();
	}
	
	public void returnParticle(Particle p) {
		synchronized(particles) {
			int number = p.getNumber();
			particleInUse[number] = false;
		}		
	}
	
	public void doParticleMoves(float fraction) {
		for (int i = 0; i < MAX_PARTICLES; i++) {
			particles[i].doMoveFraction(fraction);
		}
	}	
	

	/**
	 * This function sets the current state to the original state.
	 */
	public void initializeUniverse() {
		//Clear the slate
		visualRegistry.clear();
		namesToVisuals.clear();
		namesToParents.clear();

		//Fill the visualRegistry again with new objects
		universe = new JGUniverse(this, null, glu, collector.getRoot());
		//universe.setCoordinates(m.getCurrentCoordinates());
		universe.init(gl);

		//And create links between them
		linkRegistry.clear();
		for (Entry<Element, JGVisual> entry : visualRegistry.entrySet()) {
			Element data = entry.getKey();

			Link[] links = data.getLinks();
			for (Link link : links) {
				JGVisual v_source = visualRegistry.get(link.getSource());
				JGVisual v_dest = visualRegistry.get(link.getDestination());

				JGLink jglink = new JGLink(this, v_source, glu, v_source, v_dest, link);
				jglink.init(gl);
				//jglink.setCoordinates(m.getCurrentCoordinates());

				linkRegistry.put(link, jglink);
			}
		}
		//System.out.println("Goggles created "+linkRegistry.size()+" links.");
		
		//Re-Init the particles
		for (int i = 0; i < MAX_PARTICLES; i++) {		
			particleInUse[i] = false; 
		}
		
		//Re-Apply the current Shapes
		setMetricForm(currentMetricForm);
		setNetworkForm(currentNWForm);
		
		rePositionUniverse();
	}
	
	private void rePositionUniverse() {
		universe.setCoordinates(m.getCurrentCoordinates());
		for (JGVisual jglink : linkRegistry.values()) {
			jglink.setCoordinates(m.getCurrentCoordinates());
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

	/*
	 * --------------------------------------------------------------------------
	 * ---------------------- Drawing section
	 */

	/**
	 * display() will be called repeatedly by the Animator when Animator is done
	 * it will swap buffers and update the display.
	 */
	public void display(GLAutoDrawable drawable) {
		//Determine, if any, the selected item and it's parent
		JGVisual selectedVisual = namesToParents.get(selectedItem);
		JGVisual selectedParent = namesToParents.get(selectedItem);	
		
		/** Draw all desireables
		 */
		
		// Get the current opengl instance, and clear the depth and color buffers		
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		// Draw the current state of the universe
		drawUniverse(gl, GL2.GL_RENDER);
		
		//Draw a selection indicator around the currently selected item			
		if (selectedParent != null) {
			selectedParent.drawSelectionCube(gl);
		}

		// Draw the Heads Up Display
		drawHud(gl);
		
		if (menuRequest) {
			if (selectedParent != null) {
				ContextSensitiveMenu popup = new ContextSensitiveMenu(namesToParents.get(selectedItem));
				GLJPanel canvas = panel.getPanel();
				canvas.add(popup);
				popup.show(canvas, menuCoordX, menuCoordY);
			}
			menuRequest = false;
		}
		
		// Start the rendering process so that it runs in parallel with the
		// computations we need to do for the NEXT frame
		gl.glFinish();

		
		/** While we are rendering, update visuals for the next display cycle:
		 */
		// First, reset the universe if requested to do so
		if (resetRequest || collector.change()) {
			initializeUniverse();
			resetRequest = false;
		} 
		
		if (repositionRequest) {
			rePositionUniverse();
			repositionRequest = false;
		}

		// Then handle input, first determine where the user has clicked
		if (pickRequest) {
			selectedItem = pick(gl, pickPoint);			
			pickRequest = false;
		}			

		// And recenter (move) to that location
		if (recenterRequest) {
			if (selectedVisual != null) {
				float[] newCenter = selectedVisual.getCoordinates();
				m.moveTo(newCenter);
			}

			recenterRequest = false;
		}

		// Then, change the location according to input given.
		if (m.locationChanged()) {
			universe.setCoordinates(m.getCurrentCoordinates());
			for (JGVisual link : linkRegistry.values()) {
				link.setCoordinates(m.getCurrentCoordinates());
			}
		}

		// Lastly, update the current values for all of the visual elements if
		// it is time to do so.
		if (updateRequest) {
			universe.update();
			for (JGVisual link : linkRegistry.values()) {
				link.update();
			}
			updateRequest = false;
		}
	}

	private void drawUniverse(GL2 gl, int renderMode) {
		// Reset the modelview matrix
		gl.glLoadIdentity();

		// Change the view according to mouse input
		gl.glTranslatef(viewTranslation[0], viewTranslation[1], viewDist);
		gl.glRotatef(viewRotation[0], 1, 0, 0);
		gl.glRotatef(viewRotation[1], 0, 1, 0);

		// Draw the solid elements (location tree and links)
		for (JGVisual link : linkRegistry.values()) {
			link.drawSolids(gl, renderMode);
		}
		
		universe.drawSolids(gl, renderMode);

		//Then, draw the transparent elements of both.		
		for (JGVisual link : linkRegistry.values()) {
			link.drawTransparents(gl, renderMode);
		}
		
		universe.drawTransparents(gl, renderMode);
	}

	private void drawHud(GL2 gl) {
		// Increase the counter
		framesPassed++;

		// Move to the hud location
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -1.0f);

		gl.glColor3f(1.0f, 1.0f, 1.0f);

		gl.glRasterPos2f(0.45f, -0.35f);

		// Draw the hud
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "FPS: " + fps);
	}

	/**
	 * Function called by the updater thread to enable updating on the next
	 * cycle.
	 */
	public void doUpdateRequest() {
		updateRequest = true;
	}

	/*
	 * --------------------------------------------------------------------------
	 * ---------------------- Interaction section
	 */

	/**
	 * Functions for I/O (GUI)
	 */
	public void setRotation(Float[] newViewRotation) {
		viewRotation = newViewRotation;
	}

	public void setViewDist(float newViewDist) {
		viewDist = newViewDist;
	}
	
	public float getViewDist() {
		return viewDist;
	}

	public void doPickRequest(Point p) {
		pickRequest = true;
		pickPoint = p;
	}
	
	public boolean currentlySelected(int glName) {
		JGVisual selectedParent = namesToParents.get(selectedItem);
		if (selectedParent == null) {
			return false;
		}
		JGVisual myParent = namesToParents.get(glName);
		
		if (selectedParent == myParent) {
			return true;
		} else {		
			return false;
		}
	}
	
	public boolean currentlySelected(JGVisual me) {
		JGVisual selectedParent = namesToParents.get(selectedItem);
		if (selectedParent == null) {
			return false;
		}
		
		if (selectedParent == me) {
			return true;
		} else {		
			return false;
		}
	}
	
	public void unselect() {
		selectedItem = -1;
	}

	public void doMenuRequest(int x, int y) {
		menuCoordX = x;
		menuCoordY = y;
		menuRequest = true;
	}
	
	public void doRecenterRequest() {
		recenterRequest = true;
	}

	public void doReset() {
		resetRequest = true;
	}
	
	public void doRepositioning() {
		repositionRequest = true;
	}
	
	public void setNetworkForm(MetricShape newShape) {
		currentNWForm = newShape;
		for (JGVisual link : linkRegistry.values()) {
			link.setMetricShape(newShape);
		}
	}
	
	public void setMetricForm(MetricShape newShape) {
		currentMetricForm = newShape;
		universe.setMetricShape(newShape);
	}
	
	public MetricDescription getMetricDescription(String name) throws MetricDescriptionNotAvailableException {
		HashSet<MetricDescription> bla = collector.getAvailableMetrics();
		
		for (MetricDescription md : bla) {
			if (md.getName().compareTo(name) == 0) {
				return md;
			}
		}
		
		throw new MetricDescriptionNotAvailableException();
	}
	
	public int getRefreshrate() {
		return collector.getRefreshrate();
	}
	
	public void setRefreshrate(int newRate) {
		collector.setRefreshrate(newRate);
	}

	/**
	 * Functions to enable picking
	 */
	private int pick(GL2 gl, Point pickPoint) {
		final int BUFSIZE = 512;

		int[] selectBuf = new int[BUFSIZE];
		IntBuffer selectBuffer = Buffers.newDirectIntBuffer(BUFSIZE);
		int hits;

		// Save the current viewport
		int viewport[] = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

		// Switch to selection mode
		gl.glSelectBuffer(BUFSIZE, selectBuffer);
		gl.glRenderMode(GL2.GL_SELECT);

		gl.glInitNames();
		gl.glPushName(-1);

		// Switch to Projection mode and save the current projection matrix
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		// create 5x5 pixel picking region near cursor location
		glu.gluPickMatrix((double) pickPoint.x,
				(double) (viewport[3] - pickPoint.y), 3.0, 3.0, viewport, 0);

		// Multiply by the perspective
		glu.gluPerspective(fovy, aspect, zNear, zFar);

		// Draw the models in selection mode
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		drawUniverse(gl, GL2.GL_SELECT);

		// Restore the original projection matrix
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();

		// Switch back to modelview and make sure there are no stragglers
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glFlush();

		// Process the hits
		hits = gl.glRenderMode(GL2.GL_RENDER);
		selectBuffer.get(selectBuf);
		int selection = processHits(hits, selectBuf);

		return selection;
	}

	/**
	 * Internal function to handle picking requests, and determine the object
	 * that the user wanted to select ( assumed to be the closest object to the
	 * user at the picking location )
	 */
	private int processHits(int hits, int buffer[]) {
		int selection = -1;
		int depth;

		if (hits > 0) {
			selection = buffer[3];
			depth = buffer[1];

			for (int i = 0; i < hits; i++) {
				if (buffer[i * 4 + 1] < depth) {
					selection = buffer[i * 4 + 3];
					depth = buffer[i * 4 + 1];
				}
			}
		}

		return selection;
	}

}