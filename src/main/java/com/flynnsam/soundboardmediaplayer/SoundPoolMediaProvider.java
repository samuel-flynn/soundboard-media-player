package com.flynnsam.soundboardmediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;

/**
 * Implementation of {@link SoundboardMediaProvider} that uses {@link SoundPool}s for low-latency
 * playback.<br />
 * Created by sam on 2017-07-30.
 * <p>
 *     References:
 *     <ul>
 *         <li>https://github.com/genhau/SoundPoolPlayer/blob/master/SoundPoolPlayer.java</li>
 *     </ul>
 * </p>
 *
 */
@Deprecated
class SoundPoolMediaProvider implements SoundboardMediaProvider {

    private static final String LOGGER_TAG = SoundPoolMediaProvider.class.getName();

    /**
     * Set load priority to 1 for future compatibility's sake.<br />
     * Source: https://developer.android.com/reference/android/media/SoundPool.html#load(android.content.Context, int, int)
     */
    private static final int LOAD_PRIORITY = 1;

    /**
     * Set source quality to 0 for future compatibility's sake.<br />
     * Source: https://developer.android.com/reference/android/media/SoundPool.html#SoundPool(int, int, int)
     */
    private static final int SOURCE_QUALITY = 0;

    private static final float VOLUME = 1.0f;

    private static final int PRIORITY = 1;

    private static final int NUM_TIMES_TO_LOOP = 0;

    private static final float PLAYBACK_RATE = 1.0f;

    private SoundPool soundPool;

    private Integer currentlyPlayingResourceId;

    private Integer nextSoundResourceId;

    private Integer nextSoundIdToPlay;

    private Handler nextSoundScheduler;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void play(Context context, int soundResourceId, OnCompletionPlayNextListener playNextListener) {

        Log.d(LOGGER_TAG, String.format("Received request to play sound ID [%1$d]", soundResourceId));

        if (soundPool == null) {
            soundPool = createNewSoundPool();
        }

        loadAndPlay(context, playNextListener, soundResourceId);
    }

    @Override
    public synchronized void switchTrack(Context context, int soundIdToSwitchTo, OnCompletionPlayNextListener playNextListener) {
        throw new UnsupportedOperationException("Track switching is not currently supported for SoundPool players.");
    }

    @Override
    public synchronized void stop() {
        soundPool.release();
        soundPool = null;
        currentlyPlayingResourceId = null;
        nextSoundResourceId = null;
        nextSoundIdToPlay = null;
        nextSoundScheduler.removeCallbacksAndMessages(null);
        nextSoundScheduler = null;
    }

    @Override
    public synchronized Integer getCurrentlyPlayingSoundId() {
        return currentlyPlayingResourceId;
    }

    private void loadAndPlay(Context context, OnCompletionPlayNextListener playNextListener, int soundResourceId) {

        soundPool.load(context, soundResourceId, LOAD_PRIORITY);
        soundPool.setOnLoadCompleteListener(new LoadCompletionListener(context, playNextListener, soundResourceId));
    }

    private void doPlay(Context context, OnCompletionPlayNextListener playNextListener, int soundIdToPlay, int soundResourceId) {

        soundPool.play(soundIdToPlay, VOLUME, VOLUME, PRIORITY, NUM_TIMES_TO_LOOP, PLAYBACK_RATE);
        currentlyPlayingResourceId = soundResourceId;

        preLoadNextSound(context, playNextListener);
        scheduleNextSound(context, playNextListener, soundResourceId);
    }

    /**
     * Precondition: soundPool is not null
     * @param context Android context for resolving resources
     * @param playNextListener The listener that handles which sound file to play next
     */
    private void preLoadNextSound(Context context, OnCompletionPlayNextListener playNextListener) {

        nextSoundResourceId = null;

        if (playNextListener != null && playNextListener.getNextTrackResId(this) != null) {
            nextSoundResourceId = playNextListener.getNextTrackResId(this);

            nextSoundIdToPlay = soundPool.load(context, nextSoundResourceId, LOAD_PRIORITY);
        }
    }

    private void scheduleNextSound(Context context, OnCompletionPlayNextListener playNextListener, int currentResourceId) {

        if (nextSoundIdToPlay != null) {
            nextSoundScheduler = new Handler();
            long currentResourceDuration = getSoundDuration(context, currentResourceId);
            Log.d(LOGGER_TAG, String.format("Scheduling on-completion listener to trigger in [%1$d]ms", currentResourceDuration));
            nextSoundScheduler.postDelayed(new PlaybackCompletionListener(context, playNextListener), currentResourceDuration);
        }
    }

    private SoundPool createNewSoundPool() {
        return new SoundPool(1, AudioManager.STREAM_MUSIC, SOURCE_QUALITY);
    }

    private long getSoundDuration(Context context, int resourceId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, resourceId);
        return mediaPlayer.getDuration();
    }

    private void playNextSound(Context context, OnCompletionPlayNextListener playNextListener) {

        if (nextSoundIdToPlay != null) {

            Log.d(LOGGER_TAG, String.format("Playing next song in queue. Resource ID [%1$d]", nextSoundResourceId));

            doPlay(context, playNextListener, nextSoundIdToPlay, nextSoundResourceId);

        } else {
            stop();
        }
    }

    private class LoadCompletionListener implements SoundPool.OnLoadCompleteListener {

        private Context context;

        private OnCompletionPlayNextListener playNextListener;

        private int soundResourceId;

        LoadCompletionListener(Context context, OnCompletionPlayNextListener playNextListener, int soundResourceId) {
            this.context = context;
            this.playNextListener = playNextListener;
            this.soundResourceId = soundResourceId;
        }

        @Override
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            doPlay(context, playNextListener, sampleId, soundResourceId);
            soundPool.setOnLoadCompleteListener(null);
        }
    }

    private class PlaybackCompletionListener implements Runnable {

        Context context;

        OnCompletionPlayNextListener playNextListener;

        PlaybackCompletionListener(Context context, OnCompletionPlayNextListener playNextListener) {
            this.context = context;
            this.playNextListener = playNextListener;
        }

        @Override
        public void run() {
            if (soundPool != null) {
                playNextSound(context, playNextListener);
            }
        }
    }
}
