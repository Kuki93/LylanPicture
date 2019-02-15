package cn.yaoxi.slavingpicture.loader;

import android.net.Uri;

public class SourceData {
    private Uri source;
    private Uri thumbnail;
    private int sWidth;
    private int sHeight;
    private int vWidth;
    private int vHeight;

    public SourceData(String source) {
        this.source = Uri.parse(source);
    }

    public SourceData(String source, String thumbnail) {
        this.source = Uri.parse(source);
        this.thumbnail = Uri.parse(thumbnail);
    }

    public SourceData(Uri source) {
        this.source = source;
    }

    public SourceData(Uri source, Uri thumbnail) {
        this.source = source;
        this.thumbnail = thumbnail;
    }

    public SourceData(Uri source, Uri thumbnail, int sWidth, int sHeight, int vWidth, int vHeight) {
        this.source = source;
        this.thumbnail = thumbnail;
        this.sWidth = sWidth;
        this.sHeight = sHeight;
        this.vWidth = vWidth;
        this.vHeight = vHeight;
    }

    public Uri getSource() {
        return source;
    }

    public void setSource(Uri source) {
        this.source = source;
    }

    public Uri getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Uri thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getsWidth() {
        return sWidth;
    }

    public void setsWidth(int sWidth) {
        this.sWidth = sWidth;
    }

    public int getsHeight() {
        return sHeight;
    }

    public void setsHeight(int sHeight) {
        this.sHeight = sHeight;
    }

    public int getvWidth() {
        return vWidth;
    }

    public void setvWidth(int vWidth) {
        this.vWidth = vWidth;
    }

    public int getvHeight() {
        return vHeight;
    }

    public void setvHeight(int vHeight) {
        this.vHeight = vHeight;
    }
}
