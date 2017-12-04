/**
 * 
 */
package com.jiubang.shell.utils;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.graphics.NinePatchGLDrawable;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 图片工具类 封装一些对MImage的操作以方便使用
 * 
 * @author dengweiming
 * 
 */
public class GLImageUtil {
	//	public final static int TILEMODE = 0;
	public final static int STRETCHMODE = 1;
	public final static int CENTERMODE = 2;

	//	/**
	//	 * 绘制平铺图片 指定一个矩形和图片，这个图片根据自己大小填充矩形 当图片大小大于矩形框时，默认拉伸图片与矩形大小一样
	//	 * 
	//	 * @param canvas
	//	 *            画布
	//	 * @param bitmap
	//	 *            图片
	//	 * @param left
	//	 *            左边界
	//	 * @param top
	//	 *            上边界
	//	 * @param right
	//	 *            右边界
	//	 * @param bottom
	//	 *            下边界
	//	 * @param paint
	//	 *            画笔，不能为null
	//	 */
	//
	//	public static void drawTileImage(GLCanvas canvas, Bitmap bitmap, int left, int top, int right,
	//			int bottom, Paint paint) {
	//		if (bitmap.getWidth() > (right - left) || bitmap.getHeight() > (bottom - top)) {
	//			// 图片比矩形大时，让图片自动拉伸成矩形大小
	//			drawStretchImage(canvas, bitmap, left, top, right, bottom, paint);
	//		} else {
	//			Rect rect = new Rect(0, 0, right - left, bottom - top);
	//			BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT,
	//					Shader.TileMode.REPEAT);
	//			Shader shaderBak = paint.getShader();
	//			paint.setShader(shader);
	//			canvas.save();
	//			canvas.translate(left, top);
	//			canvas.drawRect(rect, paint);
	//			paint.setShader(shaderBak);
	//			canvas.restore();
	//		}
	//	}

	/**
	 * 绘制图片于矩形中间 指定一个矩形和图片，这个图片绘制在矩形中间 如果图片大于矩形时，图片将以fit to widow的方式显示
	 * 
	 * @param canvas
	 *            画布
	 * @param bitmap
	 *            图片
	 * @param left
	 *            左边界
	 * @param top
	 *            上边界
	 * @param right
	 *            右边界
	 * @param bottom
	 *            下边界
	 * @param paint
	 *            画笔，不能为null
	 */
	public static void drawCenterImage(GLCanvas canvas, BitmapGLDrawable drawable, int left,
			int top, int right, int bottom, Paint paint) {
		int offsetx = 0;
		int offsety = 0;
		int imageW;
		int imageH;
		int newWidth;
		int newHeight;

		imageW = drawable.getIntrinsicWidth();
		imageH = drawable.getIntrinsicHeight();
		newWidth = right - left;
		newHeight = bottom - top;

		canvas.save();
		if (imageW > newWidth || imageH > newHeight) // 图片大于矩形时
		{
			float factor;
			if (newWidth * imageH > newHeight * imageW) {
				// 以宽度为主进行缩放
				factor = (float) newHeight / imageH;
				offsetx = (newWidth - (int) (factor * imageW)) / 2;
				offsety = (newHeight - (int) (factor * imageH)) / 2;
				canvas.translate(left + offsetx, top + offsety);
				canvas.scale(factor, factor);

			} else {
				// 以高度为主进行缩放
				factor = (float) newWidth / imageW;
				offsetx = (newWidth - (int) (factor * imageW)) / 2;
				offsety = (newHeight - (int) (factor * imageH)) / 2;
				canvas.translate(left + offsetx, top + offsety);
				canvas.scale(factor, factor);
			}
		} else {
			offsetx = (newWidth - imageW) / 2;
			offsety = (newHeight - imageH) / 2;
			canvas.translate(left + offsetx, top + offsety);
		}
		//		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.drawDrawable(drawable);
		canvas.restore();
	}

	/**
	 * 绘制拉伸图片 将图片拉伸至矩形大小显示
	 * 
	 * @param canvas
	 *            画布
	 * @param bitmap
	 *            图片
	 * @param left
	 *            左边界
	 * @param top
	 *            上边界
	 * @param right
	 *            右边界
	 * @param bottom
	 *            下边界
	 * @param paint
	 *            画笔，不能为null
	 */
	public static void drawStretchImage(GLCanvas canvas, BitmapGLDrawable drawable, int left,
			int top, int right, int bottom, Paint paint) {
		final float scaleFactorW = (right - left) / (float) drawable.getIntrinsicWidth();
		final float scaleFactorH = (bottom - top) / (float) drawable.getIntrinsicHeight();

		canvas.save();
		canvas.translate(left, top);
		canvas.scale(scaleFactorW, scaleFactorH);
		//		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.drawDrawable(drawable);
		canvas.restore();
	}

