package ibis.deploy.gui.outputViz;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.common.scenegraph.SGNode;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;
import ibis.deploy.gui.outputViz.hfd5reader.Astrophysics;
import ibis.deploy.gui.outputViz.hfd5reader.CubeNode;
import ibis.deploy.gui.outputViz.hfd5reader.HDFTimer;
import ibis.deploy.gui.outputViz.models.Axis;
import ibis.deploy.gui.outputViz.models.base.Quad;
import ibis.deploy.gui.outputViz.shaders.*;

import java.nio.FloatBuffer;

import javax.media.opengl.*;

public class GLWindow implements GLEventListener {	
	public static boolean MULTICOLOR_GAS = false;
	public static boolean GAS_COLORMAP = true;
	public static boolean POST_PROCESS = true;
	public static boolean AXES = true;
	public static boolean GAS_ON = true;
	public static boolean PREDICTION_ON = false;
	
	public static long WAITTIME = 100;
	public static long LONGWAITTIME = 10000;
	public static int MAX_CLOUD_DEPTH = 25;
	public static int MAX_STAR_SIZE = 50;
	public static float GAS_EDGES = 800f;
	public static int MAX_ELEMENTS_PER_OCTREE_NODE = 25;
	public static float EPSILON = 1.0E-7f;
	public static float GAS_OPACITY_FACTOR = .8f;
	
	private ProgramLoader loader;
	
	Program animatedTurbulence;
	Program ppl, axes, gas, postprocess;
	Program gaussianBlur;
	
	Perlin3D noiseTex;
		
	FloatBuffer color1 = (new Vec3(0f,0f,0f)).asBuffer();
	FloatBuffer color2 = (new Vec3(.6f,.1f,0f)).asBuffer();
	FloatBuffer color3 = (new Vec3(.5f,.5f,.5f)).asBuffer();
	
	float[] bla1 = {1.2f};
	FloatBuffer noiseScale = FloatBuffer.wrap(bla1);
	FloatBuffer lightPos = (new Vec3(2f,2f,2f)).asBuffer();
	float[] bla2 = {2f};
	FloatBuffer scale = FloatBuffer.wrap(bla2);
	float[] bla3 = {0f};
	FloatBuffer offset = FloatBuffer.wrap(bla3);
	
	HDFTimer timer;
	public boolean timerInitialized = false;
	
	SGNode root, root2;
	CubeNode cubeRoot, cubeRoot2;		
	boolean newRoot = true, newCubeRoot = true;
	
	float[] bla4 = {50f};
	FloatBuffer shininess = FloatBuffer.wrap(bla4);
	
	private float radius = 1.0f;
	private float ftheta = 0.0f;
	private float phi = 0.0f;
    
	private float  fovy = 45.0f;
	private float  aspect;
	private float  zNear = 0.1f, zFar = 3000.0f;
	
	private int canvasWidth, canvasHeight;
	
	private Vec3 rotation = new Vec3();
	private Vec3 translation = new Vec3(0f, 0f, -150f);
	
	Texture2D axesTex, gasColorTex;
	
	Texture2D gasTex, starTex;
	
	Model fullscreenQuad, fullscreenQuad0, fullscreenQuad1, fullscreenQuad2, volumeGas;
	Model xAxis, yAxis, zAxis;
	
	public GLWindow() {		
		loader = new ProgramLoader();
		
		noiseTex = new Perlin3D(128);		
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		// Anti-Aliasing
		gl.glEnable(GL3.GL_LINE_SMOOTH);
		gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
		gl.glEnable(GL3.GL_POLYGON_SMOOTH);
		gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);

		// Depth testing
//		gl.glEnable(GL3.GL_DEPTH_TEST);
		gl.glDepthFunc(GL3.GL_LEQUAL);
		gl.glClearDepth(1.0f);

		// Culling
		//gl.glEnable(GL3.GL_CULL_FACE);

