package com.uiza.player.ui.views.view.listview;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.uiza.player.ui.data.UizaData;
import com.uiza.player.ui.util.UizaScreenUtil;

import java.util.ArrayList;
import java.util.List;

import io.uiza.sdk.ui.R;
import vn.loitp.core.utilities.LLog;
import vn.loitp.restapi.uiza.model.v2.listallentity.Item;
import vn.loitp.restapi.uiza.model.v2.listallentityrelation.ListAllEntityRelation;
import vn.loitp.views.LToast;

/**
 * Created by www.muathu@gmail.com on 5/13/2017.
 */

public class PlayListView extends RelativeLayout {
    private final String TAG = getClass().getSimpleName();
    //TODO remove gson later
    private Gson gson = new Gson();
    private List<Item> itemList;
    private RecyclerView recyclerView;
    private PlayListAdapter playListAdapter;
    private TextView tvMsg;

    public PlayListView(Context context) {
        super(context);
        init();
    }

    public PlayListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LLog.d(TAG, "init");
        if (UizaData.getInstance().getInputModel() == null || UizaData.getInstance().getInputModel().getEntityID() == null) {
            LLog.d(TAG, "UizaData.getInstance().getInputModel() == null || UizaData.getInstance().getInputModel().getEntityID() == null -> return");
            return;
        }
        String entityId = UizaData.getInstance().getInputModel().getEntityID();
        LLog.d(TAG, "entityId: " + entityId);

        ListAllEntityRelation listAllEntityRelation = UizaData.getInstance().getListAllEntityRelation(entityId);
        itemList = listAllEntityRelation.getItemList();
        LLog.d(TAG, "listAllEntityRelation: " + gson.toJson(listAllEntityRelation));

        inflate(getContext(), R.layout.play_list_view, this);

        this.tvMsg = (TextView) findViewById(R.id.tv_msg);
        if (itemList == null || itemList.isEmpty()) {
            tvMsg.setVisibility(VISIBLE);
            return;
        } else {
            tvMsg.setVisibility(GONE);
        }
        LLog.d(TAG, "itemList size: " + itemList.size());

        this.recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        int widthRecyclerView;
        int heightRecyclerView;

        if (UizaData.getInstance().isLandscape()) {
            widthRecyclerView = UizaScreenUtil.getScreenWidth();
            heightRecyclerView = UizaScreenUtil.getScreenHeight() / 2;

            ViewGroup.LayoutParams recyclerViewParams = recyclerView.getLayoutParams();
            recyclerViewParams.width = widthRecyclerView;
            recyclerViewParams.height = heightRecyclerView;
            recyclerView.setLayoutParams(recyclerViewParams);
        } else {
            widthRecyclerView = UizaScreenUtil.getScreenWidth();
            heightRecyclerView = UizaScreenUtil.getScreenHeight() / 5;

            ViewGroup.LayoutParams recyclerViewParams = recyclerView.getLayoutParams();
            recyclerViewParams.width = widthRecyclerView;
            recyclerViewParams.height = heightRecyclerView;
            recyclerView.setLayoutParams(recyclerViewParams);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        playListAdapter = new PlayListAdapter(getContext(), itemList, widthRecyclerView, heightRecyclerView, new PlayListAdapter.Callback() {
            @Override
            public void onClickItem(Item item) {
                //TODO
                LToast.show(getContext(), "Click " + gson.toJson(item));
            }
        });
        recyclerView.setAdapter(playListAdapter);
    }
}