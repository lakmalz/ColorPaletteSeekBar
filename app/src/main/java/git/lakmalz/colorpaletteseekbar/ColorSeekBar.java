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
/*import android.graphics.Rect;*/
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lakmal Weerasekara on 20/3/17.
 */

public class ColorSeekBar extends View {
    private int BLACK = -251658240;
    private int mBackgroundColor = 0xffffffff;
    private int[] mColorSeeds = new int[]{0xFF000000, 0xFF9900FF, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFF6600, 0xFFFFFF00, 0xFFFFFFFF, 0xFF000000};
    ;
    private int c0, c1, mAlpha, mOpacity, mRed, mGreen, mBlue;
    private float x, y;
    private OnColorChangeListener mOnColorChangeLister;
    private Context mContext;
    private boolean mIsShowAlphaBar = true;
    private boolean mIsShownOpacityBar = true;
    private Bitmap mTransparentBitmap;
    private boolean mMovingColorBar;
    private boolean mMovingAlphaBar;
    private boolean mMovingBrightnessBar;
    private RectF mColorRect;
    private int mThumbHeight = 30;
    private int mBarHeight = 2;
    private LinearGradient mColorGradient;
    private Paint mColorRectPaint;
    private Paint mColorRectPaintStroke;
    private int realLeft;
    private int realRight;
    private int realTop;
    private int realBottom;
    private int mBarWidth;
    private int mMaxPosition;
    private RectF mAlphaRect;
    private RectF mBrightnessRect;
    private int mColorBarPosition;
    private int mAlphaBarPosition;
    private int mBrightnessPosition;
    private float mThumbRadius;
    private int mBarMargin = 5;
    private int mPaddingSize;
    private int mViewWidth;
    private int mViewHeight;
    private List<Integer> mColors = new ArrayList<>();
    private int mColorsToInvoke = -1;
    private boolean mInit = false;
    private boolean mFirstDraw = true;
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

    public void applyStyle(int resId) {
        applyStyle(getContext(), null, 0, resId);
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
        mIsShowAlphaBar = a.getBoolean(R.styleable.ColorSeekBar_showAlphaBar, false);
        mBackgroundColor = a.getColor(R.styleable.ColorSeekBar_bgColor, Color.TRANSPARENT);
        mBarHeight = (int) a.getDimension(R.styleable.ColorSeekBar_barHeight, (float) dp2px(2));
        mThumbHeight = (int) a.getDimension(R.styleable.ColorSeekBar_thumbHeight, (float) dp2px(30));
        mBarMargin = (int) a.getDimension(R.styleable.ColorSeekBar_barMargin, (float) dp2px(5));
        a.recycle();

        if (colorsId != 0) mColorSeeds = getColorsById(colorsId);

        setBackgroundColor(mBackgroundColor);
    }

    /**
     * @param id
     * @return
     */
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

        //init l r t b
        realLeft = getPaddingLeft() + mPaddingSize;
        realRight = getWidth() - getPaddingRight() - mPaddingSize;
        realTop = getPaddingTop() + mPaddingSize;
        realBottom = getHeight() - getPaddingBottom() - mPaddingSize;

        mBarWidth = realRight - realLeft;

        //init rect
        mColorRect = new RectF(realLeft, realTop, realRight, realTop + mBarHeight);

        mColorRectPaintStroke = new Paint();
        mColorRectPaintStroke.setStyle(Paint.Style.STROKE);
        mColorRectPaintStroke.setColor(Color.WHITE);
        mColorRectPaintStroke.setStrokeWidth(3);

