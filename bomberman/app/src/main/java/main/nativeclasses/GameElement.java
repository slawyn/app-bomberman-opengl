package main.nativeclasses;
import main.game.events.EventObject;


public class GameElement extends EventObject {
    public int mObjectStateOffset;
    private int mObjectType;
    private int mObjectSubtype;

    public final static int OBJ_PLAYR = 0x00006000;
    public final static int OBJ_BOMB = 0x0001000;
    public final static int OBJ_EXPLN = 0x0002000;
    public final static int OBJ_CRATE = 0x0003000;
    public final static int OBJ_BLOCK = 0x0004000;
    public final static int OBJ_ITEM = 0x0005000;
    public int mLayer;
    private static float xScaling = 1.0f;
    private static float yScaling = 1.0f;

    public void init(int type, int slot, int subtype)
    {
        mObjectType = type;
        mObjectStateOffset = slot;
        mObjectSubtype = 0;
        mLayer = 1;
    }
    public static void setScalings(float x, float y){
        xScaling = x;
        yScaling = y;
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

    public void setInput(byte input)
    {
        GameManager.setInput(mObjectType, mObjectStateOffset, input);
    }

    @Override
    public int[][] getHitboxes()
    {
        int[][] hitboxes = GameManager.getHitboxes(mObjectType, mObjectStateOffset);
        for(int idx = 0; idx<hitboxes.length; ++idx)
        {
            hitboxes[idx][2] *= xScaling;
            hitboxes[idx][3] *= yScaling;

        }
        return hitboxes;
    }

    @Override
    public int getState()
    {
        return GameManager.getState(mObjectType, mObjectStateOffset);
    }

    @Override
    public int getId()
    {
        return mObjectType | mObjectStateOffset;
    }

    @Override
    public int getSubtype()
    {
        return mObjectSubtype;
    }

    @Override
    public int getType()
    {
        return mObjectType;
    }

    public int getLayer() {return mLayer;}

    @Override
    public boolean hasBoundingboxes(){
        return true;
    }
}