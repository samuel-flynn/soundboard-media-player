package com.flynnsam.soundboardmediaplayer;

import android.content.Context;

/**
 * This is a stripped down version of the android media player that makes a few assumptions:
 * <ul>
 *     <li>The underlying media player is always either playing or released. There is never
 *     a state where the media player is created, but not playing anything.</li>
 *     <li>The media player can be released as soon as a single sound action or set of sound actions
 *     are complete. There is no pausing, and as soon as a sound is finished or interrupted, the player
 *     can be released immediately.</li>
 *     <li>The media player is playing sounds based on a packaged resource ID</li>
 * </ul>
 *
 * Created by sam on 2017-07-30.
 */

public interface SoundboardMediaProvider {

    /**
     * Play a sound from the beginning, with a specified track to play next once this one is complete.
     * @param context The initiating android context
     * @param soundId The resource ID of the sound to be played
     * @param playNextListener The on-completion listener to use to determine the next track to play
     */
    void play(final Context context, final int soundId, final OnCompletionPlayNextListener playNextListener);

    /**
     * Switch over to a new track at the current timestamp of the track that is currently playing.
     * If no track is currently playing, the new track is started from the beginning.
     * @param context The android context that is initiating this action
     * @param soundIdToSwitchTo The resource ID of the track to switch to
     */
    void switchTrack( final Context context, final int soundIdToSwitchTo, final OnCompletionPlayNextListener playNextListener);

    /**
     * Halt the underlying media player and clean everything up.
     */
    void stop();

    /**
     * Get the resource ID of the track that is currently playing.
     * @return The resource ID of the currently playing track, or {@code null} if there is no track
     * currently playing
     */
    Integer getCurrentlyPlayingSoundId();
}
