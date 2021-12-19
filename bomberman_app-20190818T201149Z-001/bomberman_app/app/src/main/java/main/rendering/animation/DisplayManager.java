package main.rendering.animation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.game.bomber.R;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import main.Globals;
import main.game.Events;
import main.game.GameLogic;
import main.game.SceneManager;
import main.game.GameObject;
import main.game.sceneobjects.SceneObject;
import main.rendering.VertexArray;

import static main.Constants.*;
import static main.Constants.DEBUG_DRAW_HITBOXES;
import static main.game.GameStateBuffer.OBJ_BLOCK;
import static main.game.GameStateBuffer.OBJ_BOMB;
import static main.game.GameStateBuffer.OBJ_CRATE;
import static main.game.GameStateBuffer.OBJ_EXPLN;
import static main.game.GameStateBuffer.OBJ_ITEM;
import static main.game.GameStateBuffer.OBJ_PLAYR;
import static main.game.SceneManager.SOBJ_BACKGROUND;
import static main.game.SceneManager.SOBJ_BACKGROUND_LOADING;
import static main.game.SceneManager.SOBJ_BUTTON;
import static main.game.SceneManager.SOBJ_TOUCH;

public class DisplayManager
{
    private final String TAG = "AnimatedObjectManager";
    // every type has a subset as vector
    public SparseArray<int[]> mAnimationParameters;
    public SparseArray<Vector<int[]>> mAnimationSequences;
    private Vector<Layer> mLayers;
    public float mScaleFactor = 1;
    public int mGamePortWidth = 1920;
    public int gamePortHeight = 1080;
    public int gamePortXOffset = 0;
    public int gamePortYOffset = 0;
    public int gameBombXOffset = (CELLSIZE_X - 136) / 2;
    public int gameBombYOffset = (((CELLSIZE_Y) - 136) / 2);
    public int gameObjectXOffset = (CELLSIZE_X - 136) / 2;
    public int gameObjectYOffset = (-((136 - (BLOCK_BOX_HEIGHT + BLOCK_BOX_OFFSET_Y * 2))));
    public int gameExplosionXOffset = 0;
    public int gameExplosionYOffset = 0 + ((CELLSIZE_Y - 100));
    public int gamePlayerXOffset = 0;
    public int gamePlayerYOffset = 0;
    public int gameTouchXOffset = (-(300 / 2));
    public int gameTouchYOffset = (-(300 / 2));
    public int gameLoadingXOffset = (mGamePortWidth) / 2 - 300;
    public int gameLoadingYOffset = (gamePortHeight) / 2 - 300;
    public int gameButtonXOffset0 = gamePortXOffset;
    public int gameButtonYOffset0 = gamePortYOffset;
    private SparseArray<VertexArray> mVertexData;

    private int mTotalNumberOfRenderObjects;
    private int mTotalNumberOfAnimatedObjects;
    public RenderObject[] mFreeRenderObjects;
    public AnimatedObject[] mFreeAnimatedObjects;

    public LoaderConfig mLoaderConfig;
    private ConcurrentLinkedQueue<int[]> mLoadables;

    /* Animation reference */
    private final int ANIMATION_NONE = 0;
    private final int ANIMATION_BACKGROUND = 1;
    private final int ANIMATION_LOADING = 2;
    private final int ANIMATION_FIELD = 10;
    private final int ANIMATION_PLAYER_WALK_LEFT = 20;
    private final int ANIMATION_PLAYER_WALK_RIGHT = 30;
    private final int ANIMATION_PLAYER_WALK_UP = 40;
    private final int ANIMATION_PLAYER_WALK_DOWN = 50;
    private final int ANIMATION_PLAYER_STAND = 60;
    private final int ANIMATION_PLAYER_BOMB = 70;
    private final int ANIMATION_PLAYER_DYING = 80;
    private final int ANIMATION_PLAYER_EXPLOSION_CENTER = 90;
    private final int ANIMATION_PLAYER_EXPLOSION_UPDOWN = 100;
    private final int ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT = 110;
    private final int ANIMATION_BUTTON = 120;
    private final int ANIMATION_BUTTON_PRESSED = 125;
    private final int ANIMATION_OBJECT_CRATE = 130;
    private final int ANIMATION_OBJECT_BLOCK = 140;
    private final int ANIMATION_TOUCH_MARKER = 150;
    private final int ANIMATION_NUMBERS = 160;

