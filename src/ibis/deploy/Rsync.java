package ibis.deploy;

import java.io.File;
import java.util.List;

import org.gridlab.gat.engine.util.OutputForwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple rsync commandline wrapper
 * 
 * @author Niels Drost
 * 
 */
class Rsync {

    private static final Logger logger = LoggerFactory.getLogger(Rsync.class);

    private static Process createProcess(File src, File dst, String userName,
            String host) throws Exception {
        if (!dst.isAbsolute()) {
            throw new Exception("destination must be absolute, not: " + dst);
        }

        ProcessBuilder builder = new ProcessBuilder();

        List<String> command = builder.command();

        // command
        command.add("rsync");

        // print more output
        // command.add("--verbose");

        // recursive
        command.add("--recursive");

        // use compression
        command.add("--compress");

        // compare files using checksums, not last-modified time and size
        command.add("--checksum");

        // use ssh
        command.add("--rsh=ssh");

        // delete any file not at source
        command.add("--delete");

        // do nothing, only pretend
        // command.add("--dry-run");

        // source file
        command.add(src.getAbsolutePath());

        // destination file
        if (userName == null) {
            command.add(host + ":" + dst.getAbsolutePath());
        } else {
            command.add(userName + "@" + host + ":" + dst.getAbsolutePath());
        }

        if (logger.isDebugEnabled()) {
            String message = "Running rsync:";
            for (String string : command) {
                message += " " + string;
            }
            logger.debug(message);
        }

        return builder.start();
    }

    static synchronized void rsync(File src, File dst, String host,
            String userName) throws Exception {
        Process process = createProcess(src, dst, userName, host);

        OutputForwarder out = new OutputForwarder(process.getInputStream(),
                System.out);
        OutputForwarder err = new OutputForwarder(process.getErrorStream(),
                System.out);

        int exitValue = process.waitFor();

        out.waitUntilFinished();
        err.waitUntilFinished();

        if (exitValue != 0) {
            throw new Exception("error on running process! exit value was: "
                    + exitValue);
        }
    }

}
