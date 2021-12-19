package main.rendering.animation;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Layer
{
    private final static String TAG = "Layer";
    private RenderObject[] mSortedArrayForGPU;
    private int mArraySize;
    private boolean mDirty;

    private ConcurrentLinkedQueue<RenderObject> mNewRenderObjectsQueue;

    public Layer(int sz)
    {

        mSortedArrayForGPU = new RenderObject[sz];
        mNewRenderObjectsQueue = new ConcurrentLinkedQueue<>();
        mDirty = true;
    }




    public void addRenderObjectToLayer(RenderObject ro)
    {
        mNewRenderObjectsQueue.add(ro);
    }

    public RenderObject[] getSortedArray()
    {
        int size = mNewRenderObjectsQueue.size();
        for(int idx = size - 1; idx >= 0; idx--)
        {
            RenderObject ro = mNewRenderObjectsQueue.remove();
            add(ro);
        }

        if(mDirty)
        {
            // remove objects
            for(int idx = mArraySize - 1; idx >= 0; --idx)
            {
                if(mSortedArrayForGPU[idx].removeFromGPUthread)
                {
                    mSortedArrayForGPU[idx] = mSortedArrayForGPU[mArraySize - 1];
                    --mArraySize;
                }
            }
            sort();
        }
        return mSortedArrayForGPU;
    }

    public void add(RenderObject ro)
    {
        if(mArraySize < mSortedArrayForGPU.length)
        {
            mSortedArrayForGPU[mArraySize] = ro;
            ++mArraySize;
        }
    }

    public int getSortedArraySize()
    {
        return mArraySize;
    }


    public void sort()
    {
        if(mArraySize > 0)
        {
            quicksort(0, mArraySize - 1);
        }
    }

    // in-place: https://www.vogella.com/tutorials/JavaAlgorithmsQuicksort/article.html
    private void quicksort(int low, int high)
    {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        int pivot = mSortedArrayForGPU[low + (high - low) / 2].getSortCriteria();

        // Divide into two lists
        while(i <= j)
        {
            // If the current value from the left list is smaller than the pivot
            // element then get the next element from the left list
            while(mSortedArrayForGPU[i].getSortCriteria() > pivot)
            {
                i++;
            }
            // If the current value from the right list is larger than the pivot
            // element then get the next element from the right list
            while(mSortedArrayForGPU[j].getSortCriteria() < pivot)
            {
                j--;
            }

            // If we have found a value in the left list which is larger than
            // the pivot element and if we have found a value in the right list
            // which is smaller than the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if(i <= j)
            {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if(low < j)
            quicksort(low, j);
        if(i < high)
            quicksort(i, high);
    }

    private void exchange(int i, int j)
    {
        RenderObject temp = mSortedArrayForGPU[i];
        mSortedArrayForGPU[i] = mSortedArrayForGPU[j];
        mSortedArrayForGPU[j] = temp;
    }
}

