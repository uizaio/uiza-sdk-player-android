package vn.loitp.app.uiza.home.v1.cannotslide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uiza.player.ui.player.v1.cannotslide.UizaPlayerActivityV1;

import java.util.ArrayList;
import java.util.List;

import vn.loitp.app.app.LSApplication;
import vn.loitp.app.uiza.data.HomeDataV1;
import vn.loitp.app.uiza.home.view.BlankView;
import vn.loitp.app.uiza.home.view.EntityItemV1;
import vn.loitp.app.uiza.home.view.LoadingView;
import vn.loitp.core.base.BaseFragment;
import vn.loitp.core.common.Constants;
import vn.loitp.core.utilities.LActivityUtil;
import vn.loitp.core.utilities.LDisplayUtils;
import vn.loitp.core.utilities.LLog;
import vn.loitp.core.utilities.LUIUtil;
import vn.loitp.restapi.restclient.RestClientV2;
import vn.loitp.restapi.uiza.UizaService;
import vn.loitp.restapi.uiza.model.v1.listallentity.Item;
import vn.loitp.restapi.uiza.model.v1.listallentity.ListAllEntity;
import vn.loitp.restapi.uiza.model.v2.listallentity.JsonBodyListAllEntity;
import vn.loitp.rxandroid.ApiSubscriber;
import vn.loitp.uiza.R;
import vn.loitp.views.LToast;
import vn.loitp.views.placeholderview.lib.placeholderview.PlaceHolderView;
import vn.loitp.views.progressloadingview.avloadingindicatorview.lib.avi.AVLoadingIndicatorView;

import static vn.loitp.core.common.Constants.KEY_UIZA_ENTITY_COVER;
import static vn.loitp.core.common.Constants.KEY_UIZA_ENTITY_ID;
import static vn.loitp.core.common.Constants.KEY_UIZA_ENTITY_TITLE;

/**
 * Created by www.muathu@gmail.com on 7/26/2017.
 */

public class FrmChannelV1 extends BaseFragment {
    private final String TAG = getClass().getSimpleName();
    private TextView tv;
    private TextView tvMsg;
    private PlaceHolderView placeHolderView;
    private AVLoadingIndicatorView avLoadingIndicatorView;

    private final int NUMBER_OF_COLUMN_1 = 1;
    private final int NUMBER_OF_COLUMN_2 = 2;
    private final int POSITION_OF_LOADING_REFRESH = 2;

    private boolean isRefreshing;
    private boolean isLoadMoreCalling;
    private final int limit = 50;
    private int page = 0;
    private int totalPage = Integer.MAX_VALUE;
    private final String orderBy = "createdAt";
    private final String orderType = "DESC";

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
        View view = inflater.inflate(R.layout.uiza_frm_channel, container, false);
        tv = (TextView) view.findViewById(R.id.tv);
        tvMsg = (TextView) view.findViewById(R.id.tv_msg);
        if (Constants.IS_DEBUG) {
            tv.setVisibility(View.VISIBLE);
            tv.setText("Debug: " + HomeDataV1.getInstance().getItem().getName());
        } else {
            tv.setVisibility(View.GONE);
        }

