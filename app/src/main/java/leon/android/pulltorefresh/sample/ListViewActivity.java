package leon.android.pulltorefresh.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cherry.android.toast.Toaster;
import leon.android.pulltorefresh.DefaultRefreshHeader;
import leon.android.pulltorefresh.NestedPullRefreshLayout;
import leon.android.pulltorefresh.OnRefreshListener;


/**
 * Created by Administrator on 2017/6/28.
 */

public class ListViewActivity extends AppCompatActivity {

    NestedPullRefreshLayout pullRefreshLayout;
    ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_list);
        pullRefreshLayout = findViewById(R.id.layout_pull_to_refresh);
        listView = findViewById(R.id.list_view);

        pullRefreshLayout.setRefreshHeader(new DefaultRefreshHeader(this));
        pullRefreshLayout.setOverScrollEnable(false);
        pullRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.iSuccess(ListViewActivity.this, "刷新完成").show();
                        pullRefreshLayout.refreshComplete();
                    }
                }, 3000);
            }
        });

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("item " + i);
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toaster.iInfo(view.getContext(), "itemClick").show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toaster.iError(view.getContext(), "itemLongClick").show();
                return false;
            }
        });
    }
}
