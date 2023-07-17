package main.rendering.display;

import static main.Constants.NUMBER_OF_ANIMATED_OBJECTS;
import static main.Constants.STATE_ALIVE;
import static main.Constants.STATE_DEAD;
import static main.Constants.STATE_EXPLODED;
import static main.Constants.STATE_MOVEDOWN;
import static main.Constants.STATE_MOVELEFT;
import static main.Constants.STATE_MOVERIGHT;
import static main.Constants.STATE_MOVEUP;
import static main.Constants.STATE_PRESSED;
import static main.Constants.*;
import static main.game.SceneManager.SOBJ_BACKGROUND;
import static main.game.SceneManager.SOBJ_BACKGROUND_LOADING;
import static main.game.SceneManager.SOBJ_BUTTON;
import static main.game.SceneManager.SOBJ_TIMER;
import static main.game.SceneManager.SOBJ_TOUCH;
import static main.nativeclasses.GameElement.OBJ_BLOCK;
import static main.nativeclasses.GameElement.OBJ_BOMB;
import static main.nativeclasses.GameElement.OBJ_CRATE;
import static main.nativeclasses.GameElement.OBJ_EXPLN;
import static main.nativeclasses.GameElement.OBJ_ITEM;
import static main.nativeclasses.GameElement.OBJ_PLAYR;
import static main.rendering.animation.Animation.ANIMATION_PAR_INFINITE;
import static main.rendering.animation.Animation.ANIMATION_PAR_ONCE;
import static main.rendering.display.DisplayManager.GAME_H;
import static main.rendering.display.DisplayManager.GAME_V;
import static main.rendering.display.DisplayManager.gameBombOffsetXY;
import static main.rendering.display.DisplayManager.gameExplosionOffsetXY;
import static main.rendering.display.DisplayManager.gameLoadingOffsetXY;
import static main.rendering.display.DisplayManager.gameObjectOffsetXY;
import static main.rendering.display.DisplayManager.gamePlayerOffsetXY;
import static main.rendering.display.DisplayManager.gamePortOffsetXY;
import static main.rendering.display.DisplayManager.gameTimer2ndOffsets;
import static main.rendering.display.DisplayManager.gameTouchOffsetXY;
import static main.rendering.display.DisplayManager.gameZeroOffsetXY;
import static main.rendering.display.DisplayManager.mScaleFactor;

import android.util.SparseArray;

import com.game.bomber.R;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import main.rendering.animation.Animation;
import main.rendering.animation.Layer;
import main.rendering.elements.RenderElement;

public class AnimationManager {

    private Vector<Layer> mLayers;
    public Animation[] mFreeAnimations;
    private int mTotalNumberOfAnimatedObjects;

    /* every type has a subset as vector */
    public SparseArray<int[]> mAnimationParameters;
    public SparseArray<Vector<int[]>> mAnimationSequences;

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

