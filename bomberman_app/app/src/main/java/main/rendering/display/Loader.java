package main.rendering.display;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.concurrent.ConcurrentLinkedQueue;


public class Loader {
    private ConcurrentLinkedQueue<Loadable> mLoadablesQueue;
    private BitmapFactory.Options opts;

    public Loader(int width, float scale)
    {
        mLoadablesQueue = new ConcurrentLinkedQueue<Loadable>();
        opts = new BitmapFactory.Options();
        opts.inScaled = true;
        opts.inDensity = width;
        opts.inTargetDensity = (int) (width * scale);
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    public void putLoadable(Loadable loadable)
    {
        mLoadablesQueue.add(loadable);
    }

    public boolean hasLoadables()
    {
        return !mLoadablesQueue.isEmpty();
    }

    public Loadable getLoadable()
    {
        return mLoadablesQueue.remove();
    }

    public BitmapFactory.Options getLoaderOptions()
    {
        return opts;
    }
}
