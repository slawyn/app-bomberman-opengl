package main.rendering.animation;

/* AnimatedObject class*/
public class Animation
{

    private int[] mFrameSequence;
    private long mFrameTime;
    private float mElapsedTime;
    private int mInfinite;
    private int mFrameIndex;

    private final float[] mTranslation;
    private final float[] mTextureOffset;
    private int mCurrentTexture;

    public static final int ANIMATION_PAR_ONCE = 0;
    public static final int ANIMATION_PAR_INFINITE = 1;

    public Animation(){
        mTranslation = new float[2];
        mTextureOffset = new float[2];
    }

    public void init(int offsetxy[])
    {
        mTextureOffset[0] = offsetxy[0];
        mTextureOffset[1] = offsetxy[1];
    }

    public int getCurrentTexture()
    {
        return mCurrentTexture;
    }

    public void setAnimationParameters(int[] animsequence, int[] animparams)
    {
        mFrameSequence = animsequence;
        mFrameTime = animparams[0];
        mInfinite = animparams[1];
        mFrameIndex = 0;
        mElapsedTime = 0;
        mCurrentTexture = mFrameSequence[mFrameIndex];
    }

    public void updateTranslation(float[] basepositon)
    {
        mTranslation[0] = mTextureOffset[0] + basepositon[0];
        mTranslation[1] = mTextureOffset[1] + basepositon[1];
    }

    public float[] getTranslation(){
        return mTranslation;
    }

    /* Switch frames: true if AnimatedObject is done*/
    public void updateFrame(float dt)
    {
        mElapsedTime += dt;
        if(mElapsedTime >= mFrameTime)
        {
            if((mInfinite == ANIMATION_PAR_INFINITE) || (mFrameIndex + 1) != mFrameSequence.length)
            {
                mFrameIndex = (mFrameIndex + 1) % mFrameSequence.length;
                mCurrentTexture = mFrameSequence[mFrameIndex];
                mElapsedTime = 0;
            }
        }
    }
}