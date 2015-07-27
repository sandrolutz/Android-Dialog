package ch.temparus.android.dialog.holder;

import ch.temparus.android.dialog.listeners.OnHolderListener;

/**
 * Interface for {@link Holder} using an adapter to display their content.
 *
 * @author Sandro Lutz
 */
public interface HolderAdapter<Adapter> extends Holder {

    /**
     * Set Adapter
     * @param adapter holding the data set to display on the {@link ch.temparus.android.dialog.Dialog}
     */
    @SuppressWarnings("unused")
    void setAdapter(Adapter adapter);

    /**
     * Set {@link OnHolderListener} for catching click events on items of the adapters data set.
     * @param listener provided by {@link ch.temparus.android.dialog.Dialog}
     */
    void setOnItemClickListener(OnHolderListener listener);
}
