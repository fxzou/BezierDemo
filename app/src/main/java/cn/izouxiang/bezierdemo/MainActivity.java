package cn.izouxiang.bezierdemo;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import cn.izouxiang.bezierdemo.bezier.BezierView;

public class MainActivity extends AppCompatActivity {
    private BezierView mBezierView;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBezierView = (BezierView) findViewById(R.id.bezier);
        mEditText = (EditText) findViewById(R.id.editText);
        List<Point> pointList = new ArrayList<>();
        pointList.add(new Point(10,200));
        pointList.add(new Point(110,300));
        pointList.add(new Point(210,100));
        pointList.add(new Point(310,400));
        pointList.add(new Point(410,100));
        pointList.add(new Point(510,200));
        pointList.add(new Point(610,500));
        mBezierView.setPointList(pointList);
    }
    public void onClick(View v){
        try {
            mBezierView.setLineSmoothness(Float.valueOf(mEditText.getText().toString()));
        } catch (NumberFormatException e) {
        }
        mBezierView.startAnimation(2000);
    }
}
