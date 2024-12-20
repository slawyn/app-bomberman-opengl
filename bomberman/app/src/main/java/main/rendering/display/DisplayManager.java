package main.rendering.display;

import android.util.SparseArray;

import java.util.Vector;

import main.Globals;
import main.game.events.EventObject;
import main.game.events.Events;
import main.nativeclasses.GameManager;
import main.game.SceneManager;
import main.nativeclasses.GameElement;
import main.game.SceneElement;
import main.rendering.color.ColorShaderProgram;
import main.rendering.elements.RenderElement;
import main.rendering.VertexArray;
import main.rendering.animation.Layer;

import static main.Constants.*;

public class DisplayManager {
    private final String TAG = "DisplayManager";
    private static float mPortScaleFactor = 1.0f;
    public static int mGamePortWidth = 1920;
    public static int mGamePortHeight = 1080;
    public static float mGameFieldOffsetX = 190f;
    public static float mGameFieldOffsetY = 36f;
    public static float mGameFieldOffsetY2 = 10f;
    public static float mGameFieldWidth = mGamePortWidth - mGameFieldOffsetX;
    public static float mGameFieldHeight = mGamePortHeight - mGameFieldOffsetY;

    public static final int GAME_H = 136;
    public static final int GAME_V = 94;
    public static int[] gamePortOffsetXY = { 0, 0 };
    public static int[] gameBombOffsetXY = { -GAME_H / 2, -GAME_H / 2 };
    public static int[] gameObjectOffsetXY = { -180 / 2, -180 / 2 - 40  };
    public static int[] gameExplosionOffsetXY = { -GAME_H / 2, -GAME_H / 2 };

    // Scene objects
    public static int[] gameTouchOffsetXY = { (-(300 / 2)), (-(300 / 2)) };
    public static int[] gameLoadingOffsetXY = { (mGamePortWidth) / 2 - 300, (mGamePortHeight) / 2 - 300 };
    public static int[] gameButtonOffsetXY = { gamePortOffsetXY[0], gamePortOffsetXY[1] };
    public static int[] gameTimerOffsetXY = { mGamePortWidth - 400, 20 };

    public static int[] gameZeroOffsetXY = { 0, 0 };
    public static int[][] gameTimer2ndOffsets = { { 0, 0 }, { 78, 0 }, { 200, 0 }, { 278, 0 } };

    private int mTotalNumberOfRenderElements;
    private RenderElement[] mFreeRenderElements;
    private SparseArray<RenderElement> mRos;

    private Loader mLoader;
    private Vertices mVertices;
    private AnimationManager mAnimationManager;

    public static final int LAYER_0000 = 0000;
    public static final int LAYER_0001 = 0001;
    public static final int LAYER_0002 = 0002;
    public static final int LAYER_0003 = 0003;
    public static final int LAYER_0004 = 0004;

    private Vector<Layer> mLayers;

    private static boolean DEBUG_DRAW_HITBOXES = false;

    public DisplayManager() {
        mAnimationManager = new AnimationManager();

        /*
         * Render Elements hold the base offset and animations
         * :Translation is updated on game thread, and is interpolated
         * :Animation timings are updated on the GPU thread
         */
        mTotalNumberOfRenderElements = CONFIG_RENDER_OBJECTS_MAX;

        mFreeRenderElements = new RenderElement[mTotalNumberOfRenderElements];
        for (int idx = 0; idx < CONFIG_RENDER_OBJECTS_MAX; idx++) {
            mFreeRenderElements[idx] = new RenderElement();
        }

        mRos = new SparseArray<>(CONFIG_RENDER_OBJECTS_MAX);

        /* @attention LAYER_xxxx constants are used outside of this class as indices */
        int[][] layerConfiguration = { { LAYER_0000, 10 },
                { LAYER_0001, 50 },
                { LAYER_0002, 10 },
                { LAYER_0003, 10 },
                { LAYER_0004, 50 } };
        mLayers = Layer.createLayers(layerConfiguration);
    }

    public int getResourceCount() {
        return mAnimationManager.getDrawableCount();
    }

    public static float scale(float pos) {
        return mPortScaleFactor * pos;
    }

