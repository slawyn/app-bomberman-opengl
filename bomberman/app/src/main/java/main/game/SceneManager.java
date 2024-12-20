package main.game;

import android.util.SparseArray;

import main.game.events.ButtonCallback;
import main.game.events.Events;
import main.game.sceneobjects.Background;
import main.game.sceneobjects.Button;
import main.game.sceneobjects.ButtonRectangular;
import main.game.sceneobjects.ButtonSquared;
import main.game.sceneobjects.Loading;
import main.game.sceneobjects.Timer;
import main.game.sceneobjects.Touch;
import main.rendering.display.DisplayManager;
import main.rendering.display.AnimationManager;

import static main.Constants.ST_APP_CLIENT_DISCOVER_BLUETOOTH;
import static main.Constants.ST_APP_CLIENT_DISCOVER_WLAN;
import static main.Constants.ST_APP_OFFLINE_START;
import static main.Constants.ST_APP_SELECTION_BLUETOOTH;
import static main.Constants.ST_APP_SELECTION_MAIN;
import static main.Constants.ST_APP_SELECTION_WLAN;
import static main.Constants.ST_APP_SERVER_DISCOVERABLE;

public class SceneManager {
    private final String TAG = "SceneManager";

    public final static int SOBJ_TOUCH = 0x10000000;
    public final static int SOBJ_BACKGROUND = 0x20000000;
    public final static int SOBJ_BUTTON = 0x30000000;
    public final static int SOBJ_BACKGROUND_LOADING = 0x40000000;
    public final static int SOBJ_FPS_COUNTER = 0x50000000;
    public final static int SOBJ_TIMER = 0x60000000;
    public final static int SOBJ_BUTTON_SQUARED = 0x70000000;
    public final static int SOBJ_DEBUG = 0x80000000;
    public final static int SOBJ_DEBUG_LINE = 0x90000000;

    private SparseArray<Button> mButtons;
    private SparseArray<SceneElement> mSceneObjects;
    private Events mUpdateEvents;
    private SparseArray<Touch> mTouches;
    private DisplayManager mDisplayManager;
    private Timer timer;
    private ButtonCallback callback;

    private int mObjectCounter;

    public SceneManager(DisplayManager displayman) {
        mDisplayManager = displayman;
        mObjectCounter = 0;
        mButtons = new SparseArray<>();
        mSceneObjects = new SparseArray<>();
        mTouches = new SparseArray<>();
        mUpdateEvents = new Events();
    }


    public Events getUpdateEvents() {
        return mUpdateEvents;
    }

    public SceneElement getSceneObject(int key) {
        return mSceneObjects.get(key);
    }

    private void addTouchMarker(int touchid) {
        int id = ++mObjectCounter;
        Touch touch = new Touch(id, touchid);
        mTouches.put(touchid, touch);
        mSceneObjects.put(touch.getType() | id, touch);
        touch.mLayer = DisplayManager.LAYER_0003;
    }

    private void addRectangularButton(int posx, int posy, int action, int animation, int layer) {
        final int id = ++mObjectCounter;
        final int xSize = (int) mDisplayManager.scale(524);
        final int ySize = (int) mDisplayManager.scale(200);

        Button button = new ButtonRectangular(id,
                (int) (mDisplayManager.gameButtonOffsetXY[0] + mDisplayManager.scale(posx)),
                (int) (mDisplayManager.gameButtonOffsetXY[1] + mDisplayManager.scale(posy)),
                xSize, ySize,
                action, animation);
        mButtons.put(id, button);
        mSceneObjects.put(button.getType() | id, button);
        button.mLayer = layer;
    }

    private void addSquaredButton(int posx, int posy, int action, int animation, int layer) {
        final int id = ++mObjectCounter;
        final int ySize = (int) (mDisplayManager.scale(200));
        final int xSize = ySize;

        ButtonSquared button = new ButtonSquared(id,
                (int) (mDisplayManager.gameButtonOffsetXY[0] + mDisplayManager.scale(posx)),
                (int) (mDisplayManager.gameButtonOffsetXY[1] + mDisplayManager.scale(posy)),
                xSize, ySize,
                action, animation);
        mButtons.put(id, button);
        mSceneObjects.put(button.getType() | id, button);
        button.mLayer = layer;
    }

