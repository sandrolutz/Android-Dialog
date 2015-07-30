package ch.temparus.android.dialog;

import android.content.Context;
import android.support.annotation.*;
import android.view.LayoutInflater;
import android.view.View;
import ch.temparus.android.dialog.holder.Holder;
import ch.temparus.android.dialog.listeners.OnCancelListener;
import ch.temparus.android.dialog.listeners.OnClickListener;
import ch.temparus.android.dialog.listeners.OnDismissListener;
import ch.temparus.android.dialog.listeners.OnItemClickListener;

import java.util.Arrays;

/**
 * Dialog component for Android supporting bottom sheet dialog and center dialog.
 *
 * @author Sandro Lutz
 */
public class Dialog {

    public enum State {DRAGGING, SETTLING, COLLAPSED, EXPANDED}

    // Determine whether the resources are set or not
    private static final int INVALID = -1;
    private DialogLayout mRootView;

    private Dialog(Builder builder) {
        mRootView = new DialogLayout(this, builder);
    }

    /**
     * It adds the dialog view into the decorView of activity
     */
    public void show() {
        mRootView.show();
    }

    /**
     * It basically check if the rootView contains the dialog view.
     *
     * @return true if the dialog is attached to the view hierarchy
     */
    @SuppressWarnings("unused")
    public boolean isShowing() {
        return mRootView.isShowing();
    }

    /**
     * It is called when to dismiss the dialog, either by calling dismiss() method or with cancellable
     */
    public void dismiss() {
        mRootView.dismiss();
    }

    @SuppressWarnings("unused")
    public View findViewById(@IdRes int resId) {
        return mRootView.getDialogView().findViewById(resId);
    }

    @SuppressWarnings("unused")
    public View getHeaderView() {
        return mRootView.getHeaderView();
    }

    @SuppressWarnings("unused")
    public View getFooterView() {
        return mRootView.getFooterView();
    }

    @SuppressWarnings("unused")
    public View getHolderView() {
        return mRootView.getHolderView();
    }

    /**
     * Custom values for gravity
     */
    public enum Gravity {
        BOTTOM, CENTER
    }

    /**
     * Use this builder to create a dialog
     */
    public static final class Builder {
        protected final int[] margin = new int[4];
        protected final int[] padding = new int[4];

        protected Context context;
        protected Holder holder;
        protected View footerView;
        protected View headerView;
        protected CharSequence title;
        protected Gravity gravity = Gravity.BOTTOM;
        protected int maxWidth = INVALID;
        protected int maxHeight = INVALID;
        protected int collapsedHeight = INVALID;
        protected OnItemClickListener onItemClickListener;
        protected OnClickListener onClickListener;
        protected OnDismissListener onDismissListener;
        protected OnCancelListener onCancelListener;

        protected boolean isFooterAlwaysVisible = true;
        protected boolean isBackgroundDimEnabled = true;
        protected boolean isCancelable = true;
        protected int backgroundColorResourceId = android.R.color.white;
        protected int inAnimation = INVALID;
        protected int outAnimation = INVALID;

        /**
         * Initialize the builder with a valid context in order to inflate the dialog
         */
        public Builder(Context context) {
            if (context == null) {
                throw new NullPointerException("Context must not be null");
            }
            this.context = context;
            Arrays.fill(margin, INVALID);
        }

        /**
         * Set the footer view using the id of the layout resource
         */
        @SuppressWarnings("unused")
        public Builder setFooter(@LayoutRes int resId) {
            this.footerView = LayoutInflater.from(context).inflate(resId, null);
            return this;
        }

        /**
         * Set the footer view using a view
         */
        @SuppressWarnings("unused")
        public Builder setFooter(View view) {
            this.footerView = view;
            return this;
        }

        /**
         * Set the header view using the id of the layout resource
         */
        @SuppressWarnings("unused")
        public Builder setHeader(@LayoutRes int resId) {
            this.headerView = LayoutInflater.from(context).inflate(resId, null);
            return this;
        }

        /**
         * Set the header view using a view
         */
        @SuppressWarnings("unused")
        public Builder setHeader(View view) {
            this.headerView = view;
            return this;
        }

        /**
         * Set title to display in the header container using the default layout provided by this library.
         */
        @SuppressWarnings("unused")
        public Builder setTitle(@StringRes int resId) {
            this.title = context.getString(resId);
            return this;
        }

