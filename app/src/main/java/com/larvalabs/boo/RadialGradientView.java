package com.larvalabs.boo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

public class RadialGradientView extends View {

    private static final long TRANSITION_TIME = 1500L;

    private int startColor;
    private int endColor;
    private int newStartColor;
    private int newEndColor;
    private boolean changing = false;
    private long changeTime = 0;
    private final float gradientRadiusWidthPercent;
    private final float centerY;
    private final float centerX;
    private Paint paint;
    private Paint changePaint;

    public RadialGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadialGradientView, 0, 0);
            startColor = a.getColor(R.styleable.RadialGradientView_startColor, Color.RED);
            endColor = a.getColor(R.styleable.RadialGradientView_endColor, Color.BLACK);
            gradientRadiusWidthPercent = a.getFloat(R.styleable.RadialGradientView_gradientRadiusWidthPercent, 1);
            centerX = a.getFloat(R.styleable.RadialGradientView_centerX, .5f);
            centerY = a.getFloat(R.styleable.RadialGradientView_centerY, .5f);
            a.recycle();
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        changePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        changePaint.setDither(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = View.MeasureSpec.getSize(heightMeasureSpec);

        RadialGradient gradient = createGradient(startColor, endColor, parentWidth, parentHeight);

        paint.setShader(gradient);

    }

    private RadialGradient createGradient(int startColor, int endColor, int width, int height) {
        return new RadialGradient(
                    width*centerX,
                    height*centerY,
                    width*gradientRadiusWidthPercent,
                    new int[] {startColor, endColor},
                    null,
                    android.graphics.Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        if (changing) {
            long elapsed = System.currentTimeMillis() - changeTime;
            int alpha = MathUtils.clampedMapInt(elapsed, 0, TRANSITION_TIME, 0, 255);
            changePaint.setAlpha(alpha);
            canvas.drawRect(0, 0, getWidth(), getHeight(), changePaint);
            if (elapsed >= TRANSITION_TIME) {
                changing = false;
                changePaint.setAlpha(255);
                Paint temp = paint;
                paint = changePaint;
                changePaint = temp;
            } else {
                postInvalidate();
            }
        }
    }

    public void changeColor(int startColor, int endColor) {
        newStartColor = startColor;
        newEndColor = endColor;
        changeTime = System.currentTimeMillis();
        changing = true;
        RadialGradient gradient = createGradient(startColor, endColor, getWidth(), getHeight());
        changePaint.setShader(gradient);
        invalidate();
    }

}