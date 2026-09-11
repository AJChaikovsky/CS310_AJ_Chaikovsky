package com.example.minesweeper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;

/** Keeps all ten rows as tall as the ten columns are wide. */
public class SquareGridLayout extends GridLayout {
    public SquareGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int side = MeasureSpec.getSize(widthMeasureSpec);
        // The enclosing ScrollView keeps a square board usable on short screens.
        int squareSpec = MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY);
        super.onMeasure(squareSpec, squareSpec);
    }
}
