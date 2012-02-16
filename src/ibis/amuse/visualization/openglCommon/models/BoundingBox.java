package ibis.amuse.visualization.openglCommon.models;

import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.math.Vec3;

public class BoundingBox {
    private boolean initialized = false;
    private float minX, minY, minZ;
    private float maxX, maxY, maxZ;

    public BoundingBox() {
        initialized = false;
    }

    public void reset() {
        initialized = false;
    }

    public void resize(Vec3 newEntry) {
        if (initialized) {
            minX = Math.min(minX, newEntry.get(0));
            minY = Math.min(minY, newEntry.get(1));
            minZ = Math.min(minZ, newEntry.get(2));

            maxX = Math.max(maxX, newEntry.get(0));
            maxY = Math.max(maxY, newEntry.get(1));
            maxZ = Math.max(maxZ, newEntry.get(2));
        } else {
            minX = newEntry.get(0);
            minY = newEntry.get(1);
            minZ = newEntry.get(2);

            maxX = newEntry.get(0);
            maxY = newEntry.get(1);
            maxZ = newEntry.get(2);
        }

        initialized = true;
    }

    public Vec3 getMin() throws UninitializedException {
        if (!initialized)
            throw new UninitializedException("BoundingBox not initialized.");
        return new Vec3(minX, minY, minZ);
    }

    public Vec3 getMax() throws UninitializedException {
        if (!initialized)
            throw new UninitializedException("BoundingBox not initialized.");
        return new Vec3(maxX, maxY, maxZ);
    }

    public float getHeight() {
        return maxY - minY;
    }

    public float getWidth() {
        return maxX - minX;
    }

    public float getDepth() {
        return maxZ - minZ;
    }
}
