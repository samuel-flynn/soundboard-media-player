package com.flynnsam.soundboardmediaplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * This is a stripped down version of the android media player that makes a few assumptions:
 * <ol>
 *     <li>The underlying media player is always either playing or released. There is never
 *     a state where the media player is created, but not playing anything.</li>
 *     <li>The media player can be released as soon as a single sound action or set of sound actions
 *     are complete. There is no pausing, and as soon as a sound is finished or interrupted, the player
 *     can be released immediately.</li>
 *     <li>The media player is playing sounds based on a packaged resource ID</li>
 * </ol>
 *
 * Created by Sam on 2017-07-29.
 */

public class MediaPlayerProvider {

    protected final MediaPlayer.OnCompletionListener closeCallback;

    protected MediaPlayer player;

    protected Integer currentlyPlayingSoundId;

    public MediaPlayerProvider() {

        closeCallback = new CloseCallback();
        player = null;
        currentlyPlayingSoundId = null;

    }

    public synchronized void play(final Context context, final int soundId) {

        // Resetting the whole player can cause performance problems. If currently playing
        // and sound ID hasn't changed, just seek to beginning.
        if (isPlaying() && Integer.valueOf(soundId).equals(currentlyPlayingSoundId)) {
            player.seekTo(0);

        } else {
            if (isPlaying()) {
                player.stop();
                player.reset();

            }

            switchTrack(context, soundId);

        }
    }

    public synchronized void switchTrack( final Context context, final int soundIdToSwitchTo) {

        int positionToSeekTo = 0;

        if (isPlaying()) {
            positionToSeekTo = player.getCurrentPosition();
            player.stop();
            player.reset();
        }

        if (player == null) {
            player = new MediaPlayer();
        }

        prepareMediaPlayer(context, soundIdToSwitchTo);
        player.setOnCompletionListener(closeCallback);
        player.seekTo(positionToSeekTo);
        player.start();
    }

    private boolean isPlaying() {

        // Must make sure player is still open before checking isPlaying
        return player != null && player.isPlaying();
    }

    /**
     * Load a project resource into the media player and prepare it for playing.
     * Precondition: this.player has already been properly released
     * Source: https://stackoverflow.com/questions/1283499/setting-data-source-to-an-raw-id-in-mediaplayer
     * @param context Android context
     * @param resourceToLoad The android resource to load into the player
     */
    private void prepareMediaPlayer(final Context context, final int resourceToLoad) {

        AssetFileDescriptor afd = context.getResources().openRawResourceFd(resourceToLoad);
        if (afd == null) {
            throw new Resources.NotFoundException();
        }
        try {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.prepare();
        } catch (IOException e) {
            throw new RuntimeException("Unable to set data source.", e); // TODO throw a more appropriate exception
        }
    }

    protected class CloseCallback implements MediaPlayer.OnCompletionListener {

        @Override
        public synchronized void onCompletion(MediaPlayer mp) {
            mp.release();
            player = null;
            currentlyPlayingSoundId = null;
        }
    }
}
