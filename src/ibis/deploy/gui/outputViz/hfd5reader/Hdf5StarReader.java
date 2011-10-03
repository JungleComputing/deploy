package ibis.deploy.gui.outputViz.hfd5reader;

import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.Vec4;
import ibis.deploy.gui.outputViz.exceptions.FileOpeningException;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.models.base.Sphere;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import ncsa.hdf.object.*;

public class Hdf5StarReader {	
	static class ExtFilter implements FilenameFilter {
		private String ext;
		
		public ExtFilter(String ext) {
			this.ext = ext;
		}
		
		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
		
	}
	
	private static ArrayList<FileFormat> openFiles = new ArrayList<FileFormat>();
	
	public long[] keys;
    
	public Hdf5StarReader(int frame, HashMap<Integer, Model> models, HashMap<Long, Particle> particles, String evo, String grav) throws FileOpeningException {
		
		HashMap<String, Dataset> result = new HashMap<String, Dataset>();
		
	    List<HObject> memberList = getRoot(evo).getMemberList();	    
	    traverse("evo", result, memberList);	
	    
	    memberList = getRoot(grav).getMemberList();
	    traverse("grav", result, memberList);
	    
	    Dataset keysSet = result.get("evo/particles/0000000001/keys");
	    int numParticles = (int) keysSet.getDims()[0];	    
	    	    
	    double[] luminosity, realRadius, x, y, z, vx = null, vy = null, vz = null;
	    
	    try {
	    	keys		= (long[]) keysSet.read();
	    	luminosity 	= (double[]) result.get("evo/particles/0000000001/attributes/luminosity").read();
	    	realRadius 	= (double[]) result.get("evo/particles/0000000001/attributes/radius").read();
//	    	visualRadius= (double[]) result.get("grav/particles/0000000001/attributes/radius").read();
	    	x 			= (double[]) result.get("grav/particles/0000000001/attributes/x").read();
	    	y 			= (double[]) result.get("grav/particles/0000000001/attributes/y").read();
	    	z 			= (double[]) result.get("grav/particles/0000000001/attributes/z").read();
	    	
	    	if (GLWindow.PREDICTION_ON) { 	    	
		    	vx 			= (double[]) result.get("grav/particles/0000000001/attributes/vx").read();
		    	vy 			= (double[]) result.get("grav/particles/0000000001/attributes/vy").read();
		    	vz 			= (double[]) result.get("grav/particles/0000000001/attributes/vz").read();
	    	}
		    
		    for (int i = 0; i< numParticles; i++) {
		    	boolean modelChange = false;
		    	
		    	Particle current = particles.get(keys[i]);
		    	
		    	if (realRadius[i] != current.getLastUniqueRadius(frame)) {
		    		current.radius.put(frame, realRadius[i]);
		    		modelChange = true;
		    	}
		    	
		    	Vec4 color =  Astrophysics.toColor(luminosity[i], realRadius[i]);
		    	if (!color.equals(current.getLastUniqueColor(frame))) {
		    		current.luminosity.put(frame, luminosity[i]);
		    		current.color.put(frame, color);
		    		modelChange = true;
		    	}
		    	
		    	if (modelChange) {
		    		Material material = new Material(color,color,color);
		    		
		    		Model sphere;
		    		int index = (int) Math.round((realRadius[i] / 10E9)/ 0.01);		    		
		    		if (models.get(index) == null) {
//		    			System.out.println(Astrophysics.starToScreenCoord(realRadius[i]));
		    			sphere = new Sphere(models.get(0).program, material, 2, index*0.01f + 0.01f, new Vec3());
		    		} else {
			    		sphere = models.get(index);
		    		}
			    	//Model sphere = new Sphere(program, material, 3, ((float)(visualRadius[i] / 10E13)), new Vec3());
			    	current.model.put(frame, sphere);
		    	}
		    	
		    	current.location.put(frame, Astrophysics.toScreenCoord(x[i], y[i], z[i]));
		    	
		    	if (GLWindow.PREDICTION_ON) {
		    		current.location.put(frame, Astrophysics.toScreenCoord(vx[i], vy[i], vz[i]));	    			    	
		    	}
		    }
		    //System.out.println("x: "+x[200] / Astrophysics.parsec+" y: "+y[200] / Astrophysics.parsec+" z: "+z[200] / Astrophysics.parsec);
	    	//System.out.println("vx: "+vx[200]+" vy: "+vy[200]+" vz: "+vz[200]);
	    } catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    for (Dataset d : result.values()) {
	    	d.clear();
	    	d.close(0);
	    }
	    		
	    closeFiles();
	}
	
	protected static int getNumFiles(String path, String namePostfix) {
		String[] ls = new File(path).list(new ExtFilter(namePostfix));
		
		return ls.length;
	}
	
	protected static Group getRoot(String filename) throws FileOpeningException {
		// retrieve an instance of H5File
	    FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);	
	    if (fileFormat == null) {
	        throw new FileOpeningException("Cannot find HDF5 FileFormat.");
	    }
	    
		// open the file with read and write access
	    FileFormat file = null;
		try {
			file = fileFormat.createInstance(filename, FileFormat.READ);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	    if (file == null) {
	    	throw new FileOpeningException("Failed to open file, file is null: "+filename);
	    }
	
	    // open the file and retrieve the file structure
	    try {
			file.open();
		} catch (Exception e) {
			throw new FileOpeningException("Failed to open file: "+filename);
		}
		
		openFiles.add(file);
				
	    return (Group)((javax.swing.tree.DefaultMutableTreeNode)file.getRootNode()).getUserObject();
	}
	
	protected static void traverse(String prefix, HashMap<String, Dataset> result, List<HObject> memberList) {
		for (HObject o : memberList) {	
	    	if (o instanceof Group) {
	    		traverse(prefix, result, ((Group) o).getMemberList());
	    	} else if (o instanceof Dataset) {
//	    		System.out.println(prefix+o.getFullName());
	    		result.put(prefix+o.getFullName(), (Dataset) o);
	    		((Dataset) o).init();
	    	} else {
	    		System.err.println("Unknown object type discovered: "+o.getFullName());
	    		System.exit(1);
	    	}
	    }
	}
	
	protected static void closeFiles() {
		for (FileFormat f : openFiles) {
			// close file resource
		    try {
				f.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		openFiles.clear();
	}
}
