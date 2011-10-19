package ibis.deploy.gui.outputViz;

import ibis.deploy.gui.outputViz.amuse.Astrophysics;
import ibis.deploy.gui.outputViz.amuse.Hdf5TimedPlayer;
import ibis.deploy.gui.outputViz.amuse.StarSGNode;
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

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;

public class GLWindow implements GLEventListener {
    private static boolean post_process = true;
    private static boolean axes = true;
    private static boolean exaggerate_colors = true;
    private static long waittime = 50;

    private static int levelOfDetail = 0;

    public static float gas_opacity_factor = 1.75f;

    public static final long LONGWAITTIME = 10000;
    public static final float EPSILON = 1.0E-7f;

    public static enum octants {
        PPP, PPN, PNP, PNN, NPP, NPN, NNP, NNN
    }

    public static octants current_view_octant = octants.PPP;

    private final OutputVizPanel panel;
    private final ProgramLoader loader;

    private Program animatedTurbulenceShader, pplShader, axesShader, gasShader, postprocessShader, gaussianBlurShader;

    private Perlin3D noiseTex;

    private final Vec3 lightPos = new Vec3(2f, 2f, 2f);

    private Hdf5TimedPlayer timer;

    private boolean timerInitialized = false;

    private final float shininess = 50f;

    private final float radius = 1.0f;
    private final float ftheta = 0.0f;
    private final float phi = 0.0f;

    private final float fovy = 45.0f;
    private final float zNear = 0.1f, zFar = 3000.0f;

    private int canvasWidth, canvasHeight;

    private Vec3 rotation = new Vec3();
    private final Vec3 translation = new Vec3(0f, 0f, -150f);

    private Texture2D axesTex, starHaloTex, gasTex, starTex, intermediateTex;
    private Texture2D axesTex4k, starHaloTex4k, gasTex4k, starTex4k, intermediateTex4k;

    // private FBO starFBO, starHaloFBO, gasFBO, axesFBO;

    private Model FSQ_postprocess, FSQ_blur;
    private Model xAxis, yAxis, zAxis;

    private boolean snapshotting = false;
    private StarSGNode sgRoot;
    private OctreeNode octreeRoot;
    private final GLContext offScreenContext;

    public GLWindow(OutputVizPanel panel, GLContext offScreenContext) {
        this.panel = panel;
        this.offScreenContext = offScreenContext;
        loader = new ProgramLoader();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        canvasWidth = drawable.getWidth();
        canvasHeight = drawable.getHeight();
        //
        // initDrawable(drawable.getContext());
        // initDrawable(context);
        // }
        //
        // private void initDrawable(GLContext drawable) {
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
            animatedTurbulenceShader = loader.createProgram(gl,
                    "src/ibis/deploy/gui/outputViz/shaders/src/vs_sunsurface.vp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_animatedTurbulence.fp");
            // gas = loader.createProgram(gl,
            // "src/ibis/deploy/gui/outputViz/shaders/src/vs_sunsurface.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/fs_animatedTurbulence.fp");
            pplShader = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_ppl.vp",
            // "src/ibis/deploy/gui/outputViz/shaders/src/gs_passthrough.fp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_ppl.fp");
            axesShader = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_axes.vp",
                    "src/ibis/deploy/gui/outputViz/shaders/src/fs_axes.fp");
            gasShader = loader.createProgram(gl, "src/ibis/deploy/gui/outputViz/shaders/src/vs_gas.vp",
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
            if (post_process)
                postprocessShader = loader.createProgram(gl,
                        "src/ibis/deploy/gui/outputViz/shaders/src/vs_postprocess.vp",
                        "src/ibis/deploy/gui/outputViz/shaders/src/fs_postprocess.fp");
            if (post_process)
                gaussianBlurShader = loader.createProgram(gl,
                        "src/ibis/deploy/gui/outputViz/shaders/src/vs_postprocess.vp",
                        "src/ibis/deploy/gui/outputViz/shaders/src/fs_gaussian_blur.fp");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        //
        // sgRoot = new SGNode();
        // newRoot = false;
        // sgRoot.init(gl);
        //
        // octreeRoot = new OctreeNode();
        // newCubeRoot = false;
        // octreeRoot.init(gl);

        // AXES
        Color4 axisColor = new Color4(0f, 1f, 0f, 1f);
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
        FSQ_postprocess = new Quad(postprocessShader, Material.random(), 2, 2, new Vec3(0, 0, 0.1f));
        FSQ_postprocess.init(gl);

        FSQ_blur = new Quad(gaussianBlurShader, Material.random(), 2, 2, new Vec3(0, 0, 0.1f));
        FSQ_blur.init(gl);

        // TEXTURES
        noiseTex = new Perlin3D(128, GL3.GL_TEXTURE0);
        noiseTex.init(gl);

        // Full screen textures (for post processing)
        starTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE1);
        starTex.init(gl);

        starHaloTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE2);
        starHaloTex.init(gl);

        gasTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE3);
        gasTex.init(gl);

        axesTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE4);
        axesTex.init(gl);

        starTex4k = new PostProcessTexture(Settings.getScreenshotScreenWidth(), Settings.getScreenshotScreenHeight(),
                GL3.GL_TEXTURE5);
        starTex4k.init(gl);

        starHaloTex4k = new PostProcessTexture(Settings.getScreenshotScreenWidth(),
                Settings.getScreenshotScreenHeight(), GL3.GL_TEXTURE6);
        starHaloTex4k.init(gl);

        gasTex4k = new PostProcessTexture(Settings.getScreenshotScreenWidth(), Settings.getScreenshotScreenHeight(),
                GL3.GL_TEXTURE7);
        gasTex4k.init(gl);

        axesTex4k = new PostProcessTexture(Settings.getScreenshotScreenWidth(), Settings.getScreenshotScreenHeight(),
                GL3.GL_TEXTURE8);
        axesTex4k.init(gl);

        intermediateTex = new PostProcessTexture(Settings.getScreenshotScreenWidth(),
                Settings.getScreenshotScreenHeight(), GL3.GL_TEXTURE9);
        intermediateTex.init(gl);

        intermediateTex4k = new PostProcessTexture(Settings.getScreenshotScreenWidth(),
                Settings.getScreenshotScreenHeight(), GL3.GL_TEXTURE10);
        intermediateTex4k.init(gl);

        // if (offscreen_rendering) {
        // fbo = new FBO(gl, 4096, 3112, GL3.GL_TEXTURE9);

        // starFBO = new FBO(gl, canvasWidth, canvasHeight, GL3.GL_TEXTURE10);
        // starHaloFBO = new FBO(gl, canvasWidth, canvasHeight,
        // GL3.GL_TEXTURE11);
        // gasFBO = new FBO(gl, canvasWidth, canvasHeight, GL3.GL_TEXTURE12);
        // axesFBO = new FBO(gl, canvasWidth, canvasHeight, GL3.GL_TEXTURE13);
        // }

        gl.glClearColor(0f, 0f, 0f, 0f);

        panel.callback();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (isTimerInitialized()) {
            synchronized (this) {
                try {
                    int status = drawable.getContext().makeCurrent();
                    if (status != GLContext.CONTEXT_CURRENT && status != GLContext.CONTEXT_CURRENT_NEW) {
                        System.err.println("Error swapping context to onscreen.");
                    }
                } catch (GLException e) {
                    System.err.println("Exception while swapping context to onscreen.");
                    e.printStackTrace();
                }

                GL3 gl = drawable.getContext().getGL().getGL3();
                gl.glViewport(0, 0, canvasWidth, canvasHeight);

                sgRoot = timer.getSgRoot();
                octreeRoot = timer.getOctreeRoot();

                displayContext(sgRoot, octreeRoot, starTex, starHaloTex, gasTex, axesTex);

                try {
                    drawable.getContext().release();
                } catch (GLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayContext(SGNode sgRoot, OctreeNode octreeRoot, Texture2D starTex, Texture2D starHaloTex,
            Texture2D gasTex, Texture2D axesTex) {
        GL3 gl = GLContext.getCurrentGL().getGL3();

        sgRoot.init(gl);
        octreeRoot.init(gl);

        int width = GLContext.getCurrent().getGLDrawable().getWidth();
        int height = GLContext.getCurrent().getGLDrawable().getHeight();
        float aspect = (float) width / (float) height;

        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        Point4 eye = new Point4((float) (radius * Math.sin(ftheta) * Math.cos(phi)),
                (float) (radius * Math.sin(ftheta) * Math.sin(phi)), (float) (radius * Math.cos(ftheta)), 1.0f);
        Point4 at = new Point4(0.0f, 0.0f, 0.0f, 1.0f);
        Vec4 up = new Vec4(0.0f, 1.0f, 0.0f, 0.0f);

        Mat4 mv = MatrixMath.lookAt(eye, at, up);
        mv = mv.mul(MatrixMath.translate(translation));
        mv = mv.mul(MatrixMath.rotationX(rotation.get(0)));
        mv = mv.mul(MatrixMath.rotationY(rotation.get(1)));

        Mat3 n = new Mat3();
        Mat4 p = MatrixMath.perspective(fovy, aspect, zNear, zFar);

        // Vertex shader variables
        loader.setUniformMatrix("NormalMatrix", n);
        loader.setUniformMatrix("PMatrix", p);
        loader.setUniformMatrix("SMatrix", MatrixMath.scale(1));

        renderScene(gl, mv, sgRoot, octreeRoot, starHaloTex, starTex, gasTex, axesTex);

        if (post_process) {
            renderTexturesToScreen(gl, width, height, starHaloTex, starTex, gasTex, axesTex);
        }

    }

    private void renderScene(GL3 gl, Mat4 mv, SGNode sgRoot, OctreeNode octreeRoot, Texture2D starHaloTex,
            Texture2D starTex, Texture2D gasTex, Texture2D axesTex) {
        try {
            if (post_process) {
                pplShader.setUniformVector("LightPos", lightPos);
                pplShader.setUniform("Shininess", shininess);

                pplShader.setUniformMatrix("SMatrix", MatrixMath.scale(2));
                pplShader.setUniform("StarDrawMode", 1);

                sgRoot.draw(gl, pplShader, mv);

                renderToTexture(gl, starHaloTex);
                starBlur(gl, starHaloTex, FSQ_blur, Settings.getStarHaloBlurType(levelOfDetail),
                        Settings.getStarHaloBlurSize(levelOfDetail));

                blur(gl, starHaloTex, FSQ_blur, Settings.getStarHaloBlurPasses(levelOfDetail),
                        Settings.getStarHaloBlurType(levelOfDetail), Settings.getStarHaloBlurSize(levelOfDetail));
            }

            gl.glDisable(GL3.GL_DEPTH_TEST);

            octreeRoot.draw(gl, gasShader, mv);

            if (post_process) {
                renderToTexture(gl, gasTex);
                if (snapshotting) {
                    blur(gl, gasTex, FSQ_blur, Settings.getSnapshotGasBlurPasses(), Settings.getSnapshotGasBlurType(),
                            Settings.getSnapshotGasBlurSize());
                } else {
                    blur(gl, gasTex, FSQ_blur, Settings.getGasBlurPasses(levelOfDetail),
                            Settings.getGasBlurType(levelOfDetail), Settings.getGasBlurSize(levelOfDetail));
                }
            }

            gl.glEnable(GL3.GL_DEPTH_TEST);

            if (snapshotting) {
                noiseTex.use(gl);
                animatedTurbulenceShader.setUniform("Noise", noiseTex.getMultitexNumber());

                animatedTurbulenceShader.setUniformMatrix("SMatrix", MatrixMath.scale(1));
                animatedTurbulenceShader.setUniform("StarDrawMode", 0);

                sgRoot.draw(gl, animatedTurbulenceShader, mv);
            } else {
                pplShader.setUniformVector("LightPos", lightPos);
                pplShader.setUniform("Shininess", shininess);

                pplShader.setUniformMatrix("SMatrix", MatrixMath.scale(1));
                pplShader.setUniform("StarDrawMode", 0);

                sgRoot.draw(gl, pplShader, mv);
            }

            if (post_process) {
                renderToTexture(gl, starTex);
            }

            if (axes) {
                axesShader.use(gl);
                xAxis.draw(gl, axesShader, mv);
                yAxis.draw(gl, axesShader, mv);
                zAxis.draw(gl, axesShader, mv);
            } else {
                gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
            }

            if (post_process) {
                renderToTexture(gl, axesTex);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void renderTexturesToScreen(GL3 gl, int width, int height, Texture2D starHaloTex, Texture2D starTex,
            Texture2D gasTex, Texture2D axesTex) {
        postprocessShader.setUniform("axesTexture", axesTex.getMultitexNumber());
        postprocessShader.setUniform("gasTexture", gasTex.getMultitexNumber());
        postprocessShader.setUniform("starTexture", starTex.getMultitexNumber());
        postprocessShader.setUniform("starHaloTexture", starHaloTex.getMultitexNumber());

        postprocessShader.setUniform("starBrightness", Settings.getPostprocessingStarBrightness());
        postprocessShader.setUniform("starHaloBrightness", Settings.getPostprocessingStarHaloBrightness());
        postprocessShader.setUniform("gasBrightness", Settings.getPostprocessingGasBrightness());
        postprocessShader.setUniform("axesBrightness", Settings.getPostprocessingAxesBrightness());
        postprocessShader.setUniform("overallBrightness", Settings.getPostprocessingOverallBrightness());

        postprocessShader.setUniformMatrix("PMatrix", new Mat4());
        postprocessShader.setUniform("scrWidth", width);
        postprocessShader.setUniform("scrHeight", height);

        try {
            postprocessShader.use(gl);
            FSQ_postprocess.draw(gl, postprocessShader, new Mat4());
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void blur(GL3 gl, Texture2D target, Model fullScreenQuad, int passes, int blurType, float blurSize) {
        gaussianBlurShader.setUniform("Texture", target.getMultitexNumber());
        gaussianBlurShader.setUniformMatrix("PMatrix", new Mat4());
        gaussianBlurShader.setUniform("blurType", blurType);
        gaussianBlurShader.setUniform("blurSize", blurSize);
        gaussianBlurShader.setUniform("scrWidth", target.getWidth());
        gaussianBlurShader.setUniform("scrHeight", target.getHeight());
        gaussianBlurShader.setUniform("Alpha", 1f);

        try {
            gaussianBlurShader.use(gl);

            for (int i = 0; i < passes; i++) {
                gaussianBlurShader.setUniform("blurDirection", 0);
                fullScreenQuad.draw(gl, new Mat4());
                renderToTexture(gl, target);

                gaussianBlurShader.setUniform("blurDirection", 1);
                fullScreenQuad.draw(gl, new Mat4());
                renderToTexture(gl, target);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void starBlur(GL3 gl, Texture2D target, Model fullScreenQuad, int blurType, float blurSize) {
        gaussianBlurShader.setUniform("Texture", target.getMultitexNumber());
        gaussianBlurShader.setUniformMatrix("PMatrix", new Mat4());

        gaussianBlurShader.setUniform("scrWidth", target.getWidth());
        gaussianBlurShader.setUniform("scrHeight", target.getHeight());

        try {
            gaussianBlurShader.use(gl);

            gaussianBlurShader.setUniform("blurType", Settings.getStarShapeBlurType());
            gaussianBlurShader.setUniform("Alpha", Settings.getStarShapeAlpha());
            gaussianBlurShader.setUniform("Sigma", Settings.getStarShapeSigma());
            gaussianBlurShader.setUniform("NumPixelsPerSide", Settings.getStarShapeBlurfilterSize());
            gaussianBlurShader.setUniform("blurSize", Settings.getStarShapeBlurSize());

            gaussianBlurShader.setUniform("blurDirection", 0);
            fullScreenQuad.draw(gl, new Mat4());
            gaussianBlurShader.setUniform("blurDirection", 1);
            fullScreenQuad.draw(gl, new Mat4());
            renderToTexture(gl, target);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void renderToTexture(GL3 gl, Texture2D target) {
        try {
            target.use(gl);
            gl.glCopyTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA, 0, 0, target.getWidth(), target.getHeight(), 0);
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, w, h);

        canvasWidth = w;
        canvasHeight = h;

        starTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE1);
        starTex.init(gl);

        starHaloTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE2);
        starHaloTex.init(gl);

        gasTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE3);
        gasTex.init(gl);

        axesTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE4);
        axesTex.init(gl);

        intermediateTex = new PostProcessTexture(canvasWidth, canvasHeight, GL3.GL_TEXTURE9);
        intermediateTex.init(gl);
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        timer.delete(gl);

        noiseTex.delete(gl);

        starTex.delete(gl);
        starHaloTex.delete(gl);
        gasTex.delete(gl);
        axesTex.delete(gl);
        intermediateTex.delete(gl);

        starTex4k.delete(gl);
        starHaloTex4k.delete(gl);
        gasTex4k.delete(gl);
        axesTex4k.delete(gl);
        intermediateTex4k.delete(gl);

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

    public Vec3 getRotation() {
        return rotation;
    }

    public static octants getCurrentOctant() {
        return current_view_octant;
    }

    public void startAnimation(Hdf5TimedPlayer timer) {
        this.timer = timer;
        timer.init();
        new Thread(timer).start();
        setTimerInitialized(true);
    }

    public void stopAnimation() {
        if (isTimerInitialized()) {
            timer.close();
        }
        setTimerInitialized(false);
    }

    public static void setPostprocess(boolean newSetting) {
        post_process = newSetting;
    }

    public static boolean isPostprocess() {
        return post_process;
    }

    public static void setLOD(int newSetting) {
        levelOfDetail = newSetting;
    }

    public static int getLOD() {
        return levelOfDetail;
    }

    public static void setAxes(boolean newSetting) {
        axes = newSetting;
    }

    public void makeSnapshot(String fileName) {
        synchronized (timer) {
            synchronized (this) {
                snapshotting = true;
                // Hdf5TimedPlayer.setState(states.SNAPSHOTTING);

                axes = false;

                try {
                    int status = offScreenContext.makeCurrent();
                    if (status != GLContext.CONTEXT_CURRENT && status != GLContext.CONTEXT_CURRENT_NEW) {
                        System.err.println("Error swapping context to offscreen.");
                    }
                } catch (GLException e) {
                    System.err.println("Exception while swapping context to offscreen.");
                    e.printStackTrace();
                }

                int width = offScreenContext.getGLDrawable().getWidth();
                int height = offScreenContext.getGLDrawable().getHeight();

                GL3 gl = offScreenContext.getGL().getGL3();
                gl.glViewport(0, 0, width, height);

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

                gl.glClearColor(0f, 0f, 0f, 0f);

                starTex4k.init(gl);
                starHaloTex4k.init(gl);
                gasTex4k.init(gl);
                axesTex4k.init(gl);

                displayContext(sgRoot, octreeRoot, starTex4k, starHaloTex4k, gasTex4k, axesTex4k);

                Picture p = new Picture(width, height);
                
                gl.glFinish();
                
                p.copyFrameBufferToFile(panel.getPath(), fileName);

                try {
                    offScreenContext.release();
                } catch (GLException e) {
                    e.printStackTrace();
                }

                axes = true;
                snapshotting = false;
            }
        }
    }

    public static void setWaittime(long newWaittime) {
        GLWindow.waittime = newWaittime;
    }

    public static long getWaittime() {
        return waittime;
    }

    public static boolean isExaggerate_colors() {
        return exaggerate_colors;
    }

    public static void setExaggerate_colors(boolean newSetting) {
        exaggerate_colors = newSetting;
    }

    public boolean isTimerInitialized() {
        return timerInitialized;
    }

    public void setTimerInitialized(boolean timerInitialized) {
        this.timerInitialized = timerInitialized;
    }
}