    public Vector<Layer> getLayers() {
        return mLayers;
    }

    public static void toggleDebugHitboxes() {
        DEBUG_DRAW_HITBOXES = !DEBUG_DRAW_HITBOXES;
    }

    public RenderElement getRenderObject(EventObject go) {
        final int layerHitbox = 4;
        final int unique = go.getId();

        RenderElement ro = mRos.get(unique);
        if (ro == null) {
            ro = getFreeRenderObjectFromPool();
            mRos.put(unique, ro);
            
            /* Save previous movement state */
            final int laynum = go.getLayer();
            ro.init(go.getType(), go.getSubtype(), go.getState(), unique);
            ro.setAdditionalParams(go.getUpdatedAdditionals());
            mAnimationManager.createAnimatedObject(ro, laynum);
            mLayers.get(laynum).addRenderObjectToLayer(ro);
        }

        /* Add a visible hitbox */
        if (DEBUG_DRAW_HITBOXES) {
            if (go.hasBoundingboxes() && ro.mDebugObject == null) {
                ro.addDebugObject(go, mPortScaleFactor);
                mLayers.get(layerHitbox).addRenderObjectToLayer(ro.mDebugObject);
            }
        } else if (ro.mDebugObject != null) {
            if (ro.mDebugObject.removed) {
                ro.mDebugObject = null;
            } else {
                ro.mDebugObject.removeFromRenderingGpu = true;
                // ro.mDebugObject = null;
                /* TODO: destroy object */
            }
        }

        return ro;
    }

    private RenderElement getFreeRenderObjectFromPool() {
        RenderElement ro = mFreeRenderElements[--mTotalNumberOfRenderElements];
        return ro;
    }

    private void returnRenderObjectToPool(RenderElement ro) {

        /* Return RenderElement to pool */
        ro.removeFromRenderingGpu = true;
        mRos.remove(ro.getUniqueId());
        mFreeRenderElements[mTotalNumberOfRenderElements++] = ro;

        /* Return all animations to pool */
        mAnimationManager.returnAnimationsToPool(ro.getUsedAnimatedObjects());
    }

    private void updateGameManager(GameManager gamemanager) {
        Events goupdates = gamemanager.getUpdateEvents();

        /* Update/Create Renderobjects */
        int sz = goupdates.getCount();
        while (--sz >= 0) {
            GameElement go = (GameElement) goupdates.getEvent(sz);
            RenderElement ro = getRenderObject(go);

            /** Translate and sort after 'Z' all animations that are attached to the render object */
            float pos[] = go.getPositionXY();
            ro.setTranslation(mGameFieldOffsetX + pos[0] * mPortScaleFactor, mGameFieldOffsetY + pos[1] * mPortScaleFactor);
            ro.updateSortCriteria(go.getZ());
            mAnimationManager.updateAnimatedObject(ro, go.getState());
        }

        goupdates.resetEvents();
    }

    private void updateSceneManager(SceneManager manager) {

        /* Updates RenderElements from SceneManager */
        Events soupdates = manager.getUpdateEvents();
        int sz = soupdates.getCount();
        while (--sz >= 0) {
            SceneElement so = (SceneElement) soupdates.getEvent(sz);
            RenderElement ro = getRenderObject(so);

            ro.setTranslation(so.mPositionX, so.mPositionY);
            mAnimationManager.updateAnimatedObject(ro, so.getState());
        }

        soupdates.resetEvents();
    }

    public void removeRenderElements()
    {
        for(int i =  mRos.size()-1; i >= 0; --i) {
            RenderElement re = mRos.valueAt(i);
            if(!re.updated) {
                re.removeFromRenderingGpu = true;
                returnRenderObjectToPool(re);
            }
        }
    }

    public void markRenderElements()
    {
        for(int i = 0, nsize = mRos.size(); i < nsize; i++) {
            RenderElement re = mRos.valueAt(i);
            re.updated = false;
        }
    }

    public void updateRenderElementsForGPU(GameManager gamemanager, SceneManager scenemanager) {
        markRenderElements();
        updateGameManager(gamemanager);
        updateSceneManager(scenemanager);
        removeRenderElements();
        
        RenderElement.latch();
    }

