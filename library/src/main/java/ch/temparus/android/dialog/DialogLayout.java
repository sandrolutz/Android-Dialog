package ch.temparus.android.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import ch.temparus.android.dialog.holder.Holder;
import ch.temparus.android.dialog.holder.HolderAdapter;
import ch.temparus.android.dialog.holder.ListViewHolder;
import ch.temparus.android.dialog.holder.ViewHolder;
import ch.temparus.android.dialog.listeners.*;

/**
 * DialogLayout is used internally by {@link Dialog}
 *
 * @author Sandro Lutz
 */
@SuppressLint("ViewConstructor")
class DialogLayout extends LinearLayout {

    // Determine whether the resources are set or not
    private static final int INVALID = -1;
    private static final int DIM_COLOR = 0x60000000;

    private final ViewGroup mDecorView; // activity root view
    private final int[] mMargin = new int[4];
    private final int[] mPadding = new int[4];
    private View mTopView;
    private View mBottomView;
    private View mHeaderView;
    private View mFooterView;
    private ViewGroup mHeaderContainer;
    private ViewGroup mFooterContainer;
    private BoundedFrameLayout mContentContainer;
    private int mCollapsedHeight;
    private int mBackgroundColorResourceId;
    private int mInAnimation;
    private int mOutAnimation;
    private Dialog.Gravity mGravity;
    private Holder mHolder; // Content
    private boolean mIsFooterAlwaysVisible;
    private boolean mIsCancelable;
    private boolean mIsShowing;
    private Dialog.State mState = Dialog.State.SETTLING;
    private Dialog.State mSettlingState = mState;
    private boolean mIsDismissing;
    private float mMaxScroll = INVALID;
    private Dialog mDialog;
    private OnItemClickListener mOnItemClickListener;
    private ch.temparus.android.dialog.listeners.OnClickListener mOnClickListener;
    private OnDismissListener mOnDismissListener;
    private OnCancelListener mOnCancelListener;
    private OnStateChangeListener mOnStateChangeListener;
    private int mTouchSlop;
    private int mActivePointerId = INVALID;
    private float mLastMotionY; // position of the last motion event
    private float mInitialMotionY; // position on action down motion event
    private float mLastMotionDeltaY;
    private Scroller mScroller; // calculates smooth scroll animation

