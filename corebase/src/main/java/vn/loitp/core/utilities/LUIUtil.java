package vn.loitp.core.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import loitp.core.R;
import vn.loitp.views.overscroll.lib.overscroll.IOverScrollDecor;
import vn.loitp.views.overscroll.lib.overscroll.IOverScrollUpdateListener;
import vn.loitp.views.overscroll.lib.overscroll.OverScrollDecoratorHelper;

/**
 * File created on 11/3/2016.
 *
 * @author loitp
 */
public class LUIUtil {
    private static String TAG = LUIUtil.class.getSimpleName();

    /*
      * settext marquee
      */
    public static void setMarquee(TextView tv, String text) {
        tv.setText(text);
        setMarquee(tv);
    }

    public static void setMarquee(TextView tv) {
        tv.setSelected(true);
        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv.setSingleLine(true);
        tv.setMarqueeRepeatLimit(-1);//no limit loop
    }

  /*public void setAnimation(View v) {
    Animation mAnimation;
    mAnimation = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
    v.startAnimation(mAnimation);
  }*/

    public static GradientDrawable createGradientDrawableWithRandomColor() {
        int color = LStoreUtil.getRandomColor();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(color);
        gradientDrawable.setCornerRadius(0f);
        gradientDrawable.setStroke(1, color);
        return gradientDrawable;
    }

