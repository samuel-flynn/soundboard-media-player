package com.flynnsam.soundboardmediaplayer.audiotrack;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by sam on 2017-10-18.
 */

public class MediaCodecCallbacks extends MediaCodec.Callback {

    private static final int BUFFER_SIZE = 128;

    private InputStream inputStream;

    private int offset;

    public MediaCodecCallbacks(InputStream inputStream) {
        super();

        this.inputStream = inputStream;
        this.offset = 0;
    }

    @Override
    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        ByteBuffer inputBuffer = codec.getInputBuffer(index);

        byte[] bytes = new byte[BUFFER_SIZE];

        int bytesRead = inputStream.read(bytes, offset, BUFFER_SIZE);

        if (bytesRead < BUFFER_SIZE) {
            byte[] compressedBytes = new byte[bytesRead];
            System.arraycopy(bytes, 0, compressedBytes, 0, bytesRead);
            bytes = compressedBytes;
        }

        inputBuffer.put(bytes);

        codec.queueInputBuffer(index, 0, BUFFER_SIZE, 0); //https://developer.android.com/reference/android/media/MediaCodec.html
    }

    @Override
    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

    }

    @Override
    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

    }

    @Override
    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

    }
}
