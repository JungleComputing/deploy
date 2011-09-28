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
import ibis.deploy.gui.outputViz.common.Model;
import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.Vec4;
import ibis.deploy.gui.outputViz.exceptions.FileOpeningException;
import ibis.deploy.gui.outputViz.models.base.Rectangle;
import ibis.deploy.gui.outputViz.models.base.Sphere;
import ibis.deploy.gui.outputViz.shaders.Program;

public class HDFTimer implements Runnable {	
	public static enum states { UNOPENED, UNINITIALIZED, INITIALIZED, STOPPED, WAITINGONFRAME, PLAYING};
	
	public states currentState = states.UNOPENED;
	public int currentFrame;
	
	private JSlider timeBar;
	private JFormattedTextField frameCounter;
	//private Hdf5Reader reader;
	
	@SuppressWarnings("unused")
	private Hdf5StarReader reader;
	
	//private Hdf5GasReader gasReader;
	
	@SuppressWarnings("unused")
	private Hdf5GasCloudReader gasReader;
	
	CubeNode cubeRoot;
	
	private ArrayList<Integer> framesRead;
	private HashMap<Long, Particle> particles;
//	private HashMap<Integer, Texture3D> gasses;
	private HashMap<Long, ParticleNode> nodes;
	private long[] particleKeys;
	
	Material starMaterial = new Material();
	List<Model> starModels;
	
	Vec4 gasColor = new Vec4(.6f,.3f,.3f,0f);
	Vec4 transparent = new Vec4(0,0,0,0);
	Material gasMaterial = new Material(gasColor,transparent,transparent);	
	List<Model> cloudModels;
	
	private boolean running = true;
	
	GLWindow glw;
	Program ppl, gas;
	
	String path;
	String namePrefix;
	String evoNamePostfix = ".evo";	
	String gravNamePostfix = ".grav";
	String gasNamePostfix = ".gas";
	
	long startTime, stopTime;
	
	float maxGasDensity;
	
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
    
