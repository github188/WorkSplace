package com.joysee.appstore.animation;

public class AnimUtils {
	
	public final static float VIEW_ALPHA_FLAG=(float) 1.1;//表示动画到达此view时,不作移动动画,直接到达
	public final static float VIEW_ALPHA_NORMAL=(float) 1.0;//恢复之前正常的动画,这个不能随便写

	public static int ANIMATION_DEAYLE_TIME = 100;//不移动动画,直接到达要延迟时间,不会找此view时,它的坐标会不准,主要用于首页翻页
	
	public static int ANIMATION_MOVE_SLOW   = 100;//移动时间
	public static int ANIMATION_MOVE_FAST   = 30;
	
	public static int ANIMATION_PAGE_TIME   =500;//当焦点在首页"推荐"上时,翻页要进行延迟,不然太快.
	
}
