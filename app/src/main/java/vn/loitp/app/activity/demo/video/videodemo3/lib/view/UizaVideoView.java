package vn.loitp.app.activity.demo.video.videodemo3.lib.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.uiza.player.ext.ima.ImaAdsLoader;
import com.uiza.player.ext.ima.ImaAdsMediaSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.uiza.player.ui.views.DebugTextViewHelper;
import com.uiza.player.ui.views.PlaybackControlView;
import com.uiza.player.ui.views.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.UUID;

import vn.loitp.app.activity.demo.video.videodemo3.lib.helper.EventLogger;
import vn.loitp.app.activity.demo.video.videodemo3.lib.helper.InputModel;
import vn.loitp.app.activity.demo.video.videodemo3.lib.helper.TrackSelectionHelper;
import vn.loitp.app.app.LSApplication;
import vn.loitp.app.utilities.LLog;
import vn.uiza.app.R;
import vn.loitp.utils.util.ToastUtils;

/**
 * Created by www.muathu@gmail.com on 5/13/2017.
 */

public class UizaVideoView extends RelativeLayout implements View.OnClickListener, Player.EventListener, PlaybackControlView.VisibilityListener {
    private final String TAG = getClass().getSimpleName();

    public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
    public static final String ACTION_VIEW_LIST = "com.google.android.exoplayer.demo.action.VIEW_LIST";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Handler mainHandler;
    private EventLogger eventLogger;
    private SimpleExoPlayerView simpleExoPlayerView;
    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private TrackSelectionHelper trackSelectionHelper;
    private DebugTextViewHelper debugViewHelper;
    private boolean inErrorState;
    private TrackGroupArray lastSeenTrackGroupArray;

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;

    // Fields used only for ad playback. The ads loader is loaded via reflection.
    private Object imaAdsLoader; // ImaAdsLoader
    private Uri loadedAdTagUri;
    private ViewGroup adOverlayViewGroup;

