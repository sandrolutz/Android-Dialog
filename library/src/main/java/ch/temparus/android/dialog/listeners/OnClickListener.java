package ch.temparus.android.dialog.listeners;

import android.view.View;
import ch.temparus.android.dialog.Dialog;

/**
 * Interface for listening to all click events of the {@link Dialog} (including header and footer!).
 *
 * @author Sandro Lutz
 */
public interface OnClickListener {

    /**
     * Called when a click event occurred on the {@link Dialog}.
     * @param dialog dialog instance
     * @param view   view of clicked item
     */
    void onClick(Dialog dialog, View view);

}
