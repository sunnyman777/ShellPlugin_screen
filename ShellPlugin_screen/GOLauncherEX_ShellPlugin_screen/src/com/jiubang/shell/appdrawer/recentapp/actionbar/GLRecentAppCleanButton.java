package com.jiubang.shell.appdrawer.recentapp.actionbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLFrameLayout;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.shell.appdrawer.component.GLCleanView;
import com.jiubang.shell.appdrawer.component.GLCleanView.OnCleanButtonClickListener;
import com.jiubang.shell.common.component.GLCircleProgressBar;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLRecentAppCleanButton extends GLFrameLayout {
	private static final int PROGRESS_BAR_BG_ALPHA = 64;
	private GLCircleProgressBar mProgressBar;
	private GLCleanView mCleanView;

	private GLDrawable mCleanViewStyle;

	private int mCleanViewColor;

	private AppDrawerControler mAppDrawerControler;

	public GLRecentAppCleanButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		loadResource(false);

		mProgressBar = new GLCircleProgressBar(context);
		int stroke = getResources()
				.getDimensionPixelSize(R.dimen.promanage_memory_bar_stroke_width);
		mProgressBar.setStroke(stroke);
		mProgressBar.setBackgroundColor(mCleanViewColor);
		mProgressBar.setBackgroundAlpha(PROGRESS_BAR_BG_ALPHA);
		mProgressBar.setProgressColor(mCleanViewColor);
		addView(mProgressBar);
		mCleanView = new GLCleanView(context);
		addView(mCleanView);
	}

	public void loadResource(boolean useTheme) {
		if (mCleanViewStyle != null) {
			mCleanViewStyle.clear();
		}
		mCleanViewStyle = GLImageUtil
				.getGLDrawable(R.drawable.gl_appdrawer_recentapp_clean_button_bg);

		mCleanViewColor = getResources().getColor(R.color.recentapp_progress_yellow);
	}

	@Override
	protected void onFinishInflate() {
		mCleanView.setBg(mCleanViewStyle);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mProgressBar.layout(0, 0, mWidth, mHeight);
		int offset = DrawUtils.dip2px(8);
		mCleanView.layout(offset, offset, mWidth - offset, mHeight - offset);
	}

	@Override
	public void setPressed(boolean pressed) {
		mCleanView.setPressed(pressed);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				setPressed(true);
				break;
			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				setPressed(false);
				break;
			default :
				break;
		}
		return super.onTouchEvent(event);
	}

	public void setOnCleanButtonClickListener(OnCleanButtonClickListener listener) {
		mCleanView.setOnCleanButtonClickListener(listener);
	}
}
