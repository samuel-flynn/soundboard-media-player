package com.flynnsam.soundboardmediaplayer.audiotrack;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;

/**
 * Created by sam on 2017-12-09.
 */

public class MediaCodecConfigurator {

    private int resourceId;
    private Context context;

    public MediaCodecConfigurator(int resourceId, Context context) {
        this.resourceId = resourceId;
        this.context = context;
    }

    public void configureCodec(MediaCodec codec) {

        AssetFileDescriptor fd =  this.context.getResources().openRawResourceFd(this.resourceId);
        MediaExtractor metaDataExtractor = new MediaExtractor();
        try {
            metaDataExtractor.setDataSource(fd);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
        int numberTracks = metaDataExtractor.getTrackCount();

        for (int i = 0; i < numberTracks; i++) {
            MediaFormat format = metaDataExtractor.getTrackFormat(i);

            codec.configure(format, null, null, 0);
        }
    }
}
