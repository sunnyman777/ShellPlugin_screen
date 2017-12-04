package com.jiubang.shell.screenedit.tabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.widget.GLImageView;

/**
 * 
 * @author zouguiquan
 *
 */
public class PreviewImage extends GLImageView {

	private Bitmap mPreviewBitmap;
	private boolean mHasDrawPreview;
	
	public PreviewImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public PreviewImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PreviewImage(Context context) {
		super(context);
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		Log.d("zgq", "setImageBitmap width= " + bm.getWidth() + " height= " + bm.getHeight());
		
		super.setImageBitmap(bm);
//		mPreviewBitmap = bm;
//		invalidate();
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		
//		if (mPreviewBitmap != null && !mHasDrawPreview) {
//			mHasDrawPreview = true;
//			
//			canvas.save();
//			
//			int width = getWidth();
//			int height = getHeight();
//			
//			int bitmapWidth = mPreviewBitmap.getWidth();
//			int bitmapHeight = mPreviewBitmap.getHeight();
//
//			Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//			
//			float scale = 1.0f;
//			float translate = 0f;
//			if (width >= height) {
//				scale = (float)width / (float)bitmapWidth;
//				translate = bitmapHeight * scale - height;
//			} else {
//				scale = (float)height / (float)bitmapHeight;
//				translate = (width * scale - width) / 2.0f;
//			}
//
//			Log.d("zgq", "dispatchDraw width= " + width + " height= " + height);
//			Log.d("zgq", "dispatchDraw scale= " + scale + " translate= " + translate);
//			
//			Matrix m = new Matrix();
//			m.setScale(scale * DrawUtils.sDensity, scale * DrawUtils.sDensity);
//			
//			Canvas canvas2 = new Canvas(bitmap);
//			if (width >= height) {
//				canvas2.translate(0, -translate);
//			} else {
//				canvas2.translate(-translate, 0);
//			}
//			canvas2.drawBitmap(mPreviewBitmap, m, null);
//
////			Matrix matrix = new Matrix();
////			canvas.drawBitmap(bitmap, matrix, null);
//			canvas.drawBitmap(bitmap, 0, 0, null);
//			canvas.restore();
//		} else {
//			super.dispatchDraw(canvas);
//		}
		
		super.dispatchDraw(canvas);
	}
}
