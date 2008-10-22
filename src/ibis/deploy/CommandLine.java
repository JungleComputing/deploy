package ibis.deploy;

import java.io.File;

public class CommandLine {

	
	public static void main(String[] arguments) throws Exception {
		
		Deploy deploy = new Deploy();
		
		File ibisLib = new File("/home/ndrost/workspace/ipl/lib");
		File log4j = new File("log4j.properties");
	    
	    Grid grid = new Grid(new File("grids/local.new.properties"));
	    
	    grid.save(new File("grid.dummy.file"));

	    Grid das3 = new Grid(new File("grids/das3.new.properties"));
	    
	    das3.save(new File("das3.grid"));
	    
	    System.err.println(grid.toPrintString());
	    
		Cluster serverCluster = grid.getCluster("local");
		
		deploy.initialize(serverCluster, ibisLib, log4j);

		
		
		
		
		deploy.end();
	}
}
