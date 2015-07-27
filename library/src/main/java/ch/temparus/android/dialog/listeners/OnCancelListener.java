package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

/**
 * Interface for listening to the cancel event of {@link Dialog}.
 *
 * @author Sandro Lutz
 */
public interface OnCancelListener {

    /**
     * Called when the dialog has been cancelled.
     * @param dialog dialog instance
     */
    void onCancel(Dialog dialog);
}
