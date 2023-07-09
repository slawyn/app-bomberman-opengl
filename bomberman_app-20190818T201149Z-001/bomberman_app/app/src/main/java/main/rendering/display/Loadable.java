package main.rendering.display;

public class Loadable {
    public int texturebaseindex;
    public int numberoftextures;
    public int resourceid;
    public Loadable(int baseindex, int numboftextures, int resid)
    {
        texturebaseindex = baseindex;
        numberoftextures = numboftextures;
        resourceid = resid;
    }
}