    private void addLoading(int animation, int layer) {
        int id = ++mObjectCounter;
        Loading loading = new Loading(id, animation);
        mSceneObjects.put(loading.getType() | id, loading);
        loading.mLayer = layer;

    }

    private void addTimer(int animation, int layer) {
        int id = ++mObjectCounter;
        timer = new Timer(id, mDisplayManager.gameTimerOffsetXY[0], mDisplayManager.gameTimerOffsetXY[1], animation);
        mSceneObjects.put(timer.getType() | id, timer);
        timer.mLayer = layer;
    }

    private void addBackground(int animation, int layer) {
        int id = ++mObjectCounter;
        Background background = new Background(id, animation);
        mSceneObjects.put(background.getType() | id, background);
        background.mLayer = layer;
    }

    public void setTimer(int time) {
        timer.setTime(time);
    }

    public void run(long dt, int[] input) {
        int sz = mSceneObjects.size();
        for (int idx = 0; idx < sz; idx++) {
            SceneElement so = mSceneObjects.valueAt(idx);
            if(so.updateState(dt, input) && callback != null)
            {
                callback.onCallback(so.mAction);
            }

            /* element has been updated 
             * TODO: update elements only if it has changed */
            mUpdateEvents.addEvent(so);
        }
    }

    public void showLoadingProgress() {
        // TODO
    }

    private void clearScene() {
        callback = null;
        int size = mSceneObjects.size() - 1;

        for (int i = size; i >= 2; --i) {
            SceneElement so = mSceneObjects.valueAt(i);
            mSceneObjects.removeAt(i);
        }
        mButtons.clear();
    }

    public void createLoadingScene() {
        clearScene();
        addBackground(AnimationManager.AG000, DisplayManager.LAYER_0000);
        addLoading(AnimationManager.AG000, DisplayManager.LAYER_0003);

    }

    public void createSelectionScene(ButtonCallback btncallback) {
        clearScene();
        addBackground(AnimationManager.AG000, DisplayManager.LAYER_0000);
        addRectangularButton(700, 80, ST_APP_SELECTION_BLUETOOTH, AnimationManager.AG002, DisplayManager.LAYER_0002);
        addRectangularButton(700, 360, ST_APP_SELECTION_WLAN, AnimationManager.AG003, DisplayManager.LAYER_0002);
        addRectangularButton(700, 640, ST_APP_OFFLINE_START, AnimationManager.AG004, DisplayManager.LAYER_0002);
        callback = btncallback;
    }

    public void createSelectionBluetoothScene(ButtonCallback btncallback) {
        clearScene();
        addBackground(AnimationManager.AG000, DisplayManager.LAYER_0000);
        addRectangularButton(700, 360, ST_APP_CLIENT_DISCOVER_BLUETOOTH, AnimationManager.AG000,
                DisplayManager.LAYER_0002);
        addRectangularButton(700, 640, ST_APP_SERVER_DISCOVERABLE, AnimationManager.AG001, DisplayManager.LAYER_0002);
        addRectangularButton(100, 60, ST_APP_SELECTION_MAIN, AnimationManager.AG005, DisplayManager.LAYER_0002);
        callback = btncallback;
    }

    public void createSelectionWlanScene(ButtonCallback btncallback) {

        clearScene();
        addBackground(AnimationManager.AG000, DisplayManager.LAYER_0000);
        addRectangularButton(700, 360, ST_APP_CLIENT_DISCOVER_WLAN, AnimationManager.AG000, DisplayManager.LAYER_0002);
        addRectangularButton(700, 640, ST_APP_SERVER_DISCOVERABLE, AnimationManager.AG001, DisplayManager.LAYER_0002);
        addRectangularButton(100, 60, ST_APP_SELECTION_MAIN, AnimationManager.AG005, DisplayManager.LAYER_0002);
        callback = btncallback;
    }

    public void createFirstScene() {
        clearScene();
        createLoadingScene();
        addTouchMarker(AnimationManager.AG000);
        addTouchMarker(AnimationManager.AG001);
    }

    // TODO add other stages
    public void createGameScene(ButtonCallback btncallback) {
        clearScene();
        addBackground(AnimationManager.AG001, DisplayManager.LAYER_0000);
        addTimer(AnimationManager.AG000, DisplayManager.LAYER_0002);
        addSquaredButton(0, 800, 1, AnimationManager.AG006, DisplayManager.LAYER_0002);
        callback = btncallback;
    }
}
