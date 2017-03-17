package git.lakmalz.colorpaletteseekbar;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ColorSeekBar extends View {
    private static final String TAG = "ColorSeekBar";
    private int mBackgroundColor = 0xffffffff;
    private int[] mColorSeeds = new int[]{0xFF000000, 0xFF9900FF, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFF6600, 0xFFFFFF00, 0xFFFFFFFF, 0xFF000000};
    ;
    private int c0, c1, mAlpha, mOpacity, mRed, mGreen, mBlue;
    private float x, y;
    private OnColorChangeListener mOnColorChangeLister;
    private Context mContext;
    private boolean mIsShowAlphaBar = false;
    private boolean mIsShownOpacityBar = false;
    private Bitmap mTransparentBitmap;
    private boolean mMovingColorBar;
    private boolean mMovingAlphaBar;
    private boolean mMovingOpacityBar;
    private Rect mColorRect;
    private int mThumbHeight = 20;
    private int mBarHeight = 2;
    private LinearGradient mColorGradient;
    private Paint mColorRectPaint;
    private int realLeft;
    private int realRight;
    private int realTop;
    private int realBottom;
    private int mBarWidth;
    private int mMaxPosition;
    private Rect mAlphaRect;
    private Rect mOpacityRect;
    private int mColorBarPosition;
    private int mAlphaBarPosition = 0;
    private int mOpacityBarPosition;
    private float mThumbRadius;
    private int mBarMargin = 5;
    private int mPaddingSize;
    private int mViewWidth;
    int colorToDark;
    private int mViewHeight;
    private List<Integer> mColors = new ArrayList<>();
    private int mColorsToInvoke = -1;
    private boolean mInit = false;

    private int thumbSize = 50;
    public ColorSeekBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public ColorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public ColorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mViewWidth = widthMeasureSpec;
        mViewHeight = heightMeasureSpec;

        int speMode = MeasureSpec.getMode(heightMeasureSpec);
        if (speMode == MeasureSpec.AT_MOST || speMode == MeasureSpec.UNSPECIFIED) {
            if (mIsShowAlphaBar) {
                setMeasuredDimension(mViewWidth, mThumbHeight * 2 + mBarHeight * 2 + mBarMargin);
            } else if (mIsShownOpacityBar) {
                setMeasuredDimension(mViewWidth, mThumbHeight * 2 + mBarHeight * 2 + mBarMargin);
            } else {
                setMeasuredDimension(mViewWidth, mThumbHeight + mBarHeight);
            }
        }
    }


    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        //get attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorSeekBar, defStyleAttr, defStyleRes);
        int colorsId = a.getResourceId(R.styleable.ColorSeekBar_colorSeeds, 0);
        mMaxPosition = a.getInteger(R.styleable.ColorSeekBar_maxPosition, 100);
        mColorBarPosition = a.getInteger(R.styleable.ColorSeekBar_colorBarPosition, 0);
        mAlphaBarPosition = a.getInteger(R.styleable.ColorSeekBar_alphaBarPosition, 0);
        mOpacityBarPosition = a.getInteger(R.styleable.ColorSeekBar_opacityBarPosition, 0);
        mIsShowAlphaBar = a.getBoolean(R.styleable.ColorSeekBar_showAlphaBar, false);
        mBackgroundColor = a.getColor(R.styleable.ColorSeekBar_bgColor, Color.TRANSPARENT);
        mBarHeight = (int) a.getDimension(R.styleable.ColorSeekBar_barHeight, (float) dp2px(2));
        mThumbHeight = (int) a.getDimension(R.styleable.ColorSeekBar_thumbHeight, (float) dp2px(30));
        mBarMargin = (int) a.getDimension(R.styleable.ColorSeekBar_barMargin, (float) dp2px(5));
        a.recycle();

        if (colorsId != 0) mColorSeeds = getColorsById(colorsId);

        setBackgroundColor(mBackgroundColor);
    }

    private int[] getColorsById(int id) {
        if (isInEditMode()) {
            String[] s = mContext.getResources().getStringArray(id);
            int[] colors = new int[s.length];
            for (int j = 0; j < s.length; j++) {
                colors[j] = Color.parseColor(s[j]);
            }
            return colors;
        } else {
            TypedArray typedArray = mContext.getResources().obtainTypedArray(id);
            int[] colors = new int[typedArray.length()];
            for (int j = 0; j < typedArray.length(); j++) {
                colors[j] = typedArray.getColor(j, Color.BLACK);
            }
            typedArray.recycle();
            return colors;
        }
    }

    private void init() {

        //init size
        mThumbRadius = mThumbHeight / 2;
        mPaddingSize = (int) mThumbRadius;
        mColorRectPaint = new Paint();

        //init l r t b
        realLeft = getPaddingLeft() + mPaddingSize;
        realRight = getWidth() - getPaddingRight() - mPaddingSize;
        realTop = getPaddingTop() + mPaddingSize;
        realBottom = getHeight() - getPaddingBottom() - mPaddingSize;

        mBarWidth = realRight - realLeft;

        //init rect
        mColorRect = new Rect(realLeft, realTop, realRight, realTop + 50);


        //init paint
        mColorGradient = new LinearGradient(0, 0, mColorRect.width(), 0, mColorSeeds, null, Shader.TileMode.MIRROR);

        mColorRectPaint.setShader(mColorGradient);
        mColorRectPaint.setAntiAlias(true);


        colorToDark = Color.argb(255, Color.red(Color.BLACK), Color.green(Color.BLACK), Color.blue(Color.BLACK));

        cacheColors();
        setAlphaValue();
        setOpacityValue();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mTransparentBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        mTransparentBitmap.eraseColor(Color.TRANSPARENT);
        init();
        mInit = true;
    }


    private void cacheColors() {
        //if the view's size hasn't been initialized. do not cache.
        if (mBarWidth < 1) return;
        mColors.clear();
        for (int i = 0; i <= mMaxPosition; i++) {
            mColors.add(pickColor(i));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float colorPosition = (float) mColorBarPosition / mMaxPosition * mBarWidth;

        Paint colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        int color = getColor(false);
        int opacityColor = getColor(true);

        //draw color bar
        RectF mAlphaRect0 = new RectF(realLeft, 50, realRight, 50 + mBarHeight);

        colorPaint.setStyle(Paint.Style.STROKE);
        colorPaint.setColor(Color.WHITE);
        colorPaint.setStrokeWidth(3);
        canvas.drawRoundRect(mAlphaRect0, 25, 25, colorPaint);
        canvas.drawRoundRect(mAlphaRect0, 25, 25, mColorRectPaint);

        //draw color bar thumb
        float thumbX = colorPosition + realLeft;
        float thumbY = mColorRect.top + mColorRect.height() / 2;
        canvas.drawCircle(thumbX, thumbY, mBarHeight / 2 + 5, colorPaint);
        int[] toThumbNormal = new int[]{Color.WHITE, Color.WHITE};

        //draw color bar thumb radial gradient shader
        RadialGradient thumbShader = new RadialGradient(thumbX, thumbY, mThumbRadius, toThumbNormal, null, Shader.TileMode.MIRROR);
        Paint thumbGradientPaint = new Paint();
        thumbGradientPaint.setAntiAlias(true);
        thumbGradientPaint.setShader(thumbShader);
        canvas.drawCircle(thumbX, thumbY, thumbSize / 2, thumbGradientPaint);

        if (mIsShowAlphaBar) {

            //init rect
            int top = 150;
            mAlphaRect = new Rect(realLeft, top, realRight, top + mBarHeight);
            RectF mAlphaRect1 = new RectF(realLeft, top, realRight, top + mBarHeight);
            int[] toAlpha = new int[]{Color.WHITE, color};
            //draw alpha bar
            Paint alphaBarPaint = new Paint();

            alphaBarPaint.setStyle(Paint.Style.STROKE);
            alphaBarPaint.setColor(Color.WHITE);
            alphaBarPaint.setStrokeWidth(3);
            canvas.drawRoundRect(mAlphaRect1, 25, 25, alphaBarPaint);

            alphaBarPaint.setAntiAlias(true);

            LinearGradient alphaBarShader = new LinearGradient(0, 0, mAlphaRect.width(), 0, toAlpha, null, Shader.TileMode.MIRROR);
            alphaBarPaint.setShader(alphaBarShader);

            alphaBarPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(mAlphaRect1, 25, 25, alphaBarPaint);

            //draw alpha bar thumb
            float alphaPosition = (float) mAlphaBarPosition / 255 * mBarWidth;
            float alphaThumbX = alphaPosition + realLeft;
            float alphaThumbY = mAlphaRect.top + mAlphaRect.height() / 2;
            canvas.drawCircle(alphaThumbX, alphaThumbY, mBarHeight / 2 + 5, colorPaint);

            //draw alpha bar thumb radial gradient shader
            int[] toThumbAlpha = new int[]{Color.WHITE, Color.WHITE};
            RadialGradient alphaThumbShader = new RadialGradient(alphaThumbX, alphaThumbY, mThumbRadius, toThumbAlpha, null, Shader.TileMode.CLAMP);
            Paint alphaThumbGradientPaint = new Paint();
            alphaThumbGradientPaint.setColor(Color.BLACK);
            alphaThumbGradientPaint.setAntiAlias(true);
            alphaThumbGradientPaint.setShader(alphaThumbShader);
            canvas.drawCircle(alphaThumbX, alphaThumbY, thumbSize / 2, alphaThumbGradientPaint);
        }

        if (mIsShownOpacityBar) {
            //init rect
            int top = 250;
            mOpacityRect = new Rect(realLeft, top, realRight, top + mBarHeight);
            RectF mOpacityRect1 = new RectF(realLeft, top, realRight, top + mBarHeight);
            RectF mOpacityRect1White = new RectF(realLeft, top, realRight, top + mBarHeight);

            /*int[] toOpacity = new int[]{colorToDark, opacityColor};*/
            int[] toOpacity = new int[]{colorToDark, opacityColor};
            //draw alpha bar
            Paint alphaBarPaint = new Paint();


            alphaBarPaint.setStyle(Paint.Style.STROKE);
            alphaBarPaint.setColor(Color.WHITE);
            alphaBarPaint.setStrokeWidth(3);
            canvas.drawRoundRect(mOpacityRect1, 25, 25, alphaBarPaint);

            /*alphaBarPaint.setStyle(Paint.Style.FILL);
            alphaBarPaint.setColor(Color.WHITE);
            alphaBarPaint.setStrokeWidth(3);
            canvas.drawRoundRect(mOpacityRect1White, 25, 25, alphaBarPaint);*/

            alphaBarPaint.setAntiAlias(true);
            LinearGradient alphaBarShader = new LinearGradient(0, 0, mOpacityRect.width(), 0, toOpacity, null, Shader.TileMode.CLAMP);
            alphaBarPaint.setShader(alphaBarShader);


            alphaBarPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(mOpacityRect1, 25, 25, alphaBarPaint);

            //draw alpha bar thumb
            float alphaPosition = (float) mOpacityBarPosition / 255 * mBarWidth;
            float alphaThumbX = alphaPosition + realLeft;
            float alphaThumbY = mOpacityRect.top + mOpacityRect.height() / 2;
            canvas.drawCircle(alphaThumbX, alphaThumbY, 20 / 2 + 5, colorPaint);

            int[] toThumbOpacity = new int[]{Color.WHITE, Color.WHITE};
            //draw alpha bar thumb radial gradient shader
            RadialGradient alphaThumbShader = new RadialGradient(alphaThumbX, alphaThumbY, mThumbRadius, toThumbOpacity, null, Shader.TileMode.CLAMP);
            Paint alphaThumbGradientPaint = new Paint();
            alphaThumbGradientPaint.setAntiAlias(true);
            alphaThumbGradientPaint.setShader(alphaThumbShader);
            canvas.drawCircle(alphaThumbX, alphaThumbY, thumbSize / 2, alphaThumbGradientPaint);
        }

        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isOnBar(mColorRect, x, y)) {
                    mMovingColorBar = true;
                }
                if (mIsShowAlphaBar) {
                    if (isOnBar(mAlphaRect, x, y)) {
                        mMovingAlphaBar = true;
                        mMovingOpacityBar = false;
                    }
                }
                if (mIsShownOpacityBar) {
                    if (isOnBar(mOpacityRect, x, y)) {
                        mMovingOpacityBar = true;
                        mMovingAlphaBar = false;

                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);

                if (mMovingColorBar) {
                    float value = (x - realLeft) / mBarWidth * mMaxPosition;
                    mColorBarPosition = (int) value;
                    if (mColorBarPosition < 0) mColorBarPosition = 0;
                    if (mColorBarPosition > mMaxPosition) mColorBarPosition = mMaxPosition;
                    mOnColorChangeLister.onColorChangeListener(getColor(), true);
                }

                if (mIsShowAlphaBar) {
                    if (mMovingAlphaBar) {
                        float value = (x - realLeft) / mBarWidth * 255;

                        mAlphaBarPosition = (int) value;

                        if (mAlphaBarPosition > 0 && mAlphaBarPosition < 255) {
                            if (mAlphaBarPosition < 0) mAlphaBarPosition = 0;
                            if (mAlphaBarPosition > 255) mAlphaBarPosition = 255;
                            mOnColorChangeLister.onColorChangeListener(getColor(), true);
                            setAlphaValue();
                        } else {
                            if (mAlphaBarPosition < 0) mAlphaBarPosition = 0;
                            if (mAlphaBarPosition > 255) mAlphaBarPosition = 255;
                            setAlphaValue();
                        }

                    }
                }

                if (mIsShownOpacityBar) {
                    if (mMovingOpacityBar) {
                        float value = (x - realLeft) / mBarWidth * 255;

                        mOpacityBarPosition = (int) value;


                        if (mOpacityBarPosition > 0 && mOpacityBarPosition < 255) {
                            if (mOpacityBarPosition < 0) mOpacityBarPosition = 0;
                            if (mOpacityBarPosition > 255) mOpacityBarPosition = 255;
                            mOnColorChangeLister.onColorChangeListener(getOpacityColor(), false);
                            setOpacityValue();
                        } else {
                            if (mOpacityBarPosition < 0) mOpacityBarPosition = 0;
                            if (mOpacityBarPosition > 255) mOpacityBarPosition = 255;
                            setOpacityValue();
                        }
                    }
                }


                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mMovingColorBar = false;
                mMovingAlphaBar = false;
                mMovingOpacityBar = false;
                break;
        }
        return true;
    }

    private boolean isOnBar(Rect r, float x, float y) {
        if (r.left - mThumbRadius < x && x < r.right + mThumbRadius && r.top - mThumbRadius < y && y < r.bottom + mThumbRadius) {
            return true;
        } else {
            return false;
        }
    }

    private int pickColor(int value) {
        return pickColor((float) value / mMaxPosition * mBarWidth);
    }

    private int pickOpacityColor(int value) {
        return pickColor((float) value / mMaxPosition * mBarWidth);
    }

    private int pickColor(float position) {
        float unit = position / mBarWidth;
        if (unit <= 0.0)
            return mColorSeeds[0];

        if (unit >= 1)
            return mColorSeeds[mColorSeeds.length - 1];

        float colorPosition = unit * (mColorSeeds.length - 1);
        int i = (int) colorPosition;
        colorPosition -= i;
        c0 = mColorSeeds[i];
        c1 = mColorSeeds[i + 1];
        mRed = mix(Color.red(c0), Color.red(c1), colorPosition);
        mGreen = mix(Color.green(c0), Color.green(c1), colorPosition);
        mBlue = mix(Color.blue(c0), Color.blue(c1), colorPosition);
        return Color.rgb(mRed, mGreen, mBlue);
    }

    private int pickOpacityColor(float position) {
        return getColor(true);
    }

    private int mix(int start, int end, float position) {
        return start + Math.round(position * (end - start));
    }

    public int getColor() {
        return getColor(true);
    }

    public int getOpacityColor() {
        return getOpacityColor(false);
    }

    int saturationSolor = 0;
    public int getColor(boolean withAlpha) {
        if (mColorBarPosition >= mColors.size()) {
            int color = pickColor(mAlphaBarPosition);
            if (withAlpha) {
                return color;
            } else {
                return Color.argb(getAlphaValue(), Color.red(color), Color.green(color), Color.blue(color));
            }
        }

        //cache mode
        int color = mColors.get(mColorBarPosition);
        Log.e(TAG, "Saturation - " + saturationSolor );
        //------------

/*
        ColorUtils.RGBToHSL(red, green, blue, hsl);
        hsl[2] = ((float)mOpacityBarPosition / 255);

        Color.HSVToColor(hsl);*/
        //------------

        if (withAlpha) {
            /*color = Color.argb(getAlphaValue(), Color.red(color), Color.green(color), Color.blue
                    (color));
*/
            //--------------------
            int saturation = saturation(color);
            saturationSolor = saturation;
            //--------------------
            return saturation;
        } else {
            return color;
        }
    }
