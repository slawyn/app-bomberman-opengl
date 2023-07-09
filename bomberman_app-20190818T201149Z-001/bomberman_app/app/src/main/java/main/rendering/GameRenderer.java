package main.rendering;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import main.Constants;
import main.Globals;
import main.Logger;
import main.game.SceneManager;
import main.rendering.animation.Animation;
import main.rendering.display.DisplayManager;
import main.rendering.display.Loadable;
import main.rendering.display.Loader;
import main.rendering.elements.DebugElement;
import main.rendering.animation.Layer;
import main.rendering.color.ColorShaderProgram;
import main.rendering.elements.RenderElement;
import main.rendering.texture.TextureLoader;
import main.rendering.texture.TextureShaderProgram;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;
import static main.Constants.NUMBER_OF_TEXTURES;
import static main.rendering.display.DisplayManager.mGamePortHeight;
import static main.rendering.display.DisplayManager.mGamePortWidth;

/* View, used as main game loop */
public class GameRenderer implements GLSurfaceView.Renderer
{
    private final String TAG = "GameRenderer";
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int[] mTextures;       // holds mTextures together with references to the DRAWABLES
    private float[] projectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mModelViewProjectionMatrix = new float[16];

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;
    private DisplayManager mDisplayManager;
    private float mFpsCounter;
    private int mFpsUpdateFrequency;
    private int mFpsDecimalCurrent0;
    private int mFpsDecimalCurrent1;
    private int mResourcesLoaded;
    private int mTextureIndex;
    private long mFrameStartTime;
    private long mFrameLastTime;
    private float mTotalTime;
    private Resources mResources;

    public GameRenderer(Resources res, int widthpixels, int heightpixels)
    {
        mResources = res;
        mDisplayManager = new DisplayManager();
        mTextures = new int[NUMBER_OF_TEXTURES];
        mMeasuredWidth = widthpixels;
        mMeasuredHeight = heightpixels;
        rescale();
    }

    public DisplayManager getDisplayManager()
    {
        return mDisplayManager;
    }


    /* Calculate offsets
      36x36 (0.75x) for low-density (ldpi)
      48x48 (1.0x baseline) for medium-density (mdpi)
      72x72 (1.5x) for high-density (hdpi)
      96x96 (2.0x) for extra-high-density (xhdpi)
      144x144 (3.0x) for extra-extra-high-density (xxhdpi)
      192x192 (4.0x) for extra-extra-extra-high-density (xxxhdpi)
  */
    private void rescale()
    {
        int newScreenWidth, newScreenHeight;
        float ratioPhysicScreen = (float) mMeasuredWidth / (float) mMeasuredHeight;
        float ratioWanted = mGamePortWidth / (float)mGamePortHeight;

        if(ratioWanted > ratioPhysicScreen)
        {
            newScreenWidth = (mMeasuredWidth);
            newScreenHeight = (int) (mMeasuredWidth * (mGamePortHeight) / (mGamePortWidth));
        }
        else
        {
            newScreenWidth = (int) (mMeasuredHeight / (mGamePortHeight) * (mGamePortWidth));
            newScreenHeight = (mMeasuredHeight);
        }


        mDisplayManager.load(newScreenWidth, newScreenHeight,
                (float) newScreenWidth / mGamePortWidth,
                (mMeasuredWidth - newScreenWidth) / 2,
                (mMeasuredHeight - newScreenHeight) / 2);
    }


    public void loadTextures()
    {
        /* Loadables are fed from the main thread */
        Loader loader = mDisplayManager.getLoader();
        if(loader.hasLoadables())
        {
            Loadable loadable = loader.getLoadable();
            Bitmap bigBitmap = BitmapFactory.decodeResource(mResources, loadable.resourceid, loader.getLoaderOptions());

            int width = bigBitmap.getWidth() / loadable.numberoftextures;
            int height = bigBitmap.getHeight();

            // Loader texture in OPENGL Memory
            for(int c = 0; c < loadable.numberoftextures; c++)
            {
                Bitmap bitmap = Bitmap.createBitmap(bigBitmap, width * c, 0, width, height);
                mTextures[loadable.texturebaseindex + c] = TextureLoader.loadTexture(bitmap);
                bitmap.recycle();
                ++mTextureIndex;
            }

            bigBitmap.recycle();
            ++mResourcesLoaded;
        }
    }


    public void prepareMatrix(float[] translation){
        mModelMatrix[0] = 1.0f;
        mModelMatrix[1] = 0;
        mModelMatrix[2] = 0;
        mModelMatrix[3] = 0;
        mModelMatrix[4] = 0;
        mModelMatrix[5] = 1.0f;
        mModelMatrix[6] = 0;
        mModelMatrix[7] = 0;
        mModelMatrix[8] = 0;
        mModelMatrix[9] = 0;
        mModelMatrix[10] = 1.0f;
        mModelMatrix[11] = 0;
        mModelMatrix[12] = translation[0];
        mModelMatrix[13] = translation[1];
        mModelMatrix[14] = 0;
        mModelMatrix[15] = 1.0f;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        glClearColor(1f, 0.0f, 0.0f, 1.0f);
        glViewport(0, 0, mMeasuredWidth, mMeasuredHeight);
        textureProgram = new TextureShaderProgram(mResources);
        colorProgram = new ColorShaderProgram(mResources);

        /*
        glDepthFunc(GL_LESS);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        */

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);//GL_ONE_MINUS_SRC_ALPHA);
        Logger.log(Logger.INFO, TAG, "GPU SURFACE CREATED");

