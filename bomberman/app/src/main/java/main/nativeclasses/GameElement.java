package main.nativeclasses;

import main.game.events.EventObject;


public class GameElement extends EventObject {
    public int mObjectStateOffset;
    public int mObjectType;
    public int mObjectSubtype;

    public final static int OBJ_PLAYR = 0x00006000;
    public final static int OBJ_BOMB = 0x0001000;
    public final static int OBJ_EXPLN = 0x0002000;
    public final static int OBJ_CRATE = 0x0003000;
    public final static int OBJ_BLOCK = 0x0004000;
    public final static int OBJ_ITEM = 0x0005000;

    private static float xScaling = 1.0f;
    private static float yScaling = 1.0f;

    public void init(int type, int slot, int subtype)
    {
        mObjectType = type;
        mObjectStateOffset = slot;
        mObjectSubtype = 0;
    }
    public static void setScalings(float x, float y){
        xScaling = x;
        yScaling = y;
    }
    public int getUniqeueID()
    {
        return mObjectType | mObjectStateOffset;
    }

    public int getZ()
    {
        return GameManager.getZ(mObjectType, mObjectStateOffset);
    }

    public float[] getPositionXY()
    {
        float[]  positions = GameManager.getPosition(mObjectType, mObjectStateOffset);
        positions[0] *=xScaling;
        positions[1] *=yScaling;
        return positions;
    }

    public int getState()
    {
        return GameManager.getState(mObjectType, mObjectStateOffset);
    }

    public void setInput(byte input)
    {
        GameManager.setInput(mObjectType, mObjectStateOffset, input);
    }

    public int[][] getHitBoxes()
    {
        return GameManager.getHitboxes(mObjectType, mObjectStateOffset);
    }

}