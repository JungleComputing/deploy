package ibis.deploy.gui.outputViz;

import ibis.deploy.gui.outputViz.amuse.Astrophysics;
import ibis.deploy.gui.outputViz.amuse.Hdf5TimedPlayer;
import ibis.deploy.gui.outputViz.amuse.Hdf5TimedPlayer.states;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.Perlin3D;
import ibis.deploy.gui.outputViz.common.Picture;
import ibis.deploy.gui.outputViz.common.PostProcessTexture;
import ibis.deploy.gui.outputViz.common.Texture2D;
import ibis.deploy.gui.outputViz.common.math.Color4;
import ibis.deploy.gui.outputViz.common.math.Mat3;
import ibis.deploy.gui.outputViz.common.math.Mat4;
import ibis.deploy.gui.outputViz.common.math.MatrixMath;
import ibis.deploy.gui.outputViz.common.math.Point4;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.math.Vec4;
import ibis.deploy.gui.outputViz.common.scenegraph.OctreeNode;
import ibis.deploy.gui.outputViz.common.scenegraph.SGNode;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;
import ibis.deploy.gui.outputViz.models.Axis;
import ibis.deploy.gui.outputViz.models.Model;
import ibis.deploy.gui.outputViz.models.base.Quad;
import ibis.deploy.gui.outputViz.shaders.Program;
import ibis.deploy.gui.outputViz.shaders.ProgramLoader;

import java.nio.FloatBuffer;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

public class GLWindow implements GLEventListener {
    public static boolean MULTICOLOR_GAS = false;
    public static boolean GAS_COLORMAP = true;
    public static boolean POST_PROCESS = true;
    public static boolean AXES = true;
    public static boolean GAS_ON = true;
    public static boolean PREDICTION_ON = false;
    public static boolean DEPTH_TESTED_GAS = false;
    public static boolean movie_mode = true;

    public static long WAITTIME = 200;
    public static long LONGWAITTIME = 10000;
    public static int MAX_CLOUD_DEPTH = 25;
    public static double MAX_PREGENERATED_STAR_SIZE = 10.0;
    public static float GAS_EDGES = 800f;
    public static int MAX_ELEMENTS_PER_OCTREE_NODE = 100;
    public static int MAX_ELEMENTS_PER_OCTREE_NODE_IN_SNAPSHOT = 2;
    public static float EPSILON = 1.0E-7f;
    public static float GAS_OPACITY_FACTOR = 1f;

    public static enum octants {
        PPP, PPN, PNP, PNN, NPP, NPN, NNP, NNN
    }

    public static int selected_blur_type = 4;
    public static int blur_passes = 2;

    public static boolean saved_once = true;
    public static octants current_view_octant = octants.PPP;

    private final OutputVizPanel panel;
    private final ProgramLoader loader;

    private int movieCounter = 0;

    Program animatedTurbulence;
    Program ppl, axesShader, gas, postprocess;
    Program gaussianBlur;

    Perlin3D noiseTex;

    FloatBuffer color1 = (new Vec3(0f, 0f, 0f)).asBuffer();
    FloatBuffer color2 = (new Vec3(.6f, .1f, 0f)).asBuffer();
    FloatBuffer color3 = (new Vec3(.5f, .5f, .5f)).asBuffer();

    float[] bla1 = { 2f };
    FloatBuffer noiseScale = FloatBuffer.wrap(bla1);
    FloatBuffer lightPos = (new Vec3(2f, 2f, 2f)).asBuffer();
    float[] bla2 = { 2f };
    FloatBuffer scale = FloatBuffer.wrap(bla2);
    float[] bla3 = { 0f };
    FloatBuffer offset = FloatBuffer.wrap(bla3);

    Hdf5TimedPlayer timer;
    public boolean timerInitialized = false;
    public boolean snapshotting = false;

    SGNode root, root2;
    OctreeNode cubeRoot, cubeRoot2;
    boolean newRoot = true, newCubeRoot = true;

    float[] bla4 = { 50f };
    FloatBuffer shininess = FloatBuffer.wrap(bla4);

