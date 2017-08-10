package com.flynnsam.soundboardmediaplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Sam on 2017-08-09.
 */

public class ThreadedMediaPlayerProvider implements SoundboardMediaProvider {

    private static final String LOGGER_TAG = ThreadedMediaPlayerProvider.class.getName();

    private static final int DELAY_TUNING_MS = 100;

    private static final int DELAY_CLEANUP_MS = 500;

    private OnCompletionPlayNextListener onCompletionPlayNextListener;

    private MediaPlayer activePlayer;

    private MediaPlayer nextPlayer;

    private Integer currentlyPlayingSoundId;

    private Integer nextSoundId;

    private Handler nextSoundScheduler;

    private Handler mediaPlayerCleaner;

    /**
     * Play a sound from the beginning, with a specified track to play next once this one is complete.
     * @param context The initiating android context
     * @param soundId The resource ID of the sound to be played
     * @param playNextListener The on-completion listener to use to determine the next track to play
     */
    @Override
    public synchronized void play(final Context context, final int soundId, final OnCompletionPlayNextListener playNextListener) {

        onCompletionPlayNextListener = playNextListener;

        // Resetting the whole activePlayer can cause performance problems. If currently playing
        // and sound ID hasn't changed, just seek to beginning.
        if (isPlaying() && Integer.valueOf(soundId).equals(currentlyPlayingSoundId)) {
            activePlayer.seekTo(0);
            prepareNextPlayer(context);

        } else {

            if (activePlayer != null) {
                activePlayer.reset();
            }

            switchTrack(context, soundId, playNextListener);
        }
    }

    /**
     * Switch over to a new track at the current timestamp of the track that is currently playing.
     * If no track is currently playing, the new track is started from the beginning.
     * @param context The android context that is initiating this action
     * @param soundIdToSwitchTo The resource ID of the track to switch to
     */
    @Override
    public synchronized void switchTrack( final Context context, final int soundIdToSwitchTo, final OnCompletionPlayNextListener playNextListener) {

        onCompletionPlayNextListener = playNextListener;

        int positionToSeekTo = 0;

        if (isPlaying()) {

            positionToSeekTo = activePlayer.getCurrentPosition();
            activePlayer.stop();
            activePlayer.reset();
        }

        if (activePlayer == null) {
            activePlayer = new MediaPlayer();
        }

        prepareMediaPlayer(context, activePlayer, soundIdToSwitchTo);
        //activePlayer.setOnCompletionListener(new ThreadedMediaPlayerProvider.CloseCallback(context));
        activePlayer.seekTo(positionToSeekTo);
        currentlyPlayingSoundId = soundIdToSwitchTo;
        activePlayer.start();

        prepareNextPlayer(context);
    }

    /**
     * Halt the underlying media activePlayer and release it.
     */
    @Override
    public synchronized void stop() {

        if (nextSoundScheduler != null) {
            nextSoundScheduler.removeCallbacksAndMessages(null);
            nextSoundScheduler = null;
        }

        if (activePlayer != null) {
            activePlayer.release();
            activePlayer = null;
        }
        if (nextPlayer != null) {
            nextPlayer.release();
            nextPlayer = null;
        }
        currentlyPlayingSoundId = null;
        nextSoundId = null;
    }

    /**
     * Get the resource ID of the track that is currently playing.
     * @return The resource ID of the currently playing track, or {@code null} if there is no track
     * currently playing
     */
    @Override
    public synchronized Integer getCurrentlyPlayingSoundId() {
        return currentlyPlayingSoundId;
    }

    /**
     * Determine if the managed media activePlayer is currently playing a track.
     * @return {@code true} if the media activePlayer is currently playing a track. {@code false} otherwise.
     */
    private  boolean isPlaying() {

        // Must make sure activePlayer is still open before checking isPlaying
        return activePlayer != null && activePlayer.isPlaying();
    }

