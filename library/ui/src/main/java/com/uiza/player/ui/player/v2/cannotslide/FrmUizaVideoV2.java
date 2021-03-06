package com.uiza.player.ui.player.v2.cannotslide;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
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
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.uiza.player.ext.ima.ImaAdsLoader;
import com.uiza.player.ext.ima.ImaAdsMediaSource;
import com.uiza.player.ui.data.UizaData;
import com.uiza.player.ui.player.FrmBaseUiza;
import com.uiza.player.ui.player.v2.WrapperCallback;
import com.uiza.player.ui.util.UizaAnimationUtil;
import com.uiza.player.ui.util.UizaImageUtil;
import com.uiza.player.ui.util.UizaTrackingUtil;
import com.uiza.player.ui.util.UizaUIUtil;
import com.uiza.player.ui.views.DebugTextViewHelper;
import com.uiza.player.ui.views.PlaybackControlView;
import com.uiza.player.ui.views.SimpleExoPlayerView;
import com.uiza.player.ui.views.helper.EventLogger;
import com.uiza.player.ui.views.helper.InputModel;
import com.uiza.player.ui.views.helper.TrackSelectionHelper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.UUID;

import io.uiza.sdk.ui.BuildConfig;
import io.uiza.sdk.ui.R;
import vn.loitp.core.common.Constants;
import vn.loitp.core.utilities.LDialogUtil;
import vn.loitp.core.utilities.LLog;
import vn.loitp.restapi.uiza.model.v2.getplayerinfo.PlayerConfig;
import vn.loitp.restapi.uiza.model.v2.listallentity.Item;
import vn.loitp.views.LToast;
import vn.loitp.views.progressloadingview.avloadingindicatorview.lib.avi.AVLoadingIndicatorView;
import vn.loitp.views.realtimeblurview.RealtimeBlurView;

/**
 * Created by www.muathu@gmail.com on 7/26/2017.
 */
public class FrmUizaVideoV2 extends FrmBaseUiza implements View.OnClickListener, Player.EventListener, PlaybackControlView.VisibilityListener, ImaAdsMediaSource.AdsListener {
    private final String TAG = getClass().getSimpleName();
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

    private int resumeWindow;
    private long resumePosition;

    // Fields used only for ad playback. The ads loader is loaded via reflection.
    private Object imaAdsLoader; // ImaAdsLoader
    private Uri loadedAdTagUri;
    private ViewGroup adOverlayViewGroup;
    private FrameLayout rootView;
    private boolean isVideoStarted;//detect video is has ready started or not

    private InputModel inputModel;
    private PlayerConfig mPlayerConfig;
    private String userAgent = Constants.USER_AGENT;

