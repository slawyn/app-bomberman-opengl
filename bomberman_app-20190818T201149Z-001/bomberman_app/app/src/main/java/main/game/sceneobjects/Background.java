package main.game.sceneobjects;

import static main.game.SceneManager.SOBJ_BACKGROUND;

import main.game.SceneElement;

public class Background extends SceneElement {
    public Background(int id,  int subtype) {
        super(SOBJ_BACKGROUND, id );
        mObjectSubtype = subtype;
        mPositionX = 0;
        mPositionY = 0;
    }
}