    private final int DRAWABLES[][] = {
            // textureoffset, lines, columns, frametime, runonce
            {ANIMATION_BACKGROUND, R.drawable.sprt_background, 4},
            {ANIMATION_LOADING, R.drawable.sprt_loading, 7},
            {ANIMATION_TOUCH_MARKER, R.drawable.touch_square, 10},
            {ANIMATION_TOUCH_MARKER, R.drawable.touch_circle, 10},
            {ANIMATION_BUTTON, R.drawable.buttonclient, 1},//0
            {ANIMATION_BUTTON_PRESSED, R.drawable.buttonclient_pressed, 1},
            {ANIMATION_BUTTON, R.drawable.buttonserver, 1},//1
            {ANIMATION_BUTTON_PRESSED, R.drawable.buttonserver_pressed, 1},
            {ANIMATION_BUTTON, R.drawable.buttonbluetooth, 1},//2
            {ANIMATION_BUTTON_PRESSED, R.drawable.buttonbluetooth_pressed, 1},
            {ANIMATION_BUTTON, R.drawable.buttonwlan, 1},//3
            {ANIMATION_BUTTON_PRESSED, R.drawable.buttonwlan_pressed, 1},
            {ANIMATION_BUTTON, R.drawable.buttonoffline, 1},//4
            {ANIMATION_BUTTON_PRESSED, R.drawable.buttonoffline_pressed, 1},
            {ANIMATION_BUTTON, R.drawable.buttonback, 1},//5
            {ANIMATION_BUTTON_PRESSED, R.drawable.buttonback_pressed, 1},

            {ANIMATION_BACKGROUND, R.drawable.sprt_background_field, 4},
            {ANIMATION_PLAYER_WALK_LEFT, R.drawable.robot_walk_left, 12},
            {ANIMATION_PLAYER_WALK_LEFT, R.drawable.robot_walk_left2, 12},
            {ANIMATION_PLAYER_WALK_RIGHT, R.drawable.robot_walk_right, 12},
            {ANIMATION_PLAYER_WALK_RIGHT, R.drawable.robot_walk_right2, 12},
            {ANIMATION_PLAYER_WALK_UP, R.drawable.robot_walk_up, 11},
            {ANIMATION_PLAYER_WALK_UP, R.drawable.robot_walk_up2, 11},
            {ANIMATION_PLAYER_WALK_DOWN, R.drawable.robot_walk_down, 11},
            {ANIMATION_PLAYER_WALK_DOWN, R.drawable.robot_walk_down2, 11},
            {ANIMATION_PLAYER_STAND, R.drawable.robot_stand, 1},
            {ANIMATION_PLAYER_STAND, R.drawable.robot_stand2, 1},
            {ANIMATION_PLAYER_BOMB, R.drawable.sprt_bomb, 15},
            {ANIMATION_PLAYER_BOMB, R.drawable.sprt_bomb_2, 15},
            {ANIMATION_PLAYER_DYING, R.drawable.robot_dying, 14},
            {ANIMATION_PLAYER_DYING, R.drawable.robot_dying2, 14},
            {ANIMATION_PLAYER_EXPLOSION_CENTER, R.drawable.robot_explosion_center, 3},
            {ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT, R.drawable.robot_explosion_horizontal, 3},
            {ANIMATION_PLAYER_EXPLOSION_UPDOWN, R.drawable.robot_explosion_vertical, 3},
            {ANIMATION_OBJECT_CRATE, R.drawable.object_crate, 1},
            {ANIMATION_OBJECT_BLOCK, R.drawable.object_block, 1},
            {ANIMATION_NUMBERS, R.drawable.numbers, 10}

    };


