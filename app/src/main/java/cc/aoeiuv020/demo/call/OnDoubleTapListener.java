package cc.aoeiuv020.demo.call;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnDoubleTapListener implements OnTouchListener {

    private GestureDetector mGestureDetector;
    private View mView;

    public OnDoubleTapListener(Context c) {
        mGestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        mView = view;
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    public void onDoubleTap(View view, MotionEvent e) {

    }

    public void onSingleTapUp() {

    }

    private final class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            OnDoubleTapListener.this.onDoubleTap(mView, e);
            return super.onDoubleTap(e);
        }

        public boolean onSingleTapUp(MotionEvent e) {
            OnDoubleTapListener.this.onSingleTapUp();
            return super.onSingleTapUp(e);
        }
    }
}
