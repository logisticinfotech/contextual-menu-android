package simple.circledraganimation;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnViewCrossed, MenuSelectionListener {

    private static final String TAG = "MainActivity";

    private RelativeLayout relativeParent;       // outer main layout
    private RelativeLayout rlTouchLayout;       // outer main layout
    private RelativeLayout rlMenuView;   // drag layout
    private RelativeLayout rlCenterView;   // drag layout

    private View viewCircle;             // draggable center circle
    private TextView tvResult;             // used for show drag selection result
    private RadioGroup radioGroup;

    private List<TextView> textViewList = new ArrayList<>();
    private List<ImageView> menuItemViewList = new ArrayList<>();
    private List<ImageView> indicatorViewList = new ArrayList<>();

    private int DOTS_COUNT = 0;
    private int DOTS_POSITION_ANGLE = 90;
    private int START_ANGLE = 180;
    private boolean setVisible = false;
    private int lastSelectedPosition = -1;
    private float pos_center_view[] = new float[2];

    private int[] icons = new int[]{
            R.mipmap.facebook,
            R.mipmap.instagram,
            R.mipmap.google_plus,
            R.mipmap.twitter,
            R.mipmap.dribbble,
            R.mipmap.flickr,
            R.mipmap.linkedin,
            R.mipmap.pinterest,
            R.mipmap.tumblr,
    };

    private String[] names = new String[]{
            "Facebook",
            "Instagram",
            "Google+",
            "Twitter",
            "Dribbble",
            "Flickr",
            "LinkedIn",
            "Pinterest",
            "Tumblr",
    };

    private boolean isLongPressDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* main outer view */
        relativeParent = (RelativeLayout) findViewById(R.id.relativeParent);
        /* rl menu main */
        rlTouchLayout = (RelativeLayout) findViewById(R.id.rlTouchLayout);
        rlMenuView = (RelativeLayout) findViewById(R.id.rl_MenuView);
        rlCenterView = (RelativeLayout) findViewById(R.id.rl_center);
        viewCircle = findViewById(R.id.view_circle);
        tvResult = (TextView) findViewById(R.id.tvResult);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        relativeParent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                Log.e("GestureDetector", "Longpress detected");

                if (setVisible) {
                    rlMenuView.setVisibility(View.VISIBLE);
                    rlTouchLayout.setBackgroundResource(R.color.menu_background);
                    isLongPressDetected = true;
                }

            }
        });

        Log.e("GestureDetector", "--> "+gestureDetector.isLongpressEnabled() );

        relativeParent.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                gestureDetector.onTouchEvent(event);

                if (event.getX() >= getResources().getDimension(R.dimen._10sdp) && event.getX() <= relativeParent.getWidth() - getResources().getDimension(R.dimen._10sdp) &&
                        event.getY() >= getResources().getDimension(R.dimen._10sdp) && event.getY() <= relativeParent.getHeight() - getResources().getDimension(R.dimen._10sdp)) {
                    setVisible = true;
                    //pass touch event

                    viewCircle.dispatchTouchEvent(event);

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            lastSelectedPosition = -1;

                            float centerX = event.getX() - rlMenuView.getWidth() / 2;
                            float centerY = event.getY() - rlMenuView.getHeight() / 2;

                            rlMenuView.setX(centerX);
                            rlMenuView.setY(centerY);

                            setMenuItemsPosition(relativeParent, rlMenuView, centerX, centerY);

                            break;
                        case MotionEvent.ACTION_MOVE:

                            break;

                        case MotionEvent.ACTION_UP:

                            if (isLongPressDetected) {
                                if (lastSelectedPosition != -1) {
                                    onMenuSelected(lastSelectedPosition, textViewList.get(lastSelectedPosition).getText().toString());
                                } else {
                                    onNoMenuSelected();
                                }
                            }

                            setVisible = false;
                            isLongPressDetected = false;
                            setMenusPosition(Angle.CENTER);
                            rlMenuView.setVisibility(View.INVISIBLE);
                            rlTouchLayout.setBackgroundResource(R.color.tranparent);
                            hideTextViews();

                            break;
                    }

                } else {

                    if (event.getAction() == MotionEvent.ACTION_UP) {

                        if (isLongPressDetected) onNoMenuSelected();

                        setVisible = false;
                        isLongPressDetected = false;
                        setMenusPosition(Angle.CENTER);
                        rlMenuView.setVisibility(View.INVISIBLE);
                        rlTouchLayout.setBackgroundResource(R.color.tranparent);
                        hideTextViews();
                    }

                }

                return false;
            }

        });


        createMenuItem(names[0], icons[0]);
        createMenuItem(names[1], icons[1]);
        createMenuItem(names[2], icons[2]);

