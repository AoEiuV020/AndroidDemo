package cc.aoeiuv020.demo.call;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class SmallVideoViewAdapter extends VideoViewAdapter {
    private final static Logger log = LoggerFactory.getLogger(SmallVideoViewAdapter.class);

    private int mExceptedUid;

    public SmallVideoViewAdapter(Context context, int exceptedUid, HashMap<Integer, UserStatusData> userList, VideoViewEventListener listener) {
        super(context, userList, listener);
        mExceptedUid = exceptedUid;
        log.debug("SmallVideoViewAdapter " + (mExceptedUid & 0xFFFFFFFFL));
    }

    @Override
    protected void customizedInit(HashMap<Integer, UserStatusData> userList, boolean force) {
        mUsers.clear();
        mUsers.addAll(userList.values());
        mUsers.remove(userList.get(mExceptedUid));

        if (force || mItemWidth == 0 || mItemHeight == 0) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
            mItemWidth = outMetrics.widthPixels / 4;
            mItemHeight = outMetrics.heightPixels / 4;
        }
    }

    @Override
    public void notifyUiChanged(HashMap<Integer, UserStatusData> userList, int uidExcepted) {
        mUsers.clear();

        mExceptedUid = uidExcepted;

        log.debug("notifyUiChanged " + " " + (uidExcepted & 0xFFFFFFFFL) + " " + userList);
        mUsers.addAll(userList.values());
        mUsers.remove(userList.get(mExceptedUid));

        notifyDataSetChanged();
    }

    public int getExceptedUid() {
        return mExceptedUid;
    }
}
