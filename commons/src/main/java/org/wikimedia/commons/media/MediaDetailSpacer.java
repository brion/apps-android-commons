package org.wikimedia.commons.media;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;

public class MediaDetailSpacer extends View {
    public MediaDetailSpacer(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int width, height;
        ScrollView scrollView = findScrollView();

        // hope this doesn't ESPLODE
        width = scrollView.getWidth();
        height = scrollView.getHeight();
        setMeasuredDimension(width, height);
    }

    private ScrollView findScrollView()
    {
        return findScrollView(this);
    }

    private ScrollView findScrollView(View view)
    {
        if (view instanceof ScrollView) {
            return (ScrollView)view;
        } else {
            ViewParent parent = view.getParent();
            if (parent == null || !(parent instanceof View)) {
                return null;
            } else {
                return findScrollView((View)parent);
            }
        }
    }
}
