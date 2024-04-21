package main.game.sceneobjects;

import main.Constants;
import main.game.SceneElement;

import static main.game.SceneManager.SOBJ_BUTTON;

public abstract class Button extends SceneElement {
    private final int mSizeX;
    private final int mSizeY;
    private int mTouchIndex;

    public Button(int subtype, int id, int posx, int posy, int sizex, int sizey, int action, int animation) {
        super(subtype, id, animation);
        mAction = action;
        mSizeX = sizex;
        mSizeY = sizey;
        mPositionX = posx;
        mPositionY = posy;
        mState = Constants.STATE_UNPRESSED;
    }

    @Override
    public boolean updateState(long dt, int[] input) {

        final int x1 = input[0];
        final int y1 = input[1];
        final int touch1 = input[2];

        final int x2 = input[3];
        final int y2 = input[4];
        final int touch2 = input[5];

        final boolean selected = isSelected(x1, y1);
        final boolean selected2 = isSelected(x2, y2);

        boolean released = false;
        switch (mState) {
            case Constants.STATE_UNPRESSED:
                if (touch1 > 0 && selected) {
                    mState = Constants.STATE_PRESSED;
                    mTouchIndex = 0;
                } else if (touch2 > 0 && selected2) {
                    mState = Constants.STATE_PRESSED;
                    mTouchIndex = 1;
                }

                break;
            case Constants.STATE_PRESSED:
                if (mTouchIndex == 0) {
                    if (touch1 == 0 && selected) {
                        released = true;
                        mState = Constants.STATE_UNPRESSED;

                    } else if (!(selected)) {
                        mState = Constants.STATE_UNPRESSED;
                    }
                } else if (mTouchIndex == 1) {
                    if (touch2 == 0 && selected2) {
                        released = true;
                        mState = Constants.STATE_UNPRESSED;

                    } else if (!(selected2)) {
                        mState = Constants.STATE_UNPRESSED;
                    }
                }
                break;
        }

        return released;
    }

    private boolean isSelected(int touchx, int touchy) {
        return ((touchx >= (mPositionX)
                && touchx <= (mPositionX + mSizeX)
                && touchy >= (mPositionY)
                && touchy <= (mPositionY + mSizeY)));
    }
}