package main.game.sceneobjects;

import static main.game.SceneManager.SOBJ_BUTTON;
import static main.game.SceneManager.SOBJ_BUTTON_SQUARED;

import main.Constants;
import main.game.SceneElement;

public class ButtonSquared extends Button
{
    public ButtonSquared(int id, int posx, int posy, int sizex, int sizey, int action, int animation)
    {
        super(SOBJ_BUTTON_SQUARED, id, posx, posy, sizex, sizey, action, animation);
    }
}