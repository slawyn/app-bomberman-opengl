package main.game;

import main.Constants;
import main.Logger;
import main.rendering.animation.DebugObject;
import main.rendering.animation.RenderObject;

import static main.Constants.BLOCK_BOX_HEIGHT;
import static main.Constants.BLOCK_BOX_OFFSET_X;
import static main.Constants.BLOCK_BOX_OFFSET_Y;
import static main.Constants.BLOCK_BOX_WIDTH;
import static main.Constants.BOMB_BOX_HEIGHT;
import static main.Constants.BOMB_BOX_OFFSET_X;
import static main.Constants.BOMB_BOX_OFFSET_Y;
import static main.Constants.BOMB_BOX_WIDTH;
import static main.Constants.BOMB_TIMER;
import static main.Constants.BOUNDS;
import static main.Constants.CELLSIZE_X;
import static main.Constants.CELLSIZE_Y;
import static main.Constants.FIELD_X1;
import static main.Constants.FIELD_X2;
import static main.Constants.FIELD_Y1;
import static main.Constants.FIELD_Y2;
import static main.Constants.GAME_HEIGHT;
import static main.Constants.GAME_WIDTH;
import static main.Constants.INPUT_MOVE_DOWN;
import static main.Constants.INPUT_MOVE_LEFT;
import static main.Constants.INPUT_MOVE_RIGHT;
import static main.Constants.INPUT_MOVE_UP;
import static main.Constants.INPUT_NONE;
import static main.Constants.INPUT_PLACE_BOMB;
import static main.Constants.PLAYER_BASE_SPEED;
import static main.Constants.PLAYER_BOMB_EXPLOSION_STRENGTH;
import static main.Constants.PLAYER_BOMB_STARTING_AMOUNT;
import static main.Constants.PLAYER_BOX_HEIGHT;
import static main.Constants.PLAYER_BOX_OFFSET_X;
import static main.Constants.PLAYER_BOX_OFFSET_Y;
import static main.Constants.PLAYER_BOX_WIDTH;
import static main.Constants.STATE_ALIVE;
import static main.Constants.STATE_DEAD;
import static main.Constants.STATE_DETONATED;
import static main.Constants.STATE_MOVEDOWN;
import static main.Constants.STATE_MOVELEFT;
import static main.Constants.STATE_MOVERIGHT;
import static main.Constants.STATE_MOVEUP;
import static main.game.GameStateBuffer.OBJ_BLOCK;
import static main.game.GameStateBuffer.OBJ_BOMB;
import static main.game.GameStateBuffer.OBJ_CRATE;
import static main.game.GameStateBuffer.OBJ_EXPLN;
import static main.game.GameStateBuffer.OBJ_PLAYR;

public class GameObject extends EventObject
{
    public Hitbox[] mBoxes;
    public int state[];
    public int mObjectStateOffset;
    public int mObjectType;
    public int mObjectSubtype;
    public int[] mLocalIntParameters;
    public float[] mLocalFloatParameters;
    private Hitbox mCollidedWith;
    private RenderObject mBoundRenderObject;
    private DebugObject mBoundDebugObject;
    public static GameStateBuffer mGameStateBuffer;


    public void init(int type, int slot, int subtype){

        mBoundRenderObject = null;
        mBoundDebugObject = null;
        mLocalIntParameters = null;
        mLocalFloatParameters = null;
        mObjectType = type;
        mObjectStateOffset = slot;
        mObjectSubtype = subtype;

        switch(type){
            case OBJ_PLAYR:
                createBoundingBoxes(1);
                addBoundingBox(0,(PLAYER_BOX_OFFSET_X), (PLAYER_BOX_OFFSET_Y), (PLAYER_BOX_WIDTH), (PLAYER_BOX_HEIGHT));
                mLocalIntParameters = new int[5];
                mLocalIntParameters[0] = BOMB_TIMER;                                // Bomb timer
                mLocalIntParameters[1] = PLAYER_BOMB_STARTING_AMOUNT;               // Number of bombs
                mLocalIntParameters[2] = PLAYER_BOMB_EXPLOSION_STRENGTH;            // radius
                mLocalIntParameters[3] = PLAYER_BASE_SPEED;
                break;
            case OBJ_BLOCK:
                createBoundingBoxes(1);
                addBoundingBox(0,(BLOCK_BOX_OFFSET_X),(BLOCK_BOX_OFFSET_Y),(BLOCK_BOX_WIDTH), (BLOCK_BOX_HEIGHT));
                break;
            case OBJ_CRATE:
                createBoundingBoxes(1);
                addBoundingBox(0,(Constants.CRATE_BOX_OFFSET_X),(Constants.CRATE_BOX_OFFSET_Y),(Constants.CRATE_BOX_WIDTH), (Constants.CRATE_BOX_HEIGHT));
                break;
            case OBJ_BOMB:
                mLocalIntParameters = new int[3];
                mLocalIntParameters[0] = BOMB_TIMER;                                // Bomb timer
                mLocalIntParameters[1] = PLAYER_BOMB_EXPLOSION_STRENGTH;            // Bomb timer
                mLocalIntParameters[2] = 1;                                         // Clip state
                createBoundingBoxes(1);
                addBoundingBox(0, BOMB_BOX_OFFSET_X, BOMB_BOX_OFFSET_Y, BOMB_BOX_WIDTH, BOMB_BOX_HEIGHT);
                break;
            case OBJ_EXPLN:
                createBoundingBoxes(2);
                /*

                 mEpicenterX = cellx;
        mEpicenterY = celly;
        timer = EXPLOSION_TIME;
        owner = owner_;
        mStrength = strength_;
        mLeft = radius[0];
        mRight = radius[1];
        mUp = radius[2];
        mDown = radius[3];
                addBoundingBox(0,(-mLeft * CELLSIZE_X),  0,((mRight + mLeft + 1) * CELLSIZE_X), (CELLSIZE_Y));
                addBoundingBox(1, (0), (-mUp * CELLSIZE_Y), (CELLSIZE_X), ((mUp + mDown + 1) * CELLSIZE_Y));*/
                break;
        }




        mObjectSubtype = 0;     // setup
        //updateBoundingBoxes();
    }

