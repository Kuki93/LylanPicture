package cn.yaoxi.slavingpicture.loader;

import android.graphics.PointF;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.Objects;

public class DisplayOptimizeListener extends SubsamplingScaleImageView.DefaultOnImageEventListener {
    private static final float LONG_IMAGE_SIZE_RATIO = 2f;

    private final SubsamplingScaleImageView mImageView;

    private int mInitScaleType;

    private int viewWidth ;
    private int viewHeight;

    public DisplayOptimizeListener(SubsamplingScaleImageView imageView, int viewWidth, int viewHeight) {
        mImageView = imageView;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        mInitScaleType = -1;
    }

    @Override
    public void onReady() {

        float result = 0.5f;
        float scale = 0.5f;
        int imageWidth = mImageView.getSWidth();
        int imageHeight = mImageView.getSHeight();
        int viewWidth = Math.max(this.viewWidth, mImageView.getWidth());
        int viewHeight = Math.max(this.viewHeight, mImageView.getHeight());

        boolean hasZeroValue = false;
        if (imageWidth == 0 || imageHeight == 0 || viewWidth == 0 || viewHeight == 0) {
            result = 0.5f;
            hasZeroValue = true;
        }

        if (!hasZeroValue) {
            scale = (float) viewWidth / imageWidth;

            if (imageWidth <= imageHeight) {
                result = (float) viewWidth / imageWidth;
            } else {
                result = (float) viewHeight / imageHeight;
            }
        }

        // `对结果进行放大裁定，防止计算结果跟双击放大结果过于相近`
        if (Math.abs(result - 0.1) < 0.2f) {
            result += 0.2f;
        }

        if (mInitScaleType == EasyPreView.INIT_SCALE_TYPE_CUSTOM) {
            final float maxScale = Math.max(scale * 3f, Math.max(result, mImageView.getMaxScale()));
            final float minScale = scale;

            mImageView.setMaxScale(maxScale);
            // scale to fit screen, and center
            if (!hasZeroValue && (float) imageHeight / imageWidth > LONG_IMAGE_SIZE_RATIO) {
                // scale at top
                Objects.requireNonNull(mImageView
                        .animateScaleAndCenter(scale, new PointF(imageWidth / 2, 0)))
                        .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                        .withOnAnimationEventListener(new SubsamplingScaleImageView.DefaultOnAnimationEventListener() {
                            @Override
                            public void onComplete() {
                                mImageView.setMinScale(minScale);
                            }
                        })
                        .start();
            } else {
                mImageView.setMinScale(minScale);
                mImageView.setScaleAndCenter(scale, new PointF(imageWidth / 2, 0));
            }

            if (result == scale) {
                result = scale * 1.5f;
            }
        } else {
            if (!hasZeroValue && (float) imageHeight / imageWidth > LONG_IMAGE_SIZE_RATIO) {
                // scale at top
                Objects.requireNonNull(mImageView
                        .animateScaleAndCenter(scale, new PointF(imageWidth / 2, 0)))
                        .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                        .start();
            }
        }
        mImageView.setDoubleTapZoomScale(result);
    }

    public void setInitScaleType(int initScaleType) {
        if (mInitScaleType != -1) {
            mInitScaleType = initScaleType;
            onReady();
        } else {
            mInitScaleType = initScaleType;
        }
    }
}