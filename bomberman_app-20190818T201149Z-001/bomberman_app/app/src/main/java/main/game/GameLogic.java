package main.game;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import main.Globals;
import main.Logger;

import static main.Constants.*;
import static main.game.GameStateBuffer.OBJ_BLOCK;
import static main.game.GameStateBuffer.OBJ_BOMB;
import static main.game.GameStateBuffer.OBJ_CRATE;
import static main.game.GameStateBuffer.OBJ_EXPLN;
import static main.game.GameStateBuffer.OBJ_PLAYR;

public class GameLogic
{
    private final String TAG = "GameLogicManager";
    private final int mCapacity = 50;
    private Events mStateUpdateEvents;
    private Events mKillEvents;
    private Events mBombEvents;
    private Events mRemovalEvents;
    private int mNextFreeGameObject;
    private int mNextFreeSlotForGameObject;
    private final GameObject[] mGameObjectPool;
    private final GameStateBuffer mGameStateBuffer;
    private SparseArray<GameObject> mAllObjects;
    private int mGameTicker;
    private int[][] mFieldMap;

    @SuppressLint("UseSparseArrays")
    public GameLogic(int maxobjects)
    {
        mGameTicker = 0;
        mAllObjects = new SparseArray<>(mCapacity);

        mStateUpdateEvents = new Events();
        mRemovalEvents = new Events();
        mKillEvents = new Events();
        mBombEvents = new Events();
        mFieldMap = new int[NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS];
        mGameObjectPool = new GameObject[maxobjects];
        mNextFreeGameObject = 0;
        mNextFreeSlotForGameObject = 0;
        mGameStateBuffer = new GameStateBuffer();

        GameObject.setGameStateBuffer(mGameStateBuffer);
    }

    public GameObject getFreeGameObject(){
        GameObject go = mGameObjectPool[mNextFreeGameObject];
        mNextFreeGameObject=(mNextFreeGameObject+1)%mGameObjectPool.length;;
        if(null == go){
            go = new GameObject();
        }
        return go;
    }

    public void returnGameObjectToPool(GameObject go){
        mGameObjectPool[mNextFreeSlotForGameObject] = go;
        mNextFreeSlotForGameObject+=(1)%mGameObjectPool.length;
    }


    private int addPlayer(int posx, int posy)
    {   int playerslot = mGameStateBuffer.getFreeSlot(OBJ_PLAYR);
        mGameStateBuffer.setStateComplete(OBJ_PLAYR, playerslot, posx, posy, STATE_ALIVE, INPUT_NONE);
        GameObject go = getFreeGameObject();
        go.init(OBJ_PLAYR, playerslot,0);
        mAllObjects.put(go.getUniqeueID(), go);
        return -1;
    }

    public void setGameTicker(int ticker){
        mGameTicker = ticker;
    }

    public SparseArray<GameObject> getGameObjects(){
        return mAllObjects;
    }

    /* Add normal object_block*/
    private void addBlock(int posx, int posy)
    {   int blockslot = mGameStateBuffer.getFreeSlot(OBJ_BLOCK);
        mGameStateBuffer.setStateComplete(OBJ_BLOCK, blockslot, posx, posy, STATE_ALIVE, INPUT_NONE);
        GameObject go = getFreeGameObject();
        go.init(OBJ_BLOCK, blockslot,0);
        int uniqueid = go.getUniqeueID();
        mAllObjects.put(uniqueid, go);
    }

    /* Add a player robot_bomb*/
    private void addBomb(int posx, int posy, long bombtimer, int bombowner, int strength)
    {
        int bombslot = mGameStateBuffer.getFreeSlot(OBJ_BOMB);
        mGameStateBuffer.setStateComplete(OBJ_BOMB, bombslot, posx, posy, STATE_ALIVE, INPUT_NONE);
        GameObject go = getFreeGameObject();
        go.init(OBJ_BOMB, bombslot,0);
        int uniqueid = go.getUniqeueID();
        mAllObjects.put(uniqueid, go);
    }

    /**/
    private void addCrate(int posx, int posy)
    {
        int crateslot = mGameStateBuffer.getFreeSlot(OBJ_CRATE);
        mGameStateBuffer.setStateComplete(OBJ_CRATE, crateslot, posx, posy, STATE_ALIVE, INPUT_NONE);
        GameObject go = getFreeGameObject();
        go.init(OBJ_CRATE, crateslot,0);
        int uniqueid = go.getUniqeueID();
        mAllObjects.put(uniqueid, go);
    }

    /* Add Explosion */

