package vn.loitp.core.utilities;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

import loitp.core.R;
import vn.loitp.core.utilities.animation.Techniques;
import vn.loitp.core.utilities.animation.YoYo;

/**
 * Created by www.muathu@gmail.com on 6/9/2017.
 */

public class LAnimationUtil {
    public interface Callback {
        public void onCancel();

        public void onEnd();

        public void onRepeat();

        public void onStart();
    }

    public static void play(View view, int duration, int repeatCount, Techniques techniques, int delayInMls, final Callback callback) {
        if (view == null) {
            return;
        }

        view.clearAnimation();
        YoYo.with(techniques)
                .duration(duration)
                .repeat(repeatCount)
                .onCancel(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (callback != null) {
                            callback.onCancel();
                        }
                    }
                })
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (callback != null) {
                            callback.onEnd();
                        }
                    }
                })
                .onRepeat(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (callback != null) {
                            callback.onRepeat();
                        }
                    }
                })
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (callback != null) {
                            callback.onStart();
                        }
                    }
                })
                .delay(delayInMls)
                .playOn(view);
    }

    public static void play(View view, Techniques techniques) {
        play(view, 500, 1, techniques, 0, null);
    }

    public static void playRepeatCount(View view, Techniques techniques, int count) {
        play(view, 500, count, techniques, 0, null);
    }

    public static void play(View view, Techniques techniques, int delayInMls) {
        play(view, 500, 1, techniques, delayInMls, null);
    }

    public static void play(View view, Techniques techniques, Callback callback) {
        play(view, 500, 1, techniques, 0, callback);
    }

    public static void playDuration(View view, Techniques techniques, int duration) {
        play(view, duration, 1, techniques, 0, null);
    }

    public static void playDuration(View view, Techniques techniques, int duration, Callback callback) {
        play(view, duration, 1, techniques, 0, callback);
    }

    public static void playRotate(View view, Animation.AnimationListener animationListener) {
        RotateAnimation anim = new RotateAnimation(0.0f, 90.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setFillAfter(true);
        //anim.setRepeatCount(Animation.INFINITE); //Repeat animation indefinitely
        anim.setDuration(500); //Put desired duration per anim cycle here, in milliseconds
        anim.setAnimationListener(animationListener);
        view.startAnimation(anim);
    }

    public static void slideInDown(Context context, View view, Animation.AnimationListener animationListener) {
        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        if (animationListener != null) {
            slideDown.setAnimationListener(animationListener);
        }
        view.startAnimation(slideDown);
    }

    public static void slideInUp(Context context, View view, Animation.AnimationListener animationListener) {
        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        if (animationListener != null) {
            slideDown.setAnimationListener(animationListener);
        }
        view.startAnimation(slideDown);
    }

    public static void fadeIn(Context context, View view, Animation.AnimationListener animationListener) {
        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.fade_enter);
        if (animationListener != null) {
            slideDown.setAnimationListener(animationListener);
        }
        view.startAnimation(slideDown);
    }

    public static void fadeOut(Context context, View view, Animation.AnimationListener animationListener) {
        Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.fade_exit);
        if (animationListener != null) {
            slideDown.setAnimationListener(animationListener);
        }
        view.startAnimation(slideDown);
    }

    public static void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(false); // Needed to keep the result of the animation
        anim.setDuration(200);
        v.startAnimation(anim);
    }
}
