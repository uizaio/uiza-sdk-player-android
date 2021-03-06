/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uiza.player.ext.ima;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Assertions;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import vn.loitp.core.utilities.LLog;

/**
 * A {@link MediaSource} that inserts ads linearly with a provided content media source using the
 * Interactive Media Ads SDK for ad loading and tracking.
 */
public final class ImaAdsMediaSource implements MediaSource {

    /**
     * Listener for events relating to ad loading.
     */
    public interface AdsListener {

        /**
         * Called if there was an error loading ads. The media source will load the content without ads
         * if ads can't be loaded, so listen for this event if you need to implement additional handling
         * (for example, stopping the player).
         *
         * @param error The error.
         */
        void onAdLoadError(IOException error);

        /**
         * Called when the user clicks through an ad (for example, following a 'learn more' link).
         */
        void onAdClicked();

        /**
         * Called when the user taps a non-clickthrough part of an ad.
         */
        void onAdTapped();
    }

    private static final String TAG = ImaAdsMediaSource.class.getSimpleName();

    private final MediaSource contentMediaSource;
    private final DataSource.Factory dataSourceFactory;
    private final ImaAdsLoader imaAdsLoader;
    private final ViewGroup adUiViewGroup;
    private final Handler mainHandler;
    private final AdsLoaderListener adsLoaderListener;
    private final Map<MediaPeriod, MediaSource> adMediaSourceByMediaPeriod;
    private final Timeline.Period period;
    @Nullable
    private final Handler eventHandler;
    @Nullable
    private AdsListener eventListener;

    private Handler playerHandler;
    private ExoPlayer player;
    private volatile boolean released;

    // Accessed on the player thread.
    private Timeline contentTimeline;
    private Object contentManifest;
    private AdPlaybackState adPlaybackState;
    private MediaSource[][] adGroupMediaSources;
    private long[][] adDurationsUs;
    private Listener listener;

    /**
     * Constructs a new source that inserts ads linearly with the content specified by
     * {@code contentMediaSource}.
     *
     * @param contentMediaSource The {@link MediaSource} providing the content to play.
     * @param dataSourceFactory  Factory for data sources used to load ad media.
     * @param imaAdsLoader       The loader for ads.
     * @param adUiViewGroup      A {@link ViewGroup} on top of the player that will show any ad UI.
     */
    public ImaAdsMediaSource(MediaSource contentMediaSource, DataSource.Factory dataSourceFactory,
                             ImaAdsLoader imaAdsLoader, ViewGroup adUiViewGroup) {
        this(contentMediaSource, dataSourceFactory, imaAdsLoader, adUiViewGroup, null, null);
    }

    public ImaAdsMediaSource(MediaSource contentMediaSource, DataSource.Factory dataSourceFactory,
                             ImaAdsLoader imaAdsLoader, ViewGroup adUiViewGroup, AdsLoaderListener adsLoaderListener) {
        this(contentMediaSource, dataSourceFactory, imaAdsLoader, adUiViewGroup, null, null);
    }

    /**
     * Constructs a new source that inserts ads linearly with the content specified by
     * {@code contentMediaSource}.
     *
     * @param contentMediaSource The {@link MediaSource} providing the content to play.
     * @param dataSourceFactory  Factory for data sources used to load ad media.
     * @param imaAdsLoader       The loader for ads.
     * @param adUiViewGroup      A {@link ViewGroup} on top of the player that will show any ad UI.
     * @param eventHandler       A handler for events. May be null if delivery of events is not required.
     * @param eventListener      A listener of events. May be null if delivery of events is not required.
     */
    public ImaAdsMediaSource(MediaSource contentMediaSource, DataSource.Factory dataSourceFactory,
                             ImaAdsLoader imaAdsLoader, ViewGroup adUiViewGroup, @Nullable Handler eventHandler,
                             @Nullable AdsListener eventListener) {
        this.contentMediaSource = contentMediaSource;
        this.dataSourceFactory = dataSourceFactory;
        this.imaAdsLoader = imaAdsLoader;
        this.adUiViewGroup = adUiViewGroup;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        mainHandler = new Handler(Looper.getMainLooper());
        adsLoaderListener = new AdsLoaderListener();
        adMediaSourceByMediaPeriod = new HashMap<>();
        period = new Timeline.Period();
        adGroupMediaSources = new MediaSource[0][];
        adDurationsUs = new long[0][];
    }