    /*
    private void addExplosion( Bomb firstbomb)
    {
        int mapWidth = mFieldMap[0].length;
        int mapHeight = mFieldMap.length;
        int strength;
        int owner;
        int explosionid;
        int cellposx;
        int cellposy;

        ConcurrentLinkedQueue<Bomb> triggeredBombs = new ConcurrentLinkedQueue<>();
        triggeredBombs.add(firstbomb);


        int[] state = mGameStateBuffer.getStateData(OBJ_BOMB);
        do
        {

            int explosionslot = mGameStateBuffer.getFreeSlot(OBJ_EXPLN);
            Bomb bomb = triggeredBombs.remove();
            state[bomb.mObjectStateOffset+2] = STATE_DEAD;
            cellposx = bomb.getCellFromCenteredX();
            cellposy = bomb.getCellFromCenteredY();
            strength = bomb.mRadius;
            owner = bomb.mOwner;

            explosionid = OBJ_EXPLN | explosionslot;

            mPlayers.get(owner).mBombCount++;

            int[] cellsCovered = {0, 0, 0, 0};
            boolean[] expansionUnderway = {true, true, true, true};
            mFieldMap[cellposx][cellposy] = (explosionid);

            for(int direction = 0; direction < expansionUnderway.length && expansionUnderway[direction]; direction++)
            {
                // Continue only if the exposion didn't hit a wall
                for(int radius = 1; radius <= strength; radius++)
                {
                    int posx = 0;
                    int posy = 0;

                    switch(direction)
                    {
                        case 0: // mLeft
                            posx = cellposx - radius;
                            posy = cellposy;
                            break;
                        case 1: // mRight
                            posx = cellposx + radius;
                            posy = cellposy;
                            break;
                        case 2: // mUp
                            posx = cellposx;
                            posy = cellposy - radius;
                            break;
                        case 3: // mDown
                            posx = cellposx;
                            posy = cellposy + radius;
                            break;
                    }

                    if(posx < 0 || posx == mapWidth || posy < 0 || posy == mapHeight)
                    {
                        expansionUnderway[direction] = false;
                        continue;
                    }

                    int id = mFieldMap[posx][posy];
                    if(id != OBJ_NONE)
                    {
                        GameObject go = mAllObjects.get(id);
                        int type = go.mObjectType;
                        switch(type)
                        {
                            case OBJ_BOMB:
                                triggeredBombs.add((Bomb) go);
                                expansionUnderway[direction] = false;
                                break;
                            case OBJ_CRATE:
                                mGameStateBuffer.setState(OBJ_CRATE,go.mObjectStateOffset,STATE_DEAD);
                                expansionUnderway[direction] = false;
                                cellsCovered[direction]++;
                                break;
                            case OBJ_BLOCK:
                            case OBJ_EXPLN:
                                expansionUnderway[direction] = false;
                                break;
                        }
                    } else
                    {
                        mFieldMap[posx][posy] = (explosionid);
                        cellsCovered[direction]++;
                    }
                }
            }


            mGameStateBuffer.setStateComplete(OBJ_EXPLN, explosionslot, getPositionXFromCell(cellposx), getPositionYFromCell(cellposy),STATE_ALIVE);
            Explosion explosion = new Explosion(explosionslot, cellposx, cellposy, owner, strength, cellsCovered);
            explosion.updateBoundingBoxes(mGameStateBuffer.getStateData(OBJ_EXPLN));
            int uniqueid = explosion.getUniqeueID();
            mExplosions.put(uniqueid, explosion);
            mAllObjects.put(uniqueid, explosion);

        } while(triggeredBombs.size() > 0);
    }*/

    public void updateGameOfflineInput(int input){
        updatePlayerInput(0, (byte)input);
    }

    public int getGameTicker()
    {
        return mGameTicker;
    }


    public GameObject getGameObject(int key)
    {
        return mAllObjects.get(key);
    }

    public SparseArray<GameObject> getObjects()
    {
        return mAllObjects;
    }

    public void createGameOffline()
    {
        deleteAllGameObjects();
        createGameLevel(Globals.selectedMap);
        createPlayerAtDefaultPosition(0);
    }

    public void updateGameTicker()
    {
        mGameTicker = (mGameTicker + 1) % 256;
        mGameStateBuffer.initCurrentState(mGameTicker);
    }

    public void deleteAllGameObjects()
    {
        // TODO reset state
        mGameStateBuffer.resetState();
        mRemovalEvents.resetEvents();
        mBombEvents.resetEvents();
        mStateUpdateEvents.resetEvents();
        mAllObjects.clear();
    }

