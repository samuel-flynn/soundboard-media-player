package com.flynnsam.soundboardmediaplayer;

import android.content.Context;
import android.media.MediaPlayer;

import com.flynnsam.carelesswhisperwidget.R;

/**
 * Utility class for handling a single instance of {@link MediaPlayer} across the JVM for the
 * air horn sound.
 *
 * Created by Sam on 2017-01-21.
 */

public class MediaPlayerProvider {

    protected static final MediaPlayer.OnCompletionListener CLOSE_CALLBACK = new CloseCallback();

    protected static final int AIR_HORN_SOUND_ID = R.raw.airhorn;

    protected static MediaPlayer player = null;

    protected static boolean playerOpen = false;

    public static void play(Context context) {

        synchronized (MediaPlayerProvider.class) {

            // Must make sure player is still open before checking isPlaying
            if (player != null && playerOpen && player.isPlaying()) {
                player.stop();
                player.release();
                playerOpen = false;
            }

            player = MediaPlayer.create(context, AIR_HORN_SOUND_ID);
            player.setOnCompletionListener(CLOSE_CALLBACK);
            player.start();

            playerOpen = true;
        }
    }

    protected static class CloseCallback implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            synchronized (MediaPlayerProvider.class) {
                mp.release();
                playerOpen = false;
            }
        }
    }
}
