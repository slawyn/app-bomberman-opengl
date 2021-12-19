package main.game.sceneobjects;

import main.Constants;
import main.Globals;

import static main.game.SceneManager.SOBJ_BACKGROUND_LOADING;


public class Loading extends SceneObject
{
    public int mLoadedPercent;
    public Loading(int id,  int subtype) {
        super(SOBJ_BACKGROUND_LOADING, id);
        mObjectSubtype = subtype;
        mPositionX = 0;
        mPositionY = 0;
        mLoadedPercent = 0;
    }
}
