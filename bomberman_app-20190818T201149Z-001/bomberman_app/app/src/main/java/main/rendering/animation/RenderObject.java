package main.rendering.animation;

import java.util.Vector;

import main.Constants;

// Each render object has multiple animated objects
public class RenderObject implements Comparable<RenderObject>
{
    private Vector<AnimatedObject> mAnimations;
    private float[][] mSwapBufferPosition;
    private float[] mRenderObjectPosition;
    private int mSortCriteria;
    public int mOptimizationObjectType;
    public int mAnimationState;
    public boolean removeFromGPUthread;
    private boolean mInterpolate;
    private static int mBufferIndex;
    public static float elapsed;

    public RenderObject()
    {
        mAnimations = new Vector<>();
        mSwapBufferPosition = new float[2][4];
    }

    public void init(int type)
    {
        mAnimations.clear();
        mOptimizationObjectType = type;
        mSortCriteria = 0;
        mAnimationState = -1;
        mInterpolate = false;
        removeFromGPUthread = false;
        mSwapBufferPosition[mBufferIndex][0] = -1000.0f;
        mSwapBufferPosition[mBufferIndex][1] = -1000.0f;
        mSwapBufferPosition[mBufferIndex][2] = -1000.0f;
        mSwapBufferPosition[mBufferIndex][3] = -1000.0f;

        mRenderObjectPosition = new float[2];
    }

    public void setInterpolation(boolean enable){
        mInterpolate = enable;
    }

    public void updateSortCriteria(int mZ)
    {
        mSortCriteria = mZ;
    }

    public static void updateelapsed(float dt){
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

    public Vector<AnimatedObject> getUsedAnimatedObjects()
    {
        return mAnimations;
    }


    public void updateAnimations(float dt)
    {

        int sz = mAnimations.size();
        for(int i = 0; i < sz; i++)
        {
            AnimatedObject a = mAnimations.get(i);
            a.updateFrame(dt);

            if(mInterpolate)
            {
                lerp(elapsed/ Constants.SERVER_TICK_TIME);
            }
            a.updateTranslation(mRenderObjectPosition);
        }
    }

    public void setAnimationTranslation(float posx, float posy)
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
    }


    public void lerp(float weight)
    {
        mRenderObjectPosition[0] = mSwapBufferPosition[mBufferIndex][0] * (1 - weight) + mSwapBufferPosition[mBufferIndex][2]* weight;
        mRenderObjectPosition[1] = mSwapBufferPosition[mBufferIndex][1] * (1 - weight) + mSwapBufferPosition[mBufferIndex][3] * weight;
    }


    public void addAnimation(AnimatedObject obj)
    {
        mAnimations.add(obj);
    }

    public AnimatedObject getAnimatedObject(int loclayer)
    {
        return mAnimations.get(loclayer);
    }

    public Vector<AnimatedObject> getAnimations()
    {
        return mAnimations;
    }

    @Override
    public int compareTo(RenderObject renderObject)
    {
        return this.mSortCriteria - renderObject.mSortCriteria;
    }
}
