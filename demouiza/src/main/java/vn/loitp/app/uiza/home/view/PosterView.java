package vn.loitp.app.uiza.home.view;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.Animation;
import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.annotations.Animate;
import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.annotations.Layout;
import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.annotations.NonReusable;
import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.annotations.Resolve;
import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.annotations.View;
import vn.loitp.app.activity.customviews.viewpager.parrallaxviewpager._lib.parrallaxviewpager.Mode;
import vn.loitp.app.activity.customviews.viewpager.parrallaxviewpager._lib.parrallaxviewpager.ParallaxViewPager;
import vn.loitp.app.uiza.home.model.PosterObject;
import vn.loitp.app.utilities.LImageUtil;
import vn.loitp.app.utilities.LLog;
import vn.loitp.app.utilities.LUIUtil;
import vn.loitp.livestar.R;

/**
 * Created by www.muathu@gmail.com on 9/16/2017.
 */

@Animate(Animation.CARD_TOP_IN_DESC)
@NonReusable
@Layout(R.layout.uiza_poster_list)
public class PosterView {
    private final String TAG = getClass().getSimpleName();
    @View(R.id.viewpager)
    private ParallaxViewPager viewPager;

    private Context mContext;
    private List<PosterObject> posterObjectList;

    public PosterView(Context context, List<PosterObject> posterObjectList) {
        mContext = context;
        this.posterObjectList = posterObjectList;
    }

    @Resolve
    private void onResolved() {
        LUIUtil.setPullLikeIOSVertical(viewPager);
        viewPager.setMode(Mode.RIGHT_OVERLAY);
        viewPager.setAdapter(new SlidePagerAdapter());
        //LLog.d(TAG, "size: " + posterObjectList.size());
    }

    private class SlidePagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.uiza_poster, collection, false);

            ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
            //imageView.setImageResource(R.drawable.iv);
            LImageUtil.load((Activity) mContext, posterObjectList.get(position).getUrl(), imageView);

            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((android.view.View) view);
        }

        @Override
        public int getCount() {
            return posterObjectList == null ? 0 : posterObjectList.size();
        }

        @Override
        public boolean isViewFromObject(android.view.View view, Object object) {
            return view == object;
        }
    }
}