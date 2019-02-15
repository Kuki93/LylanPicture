package cn.yaoxi.slavingpicture.loader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ImageBrowser {

    private Builder builder;
    private static boolean open;
    private static boolean isFullScreen;
    private static boolean glideInit;
    static int DEFAULT_ANIMATOR_TIME = 450;


    private ImageBrowser(@NonNull Activity activity) {
        builder = createImageBuilder(activity);
    }

    public static ImageBrowser with(@NonNull Activity activity) {
        if (!glideInit) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            OkHttpClient okHttpClient = builder.build();
            GlideProgressSupport.init(Glide.get(activity), okHttpClient);
            glideInit = true;
        }
        return new ImageBrowser(activity);
    }

    private Builder createImageBuilder(@NonNull Activity activity) {
        builder = new Builder(activity);
        return builder;
    }

    public Builder setSources(@NonNull List<SourceData> sources) {
        return builder.setSources(sources);
    }

    public static void saveImageIntoGallery(Activity activity, ImageSaveCallback imageSaveCallback) {
        PicturePreView picturePreView = getCurrentImageView(activity);
        if (picturePreView != null) {
            picturePreView.saveImageIntoGallery(imageSaveCallback);
        } else {
            imageSaveCallback.onFail(new NullPointerException("无法保存"));
        }
    }

    public static void saveImageWithTextWaterMaskIntoGallery(Activity activity, ImageSaveCallback imageSaveCallback,
                                                             final String content,
                                                             final float textSize,
                                                             @ColorInt final int color,
                                                             final float x,
                                                             final float y,
                                                             final boolean recycle) {
        PicturePreView picturePreView = getCurrentImageView(activity);
        if (picturePreView != null) {
            picturePreView.saveImageWithTextWaterMaskIntoGallery(imageSaveCallback, content, textSize, color, x, y, recycle);
        } else {
            imageSaveCallback.onFail(new NullPointerException("无法保存"));
        }
    }

    public static void saveImageWithImageWaterMaskIntoGallery(Activity activity, ImageSaveCallback imageSaveCallback,
                                                              final Bitmap watermark,
                                                              final int x,
                                                              final int y,
                                                              final int alpha,
                                                              final boolean recycle) {
        PicturePreView picturePreView = getCurrentImageView(activity);
        if (picturePreView != null) {
            picturePreView.saveImageWithImageWaterMaskIntoGallery(imageSaveCallback, watermark, x, y, alpha, recycle);
        } else {
            imageSaveCallback.onFail(new NullPointerException("无法保存"));
        }
    }

    public static File getCurrentImageFile(Activity activity) {
        PicturePreView picturePreView = getCurrentImageView(activity);
        if (picturePreView != null) {
            return picturePreView.getCurrentImageFile();
        }
        return null;
    }

    public static int getCurrentIndex(Activity activity) {
        PicturePreView picturePreView = getCurrentImageView(activity);
        if (picturePreView != null) {
            return picturePreView.getCurrentIndex();
        }
        return 0;
    }

    public static PicturePreView getCurrentImageView(Activity activity) {
        final ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        if (viewGroup != null) {
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof PicturePreView) {
                    return (PicturePreView) view;
                }
            }
        }
        return null;
    }

    public static void hide(Activity activity) {
        final ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        PicturePreView picturePreView = null;
        if (viewGroup != null) {
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof PicturePreView) {
                    picturePreView = (PicturePreView) view;
                    break;
                }
            }
        }

        if (picturePreView != null) {
            picturePreView.hide();
        } else {
            release(activity, null);
        }
    }

    static void release(Activity activity, PicturePreView picturePreView) {
        final ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
        if (viewGroup != null && picturePreView != null) {
            viewGroup.removeView(picturePreView);
        }
        if (isFullScreen != PUtils.isFullScreen(activity)) {
            PUtils.toggleFullScreen(activity);
        }
        PUtils.clearMemory(activity);
        open = false;
    }

    public static class Builder {

        private Activity activity;
        private ImageBrowserConfig imageBrowserConfig;

        private Builder(@NonNull Activity activity) {
            this.activity = activity;
            imageBrowserConfig = new ImageBrowserConfig();
        }

        private Activity getActivity() {
            return activity;
        }

        private void release() {
            activity = null;
        }

        public Builder setBackgroundColor(@ColorInt int color) {
            imageBrowserConfig.setBackgroundColor(color);
            return this;
        }

        public Builder setSources(@NonNull List<SourceData> sources) {
            imageBrowserConfig.setSources(sources);
            return this;
        }

        public Builder useBlurBackground() {
            imageBrowserConfig.useBlurBackground();
            return this;
        }

        public Builder useBlurBackground(int blurMaskColor) {
            imageBrowserConfig.useBlurBackground(blurMaskColor);
            return this;
        }

        public Builder setShowIndicator(boolean showIndicator) {
            imageBrowserConfig.setShowIndicator(showIndicator);
            return this;
        }

        public Builder openEnterAnim(Rect enterRect) {
            imageBrowserConfig.setEnterRect(enterRect);
            return this;
        }

        public Builder openExitAnim() {
            imageBrowserConfig.setExitAnimEnable(true);
            return this;
        }

        public Builder openExitAnim(Rect exitRect) {
            imageBrowserConfig.setExitAnimEnable(true);
            imageBrowserConfig.setExitRect(exitRect);
            return this;
        }

        public Builder openDragBack(Rect exitRect) {
            imageBrowserConfig.setExitRect(exitRect);
            imageBrowserConfig.setDragBackEnable(true);
            return this;
        }

        public Builder openDragBack() {
            imageBrowserConfig.setDragBackEnable(true);
            return this;
        }

        public Builder setFulScreenMode(boolean fulScreenMode) {
            imageBrowserConfig.setFulScreenMode(fulScreenMode);
            return this;
        }

        public Builder setFailureRetry(boolean failClickReLoad) {
            imageBrowserConfig.setFailureRetry(failClickReLoad);
            return this;
        }

        public Builder setOnClickListener(View.OnClickListener onClickListener) {
            imageBrowserConfig.setOnClickListener(onClickListener);
            return this;
        }

        public Builder setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
            imageBrowserConfig.setOnLongClickListener(onLongClickListener);
            return this;
        }

        public Builder setBottomCustomMaskView(View bottomCustomMaskView) {
            imageBrowserConfig.setBottomCustomMaskView(bottomCustomMaskView);
            return this;
        }

        public Builder setOpenShowPosition(int openShowPosition) {
            imageBrowserConfig.setOpenShowPosition(openShowPosition);
            return this;
        }

        public void open() {
            if (open) {
                return;
            }
            final ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();

            if (viewGroup == null || imageBrowserConfig == null ||
                    PUtils.isNullOrEmpty(imageBrowserConfig.getSources())) {
                throw new NullPointerException();
            }

            if (imageBrowserConfig.isUseBlurBackground()) {
                Bitmap bitmap = PUtils.fastBlur(activity, imageBrowserConfig.getBlurMaskColor());
                if (bitmap != null) {
                    imageBrowserConfig.setBlurDrawable(new BitmapDrawable(activity.getResources(), bitmap));
                }
            }

            final PicturePreView picturePreView = new PicturePreView(activity);

            picturePreView.setImageBrowserConfig(imageBrowserConfig);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);

            viewGroup.addView(picturePreView, layoutParams);
            picturePreView.open();

            picturePreView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFullScreen = PUtils.isFullScreen(activity);
                    if (!isFullScreen && imageBrowserConfig.isFulScreenMode()) {
                        PUtils.setFullScreen(activity);
                    }
                }
            }, DEFAULT_ANIMATOR_TIME);

            open = true;
        }
    }
}
