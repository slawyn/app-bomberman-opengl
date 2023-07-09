package main.rendering.display;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import main.game.events.Events;
import main.nativeclasses.GameLogic;
import main.game.SceneManager;
import main.nativeclasses.GameElement;
import main.game.sceneobjects.SceneObject;
import main.rendering.elements.RenderElement;
import main.rendering.VertexArray;
import main.rendering.animation.Layer;

import static main.Constants.*;
import static main.Constants.DEBUG_DRAW_HITBOXES;
import static main.game.SceneManager.SOBJ_TIMER;
import static main.game.SceneManager.SOBJ_BACKGROUND;
import static main.game.SceneManager.SOBJ_BACKGROUND_LOADING;
import static main.game.SceneManager.SOBJ_BUTTON;
import static main.game.SceneManager.SOBJ_TOUCH;
import static main.game.SceneManager.SOBJ_FPS_COUNTER;
import static main.nativeclasses.GameElement.OBJ_BLOCK;
import static main.nativeclasses.GameElement.OBJ_BOMB;
import static main.nativeclasses.GameElement.OBJ_CRATE;
import static main.nativeclasses.GameElement.OBJ_EXPLN;
import static main.nativeclasses.GameElement.OBJ_ITEM;
import static main.nativeclasses.GameElement.OBJ_PLAYR;

public class DisplayManager
{
    private final String TAG = "DisplayManager";

    public final static float SCALE_FACTOR = 1.0f;

    public static float mScaleFactor = SCALE_FACTOR;
    public final static float GAME_HEIGHT= 1080;
    public final static float GAME_WIDTH = 1920;
    public static int mGamePortWidth = (int)GAME_WIDTH;
    public static int mGamePortHeight = (int)GAME_HEIGHT;
    public static int[] gamePortOffsetXY = {0, 0};
    public static int[] gameBombOffsetXY      = {-136 / 2, -136/2};
    public static int[] gameObjectOffsetXY    = {-136 / 2, (94 - 136)-94/2};
    public static int[] gameExplosionOffsetXY = {0, 0 + ((94 - 100))};
    public static int[] gamePlayerOffsetXY = {-180/2, -180/2 - 40};

    // Scene objects
    public static int[] gameTouchOffsetXY  = {(-(300 / 2)),(-(300 / 2))};
    public static int[] gameLoadingOffsetXY = {(mGamePortWidth) / 2 - 300, (mGamePortHeight) / 2 - 300};
    public static int[] gameButtonOffsetXY = {gamePortOffsetXY[0],gamePortOffsetXY[1]};
    public static int[] gameTimerOffsetXY = {mGamePortWidth - 400, 20};

    public static int[] gameZeroOffsetXY = {0, 0};
    public static int[][] gameTimer2ndOffsets = {{0,0},{78,0},{200,0},{278,0}};

    private SparseArray<VertexArray> mVertexData;

    private int mTotalNumberOfRenderObjects;

    public RenderElement[] mFreeRenderElements;


    public LoaderConfig mLoaderConfig;
    private ConcurrentLinkedQueue<int[]> mLoadables;
    private SparseArray<RenderElement> mRos;
    private AnimationManager mAnimationManager;

    /* Display manager controls renderelements based on game object states*/
    public DisplayManager()
    {
        mAnimationManager = new AnimationManager();

        /* Render Elements hold the base offset and animations
         *  :Translation is updated on game thread, and is interpolated
         *  :Animation timings are updated on the GPU thread*/
        mTotalNumberOfRenderObjects = NUMBER_OF_RENDER_OBJECTS;

        mFreeRenderElements = new RenderElement[mTotalNumberOfRenderObjects];
        for(int idx = 0; idx < NUMBER_OF_RENDER_OBJECTS; idx++)
        {
            mFreeRenderElements[idx] = new RenderElement();
        }

        mRos = new SparseArray<>(NUMBER_OF_RENDER_OBJECTS);
        loadAnimations();
    }

    public BitmapFactory.Options getLoaderOptions()
    {
        return mLoaderConfig.opts;
    }

    public int getResourceCount()
    {
        return mAnimationManager.getDrawableCount();
    }

    public void setPrimaries(int width, int height, float scalefactor, int portx, int porty)
    {
        mGamePortWidth = width;
        mGamePortHeight = height;
        mScaleFactor = scalefactor;
        gamePortOffsetXY[0] = portx;
        gamePortOffsetXY[1] = porty;
    }

    public Vector<Layer> getLayers()
    {
        return mAnimationManager.getLayers();
    }


