package com.sleepbot.datetimepicker.time;
/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.View;

import com.fourmob.datetimepicker.R;
import com.nineoldandroids.animation.Keyframe;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.sleepbot.datetimepicker.canvassaveproxy.CanvasSaveProxy;
import com.sleepbot.datetimepicker.canvassaveproxy.CanvasSaveProxyFactory;

/**
 * A view to show a series of numbers in a circular pattern.
 */
public class RadialTextsView extends View {
    private final static String TAG = "RadialTextsView";

    private static final int DEGREES_IN_CIRCLE = 360; // each number represents 30 degrees.
    private static final int DEGREE_POSITION_TICK = DEGREES_IN_CIRCLE / 12; // each number represents 30 degrees.

    private final Paint mPaint = new Paint();

    private boolean mDrawValuesReady;
    private boolean mIsInitialized;

    private int mNumbersTextColor;
    private Typeface mTypefaceLight;
    private Typeface mTypefaceRegular;
    private String[] mTexts;
    private String[] mInnerTexts;
    private boolean mIs24HourMode;
    private boolean mHasInnerCircle;
    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private float mNumbersRadiusMultiplier;
    private float mInnerNumbersRadiusMultiplier;
    private float mTextSizeMultiplier;
    private float mInnerTextSizeMultiplier;

    private int mXCenter;
    private int mYCenter;
    private float mCircleRadius;
    private boolean mTextGridValuesDirty;
    private float mTextSize;
    private float mInnerTextSize;
    private float[] mTextGridHeights;
    private float[] mTextGridWidths;
    private float[] mInnerTextGridHeights;
    private float[] mInnerTextGridWidths;

    private float mAnimationRadiusMultiplier;
    private float mTransitionMidRadiusMultiplier;
    private float mTransitionEndRadiusMultiplier;
    ObjectAnimator mDisappearAnimator;
    ObjectAnimator mReappearAnimator;
    private InvalidateUpdateListener mInvalidateUpdateListener;

    private float mSelectionRadiusMultiplier;
    private int mSelectionHourDegrees;
    private boolean mSelectionInnerCircle;
    private float mSelectionRadius;
    private Path mSelectorPath;

    private final CanvasSaveProxyFactory mCanvasSaveProxyFactory;
    private CanvasSaveProxy mCanvasSaveProxy;


    public RadialTextsView(Context context) {
        super(context);
        mIsInitialized = false;
        mCanvasSaveProxyFactory = new CanvasSaveProxyFactory();
    }

    public void initialize(Resources res, String[] texts, String[] innerTexts,
                           boolean is24HourMode, boolean disappearsOut,
                           int selectionHourDegrees, boolean selectionInnerCircle) {
        if (mIsInitialized) {
            Log.e(TAG, "This RadialTextsView may only be initialized once.");
            return;
        }

        // Set up the paint.
        mNumbersTextColor = res.getColor(R.color.numbers_text_color);
        String typefaceFamily = res.getString(R.string.radial_numbers_typeface);
        mTypefaceLight = Typeface.create(typefaceFamily, Typeface.NORMAL);
        String typefaceFamilyRegular = res.getString(R.string.sans_serif);
        mTypefaceRegular = Typeface.create(typefaceFamilyRegular, Typeface.NORMAL);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);

        mTexts = texts;
        mInnerTexts = innerTexts;
        mIs24HourMode = is24HourMode;
        mHasInnerCircle = (innerTexts != null);

        mSelectionRadiusMultiplier = Float.parseFloat(res.getString(R.string.selection_radius_multiplier));

        mSelectionHourDegrees = selectionHourDegrees;
        mSelectionInnerCircle = selectionInnerCircle;
        mSelectorPath = new Path();