    private final float radius = 1.0f;
    private final float ftheta = 0.0f;
    private final float phi = 0.0f;

    private final float fovy = 45.0f;
    private float aspect;
    private final float zNear = 0.1f, zFar = 3000.0f;

    public int canvasWidth, canvasHeight;

    private Vec3 rotation = new Vec3();
    private final Vec3 translation = new Vec3(0f, 0f, -150f);

    Texture2D axesTex, gasColorTex;

    Texture2D gasTex, starTex;

    Model fullscreenQuad, fullscreenQuad0, fullscreenQuad1, fullscreenQuad2, volumeGas;
    Model xAxis, yAxis, zAxis;

    float moviemaker_rotation_offset = 0f;

    public GLWindow(OutputVizPanel panel) {
        this.panel = panel;
        loader = new ProgramLoader();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        // Anti-Aliasing
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);

        // Depth testing
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glClearDepth(1.0f);

        // Culling
        gl.glEnable(GL3.GL_CULL_FACE);
        gl.glCullFace(GL3.GL_BACK);

        // Enable Blending (needed for both Transparency and
        // Anti-Aliasing
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL3.GL_BLEND);

        // Enable Vertical Sync
        gl.setSwapInterval(1);

        // Load and compile shaders, then use program.
        try {
            animatedTurbulence = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_sunsurface.vp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_animatedTurbulence.fp");
            // gas = loader.createProgram(gl,
            // "src/ibis/deploy/gui/outputViz/shaders/src/vs_sunsurface.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/fs_animatedTurbulence.fp");
            ppl = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_ppl.vp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_ppl.fp");
            axesShader = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_axes.fp");
            gas = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_gas.fp");
            // gas = loader.createProgram(gl,
            // "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/fs_volumerendering.fp");
            // gas = loader.createProgram(gl,
            // "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/fs_turbulence.fp");
            // glow = loader.createProgram(gl,
            // "src/ibis/deploy/gui/outputViz/shaders/src/vs_glow.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/gs_glow.fp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/fs_glow.fp");
            // star = loader.createProgram(gl,
            // "src/ibis/deploy/gui/outputViz/shaders/src/vs_star.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/fs_star.fp");
            if (POST_PROCESS)
                postprocess = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_postprocess.vp",
                        "src/ibis/deploy/gui/outputViz/shaders/src/fs_postprocess.fp");
            if (POST_PROCESS)
                gaussianBlur = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_postprocess.vp",
                        "src/ibis/deploy/gui/outputViz/shaders/src/fs_gaussian_blur.fp");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        root = new SGNode();
        newRoot = false;

        cubeRoot = new OctreeNode();
        newCubeRoot = false;

        root.init(gl);
        cubeRoot.init(gl);

        // AXES
        Color4 axisColor = new Color4(0f, 1f, 0f, .3f);
        Material axisMaterial = new Material(axisColor, axisColor, axisColor);
        xAxis = new Axis(axesShader, axisMaterial, new Vec3(-800f, 0f, 0f), new Vec3(800f, 0f, 0f),
                Astrophysics.toScreenCoord(1), Astrophysics.toScreenCoord(.2));
        xAxis.init(gl);
        yAxis = new Axis(axesShader, axisMaterial, new Vec3(0f, -800f, 0f), new Vec3(0f, 800f, 0f),
                Astrophysics.toScreenCoord(1), Astrophysics.toScreenCoord(.2));
        yAxis.init(gl);
        zAxis = new Axis(axesShader, axisMaterial, new Vec3(0f, 0f, -800f), new Vec3(0f, 0f, 800f),
                Astrophysics.toScreenCoord(1), Astrophysics.toScreenCoord(.2));
        zAxis.init(gl);

        // FULL SCREEN QUADS
        fullscreenQuad = new Quad(postprocess, Material.random(), 2, 2, new Vec3(0, 0, 0.1f));
        fullscreenQuad.init(gl);

        fullscreenQuad0 = new Quad(ppl, Material.random(), 2, 2, new Vec3(0, 0, 0.1f));
        fullscreenQuad0.init(gl);

        fullscreenQuad1 = new Quad(gaussianBlur, Material.random(), 2, 2, new Vec3(0, 0, 0.1f));
        fullscreenQuad1.init(gl);

        fullscreenQuad2 = new Quad(gaussianBlur, Material.random(), 2, 2, new Vec3(0, 0, 0.1f));
        fullscreenQuad2.init(gl);

        // TEXTURES
        noiseTex = new Perlin3D(128, GL3.GL_TEXTURE0);
        noiseTex.init(gl);

        axesTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE1);
        axesTex.init(gl);

        gasColorTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE2);
        gasColorTex.init(gl);

        gasTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE3);
        gasTex.init(gl);

        starTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE4);
        starTex.init(gl);

        gl.glClearColor(0f, 0f, 0f, 0f);

        panel.callback();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        try {
            GL3 gl = drawable.getGL().getGL3();

            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            Point4 eye = new Point4((float) (radius * Math.sin(ftheta) * Math.cos(phi)), (float) (radius
                    * Math.sin(ftheta) * Math.sin(phi)), (float) (radius * Math.cos(ftheta)), 1.0f);
            Point4 at = new Point4(0.0f, 0.0f, 0.0f, 1.0f);
            Vec4 up = new Vec4(0.0f, 1.0f, 0.0f, 0.0f);

            Mat4 mv = MatrixMath.lookAt(eye, at, up);
            mv = mv.mul(MatrixMath.translate(translation));
            mv = mv.mul(MatrixMath.rotationX(rotation.get(0)));
            mv = mv.mul(MatrixMath.rotationY(rotation.get(1)));

            Mat3 n = new Mat3();
            loader.setUniformMatrix("NormalMatrix", n.asBuffer());

            Mat4 p = MatrixMath.perspective(fovy, aspect, zNear, zFar);
            loader.setUniformMatrix("PMatrix", p.asBuffer());

            offset.put(0, offset.get(0) + 0.0001f);

            // Vertex shader
            loader.setUniformVector("LightPos", lightPos);
            loader.setUniformMatrix("SMatrix", MatrixMath.scale(1, 1, 1).asBuffer());

            // Fragment shader
            loader.setUniformVector("Color1", color1);
            loader.setUniformVector("Color2", color2);
            loader.setUniformVector("Color3", color3);

            loader.setUniformVector("Offset", offset);
            loader.setUniformVector("Shininess", shininess);

            loader.setUniform("scrWidth", canvasWidth);
            loader.setUniform("scrHeight", canvasHeight);

            if (timerInitialized)
                synchronized (timer) {
                    SGNode root = initSGRoot(gl);
                    OctreeNode cubeRoot = initCubeRoot(gl);

                    if (GAS_COLORMAP) {
                        loader.setUniformMatrix("SMatrix", MatrixMath.scale(2).asBuffer());
                        loader.setUniform("Mode", 1);
                        loader.setUniform("Multicolor", true);

                        if (!DEPTH_TESTED_GAS)
                            gl.glEnable(GL3.GL_DEPTH_TEST);

                        root.draw(gl, mv);

                        renderToTexture(gl, gasColorTex);

                        loader.setUniformMatrix("SMatrix", MatrixMath.scale(1).asBuffer());
                        loader.setUniform("Colormap", gasColorTex.getMultitexNumber());

                        gas.use(gl);
                        if (!DEPTH_TESTED_GAS)
                            gl.glDisable(GL3.GL_DEPTH_TEST);
                        cubeRoot.draw(gl, mv);
                    } else {
                        loader.setUniform("Multicolor", false);

                        gas.use(gl);
                        if (!DEPTH_TESTED_GAS)
                            gl.glDisable(GL3.GL_DEPTH_TEST);
                        cubeRoot.draw(gl, mv);
                    }

                    if (POST_PROCESS) {
                        renderToTexture(gl, gasTex);

                        blur(gl, gasTex, blur_passes, selected_blur_type);
                    }

                    noiseTex.use(gl);
                    loader.setUniform("Noise", noiseTex.getMultitexNumber());
                    loader.setUniformMatrix("PMatrix", p.asBuffer());
                    loader.setUniform("Mode", 0);
                    ppl.use(gl);
                    if (!DEPTH_TESTED_GAS)
                        gl.glEnable(GL3.GL_DEPTH_TEST);

                    root.draw(gl, mv);

                    if (POST_PROCESS) {
                        renderToTexture(gl, starTex);
                    }

                    axesShader.use(gl);

                    if (AXES) {
                        xAxis.draw(gl, mv);
                        yAxis.draw(gl, mv);
                        zAxis.draw(gl, mv);
                    }

                    if (POST_PROCESS) {
                        renderToTexture(gl, axesTex);
                    }

                    if (POST_PROCESS) {
                        loader.setUniform("axesTexture", axesTex.getMultitexNumber());
                        loader.setUniform("gasTexture", gasTex.getMultitexNumber());
                        loader.setUniform("starTexture", starTex.getMultitexNumber());

                        loader.setUniform("scrWidth", canvasWidth);
                        loader.setUniform("scrHeight", canvasHeight);
                        postprocess.use(gl);

                        loader.setUniformMatrix("PMatrix", new Mat4().asBuffer());
                        fullscreenQuad.draw(gl, new Mat4());
                    }

                    if (!saved_once
                            && (Hdf5TimedPlayer.currentState == states.SNAPSHOTTING || Hdf5TimedPlayer.currentState == states.MOVIEMAKING)) {
                        String fileName = "" + timer.currentFrame + " {" + rotation.get(0) + "," + rotation.get(1)
                                + " - " + translation.get(2) + "} ";
                        if (Hdf5TimedPlayer.currentState == states.MOVIEMAKING && movieCounter < 2) {
                            rotation.set(1, rotation.get(1) + .5f);
                            saveToPicture(gl, "" + (timer.currentFrame * 3 + movieCounter));
                            movieCounter++;

                        } else if (Hdf5TimedPlayer.currentState == states.MOVIEMAKING) {
                            saveToPicture(gl, "" + (timer.currentFrame * 3 + movieCounter));
                            movieCounter = 0;

                            saved_once = true;
                        } else {
                            saveToPicture(gl, fileName);
                            Hdf5TimedPlayer.setState(states.CLEANUP);

                            saved_once = true;
                        }
                    }
                }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void blur(GL3 gl, Texture2D target, int passes, int blurType) {
        gaussianBlur.setUniform("Texture", target.getMultitexNumber());
        gaussianBlur.setUniformMatrix("PMatrix", new Mat4().asBuffer());
        gaussianBlur.setUniform("blurType", blurType);

        try {
            gaussianBlur.use(gl);

            for (int i = 0; i < passes; i++) {
                gaussianBlur.setUniform("blurDirection", 0);
                fullscreenQuad1.draw(gl, new Mat4());
                renderToTexture(gl, target);

                gaussianBlur.setUniform("blurDirection", 1);
                fullscreenQuad1.draw(gl, new Mat4());
                renderToTexture(gl, target);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void renderToTexture(GL3 gl, Texture2D target) {
        try {
            target.use(gl);
            gl.glCopyTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, 0, 0, canvasWidth, canvasHeight, 0);
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void saveToPicture(GL3 gl, String fileName) {
        Picture p = new Picture(canvasWidth, canvasHeight);
        p.copyFrameBufferToFile(gl, panel.getPath(), fileName);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, w, h);
        aspect = (float) w / h;
        canvasWidth = w;
        canvasHeight = h;

        axesTex.delete(gl);
        axesTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE1);
        axesTex.init(gl);

        gasColorTex.delete(gl);
        gasColorTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE2);
        gasColorTex.init(gl);

        gasTex.delete(gl);
        gasTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE3);
        gasTex.init(gl);

        starTex.delete(gl);
        starTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE4);
        starTex.init(gl);
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        loader.cleanup(gl);
    }

    public float getViewDist() {
        return translation.get(2);
    }

    public void setViewDist(float viewDist) {
        translation.set(2, viewDist);
    }

    public void setRotation(Vec3 rotation) {
        float x = rotation.get(0);
        int qx = (int) Math.floor(x / 90f);
        float y = rotation.get(1);
        int qy = (int) Math.floor(y / 90f);

        if (qx == 0 && qy == 0) {
            current_view_octant = octants.NPP;
        } else if (qx == 0 && qy == 1) {
            current_view_octant = octants.NPN;
        } else if (qx == 0 && qy == 2) {
            current_view_octant = octants.PPN;
        } else if (qx == 0 && qy == 3) {
            current_view_octant = octants.PPP;

        } else if (qx == 1 && qy == 0) {
            current_view_octant = octants.PPN;
        } else if (qx == 1 && qy == 1) {
            current_view_octant = octants.PPP;
        } else if (qx == 1 && qy == 2) {
            current_view_octant = octants.NPP;
        } else if (qx == 1 && qy == 3) {
            current_view_octant = octants.NPN;

        } else if (qx == 2 && qy == 0) {
            current_view_octant = octants.PNN;
        } else if (qx == 2 && qy == 1) {
            current_view_octant = octants.PNP;
        } else if (qx == 2 && qy == 2) {
            current_view_octant = octants.NNP;
        } else if (qx == 2 && qy == 3) {
            current_view_octant = octants.NNN;

        } else if (qx == 3 && qy == 0) {
            current_view_octant = octants.NNP;
        } else if (qx == 3 && qy == 1) {
            current_view_octant = octants.NNN;
        } else if (qx == 3 && qy == 2) {
            current_view_octant = octants.PNN;
        } else if (qx == 3 && qy == 3) {
            current_view_octant = octants.PNP;
        }

        this.rotation = rotation;
    }

    public static octants getCurrentOctant() {
        return current_view_octant;
    }

    public void setRoot(SGNode root) {
        synchronized (this) {
            this.root2 = root;
            newRoot = true;
        }
    }

    public void setCubeRoot(OctreeNode cubeRoot) {
        synchronized (this) {
            this.cubeRoot2 = cubeRoot;
            newCubeRoot = true;
        }
    }

    private SGNode initSGRoot(GL3 gl) {
        synchronized (this) {
            if (newRoot) {
                if (Hdf5TimedPlayer.currentState == states.CLEANUP
                        || Hdf5TimedPlayer.currentState == states.MOVIEMAKING) {
                    root.delete(gl);
                    cubeRoot.delete(gl);
                    if (Hdf5TimedPlayer.currentState == states.CLEANUP)
                        Hdf5TimedPlayer.setState(states.REDRAWING);
                }
                root = root2;
                root.init(gl);
            }
            newRoot = false;
        }

        return root;
    }

    private OctreeNode initCubeRoot(GL3 gl) {
        synchronized (this) {
            if (newCubeRoot) {
                cubeRoot = cubeRoot2;
                cubeRoot.init(gl);
            }
            newCubeRoot = false;
        }

        return cubeRoot;
    }

    public void startAnimation(Hdf5TimedPlayer timer) {
        this.timer = timer;
        timer.init(this, ppl, gas, animatedTurbulence);
        new Thread(timer).start();
        timerInitialized = true;
    }

    public void stopAnimation() {
        if (timerInitialized) {
            setRoot(new SGNode());
            setCubeRoot(new OctreeNode());
            timer.close();
            timer.stop();
        }
        timerInitialized = false;
    }

    public static void setMulticolor(boolean newSetting) {
        MULTICOLOR_GAS = newSetting;
    }

    public static void setColormap(boolean newSetting) {
        GAS_COLORMAP = newSetting;
    }

    public static void setPostprocess(boolean newSetting) {
        POST_PROCESS = newSetting;
    }

    public static void setResolution(int newSetting) {
        MAX_ELEMENTS_PER_OCTREE_NODE = newSetting;
        Hdf5TimedPlayer.setState(states.REDRAWING);
    }

    public static void setAxes(boolean newSetting) {
        AXES = newSetting;
    }

    public static void setGas(boolean newSetting) {
        GAS_ON = newSetting;
    }

    public static void setPrediction(boolean newSetting) {
        PREDICTION_ON = newSetting;
    }

}
