package ch.temparus.android.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import ch.temparus.android.dialog.listeners.OnStateChangeListener;

/**
 * DialogLayout is used internally by {@link Dialog}
 *
 * @author Sandro Lutz
 */
@SuppressLint("ViewConstructor")
class DialogLayout extends Layout {

    private static final int DIM_COLOR = 0x66000000;
    private static final float DIM_FACTOR = 0.4f;
    
    private View mTopView;
    private View mBottomView;
    private ViewGroup mHeaderContainer;
    private ViewGroup mFooterContainer;
    private int mCollapsedHeight;
    private boolean mIsBackgroundDimEnabled;
    private boolean mIsFooterAlwaysVisible;
    private Dialog.State mState = Dialog.State.SETTLING;
    private Dialog.State mSettlingState = mState;
    private float mMaxScroll = INVALID;
    private OnStateChangeListener mOnStateChangeListener;
    private int mTouchSlop;
    private int mActivePointerId = INVALID;
    private float mLastMotionY; // position of the last motion event
    private float mInitialMotionY; // position on action down motion event
    private float mLastMotionDeltaY;
    private Scroller mScroller; // calculates smooth scroll animation
    private int mPreviousStatusBarColor;

    DialogLayout(Dialog dialog, Dialog.Builder builder) {
        super(dialog, builder);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (mState == Dialog.State.EXPANDED) {
                    expandInternal(true);
                } else {
                    collapseInternal(true);
                }
            }
        });
    }

    /**
     * It adds the dialog view into rootView which is decorView of activity
     */
    @SuppressLint("NewApi")
    public void show() {
        if (isShowing()) {
            return;
        }
        mDecorView.addView(this);

        if (mIsBackgroundDimEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ((Activity) getContext()).getWindow();
            mPreviousStatusBarColor = window.getStatusBarColor();
            int red = Color.red(mPreviousStatusBarColor);
            int green = Color.green(mPreviousStatusBarColor);
            int blue = Color.blue(mPreviousStatusBarColor);
            red = (int) Math.max(red - (red * DIM_FACTOR), 0);
            green = (int) Math.max(green - (green * DIM_FACTOR), 0);
            blue = (int) Math.max(blue - (blue * DIM_FACTOR), 0);
            window.setStatusBarColor(Color.rgb(red, green, blue));
        }

        if (mCollapsedHeight != INVALID) {
            collapseInternal(true);
        } else {
            expandInternal(true);
        }
        if (mInAnimation != INVALID) {
            Context context = getContext();
            Animation inAnim = AnimationUtils.loadAnimation(context, mInAnimation);
            mContentContainer.startAnimation(inAnim);
        }
        mContentContainer.requestFocus();
    }

    /**
     * It is called when to dismiss the dialog, either by calling dismiss() method or with cancellable
     */
    public void dismiss() {
        if (mIsDismissing) {
            return;
        }
        Context context = getContext();
        Animation outAnim = AnimationUtils.loadAnimation(context, mOutAnimation);
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDecorView.post(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        if (mIsBackgroundDimEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ((Activity) getContext()).getWindow().setStatusBarColor(mPreviousStatusBarColor);
                        }
                        mDecorView.removeView(DialogLayout.this);
                        mIsDismissing = false;
                        if (mOnDismissListener != null) {
                            mOnDismissListener.onDismiss(mDialog);
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContentContainer.startAnimation(outAnim);
        mIsDismissing = true;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getFooterView() {
        return mFooterView;
    }

    public View getHolderView() {
        return mHolder.getInflatedView();
    }

    public View getDialogView() {
        return mContentContainer;
    }

    public void cancel() {
        if (mOnCancelListener == null || mOnCancelListener.onCancel(mDialog)) {
            dismiss();
        }
    }

    public void expand() {
        setState(Dialog.State.SETTLING);
        expandInternal(false);
    }

    private void expandInternal(boolean immediately) {
        mSettlingState = Dialog.State.EXPANDED;

        final int deltaY = -1 * getScrollY();

        scrollBy(deltaY, immediately);
    }

    public void collapse() {
        setState(Dialog.State.SETTLING);
        collapseInternal(false);
    }

    private void collapseInternal(boolean immediately) {
        mSettlingState = Dialog.State.COLLAPSED;

        if (mMaxScroll == INVALID) {
            return;
        }
        final int deltaY = (int) mMaxScroll - getScrollY();

        scrollBy(deltaY, immediately);
    }

    private void scrollBy(final int deltaY, boolean immediately) {
        if (immediately) {
            scrollBy(0, deltaY);
            if (mIsFooterAlwaysVisible) {
                mFooterContainer.setTranslationY(mFooterContainer.getTranslationY() + deltaY);
            }
            setState(mSettlingState);
        } else {
            int duration = 300;
            mScroller.startScroll(getScrollX(), getScrollY(), 0, deltaY, duration);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void abortAnimation() {
        mScroller.forceFinished(true);
    }

    /**
     * It is called to set whether the dialog is cancellable by pressing back button or
     * touching the black overlay
     */
    private void initCancelable() {
        if (!mIsCancelable) {
            return;
        }

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && mState != Dialog.State.DRAGGING && v != mContentContainer) {
                    cancel();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * It is called when the dialog has to be positioned
     */
    private void initPosition() {
        switch (mGravity) {
            case BOTTOM:
                mBottomView.setVisibility(View.GONE);
                mTopView.setVisibility(View.VISIBLE);
                break;
            default:
                mBottomView.setVisibility(View.VISIBLE);
                mTopView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void initContentView(Dialog.Builder builder) {
        final Resources res = getResources();

        switch (builder.gravity) {
            case BOTTOM:
                mInAnimation = (builder.inAnimation == INVALID) ? R.anim.t_dialog__bottom_slide_in : builder.inAnimation;
                mOutAnimation = (builder.outAnimation == INVALID) ? R.anim.t_dialog__bottom_slide_out : builder.outAnimation;
                break;
            default: // CENTER
                mInAnimation = (builder.inAnimation == INVALID) ? R.anim.t_dialog__center_fade_in : builder.inAnimation;
                mOutAnimation = (builder.outAnimation == INVALID) ? R.anim.t_dialog__center_fade_out : builder.outAnimation;
        }

        mIsBackgroundDimEnabled = builder.isBackgroundDimEnabled;
        mIsFooterAlwaysVisible = builder.isFooterAlwaysVisible;
        mCollapsedHeight = (mGravity != Dialog.Gravity.CENTER) ? builder.collapsedHeight : INVALID;
        mOnStateChangeListener = mHolder.getOnStateChangeListener();

        LinearLayout.LayoutParams helperLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        FrameLayout.LayoutParams contentLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mTopView = new View(builder.context);
        mContentContainer = new BoundedFrameLayout(builder.context);
        mBottomView = new View(builder.context);

        // set maximum dialog width depending on gravity
        switch (mGravity) {
            case BOTTOM:
                ((BoundedFrameLayout) mContentContainer).setBoundedWidth((builder.maxWidth != INVALID) ? builder.maxWidth : res.getDimensionPixelSize(R.dimen.t_dialog__max_width_bottom));
                break;
            case CENTER:
                ((BoundedFrameLayout) mContentContainer).setBoundedWidth((builder.maxWidth != INVALID) ? builder.maxWidth : res.getDimensionPixelSize(R.dimen.t_dialog__max_width_center));
                break;
        }
        ((BoundedFrameLayout) mContentContainer).setBoundedHeight(builder.maxHeight); // if INVALID, it will be ignored

        super.initContentView(builder);
        initPosition();
        initCancelable();

        mHeaderContainer = (ViewGroup) mContentContainer.findViewById(R.id.t_dialog__header_container);
        mFooterContainer = (ViewGroup) mContentContainer.findViewById(R.id.t_dialog__footer_container);

        if (mHeaderView == null && builder.title != null && builder.title.length() > 0) {
            mHeaderView = LayoutInflater.from(builder.context).inflate(R.layout.t_dialog__header, mHeaderContainer, false);
            ((TextView) mHeaderView.findViewById(R.id.t_dialog__title)).setText(builder.title);
            mHolder.addHeader(mHeaderView);
        }

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        setId(R.id.t_dialog__layout);

        mTopView.setId(R.id.t_dialog__top_view);
        mBottomView.setId(R.id.t_dialog__bottom_view);
        mContentContainer.setId(R.id.t_dialog__content_container);

        addView(mTopView, helperLayoutParams);
        addView(mContentContainer, contentLayoutParams);
        addView(mBottomView, helperLayoutParams);

        if (mIsBackgroundDimEnabled) {
            setBackgroundColor(DIM_COLOR);
        }

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

        mScroller = new Scroller(builder.context);
    }

    private void setState(Dialog.State state) {
        if (state == mState) {
            return;
        }
        if (mState == Dialog.State.EXPANDED || mState == Dialog.State.COLLAPSED) {
            mSettlingState = mState;
        }
        mState = state;

        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.onStateChanged(mDialog, mState);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (mCollapsedHeight == INVALID || mHolder.isInterceptTouchEventDisallowed()) {
            return false;
        }

        final int action = motionEvent.getAction() & MotionEventCompat.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            if (mState == Dialog.State.DRAGGING) {
                setState(Dialog.State.SETTLING);

                if (mLastMotionDeltaY < 0) {
                    expand();
                } else {
                    collapse();
                }

                mActivePointerId = INVALID;
            }
            return false;
        }

        // Nothing more to do here if state has already changed to dragging
        if (action != MotionEvent.ACTION_DOWN && mState == Dialog.State.DRAGGING) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (mActivePointerId == INVALID) {
                    onSecondaryPointerUp(motionEvent);
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(motionEvent, mActivePointerId);
                final float y = MotionEventCompat.getY(motionEvent, pointerIndex);
                final float dy = y - mLastMotionY;
                float yDiff = Math.abs(y - mInitialMotionY);

                if (yDiff > mTouchSlop * 2) {
                    mInitialMotionY = y;
                    yDiff = 0;
                }

                if (yDiff > mTouchSlop / 2) {
                    if (mState == Dialog.State.EXPANDED && dy < 0) {
                        break;
                    }
                    setState(Dialog.State.DRAGGING);
                    mLastMotionY = dy > 0 ? mInitialMotionY + mTouchSlop / 2 : mInitialMotionY - mTouchSlop / 2;
                }
                if (mState == Dialog.State.DRAGGING) {
                    mLastMotionDeltaY = dy;
                    // Scroll to follow the motion event
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(motionEvent, mActivePointerId);
                    float position = MotionEventCompat.getY(motionEvent, activePointerIndex);

                    performDrag(position, motionEvent);
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                /*
                 * Remember location of touch down.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = mInitialMotionY = motionEvent.getY();
                mActivePointerId = MotionEventCompat.getPointerId(motionEvent, 0);


                if (mState == Dialog.State.SETTLING) {
                    abortAnimation();
                    setState(Dialog.State.DRAGGING);
                }
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(motionEvent);
                break;
        }

        return mState == Dialog.State.DRAGGING;
    }

    @Override
    public boolean onTouchEvent(@android.support.annotation.NonNull MotionEvent motionEvent) {
        // do not handle touch events starting on an edge immediately
        if (mCollapsedHeight == INVALID || (motionEvent.getAction() == MotionEvent.ACTION_DOWN && motionEvent.getEdgeFlags() != 0)) {
            return false;
        }

        final int action = motionEvent.getAction();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                /*
                 * Remember location of touch down.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = mInitialMotionY = motionEvent.getY();
                mActivePointerId = MotionEventCompat.getPointerId(motionEvent, 0);

                if (mState == Dialog.State.SETTLING) {
                    abortAnimation();
                    setState(Dialog.State.DRAGGING);
                } else {
                    setState(Dialog.State.DRAGGING);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID) {
                    onSecondaryPointerUp(motionEvent);
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(motionEvent, mActivePointerId);
                final float y = MotionEventCompat.getY(motionEvent, pointerIndex);
                final float dy = y - mLastMotionY;
                final float yDiff = Math.abs(y - mLastMotionY);

                if (mState != Dialog.State.DRAGGING && mState != Dialog.State.SETTLING) {
                    if (yDiff > mTouchSlop) {
                        setState(Dialog.State.DRAGGING);
                        mLastMotionY = dy > 0 ? mInitialMotionY + mTouchSlop : mInitialMotionY - mTouchSlop;
                    }
                }

                // Important! Note that state can be set above.
                if (mState == Dialog.State.DRAGGING) {
                    mLastMotionDeltaY = dy;
                    // Scroll to follow the motion event
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(motionEvent, mActivePointerId);
                    float position = MotionEventCompat.getY(motionEvent, activePointerIndex);

                    performDrag(position, motionEvent);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // use fall through effect...
            case MotionEvent.ACTION_UP:
                if (mState == Dialog.State.DRAGGING) {

                    setState(Dialog.State.SETTLING);

                    if (mLastMotionDeltaY < 0) {
                        expand();
                    } else {
                        collapse();
                    }

                    mActivePointerId = INVALID;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(motionEvent);
                mLastMotionY = MotionEventCompat.getY(motionEvent, index);
                mActivePointerId = MotionEventCompat.getPointerId(motionEvent, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(motionEvent);
                break;
        }

        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // Active pointer was going up. Choose next pointer as active pointer and adjust values.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private void performDrag(float position, MotionEvent motionEvent) {
        final float deltaY = mLastMotionY - position;
        mLastMotionY = position;

        boolean endPositionReached = false;

        float oldScrollY = getScrollY();
        float scrollY = oldScrollY + deltaY;

        float maxScroll = (mContentContainer.getHeight() > mCollapsedHeight) ? -1 * (mContentContainer.getHeight() - mCollapsedHeight) : 0;

        if (scrollY < maxScroll) {
            scrollY = maxScroll;
            setState(Dialog.State.COLLAPSED);
        } else if (scrollY > 0) {
            scrollY = 0;
            setState(Dialog.State.EXPANDED);
            endPositionReached = true;
        }

        if (mIsFooterAlwaysVisible) {
            mFooterContainer.setTranslationY(scrollY);
        }
        scrollTo(getScrollX(), (int) scrollY);
        ViewCompat.postInvalidateOnAnimation(this);

        if (endPositionReached) {
            MotionEvent newMotionEvent = MotionEvent.obtain(motionEvent);
            newMotionEvent.setAction(MotionEvent.ACTION_CANCEL);
            dispatchTouchEvent(newMotionEvent);

            newMotionEvent = MotionEvent.obtain(motionEvent);
            newMotionEvent.setAction(MotionEvent.ACTION_DOWN);
            dispatchTouchEvent(newMotionEvent);
        }
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                scrollTo(x, y);
                if (mIsFooterAlwaysVisible) {
                    mFooterContainer.setTranslationY(y);
                }
            }

            // Keep on drawing until the animation has finished.
            ViewCompat.postInvalidateOnAnimation(this);
            return;
        }

        // Done with scroll, clean up state.
        completeScroll();
    }

    /**
     * This method is being called when scroll animation has finished
     */
    private void completeScroll() {
        if (mState == Dialog.State.SETTLING) {
            setState(mSettlingState);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = mContentContainer.getMeasuredHeight();
        int unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int verticalMargin = mMargin[1] + mMargin[3]; // mMargin[1] == marginTop & mMargin[3] == marginBottom
        View contentView = mHolder.getInflatedView();

        mHeaderContainer.measure(widthMeasureSpec, unspecified);
        mFooterContainer.measure(widthMeasureSpec, unspecified);
        contentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height - mHeaderContainer.getMeasuredHeight() - mFooterContainer.getMeasuredHeight() - verticalMargin, MeasureSpec.AT_MOST));

        int finalHeight = mHeaderContainer.getMeasuredHeight() + contentView.getMeasuredHeight() + mFooterContainer.getMeasuredHeight() + verticalMargin;

        if (finalHeight < height) {
            mContentContainer.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
            if (mGravity == Dialog.Gravity.BOTTOM) {
                mTopView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height - finalHeight, MeasureSpec.EXACTLY));
            } else {
                int helperHeightMeasureSpec = MeasureSpec.makeMeasureSpec((height - finalHeight) / 2, MeasureSpec.EXACTLY);
                mTopView.measure(widthMeasureSpec, helperHeightMeasureSpec);
                mBottomView.measure(widthMeasureSpec, helperHeightMeasureSpec);
            }
        } else {
            contentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height - mHeaderContainer.getMeasuredHeight() - mFooterContainer.getMeasuredHeight(), MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        float previousMaxScroll = mMaxScroll;
        mMaxScroll = (mContentContainer.getHeight() > mCollapsedHeight) ? -1 * (mContentContainer.getHeight() - mCollapsedHeight) : 0;

        if (previousMaxScroll == INVALID) {
            if (mCollapsedHeight != INVALID) {
                collapseInternal(true);
            } else {
                expandInternal(true);
            }
        }
    }
}
