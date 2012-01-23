package ibis.amuse.visualization.openglCommon;

import javax.media.opengl.GLProfile;

public class GLProfileSelector {
    public static final void printAvailable() {
        System.out.println(GLProfile.glAvailabilityToString());
    }
}
