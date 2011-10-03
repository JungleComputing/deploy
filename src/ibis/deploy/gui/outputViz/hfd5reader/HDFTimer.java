package ibis.deploy.gui.outputViz.hfd5reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.JSlider;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.HObject;
import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.Vec4;
import ibis.deploy.gui.outputViz.exceptions.FileOpeningException;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.models.base.Sphere;
import ibis.deploy.gui.outputViz.shaders.Program;

public class HDFTimer implements Runnable {	
	public static enum states { UNOPENED, UNINITIALIZED, INITIALIZED, STOPPED, REDRAWING, SNAPSHOTTING, CLEANUP, WAITINGONFRAME, PLAYING};
	
	public static states currentState = states.UNOPENED;
	public int currentFrame;
	
	private JSlider timeBar;
	private JFormattedTextField frameCounter;
	//private Hdf5Reader reader;
	
	@SuppressWarnings("unused")
	private Hdf5StarReader2 reader;
	
	@SuppressWarnings("unused")
	private Hdf5GasCloudReader gasReader;
	
	private ParticleNode root;
	private CubeNode cubeRoot;
	
	private HashMap<Long, Particle2> particles;
	private HashMap<Long, ParticleNode> nodes;
	private long[] particleKeys;
	
	private Material starMaterial = new Material();
	private HashMap<Integer, Model> starModels;
	
	private Vec4 gasColor = new Vec4(.6f,.3f,.3f,0f);
	private Vec4 transparent = new Vec4(0,0,0,0);
	private Material gasMaterial = new Material(gasColor,transparent,transparent);	
	private List<Model> cloudModels;
	
	private boolean running = true;
	
	private GLWindow glw;
	private Program ppl, gas, animatedTurbulence;
	
	private String path;
	private String namePrefix;
	private String evoNamePostfix = ".evo";	
	private String gravNamePostfix = ".grav";
	private String gasNamePostfix = ".gas";
	
	private long startTime, stopTime;
	
    public HDFTimer(JSlider timeBar, JFormattedTextField frameCounter) {    	
    	this.timeBar = timeBar;
    	this.frameCounter = frameCounter;
    }
    
    public void close() {
    	running = false;
    	currentState = states.UNOPENED;			
		currentFrame = 0;
		timeBar.setValue(0);
		frameCounter.setValue(0);
		timeBar.setMaximum(0);
	}
    
    public void open(String path, String namePrefix) {
    	this.path = path;
    	this.namePrefix = namePrefix;
    	currentState = states.UNINITIALIZED;
    }
    
    public void init(GLWindow glw, Program ppl, Program gas, Program animatedTurbulence) {
    	this.glw = glw;
    	this.ppl = ppl;
    	this.gas = gas;
    	this.animatedTurbulence = animatedTurbulence;
    	
    	currentState = states.INITIALIZED;
    }

