package ibis.deploy.util;

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
public class Rsync {

    private static final Logger logger = LoggerFactory.getLogger(Rsync.class);

    private static Process createProcess(File src, File dst, String userName,
            String host, String keyFile) throws Exception {
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
        if (userName == null || keyFile == null) {
            command.add("--rsh=ssh -o StrictHostKeyChecking=no");
        } else {
            command.add("--rsh=ssh -o StrictHostKeyChecking=no -i " + keyFile);
        }

        // delete any file not at source
        command.add("--delete");

        // do nothing, only pretend
        // command.add("--dry-run");

        // source file
        command.add(src.getAbsolutePath());

        // destination file
        if (userName == null) {
            command.add(host + ":" + dst.getPath());
        } else {
            command.add(userName + "@" + host + ":" + dst.getPath());
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

    public static void rsync(File src, File dst, String host,
            String userName, String keyFile) throws Exception {
        Process process = createProcess(src, dst, userName, host, keyFile);

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