        // Calculate the radius for the main circle.
        if (is24HourMode) {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.circle_radius_multiplier_24HourMode));
        } else {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.circle_radius_multiplier));
            mAmPmCircleRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.ampm_circle_radius_multiplier));
        }

        // Initialize the widths and heights of the grid, and calculate the values for the numbers.
        mTextGridHeights = new float[7];
        mTextGridWidths = new float[7];
        if (mHasInnerCircle) {
            mNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.numbers_radius_multiplier_outer));
            mTextSizeMultiplier = Float.parseFloat(
                    res.getString(R.string.text_size_multiplier_outer));
            mInnerNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.numbers_radius_multiplier_inner));
            mInnerTextSizeMultiplier = Float.parseFloat(
                    res.getString(R.string.text_size_multiplier_inner));

            mInnerTextGridHeights = new float[7];
            mInnerTextGridWidths = new float[7];
        } else {
            mNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.numbers_radius_multiplier_normal));
            mTextSizeMultiplier = Float.parseFloat(
                    res.getString(R.string.text_size_multiplier_normal));
        }

        mAnimationRadiusMultiplier = 1;
        mTransitionMidRadiusMultiplier = 1f + (0.05f * (disappearsOut ? -1 : 1));
        mTransitionEndRadiusMultiplier = 1f + (0.3f * (disappearsOut ? 1 : -1));
        mInvalidateUpdateListener = new InvalidateUpdateListener();

        mTextGridValuesDirty = true;
        mIsInitialized = true;
    }

    /**
     * Allows for smoother animation.
     */
    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    /**
     * Used by the animation to move the numbers in and out.
     */
    public void setAnimationRadiusMultiplier(float animationRadiusMultiplier) {
        mAnimationRadiusMultiplier = animationRadiusMultiplier;
        mTextGridValuesDirty = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) {
            return;
        }

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier;
            if (!mIs24HourMode) {
                // We'll need to draw the AM/PM circles, so the main circle will need to have
                // a slightly higher center. To keep the entire view centered vertically, we'll
                // have to push it up by half the radius of the AM/PM circles.
                float amPmCircleRadius = mCircleRadius * mAmPmCircleRadiusMultiplier;
                mYCenter -= amPmCircleRadius / 2;
            }

            mTextSize = mCircleRadius * mTextSizeMultiplier;
            if (mHasInnerCircle) {
                mInnerTextSize = mCircleRadius * mInnerTextSizeMultiplier;
            }

            // Because the text positions will be static, pre-render the animations.
            renderAnimations();

            mTextGridValuesDirty = true;
            mDrawValuesReady = true;
        }

        // Calculate the text positions, but only if they've changed since the last onDraw.
        float numbersRadius = mCircleRadius * mNumbersRadiusMultiplier * mAnimationRadiusMultiplier;
        if (mTextGridValuesDirty) {
            // Calculate the positions for the 12 numbers in the main circle.
            calculateGridSizes(numbersRadius, mXCenter, mYCenter,
                    mTextSize, mTextGridHeights, mTextGridWidths);
            if (mHasInnerCircle) {
                // If we have an inner circle, calculate those positions too.
                float innerNumbersRadius =
                        mCircleRadius * mInnerNumbersRadiusMultiplier * mAnimationRadiusMultiplier;
                calculateGridSizes(innerNumbersRadius, mXCenter, mYCenter,
                        mInnerTextSize, mInnerTextGridHeights, mInnerTextGridWidths);
            }

            // The radius of the selector from the radial selector.
            mSelectionRadius = (int) (mCircleRadius * mSelectionRadiusMultiplier);

            mTextGridValuesDirty = false;
        }

        // Create a mask for the radial selector.
        mSelectorPath.reset();
        double selectionRadians = mSelectionHourDegrees * Math.PI / 180;
        float selectorX = mXCenter + (int) (numbersRadius * Math.sin(selectionRadians));
        float selectorY = mYCenter - (int) (numbersRadius * Math.cos(selectionRadians));
        mSelectorPath.addCircle(selectorX, selectorY, mSelectionRadius, Path.Direction.CW);

        // Draw the texts in the pre-calculated positions.
        drawTexts(canvas, mTextSize, mTypefaceLight, mTexts, mTextGridWidths, mTextGridHeights, !mSelectionInnerCircle);
        if (mHasInnerCircle) {
            drawTexts(canvas, mInnerTextSize, mTypefaceRegular, mInnerTexts,
                    mInnerTextGridWidths, mInnerTextGridHeights, mSelectionInnerCircle);
        }
    }

    /**
     * Using the trigonometric Unit Circle, calculate the positions that the text will need to be
     * drawn at based on the specified circle radius. Place the values in the textGridHeights and
     * textGridWidths parameters.
     */
    private void calculateGridSizes(float numbersRadius, float xCenter, float yCenter,
                                    float textSize, float[] textGridHeights, float[] textGridWidths) {
        /*
         * The numbers need to be drawn in a 7x7 grid, representing the points on the Unit Circle.
         */
        float offset1 = numbersRadius;
        // cos(30) = a / r => r * cos(30) = a => r * √3/2 = a
        float offset2 = numbersRadius * ((float) Math.sqrt(3)) / 2f;
        // sin(30) = o / r => r * sin(30) = o => r / 2 = a
        float offset3 = numbersRadius / 2f;
        mPaint.setTextSize(textSize);
        // We'll need yTextBase to be slightly lower to account for the text's baseline.
        yCenter -= (mPaint.descent() + mPaint.ascent()) / 2;

        textGridHeights[0] = yCenter - offset1;
        textGridWidths[0] = xCenter - offset1;
        textGridHeights[1] = yCenter - offset2;
        textGridWidths[1] = xCenter - offset2;
        textGridHeights[2] = yCenter - offset3;
        textGridWidths[2] = xCenter - offset3;
        textGridHeights[3] = yCenter;
        textGridWidths[3] = xCenter;
        textGridHeights[4] = yCenter + offset3;
        textGridWidths[4] = xCenter + offset3;
        textGridHeights[5] = yCenter + offset2;
        textGridWidths[5] = xCenter + offset2;
        textGridHeights[6] = yCenter + offset1;
        textGridWidths[6] = xCenter + offset1;
    }

    /**
     * Draw the 12 text values at the positions specified by the textGrid parameters.
     */
    private void drawTexts(Canvas canvas, float textSize, Typeface typeface, String[] texts,
                           float[] textGridWidths, float[] textGridHeights, boolean useSelectionTextEffect) {
        mPaint.setTextSize(textSize);
        mPaint.setTypeface(typeface);

        drawText(canvas, texts, 0, textGridWidths[3], textGridHeights[0], useSelectionTextEffect);
        drawText(canvas, texts, 1, textGridWidths[4], textGridHeights[1], useSelectionTextEffect);
        drawText(canvas, texts, 2, textGridWidths[5], textGridHeights[2], useSelectionTextEffect);
        drawText(canvas, texts, 3, textGridWidths[6], textGridHeights[3], useSelectionTextEffect);
        drawText(canvas, texts, 4, textGridWidths[5], textGridHeights[4], useSelectionTextEffect);
        drawText(canvas, texts, 5, textGridWidths[4], textGridHeights[5], useSelectionTextEffect);
        drawText(canvas, texts, 6, textGridWidths[3], textGridHeights[6], useSelectionTextEffect);
        drawText(canvas, texts, 7, textGridWidths[2], textGridHeights[5], useSelectionTextEffect);
        drawText(canvas, texts, 8, textGridWidths[1], textGridHeights[4], useSelectionTextEffect);
        drawText(canvas, texts, 9, textGridWidths[0], textGridHeights[3], useSelectionTextEffect);
        drawText(canvas, texts, 10, textGridWidths[1], textGridHeights[2], useSelectionTextEffect);
        drawText(canvas, texts, 11, textGridWidths[2], textGridHeights[1], useSelectionTextEffect);
    }

    /**
     * Draw the text at the given position on the canvas.
     *
     * If the text position intersects with the selector position, the text should be drawn partially white
     * and partially the standard text colour depending on the amount of overlap.
     *
     * This implementation should be changed in future, RadialTextsView and RadialSelectorView should
     * be merged at some point since there is now a dependency on the selector position and size.
     */
    private void drawText(Canvas canvas, String[] texts, int position, float x, float y, boolean useSelectionTextEffect) {
        boolean useSelectionTextColor = false;

        // If we know the text being drawn may be affected, calculate whether it actually is.
        if (useSelectionTextEffect) {
            int positionDegrees = position * DEGREE_POSITION_TICK;

            // Is the selector between this text and the next text.
            if (positionDegrees < mSelectionHourDegrees + DEGREE_POSITION_TICK) {

                // Is the selector between this text and the previous text.
                if (positionDegrees > mSelectionHourDegrees - DEGREE_POSITION_TICK) {
                    useSelectionTextColor = true;

                } else if (positionDegrees == 0 && mSelectionHourDegrees > DEGREES_IN_CIRCLE - DEGREE_POSITION_TICK) {
                    // Special case for the first text since the previous text would be negative.
                    useSelectionTextColor = true;
                }
            }
        }

        String text = texts[position];
        if (!useSelectionTextColor) {
            // Draw the text as normal.
            mPaint.setColor(mNumbersTextColor);
            canvas.drawText(text, x, y, mPaint);
            return;
        }

        // Draw the standard text everywhere except within the selector circle.
        if(mCanvasSaveProxy == null || !mCanvasSaveProxy.isFor(canvas)) {
            mCanvasSaveProxy = mCanvasSaveProxyFactory.create(canvas);
        }

        mCanvasSaveProxy.save();

        canvas.clipPath(mSelectorPath, Region.Op.DIFFERENCE);
        mPaint.setColor(mNumbersTextColor);
        canvas.drawText(text, x, y, mPaint);
        canvas.restore();

        // Draw the white text only within selector circle.
        mCanvasSaveProxy.save();

        canvas.clipPath(mSelectorPath);
        mPaint.setColor(Color.WHITE);
        canvas.drawText(text, x, y, mPaint);
        canvas.restore();
    }

    /**
     * Render the animations for appearing and disappearing.
     */
    private void renderAnimations() {
        Keyframe kf0, kf1, kf2, kf3;
        float midwayPoint = 0.2f;
        int duration = 500;

        // Set up animator for disappearing.
        kf0 = Keyframe.ofFloat(0f, 1);
        kf1 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf2 = Keyframe.ofFloat(1f, mTransitionEndRadiusMultiplier);
        PropertyValuesHolder radiusDisappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2);

        kf0 = Keyframe.ofFloat(0f, 1f);
        kf1 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder fadeOut = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1);

        mDisappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusDisappear, fadeOut).setDuration(duration);
        mDisappearAnimator.addUpdateListener(mInvalidateUpdateListener);


        // Set up animator for reappearing.
        float delayMultiplier = 0.25f;
        float transitionDurationMultiplier = 1f;
        float totalDurationMultiplier = transitionDurationMultiplier + delayMultiplier;
        int totalDuration = (int) (duration * totalDurationMultiplier);
        float delayPoint = (delayMultiplier * duration) / totalDuration;
        midwayPoint = 1 - (midwayPoint * (1 - delayPoint));

        kf0 = Keyframe.ofFloat(0f, mTransitionEndRadiusMultiplier);
        kf1 = Keyframe.ofFloat(delayPoint, mTransitionEndRadiusMultiplier);
        kf2 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf3 = Keyframe.ofFloat(1f, 1);
        PropertyValuesHolder radiusReappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2, kf3);

        kf0 = Keyframe.ofFloat(0f, 0f);
        kf1 = Keyframe.ofFloat(delayPoint, 0f);
        kf2 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder fadeIn = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1, kf2);

        mReappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusReappear, fadeIn).setDuration(totalDuration);
        mReappearAnimator.addUpdateListener(mInvalidateUpdateListener);
    }

    public ObjectAnimator getDisappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady || mDisappearAnimator == null) {
            Log.e(TAG, "RadialTextView was not ready for animation.");
            return null;
        }

        return mDisappearAnimator;
    }

    public ObjectAnimator getReappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady || mReappearAnimator == null) {
            Log.e(TAG, "RadialTextView was not ready for animation.");
            return null;
        }

        return mReappearAnimator;
    }

    public void setSelection(int hourDegrees, boolean hourInnerCircle) {
        mSelectionHourDegrees = hourDegrees;
        mSelectionInnerCircle = hourInnerCircle;
    }

    private class InvalidateUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            RadialTextsView.this.invalidate();
        }
    }
}