        //init paint
        mColorGradient = new LinearGradient(0, 0, mColorRect.width(), 0, mColorSeeds, null, Shader.TileMode.MIRROR);
        mColorRectPaint = new Paint();
        mColorRectPaint.setShader(mColorGradient);
        mColorRectPaint.setAntiAlias(true);
        cacheColors();
        setAlphaValue();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTransparentBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        mTransparentBitmap.eraseColor(Color.TRANSPARENT);
        init();
        mInit = true;
        if(mColorsToInvoke != -1) setColor(mColorsToInvoke);
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
        colorPaint.setColor(Color.WHITE);
        colorPaint.setAntiAlias(true);
        int color = getColor(false);
        int opacityColor = getColor(true);
        int colorToTransparent = Color.argb(0, Color.red(Color.WHITE), Color.green(Color.WHITE), Color.blue(Color.WHITE));
        int[] toAlpha = new int[]{Color.WHITE,Color.WHITE};
        int[] thumbColor = new int[]{Color.WHITE,Color.WHITE};
        //clear
        canvas.drawBitmap(mTransparentBitmap, 0, 0, null);

        //draw color bar
        canvas.drawRoundRect(mColorRect, 25, 25, mColorRectPaintStroke);
        canvas.drawRoundRect(mColorRect, 25, 25, mColorRectPaint);

        //draw color bar thumb
        float thumbX = colorPosition + realLeft;
        float thumbY = mColorRect.top + mColorRect.height() / 2;
        canvas.drawCircle(thumbX, thumbY, mBarHeight / 2 + 8, colorPaint);

        //draw color bar thumb radial gradient shader
        RadialGradient thumbShader = new RadialGradient(thumbX, thumbY, mThumbRadius, thumbColor, null, Shader.TileMode.CLAMP);
        Paint thumbGradientPaint = new Paint();
        thumbGradientPaint.setAntiAlias(true);
        thumbGradientPaint.setShader(thumbShader);
        canvas.drawCircle(thumbX, thumbY, mThumbHeight / 2, thumbGradientPaint);

        if (mIsShowAlphaBar) {
            int top = (int) (mThumbHeight + mThumbRadius + mBarHeight + mBarMargin);

            //init rect
            mAlphaRect = new RectF(realLeft, top, realRight, top + mBarHeight);
            int[] toAlphaColor = new int[]{Color.WHITE, color};
            //draw alpha bar
            Paint alphaBarPaint = new Paint();
            alphaBarPaint.setAntiAlias(true);
            LinearGradient alphaBarShader = new LinearGradient(0, 0, mAlphaRect.width(), 0, toAlphaColor, null, Shader.TileMode
                    .MIRROR);
            alphaBarPaint.setShader(alphaBarShader);
            canvas.drawRoundRect(mAlphaRect, 25, 25, mColorRectPaintStroke);
            canvas.drawRoundRect(mAlphaRect,25,25, alphaBarPaint);

            //draw alpha bar thumb
            float alphaPosition = (float) mAlphaBarPosition / 255 * mBarWidth;
            float alphaThumbX = alphaPosition + realLeft;
            float alphaThumbY = mAlphaRect.top + mAlphaRect.height() / 2;
            canvas.drawCircle(alphaThumbX, alphaThumbY, mBarHeight / 2 + 8, colorPaint);

            //draw alpha bar thumb radial gradient shader
            RadialGradient alphaThumbShader = new RadialGradient(alphaThumbX, alphaThumbY, mThumbRadius, toAlpha, null, Shader
                    .TileMode.MIRROR);
            Paint alphaThumbGradientPaint = new Paint();
            alphaThumbGradientPaint.setAntiAlias(true);
            alphaThumbGradientPaint.setShader(alphaThumbShader);
            canvas.drawCircle(alphaThumbX, alphaThumbY, mThumbHeight / 2, alphaThumbGradientPaint);


        }

