package ibis.deploy.gui.outputViz.common;

import java.nio.FloatBuffer;

public class Vec2 extends Vector {
	float v[] = new float[2];
	FloatBuffer buf = FloatBuffer.wrap(v);

    public Vec2() {
        this.v[0] = 0f;
        this.v[1] = 0f;
    }

    public Vec2(Vec2 v) {
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
    }

    public Vec2(float x, float y) {
        this.v[0] = x;
        this.v[1] = y;
    }

    public Vec2 neg() {
    	v[0] = -v[0];
    	v[1] = -v[1];
        return this;
    }

    public Vec2 add(Vec2 u) {
    	v[0] += u.v[0];
    	v[1] += u.v[1];
    	return this;
    }

	public Vec2 sub(Vec2 u) {
		v[0] -= u.v[0];
		v[1] -= u.v[1];
		return this;
	}

	public Vec2 mul(Number n) {
		float fn = n.floatValue();
		v[0] *= fn;
		v[1] *= fn;
		return this;
	}

	public Vec2 mul(Vec2 u) {
		v[0] *= u.v[0];
		v[1] *= u.v[1];
		return this;
	}

	public Vec2 div(Number n) {  
		float f = n.floatValue();
    	if (f == 0f) return new Vec2();
		float fn = 1f / f;

		v[0] *= fn;
		v[1] *= fn;
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
    
    public Vec2 clone() {
    	return new Vec2(this);
    }
    
    public String toString() {
    	String result = "";
		for (int i=0; i<3; i++) {
			result += (v[i]+" ");
		}
		
		return result;
	}
}
