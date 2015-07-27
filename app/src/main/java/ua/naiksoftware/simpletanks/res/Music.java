package ua.naiksoftware.simpletanks.res;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.HashMap;

import ua.naiksoftware.simpletanks.Log;

/**
 * Created by Naik on 25.07.15.
 */
public class Music {

    private static final String TAG = Music.class.getSimpleName();

    private static Context context;

    /* Music */
    private static final HashMap<Object, SparseArray<MediaPlayer>> musicsMap = new HashMap<Object, SparseArray<MediaPlayer>>();

    /* Sound */
    private static SoundPool soundPool;
    private static SparseIntArray loadedSounds = new SparseIntArray(); // rawID -> soundID
    private static final HashMap<Object, SparseIntArray> soundsMap = new HashMap<Object, SparseIntArray>(); // key -> (soundID -> playID)

    public static void init(Context context) {
        Log.e(TAG, "Init");
        Music.context = context;
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    }

    private static MediaPlayer getMusic(Object key, int rawID) {
        SparseArray<MediaPlayer> musics = musicsMap.get(key);
        if (musics == null) {
            musics = new SparseArray<MediaPlayer>();
            musicsMap.put(key, musics);
        }
        MediaPlayer music = musics.get(rawID);
        if (music == null) {
            music = MediaPlayer.create(context, rawID);
            musics.put(rawID, music);
        }
        return music;
    }

    public static void playMusic(final Object key, final int rawID, final boolean loop) {
        MediaPlayer music = getMusic(key, rawID);
        if (loop) {
            if (!music.isPlaying()) {
                music.setLooping(true);
                music.start();
            }
        } else {
            if (music.isPlaying()) {
                music.seekTo(0);
            }
            music.start();
        }
    }

    public static void stopMusic(Object key, int rawID) {
        MediaPlayer sound = getMusic(key, rawID);
        sound.stop();
    }

    public static synchronized void playSound(final Object key, final int rawID, final float volume, final boolean loop) {
        //Log.e(TAG, "playSound(" + key + ", " + rawID + ", " + volume + ", " + loop + ")");
        SparseIntArray playingSounds = soundsMap.get(key);
        if (playingSounds == null) {
            playingSounds = new SparseIntArray();
            soundsMap.put(key, playingSounds);
            Log.e(TAG, "create new playingSounds list");
        }
        int soundID = loadedSounds.get(rawID, -1);
        if (soundID == -1) {
            Log.e(TAG, "start load new sound");
            final long startLoad = System.currentTimeMillis();
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    loadedSounds.put(rawID, sampleId);
                    long loadTime = System.currentTimeMillis() - startLoad;
                    Log.e(TAG, "loaded rawID=" + rawID + ", soundID=" + sampleId + ", time=" + loadTime);
                    if (loadTime < 500) {
                        // Проиграть звук только если прошло не более 500 мс после начала загрузки
                        Log.e(TAG, "start recursive play loaded sound");
                        playSound(key, rawID, volume, loop);
                    }
                }
            });
            soundPool.load(context, rawID, 1);
            return;
        }
        int loops = loop ? -1 : 0;
        int playID = playingSounds.get(soundID, -1);
        if (playID == -1) { // Если этот звук еще не играет - играем.
            playingSounds.put(soundID, soundPool.play(soundID, volume, volume, 0, loops, 1));
            //Log.e(TAG, "start soundID=" + soundID + " as playID=" + playingSounds.get(soundID));
        } else { // Иначе остановим его и проиграем.
            //Log.e(TAG, "sound " + soundID + " playing now as playID=" + playID + ", stopping");
            soundPool.stop(playID);
            playingSounds.put(soundID, soundPool.play(soundID, volume, volume, 0, loops, 1));
            //Log.e(TAG, "restart soundID=" + soundID + " as playID=" + playingSounds.get(soundID));
        }
    }

    public static synchronized void stopSound(Object key, int rawID) {
        //Log.e(TAG, "stopSound(" + key + ", " + rawID + ")");
        int soundID = loadedSounds.get(rawID);
        SparseIntArray playingSounds = soundsMap.get(key);
        int playID = playingSounds.get(soundID);
        soundPool.stop(playID);
        playingSounds.delete(soundID);
        //Log.e(TAG, "stopped rawID=" + rawID + ", soundID=" + soundID + ", playID=" + playID);
    }

    public static synchronized void dispose() {
        Log.e(TAG, "Dispose");
        // Stop music.
        for (SparseArray<MediaPlayer> musics : musicsMap.values()) {
            for (int i = 0, size = musics.size(); i < size; i++) {
                MediaPlayer mediaPlayer = musics.valueAt(i);
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            musics.clear();
        }
        musicsMap.clear();
        // Stop sounds.
        for (SparseIntArray playingSounds : soundsMap.values()) {
            for (int i = 0, size = playingSounds.size(); i < size; i++) {
                int playID = playingSounds.valueAt(i);
                soundPool.stop(playID);
            }
            playingSounds.clear();
        }
        soundsMap.clear();
        // Unload sounds.
        for (int i = 0, size = loadedSounds.size(); i < size; i++) {
            int soundID = loadedSounds.valueAt(i);
            soundPool.unload(soundID);
        }
        loadedSounds.clear();
        soundPool.release();
    }
}
