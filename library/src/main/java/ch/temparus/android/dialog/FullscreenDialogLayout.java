package ch.temparus.android.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import ch.temparus.android.dialog.listeners.OnConfirmListener;

import java.util.Arrays;

/**
 * DialogLayout is used internally by {@link Dialog}
 *
 * @author Sandro Lutz
 */
@SuppressLint("ViewConstructor")
class FullscreenDialogLayout extends Layout {

    private OnConfirmListener mOnConfirmListener;

    FullscreenDialogLayout(Dialog dialog, Dialog.Builder builder) {
        super(dialog, builder, R.layout.t_dialog__fullscreen);
    }

    /**
     * It adds the dialog view into rootView which is decorView of activity
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        mDecorView.addView(this);

        if (mInAnimation != INVALID) {
            Context context = getContext();
            Animation inAnim = AnimationUtils.loadAnimation(context, mInAnimation);
            startAnimation(inAnim);
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
                    @Override
                    public void run() {
                        mDecorView.removeView(FullscreenDialogLayout.this);
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
        startAnimation(outAnim);
        mIsDismissing = true;
    }

    @Override
    protected void initContentView(Dialog.Builder builder) {
        Arrays.fill(mMargin, 0);
        mOnConfirmListener = builder.onConfirmListener;
        mContentContainer = (FrameLayout) findViewById(R.id.t_dialog__content_container);
        mInAnimation = (builder.inAnimation == INVALID) ? R.anim.t_dialog__fullscreen_fade_in : builder.inAnimation;
        mOutAnimation = (builder.outAnimation == INVALID) ? R.anim.t_dialog__fullscreen_fade_out : builder.outAnimation;

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

        super.initContentView(builder);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            TypedValue typedValue = new TypedValue();
            if (builder.context.getTheme().resolveAttribute(android.R.attr.windowTranslucentStatus, typedValue, true) && typedValue.data == -1) {
                setPadding(0, (int) (25 * getResources().getDisplayMetrics().density), 0, 0);
            }
        }
    }
}
