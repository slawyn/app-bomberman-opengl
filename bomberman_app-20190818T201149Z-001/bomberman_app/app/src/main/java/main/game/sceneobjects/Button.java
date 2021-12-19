package main.game.sceneobjects;

import main.Constants;

import static main.game.SceneManager.SOBJ_BUTTON;

public class Button extends SceneObject
{
    private final int mSizeX;
    private final int mSizeY;
    private int mTouchIndex;
    public int mAction;


    public Button(int id, int posx, int posy, int sizex, int sizey, int action, int type)
    {
        super(SOBJ_BUTTON, id);
        mObjectSubtype = type;
        mAction = action;
        mSizeX = sizex;
        mSizeY = sizey;
        mPositionX = posx;
        mPositionY = posy;
        mState = Constants.STATE_UNPRESSED;
        mPreviousState = -1;
    }

    // NEED TO REDO
    public boolean updateState(int input[])
    {
        boolean released = false;
        mPreviousState = mState;

        int x1 = input[0];
        int y1 = input[1];
        int touch1 = input[2];

        int x2 = input[3];
        int y2 = input[4];
        int touch2 = input[5];

        boolean selected = isSelected(x1, y1);
        boolean selected2 = isSelected(x2, y2);


        switch(mState)
        {
            case Constants.STATE_UNPRESSED:
                if(touch1 > 0 && selected)
                {
                    mState = Constants.STATE_PRESSED;
                    mTouchIndex = 0;
                } else if(touch2 > 0 && selected2)
                {
                    mState = Constants.STATE_PRESSED;
                    mTouchIndex = 1;
                }

                break;
            case Constants.STATE_PRESSED:
                if(mTouchIndex == 0)
                {
                    if(touch1 == 0 && selected)
                    {
                        released = true;
                        mState = Constants.STATE_UNPRESSED;

                    } else if(!(selected))
                    {
                        mState = Constants.STATE_UNPRESSED;
                    }
                } else if(mTouchIndex == 1)
                {
                    if(touch2 == 0 && selected2)
                    {
                        released = true;
                        mState = Constants.STATE_UNPRESSED;

                    } else if(!(selected2))
                    {
                        mState = Constants.STATE_UNPRESSED;
                    }
                }
                break;
        }

        mRequiresUpdate = true;
        return released;
    }

    private boolean isSelected(int touchx, int touchy)
    {
        return ((touchx >= (mPositionX) && touchx <= (mPositionX + mSizeX) && touchy >= (mPositionY) && touchy <= (mPositionY+ mSizeY)));
    }
}