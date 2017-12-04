package com.jiubang.shell.screenedit;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.Animation.AnimationListener;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.TranslateAnimation;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditSkinMenu;
import com.jiubang.ggheart.apps.gowidget.BaseWidgetInfo;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.back.GLBackWorkspace;
import com.jiubang.shell.screenedit.tabs.GLBaseTab;
import com.jiubang.shell.screenedit.tabs.GLSysWidgetSubTab;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLScreenEditTitle extends GLLinearLayout
		implements
			AnimationListener,
			GLView.OnClickListener {

	private GLImageView mBackArrow;
	private GLImageView mOptionOne;
	private GLImageView mOptionTwo;
	private ShellTextViewWrapper mTitleName;
	private ShellTextViewWrapper mTitleSummary;

	private ScreenEditSkinMenu mSkinMenu;			//GoWidget皮肤弹出菜单组件

	private Context m2DContext;
	private GLBaseTab mCurrentTab;
	private GLBackWorkspace mBackWorkspace;
	private Animation mShowInAnim;
	private Animation mTabChangeAnim;

	private int mTabAppsOrderType;

	public GLScreenEditTitle(Context context) {
		this(context, null);
	}

	public GLScreenEditTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		m2DContext = ShellAdmin.sShellManager.getActivity();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setupView();
	}
	
	public void setBackWorkspace(GLBackWorkspace backWorkspace) {
		mBackWorkspace = backWorkspace;
	}

	private void setupView() {
		Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

		mBackArrow = (GLImageView) findViewById(R.id.title_back_arrow);
		mBackArrow.setOnClickListener(this);
		mTitleName = (ShellTextViewWrapper) findViewById(R.id.title_name);
		mTitleName.getTextView().setTypeface(typeface);
		mTitleName.setOnClickListener(this);
		mTitleSummary = (ShellTextViewWrapper) findViewById(R.id.title_summary);
		mTitleSummary.getTextView().setTypeface(typeface);
		mTitleSummary.setOnClickListener(this);
		mOptionOne = (GLImageView) findViewById(R.id.title_option_one);
		mOptionOne.setOnClickListener(this);
		mOptionTwo = (GLImageView) findViewById(R.id.title_option_two);
		mOptionTwo.setOnClickListener(this);
	}

	public void setCurrentTab(GLBaseTab baseTab) {
		resetTitle();

		mCurrentTab = baseTab;
		int tabId = baseTab.getTabId();
		switch (tabId) {
			case ScreenEditConstants.TAB_ID_MAIN :
				mBackArrow.setVisibility(GONE);
				mTitleName.setText(R.string.tab_add_main);
				mTitleSummary.setText(R.string.tab_add_main_summary);
				mTitleName.setFocusable(false);
				mTitleSummary.setFocusable(false);
				break;

			case ScreenEditConstants.TAB_ID_APPS :
				mTitleName.setText(R.string.tab_add_apps);
				mTitleSummary.setText(R.string.tab_add_apps_summary);
				break;

			case ScreenEditConstants.TAB_ID_FOLDER :
				mTitleName.setText(R.string.tab_add_folder);
				mTitleSummary.setText(R.string.tab_add_folder_summary);
				break;

			case ScreenEditConstants.TAB_ID_GOWIDGET :
				mTitleName.setText(R.string.tab_add_gowidget);
				mTitleSummary.setText(R.string.tab_add_gowidget_summary);
				break;

			case ScreenEditConstants.TAB_ID_SUB_GOWIDGET :
				if (baseTab.getParam() instanceof BaseWidgetInfo) {
					BaseWidgetInfo widgetInfo = (BaseWidgetInfo) baseTab.getParam();
					mTitleName.setText(widgetInfo.getTitle());
				}
				mTitleSummary.setText(R.string.tab_add_sub_gowidget_summary);
				break;

			case ScreenEditConstants.TAB_ID_SYSTEMWIDGET :
				mTitleName.setText(R.string.tab_add_syswidget);
				mTitleSummary.setText(R.string.tab_add_syswidget_summary);
				break;

			case ScreenEditConstants.TAB_ID_SUB_SYSTEMWIDGET :
				if (baseTab instanceof GLSysWidgetSubTab) {
					GLSysWidgetSubTab subTab = (GLSysWidgetSubTab) baseTab;
					mTitleName.setText(subTab.getWidgetTitle());
				}

				mTitleSummary.setText(R.string.tab_add_sub_syswidget_summary);
				break;

			case ScreenEditConstants.TAB_ID_GOSHORTCUT :
				mTitleName.setText(R.string.tab_add_goshortcut);
				mTitleSummary.setText(R.string.tab_add_goshortcut_summary);
				break;

			case ScreenEditConstants.TAB_ID_WALLPAPER :
				mBackArrow.setVisibility(GONE);
				mTitleName.setText(R.string.tab_add_wallpaper);
				mTitleSummary.setText(R.string.tab_add_wallpaper_summary);
				mTitleName.setFocusable(false);
				mTitleSummary.setFocusable(false);
				break;

			case ScreenEditConstants.TAB_ID_GOWALLPAPER :
				mTitleName.setText(R.string.tab_add_gowallpaper);
				mTitleSummary.setText(R.string.tab_add_gowallpaper_summary);
				break;

			case ScreenEditConstants.TAB_ID_GODYNAMICA_WALLPAPER :
				mTitleName.setText(R.string.tab_add_dyngowallpaper);
				mTitleSummary.setText(R.string.tab_add_dyngowallpaper_summary);
				break;

			case ScreenEditConstants.TAB_ID_EFFECTS :
				mBackArrow.setVisibility(GONE);
				mTitleName.setText(R.string.tab_add_effect);
				mTitleSummary.setText(R.string.tab_add_effect_summary);
				mTitleName.setFocusable(false);
				mTitleSummary.setFocusable(false);
				break;
			default :
				break;
		}
	}

	private void resetTitle() {
		mOptionOne.setVisibility(GONE);
		mOptionTwo.setVisibility(GONE);
		mBackArrow.setVisibility(VISIBLE);
		mTitleName.setFocusable(true);
		mTitleSummary.setFocusable(true);
	}

	public void refreshTitle(int tabId, Object[] objects) {
		switch (tabId) {
			case ScreenEditConstants.TAB_ID_APPS :
			case ScreenEditConstants.TAB_ID_FOLDER :

				mOptionOne.setVisibility(VISIBLE);
				mTabAppsOrderType = (Integer) objects[0];

				if (mTabAppsOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_NAME) {
					mOptionOne.setImageResource(R.drawable.gl_screen_edit_order_by_name);
				} else if (mTabAppsOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_TIME) {
					mOptionOne.setImageResource(R.drawable.gl_screen_edit_order_by_time);
				} else if (mTabAppsOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_FREQUENCY) {
					mOptionOne.setImageResource(R.drawable.gl_screen_edit_order_by_frequency);
				}

				startOptionOneAnim();
				break;

			case ScreenEditConstants.TAB_ID_SUB_GOWIDGET :

				int showSkinImg = (Integer) objects[0];
				if (showSkinImg == VISIBLE) {
					mOptionOne.setVisibility(VISIBLE);
					mOptionOne.setImageResource(R.drawable.gl_screenedit_widget_skin_select);
					startOptionOneAnim();
				}
				break;

			case ScreenEditConstants.TAB_ID_WALLPAPER :

				mOptionOne.setVisibility(VISIBLE);
				boolean isScrolling = (Boolean) objects[0];
				if (isScrolling) {
					mOptionOne.setImageResource(R.drawable.gl_screen_edit_wallpaper_scroll);
				} else {
					mOptionOne.setImageResource(R.drawable.gl_screen_edit_wallpaper_not_scroll);
				}
				startOptionOneAnim();
				break;

			case ScreenEditConstants.TAB_ID_SUB_SYSTEMWIDGET :

				String summary = (String) objects[0];
				mTitleName.setText(summary);
				break;

			default :
				break;
		}
	}

	public void startShowIn(GLBaseTab baseTab, boolean animate) {
		setCurrentTab(baseTab);
		if (animate) {
			setHasPixelOverlayed(false);
			mShowInAnim = new AlphaAnimation(0.0f, 1.0f);
			mShowInAnim.setAnimationListener(this);
			mShowInAnim.setDuration(ScreenEditConstants.DURATION_TAB_ENTER);
			startAnimation(mShowInAnim);
		}
	}

	public void startChangeTabAnim(GLBaseTab changeTab) {

		int offset = getTranslateOffset(changeTab);
		setCurrentTab(changeTab);

		TranslateAnimation tranAnim = new TranslateAnimation(0, 0, 0, offset);
		tranAnim.setDuration(ScreenEditConstants.DURATION_TAB_CHANGE);
		tranAnim.setFillAfter(true);

		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		alphaAnim.setDuration(ScreenEditConstants.DURATION_TAB_CHANGE);
		alphaAnim.setFillAfter(true);

		mTabChangeAnim = new AnimationSet(true);
		((AnimationSet) mTabChangeAnim).addAnimation(tranAnim);
		((AnimationSet) mTabChangeAnim).addAnimation(alphaAnim);
		mTabChangeAnim.setAnimationListener(this);

		setHasPixelOverlayed(false);
		startAnimation(mTabChangeAnim);
	}

	public Animation getChangeTabAnim(GLBaseTab changeTab) {
		int offset = getTranslateOffset(changeTab);

		TranslateAnimation tranAnim = new TranslateAnimation(0, 0, 0, offset);
		tranAnim.setDuration(ScreenEditConstants.DURATION_TAB_CHANGE);
		tranAnim.setFillAfter(true);

		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		alphaAnim.setDuration(ScreenEditConstants.DURATION_TAB_CHANGE);
		alphaAnim.setFillAfter(true);

		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(tranAnim);
		animationSet.addAnimation(alphaAnim);
		animationSet.setAnimationListener(this);

		return animationSet;
	}

	private int getTranslateOffset(GLBaseTab baseTab) {
		int curTabHeight = mCurrentTab.getTabHeight();
		int nextTabHeight = baseTab.getTabHeight();

		return curTabHeight - nextTabHeight;
	}

	@Override
	public void onClick(GLView view) {
		switch (view.getId()) {
			case R.id.title_option_one :

				if (mCurrentTab != null) {

					int tabId = mCurrentTab.getTabId();
					if (tabId == ScreenEditConstants.TAB_ID_SUB_GOWIDGET) {

						showSkinsSelecte(view, (String[]) mCurrentTab.requestTitleInfo());

					} else if (tabId == ScreenEditConstants.TAB_ID_APPS
							|| tabId == ScreenEditConstants.TAB_ID_FOLDER) {

						if (mTabAppsOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_NAME) {
							mCurrentTab.onTitleClick(ScreenEditConstants.TAB_APPS_ORDER_BY_TIME);
						} else if (mTabAppsOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_TIME) {
							mCurrentTab
									.onTitleClick(ScreenEditConstants.TAB_APPS_ORDER_BY_FREQUENCY);
						} else if (mTabAppsOrderType == ScreenEditConstants.TAB_APPS_ORDER_BY_FREQUENCY) {
							mCurrentTab.onTitleClick(ScreenEditConstants.TAB_APPS_ORDER_BY_NAME);
						}

					} else if (tabId == ScreenEditConstants.TAB_ID_WALLPAPER) {

						if (view.getAnimation() != null) {
							return;
						}

						boolean isScrolling = !(Boolean) mCurrentTab.requestTitleInfo();
						ScreenEditController.getInstance().changeCutMode(isScrolling);

						if (isScrolling) {
							mOptionOne.setImageResource(R.drawable.gl_screen_edit_wallpaper_scroll);
							ToastUtils.showToast(R.string.guide_wallpapersetting_scrollable,
									Toast.LENGTH_SHORT);
						} else {
							mOptionOne
									.setImageResource(R.drawable.gl_screen_edit_wallpaper_not_scroll);
							ToastUtils.showToast(R.string.guide_wallpapersetting_locked,
									Toast.LENGTH_SHORT);
						}
						startOptionOneAnim();
					}
				}
				break;
			case R.id.title_option_two :
				break;
			case R.id.title_back_arrow :
			case R.id.title_name :
			case R.id.title_summary :
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_HANDLE_KEY_BACK, -1);
				break;
		}
	}

	/**
	 * 显示皮肤选择
	 * @param v
	 * @param array
	 */
	public void showSkinsSelecte(GLView v, String[] array) {

		if (array == null || array.length <= 1) {
			return;
		}

		if (mSkinMenu == null) {
			mSkinMenu = new ScreenEditSkinMenu(m2DContext, array);
			mSkinMenu.setParrentHeight(v.getHeight());
			mSkinMenu.setmItemClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v instanceof TextView && v.getTag() != null
							&& v.getTag() instanceof Integer) {

						if (null != mSkinMenu) {
							mSkinMenu.dismiss();
						}
						int position = (Integer) v.getTag();
						mCurrentTab.onTitleClick(position);
					}
				}
			});
		}

		mSkinMenu.setStrings(array);
		mSkinMenu.show(ShellAdmin.sShellManager.getShell().getOverlayedViewGroup());
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		setHasPixelOverlayed(true);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
	}

	@Override
	public void onAnimationStart(Animation arg0) {
	}

	@Override
	public void onAnimationProcessing(Animation animation, float interpolator) {
		if (animation == mShowInAnim) {
			if (mBackWorkspace != null) {
				mBackWorkspace.needDrawScreenEditBg(true);
				mBackWorkspace.drawScreenEditBg(true, interpolator);
				mBackWorkspace.invalidate();
			}
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
					IScreenEditMsgId.SCREEN_EDIT_DRAW_BACKGROUND, -1, interpolator);
		}
	}

	/**
	 * 标题右上方功能按键的出现动画
	 */
	private void startOptionOneAnim() {
		mOptionOne.setHasPixelOverlayed(false);
		
		float smallScale = 0.1f;
		float largeScale = 1.2f;
		float upSpan = largeScale - smallScale;
		float downSpan = largeScale - 1.0f;
		long totalDuration = ScreenEditConstants.DURATION_TITLE_COMPONENT;

		long zoomInDuration = (long) (totalDuration * (upSpan / (upSpan + downSpan)));
		long zoomOutDuration = totalDuration - zoomInDuration;
		
		AnimationSet animationSet = new AnimationSet(true);
		
		ScaleAnimation scaleUp = new ScaleAnimation(smallScale, largeScale, smallScale, largeScale,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleUp.setDuration(zoomInDuration);
		
		ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleDown.setStartOffset(zoomInDuration);
		scaleDown.setDuration(zoomOutDuration);
		
		AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		alphaAnim.setDuration(zoomInDuration);
		
		animationSet.addAnimation(scaleUp);
		animationSet.addAnimation(scaleDown);
		animationSet.addAnimation(alphaAnim);
		mOptionOne.startAnimation(animationSet);
	}

	public void startBackArrowAnim() {
		if (mBackArrow.getVisibility() != GONE) {
			mBackArrow.setVisibility(VISIBLE);
			mBackArrow.setHasPixelOverlayed(false);

			AnimationSet animationSet = new AnimationSet(true);
			TranslateAnimation transAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.6f,
					Animation.RELATIVE_TO_SELF, 0.0f, 0, 0, 0, 0);
			transAnim.setDuration(ScreenEditConstants.DURATION_TITLE_COMPONENT);
			AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
			alphaAnim.setDuration(ScreenEditConstants.DURATION_TITLE_COMPONENT);

			animationSet.addAnimation(transAnim);
			animationSet.addAnimation(alphaAnim);
			mBackArrow.startAnimation(animationSet);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
}