        placeHolderView = (PlaceHolderView) view.findViewById(R.id.place_holder_view);

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
                loadMore();
            }
        });

        avLoadingIndicatorView = (AVLoadingIndicatorView) view.findViewById(R.id.avi);
        avLoadingIndicatorView.smoothToShow();

        getData(false);
        return view;
    }

    /*private List<Item> getSubList(List<Item> itemList, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex > itemList.size()) {
            return null;
        }
        List<Item> items = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            items.add(itemList.get(i));
        }
        LLog.d(TAG, "getList " + startIndex + " - " + endIndex + " -> " + items.size());
        return items;
    }*/

    private void setupData(List<Item> itemList, boolean isCallFromLoadMore) {
        /*//poster
        List<Item> itemListPoster = getSubList(itemList, 0, 5);
        placeHolderView.addView(new PosterView(getActivity(), itemListPoster, new PosterView.Callback() {
            @Override
            public void onClick(Item item, int position) {
                onClickVideo(item, position);
            }
        }));

        //top movie
        ChannelObject channelObjectTopMovies = new ChannelObject();
        channelObjectTopMovies.setChannelName("Top Movies");
        List<Item> itemListTopMovies = getSubList(itemList, 6, 15);
        channelObjectTopMovies.setItemList(itemListTopMovies);
        placeHolderView.addView(new ChannelList(getActivity(), channelObjectTopMovies, new ChannelItem.Callback() {
            @Override
            public void onClick(Item item, int position) {
                onClickVideo(item, position);
            }
        }));

        //top movie
        ChannelObject channelObjectNewestMovies = new ChannelObject();
        channelObjectNewestMovies.setChannelName("Newest Movies");
        List<Item> itemListNewestMovies = getSubList(itemList, 16, itemList.size() - 1);
        channelObjectNewestMovies.setItemList(itemListNewestMovies);
        placeHolderView.addView(new ChannelList(getActivity(), channelObjectNewestMovies, new ChannelItem.Callback() {
            @Override
            public void onClick(Item item, int position) {
                onClickVideo(item, position);
            }
        }));

        //top movie
        ChannelObject channelObjectAllMovies = new ChannelObject();
        channelObjectAllMovies.setChannelName("All Movies");
        channelObjectAllMovies.setItemList(itemList);
        placeHolderView.addView(new ChannelList(getActivity(), channelObjectAllMovies, new ChannelItem.Callback() {
            @Override
            public void onClick(Item item, int position) {
                onClickVideo(item, position);
            }
        }));*/

        int sizeW = LDisplayUtils.getScreenW(getActivity()) / 2;
        int sizeH = sizeW * 9 / 16;

        if (isCallFromLoadMore) {
            placeHolderView.removeView(getListSize() - 1);//remove loading view
        } else {
            addBlankView();
        }
        for (Item item : itemList) {
            placeHolderView.addView(new EntityItemV1(getActivity(), item, sizeW, sizeH, new EntityItemV1.Callback() {
                @Override
                public void onClick(Item item, int position) {
                    onClickVideo(item, position);
                }
            }));
        }
        if (!isCallFromLoadMore) {
            avLoadingIndicatorView.smoothToHide();
        } else {
            isLoadMoreCalling = false;
        }
    }

    private int getListSize() {
        return placeHolderView.getAllViewResolvers().size();
    }

    private void addBlankView() {
        for (int i = 0; i < NUMBER_OF_COLUMN_2; i++) {
            placeHolderView.addView(new BlankView());
        }
    }

    private void onClickVideo(Item item, int position) {
        LLog.d(TAG, "onClickVideo at " + position + ": " + LSApplication.getInstance().getGson().toJson(item));
        Intent intent = new Intent(getActivity(), UizaPlayerActivityV1.class);
        intent.putExtra(KEY_UIZA_ENTITY_ID, item.getId());

        //intent.putExtra(KEY_UIZA_ENTITY_ID, "bf427eb6-51e4-43f9-b668-d15e5f324d9e");

        intent.putExtra(KEY_UIZA_ENTITY_COVER, item.getThumbnail());
        intent.putExtra(KEY_UIZA_ENTITY_TITLE, item.getName());
        startActivity(intent);
        LActivityUtil.tranIn(getActivity());
    }

    private void getData(boolean isCallFromLoadMore) {
        LLog.d(TAG, ">>>getData " + page + "/" + totalPage);

        if (page >= totalPage) {
            LLog.d(TAG, "page >= totalPage -> return");
            LToast.show(getActivity(), "This is last page");
            placeHolderView.removeView(getListSize() - 1);//remove loading view
            if (isCallFromLoadMore) {
                isLoadMoreCalling = false;
            }
            return;
        }

        LToast.show(getActivity(), getString(R.string.load_page) + page);
        if (tvMsg.getVisibility() != View.GONE) {
            tvMsg.setVisibility(View.GONE);
        }
        UizaService service = RestClientV2.createService(UizaService.class);

        JsonBodyListAllEntity jsonBodyListAllEntity = new JsonBodyListAllEntity();
        if (HomeDataV1.getInstance().getItem().getId().equals(String.valueOf(Constants.NOT_FOUND))) {
            LLog.d(TAG, "HOME category");
        } else {
            LLog.d(TAG, "!HOME category");
            List<String> metadataId = new ArrayList<>();
            metadataId.add(HomeDataV1.getInstance().getItem().getId());
            jsonBodyListAllEntity.setMetadataId(metadataId);
        }
        jsonBodyListAllEntity.setLimit(limit);
        jsonBodyListAllEntity.setPage(page);
        jsonBodyListAllEntity.setOrderBy(orderBy);
        jsonBodyListAllEntity.setOrderType(orderType);
        LLog.d(TAG, "jsonBodyListAllEntity " + LSApplication.getInstance().getGson().toJson(jsonBodyListAllEntity));
        LLog.d(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<");

        subscribe(service.listAllEntityV1(jsonBodyListAllEntity), new ApiSubscriber<ListAllEntity>() {
            @Override
            public void onSuccess(ListAllEntity listAllEntity) {
                LLog.d(TAG, "getData onSuccess " + LSApplication.getInstance().getGson().toJson(listAllEntity));
                LLog.d(TAG, "getLimit " + listAllEntity.getLimit());
                LLog.d(TAG, "getPage " + listAllEntity.getPage());
                LLog.d(TAG, "getTotal " + listAllEntity.getTotal());
                LLog.d(TAG, "getItems().size " + listAllEntity.getItems().size());

                if (totalPage == Integer.MAX_VALUE) {
                    int totalItem = (int) listAllEntity.getTotal();
                    float ratio = (float) (totalItem / limit);
                    LLog.d(TAG, "ratio: " + ratio);
                    if (ratio == 0) {
                        totalPage = (int) ratio;
                    } else if (ratio > 0) {
                        totalPage = (int) ratio + 1;
                    } else {
                        totalPage = (int) ratio;
                    }
                    LLog.d(TAG, ">>>totalPage: " + totalPage);
                }

                List<Item> itemList = listAllEntity.getItems();
                if (itemList == null || itemList.isEmpty()) {
                    if (tvMsg.getVisibility() != View.VISIBLE) {
                        tvMsg.setVisibility(View.VISIBLE);
                        tvMsg.setText(getString(R.string.empty_list));
                    }
                    if (!isCallFromLoadMore) {
                        avLoadingIndicatorView.smoothToHide();
                    } else {
                        isLoadMoreCalling = false;
                    }
                }
                setupData(itemList, isCallFromLoadMore);
            }

            @Override
            public void onFail(Throwable e) {
                LLog.e(TAG, "listAllEntityV1 onFail " + e.toString());
                //handleException(e);
                if (tvMsg.getVisibility() != View.VISIBLE) {
                    tvMsg.setVisibility(View.VISIBLE);
                    if (e != null && e.getMessage() != null) {
                        tvMsg.setText("onFail " + e.getMessage());
                    }
                }
                if (!isCallFromLoadMore) {
                    avLoadingIndicatorView.smoothToHide();
                } else {
                    isLoadMoreCalling = false;
                }
            }
        });
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
        placeHolderView.addView(new LoadingView());
        placeHolderView.smoothScrollToPosition(getListSize() - 1);
        page++;
        getData(true);
    }
}