package main.game.sceneobjects;


import main.game.EventObject;
import main.rendering.animation.RenderObject;

public class SceneObject extends EventObject
{
    public int mObjectID;
    public int mObjectType;
    public int mObjectSubtype;
    public int mPreviousState;
    public int mState;
    public int mPositionX;
    public int mPositionY;
    public boolean mRequiresUpdate;
    public boolean mRemove;
    public int mLayer;
    public RenderObject ro;

    public SceneObject(int objtype, int objid)
    {
        mObjectID = objid;
        mObjectType = objtype;
        mState = -1;
        mPreviousState = -1;
        mRequiresUpdate = true;
    }

    public boolean stateChanged(){
        return mState != mPreviousState;
    }

    public void updateState(long dt, int[] input){};
}
