package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

import java.io.Serializable;

/**
 * Interface for listening to the dismiss event of {@link Dialog}.
 *
 * @author Sandro Lutz
 */
public interface OnDismissListener extends Serializable {

    /**
     * Called when the dialog has been dismissed.
     * @param dialog dialog instance
     */
    void onDismiss(Dialog dialog);
}
