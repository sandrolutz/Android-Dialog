package ch.temparus.android.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
class FullscreenDialogLayout extends LinearLayout implements Layout {

    // Determine whether the resources are set or not
    private static final int INVALID = -1;

    private final int[] mMargin = new int[4];
    private final int[] mPadding = new int[4];
    private View mHeaderView;
    private View mFooterView;
    private FrameLayout mContentContainer;
    private int mBackgroundColorResourceId;
    private int mInAnimation;
    private int mOutAnimation;
    private Dialog.Gravity mGravity;
    private Holder mHolder; // Content
    private boolean mIsDismissing;
    private Dialog mDialog;
    private OnItemClickListener mOnItemClickListener;
    private ch.temparus.android.dialog.listeners.OnClickListener mOnClickListener;
    private OnDismissListener mOnDismissListener;
    private OnCancelListener mOnCancelListener;
    private OnConfirmListener mOnConfirmListener;

    FullscreenDialogLayout(Dialog dialog, Dialog.Builder builder) {
        super(dialog.getActivity());

        LayoutInflater.from(getContext()).inflate(R.layout.t_dialog__fullscreen, this);

        mDialog = dialog;

        mHolder = (builder.holder == null) ? new ListViewHolder() : builder.holder;
        mHeaderView = builder.headerView;
        mFooterView = builder.footerView;

        mGravity = builder.gravity;
        mBackgroundColorResourceId = builder.backgroundColorResourceId;

        mInAnimation = (builder.inAnimation == INVALID) ? R.anim.t_dialog__fullscreen_fade_in : builder.inAnimation;
        mOutAnimation = (builder.outAnimation == INVALID) ? R.anim.t_dialog__fullscreen_fade_out : builder.outAnimation;

        System.arraycopy(builder.padding, 0, mPadding, 0, mPadding.length);

        mOnItemClickListener = builder.onItemClickListener;
        mOnClickListener = builder.onClickListener;
        mOnDismissListener = builder.onDismissListener;
        mOnCancelListener = builder.onCancelListener;
        mOnConfirmListener = builder.onConfirmListener;

        int minimumMargin = getResources().getDimensionPixelSize(R.dimen.t_dialog__min_margin);
        for (int i = 0; i < mMargin.length; i++) {
            mMargin[i] = getMargin(mGravity, builder.margin[i], minimumMargin);
        }

        mContentContainer = (FrameLayout) findViewById(R.id.t_dialog__content_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.t_dialog__toolbar);

        if (builder.title != null) {
            toolbar.setTitle(builder.title);
            toolbar.setNavigationIcon(R.drawable.t_dialog__cancel);
        }
        toolbar.inflateMenu(R.menu.t_dialog__fullscreen);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.t_dialog__action_confirm) {
                    if (mOnConfirmListener == null || mOnConfirmListener.onConfirm(mDialog)) {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

        initContentView();

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        setId(R.id.t_dialog__layout);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    /**
     * It adds the dialog view into rootView which is decorView of activity
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        if (mInAnimation != INVALID) {
            Context context = getContext();
            Animation inAnim = AnimationUtils.loadAnimation(context, mInAnimation);
            startAnimation(inAnim);
        }
        requestFocus();
    }

    /**
     * It basically check if the rootView contains the dialog view.
     *
     * @return true if the dialog is visible
     */
    public boolean isShowing() {
        return mDialog.isShowing();
    }

    /**
     * It is called when to dismiss the dialog, either by calling dismiss() method or with cancellable
     */
    public void dismiss() {
        if (mIsDismissing) {
            return;
        }
        if (mInAnimation != INVALID) {
            Context context = getContext();
            Animation outAnim = AnimationUtils.loadAnimation(context, mOutAnimation);
            outAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIsDismissing = false;
                    if (mOnDismissListener != null) {
                        mOnDismissListener.onDismiss(mDialog);
                    }
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.dismissInternal();
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            startAnimation(outAnim);
            mIsDismissing = true;
        } else {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss(mDialog);
            }
            mDialog.dismissInternal();
        }
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

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            cancel();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
