package com.lenovo.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

public class DrewView extends View {

	
	private float mSweepAngle;
	private float mStartAngle;

	public DrewView(Context context, float start_angle, float sweep_angle) {
		super(context);
		
		mSweepAngle = sweep_angle;
		mStartAngle = start_angle;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setAlpha(70);

		
		Shader mShader = new LinearGradient(0, 0, 0, 60,
				new int[] { 0xffffffff, 0xffbbbbbb }, null, Shader.TileMode.CLAMP); 
		
		p.setShader(mShader);
		//p.setColor(0x60ffffff);
		// p.setColor(Color.BLUE);
		RectF oval2 = new RectF(0, 0, 142, 142);
		canvas.drawArc(oval2, mStartAngle, mSweepAngle, true, p);
	}
	
}
