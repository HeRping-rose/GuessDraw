package com.andriod.guessdraw.utils;

import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LeanCloudUtil {

    public static final LeanCloudUtil sharedInstance=new LeanCloudUtil();
//     获取广告图片
        public static void getAdImage(LeanCloudListener listener) {

            LCQuery<LCObject> query = new LCQuery<>("Advertisement");
            query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                @Override
                public void onSubscribe(Disposable d) {
                //     告诉监听者开始加载
                    if (listener != null) {
                        listener.onStartLoading();
                    }

                }

                @Override
                public void onNext(List<LCObject> lcObjects) {
                    // 获取到数据
                    LCObject lcObject = lcObjects.get(0);
                    //     获取imgUrl
                    String imgUrl = lcObject.getString("image_url");

                // 将图片回调给监听者
                    if(listener != null) {
                        listener.onGetAdImgSuccess(imgUrl, null);
                    }
                }

                @Override
                public void onError(Throwable e) {
                // 获取数据失败
                    if(listener != null) {
                        listener.onGetAdImgSuccess(null, e);
                    }

                }

                @Override
                public void onComplete() {

                }
            });

        }

//         创建接口
        public interface LeanCloudListener {
            // 获取adImg成功和失败的回调
            void onGetAdImgSuccess(String imgUrl,Throwable e);
            // void onGetAdImgError(Throwable e);

        // 开始加载
            void onStartLoading();

            // 加载完成
        }

        // 其他LeanCloud相关方法可以在这里添加

}
