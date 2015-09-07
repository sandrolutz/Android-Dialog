package ch.temparus.android.dialog;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.util.TypedValue;
import android.view.*;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import ch.temparus.android.dialog.holder.Holder;
import ch.temparus.android.dialog.holder.HolderAdapter;
import ch.temparus.android.dialog.holder.ListViewHolder;
import ch.temparus.android.dialog.holder.ViewHolder;
import ch.temparus.android.dialog.listeners.*;
import ch.temparus.android.dialog.utils.ViewUtils;

/**
 * DialogLayout is used internally by {@link Dialog}
 *
 * @author Sandro Lutz
 */
abstract class Layout extends LinearLayout {

    // Determine whether the resources are set or not
    protected static final int INVALID = -1;

    protected final Dialog mDialog;
    protected final ViewGroup mDecorView; // activity root view

    protected final int[] mMargin = new int[4];
    protected final int[] mPadding = new int[4];
    protected View mHeaderView;
    protected View mFooterView;
    protected FrameLayout mContentContainer;
    protected int mBackgroundColorResourceId;
    protected int mInAnimation;
    protected int mOutAnimation;
    protected Dialog.Gravity mGravity;
    protected Holder mHolder; // Content
    protected boolean mIsCancelable;
    protected boolean mIsShowing;
    protected boolean mIsDismissing;
    protected OnItemClickListener mOnItemClickListener;
    protected ch.temparus.android.dialog.listeners.OnClickListener mOnClickListener;
    protected OnDismissListener mOnDismissListener;
    protected OnCancelListener mOnCancelListener;
    protected OnConfirmListener mOnConfirmListener;

    Layout(Dialog dialog, Dialog.Builder builder) {
        super(builder.context);
        mDialog = dialog;
        mDecorView = (ViewGroup) ((Activity) builder.context).getWindow().getDecorView().findViewById(android.R.id.content);
        initDialogLayout(builder);
    }

    Layout(Dialog dialog, Dialog.Builder builder, @LayoutRes int layoutResource) {
        super(builder.context);
        mDialog = dialog;
        mDecorView = (ViewGroup) ((Activity) builder.context).getWindow().getDecorView().findViewById(android.R.id.content);
        LayoutInflater.from(getContext()).inflate(layoutResource, this);
        initDialogLayout(builder);
    }

    /**
     * It adds the dialog view to the rootView of the current activity
     */
    public abstract void show();

    /**
     * It basically checks if the rootView contains the dialog view.
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
    public abstract void dismiss();

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

    private void initDialogLayout(Dialog.Builder builder) {
        mHolder = (builder.holder == null) ? new ListViewHolder() : builder.holder;
        mHeaderView = builder.headerView;
        mFooterView = builder.footerView;

        mGravity = builder.gravity;
        mIsCancelable = builder.isCancelable;
        mBackgroundColorResourceId = builder.backgroundColorResourceId;

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

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            TypedValue typedValue = new TypedValue();
            if (builder.context.getTheme().resolveAttribute(android.R.attr.windowTranslucentStatus, typedValue, true) && typedValue.data == -1) {
                mMargin[1] += (int) (25 * getResources().getDisplayMetrics().density);
            }
        }

        mContentContainer = (FrameLayout) findViewById(R.id.t_dialog__content_container);

        mHolder.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (isShowing()) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_UP:
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                if (mIsCancelable) {
                                    cancel();
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
                    if (view == Layout.this) {
                        mIsShowing = true;
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    mIsShowing = false;
                }
            });
        }

        initContentView(builder);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        setId(R.id.t_dialog__layout);
    }

    protected void initContentView(Dialog.Builder builder) {
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
        if (parent == null || mOnClickListener == null) {
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
        final View.OnClickListener oldListener = ViewUtils.getOnClickListener(view);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldListener != null) {
                    oldListener.onClick(v);
                }

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
            default: // FULLSCREEN
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
}
