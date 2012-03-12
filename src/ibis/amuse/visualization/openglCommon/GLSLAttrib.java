package ibis.amuse.visualization.openglCommon;

import java.nio.Buffer;

public class GLSLAttrib {
    public static int SIZE_FLOAT = 32;
    public static int SIZE_SHORT = 16;

    public Buffer buffer;
    public String name;
    public int bufferSize;
    public int vectorSize;

    public GLSLAttrib(Buffer buffer, String name, int bufferSize, int vectorSize) {
        this.buffer = buffer;
        this.name = name;
        this.bufferSize = bufferSize;
        this.vectorSize = vectorSize;
    }
}
