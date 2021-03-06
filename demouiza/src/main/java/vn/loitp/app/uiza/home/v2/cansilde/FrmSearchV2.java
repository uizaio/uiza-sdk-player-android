package vn.loitp.app.uiza.home.v2.cansilde;

/**
 * Created by www.muathu@gmail.com on 12/24/2017.
 */

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.uiza.sdk.ui.R;
import vn.loitp.app.app.LSApplication;
import vn.loitp.app.uiza.home.IOnBackPressed;
import vn.loitp.app.uiza.home.view.EntityItemV2;
import vn.loitp.app.uiza.home.view.LoadingView;
import vn.loitp.core.base.BaseFragment;
import vn.loitp.core.utilities.LDisplayUtils;
import vn.loitp.core.utilities.LKeyBoardUtil;
import vn.loitp.core.utilities.LLog;
import vn.loitp.core.utilities.LUIUtil;
import vn.loitp.restapi.restclient.RestClientV2;
import vn.loitp.restapi.uiza.UizaService;
import vn.loitp.restapi.uiza.model.v2.listallentity.Item;
import vn.loitp.restapi.uiza.model.v2.search.JsonBodySearch;
import vn.loitp.restapi.uiza.model.v2.search.Search;
import vn.loitp.rxandroid.ApiSubscriber;
import vn.loitp.views.LToast;
import vn.loitp.views.placeholderview.lib.placeholderview.PlaceHolderView;

/**
 * Created by www.muathu@gmail.com on 7/26/2017.
 */

