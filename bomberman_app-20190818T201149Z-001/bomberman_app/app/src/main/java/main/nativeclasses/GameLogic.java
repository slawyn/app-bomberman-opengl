package main.nativeclasses;

import android.annotation.SuppressLint;

import main.game.events.Events;

import static main.Constants.*;
import static main.nativeclasses.GameElement.OBJ_PLAYR;

public class GameLogic
{

    static {
        System.loadLibrary("GameLogic");
    }


    public native static long initFreeType();
    public native static void setInput(int type, int offset, byte input);
    public native static int updateGameTicker();
    public native static int createGame();
    public native static int updateGame(int dt);
    public native static int getState(int type, int offset);
    public native static long[] getPosition(int type, int offset);
    public native static int[][] getHitboxes(int type, int offset);
    public native static int getZ(int type, int offset);
    public native static int[] getObjects();
    public native static int[] getRemovedObjects();

    private final String TAG = "GameLogicManager";
    private final int mCapacity = 50;
    private Events mStateUpdateEvents;
    private Events mKillEvents;
    private Events mBombEvents;
    private Events mRemovalEvents;

    private int mNextFreeGameObject;
    private int mNextFreeSlotForGameObject;
    private final GameElement[] mGameObjectPool;
    private int mGameTime;

    @SuppressLint("UseSparseArrays")
    public GameLogic(int maxobjects)
    {
        mStateUpdateEvents = new Events();
        mRemovalEvents = new Events();
        mKillEvents = new Events();
        mBombEvents = new Events();
        mGameObjectPool = new GameElement[maxobjects];
        mNextFreeGameObject = 0;
        mNextFreeSlotForGameObject = 0;
        mGameTime = 0;
    }

    public int getGameTime()
    {
        int time = mGameTime/1000;
        return time;
    }

    public void updatePlayerInput(int index, byte playerinput)
    {
        GameLogic.setInput(OBJ_PLAYR,index, playerinput);
    }

    public void run(int dt)
    {
        mGameTime +=dt;
        GameLogic.updateGame(dt);
    }



    /* Get Free Game object to fill with live information */
    public GameElement getFreeGameObject()
    {
        GameElement go = mGameObjectPool[mNextFreeGameObject];
        mNextFreeGameObject=(mNextFreeGameObject+1)%mGameObjectPool.length;
        if(null == go){
            go = new GameElement();
        }
        return go;
    }

    public void updateGameOfflineInput(int input)
    {
        updatePlayerInput(0, (byte)input);
    }

    public void createGameLevel(int level)
    {
        deleteAllGameObjects();
        GameLogic.createGame();
    }

    public void deleteAllGameObjects()
    {
        mRemovalEvents.resetEvents();
        mBombEvents.resetEvents();
        mStateUpdateEvents.resetEvents();
    }

    public Events getUpdateEvents()
    {
        int[] objs = GameLogic.getObjects();
        int total = objs.length - 1;
        for(int i = total; i >= 0; i--)
        {
            GameElement go = getFreeGameObject();
            go.init( objs[i]&0xF000, objs[i]&0x0FFF,0);
            mStateUpdateEvents.addEvent(go);
        }
        return mStateUpdateEvents;
    }

    public Events getRemovalEvents()
    {
        int[] objs = GameLogic.getRemovedObjects();
        int total = objs.length - 1;
        for(int i = total; i >= 0; i--)
        {
            GameElement go = getFreeGameObject();
            go.init( objs[i]&0xF000, objs[i]&0x0FFF,0);
            mRemovalEvents.addEvent(go);
        }
        return mRemovalEvents;
    }

    public int getPositionXFromCell(int cellposx)
    {
        return (FIELD_X1 + (CELLSIZE_X * cellposx));
    }

    public int getPositionYFromCell(int cellposy)
    {
        return (FIELD_Y1 + (CELLSIZE_Y * cellposy));
    }
}