        /**
         * Set title to display in the header container using the default layout provided by this library.
         */
        @SuppressWarnings("unused")
        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        /**
         * Define if the dialog should dim the background around the dialog
         */
        @SuppressWarnings("unused")
        public Builder setBackgroundDimEnabled(boolean isBackgroundDimEnabled) {
            this.isBackgroundDimEnabled = isBackgroundDimEnabled;
            return this;
        }

        /**
         * Define if the footer view should always be visible.
         *
         * Note: This options is only used with collapsible bottom sheet dialogs!
         */
        @SuppressWarnings("unused")
        public Builder setFooterAlwaysVisible(boolean isFooterAlwaysVisible) {
            this.isFooterAlwaysVisible = isFooterAlwaysVisible;
            return this;
        }

        /**
         * Define if the dialog is cancelable and should be closed when back pressed or click outside is pressed
         */
        @SuppressWarnings("unused")
        public Builder setCancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        /**
         * Set the content of the dialog
         */
        @SuppressWarnings("unused")
        public Builder setContentHolder(Holder holder) {
            this.holder = holder;
            return this;
        }

        /**
         * Set background color for your dialog. If no resource is passed 'white' will be used
         */
        @SuppressWarnings("unused")
        public Builder setBackgroundColorResourceId(@ColorRes int resId) {
            this.backgroundColorResourceId = resId;
            return this;
        }

        /**
         * Set the gravity you want the dialog to have among the ones that are provided
         */
        public Builder setGravity(Gravity gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * Set max width of the dialog
         * @param maxWidth max width in pixels
         */
        @SuppressWarnings("unused")
        public Builder setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Set max height of the dialog
         * @param maxHeight max height in pixels
         */
        @SuppressWarnings("unused")
        public Builder setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Set collapsed height of the dialog. This will be the initial visible height of the dialog.
         * If not set, the dialog will be expanded
         * @param collapsedHeight collapsed height in pixels
         */
        @SuppressWarnings("unused")
        public Builder setCollapsedHeight(int collapsedHeight) {
            this.collapsedHeight = collapsedHeight;
            return this;
        }

        /**
         * Customize the in animation by passing an animation resource
         */
        @SuppressWarnings("unused")
        public Builder setInAnimation(@AnimRes int resId) {
            this.inAnimation = resId;
            return this;
        }

        /**
         * Customize the out animation by passing an animation resource
         */
        @SuppressWarnings("unused")
        public Builder setOutAnimation(@AnimRes int resId) {
            this.outAnimation = resId;
            return this;
        }

        /**
         * Add margins to your dialog. They are set to 0 except when gravity is center. In that case basic margins
         * are applied
         */
        @SuppressWarnings("unused")
        public Builder setMargins(int left, int top, int right, int bottom) {
            this.margin[0] = left;
            this.margin[1] = top;
            this.margin[2] = right;
            this.margin[3] = bottom;
            return this;
        }

        /**
         * Set paddings for the dialog content
         */
        @SuppressWarnings("unused")
        public Builder setPadding(int left, int top, int right, int bottom) {
            this.padding[0] = left;
            this.padding[1] = top;
            this.padding[2] = right;
            this.padding[3] = bottom;
            return this;
        }

        /**
         * Set an item click listener when {@link ch.temparus.android.dialog.holder.ListViewHolder} or
         * {@link ch.temparus.android.dialog.holder.RecyclerViewHolder} is chosen.
         * In that way you can have callbacks when one of your items is clicked
         */
        @SuppressWarnings("unused")
        public Builder setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
            return this;
        }

        /**
         * Set a global click listener to you dialog in order to handle all the possible click events. You can then
         * identify the view by using its id and handle the correct behaviour
         */
        @SuppressWarnings("unused")
        public Builder setOnClickListener(OnClickListener listener) {
            this.onClickListener = listener;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setOnDismissListener(OnDismissListener listener) {
            this.onDismissListener = listener;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setOnCancelListener(OnCancelListener listener) {
            this.onCancelListener = listener;
            return this;
        }

        /**
         * Create the dialog using this builder
         */
        public Dialog create() {
            return new Dialog(this);
        }
    }
}