public class FrmSearchV2 extends BaseFragment implements IOnBackPressed, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private ImageView ivBack;
    private ImageView ivClearText;
    private EditText etSearch;
    private TextView tv;
    //private AVLoadingIndicatorView avi;
    private PlaceHolderView placeHolderView;
    private final int NUMBER_OF_COLUMN_1 = 1;
    private final int NUMBER_OF_COLUMN_2 = 2;
    private final int POSITION_OF_LOADING_REFRESH = 0;
    private boolean isRefreshing;
    private boolean isLoadMoreCalling;
    private final int limit = 50;
    private int page = 0;
    private int totalPage = Integer.MAX_VALUE;

    private boolean hasKeyboard;

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
        View view = inflater.inflate(R.layout.uiza_frm_search_v2, container, false);
        ivBack = (ImageView) view.findViewById(vn.loitp.uiza.R.id.iv_back);
        ivClearText = (ImageView) view.findViewById(vn.loitp.uiza.R.id.iv_clear_text);
        etSearch = (EditText) view.findViewById(vn.loitp.uiza.R.id.et_search);
        tv = (TextView) view.findViewById(vn.loitp.uiza.R.id.tv);
        //avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        //avi.hide();//dont smoothToHide();

        etSearch.requestFocus();
        LKeyBoardUtil.show(getActivity());

        placeHolderView = (PlaceHolderView) view.findViewById(vn.loitp.uiza.R.id.place_holder_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), NUMBER_OF_COLUMN_2);
        placeHolderView.getBuilder()
                .setHasFixedSize(false)
                .setItemViewCacheSize(10)
                .setLayoutManager(gridLayoutManager);

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (position) {
                    case POSITION_OF_LOADING_REFRESH:
                        return isRefreshing ? NUMBER_OF_COLUMN_2 : NUMBER_OF_COLUMN_1;
                    default:
                        return NUMBER_OF_COLUMN_1;
                }
            }
        });

        LUIUtil.setPullLikeIOSVertical(placeHolderView, new LUIUtil.Callback() {
            @Override
            public void onUpOrLeft(float offset) {
                //do nothing
            }

            @Override
            public void onUpOrLeftRefresh(float offset) {
                LLog.d(TAG, "onUpOrLeftRefresh");
                swipeToRefresh();
            }

            @Override
            public void onDownOrRight(float offset) {
                //do nothing
            }

            @Override
            public void onDownOrRightRefresh(float offset) {
                LLog.d(TAG, "onDownOrRightRefresh");
            }
        });

        ivBack.setOnClickListener(this);
        ivClearText.setOnClickListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    ivClearText.setVisibility(View.GONE);
                    placeHolderView.removeAllViews();
                    tv.setVisibility(View.GONE);
                    //resetAllView();
                } else {
                    ivClearText.setVisibility(View.VISIBLE);
                    //search(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //do nothing
            }
        });

        LUIUtil.setImeiActionSearch(etSearch, new LUIUtil.CallbackSearch() {
            @Override
            public void onSearch() {
                search(etSearch.getText().toString(), false);
            }
        });

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                view.getWindowVisibleDisplayFrame(r);
                int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 500) { // if more than 100 pixels, its probably a keyboard...
                    if (!hasKeyboard) {
                        hasKeyboard = true;
                        setupUIWithKeyboardListener();
                    }
                } else {
                    if (hasKeyboard) {
                        hasKeyboard = false;
                        setupUIWithKeyboardListener();
                    }
                }
            }
        });
        return view;
    }

    private void setupUIWithKeyboardListener() {
        LLog.d(TAG, "setupUIWithKeyboardListener " + hasKeyboard);
        if (getActivity() != null) {
            LLog.d(TAG, "->>>>>>>>>>>>>setupUIWithKeyboardListener " + hasKeyboard + ", isBackPressed: " + isBackPressed);
            ((HomeV2CanSlideActivity) getActivity()).setupUIWithKeyboardListener(hasKeyboard, isBackPressed);
            if (isBackPressed) {
                pop();
            }
        } else {
            LLog.d(TAG, "getActivity() == null");
        }
    }

    private void pop() {
        if (getActivity() == null || getActivity().getSupportFragmentManager() == null) {
            return;
        }
        getActivity().getSupportFragmentManager().popBackStack();
        ((HomeV2CanSlideActivity) getActivity()).setVisibilityOfActionBar(View.VISIBLE);
    }

    private boolean isBackPressed;

    @Override
    public boolean onBackPressed() {
        LLog.d(TAG, TAG + " onBackPressed");
        if (getActivity() == null || getActivity().getSupportFragmentManager() == null) {
            return false;
        } else {
            //remove keyboard
            LKeyBoardUtil.hide(etSearch);
            isBackPressed = true;
            //end remove keyboard

            LLog.d(TAG, TAG + " onBackPressed else");

            if (!this.isDetached()) {
                LLog.d(TAG, "!this.isDetached()");
                setupUIWithKeyboardListener();
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case vn.loitp.uiza.R.id.iv_back:
                onBackPressed();
                break;
            case vn.loitp.uiza.R.id.iv_clear_text:
                etSearch.setText("");
                break;
        }
    }

    private void search(String keyword, boolean isCallFromLoadMore) {
        LLog.d(TAG, "search keyword: " + keyword);
        tv.setVisibility(View.GONE);
        //avi.smoothToShow();
        if (isCallFromLoadMore) {
            //do nothing
        } else {
            placeHolderView.removeAllViews();
            page = 0;
            totalPage = Integer.MAX_VALUE;
        }

        //LLog.d(TAG, ">>>getData " + page + "/" + totalPage);

        if (page >= totalPage) {
            //LLog.d(TAG, "page >= totalPage -> return");
            //LToast.show(getActivity(), getString(R.string.this_is_last_page));
            if (isCallFromLoadMore) {
                placeHolderView.removeView(getListSize() - 1);//remove loading view
                isLoadMoreCalling = false;
            }
            return;
        }

        //LToast.show(getActivity(), getString(vn.loitp.uiza.R.string.load_page) + page);
        UizaService service = RestClientV2.createService(UizaService.class);

        JsonBodySearch jsonBodySearch = new JsonBodySearch();
        jsonBodySearch.setKeyword(keyword);
        jsonBodySearch.setLimit(limit);
        jsonBodySearch.setPage(page);

        subscribe(service.searchEntityV2(jsonBodySearch), new ApiSubscriber<Search>() {
            @Override
            public void onSuccess(Search search) {
                //LLog.d(TAG, "search onSuccess " + LSApplication.getInstance().getGson().toJson(search));

                if (totalPage == Integer.MAX_VALUE) {
                    int totalItem = (int) search.getMetadata().getTotal();
                    float ratio = (float) (totalItem / limit);
                    //LLog.d(TAG, "ratio: " + ratio);
                    if (ratio == 0) {
                        totalPage = (int) ratio;
                    } else if (ratio > 0) {
                        totalPage = (int) ratio + 1;
                    } else {
                        totalPage = (int) ratio;
                    }
                    //LLog.d(TAG, ">>>totalPage: " + totalPage);
                }

                if (search == null || search.getItemList().isEmpty()) {
                    tv.setText(getString(vn.loitp.uiza.R.string.empty_list));
                    tv.setVisibility(View.VISIBLE);
                } else {
                    setupUIList(search.getItemList());
                }
                //avi.smoothToHide();
            }

            @Override
            public void onFail(Throwable e) {
                if (e == null || e.toString() == null) {
                    return;
                }
                LLog.e(TAG, "listAllEntityV2 onFail " + e.toString());
                tv.setText("Error search " + e.toString());
                tv.setVisibility(View.VISIBLE);
                //avi.smoothToHide();
            }
        });
    }

    private void resetAllView() {
        tv.setText("");
        tv.setVisibility(View.GONE);
    }

    private void setupUIList(List<Item> itemList) {
        int sizeW = LDisplayUtils.getScreenW(getActivity()) / 2;
        int sizeH = sizeW * 9 / 16;
        for (Item item : itemList) {
            placeHolderView.addView(new EntityItemV2(getActivity(), item, sizeW, sizeH, new EntityItemV2.Callback() {
                @Override
                public void onClick(Item item, int position) {
                    onClickVideo(item, position);
                }

                @Override
                public void onPosition(int position) {
                    LLog.d(TAG, "_____onPosition " + position + " ~ " + getListSize());
                    if (position == getListSize() - 1) {
                        LLog.d(TAG, "_____onLast");
                        loadMore();
                    }
                }
            }));
        }
        LKeyBoardUtil.hide(getActivity());
    }

    private void onClickVideo(Item item, int position) {
        LLog.d(TAG, "onClickVideo at " + position + ": " + LSApplication.getInstance().getGson().toJson(item));
        ((HomeV2CanSlideActivity) getActivity()).onClickVideo(item, position);
    }

    private void swipeToRefresh() {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;
        placeHolderView.addView(POSITION_OF_LOADING_REFRESH, new LoadingView());

        //TODO refresh
        LUIUtil.setDelay(2000, new LUIUtil.DelayCallback() {
            @Override
            public void doAfter(int mls) {
                placeHolderView.removeView(POSITION_OF_LOADING_REFRESH);
                isRefreshing = false;
            }
        });
    }

    private void loadMore() {
        if (isLoadMoreCalling) {
            return;
        }
        isLoadMoreCalling = true;
        placeHolderView.post(new Runnable() {
            @Override
            public void run() {
                placeHolderView.addView(new LoadingView());
                placeHolderView.smoothScrollToPosition(getListSize() - 1);
                page++;
                search(etSearch.getText().toString().trim(), true);
            }
        });
    }

    private int getListSize() {
        LLog.d(TAG, "getListSize " + placeHolderView.getAllViewResolvers().size());
        return placeHolderView.getAllViewResolvers().size();
    }

    @Override
    public void onAttach(Context context) {
        ((HomeV2CanSlideActivity) getActivity()).setDrawerLockMode(true);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        ((HomeV2CanSlideActivity) getActivity()).setDrawerLockMode(false);
        super.onDetach();
    }
}