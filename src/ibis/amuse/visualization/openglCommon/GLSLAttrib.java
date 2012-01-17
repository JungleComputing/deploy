package ibis.amuse.visualization.openglCommon;

import java.nio.FloatBuffer;

public class GLSLAttrib {	
	public FloatBuffer buffer;
	public String name;
	public int vectorSize;
	
	public GLSLAttrib(FloatBuffer buffer, String name, int vectorSize) {
		this.buffer = buffer;
		this.name = name;
		this.vectorSize = vectorSize;
	}
}
