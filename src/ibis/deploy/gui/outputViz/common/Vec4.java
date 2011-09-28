package ibis.deploy.gui.outputViz.common;


import java.nio.FloatBuffer;
import java.util.List;

public class Vec4 extends Vector {
	float v[] = new float[4];
	FloatBuffer buf = FloatBuffer.wrap(v);

    public Vec4() {
        this.v[0] = 0f;
        this.v[1] = 0f;
        this.v[2] = 0f;
        this.v[3] = 0f;
    }

    public Vec4(Vec4 v) {
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
        this.v[2] = v.v[2];
        this.v[3] = v.v[3];
    }
    
    public Vec4(Vec3 v, float v3) {
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
        this.v[2] = v.v[2];
        this.v[3] = v3;
    }

    public Vec4(float x, float y, float z, float h) {
        this.v[0] = x;
        this.v[1] = y;
        this.v[2] = z;
        this.v[3] = h;
    }

    public Vec4 neg() {
    	Vec4 result = new Vec4();
    	result.v[0] = -v[0];
    	result.v[1] = -v[1];
    	result.v[2] = -v[2];
    	result.v[3] = -v[3];
    	return result;
    }

    public Vec4 add(Vec4 u) {
    	Vec4 result = new Vec4();
    	result.v[0] = v[0] + u.v[0];
    	result.v[1] = v[1] + u.v[1];
    	result.v[2] = v[2] + u.v[2];
    	result.v[3] = v[3] + u.v[3];
    	return result;
    }

	public Vec4 sub(Vec4 u) {
		Vec4 result = new Vec4();
    	result.v[0] = v[0] - u.v[0];
    	result.v[1] = v[1] - u.v[1];
    	result.v[2] = v[2] - u.v[2];
    	result.v[3] = v[3] - u.v[3];
    	return result;
	}

	public Vec4 mul(Number n) {
		float fn = n.floatValue();
		Vec4 result = new Vec4();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	result.v[2] = v[2] *fn;
    	result.v[3] = v[3] *fn;
    	return result;
	}

	public Vec4 mul(Vec4 u) {
		Vec4 result = new Vec4();
    	result.v[0] = v[0] * u.v[0];
    	result.v[1] = v[1] * u.v[1];
    	result.v[2] = v[2] * u.v[2];
    	result.v[3] = v[3] * u.v[3];
    	return result;
	}

	public Vec4 div(Number n) {  
		float f = n.floatValue();
    	if (f == 0f) return new Vec4();
		float fn = 1f / f;

		Vec4 result = new Vec4();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	result.v[2] = v[2] *fn;
    	result.v[3] = v[3] *fn;
    	return result;
	}
    
    public float[] asArray() {
    	return v;
    }
    
    public FloatBuffer asBuffer() {
    	buf.rewind();
    	return buf;
    }
    
    public float get(int i) {
        return v[i];
    }
    
    public void set(int i, float u) {  
    	v[i] = u;
    }
    
    public Vec4 clone() {
    	return new Vec4(this);
    }
    
    public static FloatBuffer toBuffer(Vec4[] array) {
		FloatBuffer result = FloatBuffer.allocate(array.length*4);
		
		for (int i=0; i < array.length; i++) {
			result.put(array[i].asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    public static FloatBuffer toBuffer(List<Vec4> list) {
		FloatBuffer result = FloatBuffer.allocate(list.size()*4);
		
		for (int i=0; i < list.size(); i++) {
			result.put(list.get(i).asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    public String toString() {
    	String result = "";
		for (int i=0; i<3; i++) {
			result += (v[i]+" ");
		}
		
		return result;
	}
    
    @Override
	public int hashCode() {
		int hashCode = (int) (v[0] * 6833 + v[1] *7207 + v[2] * 7919 + v[3] * 3);
		return hashCode;
	}
    
    @Override
	public boolean equals(Object thatObject) {
		if (this == thatObject)
			return true;
		if (!(thatObject instanceof Vec4))
			return false;

		// cast to native object is now safe
		Vec4 that = (Vec4) thatObject;

		// now a proper field-by-field evaluation can be made
		return (v[0] == that.v[0] && v[1] == that.v[1] && v[2] == that.v[2] && v[3] == that.v[3]);
	}
}
