package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

import java.io.Serializable;

/**
 * Interface for listening to the confirm event of {@link Dialog}.
 *
 * @author Sandro Lutz
 */
public interface OnConfirmListener extends Serializable {

    /**
     * Called when the dialog has been confirmed. (Fullscreen dialog)
     * @param dialog dialog instance
     * @return true - if dialog content is valid; false - otherwise
     */
    boolean onConfirm(Dialog dialog);
}
