package main.rendering;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import main.Constants;
import main.Globals;
import main.Logger;
import main.rendering.animation.AnimatedObject;
import main.rendering.animation.DisplayManager;
import main.rendering.animation.DebugObject;
import main.rendering.animation.Layer;
import main.rendering.animation.RenderObject;
import main.rendering.color.ColorShaderProgram;
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
import static main.game.GameStateBuffer.OBJ_CRATE;
import static main.game.GameStateBuffer.OBJ_PLAYR;


//////////////////////////////////
// View, used as main game loop //
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
    private int mFpsCounter;
    private int mFpsDecimalCurrent0;
    private int mFpsDecimalCurrent1;
    private int mResourcesLoaded;
    private int mTextureIndex;
    private long mFrameStart;
    private long mLastFrame;
    private float mTotalTime;
    private Resources mResources;

    public GameRenderer(Resources res, int widthpixels, int heightpixels)
    {
        mResources = res;
        mDisplayManager = new DisplayManager();
        mTextures = new int[300];
        mMeasuredWidth = widthpixels;
        mMeasuredHeight = heightpixels;
        rescale();
    }


    public DisplayManager getmDisplayManager()
    {
        return mDisplayManager;
    }


    /*
      36x36 (0.75x) for low-density (ldpi)
      48x48 (1.0x baseline) for medium-density (mdpi)
      72x72 (1.5x) for high-density (hdpi)
      96x96 (2.0x) for extra-high-density (xhdpi)
      144x144 (3.0x) for extra-extra-high-density (xxhdpi)
      192x192 (4.0x) for extra-extra-extra-high-density (xxxhdpi)
  */
    // Calculate gameport offset
    private void rescale()
    {
        int newScreenWidth, newScreenHeight;


        float ratioPhysicScreen = (float) mMeasuredWidth / (float) mMeasuredHeight;
        float ratioWanted = Constants.GAME_WIDTH / Constants.GAME_HEIGHT;

        if(ratioWanted > ratioPhysicScreen)
        {
            newScreenWidth = (mMeasuredWidth);
            newScreenHeight = (int) (mMeasuredWidth * (Constants.GAME_HEIGHT) / (Constants.GAME_WIDTH));
        } else
        {
            newScreenWidth = (int) (mMeasuredHeight / (Constants.GAME_HEIGHT) * (Constants.GAME_WIDTH));
            newScreenHeight = (mMeasuredHeight);
        }

        mDisplayManager.setPrimaries(newScreenWidth, newScreenHeight, (float) newScreenWidth / Constants.GAME_WIDTH, (mMeasuredWidth - newScreenWidth) / 2, (mMeasuredHeight - newScreenHeight) / 2);
        mDisplayManager.createQuads();
        mDisplayManager.recalculateOffsets();
    }


    public void loadTextures()
    {
        if(mDisplayManager.hasLoadables())
        {
            int[] data = mDisplayManager.getLoadable();
            BitmapFactory.Options opts = mDisplayManager.getLoaderOptions();
            Bitmap bigBitmap = BitmapFactory.decodeResource(mResources, data[2], opts);

            int width = bigBitmap.getWidth() / data[1];
            int height = bigBitmap.getHeight();

            // Loader texture in OPENGL Memory
            for(int c = 0; c < data[1]; c++)
            {
                Bitmap bitmap = Bitmap.createBitmap(bigBitmap, width * c, 0, width, height);
                mTextures[data[0] + c] = TextureLoader.loadTexture(bitmap);
                bitmap.recycle();
                ++mTextureIndex;
            }

            bigBitmap.recycle();
            mResourcesLoaded++;
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

        mLastFrame = System.nanoTime();
    }


    @Override
    public void onDrawFrame(GL10 unused)
    {
        long timetest;
        int previousvertex = -1,currentvertex,previoustexture = -1,currenttexture;
        float deltatime;

        mFrameStart = System.nanoTime();
        deltatime = (mFrameStart - mLastFrame) / 1000000.0f;
        mLastFrame = mFrameStart;
        mTotalTime += deltatime;

        loadTextures();

        glClear(GL_COLOR_BUFFER_BIT);
        textureProgram.useProgram();


        RenderObject.updateelapsed(deltatime);

        Vector<Layer> layers = mDisplayManager.getLayers();
        int sx = layers.size() - 1;
        for(int idx = 0; idx < sx; idx++)
        {
            Layer layer = layers.get(idx);
            RenderObject[] sorted = layer.getSortedArray();
            int sz = layer.getSortedArraySize();
            int idz = sz - 1;
            while(idz >= 0)
            {
                RenderObject ro = sorted[idz];
                ro.updateAnimations(deltatime);
                Vector<AnimatedObject> animations = ro.getAnimations();

                int animsz = animations.size();
                currentvertex = ro.mOptimizationObjectType;
                for(int j = 0; j < animsz; j++)
                {
                    AnimatedObject obj = animations.get(j);

                    prepareMatrix(obj.getTranslation());
                    multiplyMM(mModelViewProjectionMatrix, 0, projectionMatrix, 0, mModelMatrix, 0);

                    currenttexture = obj.getCurrentTexture();
                    if(currenttexture!=previoustexture){
                        textureProgram.setUniforms(mModelViewProjectionMatrix, mTextures[currenttexture], GL_TEXTURE0);
                        previoustexture = currenttexture;
                    }else{
                        textureProgram.setUniformsFast(mModelViewProjectionMatrix);
                    }

                    if(previousvertex != currentvertex)
                    {
                        textureProgram.bindData(mDisplayManager.getQuad(currentvertex));
                        previousvertex = currentvertex;
                    }

                    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
                }

                --idz;
            }
        }
        // DRAW INFO
        /* FPS Counter*/
        if(mResourcesLoaded == mDisplayManager.getResourceCount())
        {
            textureProgram.bindData(mDisplayManager.getQuad(OBJ_PLAYR));
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

        // DRAW HITBOXES
        if(Constants.DEBUG_DRAW_HITBOXES)
        {
            colorProgram.useProgram();
            Layer debuglayer = layers.get(4);
            RenderObject[] sorted = debuglayer.getSortedArray();
            int sz = debuglayer.getSortedArraySize();
            int idz = sz - 1;
            while(idz >= 0)
            {

                RenderObject ro = sorted[idz];
                DebugObject debugobj = (DebugObject) ro;

                float[] offset = debugobj.getRenderObjectPosition();
                for(int idx = 0; idx < debugobj.getNumberOfObjects(); idx++)
                {
                    setIdentityM(mModelMatrix,0);
                    translateM(mModelMatrix, 0, mDisplayManager.gamePortXOffset +offset[0], mDisplayManager.gamePortYOffset +offset[1], 0);
                    multiplyMM(mModelViewProjectionMatrix, 0, projectionMatrix, 0, mModelMatrix, 0);

                    colorProgram.setUniforms(mModelViewProjectionMatrix);
                    debugobj.bindDebugData(idx, colorProgram);
                    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
                }
                --idz;
            }
        }


        // FPS Counter
        mFpsCounter++;
        if(mTotalTime >= 1000.0)
        {
            mTotalTime = 0;//-=1000;
            mFpsDecimalCurrent0 = mFpsCounter % 10;
            mFpsDecimalCurrent1 = mFpsCounter / 10 % 10;
            mFpsCounter = 0;
        }

        timetest = System.nanoTime() - mFrameStart;
        if(timetest > Globals.mDebugRendererTimeMax)
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
