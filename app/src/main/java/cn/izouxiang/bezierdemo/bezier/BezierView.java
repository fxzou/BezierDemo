package cn.izouxiang.bezierdemo.bezier;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by zouxiang on 2016/9/22.
 */

public class BezierView extends View {
    private float lineSmoothness = 0.2f;
    private List<Point> mPointList;
    private Path mPath;
    private Path mAssistPath;
    private float drawScale = 1f;
    private PathMeasure mPathMeasure;
    private float defYAxis = 700f;
    private float defXAxis = 10f;

    public BezierView(Context context) {
        super(context);
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPointList(List<Point> pointList) {
        mPointList = pointList;
        measurePath();
    }

    public void setLineSmoothness(float lineSmoothness) {
        if (lineSmoothness != this.lineSmoothness) {
            this.lineSmoothness = lineSmoothness;
            measurePath();
            postInvalidate();
        }
    }

    public void setDrawScale(float drawScale) {
        this.drawScale = drawScale;
        postInvalidate();
    }

    public void startAnimation(long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "drawScale", 0f, 1f);
        animator.setDuration(duration);
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPointList == null)
            return;
        //measurePath();
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        //绘制辅助线
        canvas.drawPath(mAssistPath,paint);

        paint.setColor(Color.GREEN);
        Path dst = new Path();
        dst.rLineTo(0, 0);
        float distance = mPathMeasure.getLength() * drawScale;
        if (mPathMeasure.getSegment(0, distance, dst, true)) {
            //绘制线
            canvas.drawPath(dst, paint);
            float[] pos = new float[2];
            mPathMeasure.getPosTan(distance, pos, null);
            //绘制阴影
            drawShadowArea(canvas, dst, pos);
            //绘制点
            drawPoint(canvas,pos);
        }
        /*greenPaint.setPathEffect(getPathEffect(mPathMeasure.getLength()));
        canvas.drawPath(mPath, greenPaint);*/
        //mPath.reset();adb shell screenrecord --bit-rate 2000000 /sdcard/test.mp4

    }

    /**
     * 绘制阴影
     * @param canvas
     * @param path
     * @param pos
     */
    private void drawShadowArea(Canvas canvas, Path path, float[] pos) {
        path.lineTo(pos[0], defYAxis);
        path.lineTo(defXAxis, defYAxis);
        path.close();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x88CCCCCC);
        canvas.drawPath(path, paint);
    }

    /**
     * 绘制点
     * @param canvas
     * @param pos
     */
    private void drawPoint(Canvas canvas, final float[] pos){
        Paint redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(3);
        redPaint.setStyle(Paint.Style.FILL);
        for (Point point : mPointList) {
            if (point.x > pos[0]) {
                break;
            }
            canvas.drawCircle(point.x, point.y, 10, redPaint);
        }
    }

    private PathEffect getPathEffect(float length) {
        return new DashPathEffect(new float[]{length * drawScale, length}, 0);
    }

    private void measurePath() {
        mPath = new Path();
        mAssistPath = new Path();
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX;
        float nextPointY;

        final int lineSize = mPointList.size();
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex) {
            if (Float.isNaN(currentPointX)) {
                Point point = mPointList.get(valueIndex);
                currentPointX = point.x;
                currentPointY = point.y;
            }
            if (Float.isNaN(previousPointX)) {
                //是否是第一个点
                if (valueIndex > 0) {
                    Point point = mPointList.get(valueIndex - 1);
                    previousPointX = point.x;
                    previousPointY = point.y;
                } else {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX)) {
                //是否是前两个点
                if (valueIndex > 1) {
                    Point point = mPointList.get(valueIndex - 2);
                    prePreviousPointX = point.x;
                    prePreviousPointY = point.y;
                } else {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1) {
                Point point = mPointList.get(valueIndex + 1);
                nextPointX = point.x;
                nextPointY = point.y;
            } else {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0) {
                // 将Path移动到开始点
                mPath.moveTo(currentPointX, currentPointY);
                mAssistPath.moveTo(currentPointX, currentPointY);
            } else {
                // 求出控制点坐标
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (lineSmoothness * firstDiffX);
                final float firstControlPointY = previousPointY + (lineSmoothness * firstDiffY);
                final float secondControlPointX = currentPointX - (lineSmoothness * secondDiffX);
                final float secondControlPointY = currentPointY - (lineSmoothness * secondDiffY);
                //画出曲线
                mPath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
                //将控制点保存到辅助路径上
                mAssistPath.lineTo(firstControlPointX, firstControlPointY);
                mAssistPath.lineTo(secondControlPointX, secondControlPointY);
                mAssistPath.lineTo(currentPointX, currentPointY);
            }

            // 更新值,
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }
        mPathMeasure = new PathMeasure(mPath, false);
    }

}
