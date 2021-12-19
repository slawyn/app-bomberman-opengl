package main.communication;

public class SimpleQueue{
    int mIndexHead;
    int mIndexTail;
    int mUsed;
    byte[][] mData;
    SimpleQueue(int cnt, int sz){
        mData = new byte[cnt][sz];
        mIndexHead=0;
        mIndexTail=0;
    }

    public byte[] next(){
        return mData[mIndexHead];
    }

    public void enqueue(){
        mIndexHead = (mIndexHead+1)%mData.length;
        incrementUsed();
    }

    private synchronized  void decrementUsed(){
        --mUsed;
    }

    private synchronized void incrementUsed(){
        ++mUsed;
    }

    public byte[] peek(){
        return mData[mIndexTail];
    }

    public byte[] dequeue(){
        int old = mIndexTail;
        mIndexTail = (mIndexTail+1)%mData.length;
        decrementUsed();
        return mData[old];
    }

    public boolean notEmpty(){
        return mIndexTail!=mIndexHead;
    }
}
