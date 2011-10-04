package ibis.deploy.gui.outputViz.amuse;

import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.math.Vec4;
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
	public static ArrayList<Model> additionalModels = new ArrayList<Model>();
	
	public long[] keys;
    
	public Hdf5StarReader(HashMap<Integer, Model> models, HashMap<Long, Star> particles, String evo, String grav) throws FileOpeningException {
		
		HashMap<String, Dataset> result = new HashMap<String, Dataset>();
		
	    List<HObject> memberList = getRoot(evo).getMemberList();	    
	    traverse("evo", result, memberList);	
	    
	    memberList = getRoot(grav).getMemberList();
	    traverse("grav", result, memberList);
	    
	    Dataset keysSet = result.get("evo/particles/0000000001/keys");
	    int numParticles = (int) keysSet.getDims()[0];	    
	    	    
	    double[] luminosity, realRadius, x, y, z;
	    
	    try {
	    	keys		= (long[]) keysSet.read();
	    	luminosity 	= (double[]) result.get("evo/particles/0000000001/attributes/luminosity").read();
	    	realRadius 	= (double[]) result.get("evo/particles/0000000001/attributes/radius").read();
	    	
	    	x 			= (double[]) result.get("grav/particles/0000000001/attributes/x").read();
	    	y 			= (double[]) result.get("grav/particles/0000000001/attributes/y").read();
	    	z 			= (double[]) result.get("grav/particles/0000000001/attributes/z").read();
	    			    
		    for (int i = 0; i< numParticles; i++) {		    	
		    	Star current = particles.get(keys[i]);		    	
		    	current.radius = realRadius[i];
		    	
		    	Vec4 color =  Astrophysics.toColor(luminosity[i], realRadius[i]);
		    	current.luminosity = luminosity[i];
		    	current.color = color;
		    	
		    	Material material = new Material(color,color,color);
		    	
		    	Model sphere;
		    	int index = (int) Math.round(Astrophysics.starToScreenRadius(realRadius[i])/ (float) Astrophysics.STAR_RADIUS_FACTOR);
	    		if (!models.containsKey(index)) {
	    			sphere = new Sphere(models.get(0).program, material, 3, index, new Vec3());
	    			models.put(index, sphere);
	    		} else {		    	
		    		sphere = models.get(index);
	    		}

		    	current.model = sphere;
		    	
		    	current.location = Astrophysics.locationToScreenCoord(x[i], y[i], z[i]);		    			    	
		    }
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
