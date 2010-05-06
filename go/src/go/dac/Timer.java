/* $Id: Timer.java 9792 2008-11-12 09:07:35Z ceriel $ */

package go.dac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for measuring time.
 */
public class Timer implements java.io.Serializable {

    static final Logger logger = LoggerFactory.getLogger(Timer.class);

    private static final long serialVersionUID = 1L;
    
    private final String name;

    /**
     * Counts the number of start/stop sequences.
     */
    protected int count;

    protected long time = 0;

    protected long lastTime = 0;

    protected long maxTime = 0;

    protected long minTime = Long.MAX_VALUE;

    protected long t_start = 0;

    protected boolean started = false;

    /**
     * Constructs a <code>Timer</code>.
     */
    public Timer(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    /**
     * Returns the current time stamp in nanoseconds.
     * 
     * @return the current time stamp.
     */
    public long currentTimeNanos() {
        return System.nanoTime();
    }

    /**
     * Resets the timer.
     */
    public synchronized void reset() {
        time = 0;
        count = 0;
    }

    /**
     * Returns the total measured time in microseconds.
     * 
     * @return total measured time.
     */
    public synchronized double totalTimeVal() {

        long cur_time = 0;
        if (started) {
            cur_time = System.nanoTime() - t_start;
        }
        return (time + cur_time) / 1000.0;
    }

    /**
     * Returns the total measured time in microseconds, nicely formatted.
     * 
     * @return total measured time.
     */
    public synchronized String totalTime() {
        return format(totalTimeVal());
    }

    /**
     * Returns the average measured time in microseconds.
     * 
     * @return the average measured time.
     */
    public synchronized double averageTimeVal() {
        if (count > 0) {
            return time / (count * 1000.0);
        }
        return 0.0;
    }

    /**
     * Returns the average measured time in microseconds, nicely formatted.
     * 
     * @return the average measured time.
     */
    public synchronized String averageTime() {
        return format(averageTimeVal());
    }

    /**
     * Returns the last measured time in microseconds.
     * 
     * @return the last measured time.
     */
    public synchronized double lastTimeVal() {
        return lastTime / 1000.0;
    }

    /**
     * Returns the maximum measured time in microseconds.
     * 
     * @return the maximum measured time.
     */
    public synchronized double maxTimeVal() {
        return maxTime / 1000.0;
    }

    /**
     * Returns the minimum measured time in microseconds.
     * 
     * @return the minimum measured time.
     */
    public synchronized double minTimeVal() {
        return minTime / 1000.0;
    }

    /**
     * Returns the last measured time in microseconds, nicely formatted.
     * 
     * @return the last measured time.
     */
    public synchronized String lastTime() {
        return format(lastTimeVal());
    }

    /**
     * Returns the maximum measured time in microseconds, nicely formatted.
     * 
     * @return the maximum measured time.
     */
    public synchronized String maxTime() {
        return format(maxTimeVal());
    }

    /**
     * Returns the minimum measured time in microseconds, nicely formatted.
     * 
     * @return the minimum measured time.
     */
    public synchronized String minTime() {
        return format(minTimeVal());
    }

    /**
     * Returns the number of measurements.
     * 
     * @return the number of measurements.
     */
    public synchronized int nrTimes() {
        return count;
    }

    /**
     * Starts the timer. If the timer is already started, this is a no-op. The
     * next {@link #stop()}call will stop the timer and add the result to the
     * total.
     */
    public synchronized void start() {
        if (started) {
            logger.error("Timer started twice!", new Error(
                    "Timer started twice"));
        }
        started = true;
        t_start = System.nanoTime();
    }

    /**
     * Stops the timer. If the timer is not started, this is a no-op. The timer
     * is stopped, and the time between the last {@link #start()}and "now" is
     * added to the total.
     */
    public synchronized void stop() {
        if (!started) {
            logger.error("Timer stopped, but not started!", new Error(
                    "Timer stopped, but not started!"));
        }

        lastTime = System.nanoTime() - t_start;
        time += lastTime;
        if (lastTime > maxTime) {
            maxTime = lastTime;
        }
        if (lastTime < minTime) {
            minTime = lastTime;
        }
        ++count;
        started = false;
    }

    /**
     * Formats a time in microseconds
     * 
     * @param micros
     *            the time to be formatted.
     * @return the result of the format.
     */
    public static String format(double micros) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
        // nf.setMaximumIntegerDigits(3);
        // nf.setMinimumIntegerDigits(3);
        nf.setGroupingUsed(false);

        // special case for 0
        if (micros <= 0.0) {
            return "  0.000  s";
        }

        if (micros < 1.0) {
            double nanos = micros * 1000;
            if (nanos < 10) {
                return "  " + nf.format(nanos) + " ns";
            }
            if (nanos < 100) {
                return " " + nf.format(nanos) + " ns";
            }
            return nf.format(nanos) + " ns";
        } else if (micros < 1000.0) {
            if (micros < 10) {
                return "  " + nf.format(micros) + " us";
            }
            if (micros < 100) {
                return " " + nf.format(micros) + " us";
            }
            return nf.format(micros) + " us";
        } else if (micros < 1000000.0) {
            double millis = micros / 1000;
            if (millis < 10) {
                return "  " + nf.format(millis) + " ms";
            }
            if (millis < 100) {
                return " " + nf.format(millis) + " ms";
            }
            return nf.format(millis) + " ms";
        } else {
            double secs = micros / 1000000;
            if (secs < 10) {
                return "  " + nf.format(secs) + "  s";
            }
            if (secs < 100) {
                return " " + nf.format(secs) + "  s";
            }
            return nf.format(secs) + "  s";
        }
    }
}
