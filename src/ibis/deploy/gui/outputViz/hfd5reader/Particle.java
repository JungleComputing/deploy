package ibis.deploy.gui.outputViz.hfd5reader;

import java.util.HashMap;

import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.Vec4;
import ibis.deploy.gui.outputViz.models.Model;

public class Particle {
	public HashMap<Integer, Double> radius;
	public HashMap<Integer, Vec4> color;
	public HashMap<Integer, Double> luminosity;
	public HashMap<Integer, Model> model;
	
	public HashMap<Integer, Vec3> location;
	public HashMap<Integer, Vec3> direction;
	
	public Particle() {
		radius = new HashMap<Integer, Double>();
		color = new HashMap<Integer, Vec4>();
		luminosity = new HashMap<Integer, Double>();
		model = new HashMap<Integer, Model>();
		
		location = new HashMap<Integer, Vec3>();
		direction = new HashMap<Integer, Vec3>();
	}
	
	public double getLastUniqueRadius(int currentFrame) {
		Double result;
		
		if (radius.size() == 0) {
			result = 0.0;
		} else if (radius.size() == 1) {
			result = radius.get(0);
		} else {
			result = null;
			int index = currentFrame;
			while (result == null) {
				result = radius.get(index);
				index--;
			}
		}		
		
		return result;
	}
	
	public Vec4 getLastUniqueColor(int currentFrame) {
		Vec4 result;
		
		if (color.size() == 0) {
			result = new Vec4();
		} else if (color.size() == 1) {
			result = color.get(0);
		} else {
			result = null;
			int index = currentFrame;
			while (result == null) {
				result = color.get(index);
				index--;
			}
		}		
		
		return result;
	}
	
	public double getLastUniqueLuminosity(int currentFrame) {
		Double result;
		
		if (luminosity.size() == 0) {
			result = 0.0;
		} else if (luminosity.size() == 1) {
			result = luminosity.get(0);
		} else {
			result = 0.0;
			int index = currentFrame;
			while (result == null) {
				result = luminosity.get(index);
				index--;
			}
		}		
		
		return result;
	}
	
	public Model getModel(int currentFrame) {
		Model result = null;
		
		if (model.size() == 0) {
			System.out.println("error in particle model code.");
			System.exit(1);
		} else if (model.size() == 1) {
			result = model.get(0);
		} else {
			result = null;
			int index = currentFrame;
			while (result == null) {
				result = model.get(index);
				index--;
			}
		}
		
		return result;
	}
}
