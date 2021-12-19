package main.rendering.animation;

/* AnimatedObject class*/
public class AnimatedObject
{

    private int[] mAnimationSequence;
    private long mFrameTime;
    private float mElapsedTime;
    private int mInfinite;
    private int mFrameIndex;

    private float[] mTranslate;
    private float[] mTextureOffset;
    private int mCurrentTexture;

    public AnimatedObject(){
        mTranslate = new float[2];
        mTextureOffset = new float[2];
    }

    public void init(int offsetx, int offsety)
    {
        mTextureOffset[0] = offsetx;
        mTextureOffset[1] = offsety;
    }

    public int getCurrentTexture()
    {
        return mCurrentTexture;
    }

    public void setAnimation(int[] animsequence, int[] animparams)
    {
        mAnimationSequence = animsequence;
        mFrameTime = animparams[0];
        mInfinite = animparams[1];
        mFrameIndex = 0;
        mElapsedTime = 0;
        mCurrentTexture = mAnimationSequence[mFrameIndex];
    }

    public void updateTranslation(float[] rposition)
    {
        mTranslate[0] = mTextureOffset[0] + rposition[0];
        mTranslate[1] = mTextureOffset[1] + rposition[1];
    }

    public float[] getTranslation(){
        return mTranslate;
    }


    /* Switch frames: true if AnimatedObject is done*/
    public void updateFrame(float dt)
    {
        mElapsedTime += dt;
        if(mElapsedTime >= mFrameTime)
        {
            if((mInfinite == 1) || (mFrameIndex + 1) != mAnimationSequence.length)
            {
                mFrameIndex = (mFrameIndex + 1) % mAnimationSequence.length;
                mCurrentTexture = mAnimationSequence[mFrameIndex];
                mElapsedTime = 0;
            }
        }
    }
}