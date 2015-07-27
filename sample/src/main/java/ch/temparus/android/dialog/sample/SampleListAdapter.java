package ch.temparus.android.dialog.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sandro Lutz
 */
public class SampleListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;
    private List<String> mData;

    public SampleListAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mData = new ArrayList<>(4);

        for (int i = 0; i < 4; ++i) {
            mData.add("Test-Item " + i);
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(mData.get(position));

        return convertView;
    }

    class ViewHolder {

        TextView textView;

        public ViewHolder(View view) {
            textView = (TextView) view;
        }
    }
}
