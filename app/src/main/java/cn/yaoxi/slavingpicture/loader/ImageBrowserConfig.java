package cn.yaoxi.slavingpicture.loader;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

public class ImageBrowserConfig {

    public static int DEFAULT_BLUR_MASK_COLOR = 0;
    private int backgroundColor;
    private int indicatorBgColor;
    private int indicatorTextColor;
    private List<SourceData> sources;
    private int blurMaskColor;
    private boolean showIndicator;
    private Rect enterRect;
    private Rect exitRect;
    private boolean exitAnimEnable;
    private boolean dragBackEnable;
    private boolean isFulScreenMode;
    private boolean isFailureRetry;
    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;
    private View bottomCustomMaskView;
    private FrameLayout.LayoutParams layoutParams;
    private int openShowPosition;
    private Typeface typeface;
    private int mOffscreenPageLimit;
    private Drawable blurDrawable;

    public ImageBrowserConfig() {
        backgroundColor = 0x4D000000;
        indicatorBgColor = 0x99000000;
        indicatorTextColor = Color.WHITE;
        showIndicator = true;
        isFulScreenMode = true;
        isFailureRetry = true;
        openShowPosition = 0;
        typeface = Typeface.DEFAULT;
        mOffscreenPageLimit = 1;
        blurMaskColor = -1;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        if (blurMaskColor > 0 && blurMaskColor == this.backgroundColor) {
            blurMaskColor = backgroundColor;
        }
        this.backgroundColor = backgroundColor;
    }

    public int getIndicatorBgColor() {
        return indicatorBgColor;
    }

    public void setIndicatorBgColor(int indicatorBgColor) {
        this.indicatorBgColor = indicatorBgColor;
    }

    public int getIndicatorTextColor() {
        return indicatorTextColor;
    }

    public void setIndicatorTextColor(int indicatorTextColor) {
        this.indicatorTextColor = indicatorTextColor;
    }

    public List<SourceData> getSources() {
        return sources;
    }

    public void setSources(List<SourceData> sources) {
        this.sources = sources;
    }

    public boolean isUseBlurBackground() {
        return blurMaskColor >= 0;
    }

    public void useBlurBackground(int blurMaskColor) {
        if (blurMaskColor > 0) {
            this.blurMaskColor = blurMaskColor;
        }
        if (this.blurMaskColor <= 0) {
            this.blurMaskColor = backgroundColor;
        }
        if (this.blurMaskColor <= 0) {
            this.blurMaskColor = 0;
        }
    }

    public void useBlurBackground() {
        this.blurMaskColor = 0;
    }

    public boolean isShowIndicator() {
        return showIndicator;
    }

    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
    }

    public boolean isEnterAnimEnable() {
        return enterRect != null;
    }

    public boolean isExitAnimEnable() {
        return exitAnimEnable;
    }

    public void setExitAnimEnable(boolean exitAnimEnable) {
        this.exitAnimEnable = exitAnimEnable;
    }

    public boolean isDragBackEnable() {
        return dragBackEnable;
    }

    public void setDragBackEnable(boolean dragBackEnable) {
        this.dragBackEnable = dragBackEnable;
    }

    public boolean isFulScreenMode() {
        return isFulScreenMode;
    }

    public void setFulScreenMode(boolean fulScreenMode) {
        isFulScreenMode = fulScreenMode;
    }

    public boolean isFailureRetry() {
        return isFailureRetry;
    }

    public void setFailureRetry(boolean failureRetry) {
        isFailureRetry = failureRetry;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public View.OnLongClickListener getOnLongClickListener() {
        return onLongClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public View getBottomCustomMaskView() {
        return bottomCustomMaskView;
    }

    public FrameLayout.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    public void setBottomCustomMaskView(View bottomCustomMaskView) {
        this.bottomCustomMaskView = bottomCustomMaskView;
    }

    public void setBottomCustomMaskView(View bottomCustomMaskView, FrameLayout.LayoutParams layoutParams) {
        this.bottomCustomMaskView = bottomCustomMaskView;
        this.layoutParams = layoutParams;
    }

    public int getOpenShowPosition() {
        return openShowPosition;
    }

    public void setOpenShowPosition(int openShowPosition) {
        this.openShowPosition = openShowPosition;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public int getOffscreenPageLimit() {
        return mOffscreenPageLimit;
    }

    public void setOffscreenPageLimit(int mOffscreenPageLimit) {
        this.mOffscreenPageLimit = mOffscreenPageLimit;
    }

    public Rect getEnterRect() {
        return enterRect;
    }

    public void setEnterRect(Rect enterRect) {
        this.enterRect = enterRect;
    }

    public Rect getExitRect() {
        return exitRect;
    }

    public void setExitRect(Rect exitRect) {
        this.exitRect = exitRect;
    }

    Drawable getBlurDrawable() {
        return blurDrawable;
    }

    void setBlurDrawable(Drawable blurDrawable) {
        this.blurDrawable = blurDrawable;
    }

    public int getBlurMaskColor() {
        return blurMaskColor;
    }
}
