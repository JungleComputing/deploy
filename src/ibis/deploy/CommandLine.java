package ibis.deploy;

import java.io.File;

public class CommandLine {

    public static void main(String[] arguments) throws Exception {

        Deploy deploy = new Deploy();

        File ibisLib = new File("/home/ndrost/workspace/ipl/lib");
        File log4j = new File("log4j.properties");

        Grid das3 = new Grid(new File("grids/das3.grid"));

        das3.save(new File("das3.grid.copy"));

        System.err.println(das3.toPrintString());

//        Cluster serverCluster = grid.getCluster("local");

//        deploy.initialize(serverCluster, ibisLib, log4j);

        deploy.end();
    }
}
