package com.flynnsam.soundboardmediaplayer;

/**
 * Factory class for {@link SoundboardMediaProvider} implementations
 * Created by sam on 2017-07-30.
 */

public class SoundboardMediaProviderFactory {

    /**
     * Create an implementation of a {@link SoundboardMediaProvider}.
     * @return A soundboard media provider
     */
    public static SoundboardMediaProvider createSoundboardMediaProvider() {
        return new ThreadedMediaPlayerProvider();
    }
}
