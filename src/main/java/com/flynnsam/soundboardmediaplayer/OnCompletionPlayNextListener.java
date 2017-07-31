package com.flynnsam.soundboardmediaplayer;

/**
 * Interface for the {@link MediaPlayerProvider}'s on-completion queueing system.
 * Implementations should indicate which resource should play next once the currently playing
 * track has completed.
 * Created by admin on 2017-07-30.
 */

public interface OnCompletionPlayNextListener {

    /**
     * Get the resource ID of the track that should be played next, once the current track has
     * completed.
     *
     * @param soundboardMediaProvider Media player provider that is requesting the next track
     * @return the resource ID of the track to play next, or {@code null} if the player should
     * halt once the current track is completed.
     */
    Integer getNextTrackResId(SoundboardMediaProvider soundboardMediaProvider);
}
