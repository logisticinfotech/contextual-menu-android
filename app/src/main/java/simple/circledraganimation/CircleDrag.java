package simple.circledraganimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import java.util.List;

public class CircleDrag extends View {

    private float dX, dY, defaultX, defaultY;
    private static final String TAG = "CircleDrag";

    public CircleDrag(Context context) {
        super(context);
    }

    public CircleDrag(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleDrag(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(final View dragCircleView, final OnViewCrossed onViewCrossed, final int radius, final List<ImageView> menuList) {

        if(menuList == null || menuList.size() == 0){
            throw new RuntimeException("Empty menu items exception!!!");
        }

        defaultX = dragCircleView.getX();
        defaultY = dragCircleView.getY();

        dragCircleView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View dragView, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = defaultX - event.getRawX();
                        dY = defaultY - event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:

                        float valueX = event.getRawX() + dX;
                        float valueY = event.getRawY() + dY;

                        final float distanceBetweenCenterAndTouch = calculateDistance(new Point(defaultX, defaultY), new Point(valueX, valueY));
                        if (distanceBetweenCenterAndTouch > radius) {

                            final Point circleLineIntersectionPoint = getIntersectionPoints(
                                    new Point(valueX, valueY),
                                    new Point(defaultX, defaultY),
                                    radius);

                            valueX = circleLineIntersectionPoint.getFloatX();
                            valueY = circleLineIntersectionPoint.getFloatY();

                        }

                        dragCircleView.animate().x(valueX).y(valueY).setDuration(0).start();

                        if (onViewCrossed != null) {

                            boolean is_crossed = false;
                            for (int i = 0; i < menuList.size(); i++) {
                                if (checkCollision(dragView, menuList.get(i))) {
                                    is_crossed = true;
                                    Log.d(TAG, "onTouch: menu touched : "+ i);
                                    onViewCrossed.OnViewTouched(menuList.get(i).getId());
                                    break;
                                }
                            }

                            if(!is_crossed){
                                Log.d(TAG, "onTouch: no menu touched");
                                onViewCrossed.OnViewTouched(-1);
                            }
                        }

                        break;

                    case MotionEvent.ACTION_UP:

                        final AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(
                                ObjectAnimator.ofFloat(dragCircleView, View.TRANSLATION_X, dragCircleView.getTranslationX(), 0f),
                                ObjectAnimator.ofFloat(dragCircleView, View.TRANSLATION_Y, dragCircleView.getTranslationY(), 0f));
                        animatorSet.setDuration(1000);
                        animatorSet.setInterpolator(new AttenuatedFluctuation());
                        animatorSet.start();

                        animatorSet.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                dragCircleView.clearAnimation();
                                animatorSet.setInterpolator(null);
                            }
                        });

                        break;
                }
                return true;
            }
        });

    }

    public boolean checkCollision(View dragCircle, View menuItemView) {
        int[] v1_coords = new int[2];
        dragCircle.getLocationOnScreen(v1_coords);
        int v1_w = dragCircle.getWidth();
        int v1_h = dragCircle.getHeight();
        Rect v1_rect = new Rect(v1_coords[0], v1_coords[1], v1_coords[0] + v1_w, v1_coords[1] + v1_h);

        int[] v2_coords = new int[2];
        menuItemView.getLocationOnScreen(v2_coords);
        int v2_w = menuItemView.getWidth();
        int v2_h = menuItemView.getHeight();
        Rect v2_rect = new Rect(v2_coords[0], v2_coords[1], v2_coords[0] + v2_w, v2_coords[1] + v2_h);

        return v1_rect.intersect(v2_rect) || v1_rect.contains(v2_rect) || v2_rect.contains(v1_rect);
    }

    public static Point getIntersectionPoints(Point touchPoint, Point center, double radius) {
        double angle = Math.atan2(touchPoint.getY() - center.getY(), touchPoint.getX() - center.getX());
        return new Point(center.getX() + (radius * Math.cos(angle)), center.getY() + (radius * Math.sin(angle)));
    }

    public float calculateDistance(Point a, Point b) {
        float squareDifference = (float) Math.pow((a.x - b.x), 2);
        float squareDifference2 = (float) Math.pow((a.y - b.y), 2);
        return (float) Math.sqrt(squareDifference + squareDifference2);
    }

    public class AttenuatedFluctuation implements Interpolator {

        public float getInterpolation(float t) {
            return (float) ((-Math.cos(5 * Math.PI * t) / Math.exp(5 * t)) + 1);
        }
    }

    public static class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public float getFloatX() {
            return (float) x;
        }

        public float getFloatY() {
            return (float) y;
        }

        @Override
        public String toString() {
            return "Point [x=" + x + ", y=" + y + "]";
        }

    }

}
