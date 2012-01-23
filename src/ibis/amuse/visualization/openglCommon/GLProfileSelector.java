package ibis.amuse.visualization.openglCommon;

import javax.media.opengl.GLProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GLProfileSelector {
    final static Logger logger = LoggerFactory
            .getLogger(GLProfileSelector.class);

    public static final void printAvailable() {
        logger.info(GLProfile.glAvailabilityToString());
    }
}
