package main.game.sceneobjects;

import main.Constants;
import main.game.SceneElement;

import static main.game.SceneManager.SOBJ_BUTTON;
import static main.game.SceneManager.SOBJ_BUTTON_SQUARED;

public class ButtonRectangular extends Button
{
    public ButtonRectangular(int id, int posx, int posy, int sizex, int sizey, int action, int animation)
    {
        super(SOBJ_BUTTON, id, posx, posy, sizex, sizey, action, animation);
    }
}