    public RenderElement getRenderObject(GameElement go, boolean drawhitbox)
    {
        int unique = go.getUniqeueID();
        RenderElement ro = mRos.get(unique);


        if (ro == null)
        {
            ro = getFreeRenderObjectFromPool();
            int laynum = 1;

            /* Save previous movement state */
            ro.init(go.mObjectType,go.mObjectSubtype, go.getState(), unique);
            mAnimationManager.createAnimatedObject(ro, laynum);
            mRos.put(unique, ro);

            /* Add a visible hitbox */
            if(drawhitbox)
            {
                ro.addDebugObject(go, mScaleFactor);
                mAnimationManager.getLayers().get(4).addRenderObjectToLayer(ro.mDebugObject);
            }
        }
        return ro;
    }

    public RenderElement getFreeRenderObjectFromPool()
    {
        RenderElement ro = mFreeRenderElements[--mTotalNumberOfRenderObjects];
        return ro;
    }

    public void returnRenderObjectToPool(RenderElement ro)
    {
        if(ro.mDebugObject != null)
        {
            ro.mDebugObject.removeFromRenderingGpu = true;
            /* TODO: destroy object */
        }

        /* Return RenderElement to pool*/
        ro.removeFromRenderingGpu = true;
        mRos.remove(ro.getUniqueId());
        mFreeRenderElements[mTotalNumberOfRenderObjects++] = ro;

        /* Return all animations to pool */
        mAnimationManager.returnAnimationsToPool(ro.getUsedAnimatedObjects());
    }

    public void updateGameManager(GameLogic gamemanager)
    {
        Events goupdates = gamemanager.getUpdateEvents();
        Events goremovals = gamemanager.getRemovalEvents();

        /* Update/Create Renderobjects */
        int sz = goupdates.getCount();
        while (--sz >= 0)
        {
            GameElement go = (GameElement) goupdates.getEvent(sz);
            RenderElement ro = getRenderObject(go, DEBUG_DRAW_HITBOXES);

            /* Translate and sort after 'Z' all animations that are attached to the render object */
            long pos[] = go.getPositionXY();
            ro.setTranslation(pos[0] * mScaleFactor,
                    pos[1] * mScaleFactor);
            ro.updateSortCriteria(go.getZ());
            mAnimationManager.updateAnimatedObject(ro, go.getState());
        }

        sz = goremovals.getCount();
        while (--sz >= 0)
        {
            GameElement go = (GameElement) goupdates.getEvent(sz);
            RenderElement ro = getRenderObject(go, DEBUG_DRAW_HITBOXES);
            returnRenderObjectToPool(ro);
        }
        goremovals.resetEvents();
        goupdates.resetEvents();
    }
    public void updateSceneManager(SceneManager manager)
    {
        // Updates RenderObjects from SceneManager
        Events soupdates = manager.getUpdateEvents();
        Events soremovals = manager.getRemovalEvents();
        int sz = soupdates.getCount();
        while (--sz >= 0)
        {
            SceneObject so = (SceneObject) soupdates.getEvent(sz);
            RenderElement ro = so.ro;
            if(ro == null)
            {
                ro = getFreeRenderObjectFromPool();
                so.ro = ro;

                int laynum = so.mLayer;
                ro.init(so.mObjectType, so.mObjectSubtype, so.mState, so.mObjectID);
                ro.setAdditionalParams(so.getUpdatedAdditionals());
                mAnimationManager.createAnimatedObject(ro, laynum);
            }

            mAnimationManager.updateAnimatedObject(ro, so.mState);
            ro.setTranslation(so.mPositionX, so.mPositionY);
        }

        // Remove RendersObjects from SceneManager
        sz = soremovals.getCount();
        while (--sz >= 0)
        {
            SceneObject so = (SceneObject) soremovals.getEvent(sz);
            RenderElement ro = so.ro;
            returnRenderObjectToPool(ro);
        }
        soupdates.resetEvents();
        soremovals.resetEvents();
    }

    public void updateRenderObjectsForGPU(GameLogic gamemanager, SceneManager scenemanager)
    {
        updateGameManager(gamemanager);
        updateSceneManager(scenemanager);
        RenderElement.latch();
    }

    /* Load animations into OPENGL Space
    * Textures are loaded on the opengl thread */
    private void loadAnimations()
    {
        mLoadables = new ConcurrentLinkedQueue<>();
        mAnimationManager.loadAnimations(mLoadables);
    }

    public boolean hasLoadables()
    {
        return !mLoadables.isEmpty();
    }

    public int[] getLoadable()
    {
        return mLoadables.remove();
    }

    public VertexArray getQuad(int type)
    {
        return mVertexData.get(type);
    }