    public DisplayManager()
    {
        mAnimationParameters = new SparseArray<>();
        mAnimationSequences = new SparseArray<>();
        mLayers = new Vector<>();
        mLayers.add(new Layer(10));   // background
        mLayers.add(new Layer(50));   // game layer
        mLayers.add(new Layer(10));   // foreground
        mLayers.add(new Layer(10));   // info

        mTotalNumberOfRenderObjects = NUMBER_OF_RENDER_OBJECTS;
        mTotalNumberOfAnimatedObjects = NUMBER_OF_ANIMATED_OBJECTS;

        mFreeRenderObjects = new RenderObject[mTotalNumberOfRenderObjects];
        mFreeAnimatedObjects = new AnimatedObject[mTotalNumberOfAnimatedObjects];

        // Precreate
        for(int idx = 0; idx < mTotalNumberOfRenderObjects; idx++)
        {
            mFreeRenderObjects[idx] = new RenderObject();
        }

        for(int idx = 0; idx < mTotalNumberOfAnimatedObjects; idx++)
        {
            mFreeAnimatedObjects[idx] = new AnimatedObject();
        }

        if(DEBUG_DRAW_HITBOXES)
        {
            mLayers.add(new Layer(50));
        }

        mLoadables = new ConcurrentLinkedQueue<>();
        createAnimations();
    }

    public BitmapFactory.Options getLoaderOptions()
    {
        return mLoaderConfig.opts;
    }

    public int getResourceCount()
    {
        return DRAWABLES.length;
    }

    public void setPrimaries(int width, int height, float scalefactor, int portx, int porty)
    {
        mGamePortWidth = width;
        gamePortHeight = height;
        mScaleFactor = scalefactor;
        gamePortXOffset = portx;
        gamePortYOffset = porty;
    }


    public Vector<Layer> getLayers()
    {
        return mLayers;
    }

    public RenderObject getFreeRenderObjectFromPool()
    {
        RenderObject ro = mFreeRenderObjects[--mTotalNumberOfRenderObjects];
        return ro;
    }

    public AnimatedObject getFreeAnimatedObjectFromPool()
    {
        AnimatedObject ao = mFreeAnimatedObjects[--mTotalNumberOfAnimatedObjects];
        //Logger.log(Logger.INFO, TAG, "get:" + (mTotalNumberOfAnimatedObjects) + " " + ao.toString());
        return ao;
    }

    public void returnRenderObjectToPool(RenderObject ro)
    {
        ro.removeFromGPUthread = true;
        mFreeRenderObjects[mTotalNumberOfRenderObjects++] = ro;

        Vector<AnimatedObject> an = ro.getUsedAnimatedObjects();
        int sz = an.size() - 1;
        for(int idx = sz; idx >= 0; idx--)
        {
            AnimatedObject ao = an.remove(idx);
            mFreeAnimatedObjects[mTotalNumberOfAnimatedObjects] = ao;
            //Logger.log(Logger.DEBUG, TAG, "ret:" + (mTotalNumberOfAnimatedObjects) + " " + ao.toString());
            ++mTotalNumberOfAnimatedObjects;
        }
    }

