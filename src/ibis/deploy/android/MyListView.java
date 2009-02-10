package ibis.deploy.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MyListView extends LinearLayout implements
        GestureDetector.OnGestureListener {
    private GestureDetector mGestureDetector;

    private ListView mListView;

    public MyListView(Context context, AttributeSet attributes) {
        super(context, attributes);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setIsLongpressEnabled(false);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setItemsCanFocus(true);
        this.addView(mListView, new LinearLayout.LayoutParams(350,
                LayoutParams.FILL_PARENT));
    }

    public void setAdapter(ListAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return true;
    }

    public void onLongPress(MotionEvent e) {
        // empty
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        int scrollWidth = mListView.getWidth() - this.getWidth();
        if ((this.getScrollX() >= 0) && (this.getScrollX() <= scrollWidth)
                && (scrollWidth > 0)) {
            int moveX = (int) distanceX;
            if (((moveX + this.getScrollX()) >= 0)
                    && ((Math.abs(moveX) + Math.abs(this.getScrollX())) <= scrollWidth)) {
                this.scrollBy(moveX, 0);
            } else {
                if (distanceX >= 0) {
                    this.scrollBy(scrollWidth
                            - Math.max(Math.abs(moveX), Math.abs(this
                                    .getScrollX())), 0);
                } else {
                    this.scrollBy(-Math.min(Math.abs(moveX), Math.abs(this
                            .getScrollX())), 0);
                }
            }
        }
        return true;
    }

    public void onShowPress(MotionEvent e) {
        // empty
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return true;
    }

}