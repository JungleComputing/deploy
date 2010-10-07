package go.dac;

public class Worker extends Thread {

    private final Go go;

    private final Timer workingTimer;

    private final Timer returningTimer;

    private final Timer waitingTimer;

    private final Timer totalTimer;

    Worker(Go go, int index) {
        this.go = go;
        this.setDaemon(true);
        this.setName("Worker Thread " + index);
        this.workingTimer = go.getTimer("Worker " + index);
        this.returningTimer = go.getTimer("Returning " + index);
        this.waitingTimer = go.getTimer("Waiting " + index);
        this.totalTimer = go.getTimer("Total " + index);
    }

    public void run() {
        totalTimer.start();
        while (true) {
            waitingTimer.start();
            Job job = go.getJob();
            waitingTimer.stop();

            if (job == null) {
                // workQ empty, we are done
                totalTimer.stop();
                return;
            }

            workingTimer.start();
            Move result = job.result();
            workingTimer.stop();

            if (result == null) {
                go.addToWaitingQ(job, job.getChildJobs(go.getIbisIdentifier()));
            } else {
                go.returnResult(job, result, returningTimer);
            }
        }
    }
}