    /* layer 0: background
     *  layer 1: game layer
     *  layer 3: foreground
     *  layer 4: info
     *  layer 5: hitboxes */
    private final int[] LAYER_SIZES = {10 ,50, 10, 10, 50};
    private final int DRAWABLES[][] = {
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

    public AnimationManager()
    {
        mAnimationParameters = new SparseArray<>();
        mAnimationSequences = new SparseArray<>();

        mLayers = new Vector<>();
        for(int size: LAYER_SIZES)
        {
            mLayers.add(new Layer(size));
        }

        mTotalNumberOfAnimatedObjects = NUMBER_OF_ANIMATED_OBJECTS;
        mFreeAnimations = new Animation[mTotalNumberOfAnimatedObjects];
        for(int idx = 0; idx < NUMBER_OF_ANIMATED_OBJECTS; idx++)
        {
            mFreeAnimations[idx] = new Animation();
        }
    }

    public int getDrawableCount()
    {
        return DRAWABLES.length;
    }

    public Vector<Layer> getLayers() {
        return mLayers;
    }

    public void loadAnimations(Loader loader) {

        for(int drawnum = 0, texIndex = 0; drawnum < DRAWABLES.length; drawnum++)
        {
            final int type = DRAWABLES[drawnum][0];
            final int resource = DRAWABLES[drawnum][1];
            final int numOfFrames = DRAWABLES[drawnum][2];
            final int animtextureindex = texIndex;

            loader.putLoadable(new Loadable(animtextureindex,numOfFrames,resource));

            // Create parameters: duration, looped
            int[] animparams;
            int[] sequence;

            switch(type)
            {
                case ANIMATION_LOADING:
                    sequence = new int[numOfFrames];
                    animparams = new int[2];

                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }

                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_BACKGROUND:
                    sequence = new int[7];
                    animparams = new int[2];

                    sequence[0] = 0 + animtextureindex;
                    sequence[1] = 1 + animtextureindex;
                    sequence[2] = 2 + animtextureindex;
                    sequence[3] = 3 + animtextureindex;
                    sequence[4] = 2 + animtextureindex;
                    sequence[5] = 1 + animtextureindex;
                    sequence[6] = 0 + animtextureindex;

                    animparams[0] = 300;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_FIELD:
                case ANIMATION_PLAYER_STAND:
                    sequence = new int[1];
                    animparams = new int[2];

                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_PLAYER_WALK_LEFT:
                case ANIMATION_PLAYER_WALK_RIGHT:
                    sequence = new int[12];
                    animparams = new int[2];

                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 40;//70;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_PLAYER_WALK_UP:
                case ANIMATION_PLAYER_WALK_DOWN:
                    sequence = new int[11];
                    animparams = new int[2];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 40;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_PLAYER_BOMB:
                    sequence = new int[34];
                    animparams = new int[2];
                    for(int idx = 0; idx < 20; idx++)
                    {
                        sequence[idx] = 0 + animtextureindex;
                    }

                    for(int idx = 20, k = 1; idx < sequence.length; idx++, k++)
                    {
                        sequence[idx] = k + animtextureindex;
                    }

                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_ONCE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_PLAYER_DYING:
                    sequence = new int[14];
                    animparams = new int[2];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 50;
                    animparams[1] = ANIMATION_PAR_ONCE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_PLAYER_EXPLOSION_CENTER:
                case ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT:
                case ANIMATION_PLAYER_EXPLOSION_UPDOWN:
                    sequence = new int[3];
                    animparams = new int[2];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_BUTTON:
                case ANIMATION_BUTTON_PRESSED:
                    sequence = new int[1];
                    animparams = new int[2];
                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_OBJECT_CRATE:
                case ANIMATION_OBJECT_BLOCK:
                    sequence = new int[1];
                    animparams = new int[2];
                    sequence[0] = animtextureindex;
                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;
                case ANIMATION_TOUCH_MARKER:
                    sequence = new int[10];
                    animparams = new int[2];
                    for(int idx = 0; idx < sequence.length; idx++)
                    {
                        sequence[idx] = idx + animtextureindex;
                    }
                    animparams[0] = 100;
                    animparams[1] = ANIMATION_PAR_INFINITE;
                    addAnimationSequence(type, sequence, animparams);
                    break;

                case ANIMATION_NUMBERS:
                    for(int idx =0; idx<numOfFrames;++idx)
                    {
                        sequence = new int[1];
                        animparams = new int[2];

                        sequence[0] = animtextureindex + idx;
                        animparams[0] = 100;
                        animparams[1] = ANIMATION_PAR_INFINITE;
                        addAnimationSequence(type, sequence, animparams);
                    }
                    break;
            }

            texIndex += numOfFrames;
        }
    }

    private void addAnimationSequence(int type, int[] animsequence, int[] animparams)
    {
        Vector<int[]> v = mAnimationSequences.get(type);
        if(null == v)
        {
            v = new Vector<>();
            mAnimationSequences.put(type, v);
        }
        v.add(animsequence);
        mAnimationParameters.put(type, animparams);
    }

    public Animation getFreeAnimationFromPool()
    {
        Animation ao = mFreeAnimations[--mTotalNumberOfAnimatedObjects];
        return ao;
    }

    public void returnAnimationsToPool(Vector<Animation> an) {
        int sz = an.size() - 1;
        for(int idx = sz; idx >= 0; idx--)
        {
            Animation ao = an.remove(idx);
            mFreeAnimations[mTotalNumberOfAnimatedObjects] = ao;
            ++mTotalNumberOfAnimatedObjects;
        }
    }