    public static GradientDrawable createGradientDrawableWithColor(int colorMain, int colorStroke) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(colorMain);
        gradientDrawable.setCornerRadius(90f);
        gradientDrawable.setStroke(3, colorStroke);
        return gradientDrawable;
    }

    /*@SuppressWarnings("deprecation")
    public static void setCircleViewWithColor(View arr[]) {
        try {
            for (View view : arr) {
                view.setBackgroundDrawable(createGradientDrawableWithRandomColor());
            }
        } catch (Exception e) {
            LLog.d(TAG, "setCircleViewWithColor setBkgColor: " + e.toString());
        }
    }*/

    @SuppressWarnings("deprecation")
    public static void setCircleViewWithColor(View view, int colorMain, int colorStroke) {
        try {
            view.setBackgroundDrawable(createGradientDrawableWithColor(colorMain, colorStroke));
        } catch (Exception e) {
            LLog.d(TAG, "setCircleViewWithColor setBkgColor: " + e.toString());
        }
    }

    /*
      get screenshot
       */
    /*public Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        Drawable backgroundDrawable = view.getBackground();
        Canvas canvas = new Canvas(bitmap);
        if (backgroundDrawable != null) {
            backgroundDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return bitmap;
    }*/

    public static void setGradientBackground(View v) {
        final View view = v;
        Drawable[] layers = new Drawable[1];

        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                LinearGradient lg = new LinearGradient(0, 0, 0, view.getHeight(), new int[]{LStoreUtil.getRandomColor(), LStoreUtil.getRandomColor(), LStoreUtil.getRandomColor(), LStoreUtil
                        .getRandomColor()}, new float[]{0, 0.49f, 0.50f, 1}, Shader.TileMode.CLAMP);
                return lg;
            }
        };
        PaintDrawable p = new PaintDrawable();
        p.setShape(new RectShape());
        p.setShaderFactory(sf);
        p.setCornerRadii(new float[]{5, 5, 5, 5, 0, 0, 0, 0});
        layers[0] = (Drawable) p;
        LayerDrawable composite = new LayerDrawable(layers);
        view.setBackgroundDrawable(composite);
    }

    public static void setTextFromHTML(TextView textView, String bodyData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(bodyData, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(bodyData));
        }
    }

    public static void setImageFromAsset(Context context, String fileName, ImageView imageView) {
        {
            Drawable drawable = null;
            InputStream stream = null;
            try {
                stream = context.getAssets().open("img/" + fileName);
                drawable = Drawable.createFromStream(stream, null);
                if (drawable != null) {
                    imageView.setImageDrawable(drawable);
                }
            } catch (Exception ignored) {
                LLog.d(TAG, "setImageFromAsset: " + ignored.toString());
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception ignored) {
                    LLog.d(TAG, "setImageFromAsset: " + ignored.toString());
                }
            }
        }
    }

    public static void fixSizeTabLayout(Context context, TabLayout tabLayout, String titleList[]) {
        if (titleList.length > 3) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        } else {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        //settext allcap = false
        /*for (int tabIndex = 0; tabIndex < tabLayout.getTabCount(); tabIndex++) {
            TextView tabTextView = (TextView) (((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(tabIndex)).getChildAt(1));
            tabTextView.setAllCaps(false);
            setTextAppearance(context, tabTextView, android.R.style.TextAppearance_Medium);
        }*/
    }

    public static void setTextAppearance(Context context, TextView textView, int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            textView.setTextAppearance(context, resId);
        } else {
            textView.setTextAppearance(resId);
        }
    }

    public interface DelayCallback {
        public void doAfter(int mls);
    }

    public static void setDelay(final int mls, final DelayCallback delayCallback) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (delayCallback != null) {
                    delayCallback.doAfter(mls);
                }
            }
        }, mls);
    }

    public interface CheckScreenIsLiveInTimeCallback {
        public void doAfter(int mls, boolean isActivityNull);
    }

    public static void checkScreenIsLiveInTime(final Activity activity, final int mls, final CheckScreenIsLiveInTimeCallback checkScreenIsLiveInTimeCallback) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkScreenIsLiveInTimeCallback != null) {
                    checkScreenIsLiveInTimeCallback.doAfter(mls, activity == null);
                }
            }
        }, mls);
    }

    public static void setSoftInputMode(Activity activity, int mode) {
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        activity.getWindow().setSoftInputMode(mode);
    }

    public static void setLastCursorEditText(EditText editText) {
        if (editText == null) {
            return;
        }
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    public static void removeUnderlineOfSearchView(SearchView searchView) {
        View v = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        v.setBackgroundColor(Color.TRANSPARENT);
    }

    public static SearchView customizeWhiteSearchView(SearchView searchView, Context context, String hintText) {
        View v = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        v.setBackgroundColor(Color.TRANSPARENT);

        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(ContextCompat.getColor(context, R.color.LightGrey));

        //ImageView imgViewSearchView = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        //imgViewSearchView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.search_color));

        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        if (hintText != null) {
            ssb.append(hintText);
        } else {
            ssb.append("Mínim 3 caràcters...");
        }

        Drawable searchIcon = ContextCompat.getDrawable(context, R.drawable.search_color);
        int textSize = (int) (searchEditText.getTextSize() * 1.05);
        searchIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchEditText.setHint(ssb);

        ImageView close = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        close.setImageResource(R.drawable.delete_border);

        return searchView;
    }

    public static void setColorForSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout == null) {
            return;
        }
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.vip1,
                R.color.vip2,
                R.color.vip3,
                R.color.vip4,
                R.color.vip5);
    }

    public static void setTextShadow(TextView textView) {
        if (textView == null) {
            return;
        }
        textView.setShadowLayer(
                1f, // radius
                1f, // dx
                1f, // dy
                Color.BLACK // shadow color
        );
    }

    public static void setTextBold(TextView textBold) {
        textBold.setTypeface(null, Typeface.BOLD);
    }

    public static void printBeautyJson(Object o, TextView textView) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(o);
        textView.setText("Debug view, will be removed later\n" + json);
    }

    public static void setPullLikeIOSVertical(RecyclerView recyclerView) {
        //guide: https://github.com/EverythingMe/overscroll-decor

        // Horizontal
        //OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);

        // Vertical
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    public static void setPullLikeIOSHorizontal(RecyclerView recyclerView) {
        //guide: https://github.com/EverythingMe/overscroll-decor

        // Horizontal
        OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
    }

    public static void setPullLikeIOSHorizontal(final ViewPager viewPager, final Callback callback) {
        //guide: https://github.com/EverythingMe/overscroll-decor
        IOverScrollDecor decor = OverScrollDecoratorHelper.setUpOverScroll(viewPager);
        if (callback != null) {
            decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
                @Override
                public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                    final View view = decor.getView();
                    if (offset > 0) {
                        // 'view' is currently being over-scrolled from the top.
                        lastOffset = offset;
                        isUp = true;
                        //LLog.d(TAG, "________________>0 " + lastOffset + " " + isUp);
                    } else if (offset < 0) {
                        // 'view' is currently being over-scrolled from the bottom.
                        lastOffset = offset;
                        isUp = false;
                        //LLog.d(TAG, "________________<0 " + lastOffset + " " + isUp);
                    } else {
                        // No over-scroll is in-effect.
                        // This is synonymous with having (state == STATE_IDLE).
                        //LLog.d(TAG, "________________STATE_IDLE" + lastOffset + " " + isUp);
                        if (isUp) {
                            //LLog.d(TAG, "________________ up " + lastOffset);
                            if (lastOffset > 1.5f) {
                                callback.onUpOrLeftRefresh(lastOffset);
                                LSoundUtil.startMusicFromAsset(viewPager.getContext(), "ting.ogg");
                            } else {
                                callback.onUpOrLeft(lastOffset);
                            }
                        } else {
                            //LLog.d(TAG, "________________ down " + lastOffset);
                            if (lastOffset < -1.5f) {
                                callback.onDownOrRightRefresh(lastOffset);
                            } else {
                                callback.onDownOrRight(lastOffset);
                            }
                        }
                        lastOffset = 0;
                        isUp = false;
                    }
                }
            });
        }
    }

    private static float lastOffset = 0.0f;
    private static boolean isUp = false;

    public static void setPullLikeIOSVertical(final RecyclerView recyclerView, final Callback callback) {
        IOverScrollDecor decor = OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        if (callback != null) {
            /*decor.setOverScrollStateListener(new IOverScrollStateListener() {
                @Override
                public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
                    switch (newState) {
                        case STATE_IDLE:
                            // No over-scroll is in effect.
                            callback.onIdle();
                            break;
                        case STATE_DRAG_START_SIDE:
                            // Dragging started at the left-end.
                            callback.onDragStarSide();
                            break;
                        case STATE_DRAG_END_SIDE:
                            // Dragging started at the right-end.
                            callback.onDragEndSide();
                            break;
                        case STATE_BOUNCE_BACK:
                            if (oldState == STATE_DRAG_START_SIDE) {
                                // Dragging stopped -- view is starting to bounce back from the *left-end* onto natural position.
                            } else { // i.e. (oldState == STATE_DRAG_END_SIDE)
                                // View is starting to bounce back from the *right-end*.
                            }
                            break;
                    }
                }
            });*/
            decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
                @Override
                public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                    final View view = decor.getView();
                    if (offset > 0) {
                        // 'view' is currently being over-scrolled from the top.
                        lastOffset = offset;
                        isUp = true;
                        //LLog.d(TAG, "________________>0 " + lastOffset + " " + isUp);
                    } else if (offset < 0) {
                        // 'view' is currently being over-scrolled from the bottom.
                        lastOffset = offset;
                        isUp = false;
                        //LLog.d(TAG, "________________<0 " + lastOffset + " " + isUp);
                    } else {
                        // No over-scroll is in-effect.
                        // This is synonymous with having (state == STATE_IDLE).
                        //LLog.d(TAG, "________________STATE_IDLE" + lastOffset + " " + isUp);
                        if (isUp) {
                            //LLog.d(TAG, "________________ up " + lastOffset);
                            if (lastOffset > 1.5f) {
                                callback.onUpOrLeftRefresh(lastOffset);
                                LSoundUtil.startMusicFromAsset(recyclerView.getContext(), "ting.ogg");
                            } else {
                                callback.onUpOrLeft(lastOffset);
                            }
                        } else {
                            //LLog.d(TAG, "________________ down " + lastOffset);
                            if (lastOffset < -1.5f) {
                                callback.onDownOrRightRefresh(lastOffset);
                            } else {
                                callback.onDownOrRight(lastOffset);
                            }
                        }
                        lastOffset = 0;
                        isUp = false;
                    }
                }
            });
        }
    }

    public interface Callback {
        public void onUpOrLeft(float offset);

        public void onUpOrLeftRefresh(float offset);

        public void onDownOrRight(float offset);

        public void onDownOrRightRefresh(float offset);
    }

    public static void setPullLikeIOSVertical(ViewPager viewPager) {
        //guide: https://github.com/EverythingMe/overscroll-decor
        OverScrollDecoratorHelper.setUpOverScroll(viewPager);
    }

    public static void setPullLikeIOSVertical(ScrollView scrollView) {
        //guide: https://github.com/EverythingMe/overscroll-decor
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
    }

    public static void setPullLikeIOSVertical(ListView listView) {
        //guide: https://github.com/EverythingMe/overscroll-decor
        OverScrollDecoratorHelper.setUpOverScroll(listView);
    }

    public static void setPullLikeIOSVertical(HorizontalScrollView scrollView) {
        //guide: https://github.com/EverythingMe/overscroll-decor
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
    }

    public static void setPullLikeIOSVertical(View view) {
        //guide: https://github.com/EverythingMe/overscroll-decor

        // Horizontal
        //OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);

        // Vertical
        OverScrollDecoratorHelper.setUpStaticOverScroll(view, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    public interface CallbackSearch {
        public void onSearch();
    }

    public static void setImeiActionSearch(EditText editText, final CallbackSearch callbackSearch) {
        if (editText == null) {
            return;
        }
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (callbackSearch != null) {
                        callbackSearch.onSearch();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public static void setTextViewUnderLine(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public static void setMarginsInDp(View view, int leftDp, int topDp, int rightDp, int bottomDp) {
        int marginLeftInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDp, view.getContext().getResources().getDisplayMetrics());
        int marginTopInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topDp, view.getContext().getResources().getDisplayMetrics());
        int marginRightInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDp, view.getContext().getResources().getDisplayMetrics());
        int marginBottomInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomDp, view.getContext().getResources().getDisplayMetrics());
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(marginLeftInDp, marginTopInDp, marginRightInDp, marginBottomInDp);
            view.requestLayout();
            LLog.d(TAG, "setMarginsInDp success with: " + marginLeftInDp + " - " + marginTopInDp + " - " + marginRightInDp + " - " + marginBottomInDp);
        }
    }

    public static void setMarginsInPixel(View view, int leftPx, int topPx, int rightPx, int bottomPx) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(leftPx, topPx, rightPx, bottomPx);
            view.requestLayout();
            LLog.d(TAG, "setMarginsInPixel success with: " + leftPx + " - " + topPx + " - " + rightPx + " - " + bottomPx);
        }
    }

    public static void resizeView(View view, int newWidth, int newHeight) {
        try {
            Constructor<? extends ViewGroup.LayoutParams> ctor = view.getLayoutParams().getClass().getDeclaredConstructor(int.class, int.class);
            view.setLayoutParams(ctor.newInstance(newWidth, newHeight));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}