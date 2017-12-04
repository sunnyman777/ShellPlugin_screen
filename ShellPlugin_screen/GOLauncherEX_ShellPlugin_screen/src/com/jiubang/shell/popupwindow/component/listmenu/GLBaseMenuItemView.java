package com.jiubang.shell.popupwindow.component.listmenu;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.gau.go.gostaticsdk.utiltool.DrawUtils;
import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 菜单Item组件
 * @author yejijiong
 *
 */
public class GLBaseMenuItemView extends ShellTextViewWrapper {
	private int mTitleNum;
	private Drawable mTitleNumBgDrawable;
	private GLDrawable mTitleNumDrawable;
	private GLDrawable mNewDrawable;
	private boolean mNeedShowNewDrawable = false;
	private Paint mTextPaint;
	private Context mShellContext;
	
	public GLBaseMenuItemView(Context context) {
		super(context);
		init();
	}

	public GLBaseMenuItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mTextPaint = new Paint();
		int textSize = DrawUtils.sp2px(16); // 这个16需要与配置文件中的textSize一致
		mTextPaint.setTextSize(textSize);
		mShellContext = ShellAdmin.sShellManager.getContext();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		layoutTitleNum();
		layoutNewDrawable();
	}

	protected void layoutTitleNum() {
		CharSequence text = getText();
		if (text != null && mTitleNum > 0) {
			int fontSize = mShellContext.getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_font_size);
			if (mTitleNumBgDrawable == null) {
				mTitleNumBgDrawable = mShellContext
						.getResources().getDrawable(R.drawable.gl_stat_notify);
			}
			Rect rect = new Rect();
			mTextPaint.getTextBounds(text.toString(), 0, text.length(), rect);
			int numLeft = getPaddingLeft() + rect.width();
			int numTop = (int) ((getHeight() - rect.height()) / 2.0f - mTitleNumBgDrawable
					.getMinimumHeight() / 2.0f);
			if (mTitleNumDrawable != null) {
				mTitleNumDrawable.clear();
			}
			mTitleNumDrawable = GLDrawable.getDrawable(BitmapUtility.composeDrawableText(
					ShellAdmin.sShellManager.getActivity(), mTitleNumBgDrawable,
					String.valueOf(mTitleNum), fontSize));
			mTitleNumDrawable.setBounds(numLeft, numTop,
					numLeft + mTitleNumDrawable.getIntrinsicWidth(),
					+mTitleNumDrawable.getIntrinsicHeight());
		}
	}
	
	private void layoutNewDrawable() {
		if (mNeedShowNewDrawable) {
			if (mNewDrawable == null) {
				mNewDrawable = GLImageUtil.getGLDrawable(R.drawable.gl_new_mark);
			}

			Rect textRect = new Rect();
			mTextPaint.getTextBounds(getText().toString(), 0, getText().length(), textRect);
			int left = textRect.width() + getPaddingLeft() + DrawUtils.dip2px(5);
			if (left > getWidth() - mNewDrawable.getIntrinsicWidth() - getPaddingRight()) {
				left = getWidth() - mNewDrawable.getIntrinsicWidth() - getPaddingRight();
			}
			int top = (textRect.height()) / 3;
			mNewDrawable.setBounds(left, top, left + mNewDrawable.getIntrinsicWidth(), top
					+ mNewDrawable.getIntrinsicHeight());
		}
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);
		CharSequence text = getText();
		if (text != null && mTitleNum > 0) {
			if (mTitleNumBgDrawable != null) {
				canvas.drawDrawable(mTitleNumDrawable);
			}
		}
		
		if (mNeedShowNewDrawable) {
			canvas.drawDrawable(mNewDrawable);
		}
	}
	
	public void setTitleNum(int num) {
		mTitleNum = num;
		layoutTitleNum();
	}
	
	/**
	 * 设置是否显示new标识
	 * @param showNewDrawable
	 */
	public void setNewDrawable(boolean showNewDrawable) {
		mNeedShowNewDrawable = showNewDrawable;
		layoutNewDrawable();
	}
	
	public void cleanup() {
		super.cleanup();
		if (mTitleNumDrawable != null) {
			mTitleNumDrawable.clear();
			mTitleNumDrawable = null;
		}
		if (mNewDrawable != null) {
			mNewDrawable.clear();
			mNewDrawable = null;
		}
	}
}
