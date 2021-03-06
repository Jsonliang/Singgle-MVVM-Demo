package com.adgvcxz.adgble.model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.adgvcxz.adgble.adapter.BaseRecyclerViewAdapter;
import com.adgvcxz.adgble.adapter.ResetObservableArrayList;
import com.adgvcxz.adgble.adapter.TopMarginSelector;
import com.adgvcxz.adgble.api.ApiService;
import com.adgvcxz.adgble.binding.LayoutManager;
import com.adgvcxz.adgble.binding.OnRecyclerViewItemClickListener;

import java.util.List;

import io.reactivex.Observable;

/**
 * zhaowei
 * Created by zhaowei on 2016/10/11.
 */

public abstract class BaseRecyclerViewModel<T> extends BaseViewModel {

    public final ResetObservableArrayList<T> items = new ResetObservableArrayList<>();
    public final ObservableBoolean isLoadAll = new ObservableBoolean(false);
    public final ObservableBoolean loadMore = new ObservableBoolean(true);
    public final LoadMoreViewModel loadMoreViewModel = new LoadMoreViewModel();
    public final ObservableField<LayoutManager.LayoutManagerFactory> layoutManager = new ObservableField<>(LayoutManager.linear());
    public final BaseRecyclerViewAdapter adapter = new BaseRecyclerViewAdapter(loadMoreViewModel);
    int page = 1;
    public ObservableField<TopMarginSelector> topMarginSelector;
    int resetStart = 0;

    void loadData() {
        loadMoreViewModel.loading.set(true);
        request(page).subscribe(ts -> {
            if (page == 1) {
                items.reset(resetStart, ts);
            } else {
                items.addAll(ts);
            }
            if (ts.size() < ApiService.PageNumber) {
                isLoadAll.set(true);
            } else {
                page += 1;
            }
            loadSuccess();
            loadMoreViewModel.loadSuccess.set(true);
            loadMoreViewModel.loading.set(false);
        }, throwable -> {
            loadFailed();
            loadMoreViewModel.loadSuccess.set(false);
            loadMoreViewModel.loading.set(false);
        });
    }


    public final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (recyclerView.getAdapter() != null && !isLoadAll.get()) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                int count = linearLayoutManager.getItemCount();
                if (!loadMoreViewModel.loading.get() && (count == lastPosition + 1)) {
                    if (loadMoreViewModel.loadSuccess.get()) {
                        loadData();
                    }
                }
            }
        }
    };

    public final OnRecyclerViewItemClickListener onLoadMoreClickListener = (recyclerView, position, v) -> {
        if (!loadMoreViewModel.loading.get() && !loadMoreViewModel.loadSuccess.get()) {
            loadData();
        }
    };


    public abstract Observable<List<T>> request(int page);

    void loadSuccess() {
    }

    void loadFailed() {
    }
}
