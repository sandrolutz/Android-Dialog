package ch.temparus.android.dialog.holder;

import android.support.annotation.ColorRes;
import android.view.*;
import android.widget.*;
import ch.temparus.android.dialog.Dialog;
import ch.temparus.android.dialog.R;
import ch.temparus.android.dialog.listeners.OnHolderListener;
import ch.temparus.android.dialog.listeners.OnStateChangeListener;

/**
 * RecyclerViewHolder holds a {@link ListView} as the content view of the {@link ch.temparus.android.dialog.Dialog}.
 *
 * @see ListView
 * @author Sandro Lutz
 */
public class ListViewHolder implements HolderAdapter<BaseAdapter>, AdapterView.OnItemClickListener {

    private ViewGroup mHeaderContainer;
    private ViewGroup mFooterContainer;
    private ListView mListView;
    private BaseAdapter mAdapter;
    private OnHolderListener mHolderListener;
    private View.OnKeyListener mKeyListener;
    private Dialog.State mState;
    private long mLastStateChange;
    private int mBackgroundColorResource;
    private boolean mIsInterceptTouchEventDisallowed = false;

    public ListViewHolder() {
        mLastStateChange = System.currentTimeMillis();
    }

    public ListViewHolder(BaseAdapter adapter) {
        mAdapter = adapter;
        mLastStateChange = System.currentTimeMillis();
    }

    @Override
    public void addHeader(View view) {
        if (view == null) {
            return;
        }
        mHeaderContainer.addView(view);
    }

    @Override
    public void addFooter(View view) {
        if (view == null) {
            return;
        }
        mFooterContainer.addView(view);
    }

    @Override
    public void setAdapter(BaseAdapter adapter) {
        if(mListView != null) {
            mListView.setAdapter(adapter);
        }
        mAdapter = adapter;
    }

    @Override
    public void setBackgroundColor(@ColorRes int resId) {
        mBackgroundColorResource = resId;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        int backgroundColor = parent.getResources().getColor(mBackgroundColorResource);
        View view = inflater.inflate(R.layout.holder_list_view, parent, false);
        view.setId(R.id.dialog_content_view);
        mListView = (ListView) view.findViewById(R.id.list);
        mListView.setBackgroundColor(backgroundColor);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                // Stub
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                View child = mListView.getChildAt(0);
                int top = (child == null) ? 0 : (child.getTop() - mListView.getPaddingTop());
                mIsInterceptTouchEventDisallowed = (top < 0 && mState == Dialog.State.EXPANDED);
            }
        });
        mListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (mKeyListener == null) {
                    throw new NullPointerException("keyListener should not be null");
                }
                return mKeyListener.onKey(v, keyCode, event);
            }
        });
        mListView.scrollTo(0, 0);
        mHeaderContainer = (FrameLayout) view.findViewById(R.id.header_container);
        mHeaderContainer.setBackgroundColor(backgroundColor);
        mFooterContainer = (FrameLayout) view.findViewById(R.id.footer_container);
        mFooterContainer.setBackgroundColor(backgroundColor);
        return view;
    }

    @Override
    public void setOnItemClickListener(OnHolderListener holderListener) {
        mHolderListener = holderListener;
    }

    @Override
    public void setOnKeyListener(View.OnKeyListener keyListener) {
        mKeyListener = keyListener;
    }

    @Override
    public View getInflatedView() {
        return mListView;
    }

    @Override
    public boolean isInterceptTouchEventDisallowed() {
        return mIsInterceptTouchEventDisallowed;
    }

    @Override
    public OnStateChangeListener getOnStateChangeListener() {
        return new OnStateChangeListener() {
            @Override
            public void onStateChanged(Dialog dialog, Dialog.State state) {
                mState = state;
                mLastStateChange = System.currentTimeMillis();
                if (state == Dialog.State.COLLAPSED) {
                    mListView.smoothScrollToPosition(0);
                }
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (System.currentTimeMillis() - mLastStateChange > 20) {
            mHolderListener.onItemClick(parent.getItemAtPosition(position), view, position);
        }
    }
}