        if (mIsShownOpacityBar) {
            int top = (int) (mThumbHeight+mThumbHeight + mThumbRadius + mBarHeight+ mBarHeight + mBarMargin+ mBarMargin);
            mBrightnessRect = new RectF(realLeft, top, realRight, top + mBarHeight);
            int[] toBrightness = new int[]{BLACK, opacityColor};
            //draw alpha bar
            Paint brightnessBarPaint = new Paint();
            brightnessBarPaint.setAntiAlias(true);
            LinearGradient brightnessBarShader = new LinearGradient(0, 0, mBrightnessRect.width(), 0, toBrightness, null, Shader
                    .TileMode
                    .MIRROR);
            brightnessBarPaint.setShader(brightnessBarShader);
            canvas.drawRoundRect(mBrightnessRect, 25, 25, mColorRectPaintStroke);
            canvas.drawRoundRect(mBrightnessRect, 25, 25, brightnessBarPaint);

            //draw alpha bar thumb
            float alphaPosition = (float) mBrightnessPosition / 255 * mBarWidth;
            float alphaThumbX = alphaPosition + realLeft;
            float alphaThumbY = mBrightnessRect.top + mBrightnessRect.height() / 2;
            canvas.drawCircle(alphaThumbX, alphaThumbY, mBarHeight / 2 + 8, colorPaint);

            //draw alpha bar thumb radial gradient shader
            RadialGradient alphaThumbShader = new RadialGradient(alphaThumbX, alphaThumbY, mThumbRadius, toAlpha, null, Shader
                    .TileMode.CLAMP);
            Paint alphaThumbGradientPaint = new Paint();
            alphaThumbGradientPaint.setAntiAlias(true);
            alphaThumbGradientPaint.setShader(alphaThumbShader);
            canvas.drawCircle(alphaThumbX, alphaThumbY, mThumbHeight / 2, alphaThumbGradientPaint);


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
                }  if (mIsShowAlphaBar) {
                if (isOnBar(mAlphaRect, x, y)) {
                    mMovingAlphaBar = true;
                    mMovingBrightnessBar = false;
                }
            }  if (mIsShownOpacityBar) {
                if (isOnBar(mBrightnessRect, x, y)) {
                    mMovingAlphaBar = false;
                    mMovingBrightnessBar = true;
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
                } if (mIsShowAlphaBar) {
                if (mMovingAlphaBar) {
                    float value = (x - realLeft) / mBarWidth * 255;
                    mAlphaBarPosition = (int) value;
                    if (mAlphaBarPosition < 0) mAlphaBarPosition = 0;
                    if (mAlphaBarPosition > 255) mAlphaBarPosition = 255;
                    mOnColorChangeLister.onColorChangeListener(getColor(), true);
                    setAlphaValue();
                }
            } if (mIsShownOpacityBar) {
                if (mMovingBrightnessBar) {
                    float value = (x - realLeft) / mBarWidth * 255;
                    mBrightnessPosition = (int) value;
                    if (mBrightnessPosition < 0) mBrightnessPosition = 0;
                    if (mBrightnessPosition > 255) mBrightnessPosition = 255;
                    mOnColorChangeLister.onColorChangeListener(getOpacityColor(), false);
                    setOpacityValue();
                }
            }
                /*if (mOnColorChangeLister != null && (mMovingAlphaBar || mMovingColorBar))
                    mOnColorChangeLister.onColorChangeListener(mColorBarPosition, mAlphaBarPosition, getColor());*/
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mMovingColorBar = false;
                mMovingAlphaBar = false;
                mMovingBrightnessBar = false;
                break;
        }
        return true;
    }


    /**
     * @param r
     * @param x
     * @param y
     * @return whether MotionEvent is performing on bar or not
     */
    private boolean isOnBar(RectF r, float x, float y) {
        if (r.left - mThumbRadius < x && x < r.right + mThumbRadius && r.top - mThumbRadius < y && y < r.bottom + mThumbRadius) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFirstDraw(){
        return mFirstDraw;
    }



    /**
     * @param value
     * @return color
     */
    private int pickColor(int value) {
        return pickColor((float) value / mMaxPosition * mBarWidth);
    }

    /**
     * @param position
     * @return color
     */
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
//         mAlpha = mix(Color.alpha(c0), Color.alpha(c1), colorPosition);
        mRed = mix(Color.red(c0), Color.red(c1), colorPosition);
        mGreen = mix(Color.green(c0), Color.green(c1), colorPosition);
        mBlue = mix(Color.blue(c0), Color.blue(c1), colorPosition);
        return Color.rgb(mRed, mGreen, mBlue);
    }

    /**
     * @param start
     * @param end
     * @param position
     * @return
     */
    private int mix(int start, int end, float position) {
        return start + Math.round(position * (end - start));
    }

    public int getColor() {
        //return getColor(mIsShowAlphaBar);
        return getColor(true);
    }

    /**
     * @param withAlpha
     * @return
     */
    /*public int getColor(boolean withAlpha) {
        //pick mode
        if (mColorBarPosition >= mColors.size()) {
            int color = pickColor(mColorBarPosition);
            if(withAlpha){
                return color;
            }else {
                return Color.argb(getAlphaValue(), Color.red(color), Color.green(color), Color.blue(color));
            }
        }

        //cache mode
        int color = mColors.get(mColorBarPosition);

        if (withAlpha) {
            return Color.argb(getAlphaValue(), Color.red(color), Color.green(color), Color.blue(color));
        }
        return color;
    }*/
    //------------ by lakmal
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
        int color = mColors.get(mColorBarPosition);

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

    public int darken(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);
        hsv[2] = ((float)mBrightnessPosition / 255);

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

    public int getOpacityColor() {
        int darken = darken(saturationSolor);
        return darken;
    }

    /*private void setOpacityValue() {
        mOpacity = mOpacityBarPosition- 255;
    }*/
    //--------------

    public int getAlphaBarPosition() {
        return mAlphaBarPosition;
    }

    public int getAlphaValue() {
        return mAlpha;
    }

    public interface OnColorChangeListener {
        //        /**
//         * @param colorBarPosition between 0-maxValue
//         * @param alphaBarPosition    between 0-255
//         * @param color         return the color contains alpha value whether showAlphaBar is true or without alpha value
//         */
        //void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color);
        void onColorChangeListener(int color, boolean isAlphaBar);
    }

    /**
     * @param onColorChangeListener
     */
    public void setOnColorChangeListener(OnColorChangeListener onColorChangeListener) {
        this.mOnColorChangeLister = onColorChangeListener;
    }


    public int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Set colors by resource id. The resource's type must be ArrayRes
     *
     * @param resId
     */
    public void setColorSeeds(@ArrayRes int resId) {
        setColorSeeds(getColorsById(resId));
    }

    public void setColorSeeds(int[] colors) {
        mColorSeeds = colors;
        invalidate();
        cacheColors();
        setAlphaValue();
        if (mOnColorChangeLister != null)
            mOnColorChangeLister.onColorChangeListener(getColor(), true);
        //mOnColorChangeLister.onColorChangeListener(mColorBarPosition, mAlphaBarPosition, getColor());
    }

    /**
     * @param color
     * @return the color's position in the bar, if not in the bar ,return -1;
     */
    public int getColorIndexPosition(int color) {
        return mColors.indexOf(color);
    }

    public List<Integer> getColors() {
        return mColors;
    }

    public boolean isShowAlphaBar() {
        return mIsShowAlphaBar;
    }

    private void refreshLayoutParams() {
        mThumbHeight = mThumbHeight < 2 ? 2 : mThumbHeight;
        mBarHeight = mBarHeight < 2 ? 2 : mBarHeight;

        int singleHeight = mThumbHeight + mBarHeight;
        int doubleHeight = mThumbHeight * 2 + mBarHeight * 2 + mBarMargin;

        if (getLayoutParams().height == -2) {
            if (mIsShowAlphaBar) {
                getLayoutParams().height = doubleHeight;
                setLayoutParams(getLayoutParams());
            } else {
                getLayoutParams().height = singleHeight;
                setLayoutParams(getLayoutParams());
            }
        } else if (getLayoutParams().height >= 0) {
            if (mIsShowAlphaBar) {
                getLayoutParams().height = doubleHeight;
                setLayoutParams(getLayoutParams());
            } else {
                getLayoutParams().height = singleHeight;
                setLayoutParams(getLayoutParams());
            }
        }
    }

    public void setShowAlphaBar(boolean show) {
        mIsShowAlphaBar = show;
        refreshLayoutParams();
        invalidate();
        if (mOnColorChangeLister != null)
            mOnColorChangeLister.onColorChangeListener(getColor(), true);
        //mOnColorChangeLister.onColorChangeListener(mColorBarPosition, mAlphaBarPosition, getColor());
    }

    public void setShowBrightnessBar(boolean show) {
        mIsShownOpacityBar = show;
        refreshLayoutParams();
        invalidate();
        if (mOnColorChangeLister != null)
            mOnColorChangeLister.onColorChangeListener(getOpacityColor(), false);
        //mOnColorChangeLister.onColorChangeListener(mColorBarPosition, mAlphaBarPosition, getColor());
    }

    /**
     * @param dp
     */
    public void setBarHeight(float dp) {
        mBarHeight = dp2px(dp);
        refreshLayoutParams();
        invalidate();
    }

    /**
     * @param px
     */
    public void setBarHeightPx(int px) {
        mBarHeight = px;
        refreshLayoutParams();
        invalidate();
    }

    private void setAlphaValue() {
        mAlpha = 255 - mAlphaBarPosition;
    }

    private void setOpacityValue() {
        /*int color1 = colorToDark;
        int color2 = mAlpha+mOpacityBarPosition;*/

        mOpacity = mBrightnessPosition- 255;

//      mOpacity =mOpacityBarPosition-mAlpha;
//      System.out.println("mAlpha : "+mAlpha);
    }

    public void setAlphaBarPosition(int value) {
        this.mAlphaBarPosition = value;
        setAlphaValue();
        invalidate();
    }

    public int getMaxValue() {
        return mMaxPosition;
    }

    public void setMaxPosition(int value) {
        this.mMaxPosition = value;
        invalidate();
        cacheColors();
    }

    /**
     * set margin between bars
     *
     * @param mBarMargin
     */
    public void setBarMargin(float mBarMargin) {
        this.mBarMargin = dp2px(mBarMargin);
        refreshLayoutParams();
        invalidate();
    }

    /**
     * set margin between bars
     *
     * @param mBarMargin
     */
    public void setBarMarginPx(int mBarMargin) {
        this.mBarMargin = mBarMargin;
        refreshLayoutParams();
        invalidate();
    }


    /**
     * Set the value of color bar, if out of bounds , it will be 0 or maxValue;
     *
     * @param value
     */
    public void setColorBarPosition(int value) {
        this.mColorBarPosition = value;
        mColorBarPosition = mColorBarPosition > mMaxPosition ? mMaxPosition : mColorBarPosition;
        mColorBarPosition = mColorBarPosition < 0 ? 0 : mColorBarPosition;
        invalidate();
        if (mOnColorChangeLister != null)
            mOnColorChangeLister.onColorChangeListener(getColor(), true);
        //mOnColorChangeLister.onColorChangeListener(mColorBarPosition, mAlphaBarPosition, getColor());
    }


    /**
     * Set color, it must correspond to the value, if not , setColorBarPosition(0);
     *
     * @paam color
     */
    public void setColor(int color) {
        int withoutAlphaColor = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));

        if (mInit) {
            int value = mColors.indexOf(withoutAlphaColor);
//            mColorsToInvoke = color;
            setColorBarPosition(value);
        } else {
            mColorsToInvoke = color;
        }

    }

    /**
     * set thumb's height by dpi
     *
     * @param dp
     */
    public void setThumbHeight(float dp) {
        this.mThumbHeight = dp2px(dp);
        refreshLayoutParams();
        invalidate();
    }

    /**
     * set thumb's height by pixels
     *
     * @param px
     */
    public void setThumbHeightPx(int px) {
        this.mThumbHeight = px;
        refreshLayoutParams();
        invalidate();
    }

    public int getBarHeight() {
        return mBarHeight;
    }

    public int getThumbHeight() {
        return mThumbHeight;
    }

    public int getBarMargin() {
        return mBarMargin;
    }

    public float getColorBarValue() {
        return mColorBarPosition;
    }

}