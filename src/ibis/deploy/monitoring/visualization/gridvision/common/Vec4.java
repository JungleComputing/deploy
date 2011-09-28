package ibis.deploy.monitoring.visualization.gridvision.common;

import java.nio.FloatBuffer;

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

    public Vec4(float x, float y, float z, float h) {
        this.v[0] = x;
        this.v[1] = y;
        this.v[2] = z;
        this.v[3] = h;
    }

    public Vec4 neg() {
    	v[0] = -v[0];
    	v[1] = -v[1];
    	v[2] = -v[2];
    	v[3] = -v[3];
        return this;
    }

    public Vec4 add(Vec4 u) {
    	v[0] += u.v[0];
    	v[1] += u.v[1];
    	v[2] += u.v[2];
    	v[3] += u.v[3];
    	return this;
    }

	public Vec4 sub(Vec4 u) {
		v[0] -= u.v[0];
		v[1] -= u.v[1];
		v[2] -= u.v[2];
		v[3] -= u.v[3];
		return this;
	}

	public Vec4 mul(Number n) {
		float fn = n.floatValue();
		v[0] *= fn;
		v[1] *= fn;
		v[2] *= fn;
		v[3] *= fn;
		return this;
	}

	public Vec4 mul(Vec4 u) {
		v[0] *= u.v[0];
		v[1] *= u.v[1];
		v[2] *= u.v[2];
		v[3] *= u.v[3];
		return this;
	}

	public Vec4 div(Number n) {
		float f = n.floatValue();
    	if (f == 0f) return new Vec4();
		float fn = 1f / f;

		v[0] *= fn;
		v[1] *= fn;
		v[2] *= fn;
		v[3] *= fn;
		return this;
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
    
    public void printReadable() {		
		for (int i=0; i<4; i++) {
			System.out.print(v[i]+" ");
		}
		System.out.println();
		System.out.println();
	}
}
