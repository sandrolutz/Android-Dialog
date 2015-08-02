package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

import java.io.Serializable;

/**
 * Interface for listening to state changes in implementations of {@link ch.temparus.android.dialog.holder.Holder}.
 *
 * @author Sandro Lutz
 */
public interface OnStateChangeListener extends Serializable {

    /**
     * Called when dialog's state has changed.
     * @param dialog dialog instance
     * @param state new state
     */
    void onStateChanged(Dialog dialog, Dialog.State state);

}