    /*
    public void updateExplosion(int dt){
        switch(state[mObjectStateOffset+2])
        {
            case STATE_ALIVE:
                timer -= dt;
                if(timer <= 0)
                {
                    state[mObjectStateOffset+2] = STATE_DEAD;
                }
                break;
            case STATE_DEAD:
                break;
        }
    }*/


    public int updateState(int dt, Events triggerevents){
        state = mGameStateBuffer.getStateData(mObjectType);
        switch(mObjectType){
            case OBJ_BLOCK:
                break;
            case OBJ_PLAYR:
                int input = state[mObjectStateOffset+3];
                int newstate = 0;
                switch(input&0x0F){
                    case INPUT_MOVE_DOWN:
                        newstate = STATE_MOVEDOWN;
                    break;
                    case INPUT_MOVE_UP:
                        newstate = STATE_MOVEUP;
                        break;
                    case INPUT_MOVE_LEFT:
                        newstate = STATE_MOVELEFT;
                        break;
                    case INPUT_MOVE_RIGHT:
                        newstate = STATE_MOVERIGHT;
                        break;
                    case INPUT_NONE:
                        newstate = STATE_ALIVE;
                        break;
                }

                if((input & 0xF0) == INPUT_PLACE_BOMB){
                    triggerevents.addEvent(this);
                }
                int speed = 0;

                switch (newstate) {
                    case STATE_ALIVE:
                        break;
                    case STATE_MOVEDOWN:
                        speed = dt * PLAYER_BASE_SPEED/10;
                        state[mObjectStateOffset+1] +=(speed);
                        break;
                    case STATE_MOVEUP:
                        speed = dt * PLAYER_BASE_SPEED/10;;
                        state[mObjectStateOffset+1] -=(speed);
                        break;
                    case STATE_MOVELEFT:
                        speed = dt * PLAYER_BASE_SPEED/10;;
                        state[mObjectStateOffset]-=(speed);
                        break;
                    case STATE_MOVERIGHT:
                        speed = dt * PLAYER_BASE_SPEED/10;;
                        state[mObjectStateOffset]+=(speed);
                        break;
                    case STATE_DEAD:
                        break;
                }



                state[mObjectStateOffset+2] = newstate;
                break;
            case OBJ_CRATE:
                break;
            case OBJ_BOMB:
                switch(state[mObjectStateOffset+2])
                {
                    case STATE_ALIVE:
                        if((state[mObjectStateOffset+3]-=dt)<=0){
                            state[mObjectStateOffset + 2] = STATE_DETONATED;
                        }
                        if(mLocalIntParameters[0]== 0)
                        {
                            mLocalIntParameters[0] = -1;
                        }
                        break;
                    case STATE_DETONATED:
                        break;
                    case STATE_DEAD:
                        break;
                }
                break;
            case OBJ_EXPLN:
                switch(state[mObjectStateOffset+2])
                {
                    case STATE_ALIVE:
                        if((mLocalIntParameters[0] -= dt) <= 0)
                        {
                            state[mObjectStateOffset+2] = STATE_DEAD;
                        }
                        break;
                    case STATE_DEAD:
                        break;
                }
                break;
        }

        updateBoundingBoxes();
        return 0;
    }


    public static void setGameStateBuffer(GameStateBuffer gsb){
        mGameStateBuffer = gsb;
    }

    public int getUniqeueID(){
        return mObjectType| mObjectStateOffset;
    }

    public void createBoundingBoxes(int nboxes){
        mBoxes = new Hitbox[nboxes];
    }

    public void addBoundingBox(int nbox, int xoffset, int yoffset, int xsize, int ysize) {
        mBoxes[nbox] = new Hitbox(xoffset, yoffset, xsize, ysize);
    }

