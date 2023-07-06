package main.nativeclasses;

import main.game.events.EventObject;
import main.nativeclasses.GameLogic;
import main.rendering.elements.DebugElement;
import main.rendering.elements.RenderElement;


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

    public void init(int type, int slot, int subtype)
    {
        mObjectType = type;
        mObjectStateOffset = slot;
        mObjectSubtype = 0;
    }

    public int getUniqeueID()
    {
        return mObjectType | mObjectStateOffset;
    }

    public int getZ()
    {
        return GameLogic.getZ(mObjectType, mObjectStateOffset);
    }

    public long[] getPositionXY()
    {
        return GameLogic.getPosition(mObjectType, mObjectStateOffset);
    }

    public int getState()
    {
        return GameLogic.getState(mObjectType, mObjectStateOffset);
    }

    public void setInput(byte input)
    {
        GameLogic.setInput(mObjectType, mObjectStateOffset, input);
    }

    public int[][] getHitBoxes()
    {
        return GameLogic.getHitboxes(mObjectType, mObjectStateOffset);
    }

}