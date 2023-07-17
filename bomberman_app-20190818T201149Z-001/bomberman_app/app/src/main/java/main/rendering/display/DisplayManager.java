package main.rendering.display;

import android.util.SparseArray;

import java.util.Vector;

import main.game.events.Events;
import main.nativeclasses.GameManager;
import main.game.SceneManager;
import main.nativeclasses.GameElement;
import main.game.SceneElement;
import main.rendering.elements.RenderElement;
import main.rendering.VertexArray;
import main.rendering.animation.Layer;

import static main.Constants.*;
import static main.Constants.DEBUG_DRAW_HITBOXES;


public class DisplayManager
{
    private final String TAG = "DisplayManager";
    public static float mScaleFactor = 1.0f;
    public static int mGamePortWidth = 1920;
    public static int mGamePortHeight = 1080;
    public static final int GAME_H = 136;
    public static final int GAME_V = 94;
    public static int[] gamePortOffsetXY = {0, 0};
    public static int[] gameBombOffsetXY      = {-GAME_H / 2, -GAME_H/2};
    public static int[] gameObjectOffsetXY    = {-GAME_H / 2, (94 - GAME_H)-GAME_V/2};
    public static int[] gameExplosionOffsetXY = {-GAME_H / 2, -GAME_H/2};
    public static int[] gamePlayerOffsetXY = {-180/2, -180/2 - 40};

    // Scene objects
    public static int[] gameTouchOffsetXY  = {(-(300 / 2)),(-(300 / 2))};
    public static int[] gameLoadingOffsetXY = {(mGamePortWidth) / 2 - 300, (mGamePortHeight) / 2 - 300};
    public static int[] gameButtonOffsetXY = {gamePortOffsetXY[0],gamePortOffsetXY[1]};
    public static int[] gameTimerOffsetXY = {mGamePortWidth - 400, 20};

    public static int[] gameZeroOffsetXY = {0, 0};
    public static int[][] gameTimer2ndOffsets = {{0,0},{78,0},{200,0},{278,0}};



    private int mTotalNumberOfRenderObjects;
    private RenderElement[] mFreeRenderElements;
    private SparseArray<RenderElement> mRos;

    private Loader mLoader;
    private Vertices mVertices;
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
    }

    public int getResourceCount()
    {
        return mAnimationManager.getDrawableCount();
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
            final int layerAnimation = 1;
            final int layerHitbox = 4;

            /* Save previous movement state */
            ro.init(go.mObjectType,go.mObjectSubtype, go.getState(), unique);
            mAnimationManager.createAnimatedObject(ro, layerAnimation);
            mRos.put(unique, ro);

            /* Add a visible hitbox */
            if(drawhitbox)
            {
                ro.addDebugObject(go, mScaleFactor);
                mAnimationManager.getLayers().get(layerHitbox).addRenderObjectToLayer(ro.mDebugObject);
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

    private void updateGameManager(GameManager gamemanager)
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
            ro.setTranslation(pos[0] * mScaleFactor, pos[1] * mScaleFactor);
            ro.updateSortCriteria(go.getZ());
            mAnimationManager.updateAnimatedObject(ro, go.getState());
        }

        /* Remove objects */
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
    private void updateSceneManager(SceneManager manager)
    {
        // Updates RenderObjects from SceneManager
        Events soupdates = manager.getUpdateEvents();
        Events soremovals = manager.getRemovalEvents();
        int sz = soupdates.getCount();
        while (--sz >= 0)
        {
            SceneElement so = (SceneElement) soupdates.getEvent(sz);
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
            SceneElement so = (SceneElement) soremovals.getEvent(sz);
            RenderElement ro = so.ro;
            returnRenderObjectToPool(ro);
        }
        soupdates.resetEvents();
        soremovals.resetEvents();
    }

    public void updateRenderObjectsForGPU(GameManager gamemanager, SceneManager scenemanager)
    {
        updateGameManager(gamemanager);
        updateSceneManager(scenemanager);
        RenderElement.latch();
    }

    public VertexArray getQuad(int type)
    {
        return mVertices.getQuad(type);
    }

    public void load(int width, int height, float scalefactor, int portx, int porty)
    {
        /* Load animations into OPENGL Space
        * Textures are loaded on the opengl thread */

        mVertices = new Vertices(scalefactor);

        mLoader = new Loader(mGamePortWidth, scalefactor);
        mAnimationManager.loadAnimations(mLoader);


        mGamePortWidth = width;
        mGamePortHeight = height;
        mScaleFactor = scalefactor;
        gamePortOffsetXY[0] = portx;
        gamePortOffsetXY[1] = porty;

        gameBombOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameBombOffsetXY[0] * scalefactor);
        gameBombOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameBombOffsetXY[1] * scalefactor);

        gamePlayerOffsetXY[0] = gamePortOffsetXY[0] + (int)(gamePlayerOffsetXY[0]* scalefactor);
        gamePlayerOffsetXY[1] = gamePortOffsetXY[1] + (int)(gamePlayerOffsetXY[1]* scalefactor);

        gameExplosionOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameExplosionOffsetXY[0] * scalefactor);
        gameExplosionOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameExplosionOffsetXY[1] * scalefactor);

        gameObjectOffsetXY[0] = gamePortOffsetXY[0]  + (int) (gameObjectOffsetXY[0] * scalefactor);
        gameObjectOffsetXY[1] = (gamePortOffsetXY[1] + (int) (gameObjectOffsetXY[1] * scalefactor));

        gameTouchOffsetXY[0] = (int) (gameTouchOffsetXY[0] * scalefactor);
        gameTouchOffsetXY[1] = (int) (gameTouchOffsetXY[1] * scalefactor);

        gameLoadingOffsetXY[0]= gamePortOffsetXY[0] + (int) (gameLoadingOffsetXY[0] * scalefactor);
        gameLoadingOffsetXY[1]= gamePortOffsetXY[1] + (int) (gameLoadingOffsetXY[1] * scalefactor);

        gameButtonOffsetXY[0]= gamePortOffsetXY[0] + (int) (gameButtonOffsetXY[0] * scalefactor);
        gameButtonOffsetXY[1]= gamePortOffsetXY[1] + (int) (gameButtonOffsetXY[1] * scalefactor);

        gameTimerOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameTimerOffsetXY[0] * scalefactor);
        gameTimerOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameTimerOffsetXY[1] * scalefactor);
    }

    public Loader getLoader() {
        return mLoader;
    }
}
