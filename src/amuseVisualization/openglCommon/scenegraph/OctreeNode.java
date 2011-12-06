package amuseVisualization.openglCommon.scenegraph;


import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL3;

import amuseVisualization.GLWindow;
import amuseVisualization.Settings;
import amuseVisualization.amuseAdaptor.Astrophysics;
import amuseVisualization.openglCommon.math.Mat4;
import amuseVisualization.openglCommon.math.MatrixMath;
import amuseVisualization.openglCommon.math.Vec3;
import amuseVisualization.openglCommon.math.Vec4;
import amuseVisualization.openglCommon.models.Model;
import amuseVisualization.openglCommon.models.base.Sphere;
import amuseVisualization.openglCommon.shaders.Program;

public class OctreeNode {

    private final int maxElements;
    private OctreeNode ppp, ppn, pnp, pnn, npp, npn, nnp, nnn;

    private final HashMap<Vec3, Double> elements;
    private int childCounter;

    private final Vec3 center;
    private final float cubeSize;
    private boolean subdivided = false;
    private boolean initialized = false;

    private final Mat4 TMatrix;

    private final int depth;
    private final HashMap<Integer, Model> models;
    private final Model model;
    private Vec4 color;

    private double total_u;
    private float density;

    private int subdivision;

    public OctreeNode(int maxElements, int depth, int subdivision, HashMap<Integer, Model> cloudModels, Vec3 corner,
            float halfSize) {
        this.maxElements = maxElements;
        this.cubeSize = halfSize;
        this.subdivision = subdivision;
        center = corner.add(new Vec3(halfSize, halfSize, halfSize));

        TMatrix = MatrixMath.translate(center);

        elements = new HashMap<Vec3, Double>();

        childCounter = 0;

        this.depth = depth;
        this.models = cloudModels;

        int index = depth + Settings.getMaxExpectedModels() * subdivision;
        if (!models.containsKey(index)) {
            model = new Sphere(Astrophysics.getGasMaterial(), 0, halfSize * 3f, new Vec3());
            models.put(index, model);
        } else {
            model = models.get(index);
        }

        total_u = 0.0;
    }

    public OctreeNode(OctreeNode other) {
        this.center = other.center;
        this.childCounter = other.childCounter;
        this.color = other.color;
        this.cubeSize = other.cubeSize;
        this.density = other.density;
        this.depth = other.depth;
        this.elements = other.elements;
        this.initialized = other.initialized;
        this.maxElements = other.maxElements;
        this.model = other.model;
        this.models = other.models;
        this.ppp = other.ppp;
        this.ppn = other.ppn;
        this.pnp = other.pnp;
        this.pnn = other.pnn;
        this.npp = other.npp;
        this.npn = other.npn;
        this.nnp = other.nnp;
        this.nnn = other.nnn;
        this.subdivided = other.subdivided;
        this.TMatrix = other.TMatrix;
        this.total_u = other.total_u;
    }

    //
    // public OctreeNode() {
    // // Dummy constructor
    // }

    public void init(GL3 gl) {
        if (!initialized) {
            model.init(gl);

            if (subdivided) {
                ppp.init(gl);
                ppn.init(gl);
                pnp.init(gl);
                pnn.init(gl);
                npp.init(gl);
                npn.init(gl);
                nnp.init(gl);
                nnn.init(gl);
            }
        }

        initialized = true;
    }

    public void delete(GL3 gl) {
        if (initialized) {
            model.delete(gl);

            if (subdivided) {
                ppp.delete(gl);
                ppn.delete(gl);
                pnp.delete(gl);
                pnn.delete(gl);
                npp.delete(gl);
                npn.delete(gl);
                nnp.delete(gl);
                nnn.delete(gl);
            }
        }
    }

    private void subDiv() {
        float size = cubeSize / 2f;
        ppp = new OctreeNode(maxElements, depth + 1, subdivision, models, center.add(new Vec3(0f, 0f, 0f)), size);
        ppn = new OctreeNode(maxElements, depth + 1, subdivision, models, center.add(new Vec3(0f, 0f, -cubeSize)), size);
        pnp = new OctreeNode(maxElements, depth + 1, subdivision, models, center.add(new Vec3(0f, -cubeSize, 0f)), size);
        pnn = new OctreeNode(maxElements, depth + 1, subdivision, models,
                center.add(new Vec3(0f, -cubeSize, -cubeSize)), size);
        npp = new OctreeNode(maxElements, depth + 1, subdivision, models, center.add(new Vec3(-cubeSize, 0f, 0f)), size);
        npn = new OctreeNode(maxElements, depth + 1, subdivision, models,
                center.add(new Vec3(-cubeSize, 0f, -cubeSize)), size);
        nnp = new OctreeNode(maxElements, depth + 1, subdivision, models,
                center.add(new Vec3(-cubeSize, -cubeSize, 0f)), size);
        nnn = new OctreeNode(maxElements, depth + 1, subdivision, models, center.add(new Vec3(-cubeSize, -cubeSize,
                -cubeSize)), size);

        for (Map.Entry<Vec3, Double> element : elements.entrySet()) {
            addSubdivided(element.getKey(), element.getValue());
        }

        elements.clear();

        subdivided = true;
    }