        /* Initial time */
        getRenderingDelta(System.nanoTime());
    }

    private float getRenderingDelta(long time)
    {
        mFrameStartTime = time;
        float deltatime = (mFrameStartTime - mFrameLastTime) / 1000000.0f;
        mFrameLastTime = mFrameStartTime;
        return deltatime;
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        long timetest;
        int previousvertex = -1;
        int currentvertex = -1;
        int previoustexture = -1;
        int currenttexture;
        float dt = getRenderingDelta(System.nanoTime());

        loadTextures();
        glClear(GL_COLOR_BUFFER_BIT);

        /* Update and DRAW INFO FPS Counter*/
        mTotalTime += dt;
        if((mFpsUpdateFrequency+=1) == 10)
        {
            mFpsCounter = 1000.0f/(mTotalTime/mFpsUpdateFrequency);
            mTotalTime = 0;
            mFpsUpdateFrequency = 0;

            /**/
            mFpsDecimalCurrent0 = (int)mFpsCounter % 10;
            mFpsDecimalCurrent1 = (int)mFpsCounter / 10 % 10;
        }


        /* Update Game */
        textureProgram.useProgram();
        RenderElement.updateelapsed(dt);
        Vector<Layer> layers = mDisplayManager.getLayers();
        {
            int sx = layers.size() - 1;
            for (int idx = 0; idx < sx; idx++) {
                Layer layer = layers.get(idx);
                RenderElement[] sorted = layer.getSortedArray();
                int idz = layer.getSortedArraySize();
                while (--idz >= 0) {
                    RenderElement ro = sorted[idz];
                    ro.updateAnimationsGpu(dt);
                    Vector<Animation> animations = ro.getAnimations();

                    int animsz = animations.size();
                    currentvertex = ro.mOptimizationObjectType;
                    for (int j = 0; j < animsz; j++)
                    {
                        Animation obj = animations.get(j);
                        prepareMatrix(obj.getTranslation());
                        multiplyMM(mModelViewProjectionMatrix, 0, projectionMatrix, 0, mModelMatrix, 0);

                        /* Reuse bound texture */
                        currenttexture = obj.getCurrentTexture();
                        if (currenttexture != previoustexture) {
                            textureProgram.setUniforms(mModelViewProjectionMatrix, mTextures[currenttexture], GL_TEXTURE0);
                            previoustexture = currenttexture;
                        } else {
                            textureProgram.setUniformsFast(mModelViewProjectionMatrix);
                        }

                        if (previousvertex != currentvertex) {
                            textureProgram.bindData(mDisplayManager.getQuad(currentvertex));
                            previousvertex = currentvertex;
                        }

                        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
                    }
                }
            }
        }

        /* Draw fps only when all resources have been loaded */
        if(mResourcesLoaded == mDisplayManager.getResourceCount())
        {
            textureProgram.bindData(mDisplayManager.getQuad(SceneManager.SOBJ_FPS_COUNTER));
            if(mFpsDecimalCurrent1 > 0)
            {
                setIdentityM(mModelMatrix, 0);
                translateM(mModelMatrix, 0, 10, 30, 0);
                multiplyMM(mModelViewProjectionMatrix, 0, projectionMatrix, 0, mModelMatrix, 0);
                textureProgram.setUniforms(mModelViewProjectionMatrix, mTextures[mTextureIndex - 10 + mFpsDecimalCurrent1], GL_TEXTURE0);
                glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
            }

            setIdentityM(mModelMatrix, 0);
            translateM(mModelMatrix, 0, 100, 30, 0);
            multiplyMM(mModelViewProjectionMatrix, 0, projectionMatrix, 0, mModelMatrix, 0);
            textureProgram.setUniforms(mModelViewProjectionMatrix, mTextures[mTextureIndex - 10 + mFpsDecimalCurrent0], GL_TEXTURE0);

            glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        }

        /* DRAW HITBOXES */
        Layer debuglayer = layers.get(4);
        RenderElement[] sorted = debuglayer.getSortedArray();
        int idz = debuglayer.getSortedArraySize();

        if(idz>0)
        {
            colorProgram.useProgram();
        }

        while(--idz >= 0)
        {
            RenderElement ro = sorted[idz];
            DebugElement debugobj = (DebugElement) ro;

            float[] offset = debugobj.getRenderObjectPosition();
            int idx = debugobj.getNumberOfObjects();
            while(--idx>=0)
            {
                setIdentityM(mModelMatrix,0);
                translateM(mModelMatrix, 0, mDisplayManager.gamePortOffsetXY[0] +offset[0], mDisplayManager.gamePortOffsetXY[1] +offset[1], 0);
                multiplyMM(mModelViewProjectionMatrix, 0, projectionMatrix, 0, mModelMatrix, 0);

                colorProgram.setUniforms(mModelViewProjectionMatrix);
                debugobj.bindDebugData(idx, colorProgram);
                glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
            }
        }


        /* @Measure exec time */
        if(( timetest = System.nanoTime() - mFrameStartTime) > Globals.mDebugRendererTimeMax)
            Globals.mDebugRendererTimeMax = timetest;
        else if(timetest < Globals.mDebugRendererTimeMin)
            Globals.mDebugRendererTimeMin = timetest;
    }

    public int getLoadedResourcesCount()
    {
        return mResourcesLoaded;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        orthoM(projectionMatrix, 0, 0, mMeasuredWidth, mMeasuredHeight, 0f, -1f, 1f);
        //orthoM(projectionMatrix, 0, -1 , 1, -1f, 1f, -1f, 1f);
    }
}
