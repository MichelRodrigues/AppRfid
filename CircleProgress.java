package com.UHF.scanlable;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/* loaded from: classes.dex */
public class CircleProgress extends View {
    private static final String TAG = "CircleProgress";
    private boolean antiAlias;
    private int foreEndColcor;
    private int foreStartColor;
    private long mAnimTime;
    private ValueAnimator mAnimator;
    private int mArcCenterX;
    private int mArcColor;
    private Paint mArcPaint;
    private float mArcWidth;
    private int mBgArcColor;
    private Paint mBgArcPaint;
    private float mBgArcWidth;
    private Point mCenterPoint;
    private Context mContext;
    private int mDefaultSize;
    private int mDottedLineCount;
    private float mDottedLineWidth;
    private float mExternalDottedLineRadius;
    protected int mHeight;
    private CharSequence mHint;
    private int mHintColor;
    private float mHintOffset;
    private TextPaint mHintPaint;
    private float mHintSize;
    private float mInsideDottedLineRadius;
    private int mLineDistance;
    private float mMaxValue;
    private float mPercent;
    private int mPrecision;
    private String mPrecisionFormat;
    private float mRadius;
    private RectF mRectF;
    private float mStartAngle;
    private float mSweepAngle;
    private float mTextOffsetPercentInRadius;
    private CharSequence mUnit;
    private int mUnitColor;
    private float mUnitOffset;
    private TextPaint mUnitPaint;
    private float mUnitSize;
    private float mValue;
    private int mValueColor;
    private float mValueOffset;
    private TextPaint mValuePaint;
    private float mValueSize;
    protected int mWidth;
    protected boolean useGradient;