    public void addGas(Vec3 location, double u) {
        if ((location.get(0) > center.get(0) - cubeSize) && (location.get(1) > center.get(1) - cubeSize)
                && (location.get(2) > center.get(2) - cubeSize) && (location.get(0) < center.get(0) + cubeSize)
                && (location.get(1) < center.get(1) + cubeSize) && (location.get(2) < center.get(2) + cubeSize)) {
            if (childCounter > maxElements && !subdivided) {
                if (depth < Settings.getMaxCloudDepth()) {
                    subDiv();
                    total_u = 0.0;
                } else {
                    System.out.println("Max division!");
                }
            }
            if (subdivided) {
                addSubdivided(location, u);
            } else {
                elements.put(location, u);
                total_u += u;
            }
            childCounter++;
        }
    }

    public void doneAddingGas() {
        elements.clear();

        if (subdivided) {
            ppp.doneAddingGas();
            ppn.doneAddingGas();
            pnp.doneAddingGas();
            pnn.doneAddingGas();
            npp.doneAddingGas();
            npn.doneAddingGas();
            nnp.doneAddingGas();
            nnn.doneAddingGas();
        } else {
            density = (childCounter / (cubeSize * cubeSize * cubeSize * 6));
            color = Astrophysics.gasColor(density, (float) total_u, childCounter);
        }
    }

    public void addSubdivided(Vec3 location, double u) {
        if (location.get(0) < center.get(0)) {
            if (location.get(1) < center.get(1)) {
                if (location.get(2) < center.get(2)) {
                    nnn.addGas(location, u);
                } else {
                    nnp.addGas(location, u);
                }
            } else {
                if (location.get(2) < center.get(2)) {
                    npn.addGas(location, u);
                } else {
                    npp.addGas(location, u);
                }
            }
        } else {
            if (location.get(1) < center.get(1)) {
                if (location.get(2) < center.get(2)) {
                    pnn.addGas(location, u);
                } else {
                    pnp.addGas(location, u);
                }
            } else {
                if (location.get(2) < center.get(2)) {
                    ppn.addGas(location, u);
                } else {
                    ppp.addGas(location, u);
                }
            }
        }
    }

    public void draw(GL3 gl, Mat4 MVMatrix) {
        if (initialized) {
            if (subdivided) {
                draw_sorted(gl, MVMatrix);
            } else {
                if (density > GLWindow.EPSILON) {
                    Mat4 newM = MVMatrix.mul(TMatrix);

                    model.material.setColor(color);
                    model.material.setTransparency(density * GLWindow.gas_opacity_factor);
                    model.draw(gl, newM);
                }
            }
        }
    }

    public void draw(GL3 gl, Program program, Mat4 MVMatrix) {
        if (initialized) {
            if (subdivided) {
                draw_sorted(gl, program, MVMatrix);
            } else {
                if (density > GLWindow.EPSILON) {
                    Mat4 newM = MVMatrix.mul(TMatrix);

                    model.material.setColor(color);
                    model.material.setTransparency(density * GLWindow.gas_opacity_factor);
                    model.draw(gl, program, newM);
                }
            }
        }
    }

