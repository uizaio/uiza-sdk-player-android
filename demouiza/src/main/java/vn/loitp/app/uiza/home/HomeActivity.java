package vn.loitp.app.uiza.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import loitp.utils.util.ToastUtils;
import vn.loitp.app.activity.customviews.placeholderview._lib.placeholderview.PlaceHolderView;
import vn.loitp.app.base.BaseActivity;
import vn.loitp.app.uiza.data.HomeData;
import vn.loitp.app.uiza.view.UizaActionBar;
import vn.loitp.app.utilities.LUIUtil;
import vn.loitp.livestar.R;

public class HomeActivity extends BaseActivity {
    private PlaceHolderView mDrawerView;
    private DrawerLayout mDrawerLayout;
    private List<String> menuList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerView = (PlaceHolderView) findViewById(R.id.drawerView);

        LUIUtil.setPullLikeIOSVertical(mDrawerView);

        setupDrawer();
        setupActionBar();
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
        return R.layout.uiza_home_activity;
    }

    private void setupDrawer() {
        mDrawerView.addView(new UizaDrawerHeader());

        menuList.add("Home");
        menuList.add("Action");
        menuList.add("Drama");
        menuList.add("Hornor");
        menuList.add("Kids");

        for (int i = 0; i < menuList.size(); i++) {
            mDrawerView.addView(new UizaDrawerMenuItem(this.getApplicationContext(), menuList, i, new UizaDrawerMenuItem.Callback() {
                @Override
                public void onMenuItemClick(int pos) {
                    HomeData.getInstance().setCurrentPosition(pos);
                    HomeData.getInstance().setData(menuList.get(pos));
                    mDrawerLayout.closeDrawers();
                    replaceFragment(new FrmChannel());
                }
            }));
        }

        //init data first
        HomeData.getInstance().setData(menuList.get(0));
        replaceFragment(new FrmChannel());

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //do nothing
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                updateUIDrawer();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                updateUIDrawer();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                //do nothing
            }
        });
    }

    private void setupActionBar() {
        UizaActionBar lActionBar = (UizaActionBar) findViewById(R.id.uiza_action_bar);
        lActionBar.setOnClickBack(new UizaActionBar.Callback() {
            @Override
            public void onClickLeft() {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    updateUIDrawer();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }

            @Override
            public void onClickRight() {
                ToastUtils.showShort("onClickRight");
            }
        });
        lActionBar.showMenuIcon();
        lActionBar.setImageRightIcon(R.drawable.ic_search_black_48dp);
        lActionBar.setImageLeftIcon(R.drawable.ic_menu_black_48dp);
        lActionBar.setTvTitle("Logo");
    }

    private void updateUIDrawer() {
        if (mDrawerView != null) {
            mDrawerView.refresh();
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}