package com.jiubang.shell.appdrawer.promanage.actionbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.widget.GLImageView;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;

/**
 * 正在运行顶部编辑状态栏子组建
 * @author yejijiong
 *
 */
public class GLProManageEditActionBarItem extends GLLinearLayout {

	private GLImageView mIcon;
	private ShellTextViewWrapper mText;
	private int mPicSize; // 图片的大小
	/**
	 * 工具类引用
	 */
	protected AppFuncUtils mUtils;
	/**
	 * 竖屏下图标距离左边距离
	 */
	private int mPaddingV;
	/**
	 * 横屏下图标距离顶部距离
	 */
	//	private int mPaddingH;
	/**
	 * 图标与文字中间距离
	 */
	private int mPaddingTextIcon = 0;
	/**
	 * 图标在竖屏下距顶部距离，横屏下距左边距离
	 */
	private int mTopPadding; // 上边距
	/**
	 * 是否有图标放在上面
	 */
	private boolean mIsBeDragOver = false;
	private Drawable mEditDockBgV; // 编辑状态下的背景(竖屏)
	//	private Drawable mEditDockBgH; // 编辑状态下的背景（横屏）
	private Drawable mEditDockTouchBgV; // 编辑状态下被触摸的背景(竖屏)
	//	private Drawable mEditDockTouchBgH; // 编辑状态下被触摸的背景（横屏）
	private GLAppDrawerThemeControler mThemeCtrl;
	/**
	 * 顶部栏高度
	 */
	private int mTopContainerHeight;

	public GLProManageEditActionBarItem(Context context) {
		super(context);
		mUtils = AppFuncUtils.getInstance(ShellAdmin.sShellManager.getActivity());
		mThemeCtrl = GLAppDrawerThemeControler.getInstance(mContext);
		initView();
		loadResource();
	}

	private void initView() {
		mIcon = new GLImageView(mContext);
		mText = new ShellTextViewWrapper(mContext);
		mText.setTextSize(DrawUtils.px2sp(getResources().getDimensionPixelSize(
				R.dimen.promanage_top_action_bar_item_text_size)));
		mText.setGravity(Gravity.CENTER_VERTICAL);
		setGravity(Gravity.CENTER_VERTICAL);
		LayoutParams paramsIcon = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		addView(mIcon, paramsIcon);
		LayoutParams paramsText = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		addView(mText, paramsText);
		// 初始化边距
		mPaddingTextIcon = mUtils.getStandardSize(1) * 20;
		mPicSize = mUtils.getStandardSize(48);
		mPaddingV = mUtils.getScaledSize(35);
		//		mPaddingH = mUtils.getScaledSize(50);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed) {
			setIsBeDragOver(mIsBeDragOver, true);
			calculatePadding();
//			int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
			//			if (GoLauncherActivityProxy.isPortait()) { // 竖屏
			mIcon.layout(mPaddingV, mTopPadding, mPaddingV + mPicSize, mTopPadding + mPicSize);
//			mText.layout(mTopPadding + mPicSize + mPaddingTextIcon, top + paddingTop / 2, right,
//					bottom);
			mText.layout(mTopPadding + mPicSize + mPaddingTextIcon, 0, mWidth,
					mHeight);
			/*	} else { // 横屏
			mText.setGravity(Gravity.CENTER_HORIZONTAL);
			mIcon.layout(mTopPadding, mPaddingH, mTopPadding + mPicSize, mPaddingH + mPicSize);
			mText.layout(left, mPaddingH + mPicSize + mPaddingTextIcon + paddingTop / 2, right, bottom);
			}*/
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childWidthMeasureSpec;
		int childHeightMeasureSpec;
		mTopContainerHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.appdrawer_top_bar_container_height);
		//		if (GoLauncherActivityProxy.isPortait()) {
		int paddingTop = StatusBarHandler.isHide() ? 0 : StatusBarHandler.getStatusbarHeight();
		mTopContainerHeight += paddingTop;
		int tabWidth = StatusBarHandler.getDisplayWidth() / 2;
		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(tabWidth, MeasureSpec.EXACTLY);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mTopContainerHeight,
				MeasureSpec.EXACTLY);

		/*} else {
		int tabHeight = StatusBarHandler.getDisplayHeight() / 2;
		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mTopContainerHeight,
				MeasureSpec.EXACTLY);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(tabHeight,
				MeasureSpec.EXACTLY);
		}*/
		mText.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	}

	public void setIconDrawable(Drawable drawable) {
		mIcon.setBackgroundDrawable(drawable);
	}

	public void setText(String text) {
		mText.setText(text);
	}

	/**
	 * 
	 * 计算边距
	 * */
	private void calculatePadding() {
//		if (GoLauncherActivityProxy.isPortait()) {
			mTopPadding = (mHeight - mPicSize) / 2;
//		} else {
//			mTopPadding = (mWidth - mPicSize) / 2;
//		}
	}

	/**
	 * 设置背景图片
	 * @param isBeDragOver
	 * @param isCompelReSet 是否强制设置
	 */
	public void setIsBeDragOver(boolean isBeDragOver, boolean isCompelReSet) {
		if (mIsBeDragOver == isBeDragOver && !isCompelReSet) {
			return;
		}
		mIsBeDragOver = isBeDragOver;
//		if (GoLauncherActivityProxy.isPortait()) {
			if (!isBeDragOver) {
				setBackgroundDrawable(mEditDockBgV);
			} else {
				setBackgroundDrawable(mEditDockTouchBgV);
			}
//		} else {
//			if (!isBeDragOver) {
//				setBackgroundDrawable(mEditDockBgH);
//			} else {
//				setBackgroundDrawable(mEditDockTouchBgH);
//			}
//		}
	}

	private void loadResource() {
		mEditDockBgV = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockBgV, true,
				R.drawable.gl_appdrawer_process_edit_dock_v);
		//		mEditDockBgH = mThemeCtrl.getDrawable(
		//				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockBgH, true);
		mEditDockTouchBgV = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockTouchBgV, true,
				R.drawable.gl_appdrawer_process_edit_dock_touch_v);
		//		mEditDockTouchBgH = mThemeCtrl.getDrawable(
		//				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeEditDockTouchBgH, true);
	}
}