    public void updatePlayerInput(int index, byte playerinput)
    {

        GameObject player = mAllObjects.valueAt(index);
        if(player != null)
        {
            mGameStateBuffer.setStateInput(player.mObjectStateOffset,  playerinput);
        }
    }


    public void updateGameState(int dt)
    {
        // Update according to priority which is set by the type: mPlayers > Bombs > Explosions> Crates > Blocks > Items
        int total = mAllObjects.size() - 1;
        for(int i = total; i >= 0; i--)
        {
            GameObject go = mAllObjects.valueAt(i);

            int[] state = go.state;
            go.updateState(dt, mBombEvents);
            mStateUpdateEvents.addEvent(go);
            if(state != null &&( state[go.mObjectStateOffset] != go.getPositionX()||state[go.mObjectStateOffset+1] != go.getPositionY())){
               //Logger.log(Logger.VERBOSE,TAG,"dt:"+dt+" Yspeed:"+(state[go.mObjectStateOffset+1]-go.getPositionY())+" Xspeed:"+(state[go.mObjectStateOffset]-go.getPositionX()));
            }

            if(go.mObjectType == OBJ_PLAYR){

                if(go.stayInsideField()){
                    go.updateBoundingBoxes();
                }

                checkCollision(go);
            }

            if(go.getCellFromCenteredX() == 11){
                total = total;
            }
            mFieldMap[go.getCellFromCenteredX()][go.getCellFromCenteredY()] = go.getUniqeueID();
        }

    }

    private void checkCollision(GameObject playr)
    {
        int cellposx = playr.getCellFromCenteredX();
        int cellposy = playr.getCellFromCenteredY();
        boolean loop = true;

        // Check the cell the player is on
        int id = mFieldMap[cellposx][cellposy];
        if(0 != id)
        {
            GameObject go = mAllObjects.get(id);

            if(collisionHelper(go,playr)){
                loop = false;
            }
        }

        // Check 8 nearest cells against collision
        for(int b = cellposx - 1; b <= (cellposx + 1) && loop; b++)
        {
            if(b < 0 || b >= NUMBER_OF_X_CELLS)
                continue;

            for(int c = cellposy - 1; c <= (cellposy + 1) && loop; c++)
            {
                if(c < 0 || c >= NUMBER_OF_Y_CELLS)
                    continue;

                id = mFieldMap[b][c];
                if(id != 0)
                {
                    GameObject go = mAllObjects.get(id);

                    if(collisionHelper(go,playr)){
                        loop = false;
                    }
                }

            }
        }
    }

    private boolean collisionHelper(GameObject collidee, GameObject collider){
        boolean collided = false;
        switch(collidee.mObjectType)
        {
            // Collision with an Explosion
            case OBJ_EXPLN:
                if(collider.collisionCheck(collidee))
                {
                    //if(state[player.mObjectStateOffset+2] != STATE_DEAD) TODO
                    //{

                    collided = true;
                    //}
                }
                break;
            // Collision with a Crate
            case OBJ_CRATE:
                if(collider.collisionCheck(collidee))
                {
                    collider.correctPosition();
                    collided = true;
                }
                break;
            // Collision with a Block
            case OBJ_BLOCK:
                if(collider.collisionCheck(collidee))
                {
                    collider.correctPosition();
                    collided = true;
                }
                break;
        }
        return collided;
    }



    public int createPlayerAtDefaultPosition(int pos)
    {
        /* Create BluetoothGattServer Player */
        int x = getPositionXFromCell(STARTING_CELL_POSITIONS[pos][0]);
        int y = getPositionXFromCell(STARTING_CELL_POSITIONS[pos][1]);
        return addPlayer(x, y);
    }


    public void createGameLevel(int selectlevel)
    {
        int[][] level = LEVELS[selectlevel];
        for(int i = 0; i < LEVELS[selectlevel][0].length; i++)
        {
            for(int j = 0; j < LEVELS[selectlevel].length; j++)
            {
                int x = getPositionXFromCell(j);
                int y = getPositionYFromCell(i);
                if(level[i][j] == 1)
                {
                   addBlock(x, y);
                } else if(level[i][j] == 2)
                {
                    addCrate(x, y);
                } else
                {
                    mFieldMap[j][i] = 0;
                }
            }
        }
    }


    public Events getUpdateEvents()
    {
        return mStateUpdateEvents;
    }

    public Events getRemovalEvents()
    {
        return mRemovalEvents;
    }

    public GameStateBuffer getGameStateBuffer(){
        return mGameStateBuffer;
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
