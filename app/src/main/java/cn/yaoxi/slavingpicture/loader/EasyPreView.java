package cn.yaoxi.slavingpicture.loader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

public class EasyPreView extends FrameLayout {

    public static final int INIT_SCALE_TYPE_CENTER = 0;
    public static final int INIT_SCALE_TYPE_CENTER_CROP = 1;
    public static final int INIT_SCALE_TYPE_CENTER_INSIDE = 2;
    public static final int INIT_SCALE_TYPE_FIT_CENTER = 3;
    public static final int INIT_SCALE_TYPE_FIT_END = 4;
    public static final int INIT_SCALE_TYPE_FIT_START = 5;
    public static final int INIT_SCALE_TYPE_FIT_XY = 6;
    public static final int INIT_SCALE_TYPE_CUSTOM = 7;
    public static final int INIT_SCALE_TYPE_START = 8;

    public static final int DEFAULT_IMAGE_SCALE_TYPE = INIT_SCALE_TYPE_CUSTOM;

    public static final ImageView.ScaleType[] IMAGE_SCALE_TYPES = {
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_XY,
    };

    public static final RequestOptions DOWNLOAD_ONLY_OPTIONS =
            new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).priority(Priority.HIGH)
                    .skipMemoryCache(true);

    private final RequestOptions CACHE_ONLY_OPTIONS = new RequestOptions().onlyRetrieveFromCache(true);

    private ProgressWheel mProgressView;
    private TextView tvProgress;
    private View mDisPlayView;
    private SubsamplingScaleImageView subsamplingScaleImageView;
    private PhotoView photoView;

    private DisplayOptimizeListener mDisplayOptimizeListener;
    private ImageSaveCallback mImageSaveCallback;
    private int mInitScaleType;
    private File mCurrentImageFile;
    private SourceData mSourceData;
    private boolean loadFailure;
    private boolean failureRetry;
    private int errorId;

    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLongClickListener;
    private OnClickListener mFailureImageClickListener;

    public EasyPreView(@NonNull Context context) {
        this(context, null);
    }

    public EasyPreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyPreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInitScaleType = DEFAULT_IMAGE_SCALE_TYPE;

        mFailureImageClickListener = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Retry loading when failure image is clicked
                if (failureRetry && loadFailure) {
                    easyDisplay(mSourceData);
                }
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(v);
                }
            }
        };
    }

    public void easyDisplay(@NonNull SourceData sourceData) {
        mCurrentImageFile = null;
        final Uri uri = sourceData.getSource();
        final Uri thumbnail = sourceData.getThumbnail();

        if (uri == null) {
            setFailureImage();
            return;
        }

        if (photoView != null) {
            photoView.setImageDrawable(null);
        } else if (subsamplingScaleImageView != null) {
            subsamplingScaleImageView.setImage(ImageSource.resource(0));
        }

        this.mSourceData = sourceData;

        getGlideCacheFile(uri, new FetchCacheFileListener() {
            @Override
            public void onResult(final File source) {
                if (source != null && source.exists()) {
                    easyDisplay(source);
                } else {
                    showProgressView(0);
                    if (thumbnail != null) {
                        getGlideCacheFile(thumbnail, new FetchCacheFileListener() {
                            @Override
                            public void onResult(final File source) {
                                if (source != null && source.exists()) {
                                    easyDisplay(source);
                                }
                            }
                        });
                    }

                    ImageDownloadTarget target = new ImageDownloadTarget(uri.toString()) {

                        @Override
                        public void onResourceReady(@NonNull File resource,
                                                    Transition<? super File> transition) {
                            super.onResourceReady(resource, transition);
                            if (TextUtils.equals(getmUrl(), mSourceData.getSource().toString())) {
                                hideProgressView();
                                easyDisplay(resource);
                            }
                        }

                        @Override
                        public void onLoadFailed(final Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            if (TextUtils.equals(getmUrl(), mSourceData.getSource().toString())) {
                                hideProgressView();
                                setFailureImage();
                            }
                        }

                        @Override
                        public void onProgress(final float progress) {
                            if (TextUtils.equals(getmUrl(), mSourceData.getSource().toString())) {
                                post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgressView(progress);
                                    }
                                });
                            }
                        }
                    };
                    Glide.with(getContext()).downloadOnly().load(uri)
                            .apply(DOWNLOAD_ONLY_OPTIONS).into(target);
                }
            }
        });
    }

    private void easyDisplay(File file) {
        mCurrentImageFile = file;
        View oldDisPlay = mDisPlayView;
        int type = ImageInfoExtractor.getImageType(file);
        switch (type) {
            case ImageInfoExtractor.TYPE_GIF:
            case ImageInfoExtractor.TYPE_ANIMATED_WEBP:
                subsamplingScaleImageView = null;
                if (photoView == null) {
                    photoView = new PhotoView(getContext());
                }
                Glide.with(getContext()).load(file).into(photoView);
                mDisPlayView = photoView;
                break;
            default:
                photoView = null;
                if (subsamplingScaleImageView == null) {
                    subsamplingScaleImageView = new SubsamplingScaleImageView(getContext());
                }

                DisplayMetrics metrics = this.getResources().getDisplayMetrics();
                float averageDpi = (metrics.xdpi + metrics.ydpi) / 3;
                subsamplingScaleImageView.setMinimumTileDpi((int) averageDpi);

                mDisplayOptimizeListener = new DisplayOptimizeListener(subsamplingScaleImageView,
                        metrics.widthPixels, metrics.heightPixels);
                subsamplingScaleImageView.setOnImageEventListener(mDisplayOptimizeListener);
                setInitScaleType(mInitScaleType);

                subsamplingScaleImageView.setImage(ImageSource.uri(Uri.fromFile(file)));
                mDisPlayView = subsamplingScaleImageView;
                break;
        }

        if (oldDisPlay != null && oldDisPlay != mDisPlayView) {
            removeView(oldDisPlay);
        }

        int index = indexOfChild(mDisPlayView);
        if (index < 0) {
            addView(mDisPlayView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public ImageView.ScaleType scaleType(int value) {
        if (0 <= value && value < IMAGE_SCALE_TYPES.length) {
            return IMAGE_SCALE_TYPES[value];
        }
        return IMAGE_SCALE_TYPES[DEFAULT_IMAGE_SCALE_TYPE];
    }

    public void setInitScaleType(int initScaleType) {
        mInitScaleType = initScaleType;
        if (subsamplingScaleImageView == null) {
            return;
        }
        switch (initScaleType) {
            case INIT_SCALE_TYPE_CENTER_CROP:
                subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                break;
            case INIT_SCALE_TYPE_CUSTOM:
                subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                break;
            case INIT_SCALE_TYPE_START:
                subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
                break;
            case INIT_SCALE_TYPE_CENTER_INSIDE:
            default:
                subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                break;
        }

        if (mDisplayOptimizeListener != null) {
            mDisplayOptimizeListener.setInitScaleType(initScaleType);
        }
    }

    private void removeDisplay() {
        if (mDisPlayView != null) {
            removeView(mDisPlayView);
        }
        photoView = null;
        subsamplingScaleImageView = null;
        mDisPlayView = null;
    }

    private void showProgressView(float progress) {
        removeDisplay();

        LayoutParams layoutParams = null;
        if (mProgressView == null) {
            mProgressView = new ProgressWheel(getContext());
            mProgressView.setRimWidth(PUtils.dp2px(getContext(), 4));
            mProgressView.setCircleRadius(PUtils.dp2px(getContext(), 56));
            mProgressView.setBarColor(0xFFFFFFFF);
            mProgressView.setRimColor(0x33000000);
            mProgressView.setBarWidth(PUtils.dp2px(getContext(), 4));
            mProgressView.setLinearProgress(true);

            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            addView(mProgressView, layoutParams);
        }

        if (tvProgress == null) {
            tvProgress = new TextView(getContext());
            tvProgress.setText("0%");
            tvProgress.setTextSize(10);
            tvProgress.setGravity(Gravity.CENTER);

            if (layoutParams == null) {
                layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER;
            }
            addView(tvProgress, layoutParams);
        }

        tvProgress.setText(MessageFormat.format("{0}%", (int) (progress * 100)));
        mProgressView.setInstantProgress(progress);
    }


    private void hideProgressView() {
        if (mProgressView != null) {
            mProgressView.setVisibility(GONE);
            removeView(mProgressView);
            mProgressView = null;
        }

        if (tvProgress != null) {
            tvProgress.setVisibility(GONE);
            removeView(tvProgress);
            tvProgress = null;
        }
    }

    public void setFailureRetry(boolean failureRetry) {
        this.failureRetry = failureRetry;
    }

    public SourceData getSourceData() {
        return mSourceData;
    }

    public File getCurrentImageFile() {
        return mCurrentImageFile;
    }

    private void saveImageIntoGallery(final ImageSaveCallback imageSaveCallback,
                                      final Bitmap watermark,
                                      final String content,
                                      final float textSize,
                                      @ColorInt final int color,
                                      final float x,
                                      final float y,
                                      final int alpha,
                                      final boolean recycle) {
        mImageSaveCallback = imageSaveCallback;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (mImageSaveCallback != null) {
                mImageSaveCallback.onFail(new IllegalStateException("please get permission first"));
            }
            return;
        }

        if (mCurrentImageFile == null) {
            // TODO: 2019/2/14 此时应该下载
            if (mImageSaveCallback != null) {
                mImageSaveCallback.onFail(new IllegalStateException("image not downloaded yet"));
            }
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentImageFile.getAbsolutePath());
                if (watermark != null) {
                    bitmap = PUtils.addImageWatermark(bitmap, watermark, (int) x, (int) y, alpha, recycle);
                }
                if (!TextUtils.isEmpty(content)) {
                    bitmap = PUtils.addTextWatermark(bitmap, content, textSize, color, x, y, recycle);
                }
                final String path = PUtils.saveImageIntoGallery(getContext(), mCurrentImageFile, bitmap);
                if (imageSaveCallback != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (TextUtils.isEmpty(path)) {
                                mImageSaveCallback.onFail(new RuntimeException("saveImageIntoGallery fail"));
                            } else {
                                mImageSaveCallback.onSuccess(path);
                            }
                        }
                    });
                }
            }
        });
        t.start();
    }

    public void saveImageIntoGallery(ImageSaveCallback imageSaveCallback) {
        saveImageIntoGallery(imageSaveCallback, null, null, 0, 0, 0, 0, 0, false);
    }

    public void saveImageWithTextWaterMaskIntoGallery(ImageSaveCallback imageSaveCallback,
                                                      final String content,
                                                      final float textSize,
                                                      @ColorInt final int color,
                                                      final float x,
                                                      final float y,
                                                      final boolean recycle) {
        saveImageIntoGallery(imageSaveCallback, null, content, textSize, color, x, y, 0, recycle);
    }

    public void saveImageWithImageWaterMaskIntoGallery(ImageSaveCallback imageSaveCallback,
                                                       final Bitmap watermark,
                                                       final int x,
                                                       final int y,
                                                       final int alpha,
                                                       final boolean recycle) {
        saveImageIntoGallery(imageSaveCallback, watermark, null, 0, 0, x, y, alpha, recycle);
    }

    public float getScale() {
        if (photoView != null) {
            return photoView.getScale();
        } else if (subsamplingScaleImageView != null) {
            return subsamplingScaleImageView.getScale();
        }
        return 0;
    }

    public float getMinScale() {
        if (photoView != null) {
            return photoView.getMinimumScale();
        } else if (subsamplingScaleImageView != null) {
            return subsamplingScaleImageView.getMinScale();
        }
        return -1f;
    }

    public float getMaxScale() {
        if (photoView != null) {
            return photoView.getMaximumScale();
        } else if (subsamplingScaleImageView != null) {
            return subsamplingScaleImageView.getMaxScale();
        }
        return -1f;
    }

    @Override
    public void setOnClickListener(final OnClickListener listener) {
        mOnClickListener = listener;
        if (mDisPlayView != null) {
            mDisPlayView.setOnClickListener(listener);
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
        if (mDisPlayView != null) {
            mDisPlayView.setOnLongClickListener(listener);
        }
    }


    private void setFailureImage() {
        // 加载失败
        loadFailure = true;
        SourceData sourceData = new SourceData(PUtils.res2Uri(getContext(), errorId));
        easyDisplay(sourceData);
        if (mDisPlayView != null) {
            mDisPlayView.setOnClickListener(mFailureImageClickListener);
        }
    }

    /**
     * 获取是否有某张原图的缓存
     * 缓存模式必须是：DiskCacheStrategy.SOURCE 才能获取到缓存文件
     */
    private void getGlideCacheFile(final Uri url, final FetchCacheFileListener runnable) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = null;
                try {
                    file = Glide.with(getContext()).downloadOnly().load(url).
                            apply(CACHE_ONLY_OPTIONS).submit().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    final File source = file;
                    post(new Runnable() {
                        @Override
                        public void run() {
                            runnable.onResult(source);
                        }
                    });
                }
            }
        });
        t.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (subsamplingScaleImageView != null) {
            subsamplingScaleImageView.recycle();
            subsamplingScaleImageView = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
