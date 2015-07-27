package ch.temparus.android.dialog.listeners;

import ch.temparus.android.dialog.Dialog;

/**
 * Interface for listening to state changes in implementations of {@link ch.temparus.android.dialog.holder.Holder}.
 *
 * @author Sandro Lutz
 */
public interface OnStateChangeListener {

    /**
     * Called when dialog has reached expanded state.
     * @param dialog dialog instance
     */
    void onExpanded(Dialog dialog);

    /**
     * Called when dialog has reached collapsed state.
     * @param dialog dialog instance
     */
    void onCollapsed(Dialog dialog);

}
