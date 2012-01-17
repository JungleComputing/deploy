package ibis.amuse.visualization.openglCommon.math;

public class Vec2 extends Vector {
	/**
     * Creates a new vector, initialized to 0.
     */
    public Vec2() {
    	super(2);
        this.v[0] = 0f;
        this.v[1] = 0f;
    }
    
    /**
     * Creates a new vector by copying the given vector.     
     * @param v
     * 		The vector to be copied.
     */
    public Vec2(Vec2 v) {
    	super(2);
        this.v[0] = v.v[0];
        this.v[1] = v.v[1];
    }
    
    /**
     * Creates a new vector with the given values.     
     * @param x
     * 		The value to be put in the first position.
     * @param y
     * 		The value to be put in the second position.
     * @param z
     * 		The value to be put in the third position.
     */
    public Vec2(float x, float y) {
    	super(2);
        this.v[0] = x;
        this.v[1] = y;
    }

    
    /**
     * Gives the negated vector of this vector. 
     * @return
     * 		The new negated vector.
     */
    public Vec2 neg() {
    	Vec2 result = new Vec2();
    	result.v[0] = -v[0];
    	result.v[1] = -v[1];
    	return result;
    }

    
    /**
     * Adds the given vector to the current vector, and returns the result.
     * @param u
     * 		The vector to be added to this vector.
     * @return
     * 		The new vector.
     */
    public Vec2 add(Vec2 u) {
    	Vec2 result = new Vec2();
    	result.v[0] = v[0] + u.v[0];
    	result.v[1] = v[1] + u.v[1];
    	return result;
    }

	/**
	 * Substracts the given vector from this vector.
	 * @param u
	 * 		The vector to be substracted from this one.
	 * @return
	 * 		The new Vector, which is a result of the substraction.
	 */
	public Vec2 sub(Vec2 u) {
		Vec2 result = new Vec2();
    	result.v[0] = v[0] - u.v[0];
    	result.v[1] = v[1] - u.v[1];
    	return result;
	}

	/**
	 * Multiplies the given scalar with this vector.
	 * @param n
	 * 		The scalar to be multiplied with this one.
	 * @return
	 * 		The new Vector, which is a result of the multiplication.
	 */
	public Vec2 mul(Number n) {
		float fn = n.floatValue();
		Vec2 result = new Vec2();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	return result;
	}

	/**
	 * Multiplies the given vector with this vector.
	 * @param u
	 * 		The vector to be multiplied with this one.
	 * @return
	 * 		The new Vector, which is a result of the multiplication.
	 */
	public Vec2 mul(Vec2 u) {
		Vec2 result = new Vec2();
    	result.v[0] = v[0] * u.v[0];
    	result.v[1] = v[1] * u.v[1];
    	return result;
	}

	/**
	 * Divides the current vector with the given scalar.
	 * @param n
	 * 		The scalar to be divided with.
	 * @return
	 * 		The new Vector, which is a result of the division.
	 */
	public Vec2 div(Number n) {  
		float f = n.floatValue();
    	if (f == 0f) return new Vec2();
		float fn = 1f / f;

		Vec2 result = new Vec2();
    	result.v[0] = v[0] *fn;
    	result.v[1] = v[1] *fn;
    	return result;
	}
    
    public Vec2 clone() {
    	return new Vec2(this);
    }
    
    @Override
	public int hashCode() {
		int hashCode = (int) (v[0]+23 * 6833 + v[1]+7 *7207);
		return hashCode;
	}
    
    @Override
	public boolean equals(Object thatObject) {
		if (this == thatObject)
			return true;
		if (!(thatObject instanceof Vec2))
			return false;

		// cast to native object is now safe
		Vec2 that = (Vec2) thatObject;

		// now a proper field-by-field evaluation can be made
		return (v[0] == that.v[0] && v[1] == that.v[1]);
	}
}
