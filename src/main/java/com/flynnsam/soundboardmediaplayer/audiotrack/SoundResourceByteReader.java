package com.flynnsam.soundboardmediaplayer.audiotrack;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

import java.io.IOException;
import java.io.InputStream;

import com.flynnsam.soundboardmediaplayer.ResourceLoadException;

/**
 * Class to handle getting audio byte data from android audio resources.
 * Created by Sam on 2017-08-23.
 *
 * References: https://stackoverflow.com/questions/14749038/how-to-get-bit-rate-sampling-rate-and-no-of-channels-of-a-audio-file-in-andro 2017-10-18
 */

public class SoundResourceByteReader {

    protected InputStream inputStream;
    int bufferLength;

    public SoundResourceByteReader(Context context, int resourceId, int bufferLength) throws ResourceLoadException {

        if (bufferLength < 1) {
            throw new IllegalArgumentException(String.format("Error creating buffer with length [%1]. Buffer length must be one or greater", bufferLength));
        }

        this.bufferLength = bufferLength;

        try {

            AssetFileDescriptor fd =  context.getResources().openRawResourceFd(resourceId);
            MediaExtractor metaDataExtractor = new MediaExtractor();
            metaDataExtractor.setDataSource(fd);

            MediaFormat format = metaDataExtractor.getTrackFormat(0);

            fd.createInputStream().;
        } catch (Resources.NotFoundException e) {
            throw new ResourceLoadException(String.format("Unable to load resource with id [%1]", resourceId), e);
        } catch (IOException e) {
            throw new ResourceLoadException(String.format("Unable to load resource with id [%1]", resourceId), e);
        }

        this.inputStream = context.getResources().openRawResource(resourceId);



    }

    public byte[] getSomeBytes() {

        inputStream.r
    }

    public void close() throws IOException {

        try {
            inputStream.close();
        } catch (IOException e) {

        }

        inputStream = null;
    }
}
