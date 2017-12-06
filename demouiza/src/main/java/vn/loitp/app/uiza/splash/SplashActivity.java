package vn.loitp.app.uiza.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import vn.loitp.core.base.BaseActivity;
import vn.loitp.app.uiza.home.HomeActivity;
import vn.loitp.app.utilities.LUIUtil;
import vn.loitp.livestar.R;
import vn.loitp.restclient.RestClient;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RestClient.init(getString(R.string.dev_uiza_URL));
        LUIUtil.setDelay(2000, new LUIUtil.DelayCallback() {
            @Override
            public void doAfter(int mls) {
                Intent intent = new Intent(activity, HomeActivity.class);
                startActivity(intent);
                LUIUtil.transActivityFadeIn(activity);
                finish();
            }
        });
    }

    @Override
    protected boolean setFullScreen() {
        return true;
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
        return R.layout.uiza_splash_activity;
    }
}
