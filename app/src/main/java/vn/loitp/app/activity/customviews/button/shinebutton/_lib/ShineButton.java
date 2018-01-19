package vn.loitp.app.activity.customviews.button.shinebutton._lib;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;

import vn.loitp.app.utilities.LLog;
import vn.uiza.app.R;

/**
 * @author Chad
 * @title com.sackcentury.shinebuttonlib
 * @description
 * @modifier
 * @date
 * @since 16/7/5 下午2:27
 **/
public class ShineButton extends PorterShapeImageView {
    private static final String TAG = ShineButton.class.getSimpleName();
    //private boolean isChecked = false;

    private int btnColor;
    private int btnFillColor;

    int DEFAULT_WIDTH = 50;
    int DEFAULT_HEIGHT = 50;

    DisplayMetrics metrics = new DisplayMetrics();

    Activity activity;
    ShineView shineView;
    ValueAnimator shakeAnimator;
    ShineView.ShineParams shineParams = new ShineView.ShineParams();

    //OnCheckedChangeListener listener;

    private int bottomHeight;
    private int realBottomHeight;

    public ShineButton(Context context) {
        super(context);
        if (context instanceof Activity) {
            init((Activity) context);
        }
    }

    public ShineButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initButton(context, attrs);
    }


    public ShineButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initButton(context, attrs);
    }

    private void initButton(Context context, AttributeSet attrs) {

        if (context instanceof Activity) {
            init((Activity) context);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShineButton);
        btnColor = a.getColor(R.styleable.ShineButton_btn_color, Color.GRAY);
        btnFillColor = a.getColor(R.styleable.ShineButton_btn_fill_color, Color.BLACK);
        shineParams.allowRandomColor = a.getBoolean(R.styleable.ShineButton_allow_random_color, false);
        shineParams.animDuration = a.getInteger(R.styleable.ShineButton_shine_animation_duration, (int) shineParams.animDuration);
        shineParams.bigShineColor = a.getColor(R.styleable.ShineButton_big_shine_color, shineParams.bigShineColor);
        shineParams.clickAnimDuration = a.getInteger(R.styleable.ShineButton_click_animation_duration, (int) shineParams.clickAnimDuration);
        shineParams.enableFlashing = a.getBoolean(R.styleable.ShineButton_enable_flashing, false);
        shineParams.shineCount = a.getInteger(R.styleable.ShineButton_shine_count, shineParams.shineCount);
        shineParams.shineDistanceMultiple = a.getFloat(R.styleable.ShineButton_shine_distance_multiple, shineParams.shineDistanceMultiple);
        shineParams.shineTurnAngle = a.getFloat(R.styleable.ShineButton_shine_turn_angle, shineParams.shineTurnAngle);
        shineParams.smallShineColor = a.getColor(R.styleable.ShineButton_small_shine_color, shineParams.smallShineColor);
        shineParams.smallShineOffsetAngle = a.getFloat(R.styleable.ShineButton_small_shine_offset_angle, shineParams.smallShineOffsetAngle);
        shineParams.shineSize = a.getDimensionPixelSize(R.styleable.ShineButton_shine_size, shineParams.shineSize);
        a.recycle();
        setSrcColor(btnColor);
    }

    public int getBottomHeight(boolean real) {
        if (real) {
            return realBottomHeight;
        }
        return bottomHeight;
    }

    public int getColor() {
        return btnFillColor;
    }

    /*public boolean isChecked() {
        return isChecked;
    }*/

    public void setBtnColor(int btnColor) {
        this.btnColor = btnColor;
        setSrcColor(this.btnColor);
    }

    public void setBtnFillColor(int btnFillColor) {
        this.btnFillColor = btnFillColor;
    }

    /*public void setChecked(boolean checked, boolean anim) {
        setChecked(checked, anim, true);
    }*/

    /*private void setChecked(boolean checked, boolean anim, boolean callBack) {
        isChecked = checked;
        if (checked) {
            setSrcColor(btnFillColor);
            isChecked = true;
            if (anim) showAnim();
        } else {
            setSrcColor(btnColor);
            isChecked = false;
            if (anim) setCancel();
        }
        if (callBack) {
            onListenerUpdate(checked);
        }
    }*/

    /*public void setChecked(boolean checked) {
        setChecked(checked, false, false);
    }*/

    /*private void onListenerUpdate(boolean checked) {
        if (listener != null) {
            listener.onCheckedChanged(this, checked);
        }
    }*/

    public void setCancel() {
        setSrcColor(btnColor);
        if (shakeAnimator != null) {
            shakeAnimator.end();
            shakeAnimator.cancel();
        }
    }

    public void setAllowRandomColor(boolean allowRandomColor) {
        shineParams.allowRandomColor = allowRandomColor;
    }

    public void setAnimDuration(int durationMs) {
        shineParams.animDuration = durationMs;
    }

    public void setBigShineColor(int color) {
        shineParams.bigShineColor = color;
    }

    public void setClickAnimDuration(int durationMs) {
        shineParams.clickAnimDuration = durationMs;
    }

    public void enableFlashing(boolean enable) {
        shineParams.enableFlashing = enable;
    }

    public void setShineCount(int count) {
        shineParams.shineCount = count;
    }

    public void setShineDistanceMultiple(float multiple) {
        shineParams.shineDistanceMultiple = multiple;
    }

    public void setShineTurnAngle(float angle) {
        shineParams.shineTurnAngle = angle;
    }

    public void setSmallShineColor(int color) {
        shineParams.smallShineColor = color;
    }

    public void setSmallShineOffAngle(float angle) {
        shineParams.smallShineOffsetAngle = angle;
    }

    public void setShineSize(int size) {
        shineParams.shineSize = size;
    }

    @Override
    public void setOnClickListener(View.OnClickListener l) {
        if (l instanceof OnButtonClickListener) {
            super.setOnClickListener(l);
        } else {
            if (onButtonClickListener != null) {
                onButtonClickListener.setListener(l);
            }
        }
    }

    /*public void setOnCheckStateChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }*/

    OnButtonClickListener onButtonClickListener;

    public void init(Activity activity) {
        this.activity = activity;
        onButtonClickListener = new OnButtonClickListener();
        setOnClickListener(onButtonClickListener);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calPixels();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    public void showAnim() {
        if (activity != null) {
            final ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            shineView = new ShineView(activity, this, shineParams);
            rootView.addView(shineView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            doShareAnim();
        } else {
            LLog.e(TAG, "Please init.");
        }
    }

    public void removeView(View view) {
        if (activity != null) {
            final ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
            rootView.removeView(view);
        } else {
            LLog.e(TAG, "Please init.");
        }
    }

    public void setShapeResource(int raw) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setShape(getResources().getDrawable(raw, null));
        } else {
            setShape(getResources().getDrawable(raw));
        }
    }

    private void doShareAnim() {
        shakeAnimator = ValueAnimator.ofFloat(0.4f, 1f, 0.9f, 1f);
        shakeAnimator.setInterpolator(new LinearInterpolator());
        shakeAnimator.setDuration(500);
        shakeAnimator.setStartDelay(180);
        invalidate();
        shakeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setScaleX((float) valueAnimator.getAnimatedValue());
                setScaleY((float) valueAnimator.getAnimatedValue());
            }
        });
        shakeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (animCallback != null) {
                    animCallback.onAnimationStart();
                }
                setSrcColor(btnFillColor);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (animCallback != null) {
                    animCallback.onAnimationEnd();
                }
                //setSrcColor(isChecked ? btnFillColor : btnColor);
                setSrcColor(btnFillColor);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if (animCallback != null) {
                    animCallback.onAnimationCancel();
                }
                setSrcColor(btnColor);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                if (animCallback != null) {
                    animCallback.onAnimationRepeat();
                }
            }
        });
        shakeAnimator.start();
    }

    public interface AnimCallback {
        public void onAnimationStart();

        public void onAnimationEnd();

        public void onAnimationCancel();

        public void onAnimationRepeat();
    }

    private AnimCallback animCallback;

    public void setAnimCallback(AnimCallback animCallback) {
        this.animCallback = animCallback;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void calPixels() {
        if (activity != null && metrics != null) {
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int[] location = new int[2];
            getLocationInWindow(location);
            Rect visibleFrame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
            realBottomHeight = visibleFrame.height() - location[1];
            bottomHeight = metrics.heightPixels - location[1];
        }
    }

    public class OnButtonClickListener implements View.OnClickListener {
        public void setListener(View.OnClickListener listener) {
            this.listener = listener;
        }

        View.OnClickListener listener;

        public OnButtonClickListener() {
        }

        public OnButtonClickListener(View.OnClickListener l) {
            listener = l;
        }

        @Override
        public void onClick(View view) {
            LLog.d(TAG, "onClick");
            /*if (!isChecked) {
                isChecked = true;
                showAnim();
            } else {
                isChecked = false;
                setCancel();
            }*/
            showAnim();
            //onListenerUpdate(isChecked);
            if (listener != null) {
                listener.onClick(view);
            }
        }
    }

    /*public interface OnCheckedChangeListener {
        void onCheckedChanged(View view, boolean checked);
    }*/

}
