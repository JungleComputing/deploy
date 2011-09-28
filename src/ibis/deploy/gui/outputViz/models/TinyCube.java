package ibis.deploy.gui.outputViz.models;

import ibis.deploy.gui.outputViz.common.*;
import ibis.deploy.gui.outputViz.models.base.*;
import ibis.deploy.gui.outputViz.shaders.Program;

public class TinyCube extends DualModel {
	static float SIZE = 0.025f;
	
	public TinyCube (Program program, Material material) {
		transparent = new Rectangle(program, material, SIZE, SIZE, SIZE, new Vec3(), true);
	}
}
