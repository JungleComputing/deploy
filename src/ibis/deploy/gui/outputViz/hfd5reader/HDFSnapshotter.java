package ibis.deploy.gui.outputViz.hfd5reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class HDFSnapshotter {		
	String evoNamePostfix = ".evo";	
	String gravNamePostfix = ".grav";
	String gasNamePostfix = ".gas";
		
    public HDFSnapshotter() {
    }
    
    public void open(String namePrefix, GLWindow glw, Program ppl, Program gas, int currentFrame) {
    	
    	@SuppressWarnings("unused")
    	Hdf5StarReader2 starReader = null;
    		
    	@SuppressWarnings("unused")
    	Hdf5GasCloudReader gasReader = null;
    	
    	CubeNode cubeRoot;
    		
    	HashMap<Long, Particle2> particles = new HashMap<Long, Particle2>();
    	long[] particleKeys = null;
    	
    	Material starMaterial = new Material();
    	List<Model> starModels = new ArrayList<Model>();
    	
    	Vec4 gasColor = new Vec4(.6f,.3f,.3f,0f);
    	Vec4 transparent = new Vec4(0,0,0,0);
    	Material gasMaterial = new Material(gasColor,transparent,transparent);	
    	List<Model> cloudModels = null;
    	    		
		String evoName, gravName, gasName;
		
		HashMap<String, Dataset> particleResult = new HashMap<String, Dataset>();
		List<HObject> particleMemberList;
		
		float starSize = 0.01f;
		for (int i=0; i < GLWindow.MAX_PREGENERATED_STAR_SIZE; i++ ) {				 
			starModels.add(new Sphere(ppl, starMaterial, 3, starSize, new Vec3()));
			starSize = starSize + 0.01f;
		}
		
		if (GLWindow.GAS_ON) {
			cloudModels = new ArrayList<Model>();
			float gasSize = GLWindow.GAS_EDGES;
			for (int i=0; i < GLWindow.MAX_CLOUD_DEPTH; i++ ) {				 
				cloudModels.add(new Rectangle(gas, gasMaterial, gasSize*2f, gasSize*2f, gasSize*2f, new Vec3(), true));
				gasSize = gasSize/2f;
			}
		}
		
		try {
			if (GLWindow.GAS_ON) {				
				gasName = namePrefix + intToString(currentFrame) + gasNamePostfix;
								
				cubeRoot = new CubeNode(5, 0, cloudModels, new Vec3(-GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES, -GLWindow.GAS_EDGES), GLWindow.GAS_EDGES);
				gasReader = new Hdf5GasCloudReader(currentFrame, cubeRoot, gasName);
				
				glw.setCubeRoot(cubeRoot);				
			}	
						
			particleMemberList = Hdf5StarReader.getRoot(namePrefix + intToString(0) + evoNamePostfix).getMemberList();
			Hdf5Reader.traverse("evo", particleResult, particleMemberList);
			
			particleMemberList = Hdf5StarReader.getRoot(namePrefix + intToString(0) + gravNamePostfix).getMemberList();
			Hdf5Reader.traverse("grav", particleResult, particleMemberList);
			
			Hdf5Reader.closeFiles();	
			
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
			starReader = new Hdf5StarReader2(starModels, particles, evoName, gravName);
			
			//Construct the Scene graph		
			ParticleNode root = new ParticleNode();
			for (int i = 0; i < particleKeys.length; i++) {
				ParticleNode node = new ParticleNode();		
				Particle2 p = particles.get(particleKeys[i]);
				node.addModel(p.model);
				Vec4 color = p.color;
				node.materials.add( new Material(color,color,color) );
				node.setTranslation(p.location);				
				root.addChild(node);
			}
						
			glw.setRoot(root);					
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
}
