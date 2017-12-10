package com.flynnsam.soundboardmediaplayer.audiotrack;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by sam on 2017-10-18.
 */

public class MediaCodecCallbacks extends MediaCodec.Callback {

    private static final int INPUT_BUFFER_SIZE = 128;

    private static final int OUTPUT_BUFFER_SIZE = 128;

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

        byte[] bytes = new byte[INPUT_BUFFER_SIZE];

        int bytesRead = 0;

        int bufferFlagToUse = MediaCodec.BUFFER_FLAG_KEY_FRAME;

        try {
            bytesRead = inputStream.read(bytes, offset, INPUT_BUFFER_SIZE);
        } catch (IOException e) {
            // TODO Log error
            codec.queueInputBuffer(index, 0, INPUT_BUFFER_SIZE, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return;
        }

        if (bytesRead < INPUT_BUFFER_SIZE) {
            byte[] compressedBytes = new byte[bytesRead];
            System.arraycopy(bytes, 0, compressedBytes, 0, bytesRead);
            bytes = compressedBytes;
            bufferFlagToUse = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
        }

        inputBuffer.put(bytes);

        codec.queueInputBuffer(index, 0, INPUT_BUFFER_SIZE, 0, bufferFlagToUse); //https://developer.android.com/reference/android/media/MediaCodec.html
    }

    @Override
    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

        ByteBuffer outputBuffer = codec.getOutputBuffer(index);
        MediaFormat bufferFormat = codec.getOutputFormat();

        byte[] bytes = new byte[OUTPUT_BUFFER_SIZE];

        outputBuffer.get(bytes); // TODO get bytes to the client

        codec.releaseOutputBuffer(index, false);
    }

    @Override
    public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        // TODO log error

        if (e.isRecoverable()) {
            codec.stop();
            //codec.configure(); TODO
            codec.start();
        } else {
            codec.reset();
        }
    }

    @Override
    public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        // Ignore this for now
    }
}
