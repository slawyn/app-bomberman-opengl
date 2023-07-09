package main.rendering.animation;

import java.util.concurrent.ConcurrentLinkedQueue;

import main.rendering.elements.RenderElement;

public class Layer
{
    private final static String TAG = "Layer";
    private RenderElement[] mSortedArrayForGPU;
    private int mArraySize;
    private boolean mDirty;

    private ConcurrentLinkedQueue<RenderElement> mNewRenderObjectsQueue;

    public Layer(int sz)
    {

        mSortedArrayForGPU = new RenderElement[sz];
        mNewRenderObjectsQueue = new ConcurrentLinkedQueue<>();
        mDirty = true;
        mArraySize = 0;
    }


    public void addRenderObjectToLayer(RenderElement ro)
    {
        mNewRenderObjectsQueue.add(ro);
    }

    private void removeObjectsFromSortedArray()
    {
        if(mDirty)
        {
            // remove objects
            for(int idx = mArraySize - 1; idx >= 0; --idx)
            {
                if(mSortedArrayForGPU[idx].removeFromRenderingGpu)
                {
                    mSortedArrayForGPU[idx] = mSortedArrayForGPU[mArraySize - 1];
                    --mArraySize;
                }
            }
            sort();
        }
    }

    private void addONewbjectsToSortedArray()
    {
        int size = mNewRenderObjectsQueue.size();
        for(int idx = size - 1; idx >= 0; idx--)
        {
            RenderElement ro = mNewRenderObjectsQueue.remove();
            if(mArraySize < mSortedArrayForGPU.length)
            {
                mSortedArrayForGPU[mArraySize] = ro;
                ++mArraySize;
            }
        }
    }

    public RenderElement[] getSortedArray()
    {
        addONewbjectsToSortedArray();
        removeObjectsFromSortedArray();
        return mSortedArrayForGPU;
    }

    public int getSortedArraySize()
    {
        return mArraySize;
    }


    private void sort()
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
        RenderElement temp = mSortedArrayForGPU[i];
        mSortedArrayForGPU[i] = mSortedArrayForGPU[j];
        mSortedArrayForGPU[j] = temp;
    }
}

