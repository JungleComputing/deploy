package ibis.deploy.gui.outputViz.common;

public class Point4 extends Vec4{
	public Point4() {
		super();
	}
	
	public Point4(Vec4 v) {
		super(v);
	}
	
	public Point4(float x, float y, float z, float w) {
		super(x, y, z, w);
	}

	public Point4(Vec3 vec, float w) {
		super(vec, w);
	}
}
