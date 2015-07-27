package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

/**
 * Interface for listening to the dismiss event of {@link Dialog}.
 *
 * @author Sandro Lutz
 */
public interface OnDismissListener {

    /**
     * Called when the dialog has been dismissed.
     * @param dialog dialog instance
     */
    void onDismiss(Dialog dialog);
}
