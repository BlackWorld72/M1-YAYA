package fr.allanh.yaya.swipe;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnSwipeTouchListener implements View.OnTouchListener {

    private GestureDetector gestureDetector;

    public OnSwipeTouchListener(View view) {
        GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
                float xDiff = e2.getX() - e1.getX();
                float yDiff = e2.getY() - e1.getY();

                try {
                    if (Math.abs(xDiff) > Math.abs(yDiff)) {
                        if (Math.abs(xDiff) > 100 && Math.abs(velocityX) > 100) {
                            if (xDiff > 0) {
                                onSwipeRight();
                            }
                            else {
                                onSwipeLeft();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };

        gestureDetector = new GestureDetector(listener);
        view.setOnTouchListener(this);
    }

    protected abstract void onSwipeLeft();

    protected abstract void onSwipeRight();

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }
}
