package com.example.myapplication;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
/**
 * ItemDecoration class used to add consistent spacing between items
 * in a RecyclerView grid layout.
 *
 * Role in application:
 * - Utility class for styling grid layouts in admin image browsing.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;
    /**
     * Creates a GridSpacingItemDecoration with the specified configuration.
     *
     * @param spanCount number of columns in the grid
     * @param spacing spacing (in pixels) between items
     * @param includeEdge whether to include spacing on the outer edges of the grid
     */
    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }
    /**
     * Calculates and applies spacing offsets for each item in the grid.
     *
     * @param outRect Rect object to define the spacing offsets
     * @param view the current item view
     * @param parent the RecyclerView containing the items
     * @param state the current state of the RecyclerView
     */
    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;

            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}