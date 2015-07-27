package ch.temparus.android.dialog.listeners;

import android.view.View;

/**
 * Interface for listening to click event on items in implementations of {@link ch.temparus.android.dialog.holder.HolderAdapter}.
 *
 * Note: This interface is only used internally for catching click events from the {@link ch.temparus.android.dialog.holder.Holder}
 *
 * @author Sandro Lutz
 */
public interface OnHolderListener {

    /**
     * Called when a click event occurred on an item of the {@link ch.temparus.android.dialog.holder.HolderAdapter}.
     * @param item      object of clicked item
     * @param view      view of clicked item
     * @param position  position within {@link ch.temparus.android.dialog.holder.HolderAdapter}
     */
    void onItemClick(Object item, View view, int position);

}
