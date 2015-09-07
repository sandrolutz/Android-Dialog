package ch.temparus.android.dialog.holder;

import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ch.temparus.android.dialog.listeners.OnStateChangeListener;

import java.io.Serializable;

/**
 * Interface for holding the content view of {@link ch.temparus.android.dialog.Dialog}
 *
 * @author Sandro Lutz
 */
public interface Holder extends Serializable {

    /**
     * Add header view (displayed on top of the content)
     * @param view header view
     */
    void addHeader(View view);

    /**
     * Add footer view (displayed on bottom of the content)
     * @param view footer view
     */
    void addFooter(View view);

    /**
     * Set background color of the dialog. Called by {@link ch.temparus.android.dialog.DialogLayout}
     * @param resId Color resource
     */
    void setBackgroundColor(@ColorRes int resId);

    /**
     * Get view of the {@link ch.temparus.android.dialog.Dialog}.
     * Called by {@link ch.temparus.android.dialog.Dialog}
     * @param inflater LayoutInflater
     * @param parent   Parent in view hierarchy.
     * @return content view of the {@link ch.temparus.android.dialog.Dialog}
     */
    View getView(LayoutInflater inflater, ViewGroup parent);

    /**
     * Get inflated view without header / footer containers.
     * @return inflated view holding the {@link ch.temparus.android.dialog.Dialog}'s content.
     */
    View getInflatedView();

    /**
     * Check whether {@link ch.temparus.android.dialog.Dialog} is allowed to intercept the touch event or not.
     * Called by {@link ch.temparus.android.dialog.DialogLayout}
     * @return true - if intercept touch event is not allowed; false - otherwise
     */
    boolean isInterceptTouchEventDisallowed();

    /**
     * Set OnKeyListener.
     * Called by {@link ch.temparus.android.dialog.Layout} to catch key events for back button.
     * @param keyListener OnKeyListener
     */
    void setOnKeyListener(View.OnKeyListener keyListener);

    /**
     * Get {@link OnStateChangeListener} to catch state changes of {@link ch.temparus.android.dialog.Dialog}.
     * Called by {@link ch.temparus.android.dialog.DialogLayout}
     * @return OnStateChangeListener
     */
    OnStateChangeListener getOnStateChangeListener();
}
