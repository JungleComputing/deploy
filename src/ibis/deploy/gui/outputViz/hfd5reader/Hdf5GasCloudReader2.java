package ibis.deploy.gui.outputViz.hfd5reader;

import ibis.deploy.gui.outputViz.GLWindow;
import ibis.deploy.gui.outputViz.common.Texture3D;
import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.exceptions.FileOpeningException;

import java.nio.ByteBuffer;
import java.util.*;

import ncsa.hdf.object.*;

public class Hdf5GasCloudReader2 {	
	static final int TEX_SIZE = 256;
	private static ArrayList<FileFormat> openFiles = new ArrayList<FileFormat>();
	
	//public long[] keys;
	
	Texture3D tex3d;
    
	public Hdf5GasCloudReader2(int frame, String gasName) throws FileOpeningException {
		HashMap<String, Dataset> result = new HashMap<String, Dataset>();
		
	    List<HObject> memberList = getRoot(gasName).getMemberList();	    
	    traverse("gas", result, memberList);	
	    
	    Dataset keysSet = result.get("gas/particles/0000000001/keys");
	    int numParticles = (int) keysSet.getDims()[0];	    
	    	    
	    double[] x, y, z;
	    
	    try {
	    	//keys		= (long[]) keysSet.read();
	    	
	    	x 			= (double[]) result.get("gas/particles/0000000001/attributes/x").read();
	    	y 			= (double[]) result.get("gas/particles/0000000001/attributes/y").read();
	    	z 			= (double[]) result.get("gas/particles/0000000001/attributes/z").read();
		    
	    	byte[] imageBytes = new byte[TEX_SIZE * TEX_SIZE * TEX_SIZE];   		
	    	
		    for (int i = 0; i< numParticles; i++) {		
		    	Vec3 coords = Astrophysics.toScreenCoord(x[i], y[i], z[i]);
		    	float px = coords.get(0);
		    	float py = coords.get(1);
		    	float pz = coords.get(2);
		    	
		    	if (!(px < -GLWindow.GAS_EDGES || px > GLWindow.GAS_EDGES ||
		    		  py < -GLWindow.GAS_EDGES || py > GLWindow.GAS_EDGES ||
		    		  pz < -GLWindow.GAS_EDGES || pz > GLWindow.GAS_EDGES)) {
		    		int ix = (int)(((px+GLWindow.GAS_EDGES)/(GLWindow.GAS_EDGES*2)) * TEX_SIZE);
		    		int iy = (int)(((py+GLWindow.GAS_EDGES)/(GLWindow.GAS_EDGES*2)) * TEX_SIZE);
		    		int iz = (int)(((pz+GLWindow.GAS_EDGES)/(GLWindow.GAS_EDGES*2)) * TEX_SIZE);
		    		
//		    		System.out.println("px: "+px+" py: "+py+" pz: "+pz);
		    		
		    		imageBytes[ix + iy * TEX_SIZE + iz * TEX_SIZE*TEX_SIZE] += 1;
		    	}		    	
		    }
		    
		    ByteBuffer image = ByteBuffer.wrap(imageBytes);
		    tex3d = new Texture3D(image);
		    		    
	    } catch (OutOfMemoryError e) {
	    	System.err.println("Out of memory.");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("General error?");
			e.printStackTrace();
		}	
	    
	    for (Dataset d : result.values()) {
	    	d.clear();
	    	d.close(0);
	    }
	    		
	    closeFiles();
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
	    		//System.out.println(prefix+o.getFullName());
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
				System.err.println("Error closing file: "+f.getPath());
				e.printStackTrace();
			}
		}
		
		openFiles.clear();
	}
}
