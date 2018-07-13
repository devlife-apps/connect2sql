/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.iosched.ui.widget;

import me.jromero.connect2sql.log.EzLogger;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Custom layout that arranges children in a grid-like manner, optimizing for
 * even horizontal and vertical whitespace.
 */
public class DashboardLayout extends ViewGroup {

    private static final int UNEVEN_GRID_PENALTY_MULTIPLIER = 2;

    private int mMaxChildWidth = 0;
    private int mMaxChildHeight = 0;

    private int mRows = 1;

    private int mHorizontalSpace;

    private int mVerticalSpace;

    private int mCols;

    private int mHeight;

    private int mWidth;

    public DashboardLayout(Context context) {
        super(context, null);
    }

    public DashboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public DashboardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMaxChildWidth = 0;
        mMaxChildHeight = 0;

        // Measure once to find the maximum child size.
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
            mMaxChildHeight = Math.max(mMaxChildHeight,
                    child.getMeasuredHeight());
        }

        // Measure again for each child to be exactly the same size.
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth,
                MeasureSpec.EXACTLY);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight,
                MeasureSpec.EXACTLY);

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        calculateBestFit(MeasureSpec.getSize(widthMeasureSpec));

        setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),
                resolveSize(mHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Lay out children based on calculated best-fit number of rows and cols.
        // If we chose a layout that has negative horizontal or vertical space, force it to zero.
        mHorizontalSpace = Math.max(0, mHorizontalSpace);
        mVerticalSpace = Math.max(0, mVerticalSpace);

        // ensure columns and rows are at least 1
        mCols = (mCols > 0) ? mCols : 1;
        mRows = (mRows > 0) ? mRows : 1;

        int width = (mWidth - mHorizontalSpace * (mCols + 1)) / mCols;
        int height = (mHeight - mVerticalSpace * (mRows + 1)) / mRows;

        final int count = getChildCount();

        int left, top;
        int col, row;
        int visibleIndex = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            row = visibleIndex / mCols;
            col = visibleIndex % mCols;

            left = mHorizontalSpace * (col + 1) + width * col;
            top = mVerticalSpace * (row + 1) + height * row;

            child.layout(left, top,
                    (mHorizontalSpace == 0 && col == mCols - 1) ? r
                            : (left + width),
                    (mVerticalSpace == 0 && row == mRows - 1) ? b
                            : (top + height));
            ++visibleIndex;
        }
    }

    private void calculateBestFit(int width) {
        EzLogger.d("Calculating fit for width: " + width);

        // we increase height as we add rows
        mHeight = 0;
        mWidth = width;

        final int count = getChildCount();

        // Calculate the number of visible children.
        int visibleCount = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            ++visibleCount;
        }

        if (visibleCount == 0) {
            return;
        }

        // Calculate what number of rows and columns will optimize for even horizontal and
        // vertical whitespace between items. Start with a 1 x N grid, then try 2 x N, and so on.
        int bestSpaceDifference = Integer.MAX_VALUE;
        int spaceDifference;

        // Horizontal and vertical space between items
        mHorizontalSpace = 0;
        mVerticalSpace = 0;

        mCols = 1;
        mRows = 1;

        while (true) {
            mRows = (visibleCount) / mCols + 1;
            mHeight = mMaxChildHeight * (mRows + 1);

            mHorizontalSpace = ((mWidth - mMaxChildWidth * mCols) / (mCols + 1));
            mVerticalSpace = ((mHeight - mMaxChildHeight * mRows) / (mRows + 2));

            spaceDifference = Math.abs(mVerticalSpace - mHorizontalSpace);
            if (mRows * mCols != visibleCount) {
                spaceDifference *= UNEVEN_GRID_PENALTY_MULTIPLIER;
            } else if (mRows * mMaxChildHeight > mHeight
                    || mCols * mMaxChildWidth > mWidth) {
                spaceDifference *= UNEVEN_GRID_PENALTY_MULTIPLIER;
            }

            if (spaceDifference < bestSpaceDifference) {
                // Found a better whitespace squareness/ratio
                bestSpaceDifference = spaceDifference;

                // If we found a better whitespace squareness and there's only 1 row, this is
                // the best we can do.
                if (mRows == 1) {
                    break;
                }
            } else {
                // This is a worse whitespace ratio, use the previous value of cols and exit.
                --mCols;
                mRows = (visibleCount - 1) / mCols + 1;
                mHorizontalSpace = ((mWidth - mMaxChildWidth * mCols) / (mCols + 1));
                mVerticalSpace = ((mHeight - mMaxChildHeight * mRows) / (mRows + 1));
                break;
            }

            ++mCols;
        }
    }
}