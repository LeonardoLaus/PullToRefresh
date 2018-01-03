//package leon.android.pulltorefresh.sample;
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.RequestOptions;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import cherry.android.banner.Banner;
//import cherry.android.banner.Function;
//import cherry.android.banner.SimpleBannerAdapter;
//import cherry.android.banner.transformer.ZoomOutPageTransformer;
//
///**
// * Created by Administrator on 2017/7/4.
// */
//
//public class BannerActivity extends AppCompatActivity implements View.OnClickListener {
//
//    //one piece, naruto, attack on titan, dragon ball, bleach
//    private static final String[] URLS = {"http://imgsrc.baidu.com/forum/pic/item/6b254190f603738d4fcd7dadb31bb051f919ec5b.jpg",
//            "http://www.bz55.com/uploads/allimg/150309/139-150309161302.jpg",
//            "http://ww4.sinaimg.cn/large/006uZZy8jw1faic21363tj30ci08ct96.jpg",
//            "http://pic35.nipic.com/20131108/8054625_084759432147_2.jpg",
//            "http://g.hiphotos.baidu.com/zhidao/pic/item/38dbb6fd5266d0169f17ec4b972bd40735fa3542.jpg"};
////    private static final String[] URLS = {"http://ww4.sinaimg.cn/large/006uZZy8jw1faic1xjab4j30ci08cjrv.jpg",
////            "http://ww4.sinaimg.cn/large/006uZZy8jw1faic21363tj30ci08ct96.jpg",
////            "http://ww4.sinaimg.cn/large/006uZZy8jw1faic259ohaj30ci08c74r.jpg",
////            "http://ww4.sinaimg.cn/large/006uZZy8jw1faic2b16zuj30ci08cwf4.jpg",
////            "http://ww4.sinaimg.cn/large/006uZZy8jw1faic2e7vsaj30ci08cglz.jpg"};
//
//    Banner banner;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_debug_banner);
//        banner = (Banner) findViewById(R.id.banner);
//        banner.setAdapter(new SimpleBannerAdapter<String>(Arrays.asList(URLS)) {
//            @Override
//            public void convert(String s, ImageView imageView) {
//                Glide.with(BannerActivity.this)
//                        .load(s)
//                        .apply(new RequestOptions().placeholder(R.mipmap.ic_movie_default))
//                        .into(imageView);
//            }
//        });
//        List<String> list = new ArrayList<>();
//        list.add("51巅峰钜惠");
//        list.add("十大星级品牌联盟，全场2折起");
//        list.add("生命不是要超越别人，而是要超越自己。");
//        list.add("己所不欲，勿施于人。——孔子");
//        list.add("嗨购5折不要停");
//        banner.setTitles(list, new Function<String, String>() {
//            @Override
//            public String apply(String s) {
//                return s;
//            }
//        });
//        banner.setBannerTransformer(new ZoomOutPageTransformer());
//        banner.start();
//        findViewById(R.id.btn_left).setOnClickListener(this);
//        findViewById(R.id.btn_center).setOnClickListener(this);
//        findViewById(R.id.btn_right).setOnClickListener(this);
//
//        findViewById(R.id.btn_none).setOnClickListener(this);
//        findViewById(R.id.btn_circle).setOnClickListener(this);
//        findViewById(R.id.btn_num).setOnClickListener(this);
//        findViewById(R.id.btn_num_title).setOnClickListener(this);
//        findViewById(R.id.btn_circle_title).setOnClickListener(this);
//        findViewById(R.id.btn_circle_title_inside).setOnClickListener(this);
//
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_left:
//                banner.getSettings().setIndicatorGravity(Banner.GRAVITY_LEFT).apply();
//                break;
//            case R.id.btn_center:
//                banner.getSettings().setIndicatorGravity(Banner.GRAVITY_CENTER).apply();
//                break;
//            case R.id.btn_right:
//                banner.getSettings().setIndicatorGravity(Banner.GRAVITY_RIGHT).apply();
//                break;
//            case R.id.btn_none:
//                banner.getSettings().setIndicatorStyle(Banner.NONE_INDICATOR).apply();
//                break;
//            case R.id.btn_circle:
//                banner.getSettings().setIndicatorStyle(Banner.CIRCLE_INDICATOR).apply();
//                break;
//            case R.id.btn_num:
//                banner.getSettings().setIndicatorStyle(Banner.NUMBER_INDICATOR).apply();
//                break;
//            case R.id.btn_num_title:
//                banner.getSettings().setIndicatorStyle(Banner.TITLE_NUMBER_INDICATOR).apply();
//                break;
//            case R.id.btn_circle_title:
//                banner.getSettings().setIndicatorStyle(Banner.TITLE_CIRCLE_INDICATOR).apply();
//                break;
//            case R.id.btn_circle_title_inside:
//                banner.getSettings().setIndicatorStyle(Banner.TITLE_CIRCLE_INDICATOR_INSIDE).apply();
//                break;
//        }
//    }
//}