		// Enable Blending (needed for both Transparency and Anti-Aliasing
		gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL3.GL_BLEND);
		
		// Enable Vertical Sync
		gl.setSwapInterval(1);

		// Load and compile shaders, then use program.		
		try {
			animatedTurbulence = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_sunsurface.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_animatedTurbulence.fp");
//			gas = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_sunsurface.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_animatedTurbulence.fp");
			ppl = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_ppl.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_ppl.fp");
			axes = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_axes.fp");
			gas = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_gas.fp");
//			gas = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_volumerendering.fp");
//			gas = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_turbulence.fp");
			//glow = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_glow.vp", "src/ibis/deploy/gui/outputViz/shaders/src/gs_glow.fp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_glow.fp");
			//star = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_star.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_star.fp");
			if (POST_PROCESS) postprocess = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_postprocess.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_postprocess.fp");
			if (POST_PROCESS) gaussianBlur = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_postprocess.vp", "src/ibis/deploy/gui/outputViz/shaders/src/fs_gaussian_blur.fp");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
//		timer.init(this, ppl);
//		new Thread(timer).start();
				
		root = new SGNode();
		newRoot = false;
		
		cubeRoot = new CubeNode();
		newCubeRoot = false;
		
//		for (int i = 0; i < particles.length; i++) {
//			Material randomMaterial = Material.random();
//			SGNode node = new SGNode();			
//			node.addModel(new Sphere(animatedTurbulence, randomMaterial, 3, 1, new Vec3()));
//			//node.translate(new Vec3((float)particles[i].x, (float)particles[i].y, (float)particles[i].z));
//			root.addChild(node);
//		}
			
//		for (int i = 0; i < particles.length; i++) {
//			//Material material = Material.random();
//			Material material = new Material(particles[i].color,particles[i].color,particles[i].color);
//			SGNode node = new SGNode();			
//			node.addModel(new Sphere(ppl, material, 3, (float)(particles[i].radius / 10E9), new Vec3()));
//			node.translate(new Vec3((float)(particles[i].x / 10E14), (float)(particles[i].y / 10E14), (float)(particles[i].z / 10E14)));
//			//node.setDirection(new Vec3((float)particles[i].vx, (float)particles[i].vy, (float)particles[i].vz));
//			root.addChild(node);
//		}
		
		root.init(gl);
		cubeRoot.init(gl);
		
		noiseTex.init(gl);
				
//		volumeGas = new Rectangle(gas, new Material(), 400, 400, 400, new Vec3(0,0,0), true);
//		volumeGas.init(gl);
		
		if (AXES) {
			Color4 axisColor = new Color4(0f,1f,0f,.3f);
			Material axisMaterial = new Material(axisColor, axisColor, axisColor);
			xAxis = new Axis(axes, axisMaterial, new Vec3(-800f,0f,0f), new Vec3(800f,0f,0f), Astrophysics.toScreenCoord(1), Astrophysics.toScreenCoord(.2));
			xAxis.init(gl);
			yAxis = new Axis(axes, axisMaterial, new Vec3(0f,-800f,0f), new Vec3(0f,800f,0f), Astrophysics.toScreenCoord(1), Astrophysics.toScreenCoord(.2));
			yAxis.init(gl);			
			zAxis = new Axis(axes, axisMaterial, new Vec3(0f,0f,-800f), new Vec3(0f,0f,800f), Astrophysics.toScreenCoord(1), Astrophysics.toScreenCoord(.2));
			zAxis.init(gl);
		}
		
		if (POST_PROCESS) {
			fullscreenQuad = new Quad(postprocess, Material.random(), 2, 2, new Vec3(0,0,0.1f));
			fullscreenQuad.init(gl);
			
			fullscreenQuad0 = new Quad(ppl, Material.random(), 2, 2, new Vec3(0,0,0.1f));
			fullscreenQuad0.init(gl);
			
			fullscreenQuad1 = new Quad(gaussianBlur, Material.random(), 2, 2, new Vec3(0,0,0.1f));
			fullscreenQuad1.init(gl);
			
			fullscreenQuad2 = new Quad(gaussianBlur, Material.random(), 2, 2, new Vec3(0,0,0.1f));
			fullscreenQuad2.init(gl);
			
			gl.glActiveTexture(GL3.GL_TEXTURE1);
			axesTex = new PostProcessTexture(canvasWidth, canvasHeight);
			axesTex.init(gl);
			
			gl.glActiveTexture(GL3.GL_TEXTURE2);
			gasColorTex = new PostProcessTexture(canvasWidth, canvasHeight);
			gasColorTex.init(gl);
			
			gl.glActiveTexture(GL3.GL_TEXTURE3);
			gasTex = new PostProcessTexture(canvasWidth, canvasHeight);
			gasTex.init(gl);
			
			gl.glActiveTexture(GL3.GL_TEXTURE4);
			starTex = new PostProcessTexture(canvasWidth, canvasHeight);
			starTex.init(gl);
		}
		
		//Cube map test
		//enableCubemaps(gl);	
		
		gl.glClearColor(0f, 0f, 0f, 0f);
	}

	public void display(GLAutoDrawable drawable) {
		try {
			GL3 gl = drawable.getGL().getGL3();
					
			gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
			
			int multiTex;
				    
		    Point4 eye = new Point4((float)(radius*Math.sin(ftheta)*Math.cos(phi)),
						    		(float)(radius*Math.sin(ftheta)*Math.sin(phi)),
						    		(float)(radius*Math.cos(ftheta)),
									1.0f );
		    Point4 at = new Point4( 0.0f, 0.0f, 0.0f, 1.0f);
		    Vec4 up = new Vec4( 0.0f, 1.0f, 0.0f, 0.0f );
		    
		    Mat4 mv = MatrixMath.lookAt(eye, at, up);
		    mv = mv.mul(MatrixMath.translate(translation));
		    mv = mv.mul(MatrixMath.rotationX(rotation.get(0)));
		    mv = mv.mul(MatrixMath.rotationY(rotation.get(1)));
	
		    Mat3 n = new Mat3();
		    loader.setUniformMatrix("NormalMatrix", n.asBuffer());
		    
		    Mat4 p = MatrixMath.perspective( fovy, aspect, zNear, zFar );	    
		    loader.setUniformMatrix("PMatrix", p.asBuffer());
		    
		    offset.put(0, offset.get(0)+0.0001f);    
		    	    	
			//Vertex shader    	
			loader.setUniformVector("LightPos", lightPos);
			loader.setUniformMatrix("SMatrix", MatrixMath.scale(1, 1, 1).asBuffer());		
	    	
	    	//Fragment shader    	
			loader.setUniformVector("Color1", color1);
			loader.setUniformVector("Color2", color2);
			loader.setUniformVector("Color3", color3);
			
	    	loader.setUniformVector("Offset", offset);		
			loader.setUniformVector("Shininess", shininess);
			
			loader.setUniform("scrWidth", canvasWidth);
    		loader.setUniform("scrHeight", canvasHeight);
    		
    		if (timerInitialized) synchronized (timer) {
	    	SGNode root = initSGRoot(gl);
	    	CubeNode cubeRoot = initCubeRoot(gl);
	
			if (GAS_COLORMAP) {
				multiTex = 2;
				
	    		loader.setUniformMatrix("SMatrix", MatrixMath.scale(2, 2, 2).asBuffer());
	    		loader.setUniform("Mode", 1);
	    		loader.setUniform("Multicolor", 1);	    
	    		ppl.use(gl);
	    				    	
		    	gl.glEnable(GL3.GL_DEPTH_TEST);
		    	
		    	root.draw(gl, mv);
		    	
		    	renderToTexture(gl, multiTex, gasColorTex);
		    			    	
		    	loader.setUniformMatrix("SMatrix", MatrixMath.scale(1, 1, 1).asBuffer());
		    	loader.setUniform("Colormap", multiTex);
		    	
		    	gas.use(gl);
		    	gl.glDisable(GL3.GL_DEPTH_TEST);		    	
		    	cubeRoot.draw(gl, mv);
			} else {
				loader.setUniform("Multicolor", 0);
				
				gas.use(gl);
		    	gl.glDisable(GL3.GL_DEPTH_TEST);		    	
		    	cubeRoot.draw(gl, mv);
			}
	    	
	    	if (POST_PROCESS) {
	    		multiTex = 3;
	    		renderToTexture(gl, multiTex, gasTex);	    		
    			loader.setUniform("Texture", multiTex);  
    			
        		loader.setUniformMatrix("PMatrix", new Mat4().asBuffer());
        		
        		loader.setUniform("blurType", 2);
        		loader.setUniform("blurDirection", 0);  
        		gaussianBlur.use(gl);
    	    	fullscreenQuad1.draw(gl, new Mat4());
        		renderToTexture(gl, multiTex, gasTex);
        		
        		loader.setUniform("blurDirection", 1);
        		gaussianBlur.use(gl);
    	    	fullscreenQuad2.draw(gl, new Mat4());
        		renderToTexture(gl, multiTex, gasTex);
	    	}
	    	
	    	
    		multiTex = 0;
    		noiseTex.use(gl, multiTex);
    		loader.setUniform("Noise", multiTex);
    		loader.setUniformMatrix("PMatrix", p.asBuffer());    
    		loader.setUniform("Mode", 0);
    		ppl.use(gl);
	    	gl.glEnable(GL3.GL_DEPTH_TEST);
	    	
	    	root.draw(gl, mv);
	    	    	
	    	if (POST_PROCESS) {
	    		multiTex = 4;
	    		renderToTexture(gl, multiTex, starTex);
	    	}
	    	
	    	if (AXES) {
	    		axes.use(gl);
	    		
	    		xAxis.draw(gl, mv);
	    		yAxis.draw(gl, mv);
	    		zAxis.draw(gl, mv);
	    		
	    		if (POST_PROCESS) {
	        		multiTex = 1;
	        		renderToTexture(gl, multiTex, axesTex);
	    		}
	    	}
	    	
	    	if (POST_PROCESS) {		
    			loader.setUniform("axesTexture", 1);
        		loader.setUniform("gasTexture", 3);
        		loader.setUniform("starTexture", 4);        		
        		
        		loader.setUniform("scrWidth", canvasWidth);
        		loader.setUniform("scrHeight", canvasHeight);
	    		postprocess.use(gl);
		    	    	    	
		    	loader.setUniformMatrix("PMatrix", new Mat4().asBuffer());
		    	fullscreenQuad.draw(gl, new Mat4());
	    	}
    		}
		} catch (UninitializedException e) {
			e.printStackTrace();
		}
	}
	
	private void renderToTexture(GL3 gl, int multiTex, Texture2D target) {
		try {
			target.use(gl, multiTex);
			gl.glCopyTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, 0, 0, canvasWidth, canvasHeight, 0);
			gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
		} catch (UninitializedException e) {
			e.printStackTrace();
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
	    GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, w, h);
        aspect = (float) w / h;
        canvasWidth = w;
        canvasHeight = h;
        
        gl.glActiveTexture(GL3.GL_TEXTURE2);
		gasColorTex = new PostProcessTexture(canvasWidth, canvasHeight);
		gasColorTex.init(gl);
		
		gl.glActiveTexture(GL3.GL_TEXTURE3);
		gasTex = new PostProcessTexture(canvasWidth, canvasHeight);
		gasTex.init(gl);
		
		gl.glActiveTexture(GL3.GL_TEXTURE4);
		starTex = new PostProcessTexture(canvasWidth, canvasHeight);
		starTex.init(gl);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
	}

	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		loader.cleanup(gl);
	}	

	public float getViewDist() {		
		return translation.get(2);
	}

	public void setViewDist(float viewDist) {
		translation.set(2, viewDist);
	}

	public void setRotation(Vec3 rotation) {
		this.rotation = rotation;
	}

	public synchronized void setRoot(SGNode root) {
		this.root2 = root; 		
		newRoot = true;
	}
	
	public void setCubeRoot(CubeNode cubeRoot) {
		this.cubeRoot2 = cubeRoot;
		newCubeRoot = true;
	}
	
	private synchronized SGNode initSGRoot(GL3 gl) {
		if (newRoot) {
			root = root2;			
			root.init(gl);
		}
		newRoot = false;
		
		return root;
	}
	
	private synchronized CubeNode initCubeRoot(GL3 gl) {
		if (newCubeRoot) {
			cubeRoot = cubeRoot2;
			cubeRoot.init(gl);
		}
		newCubeRoot = false;
		
		return cubeRoot;
	}

	public void startAnimation(HDFTimer timer) {
		this.timer = timer;
		timer.init(this, ppl, gas);
		new Thread(timer).start();	
		timerInitialized = true;
	}
	
	public void stopAnimation() {
		if (timerInitialized) {
			setRoot(new SGNode());
			setCubeRoot(new CubeNode());
			timer.close();
			timer.stop();
		}
		timerInitialized = false;
	}

	public static void setMulticolor(boolean newSetting) {
		MULTICOLOR_GAS = newSetting;
	}
	
	public static void setColormap(boolean newSetting) {
		GAS_COLORMAP = newSetting;
	}
	
	public static void setPostprocess(boolean newSetting) {
		POST_PROCESS = newSetting;
	}
	
	public static void setAxes(boolean newSetting) {
		AXES = newSetting;
	}
	
	public static void setGas(boolean newSetting) {
		GAS_ON = newSetting;
	}
	
	public static void setPrediction(boolean newSetting) {
		PREDICTION_ON = newSetting;
	}
}