    private void createGameObjectAnimation(RenderObject ro, GameObject go)
    {
        int type = go.mObjectType;
        ro.init(type);
        AnimatedObject ao = getFreeAnimatedObjectFromPool();

        switch(type)
        {
            case OBJ_PLAYR:
                ro.setInterpolation(true);
                ao.init(gamePlayerXOffset, gamePlayerYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_STAND).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_STAND));
                ro.addAnimation(ao);
                break;
            case OBJ_BLOCK:
                ao.init(gameObjectXOffset, gameObjectYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_OBJECT_BLOCK).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_OBJECT_BLOCK));
                ro.addAnimation(ao);
                break;
            case OBJ_CRATE:
                ao.init(gameObjectXOffset, gameObjectYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_OBJECT_CRATE).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_OBJECT_CRATE));
                ro.addAnimation(ao);
                break;
            case OBJ_BOMB:
                ao.init(gameBombXOffset, gameBombYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_BOMB).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_BOMB));
                ro.addAnimation(ao);
                break;
            case OBJ_EXPLN:


                /*

                int animset = go.mObjectSubtype;


                int strength = ((Explosion) go).mStrength;
                int left = ((Explosion) go).mLeft;
                int right = ((Explosion) go).mRight;
                int up = ((Explosion) go).mUp;
                int down = ((Explosion) go).mDown;

                //  Add animations
                ao.init(gameExplosionXOffset, gameExplosionYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_CENTER).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_CENTER));
                ro.addAnimation(ao);
                for(int i = 1; i <= strength; i++)
                {
                    // Horizontal
                    if(i <= left)
                    {
                        ao = getFreeAnimatedObjectFromPool();
                        ao.init((int) (gameExplosionXOffset + (-i * CELLSIZE_X * mScaleFactor)), gameExplosionYOffset);
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT));
                        ro.addAnimation(ao);
                    }
                    if(i <= right)
                    {
                        ao = getFreeAnimatedObjectFromPool();
                        ao.init((int) (gameExplosionXOffset + (i * CELLSIZE_X * mScaleFactor)), gameExplosionYOffset);
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT));
                        ro.addAnimation(ao);
                    }

                    // Vertical
                    if(i <= up)
                    {
                        ao = getFreeAnimatedObjectFromPool();
                        ao.init(gameExplosionXOffset, (int) (gameExplosionYOffset + (-i * CELLSIZE_Y * mScaleFactor)));
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN));
                        ro.addAnimation(ao);
                    }
                    if(i <= down)
                    {
                        ao = getFreeAnimatedObjectFromPool();
                        ao.init(gameExplosionXOffset, (int) (gameExplosionYOffset + (i * CELLSIZE_Y * mScaleFactor)));
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN));
                        ro.addAnimation(ao);
                    }

                    //SoundManager.playSound(SOUND_EXPLOSION);
                }
*/
                break;
            case OBJ_ITEM:
                break;
        }
        go.bindRenderObject(ro);
        ro.mAnimationState = go.getState();
        mLayers.get(1).addRenderObjectToLayer(ro);
    }

    private void changeGameObjectAnimation(RenderObject ro, GameObject go)
    {
        /**/
        //Logger.log(Logger.INFO,TAG,"State Changed:"+pl.mPreviousState+" ->"+pl.mState);
        switch(go.mObjectType)
        {
            case OBJ_PLAYR:
                AnimatedObject ao = ro.getAnimatedObject(0);
                switch(go.getState())
                {
                    case STATE_ALIVE:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_STAND).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_STAND));
                        break;
                    case STATE_DEAD:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_DYING).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_DYING));
                        //  SoundManager.playSound(SOUND_DEATH);      // TODO- Could be duplicates because of network, need to change
                        break;
                    case STATE_MOVEDOWN:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_WALK_DOWN).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_WALK_DOWN));
                        break;
                    case STATE_MOVEUP:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_WALK_UP).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_WALK_UP));
                        break;
                    case STATE_MOVELEFT:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_WALK_LEFT).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_WALK_LEFT));
                        break;
                    case STATE_MOVERIGHT:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_PLAYER_WALK_RIGHT).get(go.mObjectSubtype), mAnimationParameters.get(ANIMATION_PLAYER_WALK_RIGHT));
                        break;
                }
                break;

            case OBJ_CRATE:
                switch(go.getState())
                {
                    case STATE_ALIVE:
                        break;
                    case STATE_DEAD:
                        break;
                }
                break;
        }
    }

    public void createSceneObjectAnimation(RenderObject ro, SceneObject so)
    {
        int laynum = so.mLayer;

        AnimatedObject ao = getFreeAnimatedObjectFromPool();
        switch(so.mObjectType)
        {
            case SOBJ_BACKGROUND:
                ao.init(gamePortXOffset, gamePortYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_BACKGROUND).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_BACKGROUND));
                ro.addAnimation(ao);
                break;
            case SOBJ_BUTTON:
                ao.init(0, 0);//gameButtonXOffset0, gameButtonYOffset0);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_BUTTON).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_BUTTON));
                ro.addAnimation(ao);
                break;
            case SOBJ_BACKGROUND_LOADING:
                ao.init(gameLoadingXOffset, gameLoadingYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_LOADING).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_LOADING));
                ro.addAnimation(ao);
                break;
            case SOBJ_TOUCH:
                ao.init(gameTouchXOffset, gameTouchYOffset);
                ao.setAnimation(mAnimationSequences.get(ANIMATION_TOUCH_MARKER).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_TOUCH_MARKER));
                ro.addAnimation(ao);
                break;
        }

        mLayers.get(laynum).addRenderObjectToLayer(ro);
    }

    public void changeSceneObjectAnimation(RenderObject ro, SceneObject so)
    {
        switch(so.mObjectType)
        {
            case SOBJ_BUTTON:
                AnimatedObject ao = ro.getAnimatedObject(0);
                switch(so.mState)
                {
                    case STATE_UNPRESSED:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_BUTTON).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_BUTTON));
                        break;
                    case STATE_PRESSED:
                        ao.setAnimation(mAnimationSequences.get(ANIMATION_BUTTON_PRESSED).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_BUTTON_PRESSED));
                        break;
                }
            case SOBJ_BACKGROUND_LOADING:
                ao = ro.getAnimatedObject(0);
                //ao.setAnimation(mAnimationSequences.get(ANIMATION_LOADING).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_BUTTON));
                break;
        }
    }


    public void updateRenderObjects(GameLogic gamemanager, SceneManager scenemanager)
    {
        /* */
        Events goupdates = gamemanager.getUpdateEvents();
        Events soupdates = scenemanager.getUpdateEvents();
        Events soremovals = scenemanager.getRemovalEvents();

        int sz = goupdates.getCount()-1;

        // Update/ Create Renderobjects
        while(sz>=0 ){
            GameObject go = (GameObject) goupdates.getEvent(sz);
            RenderObject ro = go.getBoundRenderObject();

            // if object doesn't exist create it
            if(ro == null)
            {
                ro = getFreeRenderObjectFromPool();
                createGameObjectAnimation(ro, go);
                if(DEBUG_DRAW_HITBOXES)
                {
                    DebugObject debugObject = new DebugObject(go, mScaleFactor);
                    go.bindDebugObject(debugObject);
                    mLayers.get(4).addRenderObjectToLayer(debugObject);
                }

            }
            if(go.getState()!= ro.mAnimationState)
            {
                changeGameObjectAnimation(ro, go);
                ro.mAnimationState = go.getState();
            }

            ro.setAnimationTranslation(go.getPositionX() * mScaleFactor, go.getPositionY() * mScaleFactor);
            ro.updateSortCriteria(go.mBoxes[0].mTop);        // update criteria for z Rendering

            if(DEBUG_DRAW_HITBOXES)
            {
                DebugObject debugObject = go.getBoundDebugObject();
                debugObject.setAnimationTranslation(go.getPositionX() * mScaleFactor, go.getPositionY() * mScaleFactor);
                debugObject.updateSortCriteria(go.mBoxes[0].mTop);        // update criteria for z Rendering
            }
            --sz;
        }

        RenderObject.latch();
        goupdates.resetEvents();


        // Updates RenderObjects from SceneManager
        sz = soupdates.getCount();
        for(int idx = 0; idx < sz; idx++)
        {
            SceneObject so = (SceneObject) soupdates.getEvent(idx);
            RenderObject ro = so.ro;
            if(ro == null)
            {
                ro = getFreeRenderObjectFromPool();
                ro.init(so.mObjectType);
                so.ro = ro;
                createSceneObjectAnimation(ro, so);

            } else if(so.stateChanged())
            {
                changeSceneObjectAnimation(ro, so);
            }
            ro.setAnimationTranslation(so.mPositionX, so.mPositionY);
        }

        soupdates.resetEvents();


        // Remove RendersObjects from SceneManager
        sz = soremovals.getCount();
        for(int idx = 0; idx < sz; idx++)
        {
            SceneObject so = (SceneObject) soremovals.getEvent(idx);
            RenderObject ro = so.ro;
            returnRenderObjectToPool(ro);
        }

        soremovals.resetEvents();
    }

    private void createAnimations()
    {

        // Load animations into OPENGL Space
        for(int i = 0, texIndex = 0; i < DRAWABLES.length; i++)
        {
            final int type = DRAWABLES[i][0];
            final int numOfFrames = DRAWABLES[i][2];
            final int animtextureindex = texIndex;

            Vector<int[]> animsequence = mAnimationSequences.get(type);

            // create if the vector doesn't exist
            if(animsequence == null)
            {
                animsequence = new Vector<>();
                mAnimationSequences.put(type, animsequence);
            }

            // create parameters: first parameter is duration, second is looped
            int[] animparams = mAnimationParameters.get(type);
            if(animparams == null)
            {
                animparams = new int[2];
                mAnimationParameters.put(type, animparams);
            }

            int[] sequence;
            switch(type)
            {
                case ANIMATION_LOADING:
                    sequence = new int[numOfFrames];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }

                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_BACKGROUND:
                    sequence = new int[7];
                    sequence[0] = 0 + animtextureindex;
                    sequence[1] = 1 + animtextureindex;
                    sequence[2] = 2 + animtextureindex;
                    sequence[3] = 3 + animtextureindex;
                    sequence[4] = 2 + animtextureindex;
                    sequence[5] = 1 + animtextureindex;
                    sequence[6] = 0 + animtextureindex;

                    animparams[0] = 300;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_FIELD:
                    sequence = new int[1];
                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_PLAYER_WALK_LEFT:
                case ANIMATION_PLAYER_WALK_RIGHT:
                    sequence = new int[12];

                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 40;//70;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_PLAYER_WALK_UP:
                case ANIMATION_PLAYER_WALK_DOWN:
                    sequence = new int[11];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 40;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_PLAYER_STAND:
                    sequence = new int[1];
                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_PLAYER_BOMB:
                    sequence = new int[34];
                    for(int idx = 0; idx < 20; idx++)
                    {
                        sequence[idx] = 0 + animtextureindex;
                    }

                    for(int idx = 20, k = 1; idx < sequence.length; idx++, k++)
                    {
                        sequence[idx] = k + animtextureindex;
                    }

                    animparams[0] = 100;
                    animparams[1] = 0;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_PLAYER_DYING:
                    sequence = new int[14];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 50;
                    animparams[1] = 0;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_PLAYER_EXPLOSION_CENTER:
                case ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT:
                case ANIMATION_PLAYER_EXPLOSION_UPDOWN:
                    sequence = new int[3];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_BUTTON:
                case ANIMATION_BUTTON_PRESSED:
                    sequence = new int[1];
                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_OBJECT_CRATE:
                case ANIMATION_OBJECT_BLOCK:
                    sequence = new int[1];
                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
                case ANIMATION_TOUCH_MARKER:
                    sequence = new int[10];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;

                case ANIMATION_NUMBERS:
                    sequence = new int[10];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 100;
                    animparams[1] = 1;
                    animsequence.add(sequence);
                    break;
            }


            int[] load = new int[3];
            load[0] = animtextureindex;
            load[1] = numOfFrames;
            load[2] = DRAWABLES[i][1];
            mLoadables.add(load);


            texIndex += numOfFrames;

        }
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
        gameBombXOffset = gamePortXOffset + (int) (gameBombXOffset * mScaleFactor);
        gameBombYOffset = gamePortYOffset + (int) (gameBombYOffset * mScaleFactor);

        gamePlayerXOffset = gamePortXOffset;
        gamePlayerYOffset = gamePortYOffset;

        gameExplosionXOffset = gamePortXOffset + (int) (gameExplosionXOffset * mScaleFactor);
        gameExplosionYOffset = gamePortYOffset + (int) (gameExplosionYOffset * mScaleFactor);

        gameObjectXOffset = gamePortXOffset + (int) (gameObjectXOffset * mScaleFactor);
        gameObjectYOffset = (gamePortYOffset + (int) (gameObjectYOffset * mScaleFactor));

        gameTouchXOffset = (int) (gameTouchXOffset * mScaleFactor);
        gameTouchYOffset = (int) (gameTouchYOffset * mScaleFactor);

        gameLoadingXOffset = gamePortXOffset + (int) (gameLoadingXOffset * mScaleFactor);
        gameLoadingYOffset = gamePortYOffset + (int) (gameLoadingYOffset * mScaleFactor);

        gameButtonXOffset0 = gamePortXOffset + (int) (gameButtonXOffset0 * mScaleFactor);
        gameButtonYOffset0 = gamePortYOffset + (int) (gameButtonYOffset0 * mScaleFactor);

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


        if(gamePortXOffset > 0 || gamePortYOffset > 0)
        {
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

        }

        // Load Vertices
        mVertexData = new SparseArray<>();
        VertexArray vert78x78 = new VertexArray(vertices78x78);
        VertexArray vert136x136 = new VertexArray(vertices136x136);
        VertexArray vert150x100 = new VertexArray(vertices150x100);
        VertexArray vert180x180 = new VertexArray(vertices180x180);
        VertexArray vert300x300 = new VertexArray(vertices300x300);
        VertexArray vert600x600 = new VertexArray(vertices600x600);
        VertexArray vert524x200 = new VertexArray(vertices524x200);
        VertexArray vertFullscreen = new VertexArray(verticesFullscreen);


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
