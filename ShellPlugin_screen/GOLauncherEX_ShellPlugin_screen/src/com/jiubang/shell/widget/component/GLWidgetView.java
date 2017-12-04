package com.jiubang.shell.widget.component;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLViewWrapper;
import com.go.gl.view.GLViewWrapper.OnOutOfMemoryListner;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.shell.common.listener.OnLayoutListener;

/**
 * 用于普通Widget与3D引擎的映射
 * 
 * @author tanshu
 * 
 */
public class GLWidgetView extends GLViewWrapper implements OnOutOfMemoryListner {

	private static final int ALPHA = 255;
	private static final int OUT_OF_MEMORY_BG = 0x96000000;
	private static final int OUT_OF_MEMORY_PADDING = DrawUtils.dip2px(5);

	private int mChangeAlpha = ALPHA;
	private OnLayoutListener mLayoutListener;
	private boolean mIsOutofMemory = false; //这个widget是否已经在绘制时爆了内存，如果是，绘制一张警告的图片
	private static BitmapGLDrawable sOutofMemoryDrawable; // 内存爆掉的警告图片 // CHECKSTYLE IGNORE THIS LINE

	public GLWidgetView(Context context, View view) {
		super(context);
		setView(view, null);
		if (sOutofMemoryDrawable == null) {
			sOutofMemoryDrawable = new BitmapGLDrawable((BitmapDrawable) getResources()
					.getDrawable(R.drawable.gl_widget_out_of_memory));
		}
		setOnOutOfMemoryListner(this);
		//		setPersistentDrawingCache(false);	//让绘图缓冲一直常驻
	}

	public GLWidgetView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLWidgetView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	//	@Override
	//    protected void onDetachedFromWindow() {
	//    	TextureManager.getInstance().unRegisterTextureListener(this);
	//    	super.onDetachedFromWindow();
	//    }
	//    
	//    @Override
	//    protected void onAttachedToWindow() {
	//    	super.onAttachedToWindow();
	//    	TextureManager.getInstance().registerTextureListener(this);
	//    }

	public static AppWidgetHostView getAppWidgetHostView(GLWidgetView view) {
		if (view != null) {
			View wrappedView = view.getView();
			if (wrappedView != null && wrappedView instanceof AppWidgetHostView) {
				return (AppWidgetHostView) wrappedView;
			}
		}
		return null;
	}

	public static AppWidgetHostView getAppWidgetHostView(GLWidgetView view, int appWidgetId) {
		AppWidgetHostView widgetView = getAppWidgetHostView(view);
		if (widgetView != null && widgetView.getAppWidgetId() == appWidgetId) {
			return widgetView;
		}
		return null;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		final int oldAlpha = canvas.getAlpha();
		if (mChangeAlpha != ALPHA) {
			canvas.multiplyAlpha(mChangeAlpha);
		}
		super.dispatchDraw(canvas);
		if (mChangeAlpha != ALPHA) {
			canvas.setAlpha(oldAlpha);
		}
	}

	public void setAlpha(int alpha) {
		mChangeAlpha = alpha;
	}

	@Override
	public void cleanup() {
		if (getView() != null) {
			getView().setTag(R.id.tag_glwidgetview, null);
		}
		super.cleanup();
	}

	public static void recyleStaticResources() {
		if (sOutofMemoryDrawable != null) {
			sOutofMemoryDrawable.clear();
			sOutofMemoryDrawable = null;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		if (mLayoutListener != null) {
			mLayoutListener.onLayoutFinished(this);
		}
	}

	public void setLayoutListener(OnLayoutListener listener) {
		mLayoutListener = listener;
	}

	@Override
	public void draw(GLCanvas canvas) {
		final int oldAlpha = canvas.getAlpha();
		if (mChangeAlpha != ALPHA) {
			canvas.multiplyAlpha(mChangeAlpha);
		}
		if (mIsOutofMemory) {
			//如果是爆内存状态，给这个widget的四周留白，以便黑色半透明背景不和其他地方相连
			canvas.clipRect(OUT_OF_MEMORY_PADDING, OUT_OF_MEMORY_PADDING, mWidth
					- OUT_OF_MEMORY_PADDING, mHeight - OUT_OF_MEMORY_PADDING);
		}
		super.draw(canvas);
		if (mChangeAlpha != ALPHA) {
			canvas.setAlpha(oldAlpha);
		}
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		// TODO Auto-generated method stub
		if (mIsOutofMemory) {
			drawWidgetError(canvas);
		} else {
			super.onDraw(canvas);
		}
	}

	//绘制widget内存加载错误的画面
	private void drawWidgetError(GLCanvas canvas) {
		Rect drawableRect = sOutofMemoryDrawable.getBounds();
		int save = canvas.save();
		canvas.translate(getWidth() / 2.0f - drawableRect.width() / 2.0f, getHeight() / 2.0f
				- drawableRect.height() / 2.0f);
		sOutofMemoryDrawable.draw(canvas);
		canvas.restoreToCount(save);
	}

	@Override
	public void onOutOfMemory() {
		setOnOutOfMemoryListner(null);
		mIsOutofMemory = true;
		setBackgroundColor(OUT_OF_MEMORY_BG);
	}

	@Override
	public void setOnClickListener(final OnClickListener l) {
		if (getView() != null) {
			getView().setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					l.onClick(GLWidgetView.this);
				}
			});
		} else {
			super.setOnClickListener(l);
		}
	}

	@Override
	public void setOnLongClickListener(final OnLongClickListener l) {
		if (getView() != null) {
			getView().setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					return l.onLongClick(GLWidgetView.this);
				}
			
			});
		} else {
			super.setOnLongClickListener(l);
		}
	}
	
	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		View view = getView();
		if (view != null) {
			view.cancelLongPress();
		}
	}

	@Override
	public void getLoactionInGLViewRoot(int[] location) {
		super.getLoactionInGLViewRoot(location);
		// 因为打开ciclelauncher是带状态栏的，所以要减去状态栏高度
		location[1] -= StatusBarHandler.isHide() ? StatusBarHandler.getStatusbarHeight() : 0;
	}
	
	
}