    public void recalculateOffsets()
    {
        mLoaderConfig = new LoaderConfig(mScaleFactor);
        gameBombOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameBombOffsetXY[0] * mScaleFactor);
        gameBombOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameBombOffsetXY[1] * mScaleFactor);

        gamePlayerOffsetXY[0] = gamePortOffsetXY[0] + (int)(gamePlayerOffsetXY[0]* mScaleFactor);
        gamePlayerOffsetXY[1] = gamePortOffsetXY[1] + (int)(gamePlayerOffsetXY[1]* mScaleFactor);

        gameExplosionOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameExplosionOffsetXY[0] * mScaleFactor);
        gameExplosionOffsetXY[0] = gamePortOffsetXY[1] + (int) (gameExplosionOffsetXY[1] * mScaleFactor);

        gameObjectOffsetXY[0] = gamePortOffsetXY[0]  + (int) (gameObjectOffsetXY[0] * mScaleFactor);
        gameObjectOffsetXY[1] = (gamePortOffsetXY[1] + (int) (gameObjectOffsetXY[1] * mScaleFactor));

        gameTouchOffsetXY[0] = (int) (gameTouchOffsetXY[0] * mScaleFactor);
        gameTouchOffsetXY[1] = (int) (gameTouchOffsetXY[1] * mScaleFactor);

        gameLoadingOffsetXY[0]= gamePortOffsetXY[0] + (int) (gameLoadingOffsetXY[0] * mScaleFactor);
        gameLoadingOffsetXY[1]= gamePortOffsetXY[1] + (int) (gameLoadingOffsetXY[1] * mScaleFactor);

        gameButtonOffsetXY[0]= gamePortOffsetXY[0] + (int) (gameButtonOffsetXY[0] * mScaleFactor);
        gameButtonOffsetXY[1]= gamePortOffsetXY[1] + (int) (gameButtonOffsetXY[1] * mScaleFactor);

        gameTimerOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameTimerOffsetXY[0] * mScaleFactor);
        gameTimerOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameTimerOffsetXY[1] * mScaleFactor);
    }

    public void createQuads()
    {
        float vertices78x78[] = {
                78f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 78f, 0f, 1f,
                78f, 0f, 1f, 0f,
                0f, 78f, 0f, 1f,
                78f, 78f, 1f, 1f};


        float vertices136x136[] = {// Triangle Fan
                136, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 136f, 0f, 1f,
                136f, 0f, 1f, 0f,
                0f, 136f, 0f, 1f,
                136f, 136f, 1f, 1f};

        float vertices150x100[] = {// Triangle Fan
                150f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 100f, 0f, 1f,
                150f, 0f, 1f, 0f,
                0f, 100f, 0f, 1f,
                150f, 100f, 1f, 1f};

        // Here we define surfaces for objects
        // these get recalculated on load
        float vertices180x180[] = {// Triangle Fan
                180f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 180f, 0f, 1f,
                180f, 0f, 1f, 0f,
                0f, 180f, 0f, 1f,
                180f, 180f, 1f, 1f};


        float vertices300x300[] = {// Triangle Fan
                300f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 300f, 0f, 1f,
                300f, 0f, 1f, 0f,
                0f, 300f, 0f, 1f,
                300f, 300f, 1f, 1f};

        float vertices600x600[] = {// Triangle Fan
                600f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 600f, 0f, 1f,
                600f, 0f, 1f, 0f,
                0f, 600f, 0f, 1f,
                600f, 600f, 1f, 1f};


        float vertices524x200[] = {// Triangle Fan
                524f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 200f, 0f, 1f,
                524f, 0f, 1f, 0f,
                0f, 200f, 0f, 1f,
                524f, 200f, 1f, 1f};

        float[] verticesFullscreen = {
                // Order of coordinates: X, Y, S, T
                1920f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 1080f, 0f, 1f,
                1920f, 0f, 1f, 0f,
                0f, 1080f, 0f, 1f,
                1920f, 1080f, 1f, 1f};


        /*Rescale vertices*/
        vertices78x78[0] = vertices78x78[0] * mScaleFactor;
        vertices78x78[9] = vertices78x78[9] * mScaleFactor;
        vertices78x78[12] = vertices78x78[12] * mScaleFactor;
        vertices78x78[17] = vertices78x78[17] * mScaleFactor;
        vertices78x78[20] = vertices78x78[20] * mScaleFactor;
        vertices78x78[21] = vertices78x78[21] * mScaleFactor;

        vertices136x136[0] = vertices136x136[0] * mScaleFactor;
        vertices136x136[9] = vertices136x136[9] * mScaleFactor;
        vertices136x136[12] = vertices136x136[12] * mScaleFactor;
        vertices136x136[17] = vertices136x136[17] * mScaleFactor;
        vertices136x136[20] = vertices136x136[20] * mScaleFactor;
        vertices136x136[21] = vertices136x136[21] * mScaleFactor;

        vertices150x100[0] = vertices150x100[0] * mScaleFactor;
        vertices150x100[9] = vertices150x100[9] * mScaleFactor;
        vertices150x100[12] = vertices150x100[12] * mScaleFactor;
        vertices150x100[17] = vertices150x100[17] * mScaleFactor;
        vertices150x100[20] = vertices150x100[20] * mScaleFactor;
        vertices150x100[21] = vertices150x100[21] * mScaleFactor;

        vertices180x180[0] = vertices180x180[0] * mScaleFactor;
        vertices180x180[9] = vertices180x180[9] * mScaleFactor;
        vertices180x180[12] = vertices180x180[12] * mScaleFactor;
        vertices180x180[17] = vertices180x180[17] * mScaleFactor;
        vertices180x180[20] = vertices180x180[20] * mScaleFactor;
        vertices180x180[21] = vertices180x180[21] * mScaleFactor;

        vertices300x300[0] = vertices300x300[0] * mScaleFactor;
        vertices300x300[9] = vertices300x300[9] * mScaleFactor;
        vertices300x300[12] = vertices300x300[12] * mScaleFactor;
        vertices300x300[17] = vertices300x300[17] * mScaleFactor;
        vertices300x300[20] = vertices300x300[20] * mScaleFactor;
        vertices300x300[21] = vertices300x300[21] * mScaleFactor;

        vertices600x600[0] = vertices600x600[0] * mScaleFactor;
        vertices600x600[9] = vertices600x600[9] * mScaleFactor;
        vertices600x600[12] = vertices600x600[12] * mScaleFactor;
        vertices600x600[17] = vertices600x600[17] * mScaleFactor;
        vertices600x600[20] = vertices600x600[20] * mScaleFactor;
        vertices600x600[21] = vertices600x600[21] * mScaleFactor;

        vertices524x200[0] = vertices524x200[0] * mScaleFactor;
        vertices524x200[9] = vertices524x200[9] * mScaleFactor;
        vertices524x200[12] = vertices524x200[12] * mScaleFactor;
        vertices524x200[17] = vertices524x200[17] * mScaleFactor;
        vertices524x200[20] = vertices524x200[20] * mScaleFactor;
        vertices524x200[21] = vertices524x200[21] * mScaleFactor;

        verticesFullscreen[0] = verticesFullscreen[0] * mScaleFactor;
        verticesFullscreen[9] = verticesFullscreen[9] * mScaleFactor;
        verticesFullscreen[12] = verticesFullscreen[12] * mScaleFactor;
        verticesFullscreen[17] = verticesFullscreen[17] * mScaleFactor;
        verticesFullscreen[20] = verticesFullscreen[20] * mScaleFactor;
        verticesFullscreen[21] = verticesFullscreen[21] * mScaleFactor;


        /* Load into float buffers */
        VertexArray vert78x78 = new VertexArray(vertices78x78);
        VertexArray vert136x136 = new VertexArray(vertices136x136);
        VertexArray vert150x100 = new VertexArray(vertices150x100);
        VertexArray vert180x180 = new VertexArray(vertices180x180);
        VertexArray vert300x300 = new VertexArray(vertices300x300);
        VertexArray vert600x600 = new VertexArray(vertices600x600);
        VertexArray vert524x200 = new VertexArray(vertices524x200);
        VertexArray vertFullscreen = new VertexArray(verticesFullscreen);

        /* Assign objects */
        mVertexData = new SparseArray<>();
        mVertexData.put(OBJ_CRATE, vert136x136);
        mVertexData.put(OBJ_BLOCK, vert136x136);
        mVertexData.put(OBJ_ITEM, vert136x136);
        mVertexData.put(OBJ_BOMB, vert136x136);
        mVertexData.put(OBJ_EXPLN, vert150x100);
        mVertexData.put(OBJ_PLAYR, vert180x180);
        mVertexData.put(SOBJ_TOUCH, vert300x300);
        mVertexData.put(SOBJ_BUTTON, vert524x200);
        mVertexData.put(SOBJ_BACKGROUND, vertFullscreen);
        mVertexData.put(SOBJ_BACKGROUND_LOADING, vert600x600);
        mVertexData.put(SOBJ_FPS_COUNTER, vert78x78);
        mVertexData.put(SOBJ_TIMER, vert78x78);
    }


    class LoaderConfig
    {
        BitmapFactory.Options opts;
        LoaderConfig(float scale)
        {
            opts = new BitmapFactory.Options();
            opts.inScaled = true;
            opts.inDensity = (int) GAME_WIDTH;
            opts.inTargetDensity = (int) (GAME_WIDTH * scale);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
    }
}
