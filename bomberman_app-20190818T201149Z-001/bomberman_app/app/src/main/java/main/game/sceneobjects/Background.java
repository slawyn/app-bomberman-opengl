package main.game.sceneobjects;

import main.Constants;
import main.Globals;

import static main.Constants.*;
import static main.game.SceneManager.SOBJ_BACKGROUND;

public class Background extends SceneObject {
    public Background(int id,  int subtype) {
        super(SOBJ_BACKGROUND, id );
        mObjectSubtype = subtype;
        mPositionX = 0;
        mPositionY = 0;
    }
}