    public void updateAnimatedObject(RenderElement ro, int state) {


        if(state != ro.getmAnimationState()) {
            ro.setmAnimationState(state);
            Animation ao = ro.getAnimation(0);
            switch (ro.mOptimizationObjectType) {
                case SOBJ_BUTTON:
                    switch (state) {
                        case STATE_UNPRESSED:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_BUTTON).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_BUTTON));
                            break;
                        case STATE_PRESSED:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_BUTTON_PRESSED).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_BUTTON_PRESSED));
                            break;
                    }
                case SOBJ_BACKGROUND_LOADING:
                    //ao.setAnimation(mAnimationSequences.get(ANIMATION_LOADING).get(so.mObjectSubtype), mAnimationParameters.get(ANIMATION_BUTTON));
                    break;
                case SOBJ_TIMER:
                    int[] time = ro.mAdditionalParams;
                    Vector<int[]> v = mAnimationSequences.get(ANIMATION_NUMBERS);
                    ro.getAnimation(0).setAnimationParameters(v.get(time[0]),
                            mAnimationParameters.get(ANIMATION_NUMBERS));
                    ro.getAnimation(1).setAnimationParameters(v.get(time[1]),
                            mAnimationParameters.get(ANIMATION_NUMBERS));
                    ro.getAnimation(2).setAnimationParameters(v.get(time[2]),
                            mAnimationParameters.get(ANIMATION_NUMBERS));
                    ro.getAnimation(3).setAnimationParameters(v.get(time[3]),
                            mAnimationParameters.get(ANIMATION_NUMBERS));
                    break;
                case OBJ_PLAYR:
                    switch (state) {
                        case STATE_ALIVE:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_STAND).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_PLAYER_STAND));
                            break;
                        case STATE_DEAD:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_DYING).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_PLAYER_DYING));
                            //  SoundManager.playSound(SOUND_DEATH);      // TODO- Could be duplicates because of network, need to change
                            break;
                        case STATE_MOVEDOWN:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_WALK_DOWN).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_PLAYER_WALK_DOWN));
                            break;
                        case STATE_MOVEUP:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_WALK_UP).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_PLAYER_WALK_UP));
                            break;
                        case STATE_MOVELEFT:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_WALK_LEFT).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_PLAYER_WALK_LEFT));
                            break;
                        case STATE_MOVERIGHT:
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_WALK_RIGHT).get(ro.mSubType),
                                    mAnimationParameters.get(ANIMATION_PLAYER_WALK_RIGHT));
                            break;
                    }
                    break;
                case OBJ_CRATE:
                    switch (state) {
                        case STATE_ALIVE:
                            break;
                        case STATE_DEAD:
                            break;
                    }
                    break;
                case OBJ_BOMB:
                    switch (state) {
                        case STATE_EXPLODED:
                            int animset = 0;
                            int strength = 1;
                            int left = 1;
                            int right = 1;
                            int up = 1;
                            int down = 1;


                            //  Add animations
                            ao.init(gameExplosionOffsetXY);
                            ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_CENTER).get(animset),
                                                        mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_CENTER));
                            //ro.addAnimation(ao);
                            for(int i = 1; i <= strength; i++)
                            {
                                int[] xy = new int[2];

                                // Horizontal
                                if(i <= left)
                                {
                                    xy[0] = (int) (gameExplosionOffsetXY[0] + (-i * GAME_H * mScaleFactor));
                                    xy[1] = gameExplosionOffsetXY[1];
                                    ao = getFreeAnimationFromPool();
                                    ao.init(xy);
                                    ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT).get(animset),
                                            mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT));
                                    ro.addAnimation(ao);
                                }
                                if(i <= right)
                                {
                                    xy[0] = (int) (gameExplosionOffsetXY[0] + (i * GAME_H * mScaleFactor));
                                    xy[1] = gameExplosionOffsetXY[1];
                                    ao = getFreeAnimationFromPool();
                                    ao.init(xy);
                                    ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT).get(animset),
                                            mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_LEFTRIGHT));
                                    ro.addAnimation(ao);
                                }

                                // Vertical
                                if(i <= up)
                                {
                                    xy[0] = gameExplosionOffsetXY[0];
                                    xy[1] = (int) (gameExplosionOffsetXY[1] + (-i * GAME_V * mScaleFactor));
                                    ao = getFreeAnimationFromPool();
                                    ao.init(xy);
                                    ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN).get(animset),
                                            mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN));
                                    ro.addAnimation(ao);
                                }
                                if(i <= down)
                                {
                                    xy[0] = gameExplosionOffsetXY[0];
                                    xy[1] = (int) (gameExplosionOffsetXY[1] + (i * GAME_V * mScaleFactor));
                                    ao = getFreeAnimationFromPool();
                                    ao.init(xy);
                                    ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN).get(animset),
                                                              mAnimationParameters.get(ANIMATION_PLAYER_EXPLOSION_UPDOWN));
                                    ro.addAnimation(ao);
                                }

                                //SoundManager.playSound(SOUND_EXPLOSION);
                            }

                            break;
                    }
                    break;
            }
        }
    }

    public void createAnimatedObject(RenderElement ro, int laynum) {
        Animation ao;
        switch(ro.mOptimizationObjectType)
        {
            case SOBJ_BACKGROUND:
                ao = getFreeAnimationFromPool();
                ao.init(gamePortOffsetXY);
                ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_BACKGROUND).get(ro.mSubType), mAnimationParameters.get(ANIMATION_BACKGROUND));
                ro.addAnimation(ao);
                break;
            case SOBJ_BUTTON:
                ao = getFreeAnimationFromPool();
                ao.init(gameZeroOffsetXY);//gameButtonXOffset0, gameButtonYOffset0);
                ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_BUTTON).get(ro.mSubType), mAnimationParameters.get(ANIMATION_BUTTON));
                ro.addAnimation(ao);
                break;
            case SOBJ_BACKGROUND_LOADING:
                ao = getFreeAnimationFromPool();
                ao.init(gameLoadingOffsetXY);
                ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_LOADING).get(ro.mSubType), mAnimationParameters.get(ANIMATION_LOADING));
                ro.addAnimation(ao);
                break;
            case SOBJ_TOUCH:
                ao = getFreeAnimationFromPool();
                ao.init(gameTouchOffsetXY);
                ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_TOUCH_MARKER).get(ro.mSubType), mAnimationParameters.get(ANIMATION_TOUCH_MARKER));
                ro.addAnimation(ao);
                break;
            case SOBJ_TIMER:
                for(int idx = 0; idx< gameTimer2ndOffsets.length; ++idx)
                {
                    ao = getFreeAnimationFromPool();
                    ao.init(gameTimer2ndOffsets[idx]);
                    ao.setAnimationParameters(mAnimationSequences.get(ANIMATION_NUMBERS).get(1), mAnimationParameters.get(ANIMATION_NUMBERS));
                    ro.addAnimation(ao);
                }
                break;

            case OBJ_PLAYR:
                ao = getFreeAnimationFromPool();
                ro.setInterpolation(true);
                ao.init(gamePlayerOffsetXY);
                ao.setAnimationParameters(  mAnimationSequences.get(ANIMATION_PLAYER_STAND).get(ro.mSubType),
                        mAnimationParameters.get(ANIMATION_PLAYER_STAND));
                ro.addAnimation(ao);
                break;
            case OBJ_BLOCK:
                ao = getFreeAnimationFromPool();
                ao.init(gameObjectOffsetXY);
                ao.setAnimationParameters(  mAnimationSequences.get(ANIMATION_OBJECT_BLOCK).get(ro.mSubType),
                        mAnimationParameters.get(ANIMATION_OBJECT_BLOCK));
                ro.addAnimation(ao);
                break;
            case OBJ_CRATE:
                ao = getFreeAnimationFromPool();
                ao.init(gameObjectOffsetXY);
                ao.setAnimationParameters(  mAnimationSequences.get(ANIMATION_OBJECT_CRATE).get(ro.mSubType),
                        mAnimationParameters.get(ANIMATION_OBJECT_CRATE));
                ro.addAnimation(ao);
                break;
            case OBJ_BOMB:
                ao = getFreeAnimationFromPool();
                ao.init(gameBombOffsetXY);
                ao.setAnimationParameters(  mAnimationSequences.get(ANIMATION_PLAYER_BOMB).get(ro.mSubType),
                        mAnimationParameters.get(ANIMATION_PLAYER_BOMB));
                ro.addAnimation(ao);
                break;
            case OBJ_EXPLN:



                break;
            case OBJ_ITEM:
                break;
        }
        mLayers.get(laynum).addRenderObjectToLayer(ro);
    }
}
