package leon.android.pulltorefresh.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cherry.android.recycler.CommonAdapter;
import cherry.android.recycler.ViewHolder;
import cherry.android.recycler.wrapper.LoadMoreWrapper;

/**
 * Created by Administrator on 2017/6/16.
 */

public class LoadMoreActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private LoadMoreWrapper loadMoreWrapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_header_footer);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(i);
        }
        final CommonAdapter adapter = new CommonAdapter<Integer, ViewHolder>(list, android.R.layout.simple_list_item_1) {
            @Override
            protected void convert(ViewHolder holder, Integer integer, int position) {
                TextView textView = holder.findView(android.R.id.text1);
                textView.setText("" + integer);
            }
        };
        loadMoreWrapper = new LoadMoreWrapper(adapter, R.layout.layout_load_more);
        loadMoreWrapper.setState(LoadMoreWrapper.STATE_LOADING_MORE);
        loadMoreWrapper.setOnLoadMoreListener(new LoadMoreWrapper.SimpleLoadMoreListener() {
            @Override
            public void onLoadMore() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 5; i++) {
                            list.add(2000 + i);
                        }
                        adapter.notifyDataSetChanged();
                        loadMoreWrapper.setState(LoadMoreWrapper.STATE_NO_MORE);
                    }
                }, 2000);

            }
        });
        recyclerView.setAdapter(loadMoreWrapper);
    }
}