    DialogLayout(Dialog dialog, Dialog.Builder builder) {
        super(builder.context);

        final Resources res = getResources();

        LinearLayout.LayoutParams helperLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        FrameLayout.LayoutParams contentLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mDialog = dialog;

        mHolder = (builder.holder == null) ? new ListViewHolder() : builder.holder;
        mHeaderView = builder.headerView;
        mFooterView = builder.footerView;

        mIsFooterAlwaysVisible = builder.isFooterAlwaysVisible;
        mIsCancelable = builder.isCancelable;
        mGravity = builder.gravity;
        mCollapsedHeight = (mGravity != Dialog.Gravity.CENTER) ? builder.collapsedHeight : INVALID;
        mBackgroundColorResourceId = builder.backgroundColorResourceId;

        mInAnimation = (builder.inAnimation == INVALID) ? getInAnimation(mGravity) : builder.inAnimation;
        mOutAnimation = (builder.outAnimation == INVALID) ? getOutAnimation(mGravity) : builder.outAnimation;

        System.arraycopy(builder.padding, 0, mPadding, 0, mPadding.length);

        mOnItemClickListener = builder.onItemClickListener;
        mOnClickListener = builder.onClickListener;
        mOnDismissListener = builder.onDismissListener;
        mOnCancelListener = builder.onCancelListener;
        mOnStateChangeListener = mHolder.getOnStateChangeListener();

        int minimumMargin = res.getDimensionPixelSize(R.dimen.dialog_min_margin);
        for (int i = 0; i < mMargin.length; i++) {
            mMargin[i] = getMargin(mGravity, builder.margin[i], minimumMargin);
        }

        mDecorView = (ViewGroup) ((Activity) getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        mTopView = new View(builder.context);
        mContentContainer = new BoundedFrameLayout(builder.context);
        mBottomView = new View(builder.context);

        // set maximum dialog width depending on gravity
        switch (mGravity) {
            case BOTTOM:
                mContentContainer.setBoundedWidth((builder.maxWidth != INVALID) ? builder.maxWidth : res.getDimensionPixelSize(R.dimen.dialog_max_width_bottom));
                break;
            case CENTER:
                mContentContainer.setBoundedWidth((builder.maxWidth != INVALID) ? builder.maxWidth : res.getDimensionPixelSize(R.dimen.dialog_max_width_center));
                break;
        }
        mContentContainer.setBoundedHeight(builder.maxHeight); // if INVALID, it will be ignored

        initContentView();
        initPosition();
        initCancelable();

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        setId(R.id.dialog_layout);

        mTopView.setId(R.id.dialog_top_view);
        mBottomView.setId(R.id.dialog_bottom_view);
        mContentContainer.setId(R.id.dialog_content_container);

        addView(mTopView, helperLayoutParams);
        addView(mContentContainer, contentLayoutParams);
        addView(mBottomView, helperLayoutParams);

        if (builder.isBackgroundDimEnabled) {
            setBackgroundColor(DIM_COLOR);
        }

        mHolder.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (isShowing()) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_UP:
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                if (mIsCancelable) {
                                    onBackPressed();
                                }
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    if (view == DialogLayout.this) {
                        mIsShowing = true;
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    mIsShowing = false;
                }
            });
        }

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

        mScroller = new Scroller(builder.context);
    }

    /**
     * It adds the dialog view into rootView which is decorView of activity
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        mDecorView.addView(this);
        if (mCollapsedHeight != INVALID) {
            collapseInternal(true);
        } else {
            expandInternal(true);
        }
        Context context = getContext();
        Animation inAnim = AnimationUtils.loadAnimation(context, this.mInAnimation);
        mContentContainer.startAnimation(inAnim);

        mContentContainer.requestFocus();
    }

    /**
     * It basically check if the rootView contains the dialog view.
     *
     * @return true if the dialog is visible
     */
    public boolean isShowing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return this.isAttachedToWindow();
        }
        return mIsShowing;
    }

    /**
     * It is called when to dismiss the dialog, either by calling dismiss() method or with cancellable
     */
    public void dismiss() {
        if (mIsDismissing) {
            return;
        }
        Context context = mDecorView.getContext();
        Animation outAnim = AnimationUtils.loadAnimation(context, this.mOutAnimation);
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDecorView.post(new Runnable() {
                    @Override
                    public void run() {
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
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void abortAnimation() {
        mScroller.forceFinished(true);
    }

    private int getInAnimation(Dialog.Gravity gravity) {
        switch (gravity) {
            case BOTTOM:
                return R.animator.bottom_slide_in;
            case CENTER:
                return R.animator.center_fade_in;
            default:
                return INVALID;
        }
    }

    private int getOutAnimation(Dialog.Gravity gravity) {
        switch (gravity) {
            case BOTTOM:
                return R.animator.bottom_slide_out;
            case CENTER:
                return R.animator.center_fade_out;
            default:
                return INVALID;
        }
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
                if (event.getAction() == MotionEvent.ACTION_DOWN && v != mContentContainer) {
                    if (mOnCancelListener != null) {
                        mOnCancelListener.onCancel(mDialog);
                    }
                    dismiss();
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

    private void initContentView() {
        int convertedGravity = getGravity();
        View contentView = createView(LayoutInflater.from(getContext()));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, convertedGravity
        );
        params.setMargins(mMargin[0], mMargin[1], mMargin[2], mMargin[3]);
        contentView.setLayoutParams(params);
        getHolderView().setPadding(mPadding[0], mPadding[1], mPadding[2], mPadding[3]);

        mContentContainer.addView(contentView);
        mHeaderContainer = (ViewGroup) mContentContainer.findViewById(R.id.header_container);
        mFooterContainer = (ViewGroup) mContentContainer.findViewById(R.id.footer_container);
    }

    /**
     * it is called when the content view is created
     *
     * @param inflater used to inflate the content of the dialog
     * @return content view
     */
    private View createView(LayoutInflater inflater) {
        mHolder.setBackgroundColor(mBackgroundColorResourceId);
        View view = mHolder.getView(inflater, this);

        if (mHolder instanceof ViewHolder) {
            assignClickListenerRecursively(view);
        }

        mContentContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        assignClickListenerRecursively(mHeaderView);
        mHolder.addHeader(mHeaderView);

        assignClickListenerRecursively(mFooterView);
        mHolder.addFooter(mFooterView);

        if (mHolder instanceof HolderAdapter) {
            ((HolderAdapter) mHolder).setOnItemClickListener(new OnHolderListener() {
                @Override
                public void onItemClick(Object item, View view, int position) {
                    if (mOnItemClickListener == null) {
                        return;
                    }
                    mOnItemClickListener.onItemClick(mDialog, item, view, position);
                }
            });
        }
        return view;
    }

    /**
     * Loop among the views in the hierarchy and assign listener to them
     */
    private void assignClickListenerRecursively(View parent) {
        if (parent == null) {
            return;
        }

        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            int childCount = viewGroup.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                View child = viewGroup.getChildAt(i);
                assignClickListenerRecursively(child);
            }
        }
        setClickListener(parent);
    }

    /**
     * It is used to set a click listener on view that have a valid id associated
     */
    private void setClickListener(final View view) {
        if (view.getId() == INVALID) {
            return;
        }
        // AdapterView does not support click listener
        if (view instanceof AdapterView) {
            return;
        }

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener == null) {
                    return;
                }
                mOnClickListener.onClick(mDialog, view);
            }
        });
    }

    private int getMargin(Dialog.Gravity gravity, int margin, int minimumMargin) {
        switch (gravity) {
            case BOTTOM:
                return (margin == INVALID) ? 0 : margin;
            case CENTER:
                return (margin == INVALID) ? minimumMargin : margin;
            default:
                return 0;
        }
    }

    private int getGravity() {
        switch (mGravity) {
            case BOTTOM:
                return Gravity.BOTTOM;
            default:
                return Gravity.CENTER;
        }
    }

    private void setState(Dialog.State state) {
        if (this.mState == Dialog.State.EXPANDED || this.mState == Dialog.State.COLLAPSED) {
            mSettlingState = this.mState;
        }
        this.mState = state;

        if (mOnStateChangeListener != null) {
            switch (state) {
                case EXPANDED:
                    mOnStateChangeListener.onExpanded(mDialog);
                    break;
                case COLLAPSED:
                    mOnStateChangeListener.onCollapsed(mDialog);
                    break;
            }
        }
    }

    /**
     * Dismiss the dialog when the user press the back button
     */
    public void onBackPressed() {
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(mDialog);
        }
        dismiss();
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
