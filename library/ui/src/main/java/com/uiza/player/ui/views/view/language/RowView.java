package com.uiza.player.ui.views.view.language;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.uiza.sdk.ui.R;

/**
 * Created by www.muathu@gmail.com on 5/13/2017.
 */

public class RowView extends RelativeLayout {
    private final String TAG = getClass().getSimpleName();
    private TextView tvDescription;
    private ImageView ivCheck;
    private LinearLayout rootView;
    private Callback callback;

    private boolean isCanDoubleClick = true;
    private boolean isCheck;

    public RowView(Context context) {
        super(context);
        init();
    }

    public RowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.row_view, this);

        this.rootView = (LinearLayout) findViewById(R.id.root_view);
        this.tvDescription = (TextView) findViewById(R.id.tv_description);
        this.ivCheck = (ImageView) findViewById(R.id.iv_check);

        updateUI();

        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCanDoubleClick) {
                    if (callback != null) {
                        isCheck = !isCheck;
                        updateUI();
                        callback.onClickItem();
                    }
                } else {
                    if (!isCheck && callback != null) {
                        callback.onClickItem();
                        isCheck = !isCheck;
                        updateUI();
                    }
                }
            }
        });
    }

    public interface Callback {
        public void onClickItem();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void updateUI() {
        if (isCheck) {
            ivCheck.setImageResource(R.drawable.ic_checked);
        } else {
            ivCheck.setImageResource(R.drawable.ic_unchecked);
        }
    }

    public void setTvDescription(String description) {
        tvDescription.setText(description);
    }

    public void setTvDescription(int stringRes) {
        tvDescription.setText(getContext().getString(stringRes));
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
        updateUI();
    }

    public boolean isCheck() {
        return isCheck();
    }

    public void setCanDoubleClick(boolean isCanDoubleClick) {
        this.isCanDoubleClick = isCanDoubleClick;
    }
}