    private int positionOfLinkPlayList = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiza_video_player_frm, container, false);
        clearResumePosition();
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        view.findViewById(R.id.ll_debug_view).setVisibility(Constants.IS_DEBUG ? View.VISIBLE : View.INVISIBLE);

        flRootView = (FrameLayout) view.findViewById(R.id.fl_root_view);
        rootView = (FrameLayout) view.findViewById(R.id.root_view);
        rootView.setOnClickListener(this);

        debugRootView = (LinearLayout) view.findViewById(R.id.controls_root);
        debugTextView = (TextView) view.findViewById(R.id.debug_text_view);
        retryButton = (Button) view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);

        simpleExoPlayerView = (SimpleExoPlayerView) view.findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();

        //simpleExoPlayerView.setUseArtwork(true);
        //Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic);
        //simpleExoPlayerView.setDefaultArtwork(bm);

        simpleExoPlayerView.setCallback(new SimpleExoPlayerView.Callback() {
            @Override
            public void onClickSetting() {
                //LLog.d(TAG, "onClickSetting");
                if (getBtVideo() != null) {
                    getBtVideo().performClick();
                }
                if (wrapperCallback != null) {
                    wrapperCallback.onPlaybackControllerClickSetting();
                }
            }

            @Override
            public void onPlayThrough(int percent) {
                //will be called if player play at 25%, 50%, 75%, 100% duration.
                //track play_through
                trackUiza(UizaTrackingUtil.createTrackingInput(getActivity(), String.valueOf(percent), UizaTrackingUtil.EVENT_TYPE_PLAY_THROUGHT));
                if (wrapperCallback != null) {
                    wrapperCallback.onTrackPlayThrough(percent);
                }
            }

            @Override
            public void onClickItem(Item item, int position) {
                //click item on playlist
                LLog.d(TAG, "onClick " + position);
                if (simpleExoPlayerView != null) {
                    if(UizaData.getInstance().isLandscape()){
                        simpleExoPlayerView.setFullScreen();
                    }
                }
                if (getActivity() instanceof UizaPlayerActivityV2) {
                    ((UizaPlayerActivityV2) getActivity()).playOnClickItem(item, position);
                }
                if (wrapperCallback != null) {
                    wrapperCallback.onClickItemPlayList(item, position);
                }
            }
        });
        return view;
    }

    //return button video in debug layout
    private View getBtVideo() {
        for (int i = 0; i < debugRootView.getChildCount(); i++) {
            View childView = debugRootView.getChildAt(i);
            if (childView instanceof Button) {
                if (((Button) childView).getText().toString().equalsIgnoreCase(getString(R.string.video))) {
                    return childView;
                }
            }
        }
        return null;
    }

    //return button audio in debug layout
    private View getBtAudio() {
        for (int i = 0; i < debugRootView.getChildCount(); i++) {
            View childView = debugRootView.getChildAt(i);
            if (childView instanceof Button) {
                if (((Button) childView).getText().toString().equalsIgnoreCase(getString(R.string.audio))) {
                    return childView;
                }
            }
        }
        return null;
    }

    //return button text in debug layout
    private View getBtText() {
        for (int i = 0; i < debugRootView.getChildCount(); i++) {
            View childView = debugRootView.getChildAt(i);
            if (childView instanceof Button) {
                if (((Button) childView).getText().toString().equalsIgnoreCase(getString(R.string.text))) {
                    return childView;
                }
            }
        }
        return null;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        LLog.d(TAG, "onVisibilityChange " + visibility);
        debugRootView.setVisibility(visibility);
        //dismiss dialog choose setting
        if (visibility != View.VISIBLE) {
            if (trackSelectionHelper != null) {
                trackSelectionHelper.dissmissDialog();
            }
        }
        if (wrapperCallback != null) {
            wrapperCallback.onVisibilityChange(visibility);
        }
    }

    public void setInputModel(InputModel ip, boolean reloadData) {
        LLog.d(TAG, "setInputModel reloadData: " + reloadData);
        if (ip == null) {
            this.inputModel = UizaData.getInstance().getInputModel();
        } else {
            this.inputModel = ip;
        }
        if (reloadData) {
            releasePlayer();
            clearResumePosition();
            initializePlayer();
        }
    }

    private void initializePlayer() {
        if (inputModel == null) {
            inputModel = UizaData.getInstance().getInputModel();
        }

        setCoverVideo();

        if (inputModel.isNoLinkPlay()) {
            LLog.d(TAG, "inputModel.isNoLinkPlay -> return");
            showDialogOne(getString(R.string.has_no_linkplay));
            if (wrapperCallback != null) {
                wrapperCallback.onErrorNoLinkPlay();
            }
            return;
        }
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
                        errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                    }
                }
                if (drmSessionManager == null) {
                    LToast.show(getActivity(), errorStringId);
                    return;
                }
            }
            boolean preferExtensionDecoders = inputModel.getPreferExtensionDecoders();
            /*@DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    ((LSApplication) ((Activity) getContext()).getApplication()).useExtensionRenderers()
                            ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;*/
            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers()
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
            player.setRepeatMode(Player.REPEAT_MODE_OFF);
            //player.setPlayWhenReady(false);

            //simpleExoPlayerView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL);
            simpleExoPlayerView.setPlayer(player);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
        }
        String action = inputModel.getAction();
        Uri[] uris;
        String[] extensions;
        if (Constants.ACTION_VIEW.equals(action)) {
            uris = new Uri[]{inputModel.getUri(positionOfLinkPlayList)};
            LLog.d(TAG, "________________________initializePlayer positionOfLinkPlayList: " + positionOfLinkPlayList);
            LLog.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>initializePlayer uris:" + gson.toJson(uris));
            if (wrapperCallback != null) {
                wrapperCallback.initializePlayer(uris);
            }
            extensions = new String[]{inputModel.getExtension()};
        } else if (Constants.ACTION_VIEW_LIST.equals(action)) {
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
            LToast.show(getActivity(), getContext().getString(R.string.unexpected_intent_action, action));
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
        LLog.d(TAG, "adTagUriString " + adTagUriString);
        if (adTagUriString != null) {
            Uri adTagUri = Uri.parse(adTagUriString);
            if (!adTagUri.equals(loadedAdTagUri)) {
                releaseAdsLoader();
                loadedAdTagUri = adTagUri;
            }
            try {
                mediaSource = createAdsMediaSource(mediaSource, Uri.parse(adTagUriString));
            } catch (Exception e) {
                LToast.show(getActivity(), R.string.ima_not_loaded);
            }
        } else {
            releaseAdsLoader();
        }
        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.seekTo(resumeWindow, resumePosition);
        }

        //TODO freuss47 with subtitle vtt
        /*Format textFormat = Format.createTextSampleFormat(null, MimeTypes.TEXT_VTT, Format.NO_VALUE, "en", null);
        String urlSubtitle = "http://dev-static.uiza.io/subtitle_56a4f990-17e6-473c-8434-ef6c7e40bba1_vi_1522812445904.vtt";
        MediaSource textMediaSource = new SingleSampleMediaSource(Uri.parse(urlSubtitle)
                , mediaDataSourceFactory
                , textFormat
                , C.TIME_UNSET);
        MediaSource mediaSourceWithText = new MergingMediaSource(mediaSource, textMediaSource);
        player.prepare(mediaSourceWithText, !haveResumePosition, false);*/

        player.prepare(mediaSource, !haveResumePosition, false);
        inErrorState = false;
        updateDebugButtonVisibilities();

        mPlayerConfig = UizaData.getInstance().getPlayerConfig();
        UizaUIUtil.setConfigUIPlayer(simpleExoPlayerView, inputModel, mPlayerConfig);
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
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
            eventLogger = null;
            if (wrapperCallback != null) {
                wrapperCallback.onReleasePlayer();
            }
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
        //return ((LSApplication) ((Activity) getContext()).getApplication()).buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(getContext(), bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    private boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        //return ((LSApplication) ((Activity) getContext()).getApplication()).buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
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

        /*Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");
        if (imaAdsLoader == null) {
            imaAdsLoader = loaderClass.getConstructor(Context.class, Uri.class).newInstance(this, adTagUri);
            adOverlayViewGroup = new FrameLayout(getContext());
            // The demo app has a non-null overlay frame layout.
            simpleExoPlayerView.getOverlayFrameLayout().addView(adOverlayViewGroup);
        }
        Class<?> sourceClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource");
        Constructor<?> constructor = sourceClass.getConstructor(MediaSource.class, DataSource.Factory.class, loaderClass, ViewGroup.class);
        return (MediaSource) constructor.newInstance(mediaSource, mediaDataSourceFactory, imaAdsLoader, adOverlayViewGroup);*/

        ImaAdsLoader imaAdsLoader = new ImaAdsLoader(getContext(), adTagUri);
        adOverlayViewGroup = new FrameLayout(getContext());
        simpleExoPlayerView.getOverlayFrameLayout().addView(adOverlayViewGroup);
        //ImaAdsMediaSource imaAdsMediaSource = new ImaAdsMediaSource(mediaSource, mediaDataSourceFactory, imaAdsLoader, adOverlayViewGroup);
        ImaAdsMediaSource imaAdsMediaSource = new ImaAdsMediaSource(mediaSource, mediaDataSourceFactory, imaAdsLoader, adOverlayViewGroup, new Handler(), this);
        return imaAdsMediaSource;
    }

    private void releaseAdsLoader() {
        if (imaAdsLoader != null) {
            LLog.d(TAG, "releaseAdsLoader");
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
        //LLog.d(TAG, "onLoadingChanged " + isLoading);
        if (wrapperCallback != null) {
            wrapperCallback.onLoadingChanged(isLoading);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            LLog.d(TAG, "onPlayerStateChanged STATE_ENDED");
            showDebugControls();
        } else if (playbackState == Player.STATE_BUFFERING) {
            LLog.d(TAG, "onPlayerStateChanged STATE_BUFFERING");
        } else if (playbackState == Player.STATE_IDLE) {
            LLog.d(TAG, "onPlayerStateChanged STATE_IDLE");
        } else if (playbackState == Player.STATE_READY) {
            LLog.d(TAG, "onPlayerStateChanged STATE_READY");
            //only track video_starts once time
            if (!isVideoStarted) {
                isVideoStarted = true;
                //track plays_requested
                trackUiza(UizaTrackingUtil.createTrackingInput(getActivity(), UizaTrackingUtil.EVENT_TYPE_VIDEO_STARTS));

                if (wrapperCallback != null) {
                    wrapperCallback.onTrackVideoStart();
                }

                LLog.d(TAG, "onPlayerStateChanged STATE_READY removeCoverVideo");
                removeCoverVideo();

                //track event view (after video is played 5s)
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //LLog.d(TAG, "Video is played about 5000mls");
                        trackUiza(UizaTrackingUtil.createTrackingInput(getActivity(), UizaTrackingUtil.EVENT_TYPE_VIEW));
                        if (wrapperCallback != null) {
                            wrapperCallback.onTrackVideoView();
                        }
                    }
                };
                mHandler.postDelayed(mRunnable, 5000);
            }
        }
        updateDebugButtonVisibilities();
        if (wrapperCallback != null) {
            wrapperCallback.onPlayerStateChanged(playWhenReady, playbackState);
        }
    }

    //for track event view
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        LLog.d(TAG, "onRepeatModeChanged " + repeatMode);
        if (wrapperCallback != null) {
            wrapperCallback.onRepeatModeChanged(repeatMode);
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        if (inErrorState) {
            LLog.d(TAG, "onPositionDiscontinuity inErrorState");
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        LLog.d(TAG, "onPlaybackParametersChanged");
        if (wrapperCallback != null) {
            wrapperCallback.onPlaybackParametersChanged(playbackParameters);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        LLog.d(TAG, "onTimelineChanged");
        if (wrapperCallback != null) {
            wrapperCallback.onTimelineChanged(timeline, manifest);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        LLog.d(TAG, "onPlayerError " + e.toString());
        LLog.d(TAG, "onPlayerError positionOfLinkPlayList: " + positionOfLinkPlayList);
        if (positionOfLinkPlayList >= inputModel.getListLinkPlay().size() - 1) {
            if (wrapperCallback != null) {
                wrapperCallback.onErrorCannotPlayAnyLinkPlay();
            }
            showDialogError(getString(R.string.cannot_play_any_videos), new LDialogUtil.CallbackShowOne() {
                @Override
                public void onClick() {
                    getActivity().onBackPressed();
                }
            });
            return;
        } else {
            positionOfLinkPlayList++;
            LLog.d(TAG, "Try initializePlayer again with next link play - positionOfLinkPlayList: " + positionOfLinkPlayList);
            setInputModel(inputModel, true);
            //return;
        }

        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            LLog.d(TAG, "onPlayerError TYPE_RENDERER");
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
            LToast.show(getActivity(), errorString);
        }
        inErrorState = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initializePlayer();
        } else {
            updateResumePosition();
            updateDebugButtonVisibilities();
            showDebugControls();
        }
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        updateDebugButtonVisibilities();
        if (trackGroups != lastSeenTrackGroupArray) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    LToast.show(getActivity(), R.string.error_unsupported_video);
                }
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    LToast.show(getActivity(), R.string.error_unsupported_audio);
                }
            }
            lastSeenTrackGroupArray = trackGroups;
        }
        if (wrapperCallback != null) {
            wrapperCallback.onTracksChanged(trackGroups, trackSelections);
        }
    }

    // User controls
    private void updateDebugButtonVisibilities() {
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
                Button button = new Button(getActivity());
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
                //LLog.d(TAG, "updateButtonVisibilities addView " + button.getText().toString() + ", tag: " + button.getTag().toString());
            }
        }
    }

    private void showDebugControls() {
        debugRootView.setVisibility(View.VISIBLE);
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
            LLog.d(TAG, "onClick " + ((Button) view).getText());
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                trackSelectionHelper.showSelectionDialog((Activity) getContext(), ((Button) view).getText(), mappedTrackInfo, (int) view.getTag(), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (simpleExoPlayerView != null) {
                            simpleExoPlayerView.resumePlayVideo();
                        }
                    }
                });
            }
        }
    }

    /*public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        ((Activity) getContext()).setIntent(intent);
    }*/

    /*public void onStartUizaVideo() {
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }*/

    @Override
    public void onStart() {
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
        super.onStart();
    }

    /*public void onResumeUizaVideo() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }*/

    @Override
    public void onResume() {
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }

        if (!UizaData.getInstance().getInputModel().getEntityID().equals(inputModel.getEntityID())) {
            UizaData.getInstance().setInputModel(inputModel);
        }
        super.onResume();
    }

    /*public void onPauseUizaVideo() {
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            UizaData.getInstance().setCurrentPosition(player.getCurrentPosition());
            player.setPlayWhenReady(false);
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    /*public void onStopUizaVideo() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }*/

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    /*public void onDestroyUizaVideo() {
        releaseAdsLoader();
    }*/

    @Override
    public void onDestroy() {
        //remove callback track event view
        mHandler.removeCallbacks(mRunnable);

        releaseAdsLoader();
        super.onDestroy();
    }

    public SimpleExoPlayerView getPlayerView() {
        return simpleExoPlayerView;
    }

    public void seekTo(final long position) {
        if (player != null) {
            //LLog.d(TAG, "player != null");
            player.seekTo(position);
        } else {
            //LLog.d(TAG, "player == null");
            UizaUIUtil.setDelay(200, new UizaUIUtil.DelayCallback() {
                @Override
                public void doAfter(int mls) {
                    seekTo(position);
                }
            });
        }
    }

    @Override
    public void onAdLoadError(IOException error) {
        LLog.e(TAG, "onAdLoadError");
        if (wrapperCallback != null) {
            wrapperCallback.onAdLoadError(error);
        }
    }

    @Override
    public void onAdClicked() {
        LLog.d(TAG, "onAdClicked");
        if (wrapperCallback != null) {
            wrapperCallback.onAdClicked();
        }
    }

    @Override
    public void onAdTapped() {
        LLog.d(TAG, "onAdTapped");
        if (wrapperCallback != null) {
            wrapperCallback.onAdTapped();
        }
    }

    private void logSize(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                LLog.d(TAG, "updateSize logSize " + view.getClass().getSimpleName() + " -> " + view.getWidth() + " x " + view.getHeight());
            }
        });
    }

    private WrapperCallback wrapperCallback;

    public void setWrapperCallback(WrapperCallback wrapperCallback) {
        this.wrapperCallback = wrapperCallback;
    }

    private FrameLayout flRootView;
    private ImageView ivCoverVideo;
    private ImageView ivCoverLogo;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private RealtimeBlurView realtimeBlurView;
    private final int RADIUS_BLUR_VIEW = 15;

    private void setCoverVideo() {
        if (isRemovedCoverVideo) {
            return;
        }
        if (flRootView != null && inputModel != null) {
            LLog.d(TAG, "setCoverVideo " + inputModel.getUrlImg());
            if (ivCoverVideo != null || ivCoverLogo != null || avLoadingIndicatorView != null || realtimeBlurView != null) {
                return;
            }
            ivCoverVideo = new ImageView(getActivity());
            ivCoverVideo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ivCoverVideo.setLayoutParams(layoutParams);
            UizaImageUtil.load(getActivity(), inputModel.getUrlImg(), ivCoverVideo);
            flRootView.addView(ivCoverVideo);

            realtimeBlurView = new RealtimeBlurView(getActivity(), RADIUS_BLUR_VIEW, ContextCompat.getColor(getActivity(), R.color.black_35));
            FrameLayout.LayoutParams layoutParamsBlur = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            realtimeBlurView.setLayoutParams(layoutParamsBlur);
            flRootView.addView(realtimeBlurView);

            //realtimeBlurView.setVisibility(View.GONE);

            ivCoverLogo = new ImageView(getActivity());
            ivCoverLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams layoutParamsIvLogo = new FrameLayout.LayoutParams(150, 150);
            ivCoverLogo.setLayoutParams(layoutParamsIvLogo);
            ivCoverLogo.setImageResource(R.drawable.uiza_logo_512);
            layoutParamsIvLogo.gravity = Gravity.CENTER;
            flRootView.addView(ivCoverLogo);

            avLoadingIndicatorView = new AVLoadingIndicatorView(getActivity());
            avLoadingIndicatorView.setIndicatorColor(Color.WHITE);
            avLoadingIndicatorView.setIndicator("BallSpinFadeLoaderIndicator");
            FrameLayout.LayoutParams aviLayout = new FrameLayout.LayoutParams(75, 75);
            aviLayout.setMargins(0, 0, 0, 100);
            aviLayout.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            avLoadingIndicatorView.setLayoutParams(aviLayout);
            flRootView.addView(avLoadingIndicatorView);
            LLog.d(TAG, "setCoverVideo done");
        }
    }

    private boolean isRemovedCoverVideo;

    private void removeCoverVideo() {
        if (flRootView != null && ivCoverVideo != null && ivCoverLogo != null && avLoadingIndicatorView != null && realtimeBlurView != null) {
            UizaAnimationUtil.playFadeOut(getActivity(), realtimeBlurView, null);

            avLoadingIndicatorView.smoothToHide();
            ivCoverVideo.setVisibility(View.GONE);
            flRootView.removeView(ivCoverVideo);
            ivCoverVideo = null;

            ivCoverLogo.setVisibility(View.GONE);
            flRootView.removeView(ivCoverLogo);
            ivCoverLogo = null;
            avLoadingIndicatorView = null;

            flRootView.removeView(realtimeBlurView);
            realtimeBlurView = null;

            isRemovedCoverVideo = true;
            LLog.d(TAG, "removeCoverVideo success");
        }
    }
}