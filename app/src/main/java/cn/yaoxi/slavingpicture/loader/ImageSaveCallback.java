package cn.yaoxi.slavingpicture.loader;

public interface ImageSaveCallback {
    void onSuccess(String path);

    void onFail(Throwable t);
}