    @Override
    public void prepareSource(final ExoPlayer player, boolean isTopLevelSource, Listener listener) {
        Assertions.checkArgument(isTopLevelSource);
        this.listener = listener;
        this.player = player;
        playerHandler = new Handler();
        contentMediaSource.prepareSource(player, false, new Listener() {
            @Override
            public void onSourceInfoRefreshed(Timeline timeline, Object manifest) {
                ImaAdsMediaSource.this.onContentSourceInfoRefreshed(timeline, manifest);
            }
        });
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                imaAdsLoader.attachPlayer(player, adsLoaderListener, adUiViewGroup);
            }
        });
    }

    @Override
    public void maybeThrowSourceInfoRefreshError() throws IOException {
        contentMediaSource.maybeThrowSourceInfoRefreshError();
        for (MediaSource[] mediaSources : adGroupMediaSources) {
            for (MediaSource mediaSource : mediaSources) {
                if (mediaSource != null) {
                    mediaSource.maybeThrowSourceInfoRefreshError();
                }
            }
        }
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator) {
        if (adPlaybackState.adGroupCount > 0 && id.isAd()) {
            final int adGroupIndex = id.adGroupIndex;
            final int adIndexInAdGroup = id.adIndexInAdGroup;
            if (adGroupMediaSources[adGroupIndex].length <= adIndexInAdGroup) {
                MediaSource adMediaSource = new ExtractorMediaSource(
                        adPlaybackState.adUris[id.adGroupIndex][id.adIndexInAdGroup], dataSourceFactory,
                        new DefaultExtractorsFactory(), mainHandler, adsLoaderListener);
                int oldAdCount = adGroupMediaSources[id.adGroupIndex].length;
                if (adIndexInAdGroup >= oldAdCount) {
                    int adCount = adIndexInAdGroup + 1;
                    adGroupMediaSources[adGroupIndex] =
                            Arrays.copyOf(adGroupMediaSources[adGroupIndex], adCount);
                    adDurationsUs[adGroupIndex] = Arrays.copyOf(adDurationsUs[adGroupIndex], adCount);
                    Arrays.fill(adDurationsUs[adGroupIndex], oldAdCount, adCount, C.TIME_UNSET);
                }
                adGroupMediaSources[adGroupIndex][adIndexInAdGroup] = adMediaSource;
                adMediaSource.prepareSource(player, false, new Listener() {
                    @Override
                    public void onSourceInfoRefreshed(Timeline timeline, Object manifest) {
                        onAdSourceInfoRefreshed(adGroupIndex, adIndexInAdGroup, timeline);
                    }
                });
            }
            MediaSource mediaSource = adGroupMediaSources[adGroupIndex][adIndexInAdGroup];
            MediaPeriod mediaPeriod = mediaSource.createPeriod(new MediaPeriodId(0), allocator);
            adMediaSourceByMediaPeriod.put(mediaPeriod, mediaSource);
            return mediaPeriod;
        } else {
            return contentMediaSource.createPeriod(id, allocator);
        }
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        if (adMediaSourceByMediaPeriod.containsKey(mediaPeriod)) {
            adMediaSourceByMediaPeriod.remove(mediaPeriod).releasePeriod(mediaPeriod);
        } else {
            contentMediaSource.releasePeriod(mediaPeriod);
        }
    }

    @Override
    public void releaseSource() {
        released = true;
        contentMediaSource.releaseSource();
        for (MediaSource[] mediaSources : adGroupMediaSources) {
            for (MediaSource mediaSource : mediaSources) {
                if (mediaSource != null) {
                    mediaSource.releaseSource();
                }
            }
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                imaAdsLoader.detachPlayer();
            }
        });
    }

    // Internal methods.

    private void onAdPlaybackState(AdPlaybackState adPlaybackState) {
        if (this.adPlaybackState == null) {
            adGroupMediaSources = new MediaSource[adPlaybackState.adGroupCount][];
            Arrays.fill(adGroupMediaSources, new MediaSource[0]);
            adDurationsUs = new long[adPlaybackState.adGroupCount][];
            Arrays.fill(adDurationsUs, new long[0]);
        }
        this.adPlaybackState = adPlaybackState;
        maybeUpdateSourceInfo();
    }

    private void onLoadError(final IOException error) {
        LLog.d(TAG, "Ad load error" + error.toString());
        if (eventHandler != null && eventListener != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!released) {
                        eventListener.onAdLoadError(error);
                    }
                }
            });
        }
    }

    private void onContentSourceInfoRefreshed(Timeline timeline, Object manifest) {
        contentTimeline = timeline;
        contentManifest = manifest;
        maybeUpdateSourceInfo();
    }

    private void onAdSourceInfoRefreshed(int adGroupIndex, int adIndexInAdGroup, Timeline timeline) {
        Assertions.checkArgument(timeline.getPeriodCount() == 1);
        adDurationsUs[adGroupIndex][adIndexInAdGroup] = timeline.getPeriod(0, period).getDurationUs();
        maybeUpdateSourceInfo();
    }

    private void maybeUpdateSourceInfo() {
        if (adPlaybackState != null && contentTimeline != null) {
            Timeline timeline = adPlaybackState.adGroupCount == 0 ? contentTimeline
                    : new SinglePeriodAdTimeline(contentTimeline, adPlaybackState.adGroupTimesUs,
                    adPlaybackState.adCounts, adPlaybackState.adsLoadedCounts,
                    adPlaybackState.adsPlayedCounts, adDurationsUs, adPlaybackState.adResumePositionUs);
            listener.onSourceInfoRefreshed(timeline, contentManifest);
        }
    }

    /**
     * Listener for ad loading events. All methods are called on the main thread.
     */
    public class AdsLoaderListener implements ImaAdsLoader.EventListener, ExtractorMediaSource.EventListener {

        @Override
        public void onAdPlaybackState(final AdPlaybackState adPlaybackState) {
            LLog.d(TAG, "AdsLoaderListener onAdPlaybackState");
            if (released) {
                return;
            }
            playerHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (released) {
                        return;
                    }
                    ImaAdsMediaSource.this.onAdPlaybackState(adPlaybackState);
                }
            });
        }

        @Override
        public void onLoadError(final IOException error) {
            LLog.d(TAG, "AdsLoaderListener onLoadError");
            if (released) {
                return;
            }
            playerHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (released) {
                        return;
                    }
                    ImaAdsMediaSource.this.onLoadError(error);
                }
            });
        }

        @Override
        public void onAdClicked() {
            LLog.d(TAG, "AdsLoaderListener onAdClicked");
            if (eventHandler != null && eventListener != null) {
                eventHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!released) {
                            eventListener.onAdClicked();
                        }
                    }
                });
            }
        }

        @Override
        public void onAdTapped() {
            LLog.d(TAG, "AdsLoaderListener onAdTapped");
            if (eventHandler != null && eventListener != null) {
                LLog.d(TAG, "fuck if");
                eventHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!released) {
                            eventListener.onAdTapped();
                        }
                    }
                });
            }
        }

    }

}
