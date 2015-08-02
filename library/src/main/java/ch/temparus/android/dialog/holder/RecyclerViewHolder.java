package ch.temparus.android.dialog.holder;

import android.support.annotation.ColorRes;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.FrameLayout;
import ch.temparus.android.advancedrecyclerview.AdvancedRecyclerView;
import ch.temparus.android.advancedrecyclerview.BaseAdapter;
import ch.temparus.android.dialog.Dialog;
import ch.temparus.android.dialog.R;
import ch.temparus.android.dialog.listeners.OnHolderListener;
import ch.temparus.android.dialog.listeners.OnStateChangeListener;

/**
 * RecyclerViewHolder holds a {@link AdvancedRecyclerView} as the content view of the {@link ch.temparus.android.dialog.Dialog}.
 *
 * @see AdvancedRecyclerView
 * @author Sandro Lutz
 */
public class RecyclerViewHolder implements HolderAdapter<BaseAdapter> {

    private ViewGroup mHeaderContainer;
    private ViewGroup mFooterContainer;
    private AdvancedRecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BaseAdapter mAdapter;
    private OnHolderListener mHolderListener;
    private Dialog.State mState;
    private long mLastStateChange;
    private int mBackgroundColorResource;
    private boolean mIsInterceptTouchEventDisallowed = false;

    @SuppressWarnings("unused")
    public RecyclerViewHolder() {
        mLastStateChange = System.currentTimeMillis();
    }

    public RecyclerViewHolder(RecyclerView.LayoutManager layoutManager, BaseAdapter adapter) {
        mLayoutManager = layoutManager;
        mAdapter = adapter;
        mLastStateChange = System.currentTimeMillis();
    }

    @Override
    public void addHeader(View view) {
        if (view == null) {
            return;
        }
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        mHeaderContainer.addView(view);
    }

    @Override
    public void addFooter(View view) {
        if (view == null) {
            return;
        }
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        mFooterContainer.addView(view);
    }

    /**
     * Set LayoutManager for the RecyclerView.
     *
     * Note: You can use {@link ch.temparus.android.advancedrecyclerview.LinearLayoutManager} or
     * {@link ch.temparus.android.advancedrecyclerview.GridLayoutManager} too.
     * For more information see {@link AdvancedRecyclerView}.
     *
     * @param layoutManager instance of RecyclerView.LayoutManager
     */
    @SuppressWarnings("unused")
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(layoutManager);
        }
        mLayoutManager = layoutManager;
    }

    @Override
    public void setAdapter(BaseAdapter adapter) {
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
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
        View view = inflater.inflate(R.layout.t_dialog__holder_recycler_view, parent, false);
        view.setId(R.id.t_dialog__content_view);
        mRecyclerView = (AdvancedRecyclerView) view.findViewById(R.id.t_dialog__holder_recycler_view);
        mRecyclerView.setBackgroundColor(backgroundColor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (System.currentTimeMillis() - mLastStateChange > 20) {
                    View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (view != null) {
                        int position = mRecyclerView.getChildAdapterPosition(view);
                        int contentPosition = position - mAdapter.getHeaderCount();
                        if (contentPosition >= 0 && mHolderListener != null) {
                            mHolderListener.onItemClick(mAdapter.getContentItem(contentPosition), view, position);
                        }
                    }
                }
                return super.onSingleTapUp(e);
            }
        });
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int top = 0;
                if (mLayoutManager instanceof LinearLayoutManager) {
                    int topPosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                    top = topPosition == 0 ? mLayoutManager.findViewByPosition(topPosition).getTop() : -5;
                }

                // scrolls the RecyclerView to the most top position, if it has not exactly scrolled to the top.
                if (dy < 0 && top > -5) {
                    recyclerView.scrollToPosition(0);
                    top = 0;
                }

                mIsInterceptTouchEventDisallowed = (top < 0 && mState == Dialog.State.EXPANDED);
            }
        });
        mHeaderContainer = (FrameLayout) view.findViewById(R.id.t_dialog__header_container);
        mHeaderContainer.setBackgroundColor(backgroundColor);
        mHeaderContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        mFooterContainer = (FrameLayout) view.findViewById(R.id.t_dialog__footer_container);
        mFooterContainer.setBackgroundColor(backgroundColor);
        mFooterContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        return view;
    }

    @Override
    public void setOnItemClickListener(OnHolderListener holderListener) {
        mHolderListener = holderListener;
    }

    @Override
    public View getInflatedView() {
        return mRecyclerView;
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
                    mRecyclerView.scrollToPosition(0);
                }
            }
        };
    }
}