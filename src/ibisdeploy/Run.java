package ibisdeploy;

import java.io.File;
import java.util.ArrayList;

public class Run {
    private Grid grid;

    private Application app;

    private ArrayList<Job> job =
            new ArrayList<Job>();

    private String runFileName;
    
    public static Run loadRun(String filename) {
        System.err.print("loading run: " + filename);
        Run run = new Run();

        File f = new File(filename);
        run.runFileName = f.getName();
                
        Input in = new Input(filename);

        in.skipComments();
        String gridFile = in.readWord();
        in.readln();

        in.skipComments();
        String appFile = in.readWord();
        in.readln();

        int jobno = 0;
        while (!in.eof()) {
            Job res = new Job(jobno);
            jobno++;
            int subJobNo = 0;
            while (true) {
                in.skipComments();
                int machineCount = 0;
                int CPUsPerMachine = 0;
                String cluster = in.readWord();
                in.skipWhiteSpace();

                if (!in.eoln() && in.nextChar() != ',') {
                    machineCount = in.readInt();
                    in.skipWhiteSpace();
                }

                if (!in.eoln() && in.nextChar() != ',') {
                    CPUsPerMachine = in.readInt();
                }

                if(machineCount > 0) {
                    SubJob j = new SubJob(cluster, machineCount, CPUsPerMachine, subJobNo);
                    subJobNo++;
                    res.addSubJob(j);
                }
                
                in.skipWhiteSpace();
                if (in.eoln())
                    break;

                char ch = in.readChar();
                if (ch != ',') {
                    throw new Error("parse error, \",\" expected");
                }
                in.skipWhiteSpace();
            }
            in.readln();

            run.job.add(res);
        }

        System.err.println(" DONE");

        run.grid = Grid.loadGrid(gridFile);
        run.app = Application.loadApplication(appFile);

        System.err.println("run: " + run);
        
        return run;
    }

    public Application getApp() {
        return app;
    }

    public Grid getGrid() {
        return grid;
    }

    public ArrayList<Job> getRequestedResources() {
        return job;
    }

    public String toString() {
        String res = "Run: " + app + "\n" + grid + "\n";

        res += "requests:\n";
        for (int i = 0; i < job.size(); i++) {
            Job r = job.get(i);
            res += r;
        }

        return res;
    }

    public String getRunFileName() {
        return runFileName;
    }
}