    public void init(GLWindow glw, Program ppl, Program gas) {
    	this.glw = glw;
    	this.ppl = ppl;
    	this.gas = gas;
    	
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
		framesRead = new ArrayList<Integer>();
		particles = new HashMap<Long, Particle>();
//		gasses = new HashMap<Integer, Texture3D>();
		nodes = new HashMap<Long, ParticleNode>();
		
		HashMap<String, Dataset> particleResult = new HashMap<String, Dataset>();
		List<HObject> particleMemberList;
		
		starModels = new ArrayList<Model>();
		float starSize = 0.01f;
		for (int i=0; i < GLWindow.MAX_STAR_SIZE; i++ ) {				 
			starModels.add(new Sphere(ppl, starMaterial, 2, starSize, new Vec3()));
			starSize = starSize + 0.01f;
		}
		
		if (GLWindow.GAS_ON) {
			cloudModels = new ArrayList<Model>();
			float gasSize = GLWindow.GAS_EDGES;
			for (int i=0; i < GLWindow.MAX_CLOUD_DEPTH; i++ ) {				 
				cloudModels.add(new Rectangle(gas, gasMaterial, gasSize*2f, gasSize*2f, gasSize*2f, new Vec3(), true));
//				cloudModels.add(new Sphere(gas, gasMaterial, 1, gasSize*4f, new Vec3()));
//				cloudModels.add(new Quad(gas, gasMaterial, gasSize*4f, gasSize*4f, new Vec3()));
				gasSize = gasSize/2f;
			}
		}
		
		try {
			if (GLWindow.GAS_ON) {				
				gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
								
				cubeRoot = new CubeNode(0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
				gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
				
//				maxGasDensity = cubeRoot.getMaxDensity();
//				System.out.println(maxGasDensity);
//				cubeRoot.setTransparency(maxGasDensity);
				
				glw.setCubeRoot(cubeRoot);
				Hdf5GasCloudReader.closeFiles();
				
//				Hdf5GasCloudReader2 gasReader2 = new Hdf5GasCloudReader2(currentFrame, gasName);				
//				gasses.put(currentFrame, gasReader2.tex3d);
//				Hdf5GasCloudReader2.closeFiles();
				
			}	
			
			int initialMaxBar = Hdf5StarReader.getNumFiles(path, gravNamePostfix);
			timeBar.setMaximum(initialMaxBar);
			
			particleMemberList = Hdf5StarReader.getRoot(namePrefix + intToString(0) + evoNamePostfix).getMemberList();
			Hdf5Reader.traverse("evo", particleResult, particleMemberList);
			
			particleMemberList = Hdf5StarReader.getRoot(namePrefix + intToString(0) + gravNamePostfix).getMemberList();
			Hdf5Reader.traverse("grav", particleResult, particleMemberList);
			
			Dataset keysSet = particleResult.get("evo/particles/0000000001/keys");
			particleMemberList = null;
			
			//Pre-make all the particles in the hashmap
			particleKeys = (long[]) keysSet.read();
			for (int i = 0; i< particleKeys.length; i++) {
		    	particles.put(particleKeys[i], new Particle());
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
			reader = new Hdf5StarReader(currentFrame, starModels, particles, evoName, gravName);
			
			//Construct the Scene graph		
			ParticleNode root = new ParticleNode();
			for (int i = 0; i < particleKeys.length; i++) {
				ParticleNode node = new ParticleNode();
				nodes.put(particleKeys[i], node);				
				Particle p = particles.get(particleKeys[i]);
				node.addModel(p.getModel(currentFrame));
				Vec4 color = p.getLastUniqueColor(currentFrame);
				node.materials.add( new Material(color,color,color) );
				node.setTranslation(p.location.get(currentFrame));
//				node.setTranslation(new Vec3((float)(p.x.get(currentFrame) / 10E14), (float)(p.y.get(currentFrame) / 10E14), (float)(p.z.get(currentFrame) / 10E14)));
				if (GLWindow.PREDICTION_ON) node.setSpeedVec(p.direction.get(currentFrame));				
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
		
		framesRead.add(0);
		currentFrame = 1;
				
		glw.timerInitialized = true;
		currentState = states.PLAYING;
	    		
		while(running) {
			if (currentState == states.PLAYING) {
				try {
					startTime = System.currentTimeMillis();							
					
					updateFrame();
				    
					timeBar.setValue(currentFrame);
					frameCounter.setValue(currentFrame);
				    currentFrame++;
										
					stopTime = System.currentTimeMillis();
					System.out.println("Frame "+currentFrame+" built in "+(stopTime-startTime)+" ms.");
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
			} else if (currentState == states.STOPPED) {
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
		}
	}
	
	private void updateFrame() {
		synchronized (this) {
			try {
				String gasName, evoName, gravName;
				if (!framesRead.contains(currentFrame)) {	
					if (GLWindow.GAS_ON) {
						gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
						
						cubeRoot = new CubeNode(0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
						gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
					}
					
					evoName = namePrefix + intToString(currentFrame) + evoNamePostfix;
					gravName = namePrefix + intToString(currentFrame) + gravNamePostfix;
					
					reader = new Hdf5StarReader(currentFrame, starModels, particles, evoName, gravName);
					
					framesRead.add(currentFrame);
				}
				
				for (int i = 0; i < particles.size(); i++) {
					ParticleNode node = nodes.get(particleKeys[i]);
			    	Particle p = particles.get(particleKeys[i]);
			    	if (p != null) {
			    		node.setTranslation(p.location.get(currentFrame));
			    		
			    		if (GLWindow.PREDICTION_ON) node.setSpeedVec(p.direction.get(currentFrame));
			    	}
				}
				
				if (GLWindow.GAS_ON) {
					if (GLWindow.GAS_ON) {
						gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
						
						cubeRoot = new CubeNode(0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
						try {
							gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
						} catch (FileOpeningException e) {
							e.printStackTrace();
						}
					}
					
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
		currentState = states.PLAYING;
	}
	
	public void stop() {
		currentState = states.STOPPED;
	}
	
	public void rewind() {
		setFrame(0);
		timeBar.setValue(currentFrame);
		frameCounter.setValue(currentFrame);
		currentState = states.STOPPED;
	}

	public void setFrame(int value) {
		currentState = states.STOPPED;
		currentFrame = value;	
		
		updateFrame();
	}
}
