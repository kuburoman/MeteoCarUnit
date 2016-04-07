package cz.meteocar.unit.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import cz.meteocar.unit.R;
import cz.meteocar.unit.engine.log.AppLog;


/**
 *
 */
public class SplashView extends View {

    public class Box{
        int left = 0; int right = 0; int top = 0; int bottom = 0;
    }

    Paint red;
    int width;
    int height;

    Box boxBack;
    Box boxFore;
    int alphaOfBack = 0;
    int alphaOfFore = 0;
    ValueAnimator animatorOfBack;
    ValueAnimator animatorOfFore;
    Bitmap bmpBack;
    Bitmap bmpFore;
    Paint pBack;
    Paint pFore;

    private int TIMEOUT = 5000;

    public SplashView(Context context) {
        super(context);
        init();
    }
    public SplashView(Context context, int time) {
        super(context);
        TIMEOUT = time;
        init();
    }
    public SplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public SplashView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {

        // boxy
        boxBack = new Box();
        boxFore = new Box();

        // červená čára
        red = new Paint();
        red.setColor(Color.RED);
        red.setStrokeWidth(1f);
        red.setStyle(Paint.Style.STROKE);
        red.setStrokeJoin(Paint.Join.ROUND);

        // načtení bitmap
        bmpBack = BitmapFactory.decodeResource(getResources(), R.drawable.metrocar_logo_fullscreen_bg);
        bmpFore = BitmapFactory.decodeResource(getResources(), R.drawable.metrocar_logo_fullscreen_front);

        // paints
        pBack = new Paint();
        pFore = new Paint();

        // fade-in animátor pozadí
        animatorOfFore = new ValueAnimator();
        animatorOfFore.setDuration(TIMEOUT/2);
        animatorOfFore.setIntValues(alphaOfFore, 255);
        animatorOfFore.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                alphaOfFore = (Integer) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animatorOfFore.setStartDelay(TIMEOUT/4);
        animatorOfFore.start();

        // fade-in animátor pozadí
        animatorOfBack = new ValueAnimator();
        animatorOfBack.setDuration(TIMEOUT/2);
        animatorOfBack.setIntValues(alphaOfBack, 255);
        animatorOfBack.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                alphaOfBack = (Integer)valueAnimator.getAnimatedValue();
                invalidate();

            }
        });
        animatorOfBack.start();


    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // rámeček
        Path p = new Path();
        p.moveTo(boxBack.left, boxBack.bottom);
        p.lineTo(boxBack.left,  boxBack.top);
        p.lineTo(boxBack.right, boxBack.top);
        p.lineTo(boxBack.right, boxBack.bottom);
        p.lineTo(boxBack.left, boxBack.bottom);

        // pozadí
        canvas.drawColor(getResources().getColor(R.color.splash_back));

        pBack.setAlpha(alphaOfBack);
        canvas.drawBitmap(bmpBack, null, new Rect(0,0, canvas.getWidth(), canvas.getHeight()), pBack);

        // popředí
        pFore.setAlpha(alphaOfFore);
        canvas.drawBitmap(bmpFore, (float)boxFore.left, (float)boxFore.top, pFore);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int _left, int _top, int _right, int _bottom) {

        // border
        int border = 5;
        width = _right - _left - 2*border - 1;
        height = _bottom - _top - 2*border - 1;

        // box pro pozadí
        boxBack.left = border;
        boxBack.top = border;
        boxBack.right = border + width;
        boxBack.bottom = border + height;

        // box pro popředí (logo + text)
        int realWidth = _right - _left;
        int realHeight = _bottom - _top;
        boxFore.left = _left + (realWidth - bmpFore.getWidth())/2;
        boxFore.top = _top + (realHeight - bmpFore.getHeight())/2;

        // super
        super.onLayout(changed, boxBack.left, boxBack.top, boxBack.right, boxBack.bottom);
    }
}
