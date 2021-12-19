package main.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.SparseIntArray;


import java.util.concurrent.ConcurrentLinkedQueue;


import main.Constants;

@SuppressWarnings("deprecation")
public class SoundManager implements Runnable
{
    private SoundPool mSoundPool;
    private final int mNumberOfStreams = 10;
    private Context context;
    private SparseIntArray mSoundList;
    private ConcurrentLinkedQueue<Integer> mSoundQueue;


    public SoundManager(Context context){
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(mNumberOfStreams).build();
        } else {
            mSoundPool = new SoundPool(mNumberOfStreams, AudioManager.STREAM_MUSIC, 1);
        }
        mSoundList = new SparseIntArray();
        mSoundQueue = new ConcurrentLinkedQueue<>();
    }

    public void pushToSoundQueue(int soundid){
        mSoundQueue.add(soundid);
    }


    private void loadSounds(){
        for(int idx=0;idx< Constants.SOUNDABLES.length;idx++){

            int sndid = mSoundPool.load(context, Constants.SOUNDABLES[idx][1], 1);
            mSoundList.put(Constants.SOUNDABLES[idx][0],sndid);
        }
    }

    private void playSound(int id){
        mSoundPool.play(mSoundList.get(id),1,1,1,0,1);
    }

    public void createMediaPlayer(Context context, int soundable){
        final MediaPlayer sfx =  MediaPlayer.create(context, soundable );
        // TODO
        //sfx.start();
    }

    @Override
    public void run()
    {

        loadSounds();

        boolean applicationRunning = true;
        while(applicationRunning){
            try
            {
                if(!mSoundQueue.isEmpty()){

                    int id = mSoundQueue.remove();
                    playSound(id);
                }

                Thread.sleep(1);
            } catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
