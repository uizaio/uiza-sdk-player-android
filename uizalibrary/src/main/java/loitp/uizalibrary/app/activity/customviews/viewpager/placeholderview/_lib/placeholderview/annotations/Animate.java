package loitp.uizalibrary.app.activity.customviews.viewpager.placeholderview._lib.placeholderview.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import vn.loitp.app.activity.customviews.viewpager.placeholderview._lib.placeholderview.Animation;

/**
 * Created by janisharali on 18/08/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Animate {
    int value() default Animation.ENTER_LEFT_DESC;

    int duration() default Animation.ANIM_DURATION;

    float factor() default Animation.ANIM_INTERPOLATION_FACTOR;
}
