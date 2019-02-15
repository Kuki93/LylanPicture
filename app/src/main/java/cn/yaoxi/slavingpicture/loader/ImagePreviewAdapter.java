package cn.yaoxi.slavingpicture.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ImagePreviewAdapter extends PagerAdapter {

    private Queue<View> viewPool; //View池
    private Context context;
    private List<SourceData> data;
    private ImageBrowserConfig imageBrowserConfig;
    private OnAnimatorPrepareListener prepareListener;
    private int animIndex;

    public void setPrepareListener(int animIndex, OnAnimatorPrepareListener prepareListener) {
        this.animIndex = animIndex;
        this.prepareListener = prepareListener;
    }

    public ImagePreviewAdapter(Context context, ImageBrowserConfig imageBrowserConfig) {
        viewPool = new LinkedList<>();
        this.context = context;
        this.imageBrowserConfig = imageBrowserConfig;
        data = imageBrowserConfig.getSources();
        animIndex = -1;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        EasyPreView easyPreView;
        //当池子中有存货就复用，否则才inflate
        if (viewPool.size() > 0) {
            easyPreView = (EasyPreView) viewPool.poll();
        } else {
            easyPreView = new EasyPreView(context);
        }
        easyPreView.setTag(position);
        SourceData sourceConfig = data.get(position);
        sourceConfig.setsWidth(position);
        easyPreView.easyDisplay(sourceConfig);
        container.addView(easyPreView);

        if (imageBrowserConfig.getOnClickListener() != null) {
            easyPreView.setOnClickListener(imageBrowserConfig.getOnClickListener());
        }

        if (imageBrowserConfig.getOnLongClickListener() != null) {
            easyPreView.setOnLongClickListener(imageBrowserConfig.getOnLongClickListener());
        }

        easyPreView.setFailureRetry(imageBrowserConfig.isFailureRetry());

        if (animIndex == position && prepareListener != null) {
            prepareListener.onPrepared(easyPreView);
            animIndex = -1;
        }
        return easyPreView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        EasyPreView easyPreView = (EasyPreView) object;
        container.removeView(easyPreView);
        //将当前View加入到池子中
        viewPool.offer(easyPreView);
    }

    public interface OnAnimatorPrepareListener {
        void onPrepared(EasyPreView easyPreView);
    }
}
