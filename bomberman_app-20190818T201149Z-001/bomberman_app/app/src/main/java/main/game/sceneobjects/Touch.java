package main.game.sceneobjects;


import main.Constants;
import main.game.SceneElement;

import static main.game.SceneManager.SOBJ_TOUCH;

public class Touch extends SceneElement {

    private int mInputSelector;
    public Touch(int id, int type){
        super(SOBJ_TOUCH,id);
        mObjectSubtype = type;
        mState = Constants.STATE_UNPRESSED;
        mPositionX = -1000;
        mPositionY = -1000;
        mInputSelector = type *3;
    }

    // 0: x
    // 1: y
    // 2: touch
    // 3: x2
    // 4: y2
    // 5: touch
    @Override
    public void updateState(long dt, int[] input){
        mPreviousState = mState;
        switch(mState){
            case Constants.STATE_PRESSED:
                if(input[mInputSelector+2] == 0){
                    mPositionX = - 1000;
                    mPositionY = - 1000;
                    mState = Constants.STATE_UNPRESSED;
                }
                else
                {
                    mPositionX = (input[mInputSelector]);
                    mPositionY = (input[mInputSelector+1]);
                }
                break;
            case Constants.STATE_UNPRESSED:
                if(input[mInputSelector+2]>0)
                {
                    mPositionX = (input[mInputSelector]);
                    mPositionY = (input[mInputSelector+1]);
                    mState = Constants.STATE_PRESSED;
                }
                break;
        }
    }
}