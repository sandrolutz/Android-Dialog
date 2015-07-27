package ch.temparus.android.dialog.holder;

import android.support.annotation.ColorRes;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ch.temparus.android.dialog.R;
import ch.temparus.android.dialog.listeners.OnStateChangeListener;

/**
 * ViewHolder holds a custom layout for the {@link ch.temparus.android.dialog.Dialog} content.
 *
 * Note: The custom layout should be wrapped into a {@link android.widget.ScrollView} to guarantee
 *       that the view is fully accessible. Otherwise, the layout will be cropped!
 *
 * @author Sandro Lutz
 */
public class ViewHolder implements Holder {

    private static final int INVALID = -1;

    private int mBackgroundColor;

    private ViewGroup mHeaderContainer;
    private ViewGroup mFooterContainer;
    private View.OnKeyListener mKeyListener;

    private View mContentView;
    private int mViewResourceId = INVALID;

    public ViewHolder(int viewResourceId) {
        mViewResourceId = viewResourceId;
    }

    @SuppressWarnings("unused")
    public ViewHolder(View contentView) {
        mContentView = contentView;
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
    public void setBackgroundColor(@ColorRes int resId) {
        mBackgroundColor = resId;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.holder_view, parent, false);
        view.setId(R.id.dialog_content_view);
        ViewGroup contentContainer = (ViewGroup) view.findViewById(R.id.view_container);
        contentContainer.setBackgroundColor(parent.getResources().getColor(mBackgroundColor));
        contentContainer.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent
                    event) {
                if (mKeyListener == null) {
                    throw new NullPointerException("OnKeyListener should not be null");
                }
                return mKeyListener.onKey(v, keyCode, event);
            }
        });
        addContent(inflater, parent, contentContainer);
        mHeaderContainer = (ViewGroup) view.findViewById(R.id.header_container);
        mFooterContainer = (ViewGroup) view.findViewById(R.id.footer_container);
        return view;
    }

    private void addContent(LayoutInflater inflater, ViewGroup parent, ViewGroup container) {
        if (mViewResourceId != INVALID) {
            mContentView = inflater.inflate(mViewResourceId, parent, false);
        } else {
            ViewGroup parentView = (ViewGroup) mContentView.getParent();
            if (parentView != null) {
                parentView.removeView(mContentView);
            }
        }
        container.addView(mContentView);
    }

    @Override
    public void setOnKeyListener(View.OnKeyListener keyListener) {
        mKeyListener = keyListener;
    }

    @Override
    public View getInflatedView() {
        return mContentView;
    }

    @Override
    public boolean isInterceptTouchEventDisallowed() {
        return true;
    }

    @Override
    public OnStateChangeListener getOnStateChangeListener() {
        return null;
    }
}
