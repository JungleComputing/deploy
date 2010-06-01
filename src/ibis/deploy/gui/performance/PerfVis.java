package ibis.deploy.gui.performance;

import ibis.deploy.gui.GUI;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

import java.awt.Point;
import java.awt.PopupMenu;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

public class PerfVis implements GLEventListener {
	private static final int BUFSIZE = 512;
		
	private GLU glu;
	GUI gui;
	JPanel panel;
	
	//Window variables
	private double width;
	private double height;	
	GLCanvas canvas;
	
	//Perspective variables	
	private double fovy, aspect, zNear, zFar;
	
	//MouseHandler variables
	private float viewDist = -6;
	private Float[] rotation = {0.0f,0.0f,0.0f}, translation = {0.0f,0.0f,0.0f}, origin = {0.0f,0.0f,0.0f};
	private boolean doPickNextCycle = false;
	private boolean relocateOriginNextCycle = false;
	
	private Point pickPoint = new Point();
	private int currentSelection, refreshrate;
	private float currentValue;
	
	//JMX variables
	private RegistryServiceInterface regInterface;
	private ManagementServiceInterface manInterface;
		
	private StatsManager statman;
	private VisualManager visman;
	private MouseHandler mouseHandler;
	
	PerfVis() {
		glu = new GLU();
	}
	
	PerfVis (GUI gui, GLCanvas canvas, JPanel panel) {
		this.gui = gui;
		this.panel = panel;
		glu = new GLU();
		this.canvas = canvas;
				
		try {
			this.regInterface = gui.getDeploy().getServer().getRegistryService();
			this.manInterface = gui.getDeploy().getServer().getManagementService();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		mouseHandler = new MouseHandler(this, canvas);
		visman = new VisualManager(this);				
	}
				
	public void display(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		visman.update();
		
		if (doPickNextCycle) {
			pick(gl);
			doPickNextCycle = false;
		}
				
		drawHud(gl);		
		drawUniverse(gl, GL.GL_RENDER);
		gl.glFlush();
	}	
	
	public void setRotation(Float[] rotation) {
		this.rotation = rotation;
	}
	
	public void setOrigin(Float[] origin) {
		this.origin = origin;
	}
	
	public void setTranslation(Float[] translation) {
		this.translation = translation;
	}
	
	public void setViewDist(float newViewDist) {
		this.viewDist = newViewDist;
	}
	
	private void doView(GL gl) {	
		gl.glTranslatef(origin[0], origin[1], origin[2]);
		gl.glTranslatef(translation[0], translation[1], translation[2]);
		gl.glTranslatef(0,0,viewDist);
		gl.glRotatef(rotation[0], 1,0,0);		
		gl.glRotatef(rotation[1], 0,1,0);
		
	}

	private void drawUniverse(GL gl, int mode) {		
		gl.glLoadIdentity();
		
		doView(gl);
		visman.drawConcepts(gl, mode);
	}
	
	public int getSelection() {
		return currentSelection;
	}
	
	public void setValue(float value) {
		currentValue = value;
	}
	
	private void drawHud(GL gl) {
		GLUT glut = new GLUT();
		
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -1.0f);
		
		gl.glColor3f(0.0f,0.0f,1.0f);
		
		gl.glRasterPos2f(0.40f, -0.30f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Selected: " + currentSelection);
		gl.glRasterPos2f(0.40f, -0.35f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Value   : " + currentValue);		
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		
	}

	public void init(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glDepthRange(0.0, 1.0);
		
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		
		//Found at http://pyopengl.sourceforge.net/documentation/manual/glBlendFunc.3G.html
		//To be the best blend function
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_DST_ALPHA);		
		gl.glEnable(GL.GL_BLEND);
		
		//Initial perspective
		fovy = 45.0f; 
		aspect = (this.width / this.height); 
		zNear = 0.1f;
		zFar = 1000.0f;
		
		//Mouse events		
		canvas.addMouseListener(mouseHandler);
		canvas.addMouseMotionListener(mouseHandler);
		canvas.addMouseWheelListener(mouseHandler);
		
		canvas.requestFocusInWindow();
		
		statman = new StatsManager(visman, manInterface, regInterface);
		new Thread(statman).start();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL gl = drawable.getGL();
		final GLU glu = new GLU();		
		
		gl.setSwapInterval(1);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		//gl.glOrtho(0.0, 8.0, 0.0, 8.0, -0.5, 2.5);
		
		this.width = (double) width;
		this.height = (double) height;
		
		aspect = this.width / this.height;
		
		glu.gluPerspective(fovy, aspect, zNear, zFar);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	public void pickRequest(Point newPickPoint) {
		this.pickPoint = newPickPoint;
		this.doPickNextCycle = true;
	}
	
	public void relocateOrigin(Point newPickPoint) {
		this.pickPoint = newPickPoint;
		this.doPickNextCycle = true;
		this.relocateOriginNextCycle = true;
	}
		
	public PopupMenu menuRequest() {
		PopupMenu popup = visman.getContextSensitiveMenu(currentSelection);
		return popup;		
	}	
	
	private void pick(GL gl) {
		int[] selectBuf = new int[BUFSIZE];
	    IntBuffer selectBuffer = BufferUtil.newIntBuffer(BUFSIZE);
	    int hits;
	    int viewport[] = new int[4];
	    
	    gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

	    gl.glSelectBuffer(BUFSIZE, selectBuffer);
	    gl.glRenderMode(GL.GL_SELECT);

	    gl.glInitNames();
	    gl.glPushName(-1);

	    //Switch to Projection mode and save the current projection matrix
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    
	    // create 5x5 pixel picking region near cursor location
	    glu.gluPickMatrix((double) pickPoint.x,
	        (double) (viewport[3] - pickPoint.y), 
	        5.0, 5.0, viewport, 0);
	    	    
	    //Multiply by the perspective
	    glu.gluPerspective(fovy, aspect, zNear, zFar);	    
	    
	    //Draw the models in selection mode
	    gl.glMatrixMode(GL.GL_MODELVIEW);	    
	    drawUniverse(gl, GL.GL_SELECT);
	    
	    //Restore the original projection matrix
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPopMatrix();
	    
	    //Switch back to modelview and make sure there are no stragglers
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glFlush();
	    
	    //Process the hits
	    hits = gl.glRenderMode(GL.GL_RENDER);
	    selectBuffer.get(selectBuf);
	    processHits(hits, selectBuf);
	}
	
	private void processHits(int hits, int buffer[]) {	    
	    if (hits > 0) {
	    	currentSelection = buffer[3+4*(hits-1)];
	    	if (relocateOriginNextCycle) {
	    		setOrigin(visman.getVisualLocation(currentSelection));
	    		mouseHandler.resetTranslation();
	    		relocateOriginNextCycle = false;
	    	}	    	
	    }
	}
	
	public ManagementServiceInterface getManInterface() {
		return manInterface;
	}
	
	public RegistryServiceInterface getRegInterface() {
		return regInterface;
	}	
	
	public void setRefreshrate(int newRate) {
		this.refreshrate = newRate;
		statman.setRefreshrate(newRate);
	}
}