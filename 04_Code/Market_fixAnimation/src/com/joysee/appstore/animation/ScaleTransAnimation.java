package com.joysee.appstore.animation;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ScaleTransAnimation extends Animation {

	private final float	dx;
	private final float	dy;
	private final float	sx;
	private final float	sy;

	public ScaleTransAnimation(float transXDelta, float transYDelta,
			float scaleXDelta, float scaleYDelta) {
		super();
		this.dx = transXDelta;
		this.dy = transYDelta;
		this.sx = scaleXDelta;
		this.sy = scaleYDelta;
	}
	
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		Matrix matrix = t.getMatrix();
		matrix.postScale(1 - (1 - sx) * interpolatedTime, 1 - (1 - sy)
				* interpolatedTime);
		matrix.postTranslate(1 + dx * interpolatedTime, 1 + dy
				* interpolatedTime);
	}
}
