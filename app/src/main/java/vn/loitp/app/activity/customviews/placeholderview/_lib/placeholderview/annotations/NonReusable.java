package vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by janisharali on 18/08/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NonReusable {
    boolean value() default true;
}