    public int getCellFromCenteredX() {
        return  (((mBoxes[0].mLeft + mBoxes[0].sizeX /2) - Constants.FIELD_X1) / CELLSIZE_X);
    }

    public int getCellFromCenteredY() {
        return (((mBoxes[0].mTop + mBoxes[0].sizeY / 2) - Constants.FIELD_Y1) / CELLSIZE_Y);
    }

    public int getPositionX(){
        return state[mObjectStateOffset];
    }

    public int getPositionY(){
        return state[mObjectStateOffset+1];
    }

    public int getState(){
        return state[mObjectStateOffset+2];
    }

    public boolean collisionCheck(GameObject go){
        Hitbox[] boxes = go.mBoxes;
        for(int idx = 0;idx<boxes.length;idx++){
                if(mBoxes[0].intersects(boxes[idx])){
                    mCollidedWith = boxes[idx];
                    return true;
                }
        }
        return false;
    }



    public void correctPosition() {

        int[] state = mGameStateBuffer.getStateData(mObjectType);
        Hitbox box1 = mBoxes[0];
        Hitbox box2 = mCollidedWith;

        /* Minkowski sum, simplified */
        int w = ((box1.sizeX + box2.sizeX));
        int h = ((box1.sizeY + box2.sizeY));
        int dx = ((box2.mLeft + box2.mRight) - (box1.mLeft + box1.mRight));
        int dy = ((box2.mBottom + box2.mTop) - (box1.mTop + box1.mBottom));

        /* Collision! */
        int wy = (w * dy);
        int hx = (h * dx);

        if (wy > hx)
            if (wy > -hx)        /* collision at the mUp */
                state[mObjectStateOffset +1] = ( box2.mTop - (box1.sizeY + box1.offsetY));
            else                /* on the mRight */
                state[mObjectStateOffset] =( box2.mRight - box1.offsetX);
        else if (wy > -hx)       /* on the mLeft */
            state[mObjectStateOffset] = (box2.mLeft - (box1.sizeX + box1.offsetX));
        else                    /* at the mDown */
            state[mObjectStateOffset +1] = (box2.mBottom - box1.offsetY);
    }

    /**/
    public void updateBoundingBoxes() {
        int l = mBoxes.length;
        if(l == 1){
            mBoxes[0].updateEdges(state[mObjectStateOffset], state[mObjectStateOffset + 1]);
        }
        else
        {
            for(int idx = 0; idx < l; idx++)
            {
                mBoxes[idx].updateEdges(state[mObjectStateOffset], state[mObjectStateOffset + 1]);
            }
        }

    }

    public void bindRenderObject(RenderObject ro){
        mBoundRenderObject = ro;
    }

    public void bindDebugObject(DebugObject dbgo){
        mBoundDebugObject = dbgo;
    }

    public DebugObject getBoundDebugObject(){
        return mBoundDebugObject;
    }

    public RenderObject getBoundRenderObject(){
        return mBoundRenderObject;
    }

    public boolean stayInsideField() {
        boolean status = false;
        Hitbox box1 = mBoxes[0];
        /* Horizontally */
        if ((box1.mRight) > (FIELD_X2))
        {
            state[mObjectStateOffset] = (FIELD_X2 - (box1.offsetX + box1.sizeX));
            status = true;
        }else if (box1.mLeft < FIELD_X1)
        {
            state[mObjectStateOffset] = (FIELD_X1 - box1.offsetX);
            status = true;
        }
        else{
            status = true;
        }
        /* vertically */
        if (box1.mTop < FIELD_Y1)
        {
            state[mObjectStateOffset + 1] = (FIELD_Y1 - box1.offsetY);
            status = true;
        }else if ((box1.mBottom) > (FIELD_Y2))
        {
            state[mObjectStateOffset + 1] = (FIELD_Y2 - (box1.offsetY + box1.sizeY));
            status = true;
        }
        else{
            status = true;
        }
        return status;
    }

    public void stayInsideScreen() {

        Hitbox box1 = mBoxes[0];
        /* Horizontally */
        if ((box1.mRight) > (GAME_WIDTH - BOUNDS))
            state[mObjectStateOffset]  = (int)(GAME_WIDTH - (box1.offsetX + box1.sizeX + BOUNDS));
        else if (box1.mLeft < BOUNDS)
            state[mObjectStateOffset]  = (BOUNDS - box1.offsetX);

        /* vertically */
        if (box1.mTop < BOUNDS)
            state[mObjectStateOffset +1]  = (BOUNDS - box1.offsetY);
        else if ((box1.mBottom) > (GAME_HEIGHT - BOUNDS))
            state[mObjectStateOffset +1]  =  (int)(GAME_HEIGHT - (box1.offsetY + box1.sizeY + BOUNDS));
    }

    public int[] getInts(){
        return mLocalIntParameters;
    }
}
