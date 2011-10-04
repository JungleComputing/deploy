package ibis.deploy.gui.outputViz.common.math;

public class Vec4 extends Vector {
    /**
     * Creates a new vector, initialized to 0.
     */
    public Vec4() {
    	super(4);
        this.v[0] = 0f;
        this.v[1] = 0f;
        this.v[2] = 0f;
        this.v[3] = 0f;
    }
    
    /**
     * Creates a new vector by copying the given vector.     
     * @param v
     * 		The vector to be copied.
     */
    public Vec4(Vec4 v) {
    	super(4);
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
        this.v[2] = v.v[2];
        this.v[3] = v.v[3];
    }
    
    /**
     * Creates a new vector by copying the given vector, supplemented by the scalar.     
     * @param v
     * 		The vector to be copied.
     * @param v3
     * 		The additional value to be put into the fourth index.
     */
    public Vec4(Vec3 v, float v3) {
    	super(4);
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
        this.v[2] = v.v[2];
        this.v[3] = v3;
    }
    
    /**
     * Creates a new vector with the given values.     
     * @param x
     * 		The value to be put in the first position.
     * @param y
     * 		The value to be put in the second position.
     * @param z
     * 		The value to be put in the third position.
     * @param w
     * 		The value to be put in the fourth position.
     */
    public Vec4(float x, float y, float z, float w) {
    	super(4);
        this.v[0] = x;
        this.v[1] = y;
        this.v[2] = z;
        this.v[3] = w;
    }

    
    /**
     * Gives the negated vector of this vector. 
     * @return
     * 		The new negated vector.
     */
    public Vec4 neg() {
    	Vec4 result = new Vec4();
    	result.v[0] = -v[0];
    	result.v[1] = -v[1];
    	result.v[2] = -v[2];
    	result.v[3] = -v[3];
    	return result;
    }

    
    /**
     * Adds the given vector to the current vector, and returns the result.
     * @param u
     * 		The vector to be added to this vector.
     * @return
     * 		The new vector.
     */
    public Vec4 add(Vec4 u) {
    	Vec4 result = new Vec4();
    	result.v[0] = v[0] + u.v[0];
    	result.v[1] = v[1] + u.v[1];
    	result.v[2] = v[2] + u.v[2];
    	result.v[3] = v[3] + u.v[3];
    	return result;
    }

	/**
	 * Substracts the given vector from this vector.
	 * @param u
	 * 		The vector to be substracted from this one.
	 * @return
	 * 		The new Vector, which is a result of the substraction.
	 */
	public Vec4 sub(Vec4 u) {
		Vec4 result = new Vec4();
    	result.v[0] = v[0] - u.v[0];
    	result.v[1] = v[1] - u.v[1];
    	result.v[2] = v[2] - u.v[2];
    	result.v[3] = v[3] - u.v[3];
    	return result;
	}

	/**
	 * Multiplies the given scalar with this vector.
	 * @param n
	 * 		The scalar to be multiplied with this one.
	 * @return
	 * 		The new Vector, which is a result of the multiplication.
	 */
	public Vec4 mul(Number n) {
		float fn = n.floatValue();
		Vec4 result = new Vec4();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	result.v[2] = v[2] *fn;
    	result.v[3] = v[3] *fn;
    	return result;
	}

	/**
	 * Multiplies the given vector with this vector.
	 * @param u
	 * 		The vector to be multiplied with this one.
	 * @return
	 * 		The new Vector, which is a result of the multiplication.
	 */
	public Vec4 mul(Vec4 u) {
		Vec4 result = new Vec4();
    	result.v[0] = v[0] * u.v[0];
    	result.v[1] = v[1] * u.v[1];
    	result.v[2] = v[2] * u.v[2];
    	result.v[3] = v[3] * u.v[3];
    	return result;
	}

	/**
	 * Divides the current vector with the given scalar.
	 * @param n
	 * 		The scalar to be divided with.
	 * @return
	 * 		The new Vector, which is a result of the division.
	 */
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
    
    public Vec4 clone() {
    	return new Vec4(this);
    }
    
    @Override
	public int hashCode() {
		int hashCode = (int) (v[0]+23 * 6833 + v[1]+7 *7207 + v[2]+11 * 7919 + v[3]+3 * 3);
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