    public VertexArray getQuad(int type) {
        return mVertices.getQuad(type);
    }

    public void load(int width, int height, float scalefactor, int portx, int porty) {

        /* Create vertices for drawing */
        mVertices = new Vertices(scalefactor);

        /*
         * Load animations into OPENGL Space
         * Textures are loaded on the opengl thread
         */
        mLoader = new Loader(mGamePortWidth, scalefactor);
        mAnimationManager.loadAnimations(mLoader);

        /* Adjust offsets and scalings based on device resolution */
        mGamePortWidth = width;
        mGamePortHeight = height;
        mPortScaleFactor = scalefactor;
        gamePortOffsetXY[0] = portx;
        gamePortOffsetXY[1] = porty;
        mGameFieldOffsetX *= scalefactor;
        mGameFieldOffsetY *= scalefactor;
        mGameFieldOffsetY2 *= scalefactor;
        mGameFieldWidth = mGamePortWidth - mGameFieldOffsetX * 2;
        mGameFieldHeight = mGamePortHeight - mGameFieldOffsetY - mGameFieldOffsetY2;

        gameBombOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameBombOffsetXY[0] * scalefactor);
        gameBombOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameBombOffsetXY[1] * scalefactor);

        gameExplosionOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameExplosionOffsetXY[0] * scalefactor);
        gameExplosionOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameExplosionOffsetXY[1] * scalefactor);

        gameObjectOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameObjectOffsetXY[0] * scalefactor);
        gameObjectOffsetXY[1] = (gamePortOffsetXY[1] + (int) (gameObjectOffsetXY[1] * scalefactor));

        gameTouchOffsetXY[0] = (int) (gameTouchOffsetXY[0] * scalefactor);
        gameTouchOffsetXY[1] = (int) (gameTouchOffsetXY[1] * scalefactor);

        gameLoadingOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameLoadingOffsetXY[0] * scalefactor);
        gameLoadingOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameLoadingOffsetXY[1] * scalefactor);

        gameButtonOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameButtonOffsetXY[0] * scalefactor);
        gameButtonOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameButtonOffsetXY[1] * scalefactor);

        gameTimerOffsetXY[0] = gamePortOffsetXY[0] + (int) (gameTimerOffsetXY[0] * scalefactor);
        gameTimerOffsetXY[1] = gamePortOffsetXY[1] + (int) (gameTimerOffsetXY[1] * scalefactor);
    }

    public Loader getLoader() {
        return mLoader;
    }

    public void bindDebugDataPort(ColorShaderProgram debugprogram) {
        float[] data0 = new float[12];

        /* first triangle */
        // top right
        data0[0] = (mGamePortWidth);
        data0[1] = -0;

        // top left
        data0[2] = -0;
        data0[3] = -0;

        // bottom left
        data0[4] = -0;
        data0[5] = (mGamePortHeight);

        /* second triangle */
        // top right
        data0[6] = (mGamePortWidth);
        data0[7] = -0;

        // bottom left
        data0[8] = -0;
        data0[9] = (mGamePortHeight);

        // bottom right
        data0[10] = (mGamePortWidth);
        data0[11] = (mGamePortHeight);
        new VertexArray(data0).setVertexAttribPointer(
                0,
                debugprogram.getPositionAttributeLocation(),
                2,
                8);
    }

    public void bindDebugDataField(ColorShaderProgram debugprogram) {
        float[] data0 = new float[12];

        /* first triangle */
        // top right
        data0[0] = (mGameFieldWidth);
        data0[1] = -0;

        // top left
        data0[2] = -0;
        data0[3] = -0;

        // bottom left
        data0[4] = -0;
        data0[5] = (mGameFieldHeight);

        /* second triangle */
        // top right
        data0[6] = (mGameFieldWidth);
        data0[7] = -0;

        // bottom left
        data0[8] = -0;
        data0[9] = (mGameFieldHeight);

        // bottom right
        data0[10] = (mGameFieldWidth);
        data0[11] = (mGameFieldHeight);
        new VertexArray(data0).setVertexAttribPointer(
                0,
                debugprogram.getPositionAttributeLocation(),
                2,
                8);
    }
}