    private void draw_sorted(GL3 gl, Mat4 MVMatrix) {
        if (GLWindow.getCurrentOctant() == GLWindow.octants.NNN) {
            ppp.draw(gl, MVMatrix);

            npp.draw(gl, MVMatrix);
            pnp.draw(gl, MVMatrix);
            ppn.draw(gl, MVMatrix);

            nnp.draw(gl, MVMatrix);
            pnn.draw(gl, MVMatrix);
            npn.draw(gl, MVMatrix);

            nnn.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.NNP) {
            ppn.draw(gl, MVMatrix);

            npn.draw(gl, MVMatrix);
            pnn.draw(gl, MVMatrix);
            ppp.draw(gl, MVMatrix);

            nnn.draw(gl, MVMatrix);
            pnp.draw(gl, MVMatrix);
            npp.draw(gl, MVMatrix);

            nnp.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.NPN) {
            pnp.draw(gl, MVMatrix);

            nnp.draw(gl, MVMatrix);
            ppp.draw(gl, MVMatrix);
            pnn.draw(gl, MVMatrix);

            npp.draw(gl, MVMatrix);
            ppn.draw(gl, MVMatrix);
            nnn.draw(gl, MVMatrix);

            npn.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.NPP) {
            pnn.draw(gl, MVMatrix);

            nnn.draw(gl, MVMatrix);
            ppn.draw(gl, MVMatrix);
            pnp.draw(gl, MVMatrix);

            npn.draw(gl, MVMatrix);
            ppp.draw(gl, MVMatrix);
            nnp.draw(gl, MVMatrix);

            npp.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PNN) {
            npp.draw(gl, MVMatrix);

            ppp.draw(gl, MVMatrix);
            nnp.draw(gl, MVMatrix);
            npn.draw(gl, MVMatrix);

            pnp.draw(gl, MVMatrix);
            nnn.draw(gl, MVMatrix);
            ppn.draw(gl, MVMatrix);

            pnn.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PNP) {
            npn.draw(gl, MVMatrix);

            ppn.draw(gl, MVMatrix);
            nnn.draw(gl, MVMatrix);
            npp.draw(gl, MVMatrix);

            pnn.draw(gl, MVMatrix);
            nnp.draw(gl, MVMatrix);
            ppp.draw(gl, MVMatrix);

            pnp.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PPN) {
            nnp.draw(gl, MVMatrix);

            pnp.draw(gl, MVMatrix);
            npp.draw(gl, MVMatrix);
            nnn.draw(gl, MVMatrix);

            ppp.draw(gl, MVMatrix);
            npn.draw(gl, MVMatrix);
            pnn.draw(gl, MVMatrix);

            ppn.draw(gl, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PPP) {
            nnn.draw(gl, MVMatrix);

            pnn.draw(gl, MVMatrix);
            npn.draw(gl, MVMatrix);
            nnp.draw(gl, MVMatrix);

            ppn.draw(gl, MVMatrix);
            npp.draw(gl, MVMatrix);
            pnp.draw(gl, MVMatrix);

            ppp.draw(gl, MVMatrix);
        }
    }

    private void draw_sorted(GL3 gl, Program program, Mat4 MVMatrix) {
        if (GLWindow.getCurrentOctant() == GLWindow.octants.NNN) {
            ppp.draw(gl, program, MVMatrix);

            npp.draw(gl, program, MVMatrix);
            pnp.draw(gl, program, MVMatrix);
            ppn.draw(gl, program, MVMatrix);

            nnp.draw(gl, program, MVMatrix);
            pnn.draw(gl, program, MVMatrix);
            npn.draw(gl, program, MVMatrix);

            nnn.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.NNP) {
            ppn.draw(gl, program, MVMatrix);

            npn.draw(gl, program, MVMatrix);
            pnn.draw(gl, program, MVMatrix);
            ppp.draw(gl, program, MVMatrix);

            nnn.draw(gl, program, MVMatrix);
            pnp.draw(gl, program, MVMatrix);
            npp.draw(gl, program, MVMatrix);

            nnp.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.NPN) {
            pnp.draw(gl, program, MVMatrix);

            nnp.draw(gl, program, MVMatrix);
            ppp.draw(gl, program, MVMatrix);
            pnn.draw(gl, program, MVMatrix);

            npp.draw(gl, program, MVMatrix);
            ppn.draw(gl, program, MVMatrix);
            nnn.draw(gl, program, MVMatrix);

            npn.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.NPP) {
            pnn.draw(gl, program, MVMatrix);

            nnn.draw(gl, program, MVMatrix);
            ppn.draw(gl, program, MVMatrix);
            pnp.draw(gl, program, MVMatrix);

            npn.draw(gl, program, MVMatrix);
            ppp.draw(gl, program, MVMatrix);
            nnp.draw(gl, program, MVMatrix);

            npp.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PNN) {
            npp.draw(gl, program, MVMatrix);

            ppp.draw(gl, program, MVMatrix);
            nnp.draw(gl, program, MVMatrix);
            npn.draw(gl, program, MVMatrix);

            pnp.draw(gl, program, MVMatrix);
            nnn.draw(gl, program, MVMatrix);
            ppn.draw(gl, program, MVMatrix);

            pnn.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PNP) {
            npn.draw(gl, program, MVMatrix);

            ppn.draw(gl, program, MVMatrix);
            nnn.draw(gl, program, MVMatrix);
            npp.draw(gl, program, MVMatrix);

            pnn.draw(gl, program, MVMatrix);
            nnp.draw(gl, program, MVMatrix);
            ppp.draw(gl, program, MVMatrix);

            pnp.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PPN) {
            nnp.draw(gl, program, MVMatrix);

            pnp.draw(gl, program, MVMatrix);
            npp.draw(gl, program, MVMatrix);
            nnn.draw(gl, program, MVMatrix);

            ppp.draw(gl, program, MVMatrix);
            npn.draw(gl, program, MVMatrix);
            pnn.draw(gl, program, MVMatrix);

            ppn.draw(gl, program, MVMatrix);
        } else if (GLWindow.getCurrentOctant() == GLWindow.octants.PPP) {
            nnn.draw(gl, program, MVMatrix);

            pnn.draw(gl, program, MVMatrix);
            npn.draw(gl, program, MVMatrix);
            nnp.draw(gl, program, MVMatrix);

            ppn.draw(gl, program, MVMatrix);
            npp.draw(gl, program, MVMatrix);
            pnp.draw(gl, program, MVMatrix);

            ppp.draw(gl, program, MVMatrix);
        }
    }
}
