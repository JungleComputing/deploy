package ibis.deploy.gui.performance;

import ibis.deploy.gui.GUI;
import ibis.deploy.gui.performance.visuals.Vmetric;
import ibis.deploy.gui.performance.visuals.Vobject;
import ibis.deploy.gui.performance.visuals.Vpool;
import ibis.deploy.gui.performance.exceptions.ModeUnknownException;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

import java.awt.Point;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

public class PerfVis implements GLEventListener {
	private static final int BUFSIZE = 512;
	
	public static final int SCOPE_GRID 	= 0;
	public static final int SCOPE_NODES = 1;
	public int currentScope = 0;
	
	public static final int ZOOM_POOLS = 0;
	public static final int ZOOM_SITES = 1;
	public static final int ZOOM_NODES = 2;
	public int currentZoom = 0;
	
	public static final int STAT_ALL = 0;
	public static final int STAT_CPU = 1;
	public static final int STAT_MEM = 2;
	public static final int STAT_COORDS = 3;
	public static final int STAT_LINKS = 4;	
	public int currentStat = 0;
	
	public int currentCollectionForm = Vpool.CITYSCAPE;	
	public int currentElementForm = Vmetric.BAR;
	public int currentLinkForm = Vmetric.TUBE;
	
	private GLU glu;
	GUI gui;
	
	//Window variables
	private double width;
	private double height;	
	GLCanvas canvas;
	
	//Perspective variables	
	private double fovy, aspect, zNear, zFar;
	
	//MouseHandler variables
	public float viewDist = -6, viewX, viewY;
	public boolean doPickNextCycle;
	public Point pickPoint = new Point();
	private int currentSelection;
	private float currentValue;
	
	//JMX variables
	private RegistryServiceInterface regInterface;
	private ManagementServiceInterface manInterface;
	
	private int updateInterval;
	
	private HashMap<Integer, Vobject> glNameRegistry;
	
	private StatsManager statman;
	private VisualManager visman;
	
	PerfVis() {
		glu = new GLU();
	}
	
	PerfVis (GUI gui, GLCanvas canvas) {
		this.gui = gui;
		glu = new GLU();
		this.canvas = canvas;
		
		try {
			this.regInterface = gui.getDeploy().getServer().getRegistryService();
			this.manInterface = gui.getDeploy().getServer().getManagementService();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		statman = new StatsManager(manInterface, regInterface);
		visman = new VisualManager(this);
	}
	
	public int registerGLObject(Vobject visual) {
		int name = glNameRegistry.size();
		glNameRegistry.put(name, visual);
		return name;
	}
	
	public void setScope(int scope) throws ModeUnknownException {
		if (scope != PerfVis.SCOPE_GRID && scope != PerfVis.SCOPE_NODES) {
			throw new ModeUnknownException();
		}
		this.currentScope = scope;
		updateStats();
	}
	
	public void setZoom(int zoom) throws ModeUnknownException {
		if (zoom != PerfVis.ZOOM_POOLS && zoom != PerfVis.ZOOM_SITES && zoom != PerfVis.ZOOM_NODES) {
			throw new ModeUnknownException();
		}	
		this.currentZoom = zoom;
		updateStats();
	}
	
	public void setStat(int stat) throws ModeUnknownException {
		if (stat != PerfVis.STAT_ALL && stat != PerfVis.STAT_CPU && stat != PerfVis.STAT_MEM) {
			throw new ModeUnknownException();
		}
		this.currentStat = stat;
		updateStats();
	}
	
	public void setCollectionForm(int form) throws ModeUnknownException {
		if (form != Vpool.CIRCLE && form != Vpool.CITYSCAPE) {
			throw new ModeUnknownException();
		}
		this.currentCollectionForm = form;
		updateStats();
	}
	
	public void setElementForm(int form) throws ModeUnknownException {
		if (form != Vmetric.BAR && form != Vmetric.TUBE && form != Vmetric.SPHERE) {
			throw new ModeUnknownException();
		}
		this.currentElementForm = form;
	}
		
	public void display(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		
		if (updateInterval > 50) {			
			updateStats();
									
			updateInterval = 0;
		} else {
			updateInterval++;
		}		
		
		if (doPickNextCycle) {
			pick(gl);
			doPickNextCycle = false;
		}
				
		drawHud(gl);		
		drawUniverse(gl, GL.GL_RENDER);
		gl.glFlush();
	}	
	
	public void updateStats() {		
		if (statman.checkPools()) {
			statman.update();
			//We'll need to remake the visualization tree
			System.out.println("UPDATING!");
			visman.reinitialize(statman.getTopConcepts());
		} else {
			statman.update();	
		}			
	}
	
	private void doView(GL gl) {
		gl.glTranslatef(0,0,viewDist);
		gl.glRotatef(viewX, 1,0,0);		
		gl.glRotatef(viewY, 0,1,0);
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
		MouseHandler handler = new MouseHandler(this);
		canvas.addMouseListener(handler);
		canvas.addMouseMotionListener(handler);
		canvas.addMouseWheelListener(handler);
		
		canvas.requestFocusInWindow();
			
		updateStats();			
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
		/*
	    int names, ptr = 0;

	    System.out.println("hits = " + hits);	    
	    
	    for (int i = 0; i < hits; i++) {
	      names = buffer[ptr];
	      System.out.println(" number of names for hit = " + names);
	      ptr++;
	      System.out.println("  z1 is " + buffer[ptr]);
	      ptr++;
	      System.out.println(" z2 is " + buffer[ptr]);
	      ptr++;
	      System.out.print("\n   the name is ");
	      
	      for (int j = 0; j < names; j++) {	    	  
	    	  // for each name
	    	  System.out.println("" + buffer[ptr]);
	    	  ptr++;
	      }
	      
	      System.out.println();
	    }
	    */
	    
	    if (hits > 0) {
	    	currentSelection = buffer[3+4*(hits-1)];
	    }
	}
	
	public ManagementServiceInterface getManInterface() {
		return manInterface;
	}
	
	public RegistryServiceInterface getRegInterface() {
		return regInterface;
	}
	
	public int getCurrentCollectionForm() {
		return currentCollectionForm;
	}

	public void setCurrentCollectionForm(int currentCollectionForm) {
		this.currentCollectionForm = currentCollectionForm;
	}

	public int getCurrentElementForm() {
		return currentElementForm;
	}

	public void setCurrentElementForm(int currentElementForm) {
		this.currentElementForm = currentElementForm;
	}

	public int getCurrentLinkForm() {
		return currentLinkForm;
	}

	public void setCurrentLinkForm(int currentLinkForm) {
		this.currentLinkForm = currentLinkForm;
	}

	public int getCurrentZoom() {
		return currentZoom;
	}

	public void setCurrentZoom(int currentZoom) {
		this.currentZoom = currentZoom;
	}

	public int getCurrentStat() {
		return currentStat;
	}

	public void setCurrentStat(int currentStat) {
		this.currentStat = currentStat;
	}

	public void setHUDValues(String[] names, Float[] values) {
		// TODO Auto-generated method stub
		
	}
}