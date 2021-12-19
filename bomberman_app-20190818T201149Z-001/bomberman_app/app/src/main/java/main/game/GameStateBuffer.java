package main.game;

import java.util.Arrays;
public class GameStateBuffer
{
    public int[][] mStatePlayers;
    public int[][] mStateBombs;
    public int[][] mStateBlocks;
    public int[][] mStateCrates;
    public int[][] mStateExplosions;

    public int mStateIndex = 0;

    public final static int OBJ_PLAYR = 0x00000000;
    public final static int OBJ_BOMB = 0x00010000;
    public final static int OBJ_EXPLN = 0x00020000;
    public final static int OBJ_CRATE = 0x00030000;
    public final static int OBJ_BLOCK = 0x00040000;
    public final static int OBJ_ITEM = 0x00050000;

    // Gamestate
    public final static int[] GAMESTATE_OBJECT_TYPES = {OBJ_PLAYR, OBJ_BOMB, OBJ_EXPLN, OBJ_CRATE, OBJ_BLOCK, OBJ_ITEM};
    public final static int[] GAMESTATE_SLOT_SIZES = {5, 4, 3, 3, 3, 3};
    public final static int[] GAMESTATE_SLOT_COUNT = {4, 5, 5, 50, 50, 1};

    public final static int EVENT_BOMB_PLACED = 0x001;


    private Events mPositionChanges;
    private Slots mPlayerSlots;
    private Slots mBlockSlots;
    private Slots mCrateSlots;
    private Slots mBombSlots;
    private Slots mExplosionSlots;

    public GameStateBuffer()
    {
        mStatePlayers = new int[256][GAMESTATE_SLOT_COUNT[0] * GAMESTATE_SLOT_SIZES[0]];
        mStateBombs = new int[256][GAMESTATE_SLOT_COUNT[1] * GAMESTATE_SLOT_SIZES[1]];
        mStateExplosions = new int[256][GAMESTATE_SLOT_COUNT[2] * GAMESTATE_SLOT_SIZES[2]];
        mStateCrates = new int[256][GAMESTATE_SLOT_COUNT[3] * GAMESTATE_SLOT_SIZES[3]];
        mStateBlocks = new int[256][GAMESTATE_SLOT_COUNT[4] * GAMESTATE_SLOT_SIZES[4]];


        mPlayerSlots = new Slots(GAMESTATE_SLOT_COUNT[0], GAMESTATE_SLOT_SIZES[0]);
        mBombSlots = new Slots(GAMESTATE_SLOT_COUNT[1], GAMESTATE_SLOT_SIZES[1]);
        mExplosionSlots = new Slots(GAMESTATE_SLOT_COUNT[2], GAMESTATE_SLOT_SIZES[2]);
        mCrateSlots = new Slots(GAMESTATE_SLOT_COUNT[3], GAMESTATE_SLOT_SIZES[3]);
        mBlockSlots = new Slots(GAMESTATE_SLOT_COUNT[4], GAMESTATE_SLOT_SIZES[4]);



        mPositionChanges = new Events();
    }

    public int getFreeSlot(int type)
    {
        int slot = 0;
        switch(type)
        {
            case OBJ_BLOCK:
                slot = mBlockSlots.aquireSlot();
                break;
            case OBJ_CRATE:
                slot = mCrateSlots.aquireSlot();
                break;
            case OBJ_PLAYR:
                slot = mPlayerSlots.aquireSlot();
                break;
            case OBJ_BOMB:
                slot = mBombSlots.aquireSlot();
                break;
            case OBJ_EXPLN:
                slot = mExplosionSlots.aquireSlot();
                break;
        }
        return slot;
    }

    public void returnSlot(int type, int slot)
    {
        switch(type)
        {
            case OBJ_BLOCK:
                mBlockSlots.returnSlot(slot);
                break;
            case OBJ_CRATE:
                mCrateSlots.returnSlot(slot);
                break;
            case OBJ_PLAYR:
                mPlayerSlots.returnSlot(slot);
                break;
            case OBJ_BOMB:
                mBombSlots.returnSlot(slot);
                break;
            case OBJ_EXPLN:
                mExplosionSlots.returnSlot(slot);
                break;
        }
    }

    public void resetState()
    {
        Arrays.fill(mStatePlayers[mStateIndex], 0);
        Arrays.fill(mStateBombs[mStateIndex], 0);
        Arrays.fill(mStateBlocks[mStateIndex], 0);
        Arrays.fill(mStateCrates[mStateIndex], 0);
        Arrays.fill(mStateExplosions[mStateIndex], 0);
    }

    public void initCurrentState(int stateindex)
    {
        int prevstate = mStateIndex;
        mStateIndex = stateindex;
        System.arraycopy(mStatePlayers[prevstate], 0, mStatePlayers[mStateIndex], 0, mPlayerSlots.getUsed()* GAMESTATE_SLOT_SIZES[0]);
        System.arraycopy(mStateBombs[prevstate], 0, mStateBombs[mStateIndex], 0, mBombSlots.getUsed() * GAMESTATE_SLOT_SIZES[1]);
        System.arraycopy(mStateExplosions[prevstate], 0, mStateExplosions[mStateIndex], 0, mExplosionSlots.getUsed() * GAMESTATE_SLOT_SIZES[2]);
        System.arraycopy(mStateCrates[prevstate], 0, mStateCrates[mStateIndex], 0, mCrateSlots.getUsed()* GAMESTATE_SLOT_SIZES[3]);
        System.arraycopy(mStateBlocks[prevstate], 0, mStateBlocks[mStateIndex], 0, mBlockSlots.getUsed()* GAMESTATE_SLOT_SIZES[4]);



    }

