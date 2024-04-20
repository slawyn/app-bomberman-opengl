package main.game.sceneobjects;

import static main.game.SceneManager.SOBJ_BACKGROUND_LOADING;

import main.game.SceneElement;


public class Loading extends SceneElement
{
    public int mLoadedPercent;
    public Loading(int id,  int subtype) {
        super(SOBJ_BACKGROUND_LOADING, id, subtype);
        mPositionX = 0;
        mPositionY = 0;
        mLoadedPercent = 0;
    }
}
