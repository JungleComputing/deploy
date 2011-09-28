package ibis.deploy.gui.outputViz.common;

import java.nio.FloatBuffer;
import java.util.List;

public class Vec3 extends Vector {
	float v[] = new float[3];
	FloatBuffer buf = FloatBuffer.wrap(v);

    public Vec3() {
        this.v[0] = 0f;
        this.v[1] = 0f;
        this.v[2] = 0f;
    }

    public Vec3(Vec3 v) {
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
        this.v[2] = v.v[2];
    }

    public Vec3(float x, float y, float z) {
        this.v[0] = x;
        this.v[1] = y;
        this.v[2] = z;
    }

    public Vec3 neg() {
    	Vec3 result = new Vec3();
    	result.v[0] = -v[0];
    	result.v[1] = -v[1];
    	result.v[2] = -v[2];
    	return result;
    }

    public Vec3 add(Vec3 u) {
    	Vec3 result = new Vec3();
    	result.v[0] = v[0] + u.v[0];
    	result.v[1] = v[1] + u.v[1];
    	result.v[2] = v[2] + u.v[2];
    	return result;
    }

	public Vec3 sub(Vec3 u) {
		Vec3 result = new Vec3();
    	result.v[0] = v[0] - u.v[0];
    	result.v[1] = v[1] - u.v[1];
    	result.v[2] = v[2] - u.v[2];
    	return result;
	}

	public Vec3 mul(Number n) {
		float fn = n.floatValue();
		Vec3 result = new Vec3();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	result.v[2] = v[2] *fn;
    	return result;
	}

	public Vec3 mul(Vec3 u) {
		Vec3 result = new Vec3();
    	result.v[0] = v[0] * u.v[0];
    	result.v[1] = v[1] * u.v[1];
    	result.v[2] = v[2] * u.v[2];
    	return result;
	}

	public Vec3 div(Number n) {  
		float f = n.floatValue();
    	if (f == 0f) return new Vec3();
		float fn = 1f / f;

		Vec3 result = new Vec3();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	result.v[2] = v[2] *fn;
    	return result;
	}
    
    public float[] asArray() {
    	return v;
    }
    
    public FloatBuffer asBuffer() {
    	return buf;
    }
    
    public float get(int i) {
        return v[i];
    }
    
    public void set(int i, float u) {  
    	v[i] = u;
    }
    
    public Vec3 clone() {
    	return new Vec3(this);
    }
    
    public String toString() {
    	String result = "";
		for (int i=0; i<3; i++) {
			result += (v[i]+" ");
		}
		
		return result;
	}

	public static FloatBuffer toBuffer(Vec3[] array) {
		FloatBuffer result = FloatBuffer.allocate(array.length*3);
		
		for (int i=0; i < array.length; i++) {
			result.put(array[i].asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    public static FloatBuffer toBuffer(List<Vec3> list) {
		FloatBuffer result = FloatBuffer.allocate(list.size()*3);
		
		for (int i=0; i < list.size(); i++) {
			result.put(list.get(i).asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    public static Vec3 random(Vec3 mins, Vec3 maxs) {
    	float x = (float) (mins.get(0)+Math.random()*(maxs.get(0)-mins.get(0)));
    	float y = (float) (mins.get(1)+Math.random()*(maxs.get(1)-mins.get(1)));
    	float z = (float) (mins.get(2)+Math.random()*(maxs.get(2)-mins.get(2)));
    	
    	return new Vec3(x,y,z);
    }
    
    @Override
	public int hashCode() {
		int hashCode = (int) (v[0] * 6833 + v[1] *7207 + v[2] * 7919);
		return hashCode;
	}
    
    @Override
	public boolean equals(Object thatObject) {
		if (this == thatObject)
			return true;
		if (!(thatObject instanceof Vec3))
			return false;

		// cast to native object is now safe
		Vec3 that = (Vec3) thatObject;

		// now a proper field-by-field evaluation can be made
		return (v[0] == that.v[0] && v[1] == that.v[1] && v[2] == that.v[2]);
	}
}
