package com.jiubang.shell.screen.back;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.go.gl.graphics.GLCanvas;

/**
 * 
 * @author jiangxuwen
 *
 */
public interface GLIWallpaperDrawer {
	
	public void updateXY(int x, int y);
	
	public void updateOffsetX(int offsetX, boolean drawCycloid);
	
	public void updateOffsetY(int offsetY, boolean drawCycloid);
	
	public void updateScreen(int newScreen, int oldScreen);
	
	public void doDraw(GLCanvas canvas, int bgX, int bgY);
	
	public void drawBackground(GLCanvas canvas);

	public void setBackground(Drawable drawable, Bitmap bitmap);
	
	public Drawable getBackgroundDrawable();
	
	public Bitmap getBackgroundBitmap();
	
	public void setAlpha(int alpha);

	public void setCycloidDrawListener(GLCycloidDrawListener listener);
	
	public void setUpdateBackground(boolean bool);
	
	public boolean needUpdateBackground();
	
	public void setMiddleScrollEnabled(boolean enable);
}
