package ibis.amuse.visualization.openglCommon;

import ibis.amuse.visualization.openglCommon.math.Vec4;

public class Material {
	public Vec4 ambient, diffuse, specular;
	
	public Material() {
		this.ambient = new Vec4(0,0,0,0);
		this.diffuse = new Vec4(0,0,0,0);
		this.specular = new Vec4(0,0,0,0);
	}
	
	public Material(Vec4 ambient, Vec4 diffuse, Vec4 specular) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}
	
	public static Material random() {
		Vec4 ambient = new Vec4((float)Math.random(),(float)Math.random(),(float)Math.random(),1f);
		Vec4 diffuse = new Vec4((float)Math.random(),(float)Math.random(),(float)Math.random(),1f);
		Vec4 specular = new Vec4((float)Math.random(),(float)Math.random(),(float)Math.random(),1f);
		return new Material(ambient, diffuse, specular);
	}
	
	public void setColor(Vec4 newColor) {
		ambient = newColor;
		diffuse = newColor;
		specular = newColor;
	}
	
	public void setTransparency(float newTransparency) {
		ambient.set(3, newTransparency);
		diffuse.set(3, newTransparency);
		specular.set(3, newTransparency);
	}
}
