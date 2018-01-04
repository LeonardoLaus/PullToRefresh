package leon.android.pulltorefresh.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cherry.android.recycler.BaseItemViewDelegate;
import cherry.android.recycler.CommonAdapter;
import cherry.android.recycler.ItemViewDelegate;
import cherry.android.recycler.RecyclerAdapter;
import cherry.android.recycler.ViewConverter;
import cherry.android.recycler.ViewHolder;
import cherry.android.recycler.wrapper.HeaderAndFooterWrapper;
import cherry.android.toast.Toaster;

public class HeaderFooterActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    private HeaderAndFooterWrapper mWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_header_footer);
        recyclerView = findViewById(R.id.recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("item === " + i);
        }
//        Adapter adapter = new Adapter(list);
        RecyclerAdapter adapter = new RecyclerAdapter(list);
        adapter.addDelegate(String.class).bindDelegate(new RecyclerDelegate(), new SimpleDelegate())
                .to(new ViewConverter<String>() {

                    @Override
                    public Class<? extends ItemViewDelegate> convert(String s, int position) {
                        return position == 5 ? RecyclerDelegate.class : SimpleDelegate.class;
                    }
                });
        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, RecyclerView.ViewHolder holder, int position) {
                if (position % 5 == 0) {
                    Toaster.iError(HeaderFooterActivity.this, "Header Footer Activity " + position).show();
                    startActivity(new Intent(HeaderFooterActivity.this, RecyclerPullToRefreshActivity.class));
                    return;
                }
                if (position % 5 == 1) {
                    Toaster.iInfo(HeaderFooterActivity.this, "Header Footer Activity " + position).show();
                    startActivity(new Intent(HeaderFooterActivity.this, ListViewActivity.class));
                    return;
                }
                if (position % 5 == 4) {
                    Toaster.normal(HeaderFooterActivity.this, "Header Footer Activity " + position).show();
                    return;
                }
            }
        });

        mWrapper = new HeaderAndFooterWrapper(adapter);
        TextView textView = new TextView(this);
        textView.setText("It is A Header");
        mWrapper.addHeaderView(textView);

        recyclerView.setAdapter(mWrapper);
    }

    static class Adapter extends CommonAdapter<String, ViewHolder> {

        public Adapter(List<String> data) {
            super(data, android.R.layout.simple_list_item_1);
        }

        @Override
        public void convert(ViewHolder holder, String s, int position) {
            TextView textView = holder.findView(android.R.id.text1);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    120));
            textView.setText(s);
        }
    }

    static class SimpleDelegate extends BaseItemViewDelegate<String, ViewHolder> {


        public SimpleDelegate() {
            super(android.R.layout.simple_list_item_1);
        }

        @Override
        public void convert(ViewHolder holder, String s, int position) {
            TextView textView = holder.findView(android.R.id.text1);
            textView.setText(s);
        }
    }

    static class RecyclerDelegate extends BaseItemViewDelegate<String, RecyclerDelegate.DelegateHolder> {

        public RecyclerDelegate() {
            super(R.layout.layout_recycler);
        }

        @Override
        public void convert(RecyclerDelegate.DelegateHolder holder, String s, int position) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                list.add("item === " + i);
            }
            holder.recyclerView.setAdapter(new Adapter(list));
        }

        static class DelegateHolder extends ViewHolder {
            RecyclerView recyclerView;

            public DelegateHolder(View itemView) {
                super(itemView);
                recyclerView = itemView.findViewById(R.id.recycler);
                ViewGroup.LayoutParams params = this.recyclerView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                this.recyclerView.setLayoutParams(params);
                this.recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
        }
    }
}