    /**
     * Load a project resource into the media activePlayer and prepare it for playing.
     * Precondition: this.activePlayer has already been properly released
     * Source: https://stackoverflow.com/questions/1283499/setting-data-source-to-an-raw-id-in-mediaplayer
     * @param context Android context
     * @param playerToPrepare The media player instance to prepare
     * @param resourceToLoad The android resource to load into the activePlayer
     */
    private void prepareMediaPlayer(final Context context, final MediaPlayer playerToPrepare, final int resourceToLoad) {

        AssetFileDescriptor afd = context.getResources().openRawResourceFd(resourceToLoad);
        if (afd == null) {
            throw new Resources.NotFoundException();
        }
        try {
            playerToPrepare.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            playerToPrepare.prepare();
        } catch (IOException e) {
            throw new ResourceLoadException("Unable to set data source.", e);
        }
    }

    /**
     * Prepare the next player (load it's media and prepare it for playing) so that it can be played
     * immediately on completion of the current one).
     * @param context The android context to use to set up the next media player
     */
    private void prepareNextPlayer(Context context) {

        if (nextSoundScheduler != null) {
            nextSoundScheduler.removeCallbacksAndMessages(null);
        }
        nextSoundScheduler = new Handler();

        if (nextPlayer != null) {
            nextPlayer.release();
            nextPlayer = null;
            nextSoundId = null;
        }

        if (onCompletionPlayNextListener != null && onCompletionPlayNextListener.getNextTrackResId(this) != null) {
            nextSoundId = onCompletionPlayNextListener.getNextTrackResId(this);
            nextPlayer = new MediaPlayer();
            //nextPlayer.setOnCompletionListener(new ThreadedMediaPlayerProvider.CloseCallback(context));
            prepareMediaPlayer(context, nextPlayer, nextSoundId);
            scheduleNextSound(nextSoundScheduler, new NextPlayerRunnable(context), activePlayer.getDuration());
        }
    }

    private void scheduleNextSound(Handler handler, Runnable runnableToSchedule, int duration) {

        int delay = duration - DELAY_TUNING_MS;

        if (delay < 0) {
            delay = 0;
        }

        handler.postDelayed(runnableToSchedule, delay);
    }

    /**
     * On-completion listener for {@code MediaPlayerProvider}-managed media players. This listener
     * checks for a track to play next. If one is present, it plays it immediately. Otherwise,
     * the media activePlayer is released;
     */
    private class CloseCallback implements MediaPlayer.OnCompletionListener {

        private Context initializingContext;

        /**
         * Constructor that captures the initiating context, which may be used to play the next track.
         *
         * @param initializingContext The initiating android context
         */
        private CloseCallback(final Context initializingContext) {
            this.initializingContext = initializingContext;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void onCompletion(MediaPlayer mp) {

            if (nextPlayer != null) {

                // Cycle the next player to being the active one
                activePlayer.release();
                activePlayer = nextPlayer;
                nextPlayer = null;

                currentlyPlayingSoundId = nextSoundId;
                nextSoundId = null;

                prepareNextPlayer(initializingContext);

            } else {
                stop();
            }
        }
    }

    private class NextPlayerRunnable implements Runnable {

        private Context initializingContext;

        public NextPlayerRunnable(final Context context) {
            this.initializingContext = context;
        }

        @Override
        public synchronized void run() {

            Log.d(LOGGER_TAG, String.format("Beginning scheduled play of resource ID [%s]", nextSoundId));

            if (nextPlayer != null) {

                // Cycle the next player to being the active one
                MediaPlayer lastPlayer = activePlayer;
                activePlayer = nextPlayer;
                nextPlayer = null;

                currentlyPlayingSoundId = nextSoundId;
                nextSoundId = null;
                activePlayer.start();

                prepareNextPlayer(initializingContext);

                mediaPlayerCleaner = new Handler();
                mediaPlayerCleaner.postDelayed(new ReleaseMediaPlayerRunnable(lastPlayer), DELAY_CLEANUP_MS);

            } else {
                stop();
            }
        }
    }

    private class ReleaseMediaPlayerRunnable implements Runnable {

        private MediaPlayer mediaPlayerToRelease;

        public ReleaseMediaPlayerRunnable(MediaPlayer mediaPlayerToRelease) {
            this.mediaPlayerToRelease = mediaPlayerToRelease;
        }

        @Override
        public void run() {
            mediaPlayerToRelease.release();
        }
    }
}
