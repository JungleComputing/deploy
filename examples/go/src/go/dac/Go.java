package go.dac;

import go.dac.comm.Communication;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisIdentifier;
import ibis.util.ThreadPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Go extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(Go.class);

    private final Communication communication;

    private final IbisIdentifier myIdentifier;

    private final boolean isMaster;

    private final LinkedList<Job> jobQ;

    private final ArrayList<Job> priorityJobQ;

    private final ArrayList<Job> waitingJobQ;

    private final Map<IbisIdentifier, ArrayList<Job>> stolenJobs;

    private boolean terminated;

    private boolean ended;

    // final result, only at master
    private Move result;

    private final Map<String, Timer> timers;

    private int waitingWorkers;

    public Go() throws IbisCreationFailedException, IOException {
        jobQ = new LinkedList<Job>();
        waitingJobQ = new ArrayList<Job>();
        priorityJobQ = new ArrayList<Job>();
        stolenJobs = new HashMap<IbisIdentifier, ArrayList<Job>>();
        timers = new TreeMap<String, Timer>();
        result = null;
        terminated = false;
        ended = false;

        getTimer("Total").start();

        communication = new Communication(this);

        myIdentifier = communication.getIdentifier();
        isMaster = communication.isMaster();

        // start jobQ size printer thread
        Formatter format = new Formatter();
        format.format("GO @ %-10s", myIdentifier.name());
        ThreadPool.createNew(this, format.toString());
    }

    public synchronized Timer getTimer(String name) {
        Timer result = timers.get(name);

        if (result == null) {
            result = new Timer(name);
            timers.put(name, result);
        }

        return result;
    }

    public void printStatistics() {
        Timer[] timers;

        double totalTime = 0;
        int totalWorkers = 0;
        double totalWorkerTime = 0;

        synchronized (this) {
            timers = this.timers.values().toArray(new Timer[0]);
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);

        out
                .format("--------------------------------------------------------------------------------\n");
        out.format("Statistics of %s\n", myIdentifier);
        for (Timer timer : timers) {
            if (timer.nrTimes() == 0) {
                out.format("%-12s    ---\n", timer.getName());

            } else {
                out.format("%-12s count:%5d, min:%s,"
                        + " avg:%s, max:%s, total:%s\n", timer.getName(), timer
                        .nrTimes(), timer.minTime(), timer.averageTime(), timer
                        .maxTime(), timer.totalTime());

                if (timer.getName().startsWith("Worker ")) {
                    totalWorkers++;
                    totalWorkerTime += timer.totalTimeVal();
                } else if (timer.getName().equals("Total")) {
                    totalTime = timer.totalTimeVal();
                }
            }
        }

        if (totalWorkers > 0 && totalTime > 0) {
            double usefulTime = (totalWorkerTime / (totalTime * totalWorkers)) * 100;
            out.format("Average useful time for %d workers: %.2f %% \n",
                totalWorkers, usefulTime);
        }

        communication.printStatistics(out);
        out
                .format("--------------------------------------------------------------------------------\n");

        out.flush();
        out.close();
        byte[] byteArray = bytes.toByteArray();
        System.err.write(byteArray, 0, byteArray.length);
        System.err.flush();
    }

    synchronized Move getResult() {
        return result;
    }

    public void end() {
        synchronized (this) {
            terminated = true;
            if (ended) {
                return;
            }
            ended = true;
            notifyAll();
        }
        communication.end();

        getTimer("Total").stop();
    }

    public boolean isMaster() {
        return isMaster;
    }

    public IbisIdentifier getIbisIdentifier() {
        return myIdentifier;
    }

    public synchronized Job steal(IbisIdentifier thief) {
        if (jobQ.isEmpty()) {
            return null;
        }
        // get biggest job which was made by us.
        Job result = null;
        for (int i = 0; i < jobQ.size(); i++) {
            Job job = jobQ.get(i);

            if (job == null) {
                throw new Error("Eep! Job in jobQ == null");
            }

            // only let our own jobs get stolen, don't allow the "root" job to
            // get stolen
            if (job.getSource().equals(myIdentifier)
                    && job.getParentID() != null) {
                result = job;
                jobQ.remove(i);
                break;
            }
        }

        if (result == null) {
            // sorry, no jobs
            return null;
        }

        ArrayList<Job> kerfstok = stolenJobs.get(thief);

        if (kerfstok == null) {
            kerfstok = new ArrayList<Job>();
            stolenJobs.put(thief, kerfstok);
        }

        kerfstok.add(result);

        return result;
    }

    // this job was stolen, but sending it to the peer failed. So, we add it to
    // the q again
    public synchronized void unsteal(IbisIdentifier thief, Job job) {
        ArrayList<Job> kerfstok = stolenJobs.get(thief);

        if (kerfstok == null) {
            logger.error("Eep! got stolen job back from non-thief node",
                new Exception());
            return;
        }

        boolean found = false;
        for (int i = 0; i < kerfstok.size(); i++) {
            if (kerfstok.get(i).getID().equals(job.getID())) {
                kerfstok.remove(i);
                found = true;
            }
        }

        if (!found) {
            logger.error("Eep! un-stolen job " + job
                    + " , but not stolen by given node: " + thief,
                new Exception());
            return;
        }

        if (kerfstok.isEmpty()) {
            stolenJobs.remove(thief);
        }

        jobQ.add(0, job);
    }

    public synchronized void returnStolenJob(IbisIdentifier thief, UUID jobID,
            UUID parentID, Move result) {
        ArrayList<Job> kerfstok = stolenJobs.get(thief);

        if (kerfstok == null) {
            logger.error("Eep! got stolen job back from non-thief node",
                new Exception());
        }

        boolean found = false;
        for (int i = 0; i < kerfstok.size(); i++) {
            if (kerfstok.get(i).getID().equals(jobID)) {
                kerfstok.remove(i);
                found = true;
            }
        }

        if (kerfstok.isEmpty()) {
            stolenJobs.remove(thief);
        }

        if (!found) {
            logger.error(
                "Eep! got stolen job back, but not stolen from given node: "
                        + thief, new Exception());
        }

        returnLocalResult(jobID, parentID, result);
    }

    public synchronized void addToJobQ(Job... jobs) {
        if (jobQ.size() == 0) {
            notifyAll();
        }
        for (Job job : jobs) {
            if (job == null) {
                throw new Error("adding null job to Q");
            }
            logger.debug("adding " + job + " to jobQ");
            jobQ.add(job);
        }
    }

    public synchronized void addToPriorityJobQ(Job... jobs) {
        if (priorityJobQ.size() == 0) {
            notifyAll();
        }
        for (Job job : jobs) {
            if (job == null) {
                throw new Error("adding null job to priority Q");
            }
            logger.debug("adding " + job + " to priority jobQ");
            priorityJobQ.add(job);
        }
    }

    public synchronized void addToWaitingQ(Job job, Job... children) {
        logger.debug("adding " + job + " to waitingQ");
        waitingJobQ.add(job);

        addToJobQ(children);
    }

    private synchronized void returnLocalResult(UUID jobID, UUID parentID,
            Move result) {
        if (parentID == null) {
            // this is the root job
            if (!isMaster) {
                logger
                        .error("Got final result at non-master!",
                            new Exception());
            }
            this.result = result;
            // we're done
            this.terminated = true;
            // wake up main
            notifyAll();
        } else {
            int matches = 0;
            for (int i = 0; i < waitingJobQ.size(); i++) {
                Job waiting = waitingJobQ.get(i);
                if (parentID.equals(waiting.getID())) {
                    matches++;
                    if (waiting.addResult(jobID, result)) {
                        waitingJobQ.remove(i);
                        addToPriorityJobQ(waiting);
                    }
                }
            }
            if (matches != 1) {
                logger.warn("returning result to waiting job matched to "
                        + matches + " waiting jobs");
            }
        }
    }

    public void returnResult(Job job, Move result, Timer timer) {
        if (job.getSource().equals(getIbisIdentifier())) {
            returnLocalResult(job.getID(), job.getParentID(), result);
        } else {
            timer.start();
            communication.returnResult(job, result);
            timer.stop();
        }
    }

    private synchronized Job getLocalJob() {
        if (terminated) {
            return null;
        }
        if (!priorityJobQ.isEmpty()) {
            return priorityJobQ.remove(0);
        }
        if (!jobQ.isEmpty()) {
            return jobQ.removeLast();
        }

        // no jobs
        return null;
    }

    private synchronized void workerWait() {
        waitingWorkers += 1;
        // wait a bit
        try {
            wait(1000);
        } catch (InterruptedException e) {
            // IGNORE
        }
        waitingWorkers -= 1;
    }

    public Job getJob() {
        Job result = null;

        while (!hasTerminated()) {
            result = getLocalJob();

            if (result != null) {
                return result;
            }

            // wake up stealers (if needed)
            communication.startStealing();

            workerWait();
        }

        // signal we're done
        return null;
    }

    /**
     * clear all jobs made by the given ibis from all lists.
     */
    public synchronized void clearJobsFrom(IbisIdentifier ibis) {
        // TODO: implementing orphan support would help efficiency :)

        for (Job job : jobQ.toArray(new Job[0])) {
            if (job.dependsOn(ibis)) {
                logger.info("removing job by " + ibis + " from normal Q: "
                        + job);
                jobQ.remove(job);
            }
        }

        for (Job job : waitingJobQ.toArray(new Job[0])) {
            if (job.dependsOn(ibis)) {
                logger.info("removing job by " + ibis + " from waiting Q: "
                        + job);
                waitingJobQ.remove(job);
            }
        }

        for (Job job : priorityJobQ.toArray(new Job[0])) {
            if (job.dependsOn(ibis)) {
                logger
                        .info("removing job by " + ibis + " from ready Q: "
                                + job);
                priorityJobQ.remove(job);
            }
        }

        ArrayList<Job> stolenJobList = stolenJobs.remove(ibis);

        if (stolenJobList != null) {
            for (Job job : stolenJobList) {
                // add to head of Q (these are old, so big, jobs)
                logger.info("re-inserting job which was stolen by " + ibis
                        + " into Q: " + job);
                jobQ.add(0, job);
            }
        }

    }

    public void run() {
        while (!hasTerminated()) {
            synchronized (this) {
                if (isMaster) {
                    logger.info(new Formatter().format(
                           "MASTER Job Q size now: %3d, priority jobs:"
                           + " %2d, waiting jobs: %2d, waiting workers: %2d",
                           jobQ.size(), priorityJobQ.size(),
                           waitingJobQ.size(), waitingWorkers).toString());
                } else {
                    logger.info(new Formatter().format(
                        "WORKER Job Q size now: %3d, priority jobs:"
                        + " %2d, waiting jobs: %2d, waiting workers: %2d",
                        jobQ.size(), priorityJobQ.size(),
                        waitingJobQ.size(), waitingWorkers).toString());
                }

                if (logger.isDebugEnabled()) {
                    for (Map.Entry<IbisIdentifier, ArrayList<Job>> entry : stolenJobs
                            .entrySet()) {
                        logger.debug(entry.getKey() + " has "
                                + entry.getValue().size() + " jobs, I am "
                                + myIdentifier);
                    }
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // IGNORE
            }
        }

    }

    public synchronized boolean hasTerminated() {
        return terminated;
    }

    public synchronized void terminate() {
        terminated = true;
        notifyAll();
    }

    public synchronized boolean workersWaiting() {
        return waitingWorkers > 0;
    }
}