//-----------
    public int darken(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        hsv[2] = ((float)mOpacityBarPosition / 255);

        return Color.HSVToColor(hsv);
    }

    public int saturation(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        hsv[1] = ((float)mAlphaBarPosition / 255);
        hsv[2] = 1.0f;

        return Color.HSVToColor(hsv);
    }
//---------------
    public int getOpacityColor(boolean withAlpha) {

        //pick mode
       /* if (mOpacityBarPosition > mColors.size()) {
            Log.e(TAG, "getOpacityColor: " + mOpacityBarPosition);
            int mOpacityColor = pickOpacityColor(mOpacityBarPosition);

            if (!withAlpha) {
                return Color.argb(getOpacityValue(), Color.red(mOpacityColor), Color.green(mOpacityColor), Color.blue(mOpacityColor));
            }
        }*/
        //cache mode
        //Log.e(TAG, "getOpacityColor: " + mOpacityBarPosition);
        //int mOpacityColor = pickOpacityColor(mOpacityBarPosition);
        //int mOpacityColor = mOpacityBarPosition;

        int darken = darken(saturationSolor);
        return darken;
        //return Color.argb(getOpacityValue(), Color.red(mOpacityColor), Color.green(mOpacityColor), Color.blue(mOpacityColor));
        /*return darken(Color.argb(getOpacityValue(), Color.red(mOpacityColor), Color.green(mOpacityColor), Color.blue(mOpacityColor)), 0.7);*/

    }

    public int getAlphaValue() {
        return mAlpha;
    }

    public int getOpacityValue() {
        return mOpacity;
    }


    public int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setShowAlphaBar(boolean show) {
        mIsShowAlphaBar = show;

        if (mOnColorChangeLister != null)
            mOnColorChangeLister.onColorChangeListener(getColor(), true);
    }

    public void setShowOpacityBar(boolean show) {
        mIsShownOpacityBar = show;

        if (mOnColorChangeLister != null)
            mOnColorChangeLister.onColorChangeListener(getOpacityColor(), false);
    }

    public void setBarHeight(float dp) {
        mBarHeight = dp2px(dp);
    }

    private void setAlphaValue() {
        mAlpha = mAlphaBarPosition - 255;
    }

    private void setOpacityValue() {
        /*int color1 = colorToDark;
        int color2 = mAlpha+mOpacityBarPosition;*/

        mOpacity = mOpacityBarPosition- 255;

//      mOpacity =mOpacityBarPosition-mAlpha;
//      System.out.println("mAlpha : "+mAlpha);
    }

    public interface OnColorChangeListener {

        void onColorChangeListener(int color, boolean isAlphaBar);
    }

    public void setOnColorChangeListener(OnColorChangeListener onColorChangeListener) {
        this.mOnColorChangeLister = onColorChangeListener;
    }
}