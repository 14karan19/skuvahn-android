package com.skuvahn.utilities;

import android.text.TextUtils;

import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.player.IPlayerManager;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;

import java.util.HashMap;
import java.util.Map;

public class CustomManager extends GSYVideoBaseManager {

    public static String TAG = "GSYVideoManager";
    public static final int SMALL_ID = com.shuyu.gsyvideoplayer.R.id.ad_small_id;

    public static final int FULLSCREEN_ID = com.shuyu.gsyvideoplayer.R.id.ad_full_id;
    private static Map<String, CustomManager> sMap = new HashMap<>();

    public CustomManager() {
        init();
    }

    @Override
    protected IPlayerManager getPlayManager() {
        return new IjkPlayerManager();
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos(String key) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onCompletion();
        }
        getCustomManager(key).releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public void onPause(String key) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public void onResume(String key) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public void onResume(String key, boolean seek) {
        if (getCustomManager(key).listener() != null) {
            getCustomManager(key).listener().onVideoResume(seek);
        }
    }


    /**
     * 单例管理器
     */
    public static synchronized Map<String, CustomManager> instance() {
        return sMap;
    }

    /**
     * 单例管理器
     */
    public static synchronized CustomManager getCustomManager(String key) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalStateException("key not be empty");
        }
        CustomManager customManager = sMap.get(key);
        if (customManager == null) {
            customManager = new CustomManager();
            sMap.put(key, customManager);
        }
        return customManager;
    }

    public static void onPauseAll() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                header.getValue().onPause(header.getKey());
            }
        }
    }

    public static void onResumeAll() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                header.getValue().onResume(header.getKey());
            }
        }
    }

    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作
     */
    public static void onResumeAll(boolean seek) {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                header.getValue().onResume(header.getKey(), seek);
            }
        }
    }

    public static void clearAllVideo() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, CustomManager> header : sMap.entrySet()) {
                CustomManager.releaseAllVideos(header.getKey());
            }
        }
        sMap.clear();
    }

    public static void removeManager(String key) {
        sMap.remove(key);
    }
}
