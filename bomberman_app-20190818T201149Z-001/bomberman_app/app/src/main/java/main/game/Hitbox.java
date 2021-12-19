package main.game;

public class Hitbox {
    public final int sizeX;
    public final int sizeY;
    public final int offsetX;
    public final int offsetY;
    public int mTop;
    public int mLeft;
    public int mBottom;
    public int mRight;

    public Hitbox(int xoffset, int yoffset, int xsize, int ysize) {
        sizeX = xsize;
        sizeY = ysize;
        offsetX = xoffset;
        offsetY = yoffset;
    }

    public void updateEdges(int x, int y){
        mLeft = (x + offsetX);
        mRight = (mLeft + sizeX);
        mTop = (y + offsetY);
        mBottom = (mTop + sizeY);
    }

     public boolean intersects(Hitbox box){
        return (mLeft <box.mRight && mRight >box.mLeft && mTop <box.mBottom && mBottom >box.mTop);
    }
}