	public void run() {
		if (currentState != states.INITIALIZED) {
			System.err.println("HDFTimer started while not initialized.");
			System.exit(1);
		}
		
		String evoName, gravName, gasName;
		
		reader = null;
		gasReader = null;
		particles = new HashMap<Long, Particle2>();
//		gasses = new HashMap<Integer, Texture3D>();
		nodes = new HashMap<Long, ParticleNode>();
		
		HashMap<String, Dataset> particleResult = new HashMap<String, Dataset>();
		List<HObject> particleMemberList;
		
		starModels = new HashMap<Integer, Model>();
		float starSize = 0.01f;
		for (int i=0; i < GLWindow.MAX_PREGENERATED_STAR_SIZE; i++ ) {				 
			starModels.put(i, new Sphere(ppl, starMaterial, 2, starSize, new Vec3()));
			starSize = starSize + 0.01f;
		}
		
		if (GLWindow.GAS_ON) {
			cloudModels = new ArrayList<Model>();
			float gasSize = GLWindow.GAS_EDGES;
			for (int i=0; i < GLWindow.MAX_CLOUD_DEPTH; i++ ) {
				cloudModels.add(new Sphere(gas, gasMaterial, 0, gasSize*3f, new Vec3()));
				gasSize = gasSize/2f;
			}
		}
		
		try {
			if (GLWindow.GAS_ON) {				
				gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
								
				cubeRoot = new CubeNode(GLWindow.MAX_ELEMENTS_PER_OCTREE_NODE, 0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
				gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
				
//				maxGasDensity = cubeRoot.getMaxDensity();
//				System.out.println(maxGasDensity);
//				cubeRoot.setTransparency(maxGasDensity);
				
				glw.setCubeRoot(cubeRoot);
				
//				Hdf5GasCloudReader2 gasReader2 = new Hdf5GasCloudReader2(currentFrame, gasName);				
//				gasses.put(currentFrame, gasReader2.tex3d);
//				Hdf5GasCloudReader2.closeFiles();
				
			}	
			
			int initialMaxBar = Hdf5StarReader2.getNumFiles(path, gravNamePostfix);
			timeBar.setMaximum(initialMaxBar);
			
			particleMemberList = Hdf5StarReader2.getRoot(namePrefix + intToString(0) + evoNamePostfix).getMemberList();
			Hdf5Reader.traverse("evo", particleResult, particleMemberList);
			
			particleMemberList = Hdf5StarReader2.getRoot(namePrefix + intToString(0) + gravNamePostfix).getMemberList();
			Hdf5Reader.traverse("grav", particleResult, particleMemberList);
			
			Dataset keysSet = particleResult.get("evo/particles/0000000001/keys");
			particleMemberList = null;
			
			//Pre-make all the particles in the hashmap
			particleKeys = (long[]) keysSet.read();
			for (int i = 0; i< particleKeys.length; i++) {
		    	particles.put(particleKeys[i], new Particle2());
		    }
			
			//Close the datasets
			for (Dataset d : particleResult.values()) {
				d.clear();
				d.close(0);
		    }
			
			//Get the initial data for the particles (frame 0)
			evoName = namePrefix + intToString(currentFrame) + evoNamePostfix;
			gravName = namePrefix + intToString(currentFrame) + gravNamePostfix;
			//reader = new Hdf5Reader(currentFrame, program, particles, evoName, gravName);
			reader = new Hdf5StarReader2(starModels, particles, evoName, gravName);
			
			//Construct the Scene graph		
			root = new ParticleNode();
			for (int i = 0; i < particleKeys.length; i++) {
				ParticleNode node = new ParticleNode();
				nodes.put(particleKeys[i], node);				
				Particle2 p = particles.get(particleKeys[i]);
				node.addModel(p.model);
				Vec4 color = p.color;
				node.materials.add( new Material(color,color,color) );
				node.setTranslation(p.location);
//				node.setTranslation(new Vec3((float)(p.x.get(currentFrame) / 10E14), (float)(p.y.get(currentFrame) / 10E14), (float)(p.z.get(currentFrame) / 10E14)));
//				if (GLWindow.PREDICTION_ON) node.setSpeedVec(p.direction.get(currentFrame));				
				root.addChild(node);
			}
						
			glw.setRoot(root);
			
			Hdf5Reader.closeFiles();
			
						
		} catch (FileOpeningException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
				
		glw.timerInitialized = true;
		currentState = states.PLAYING;
	    		
		while(running) {
			if (currentState == states.PLAYING || currentState == states.REDRAWING) {
				try {
					startTime = System.currentTimeMillis();					
					
					timeBar.setValue(currentFrame);
					frameCounter.setValue(currentFrame);
					
					currentFrame++;					
					updateFrame();
										
					stopTime = System.currentTimeMillis();
//					System.out.println("Frame "+currentFrame+" built in "+(stopTime-startTime)+" ms.");
				    if (startTime-stopTime < GLWindow.WAITTIME) {
						Thread.sleep(GLWindow.WAITTIME - (startTime-stopTime));
					} else {
						Thread.yield();
					}
//				} catch (FileOpeningException e) {
//					System.out.println(e.getMessage());
//					currentState = states.STOPPED;
				} catch (InterruptedException e) {
					//Bla
				}
				if (currentState == states.REDRAWING) currentState = states.STOPPED;
			} else if (currentState == states.STOPPED || currentState == states.SNAPSHOTTING || currentState == states.CLEANUP) {
				try {
					Thread.sleep(GLWindow.WAITTIME);
				} catch (InterruptedException e) {
					//Bla
				}
			} else if (currentState == states.WAITINGONFRAME) {
				try {
					Thread.sleep(GLWindow.LONGWAITTIME);
					currentState = states.PLAYING;
				} catch (InterruptedException e) {
					//Bla
				}
			}
			System.gc();
		}
	}
	
	private void updateFrame() {
		synchronized (this) {
			try {
				String gasName, evoName, gravName;					
				evoName = namePrefix + intToString(currentFrame) + evoNamePostfix;
				gravName = namePrefix + intToString(currentFrame) + gravNamePostfix;
				
				reader = new Hdf5StarReader2(starModels, particles, evoName, gravName);
									
				for (int i = 0; i < particles.size(); i++) {
					ParticleNode node = nodes.get(particleKeys[i]);
			    	Particle2 p = particles.get(particleKeys[i]);
			    	if (p != null) {
			    		Model m = p.model;
			    		Vec4 color = p.color;
			    		node.setModel(m, new Material(color,color,color));
			    		node.setTranslation(p.location);
			    		
//			    		if (GLWindow.PREDICTION_ON) node.setSpeedVec(p.direction.get(currentFrame));
			    	}
				}
				
				glw.setRoot(root);
				
				if (GLWindow.GAS_ON) {
					gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
					
					cubeRoot = new CubeNode(GLWindow.MAX_ELEMENTS_PER_OCTREE_NODE, 0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
					gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
											
					glw.setCubeRoot(cubeRoot);
				}
			} catch (FileOpeningException e) {
				currentState = states.WAITINGONFRAME;
			}
		} 
	}
		
	private String intToString(int input) {		
		String result = ""; 
		if (input < 10) {
			result += "00000"+input;
		} else if (input >= 10 && input < 100) {
			result += "0000" +input;
		} else if (input >= 100 && input < 1000) {
			result += "000"   +input;
		} else if (input >= 1000 && input < 10000) {
			result += "00"    +input;
		}  else if (input >= 10000 && input < 100000) {
			result += "0"    +input;
		} else {
			result += input;
		}
		
		return result;
	}

	public void start() {
		GLWindow.AXES = true;
		if (currentState == states.SNAPSHOTTING) {
			currentState = states.CLEANUP;
		} else {
			currentState = states.PLAYING;
		}
	}
	
	public void stop() {
		if (currentState == states.SNAPSHOTTING) {
			currentState = states.CLEANUP;
		} else {
			currentState = states.STOPPED;
		}
	}
	
	public void rewind() {
		if (currentState == states.SNAPSHOTTING) {
			currentState = states.CLEANUP;
		} else {
			currentState = states.STOPPED;
		}
		
		setFrame(0);
		timeBar.setValue(currentFrame);		
	}

	public void setFrame(int value) {
		synchronized (this) {
			if (currentState == states.SNAPSHOTTING) {
				currentState = states.CLEANUP;
			} else {
				currentState = states.STOPPED;
			}
			currentFrame = value;
			
			updateFrame();	
			frameCounter.setValue(currentFrame);
		}
	}
	
	public void makeSnapshot() {
		synchronized (this) {
			GLWindow.AXES = false;
			currentState = states.SNAPSHOTTING;
//			System.out.println("Snapshotting "+currentFrame);
			HDFSnapshotter snappy = new HDFSnapshotter();
			snappy.open(namePrefix, glw, animatedTurbulence, gas, currentFrame);
		}
	}

	public static void setState(states newState) {
		currentState = newState;		
	}
}
