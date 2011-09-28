package ibis.deploy.monitoring.visualization.gridvision.common;

import java.nio.FloatBuffer;

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
    	v[0] = -v[0];
    	v[1] = -v[1];
    	v[2] = -v[2];
        return this;
    }

    public Vec3 add(Vec3 u) {
    	v[0] += u.v[0];
    	v[1] += u.v[1];
    	v[2] += u.v[2];
    	return this;
    }

	public Vec3 sub(Vec3 u) {
		v[0] -= u.v[0];
		v[1] -= u.v[1];
		v[2] -= u.v[2];
		return this;
	}

	public Vec3 mul(Number n) {
		float fn = n.floatValue();
		v[0] *= fn;
		v[1] *= fn;
		v[2] *= fn;
		return this;
	}

	public Vec3 mul(Vec3 u) {
		v[0] *= u.v[0];
		v[1] *= u.v[1];
		v[2] *= u.v[2];
		return this;
	}

	public Vec3 div(Number n) {  
		float f = n.floatValue();
    	if (f == 0f) return new Vec3();
		float fn = 1f / f;

		v[0] *= fn;
		v[1] *= fn;
		v[2] *= fn;
		return this;
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
    
    public void printReadable() {		
		for (int i=0; i<3; i++) {
			System.out.print(v[i]+" ");
		}
		System.out.println();
		System.out.println();
	}
}
