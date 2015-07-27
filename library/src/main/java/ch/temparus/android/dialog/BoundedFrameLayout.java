package ch.temparus.android.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * FrameLayout with a optional maximal width / height
 * BoundedFrameLayout is used internally by {@link DialogLayout}
 *
 * @author Sandro Lutz
 */
class BoundedFrameLayout extends FrameLayout {

    // Determine whether the resources are set or not
    public static final int INVALID = -1;

    private int mBoundedWidth = INVALID;
    private int mBoundedHeight = INVALID;

    public BoundedFrameLayout(Context context) {
        super(context);
    }

    public BoundedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoundedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBoundedWidth(int boundedWidth) {
        mBoundedWidth = boundedWidth;
    }

    public void setBoundedHeight(int boundedHeight) {
        mBoundedHeight = boundedHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Adjust width as necessary
        if (0 < mBoundedWidth && mBoundedWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode);
        }
        // Adjust height as necessary
        if (0 < mBoundedHeight && mBoundedHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