    public int[] getStateData(int type)
    {
        switch(type)
        {
            case OBJ_BLOCK:
                return mStateBlocks[mStateIndex];
            case OBJ_CRATE:
                return mStateCrates[mStateIndex];
            case OBJ_PLAYR:
                return mStatePlayers[mStateIndex];
            case OBJ_BOMB:
                return mStateBombs[mStateIndex];
            case OBJ_EXPLN:
                return mStateExplosions[mStateIndex];
            default:
                return new int[0];
        }
    }

    public void setState(int type, int offset, int val)
    {
        int[] state;
        switch(type)
        {
            case OBJ_BLOCK:
                state = mStateBlocks[mStateIndex];
                break;
            case OBJ_CRATE:
                state = mStateCrates[mStateIndex];
                break;
            case OBJ_PLAYR:
                state = mStatePlayers[mStateIndex];
                break;
            case OBJ_BOMB:
                state = mStateBombs[mStateIndex];
                break;
            case OBJ_EXPLN:
                state = mStateExplosions[mStateIndex];
                break;
            default:
                state = new int[0];
                break;
        }

        state[offset + 2] = val;
    }

    public int getState(int type, int offset)
    {
        int[] state;
        switch(type)
        {
            case OBJ_BLOCK:
                state = mStateBlocks[mStateIndex];
                break;
            case OBJ_CRATE:
                state = mStateCrates[mStateIndex];
                break;
            case OBJ_PLAYR:
                state = mStatePlayers[mStateIndex];
                break;
            case OBJ_BOMB:
                state = mStateBombs[mStateIndex];
                break;
            case OBJ_EXPLN:
                state = mStateExplosions[mStateIndex];
                break;
            default:
                state = new int[0];
                break;
        }

        return state[offset + 2];
    }

    public int getPlayersCount(){
        return mPlayerSlots.getUsed();
    }


    public int getStateInput(int offset)
    {
        return mStatePlayers[mStateIndex][offset + 3];
    }

    public void setStateInput(int offset, int val)
    {
        mStatePlayers[mStateIndex][offset + 3] = val;
    }


    public void setStateComplete(int type, int offset, int... data)
    {
        int[] state;
        int idx = 0;
        switch(type)
        {
            case OBJ_BLOCK:
                state = mStateBlocks[mStateIndex];
                break;
            case OBJ_CRATE:
                state = mStateCrates[mStateIndex];
                break;
            case OBJ_PLAYR:
                state = mStatePlayers[mStateIndex];
                break;
            case OBJ_BOMB:
                state = mStateBombs[mStateIndex];
                break;
            case OBJ_EXPLN:
                state = mStateExplosions[mStateIndex];
                break;
            default:
                state = new int[0];
                break;
        }
        for(int d : data)
        {
            state[offset + idx] = d;
            ++idx;
        }
    }

    public void setStatePosition(int type, int offset, int x, int y)
    {
        int[] state;
        switch(type)
        {
            case OBJ_BLOCK:
                state = mStateBlocks[mStateIndex];
                break;
            case OBJ_CRATE:
                state = mStateCrates[mStateIndex];
                break;
            case OBJ_PLAYR:
                state = mStatePlayers[mStateIndex];
                break;
            case OBJ_BOMB:
                state = mStateBombs[mStateIndex];
                break;
            case OBJ_EXPLN:
                state = mStateExplosions[mStateIndex];
                break;
            default:
                state = new int[0];
                break;
        }

        state[offset + 1] = x;
        state[offset + 1] = y;
    }

    public void setStateAddToPosition(int type, int offset, int x, int y)
    {
        int[] state;
        switch(type)
        {
            case OBJ_BLOCK:
                state = mStateBlocks[mStateIndex];
                break;
            case OBJ_CRATE:
                state = mStateCrates[mStateIndex];
                break;
            case OBJ_PLAYR:
                state = mStatePlayers[mStateIndex];
                break;
            case OBJ_BOMB:
                state = mStateBombs[mStateIndex];
                break;
            case OBJ_EXPLN:
                state = mStateExplosions[mStateIndex];
                break;
            default:
                state = new int[0];
                break;
        }

        state[offset + 1] += x;
        state[offset + 1] += y;
    }

    class Slots
    {

        private int[] mSlots;
        private int mHeadIndex;
        private final int mLength;

        public Slots(int nslots, int objoffset)
        {
            mLength = nslots;
            mSlots = new int[nslots];
            for(int idx = 0; idx < nslots; ++idx)
            {
                mSlots[idx] = objoffset * idx;
            }
            mHeadIndex = 0;
        }

        public int aquireSlot()
        {
            if(mHeadIndex != mLength)
            {
                int slot = mSlots[mHeadIndex];
                ++mHeadIndex;
                return slot;
            }
            return -1;
        }

        public int getUsed(){
           return  mHeadIndex;
        }

        public void returnSlot(int x)
        {
            mSlots[mHeadIndex] = x;
            --mHeadIndex;
        }
    }
}