    public CircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDottedLineCount = 100;
        this.mLineDistance = 20;
        this.mDottedLineWidth = 40.0f;
        this.useGradient = true;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        this.mDefaultSize = dipToPx(context, 150.0f);
        this.mAnimator = new ValueAnimator();
        this.mRectF = new RectF();
        this.mCenterPoint = new Point();
        initAttrs(attrs);
        initPaint();
        setValue(this.mValue);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        this.antiAlias = obtainStyledAttributes.getBoolean(1, true);
        this.mHint = obtainStyledAttributes.getString(10);
        this.mHintColor = obtainStyledAttributes.getColor(11, ViewCompat.MEASURED_STATE_MASK);
        this.mHintSize = obtainStyledAttributes.getDimension(12, 15.0f);
        this.mValue = obtainStyledAttributes.getFloat(22, 50.0f);
        this.mMaxValue = obtainStyledAttributes.getFloat(14, 50.0f);
        int i = obtainStyledAttributes.getInt(15, 0);
        this.mPrecision = i;
        this.mPrecisionFormat = getPrecisionFormat(i);
        this.mValueColor = obtainStyledAttributes.getColor(23, ViewCompat.MEASURED_STATE_MASK);
        this.mValueSize = obtainStyledAttributes.getDimension(24, 15.0f);
        this.mUnit = obtainStyledAttributes.getString(19);
        this.mUnitColor = obtainStyledAttributes.getColor(20, ViewCompat.MEASURED_STATE_MASK);
        this.mUnitSize = obtainStyledAttributes.getDimension(21, 30.0f);
        this.mArcWidth = obtainStyledAttributes.getDimension(3, 15.0f);
        this.mStartAngle = obtainStyledAttributes.getFloat(16, 270.0f);
        this.mSweepAngle = obtainStyledAttributes.getFloat(17, 360.0f);
        this.mBgArcColor = obtainStyledAttributes.getColor(4, -1);
        this.mArcColor = obtainStyledAttributes.getColor(2, SupportMenu.CATEGORY_MASK);
        this.mBgArcWidth = obtainStyledAttributes.getDimension(5, 15.0f);
        this.mTextOffsetPercentInRadius = obtainStyledAttributes.getFloat(18, 0.33f);
        this.mAnimTime = obtainStyledAttributes.getInt(0, 50);
        this.mDottedLineCount = obtainStyledAttributes.getInteger(6, this.mDottedLineCount);
        this.mLineDistance = obtainStyledAttributes.getInteger(13, this.mLineDistance);
        this.mDottedLineWidth = obtainStyledAttributes.getDimension(7, this.mDottedLineWidth);
        this.foreStartColor = obtainStyledAttributes.getColor(9, -16776961);
        this.foreEndColcor = obtainStyledAttributes.getColor(8, -16776961);
        obtainStyledAttributes.recycle();
    }

    private void initPaint() {
        TextPaint textPaint = new TextPaint();
        this.mHintPaint = textPaint;
        textPaint.setAntiAlias(this.antiAlias);
        this.mHintPaint.setTextSize(this.mHintSize);
        this.mHintPaint.setColor(this.mHintColor);
        this.mHintPaint.setTextAlign(Paint.Align.CENTER);
        TextPaint textPaint2 = new TextPaint();
        this.mValuePaint = textPaint2;
        textPaint2.setAntiAlias(this.antiAlias);
        this.mValuePaint.setTextSize(this.mValueSize);
        this.mValuePaint.setColor(this.mValueColor);
        this.mValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.mValuePaint.setTextAlign(Paint.Align.CENTER);
        TextPaint textPaint3 = new TextPaint();
        this.mUnitPaint = textPaint3;
        textPaint3.setAntiAlias(this.antiAlias);
        this.mUnitPaint.setTextSize(this.mUnitSize);
        this.mUnitPaint.setColor(this.mUnitColor);
        this.mUnitPaint.setTextAlign(Paint.Align.CENTER);
        Paint paint = new Paint();
        this.mArcPaint = paint;
        paint.setAntiAlias(this.antiAlias);
        this.mArcPaint.setStyle(Paint.Style.STROKE);
        this.mArcPaint.setStrokeWidth(this.mArcWidth);
        this.mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        Paint paint2 = new Paint();
        this.mBgArcPaint = paint2;
        paint2.setAntiAlias(this.antiAlias);
        this.mBgArcPaint.setColor(this.mBgArcColor);
        this.mBgArcPaint.setStyle(Paint.Style.STROKE);
        this.mBgArcPaint.setStrokeWidth(this.mBgArcWidth);
        this.mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureView(widthMeasureSpec, this.mDefaultSize), measureView(heightMeasureSpec, this.mDefaultSize));
    }

    @Override // android.view.View
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float f = w;
        this.mArcCenterX = (int) (f / 2.0f);
        String str = TAG;
        Log.d(str, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        float max = Math.max(this.mArcWidth, this.mBgArcWidth);
        int i = ((int) max) * 2;
        this.mRadius = (float) (Math.min(((w - getPaddingLeft()) - getPaddingRight()) - i, ((h - getPaddingTop()) - getPaddingBottom()) - i) / 2);
        this.mCenterPoint.x = w / 2;
        this.mCenterPoint.y = h / 2;
        float f2 = max / 2.0f;
        this.mRectF.left = (((float) this.mCenterPoint.x) - this.mRadius) - f2;
        this.mRectF.top = (((float) this.mCenterPoint.y) - this.mRadius) - f2;
        this.mRectF.right = ((float) this.mCenterPoint.x) + this.mRadius + f2;
        this.mRectF.bottom = this.mCenterPoint.y + this.mRadius + f2;
        this.mValueOffset = this.mCenterPoint.y + getBaselineOffsetFromY(this.mValuePaint);
        this.mHintOffset = (this.mCenterPoint.y - (this.mRadius * this.mTextOffsetPercentInRadius)) + getBaselineOffsetFromY(this.mHintPaint);
        this.mUnitOffset = this.mCenterPoint.y + (this.mRadius * this.mTextOffsetPercentInRadius) + getBaselineOffsetFromY(this.mUnitPaint);
        if (this.useGradient) {
            this.mArcPaint.setShader(new LinearGradient(0.0f, 0.0f, f, h, this.foreEndColcor, this.foreStartColor, Shader.TileMode.CLAMP));
        } else {
            this.mArcPaint.setColor(this.mArcColor);
        }
        Log.d(str, "onSizeChanged: 控件大小 = (" + w + ", " + h + ")圆心坐标 = " + this.mCenterPoint.toString() + ";圆半径 = " + this.mRadius + ";圆的外接矩形 = " + this.mRectF.toString());
        float width = (float) (((int) (this.mRectF.width() / 2.0f)) + this.mLineDistance);
        this.mExternalDottedLineRadius = width;
        this.mInsideDottedLineRadius = width - this.mDottedLineWidth;
    }

    private float getBaselineOffsetFromY(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (Math.abs(fontMetrics.ascent) - fontMetrics.descent) / 2.0f;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawArc(canvas);
    }

    private void drawText(Canvas canvas) {
        canvas.drawText(String.format(this.mPrecisionFormat, Float.valueOf(this.mValue)), this.mCenterPoint.x, this.mValueOffset, this.mValuePaint);
        CharSequence charSequence = this.mHint;
        if (charSequence != null) {
            canvas.drawText(charSequence.toString(), this.mCenterPoint.x, this.mHintOffset, this.mHintPaint);
        }
        CharSequence charSequence2 = this.mUnit;
        if (charSequence2 != null) {
            canvas.drawText(charSequence2.toString(), this.mCenterPoint.x, this.mUnitOffset, this.mUnitPaint);
        }
    }

    private void drawArc(Canvas canvas) {
        canvas.save();
        float f = (float) (6.283185307179586d / this.mDottedLineCount);
        for (int i = 0; i < this.mDottedLineCount; i++) {
            float f2 = i * f;
            if (f2 <= 2.3561945f || f2 >= 3.9269907f) {
                double d = f2;
                canvas.drawLine(this.mArcCenterX + (((float) Math.sin(d)) * this.mInsideDottedLineRadius), this.mArcCenterX - (((float) Math.cos(d)) * this.mInsideDottedLineRadius), this.mArcCenterX + (((float) Math.sin(d)) * this.mExternalDottedLineRadius), this.mArcCenterX - (((float) Math.cos(d)) * this.mExternalDottedLineRadius), this.mBgArcPaint);
            }
        }
        canvas.rotate(this.mStartAngle, this.mCenterPoint.x, this.mCenterPoint.y);
        canvas.drawArc(this.mRectF, 2.0f, this.mSweepAngle * this.mPercent, false, this.mArcPaint);
        canvas.restore();
    }

    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    public CharSequence getHint() {
        return this.mHint;
    }

    public void setHint(CharSequence hint) {
        this.mHint = hint;
    }

    public CharSequence getUnit() {
        return this.mUnit;
    }

    public void setUnit(CharSequence unit) {
        this.mUnit = unit;
    }

    public float getValue() {
        return this.mValue;
    }

    public void setValue(float value) {
        float f = this.mMaxValue;
        if (value > f) {
            value = f;
        }
        startAnimator(this.mPercent, value / f, this.mAnimTime);
    }

    private void startAnimator(float start, float end, long animTime) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(start, end);
        this.mAnimator = ofFloat;
        ofFloat.setDuration(animTime);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.UHF.scanlable.CircleProgress.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleProgress.this.mPercent = ((Float) animation.getAnimatedValue()).floatValue();
                CircleProgress circleProgress = CircleProgress.this;
                circleProgress.mValue = circleProgress.mPercent * CircleProgress.this.mMaxValue;
                CircleProgress.this.invalidate();
            }
        });
        this.mAnimator.start();
    }

    public float getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(float maxValue) {
        this.mMaxValue = maxValue;
    }

    public int getPrecision() {
        return this.mPrecision;
    }

    public void setPrecision(int precision) {
        this.mPrecision = precision;
        this.mPrecisionFormat = getPrecisionFormat(precision);
    }

    public long getAnimTime() {
        return this.mAnimTime;
    }

    public void setAnimTime(long animTime) {
        this.mAnimTime = animTime;
    }

    public void reset() {
        startAnimator(this.mPercent, 0.0f, 1000L);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private static int measureView(int measureSpec, int defaultSize) {
        int mode = View.MeasureSpec.getMode(measureSpec);
        int size = View.MeasureSpec.getSize(measureSpec);
        return mode == 1073741824 ? size : mode == Integer.MIN_VALUE ? Math.min(defaultSize, size) : defaultSize;
    }

    public static int dipToPx(Context context, float dip) {
        return (int) ((context.getResources().getDisplayMetrics().density * dip) + ((dip >= 0.0f ? 1 : -1) * 0.5f));
    }

    public static String getPrecisionFormat(int precision) {
        return "%." + precision + "f";
    }
}
