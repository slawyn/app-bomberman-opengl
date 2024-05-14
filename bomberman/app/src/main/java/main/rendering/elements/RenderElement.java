package main.rendering.elements;

import java.util.Vector;

import main.Constants;
import main.game.events.EventObject;
import main.nativeclasses.GameElement;
import main.rendering.animation.Animation;

// Each render element has multiple animations
public class RenderElement implements Comparable<RenderElement>
{
    private Vector<Animation> mAnimations;
    private float[][] mSwapBufferPosition;
    public float[] mRenderObjectPosition;
    private int mSortCriteria;
    public int mOptimizationObjectType;
    private int mAnimationState;
    public boolean removeFromRenderingGpu;
    public boolean removed;
    private boolean mInterpolate;
    private static int mBufferIndex;
    public static float elapsed;
    private int mUniqueId;
    public DebugElement mDebugObject;
    public int[] mAdditionalParams;
    public boolean updated;
    public int mSubType;

    public RenderElement()
    {
        mAnimations = new Vector<>();
        mSwapBufferPosition = new float[2][4];
        mRenderObjectPosition = new float[2];
        updated = false;
    }

    public void init(int type, int subtype, int state, int id)
    {
        removed = false;

        /* Clear the animations */
        mAnimations.clear();
        mAdditionalParams = null;

        mSubType = subtype;
        mOptimizationObjectType = type;
        mSortCriteria = 0;
        mUniqueId = id;
        mAnimationState = state;
        mInterpolate = false;
        removeFromRenderingGpu = false;

        /* Positions */
        mSwapBufferPosition[mBufferIndex][0] = -1000.0f;
        mSwapBufferPosition[mBufferIndex][1] = -1000.0f;
        mSwapBufferPosition[mBufferIndex][2] = -1000.0f;
        mSwapBufferPosition[mBufferIndex][3] = -1000.0f;
    }


    public void setAdditionalParams(int[] iparams)
    {
        mAdditionalParams = iparams;
    }

    public int getUniqueId()
    {
        return mUniqueId;
    }
    public int getmAnimationState()
    {
        return mAnimationState;
    }
    public void setmAnimationState(int state)
    {
        mAnimationState = state;
    }

    public void addDebugObject(EventObject go, float scale)
    {
        mDebugObject = new DebugElement(go, scale);
    }

    public void setInterpolation(boolean enable)
    {
        mInterpolate = enable;
    }

    public void updateSortCriteria(int mZ)
    {
        mSortCriteria = mZ;
        if(mDebugObject!= null)
        {
            mDebugObject.updateSortCriteria(mZ);
        }
    }

    public static void updateElapsed(float dt)
    {
        elapsed += dt;
    }

    public static void latch(){
        elapsed = 0;
        mBufferIndex = (mBufferIndex+1)%2;
    }

    public float[] getRenderObjectPosition()
    {
        return mRenderObjectPosition;
    }

    public int getSortCriteria()
    {
        return mSortCriteria;
    }

    public Vector<Animation> getUsedAnimatedObjects()
    {
        return mAnimations;
    }


    /* Update Animation using RenderObject Information */
    public void updateAnimationsGpu(float dt)
    {
        int sz = mAnimations.size();
        for(int i = 0; i < sz; i++)
        {
            Animation a = mAnimations.get(i);
            a.updateFrame(dt);

            if(mInterpolate)
            {
                lerp(elapsed/ Constants.SERVER_TICK_TIME);
            }
            a.updateTranslation(mRenderObjectPosition);
        }
    }

    public void setTranslation(float posx, float posy)
    {
        if(mInterpolate)
        {
            int oldidx = mBufferIndex;
            int newidx = (mBufferIndex+1)%mSwapBufferPosition.length;
            mSwapBufferPosition[newidx][0] = mSwapBufferPosition[oldidx][2];
            mSwapBufferPosition[newidx][1] = mSwapBufferPosition[oldidx][3];
            mSwapBufferPosition[newidx][2] = posx;
            mSwapBufferPosition[newidx][3] = posy;
        }
        else{
            mRenderObjectPosition[0] = posx;
            mRenderObjectPosition[1] = posy;
        }
        if(mDebugObject != null)
        {
            mDebugObject.mRenderObjectPosition[0] = posx;
            mDebugObject.mRenderObjectPosition[1] = posy;
        }
    }

    /* Interpolation function called on Ui thread */
    public void lerp(float weight)
    {
        mRenderObjectPosition[0] = mSwapBufferPosition[mBufferIndex][0] * (1 - weight) + mSwapBufferPosition[mBufferIndex][2]* weight;
        mRenderObjectPosition[1] = mSwapBufferPosition[mBufferIndex][1] * (1 - weight) + mSwapBufferPosition[mBufferIndex][3] * weight;
    }


    public void addAnimation(Animation obj)
    {
        mAnimations.add(obj);
    }

    public Animation getAnimation(int loclayer)
    {
        return mAnimations.get(loclayer);
    }

    public Vector<Animation> getAnimations()
    {
        return mAnimations;
    }

    @Override
    public int compareTo(RenderElement renderElement)
    {
        return this.mSortCriteria - renderElement.mSortCriteria;
    }
}
