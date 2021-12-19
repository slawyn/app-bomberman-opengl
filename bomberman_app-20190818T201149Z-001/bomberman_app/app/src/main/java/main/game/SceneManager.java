package main.game;

import android.util.SparseArray;

import main.game.sceneobjects.Background;
import main.game.sceneobjects.Button;
import main.game.sceneobjects.Loading;
import main.game.sceneobjects.SceneObject;
import main.game.sceneobjects.Touch;
import main.rendering.animation.DisplayManager;

import static main.Constants.ST_APP_CLIENT_DISCOVER_BLUETOOTH;
import static main.Constants.ST_APP_CLIENT_DISCOVER_WLAN;
import static main.Constants.ST_APP_OFFLINE_START;
import static main.Constants.ST_APP_SELECTION_BLUETOOTH;
import static main.Constants.ST_APP_SELECTION_MAIN;
import static main.Constants.ST_APP_SELECTION_WLAN;
import static main.Constants.ST_APP_SERVER_DISCOVERABLE;

public class SceneManager
{
    private final String TAG = "SceneManager";

    public final static int SOBJ_TOUCH =    0x10000000;
    public final static int SOBJ_BACKGROUND = 0x20000000;
    public final static int SOBJ_BUTTON = 0x30000000;
    public final static int SOBJ_BACKGROUND_LOADING = 0x40000000;

    private SparseArray<Button> mButtons;
    private SparseArray<Background> mBackgrounds;
    private SparseArray<Loading> mLoadings;
    private SparseArray<SceneObject> mSceneObjects;
    private Events mUpdateEvents;
    private Events mRemoveEvents;
    private SparseArray<Touch> mTouches;
    private DisplayManager mDisplayManager;

    private int mObjectCounter;
    public SceneManager(DisplayManager displayman){
        mDisplayManager = displayman;
        mObjectCounter = 0;
        mButtons = new SparseArray<>();
        mBackgrounds = new SparseArray<>();
        mLoadings = new SparseArray<>();
        mSceneObjects = new SparseArray<>();
        mTouches = new SparseArray<>();
        mUpdateEvents = new Events();
        mRemoveEvents = new Events();
    }

    public Events getRemovalEvents(){
        return mRemoveEvents;
    }

    public Events getUpdateEvents(){
        return mUpdateEvents;
    }

    public SceneObject getSceneObject(int key){
        return mSceneObjects.get(key);
    }

    public void addTouchMarker(int touchid){
        int id = ++mObjectCounter;
        Touch touch = new Touch(id, touchid);
        mTouches.put(touchid, touch);
        mSceneObjects.put(touch.mObjectType|id, touch);
        touch.mLayer = 3;
    }


    public void addRectangularButton(int posx, int posy, int action, int animation, int layer)
    {
        int id = ++mObjectCounter;
        Button button = new Button(id, (int)(mDisplayManager.gameButtonXOffset0+posx*mDisplayManager.mScaleFactor),  (int)(mDisplayManager.gameButtonYOffset0+posy*mDisplayManager.mScaleFactor), (int)(524*mDisplayManager.mScaleFactor),(int)(200*mDisplayManager.mScaleFactor), action, animation);
        mButtons.put(id, button);
        mSceneObjects.put(button.mObjectType|id,button);
        button.mLayer = layer;
    }


    public void addLoading(int animation, int layer)
    {
        int id = ++mObjectCounter;
        Loading loading = new Loading(id, animation);
        mLoadings.put(id, loading);
        mSceneObjects.put(loading.mObjectType|id,loading);
        loading.mLayer = layer;

    }

    public void addBackground(int animation, int layer)
    {
        int id = ++mObjectCounter;
        Background background = new Background(id, animation);
        mBackgrounds.put(id,background);
        mSceneObjects.put(background.mObjectType|id,background);
        background.mLayer = layer;
    }


    public int parseSelection(int[] input)
    {
        int selected = -1;
        int size = mButtons.size() - 1;
        for(int i = size; i >= 0; i--)
        {
            int key = mButtons.keyAt(i);
            Button b = mButtons.get(key);
            if(b.updateState(input))
            {

                return b.mAction;
            }
        }
        return selected;
    }


    public void updateSceneObjects(long dt, int[] input)
    {

        //mUpdateEvents.resetEvents();

        int sz = mSceneObjects.size();
        for(int idx = 0;idx<sz;idx++){

            SceneObject so = mSceneObjects.valueAt(idx);
            so.updateState(dt, input);
            if(so.mRequiresUpdate)
            {
                so.mRequiresUpdate = false;
                mUpdateEvents.addEvent(so);
            }
        }
    }


    public void showLoadingProgress()
    {
            // TODO
    }


    private void deleteLastScene()
    {
        mRemoveEvents.resetEvents();
        int size = mSceneObjects.size()-1;

         // buttons
        for(int i = size; i >=2; --i)
        {
            SceneObject so = mSceneObjects.valueAt(i);
            mSceneObjects.removeAt(i);
            mRemoveEvents.addEvent(so);
        }


        mButtons.clear();
        mLoadings.clear();
        mBackgrounds.clear();
    }



    public void createLoadingScene()
    {
        deleteLastScene();
        addBackground(0,0);
        addLoading(0,3);

    }

    public void createSelectionScene()
    {
        deleteLastScene();
        addBackground(0,0);
        addRectangularButton(700, 80, ST_APP_SELECTION_BLUETOOTH, 2,2);
        addRectangularButton(700, 360, ST_APP_SELECTION_WLAN, 3,2);
        addRectangularButton(700, 640, ST_APP_OFFLINE_START, 4,2);
    }


    public void createSelectionBluetoothScene()
    {
        deleteLastScene();
        addBackground(0,0);
        addRectangularButton(700, 360, ST_APP_CLIENT_DISCOVER_BLUETOOTH, 0,2);
        addRectangularButton(700, 640, ST_APP_SERVER_DISCOVERABLE, 1,2);
        addRectangularButton(100, 60, ST_APP_SELECTION_MAIN, 5,2);
    }

    public void createSelectionWlanScene()
    {
        deleteLastScene();
        addBackground(0,0);
        addRectangularButton(700, 360, ST_APP_CLIENT_DISCOVER_WLAN, 0,2);
        addRectangularButton(700, 640, ST_APP_SERVER_DISCOVERABLE, 1,2);
        addRectangularButton(100, 60, ST_APP_SELECTION_MAIN, 5,2);
    }

    public void createFirstScene()
    {
        deleteLastScene();
        createLoadingScene();
        addTouchMarker(0);
        addTouchMarker(1);
    }


    // TODO add other stages
    public void createGameScene()
    {
        deleteLastScene();
        addBackground(1,0);
    }
}
