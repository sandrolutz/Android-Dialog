package ch.temparus.android.dialog.listeners;

import android.view.View;
import ch.temparus.android.dialog.Dialog;

/**
 * Interface for listening to click event on items in implementations of {@link ch.temparus.android.dialog.holder.HolderAdapter}.
 *
 * @author Sandro Lutz
 */
public interface OnItemClickListener {

    /**
     * Called when a click event occurred on an item of the {@link ch.temparus.android.dialog.holder.HolderAdapter}.
     * @param dialog    dialog instance
     * @param item      object of clicked item
     * @param view      view of clicked item
     * @param position  position within {@link ch.temparus.android.dialog.holder.HolderAdapter}
     */
    void onItemClick(Dialog dialog, Object item, View view, int position);

}