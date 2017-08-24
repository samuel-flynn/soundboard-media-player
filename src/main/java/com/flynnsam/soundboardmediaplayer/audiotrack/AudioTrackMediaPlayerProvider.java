package com.flynnsam.soundboardmediaplayer.audiotrack;

import android.content.Context;
import android.media.AudioTrack;

import com.flynnsam.soundboardmediaplayer.OnCompletionPlayNextListener;
import com.flynnsam.soundboardmediaplayer.SoundboardMediaProvider;

/**
 * {@link AudioTrack}-based implementation of a soundboard media provider.
 * Created by Sam on 2017-08-23.
 */

public class AudioTrackMediaPlayerProvider implements SoundboardMediaProvider {

    protected AudioTrack audioTrack = null;

    protected Integer currentlyPlayingSoundId = null;
    protected Object currentlyPlayingSoundBufferedReader = null;

    protected Integer nextPlayingSoundId = null;

    @Override
    public void play(Context context, int soundId, OnCompletionPlayNextListener playNextListener) {

    }

    @Override
    public void switchTrack(Context context, int soundIdToSwitchTo, OnCompletionPlayNextListener playNextListener) {

    }

    @Override
    public void stop() {

    }

    @Override
    public Integer getCurrentlyPlayingSoundId() {
        return null;
    }
}
