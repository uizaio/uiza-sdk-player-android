package vn.loitp.app.activity.customviews.bottomnavigationbar.bottombar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import vn.loitp.app.activity.customviews.bottomnavigationbar.bottombar.lib.LBottomBar;
import vn.loitp.app.base.BaseActivity;
import vn.loitp.app.utilities.LStoreUtil;
import vn.uiza.app.R;
import vn.loitp.utils.util.ToastUtils;

public class BottomBarActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText(LStoreUtil.readTxtFromRawFolder(activity, R.raw.loitp));

        setupBottomBar();
    }

    @Override
    protected boolean setFullScreen() {
        return false;
    }

    @Override
    protected String setTag() {
        return getClass().getSimpleName();
    }

    @Override
    protected Activity setActivity() {
        return this;
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_bottom_bar;
    }

    private void setupBottomBar() {
        LBottomBar lBottomBar = (LBottomBar) findViewById(R.id.bottom_bar);
        lBottomBar.setOnItemClick(new LBottomBar.Callback() {
            @Override
            public void OnClickItem(int position) {
                ToastUtils.showShort("Touch " + position);
            }
        });
    }
}
