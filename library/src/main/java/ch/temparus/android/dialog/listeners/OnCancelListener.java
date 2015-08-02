package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

import java.io.Serializable;

/**
 * Interface for listening to the cancel event of {@link Dialog}.
 *
 * @author Sandro Lutz
 */
public interface OnCancelListener extends Serializable {

    /**
     * Called when the dialog has been cancelled.
     * @param dialog dialog instance
     * @return true - if dialog can be dismissed; false - otherwise
     */
    boolean onCancel(Dialog dialog);
}
