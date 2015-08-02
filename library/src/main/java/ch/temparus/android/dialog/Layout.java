package ch.temparus.android.dialog;

import android.view.View;

/**
 * DialogLayout is used internally by {@link Dialog}
 *
 * @author Sandro Lutz
 */
interface Layout {

    /**
     * It adds the dialog view into rootView which is decorView of activity
     */
    public void show();

    /**
     * It basically check if the rootView contains the dialog view.
     *
     * @return true if the dialog is visible
     */
    public boolean isShowing();

    /**
     * It is called when to dismiss the dialog, either by calling dismiss() method or with cancellable
     */
    public void dismiss();

    public View getHeaderView();

    public View getFooterView();

    public View getHolderView();

    public View getDialogView();

    public void cancel();
}