    public UizaVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UizaVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.uiza_video_view, this);

        //this.ivIconBack = (ImageView) findViewById(R.id.iv_icon_back);
        shouldAutoPlay = true;
        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        View rootView = findViewById(R.id.root);
        rootView.setOnClickListener(this);
        debugRootView = (LinearLayout) findViewById(R.id.controls_root);
        debugTextView = (TextView) findViewById(R.id.debug_text_view);
        retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);

        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();
    }

    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    private InputModel inputModel;

    public void setInputModel(InputModel inputModel, boolean reloadData) {
        this.inputModel = inputModel;
        if (reloadData) {
            releasePlayer();
            shouldAutoPlay = true;
            clearResumePosition();

            initializePlayer();
        }
    }

    public void initializePlayer() {
        if (inputModel == null) {
            ToastUtils.showShort("You must init InputModel first");
            return;
        }

        //Intent intent = ((Activity) getContext()).getIntent();
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            trackSelectionHelper = new TrackSelectionHelper(trackSelector, adaptiveTrackSelectionFactory);
            lastSeenTrackGroupArray = null;
            eventLogger = new EventLogger(trackSelector);

            UUID drmSchemeUuid = inputModel.getDrmSchemeUuid();
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = inputModel.getDrmLicenseUrl();
                String[] keyRequestPropertiesArray = inputModel.getKeyRequestPropertiesArray();
                int errorStringId = R.string.error_drm_unknown;
                if (Util.SDK_INT < 18) {
                    errorStringId = R.string.error_drm_not_supported;
                } else {
                    try {
                        drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray);
                    } catch (UnsupportedDrmException e) {
                        errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                                ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                    }
                }
                if (drmSessionManager == null) {
                    showToast(errorStringId);
                    return;
                }
            }

            boolean preferExtensionDecoders = inputModel.getPreferExtensionDecoders();
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    ((LSApplication) ((Activity) getContext()).getApplication()).useExtensionRenderers()
                            ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext(), drmSessionManager, extensionRendererMode);

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);
            player.addListener(this);
            player.addListener(eventLogger);
            player.addMetadataOutput(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);

            simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
        }
        String action = inputModel.getAction();
        Uri[] uris;
        String[] extensions;
        if (ACTION_VIEW.equals(action)) {
            uris = new Uri[]{inputModel.getUri()};
            LLog.d("uris ", ">>>uris: " + LSApplication.getInstance().getGson().toJson(uris));
            extensions = new String[]{inputModel.getExtension()};
        } else if (ACTION_VIEW_LIST.equals(action)) {
            String[] uriStrings = inputModel.getUriStrings();
            uris = new Uri[uriStrings.length];
            for (int i = 0; i < uriStrings.length; i++) {
                uris[i] = Uri.parse(uriStrings[i]);
            }
            extensions = inputModel.getExtensionList();
            if (extensions == null) {
                extensions = new String[uriStrings.length];
            }
        } else {
            showToast(getContext().getString(R.string.unexpected_intent_action, action));
            return;
        }
        if (Util.maybeRequestReadExternalStoragePermission((Activity) getContext(), uris)) {
            // The player will be reinitialized if the permission is granted.
            return;
        }
        MediaSource[] mediaSources = new MediaSource[uris.length];
        for (int i = 0; i < uris.length; i++) {
            mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
        }
        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
        String adTagUriString = inputModel.getAdTagUri();
        if (adTagUriString != null) {
            Uri adTagUri = Uri.parse(adTagUriString);
            if (!adTagUri.equals(loadedAdTagUri)) {
                releaseAdsLoader();
                loadedAdTagUri = adTagUri;
            }
            try {
                mediaSource = createAdsMediaSource(mediaSource, Uri.parse(adTagUriString));
            } catch (Exception e) {
                showToast(R.string.ima_not_loaded);
            }
        } else {
            releaseAdsLoader();
        }
        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.seekTo(resumeWindow, resumePosition);
        }
        player.prepare(mediaSource, !haveResumePosition, false);
        inErrorState = false;
        updateButtonVisibilities();
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri) : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false), new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false), new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(), mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray) throws UnsupportedDrmException {
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, buildHttpDataSourceFactory(false));
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i], keyRequestPropertiesArray[i + 1]);
            }
        }
        return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    public void releasePlayer() {
        if (player != null) {
            debugViewHelper.stop();
            debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
            eventLogger = null;
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = Math.max(0, player.getContentPosition());
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((LSApplication) ((Activity) getContext()).getApplication()).buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return ((LSApplication) ((Activity) getContext()).getApplication()).buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns an ads media source, reusing the ads loader if one exists.
     *
     * @throws Exception Thrown if it was not possible to create an ads media source, for example, due
     *                   to a missing dependency.
     */
    private MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) throws Exception {
        // Load the extension source using reflection so the demo app doesn't have to depend on it.
        // The ads loader is reused for multiple playbacks, so that ad playback can resume.
        /*Class<?> loaderClass = Class.forName("ImaAdsLoader");
        if (imaAdsLoader == null) {
            imaAdsLoader = loaderClass.getConstructor(Context.class, Uri.class).newInstance(this, adTagUri);
            adOverlayViewGroup = new FrameLayout(getContext());
            // The demo app has a non-null overlay frame layout.
            simpleExoPlayerView.getOverlayFrameLayout().addView(adOverlayViewGroup);
        }
        Class<?> sourceClass = Class.forName("ImaAdsMediaSource");
        Constructor<?> constructor = sourceClass.getConstructor(MediaSource.class, DataSource.Factory.class, loaderClass, ViewGroup.class);
        return (MediaSource) constructor.newInstance(mediaSource, mediaDataSourceFactory, imaAdsLoader, adOverlayViewGroup);*/

        ImaAdsLoader imaAdsLoader = new ImaAdsLoader(getContext(), adTagUri);
        adOverlayViewGroup = new FrameLayout(getContext());
        simpleExoPlayerView.getOverlayFrameLayout().addView(adOverlayViewGroup);

        ImaAdsMediaSource imaAdsMediaSource = new ImaAdsMediaSource(mediaSource, mediaDataSourceFactory, imaAdsLoader, adOverlayViewGroup);
        return imaAdsMediaSource;
    }

    private void releaseAdsLoader() {
        if (imaAdsLoader != null) {
            try {
                Class<?> loaderClass = Class.forName("com.uiza.player.ext.ima.ImaAdsLoader");
                Method releaseMethod = loaderClass.getMethod("release");
                releaseMethod.invoke(imaAdsLoader);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            imaAdsLoader = null;
            loadedAdTagUri = null;
            simpleExoPlayerView.getOverlayFrameLayout().removeAllViews();
        }
    }

    // Player.EventListener implementation
    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            showControls();
        }
        updateButtonVisibilities();
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        // Do nothing.
    }

    @Override
    public void onPositionDiscontinuity() {
        if (inErrorState) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException = (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getContext().getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getContext().getString(R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                    } else {
                        errorString = getContext().getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getContext().getString(R.string.error_instantiating_decoder, decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        inErrorState = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initializePlayer();
        } else {
            updateResumePosition();
            updateButtonVisibilities();
            showControls();
        }
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        updateButtonVisibilities();
        if (trackGroups != lastSeenTrackGroupArray) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                        == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    showToast(R.string.error_unsupported_video);
                }
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                        == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    showToast(R.string.error_unsupported_audio);
                }
            }
            lastSeenTrackGroupArray = trackGroups;
        }
    }

    // User controls
    private void updateButtonVisibilities() {
        debugRootView.removeAllViews();

        retryButton.setVisibility(inErrorState ? View.VISIBLE : View.GONE);
        debugRootView.addView(retryButton);

        if (player == null) {
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(getContext());
                int label;
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.text;
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener(this);
                debugRootView.addView(button, debugRootView.getChildCount() - 1);
            }
        }
    }

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        ToastUtils.showShort(messageId);
    }

    private void showToast(String message) {
        ToastUtils.showShort(message);
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == retryButton) {
            initializePlayer();
        } else if (view.getParent() == debugRootView) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                trackSelectionHelper.showSelectionDialog((Activity) getContext(), ((Button) view).getText(), trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());
            }
        }
    }

    /*public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        ((Activity) getContext()).setIntent(intent);
    }*/

    public void onStart() {
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    public void onResume() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    public void onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    public void onDestroy() {
        releaseAdsLoader();
    }

    public SimpleExoPlayerView getPlayerView() {
        return simpleExoPlayerView;
    }
}