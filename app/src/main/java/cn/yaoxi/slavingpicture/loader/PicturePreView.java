package cn.yaoxi.slavingpicture.loader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.coorchice.library.SuperTextView;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class PicturePreView extends FrameLayout {

    private ImageBrowserConfig imageBrowserConfig;
    private HackyViewPager mViewPager;
    private SuperTextView superTextView;
    private View backgroundView;
    private ImagePreviewAdapter previewAdapter;
    private List<SourceData> sourceConfigList;
    private int currentPosition;

    public PicturePreView(@NonNull Context context) {
        super(context);
    }

    public PicturePreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PicturePreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageBrowserConfig(ImageBrowserConfig imageBrowserConfig) {
        this.imageBrowserConfig = imageBrowserConfig;

        mViewPager = new HackyViewPager(getContext());

        currentPosition = imageBrowserConfig.getOpenShowPosition();
        sourceConfigList = imageBrowserConfig.getSources();

        previewAdapter = new ImagePreviewAdapter(getContext(), imageBrowserConfig);
        mViewPager.setAdapter(previewAdapter);
        mViewPager.setOffscreenPageLimit(imageBrowserConfig.getOffscreenPageLimit());

        if (imageBrowserConfig.isShowIndicator()) {
            superTextView = new SuperTextView(getContext());
            superTextView.setMinWidth(PUtils.dp2px(getContext(), 70));
            superTextView.setTypeface(imageBrowserConfig.getTypeface());
            superTextView.setGravity(Gravity.CENTER);
            superTextView.setPadding(PUtils.dp2px(getContext(), 15), 0, PUtils.dp2px(getContext(), 15), 0);
            superTextView.setSolid(imageBrowserConfig.getIndicatorBgColor());
            superTextView.setTextColor(imageBrowserConfig.getIndicatorTextColor());
            superTextView.setCorner(PUtils.dp2px(getContext(), 15));
        }

        setOnClickListener(imageBrowserConfig.getOnClickListener());
        setOnLongClickListener(imageBrowserConfig.getOnLongClickListener());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                currentPosition = i;
                if (superTextView != null) {
                    superTextView.setText(String.format(Locale.getDefault(), "%d/%d",
                            currentPosition + 1, sourceConfigList.size()));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mViewPager.setCurrentItem(currentPosition);
        superTextView.setText(String.format(Locale.getDefault(), "%d/%d",
                currentPosition + 1, sourceConfigList.size()));
    }

    public void open() {
        LayoutParams layoutParams;
        final Rect rect = imageBrowserConfig.getEnterRect();
        if (imageBrowserConfig.isEnterAnimEnable()) {
            layoutParams = new LayoutParams(rect.width(), rect.height());
        } else {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        addView(mViewPager, layoutParams);

        if (imageBrowserConfig.isEnterAnimEnable()) {
            startEnterAnim(rect);
        } else {
            addOtherView();
        }
    }

    private void startEnterAnim(final Rect rect) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        final int endX = dm.widthPixels;
        final int endY = dm.heightPixels;
        final LayoutParams layoutParams = (LayoutParams) mViewPager.getLayoutParams();

        mViewPager.setTranslationX(rect.left);
        mViewPager.setTranslationY(rect.top);
        mViewPager.animate().translationX(0).translationY(0).setDuration(ImageBrowser.DEFAULT_ANIMATOR_TIME)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    IntEvaluator evaluator = new IntEvaluator();

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();
                        layoutParams.width = evaluator.evaluate(fraction, rect.width(), endX);
                        layoutParams.height = evaluator.evaluate(fraction, rect.height(), endY);
                        mViewPager.setLayoutParams(layoutParams);
                    }
                }).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layoutParams.width = endX;
                layoutParams.height = endY;
                mViewPager.setLayoutParams(layoutParams);
                addOtherView();
            }
        }).start();
    }

    private void addOtherView() {

        LayoutParams layoutParams;
        if (superTextView != null) {
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, PUtils.dp2px(getContext(), 30));
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            layoutParams.setMargins(0, PUtils.dp2px(getContext(), 40), 0, 0);
            addView(superTextView, layoutParams);
        }

        View customMask = imageBrowserConfig.getBottomCustomMaskView();
        if (customMask != null) {
            addView(customMask, imageBrowserConfig.getLayoutParams());
        }

        Drawable drawable = imageBrowserConfig.getBlurDrawable();
        if (drawable != null) {
            setBackground(drawable);
        } else {
            setBackgroundColor(imageBrowserConfig.getBackgroundColor());
        }
    }


    public void saveImageIntoGallery(ImageSaveCallback imageSaveCallback) {
        EasyPreView easyPreView = getCurrentImageView();
        if (easyPreView != null) {
            easyPreView.saveImageIntoGallery(imageSaveCallback);
        } else {
            imageSaveCallback.onFail(new NullPointerException("无法保存"));
        }
    }

    public void saveImageWithTextWaterMaskIntoGallery(ImageSaveCallback imageSaveCallback,
                                                      final String content,
                                                      final float textSize,
                                                      @ColorInt final int color,
                                                      final float x,
                                                      final float y,
                                                      final boolean recycle) {
        EasyPreView easyPreView = getCurrentImageView();
        if (easyPreView != null) {
            easyPreView.saveImageWithTextWaterMaskIntoGallery(imageSaveCallback, content, textSize, color
                    , x, y, recycle);
        } else {
            imageSaveCallback.onFail(new NullPointerException("无法保存"));
        }
    }

    public void saveImageWithImageWaterMaskIntoGallery(ImageSaveCallback imageSaveCallback,
                                                       final Bitmap watermark,
                                                       final int x,
                                                       final int y,
                                                       final int alpha,
                                                       final boolean recycle) {
        EasyPreView easyPreView = getCurrentImageView();
        if (easyPreView != null) {
            easyPreView.saveImageWithImageWaterMaskIntoGallery(imageSaveCallback, watermark,
                    x, y, alpha, recycle);
        } else {
            imageSaveCallback.onFail(new NullPointerException("无法保存"));
        }
    }


    public EasyPreView getCurrentImageView() {
        return mViewPager.findViewWithTag(currentPosition);
    }

    public File getCurrentImageFile() {
        return getCurrentImageView().getCurrentImageFile();
    }

    public int getCurrentIndex() {
        return currentPosition;
    }


    // 手指按下时的坐标
    private float mDownX, mDownY;
    private EasyPreView easyPreView;
    private int status = 0;
    // 在不退出浏览的情况下， Y 轴上的最大可移动距离
    protected float mMaxDisOnY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 是否拦截触摸事件，若拦截，则 ImagePager 自己处理触摸事件；
        // 若不拦截，则 imageView 处理触摸事件
        boolean isIntercept = super.onInterceptTouchEvent(ev);
        if (!imageBrowserConfig.isDragBackEnable()) {
            return isIntercept;
        }
        switch (ev.getAction() & ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                easyPreView = getCurrentImageView();
                if (easyPreView != null) {
                    mMaxDisOnY = Math.max(PUtils.dp2px(getContext(), 100), mViewPager.getHeight() / 5f);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                /**
                 * 拖拽触发条件：
                 * 1、仅有一个触摸点
                 * 2、图片的缩放等级为 1f
                 * 3、拖拽处理类不为空
                 */
                if (easyPreView != null && ev.getPointerCount() == 1 && easyPreView.getScale() == easyPreView.getMinScale()) {
                    float diffX = ev.getX() - mDownX;
                    float diffY = ev.getY() - mDownY;
                    // 上下滑动手势
                    if (Math.abs(diffX) < Math.abs(diffY)) {
                        isIntercept = true;
                    }
                }
                break;
        }
        return isIntercept;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageBrowserConfig.isDragBackEnable()) {
            switch (event.getAction() & event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    onActionDown(event);
                    break;

                case MotionEvent.ACTION_MOVE:
                    onActionMove(event);
                    break;

                case MotionEvent.ACTION_UP:
                    onActionUp(event);
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void onActionDown(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();
    }

    private void onActionMove(MotionEvent event) {
        // 拖拽图片，只有一个触摸点时触发
        float diffY = event.getY() - mDownY;
        if (easyPreView != null && event.getPointerCount() == 1 && easyPreView.getScale() == easyPreView.getMinScale()) {
            mViewPager.setY(mViewPager.getY() + diffY);
            mViewPager.setAlpha(1 - Math.abs(mViewPager.getY() * 1.0f) / getHeight());
            status = 1;
        }
        mDownX = event.getX();
        mDownY = event.getY();
    }

    private void onActionUp(MotionEvent event) {
        // 释放图片
        if (easyPreView != null && easyPreView.getScale() == easyPreView.getMinScale() && Math.abs(mViewPager.getY()) >= mMaxDisOnY) {
            status = 2;
            startExitAnim();
        } else {
            status = 0;
            startResetAnim();
        }
        mDownX = 0;
        mDownY = 0;
    }

    private void startResetAnim() {
        mViewPager.animate().y(0).setDuration(ImageBrowser.DEFAULT_ANIMATOR_TIME / 3)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {

                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                }).start();
    }

    private void startExitAnim() {
        mViewPager.animate().alpha(0)
                .y(mViewPager.getY() >= 0 ? mViewPager.getHeight() : -mViewPager.getHeight())
                .setDuration(ImageBrowser.DEFAULT_ANIMATOR_TIME / 2)
                .setInterpolator(new AccelerateInterpolator())
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {

                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ImageBrowser.release((Activity) getContext(), PicturePreView.this);
                    }
                }).start();
    }

    public void hide() {
        if (imageBrowserConfig.isExitAnimEnable()) {
            startExitAnim();
        } else {
            ImageBrowser.release((Activity) getContext(), this);
        }
    }
}
