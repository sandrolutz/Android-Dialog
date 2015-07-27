package ch.temparus.android.dialog.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.temparus.components.advancedrecyclerview.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sandro Lutz
 */
public class SampleRecyclerAdapter extends BaseAdapter<SampleRecyclerAdapter.ViewHolder> {

    private List<String> mData;

    public SampleRecyclerAdapter(Context context) {
        super(context);

        mData = new ArrayList<>(20);
        for (int i = 0; i < 20; ++i) {
            mData.add("Test-Item " + i);
        }
    }

    @Override
    public ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindContentViewHolder(ViewHolder holder, int position, boolean selected) {
        holder.textView.setText(mData.get(position));
    }

    @Override
    public int getContentItemCount() {
        return mData.size();
    }

    @Override
    public boolean isContentSelectable(int position) {
        return true;
    }

    @Override
    public Object getContentItem(int position) {
        return mData.get(position);
    }

    public static class ViewHolder extends BaseAdapter.ViewHolder {

        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.text_view);
        }
    }
}