//        animateMenuItem();
        initCircleDrag();
        setMenusPosition(Angle.CENTER);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                for (int i = 0; i < menuItemViewList.size(); i++) {
                    menuItemViewList.get(i).setVisibility(View.INVISIBLE);
                    indicatorViewList.get(i).setVisibility(View.INVISIBLE);
                }

                clearAnimation();

                textViewList.clear();
                menuItemViewList.clear();
                indicatorViewList.clear();

                rlCenterView.removeAllViewsInLayout();
                rlCenterView.invalidate();

                switch (checkedId) {
                    case R.id.radio2:
                        createMenuItem(names[0], icons[0]);
                        createMenuItem(names[1], icons[1]);
                        break;

                    case R.id.radio3:
                        createMenuItem(names[0], icons[0]);
                        createMenuItem(names[1], icons[1]);
                        createMenuItem(names[2], icons[2]);
                        break;

                    case R.id.radio4:
                        createMenuItem(names[0], icons[0]);
                        createMenuItem(names[1], icons[1]);
                        createMenuItem(names[2], icons[2]);
                        createMenuItem(names[3], icons[3]);
                        break;

                    case R.id.radio5:
                        createMenuItem(names[0], icons[0]);
                        createMenuItem(names[1], icons[1]);
                        createMenuItem(names[2], icons[2]);
                        createMenuItem(names[3], icons[3]);
                        createMenuItem(names[4], icons[4]);
                        break;

                    case R.id.radio6:
                        createMenuItem(names[0], icons[0]);
                        createMenuItem(names[1], icons[1]);
                        createMenuItem(names[2], icons[2]);
                        createMenuItem(names[3], icons[3]);
                        createMenuItem(names[4], icons[4]);
                        createMenuItem(names[5], icons[5]);
                        break;

                    case R.id.radio7:
                        createMenuItem(names[0], icons[0]);
                        createMenuItem(names[1], icons[1]);
                        createMenuItem(names[2], icons[2]);
                        createMenuItem(names[3], icons[3]);
                        createMenuItem(names[4], icons[4]);
                        createMenuItem(names[5], icons[5]);
                        createMenuItem(names[6], icons[6]);
                        break;

                }

                initCircleDrag();
                setMenusPosition(Angle.CENTER);

            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        initCircleDrag();
    }

    private void initCircleDrag() {
        final int radius = (int) getResources().getDimension(R.dimen._80sdp);
        final CircleDrag circleDrag = new CircleDrag(this);
        circleDrag.init(viewCircle, this, radius, indicatorViewList);
    }

    /* used for set default selection layout in center */
    private void setMenusPosition(Angle angle) {

        Log.d(TAG, "setMenusPosition: angle   " + DOTS_COUNT);
        Log.d(TAG, "setMenusPosition: angle   " + angle.name());

        Rect rectMain = new Rect();
        relativeParent.getGlobalVisibleRect(rectMain);

        float centerX = viewCircle.getX(); /*- rectMain.left;*/
        float centerY = viewCircle.getY(); /*- rectMain.top;*/

        pos_center_view[0] = centerX;
        pos_center_view[1] = centerY;

        float currentRadius1 = (rlMenuView.getWidth() / 2) - getResources().getDimension(R.dimen._35sdp);

        if (DOTS_COUNT > 3) {
            DOTS_POSITION_ANGLE = 360 / DOTS_COUNT;
        } else {
            DOTS_POSITION_ANGLE = 90;
        }

        switch (angle) {
            case CENTER:
                START_ANGLE = 180;
                if (DOTS_COUNT < 3)
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                break;

            case BOTTOM:
                START_ANGLE = 180;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 2 ? (180 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 1)
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                break;

            case TOP:
                START_ANGLE = 0;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 2 ? (180 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 1)
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                break;

            case LEFT:
                START_ANGLE = 270;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 2 ? (180 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 1)
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                break;

            case RIGHT:
                START_ANGLE = 90;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 2 ? (180 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 1)
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                break;

            case LEFT_TOP:
                START_ANGLE = 0;
                DOTS_POSITION_ANGLE = 45;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 3 ? (90 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 3)
                    DOTS_POSITION_ANGLE = DOTS_POSITION_ANGLE + (DOTS_POSITION_ANGLE / 2);
                if (DOTS_COUNT < 3) {
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                }
                break;

            case LEFT_BOTTOM:
                START_ANGLE = 270;
                DOTS_POSITION_ANGLE = 45;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 3 ? (90 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 3)
                    DOTS_POSITION_ANGLE = DOTS_POSITION_ANGLE + (DOTS_POSITION_ANGLE / 2);
                if (DOTS_COUNT < 3) {
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                }
                break;

            case RIGHT_TOP:
                START_ANGLE = 90;
                DOTS_POSITION_ANGLE = 45;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 3 ? (90 / DOTS_COUNT) : DOTS_POSITION_ANGLE;
                if (DOTS_COUNT > 3)
                    DOTS_POSITION_ANGLE = DOTS_POSITION_ANGLE + (DOTS_POSITION_ANGLE / 2);
                if (DOTS_COUNT < 3) {
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                }
                break;

            case RIGHT_BOTTOM:
                START_ANGLE = 180;
                DOTS_POSITION_ANGLE = 45;
                DOTS_POSITION_ANGLE = DOTS_COUNT > 3 ? (90 / DOTS_COUNT) : DOTS_POSITION_ANGLE;

                if (DOTS_COUNT > 3)
                    DOTS_POSITION_ANGLE = DOTS_POSITION_ANGLE + (DOTS_POSITION_ANGLE / 2);
                if (DOTS_COUNT < 3) {
                    START_ANGLE = START_ANGLE + (DOTS_POSITION_ANGLE / 2);
                }
                break;

        }

        Log.d(TAG, "setMenusPosition: angle : START_ANGLE : " + START_ANGLE);
        Log.d(TAG, "setMenusPosition: angle :  DOTS_ANGLE : " + DOTS_POSITION_ANGLE);

        for (int i = 0; i < DOTS_COUNT; i++) {

            int cX;
            int cY;
            cX = (int) (centerX + currentRadius1 * Math.cos((START_ANGLE) * Math.PI / 180));
            cY = (int) (centerY + currentRadius1 * Math.sin((START_ANGLE) * Math.PI / 180));
            START_ANGLE += DOTS_POSITION_ANGLE;

            menuItemViewList.get(i).setX(cX);
            menuItemViewList.get(i).setY(cY);
            indicatorViewList.get(i).setX(cX);
            indicatorViewList.get(i).setY(cY);

            Rect rect1 = new Rect();
            indicatorViewList.get(i).getGlobalVisibleRect(rect1);

            if (centerX < cX && (cX - centerX > 4)) {
                textViewList.get(i).setX(rect1.left - rectMain.left + getResources().getDimension(R.dimen._42sdp));
            } else if (centerX > cX && (centerX - cX > 4)) {
                textViewList.get(i).setX(rect1.left - rectMain.left - getResources().getDimension(R.dimen._10sdp) - (textViewList.get(i).getWidth()));
            } else {
                textViewList.get(i).setX(rect1.left - rectMain.left + (textViewList.get(i).getWidth() / 2) - (textViewList.get(i).getWidth() / 2));
            }

            if (centerY < cY && (cY - centerY > 4)) {
                textViewList.get(i).setY(rect1.top - rectMain.top + getResources().getDimension(R.dimen._42sdp));
            } else if (centerY > cY && centerY - cY > 4) {
                textViewList.get(i).setY(rect1.top - rectMain.top - getResources().getDimension(R.dimen._10sdp) - (textViewList.get(i).getHeight()));
            } else {
                textViewList.get(i).setY(rect1.top - rectMain.top + (menuItemViewList.get(i).getHeight() / 2) - (textViewList.get(i).getHeight() / 2));
            }

            Log.d(TAG, "setMenusPosition: angle : START_ANGLE1 : " + START_ANGLE);

        }

        animateMenuItem();

        Log.d(TAG, "setMenusPosition: ------------------ :");

    }

    private void animateMenuItem() {
        Animation meuAnimation = AnimationUtils.loadAnimation(this, R.anim.default_blue);
        for (int i = 0; i < menuItemViewList.size(); i++) {
            menuItemViewList.get(i).startAnimation(meuAnimation);
        }
    }

    private void clearAnimation() {
        for (int i = 0; i < menuItemViewList.size(); i++) {
            menuItemViewList.get(i).clearAnimation();
        }
    }

    public void setMenuItemsPosition(View parentView, View menuLayoutView, float x, float y) {

        //p for parent
        float pl, pr, pb, pt;
        //c for child
        float cl, cr, cb, ct;

        int[] v1_coords = new int[2];
        parentView.getLocationOnScreen(v1_coords);

        pl = 0;
        pr = parentView.getWidth();
        pt = 0;
        pb = parentView.getHeight();

        cl = menuLayoutView.getX();
        ct = menuLayoutView.getY();
        cr = menuLayoutView.getWidth() + cl;
        cb = menuLayoutView.getHeight() + ct;

        if (cl < pl) {
            if (ct < pt && cl < pl) {
                setMenusPosition(Angle.LEFT_TOP);
            } else if (cb > pb && cl < pl) {
                setMenusPosition(Angle.LEFT_BOTTOM);
            } else {
                setMenusPosition(Angle.LEFT);
            }
        } else if (cr > pr) {
            if (ct < pt && cr > pr) {
                setMenusPosition(Angle.RIGHT_TOP);
            } else if (cb > pb && cr > pr) {
                setMenusPosition(Angle.RIGHT_BOTTOM);
            } else {
                setMenusPosition(Angle.RIGHT);
            }
        } else if (ct < pt) {
            setMenusPosition(Angle.TOP);
        } else if (cb > pb) {
            setMenusPosition(Angle.BOTTOM);
        } else {
            setMenusPosition(Angle.CENTER);
        }

    }

    @Override
    public void onMenuSelected(int position, String selectedText) {
        tvResult.setText(String.format("Selected Item : %s", selectedText));
    }

    @Override
    public void onNoMenuSelected() {
        // TODO when no menu selected.....
        tvResult.setText(String.format("Selected Item : %s", "None"));
    }

    @Override
    public void OnViewTouched(int flag) {
        Log.d(TAG, "onSelect: ");

        lastSelectedPosition = -1;

        for (int i = 0; i < DOTS_COUNT; i++) {

            if (isLongPressDetected && flag == indicatorViewList.get(i).getId()) {
                lastSelectedPosition = i;

                if ((int) pos_center_view[0] > (int) indicatorViewList.get(i).getX()
                        && (pos_center_view[0] - indicatorViewList.get(i).getX() > 4)) {
                    menuItemViewList.get(i).setX(indicatorViewList.get(i).getX() - getResources().getDimension(R.dimen._6sdp));
                } else if ((int) pos_center_view[0] < (int) indicatorViewList.get(i).getX()
                        && (indicatorViewList.get(i).getX() - pos_center_view[0] > 4)) {
                    menuItemViewList.get(i).setX(indicatorViewList.get(i).getX() + getResources().getDimension(R.dimen._6sdp));
                } else {
                    menuItemViewList.get(i).setX(indicatorViewList.get(i).getX());
                }

                if ((int) pos_center_view[1] > (int) indicatorViewList.get(i).getY()
                        && (pos_center_view[1] - indicatorViewList.get(i).getY() > 4)) {
                    menuItemViewList.get(i).setY(indicatorViewList.get(i).getY() - getResources().getDimension(R.dimen._6sdp));
                } else if ((int) pos_center_view[1] < (int) indicatorViewList.get(i).getY()
                        && (indicatorViewList.get(i).getY() - pos_center_view[1] > 4)) {
                    menuItemViewList.get(i).setY(indicatorViewList.get(i).getY() + getResources().getDimension(R.dimen._6sdp));
                } else {
                    menuItemViewList.get(i).setY(indicatorViewList.get(i).getY());
                }

                textViewList.get(i).setVisibility(View.VISIBLE);

            } else {
                menuItemViewList.get(i).setX(indicatorViewList.get(i).getX());
                menuItemViewList.get(i).setY(indicatorViewList.get(i).getY());

                textViewList.get(i).setVisibility(View.INVISIBLE);
            }

        }

    }

    public void createMenuItem(String text, @DrawableRes int drawableId) {

        textViewList.add(createTextView(relativeParent, text));
        menuItemViewList.add(createMenuItem(rlCenterView, drawableId));
        indicatorViewList.add(createMenuItem(rlCenterView, R.drawable.trans_circle));

        DOTS_COUNT = textViewList.size();
    }

    public TextView createTextView(RelativeLayout parentView, String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setPadding((int) getResources().getDimension(R.dimen._6sdp)
                , (int) getResources().getDimension(R.dimen._3sdp)
                , (int) getResources().getDimension(R.dimen._6sdp)
                , (int) getResources().getDimension(R.dimen._3sdp)
        );
        textView.setBackgroundResource(R.drawable.draw_bg_roundcorner_menu);
        textView.setText(text);
        textView.setVisibility(View.INVISIBLE);
        textView.setTextSize(10);

        textView.setId((textViewList.size() + 1) * 13);

        if (parentView != null) {
            parentView.addView(textView);
        }

        return textView;
    }

    public ImageView createMenuItem(RelativeLayout parentView, @DrawableRes int drawableId) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen._34sdp), (int) getResources().getDimension(R.dimen._34sdp)));
        imageView.setImageResource(drawableId);
        imageView.setVisibility(View.VISIBLE);

        if (parentView != null) {
            parentView.addView(imageView);
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        imageView.setId((textViewList.size() + 1) * 11);

        return imageView;
    }

    private void hideTextViews() {
        for (int i = 0; i < textViewList.size(); i++) {
            textViewList.get(i).setVisibility(View.INVISIBLE);
        }
    }

}
