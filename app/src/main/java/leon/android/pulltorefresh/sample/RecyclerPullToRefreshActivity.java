package leon.android.pulltorefresh.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cherry.android.recycler.ItemViewDelegate;
import cherry.android.recycler.RecyclerAdapter;
import cherry.android.recycler.ViewConverter;
import cherry.android.recycler.ViewHolder;
import leon.android.pulltorefresh.DefaultRefreshHeader;
import leon.android.pulltorefresh.NestedPullRefreshLayout;
import leon.android.pulltorefresh.OnRefreshListener;

public class RecyclerPullToRefreshActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;
    NestedPullRefreshLayout pullRefreshLayout;
    RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_nested_scrolling);
        pullRefreshLayout = findViewById(R.id.layout_pull_to_refresh);
        pullRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullRefreshLayout.refreshComplete();
                    }
                }, 3000);
            }
        });
        pullRefreshLayout.setRefreshHeader(new DefaultRefreshHeader(this));
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerAdapter();
        adapter.addDelegate(String.class)
                .bindDelegate(new DefaultItemViewDelegate(), new DefaultItemViewDelegate1())
                .to(new ViewConverter<String>() {
                    @Override
                    public Class<? extends ItemViewDelegate> convert(String s, int position) {
                        return position % 2 == 0 ? DefaultItemViewDelegate.class : DefaultItemViewDelegate1.class;
                    }
                });
        adapter.addDelegate(Integer.class, new ItemViewDelegate<Integer, ViewHolder>() {
            @NonNull
            @Override
            public ViewHolder createViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
                View itemView = inflater.inflate(R.layout.item_app_simple, parent, false);
                return new ViewHolder(itemView);
            }

            @Override
            public void convert(ViewHolder holder, Integer integer, int position) {
                TextView tv = holder.findView(R.id.text);
                tv.setText("Integer item=" + integer);
            }
        });
        Random random = new Random();
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            if (random.nextInt(2) == 0) {
                items.add(i);
            } else {
                items.add("item=" + i);
            }
        }
        adapter.setItems(items);
        recyclerView.setAdapter(adapter);


        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        findViewById(R.id.btn6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                pullRefreshLayout.autoRefresh();
                pullRefreshLayout.setOverScrollEnable(true);
                break;
            case R.id.btn2:
                pullRefreshLayout.setOverScrollEnable(false);
                break;
            case R.id.btn3:
                pullRefreshLayout.setOverScrollTopShow(true);
                break;
            case R.id.btn4:
                pullRefreshLayout.setOverScrollTopShow(false);
                break;
            case R.id.btn5:
                adapter.setItems(null);
                break;
            case R.id.btn6:
                Random random = new Random();
                List<Object> items = new ArrayList<>();
                for (int i = 0; i < 30; i++) {
                    if (random.nextInt(2) == 0) {
                        items.add(i);
                    } else {
                        items.add("item=" + i);
                    }
                }
                adapter.setItems(items);
                break;
        }
    }

    static class DefaultItemViewDelegate implements ItemViewDelegate<String, ViewHolder> {

        @NonNull
        @Override
        public ViewHolder createViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            View itemView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void convert(ViewHolder holder, String s, int position) {
            TextView textView = holder.findView(android.R.id.text2);
            textView.setText("string item=" + s);
            textView = holder.findView(android.R.id.text1);
            textView.setText("Title:");
        }
    }

    static class DefaultItemViewDelegate1 implements ItemViewDelegate<String, ViewHolder> {

        @NonNull
        @Override
        public ViewHolder createViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
            View itemView = inflater.inflate(R.layout.item_app_simple_2, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void convert(ViewHolder holder, String s, int position) {
            TextView tv = holder.findView(R.id.tv);
            tv.setText(s);
        }
    }
}