	/**
	 * 绘制图片 根据mode的不同绘制不同方式的图片
	 * 
	 * @param canvas
	 *            画布
	 * @param bitmap
	 *            图片
	 * @param mode
	 *            0 ：平铺； 1 ：拉伸； 2：居中
	 * @param left
	 *            渐变区域的左边界
	 * @param top
	 *            渐变区域的上边界
	 * @param right
	 *            渐变区域的右边界
	 * @param bottom
	 *            渐变区域的下边界
	 * @param paint
	 *            画笔，当平铺时paint不能为null
	 */

	public static void drawImage(GLCanvas canvas, GLDrawable pic, int mode, int left, int top,
			int right, int bottom, Paint paint) {
		if (pic instanceof BitmapGLDrawable) {
			Bitmap bitmap = ((BitmapGLDrawable) pic).getBitmap();
			if ((bitmap != null) && (!bitmap.isRecycled())) {
				switch (mode) {
				//					case TILEMODE :
				//						drawTileImage(canvas, bitmap, left, top, right, bottom, paint);
				//						break;

					case STRETCHMODE :
						drawStretchImage(canvas, (BitmapGLDrawable) pic, left, top, right, bottom,
								paint);
						break;

					case CENTERMODE :
						drawCenterImage(canvas, (BitmapGLDrawable) pic, left, top, right, bottom,
								paint);
						break;
				}
			}
		} else if (pic instanceof NinePatchGLDrawable) {
			pic.setBounds(left, top, right, bottom);
			pic.draw(canvas);
		}
	}

	/**
	 * 绘制图片于矩形中间 指定一个矩形和图片，这个图片绘制在矩形中间 如果图片大于矩形时，图片将以fit to widow的方式显示
	 * 
	 * @param canvas
	 *            画布
	 * @param bitmap
	 *            图片
	 * @param left
	 *            左边界
	 * @param top
	 *            上边界
	 * @param right
	 *            右边界
	 * @param bottom
	 *            下边界
	 * @param paint
	 *            画笔，不能为null
	 */
	public static void drawFitImage(GLCanvas canvas, Bitmap bitmap, int left, int top, int right,
			int bottom, Paint paint) {
		if (bitmap == null) {
			return;
		}
		int offsetx = 0;
		int offsety = 0;
		int imageW;
		int imageH;
		int newWidth;
		int newHeight;

		imageW = bitmap.getWidth();
		imageH = bitmap.getHeight();
		newWidth = right - left;
		newHeight = bottom - top;

		canvas.save();
		canvas.clipRect(left, top, right, bottom);
		float factor;
		if (newWidth * imageH < newHeight * imageW) {
			// 以宽度为主进行缩放
			factor = (float) newHeight / imageH;
			offsetx = (newWidth - (int) (factor * imageW)) / 2;
			offsety = (newHeight - (int) (factor * imageH)) / 2;
			canvas.translate(left + offsetx, top + offsety);
			canvas.scale(factor, factor);

		} else {
			// 以高度为主进行缩放
			factor = (float) newWidth / imageW;
			offsetx = (newWidth - (int) (factor * imageW)) / 2;
			offsety = (newHeight - (int) (factor * imageH)) / 2;
			canvas.translate(left + offsetx, top + offsety);
			canvas.scale(factor, factor);
		}
		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.restore();
	}

	public static GLDrawable getGLDrawable(int resId) {
		GLDrawable drawable = GLDrawable.getDrawable(ShellAdmin.sShellManager.getContext()
				.getResources(), resId);
		return drawable;
	}

	public static GLDrawable getGLDrawable(Drawable drawable) {
		if (drawable != null) {
			if (drawable instanceof GLDrawable) {
				return (GLDrawable) drawable;
			} else {
				try {
					return GLDrawable.getDrawable(drawable);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("This drawable ("
							+ drawable.getClass().getSimpleName()
							+ ") cannot be convert to GLDrawable.");
				}
			}
		}
		return null;
	}
}
