package com.jiubang.shell.appdrawer.recentapp;

import java.util.ArrayList;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.appdrawer.GLAppDrawer;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.animation.FlyAnimation;
import com.jiubang.shell.appdrawer.component.GLAbsSandwichContainer;
import com.jiubang.shell.appdrawer.recentapp.actionbar.GLRecentAppActionBar;
import com.jiubang.shell.appdrawer.recentapp.actionbar.GLRecentAppTitleBar;
import com.jiubang.shell.common.listener.RemoveViewAnimationListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLRecentAppContainer extends GLAbsSandwichContainer {

	private GLRecentAppTitleBar mTopNormalActionBar;
	private GLRecentAppActionBar mBottomActionBar;
	private GLRecentAppGridView mGridView;
	private boolean mFirstEnter = true;

	public GLRecentAppContainer(Context context) {
		super(context, context.getResources().getDimensionPixelSize(
				R.dimen.recentapp_top_bar_container_height_v), context.getResources()
				.getDimensionPixelSize(R.dimen.recentapp_bottom_bar_container_height_v),
				context.getResources().getDimensionPixelSize(
						R.dimen.recentapp_top_bar_container_height_h), context.getResources()
						.getDimensionPixelSize(R.dimen.recentapp_bottom_bar_container_height_h));
		setHasPixelOverlayed(false);
		setBackgroundColor(getResources().getColor(R.color.full_layer_background_color));
		initViews();
	}

	private void initViews() {
		mTopNormalActionBar = (GLRecentAppTitleBar) ShellAdmin.sShellManager.getLayoutInflater()
				.inflate(R.layout.gl_appdrawer_recentapp_top_action_bar, null);
		mBottomActionBar = (GLRecentAppActionBar) ShellAdmin.sShellManager.getLayoutInflater()
				.inflate(R.layout.gl_appdrawer_recentapp_action_bar, null);
		mGridView = new GLRecentAppGridView(mContext);
	}

	@Override
	public int getViewId() {
		return IViewId.RECENT_APP;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		super.onAdd(parent);

		mGridViewContainer.setGridView(mGridView);
		mGridViewContainer.showIndicator(false);

		ArrayList<IActionBar> bottomBarList = new ArrayList<IActionBar>(1);
		bottomBarList.add(mBottomActionBar);
		mBottomBarContainer.setBarViewGroup(bottomBarList);

		ArrayList<IActionBar> topBarList = new ArrayList<IActionBar>(2);
		topBarList.add(mTopNormalActionBar);
		mTopBarContainer.setBarViewGroup(topBarList);
		notifyGridDataSetChange();
		mGridView.onAdd();
	}

	@Override
	public void onRemove() {
		mGridView.onRemove();
		super.onRemove();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mFirstEnter) {
			final FlyAnimation animation = new FlyAnimation();
			animation.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
			Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
			alphaAnim.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
			alphaAnim.setAnimationListener(new AnimationListenerAdapter() {

				@Override
				public void onAnimationEnd(Animation animation) {
					if (mListener != null) {
						mListener.extendFuncViewOnEnter(GLRecentAppContainer.this);
					}
					setDrawingCacheEnabled(false);
					mGridViewContainer.showIndicator(true);
				}
			});
			GLRecentAppContainer.this.startAnimation(alphaAnim);
			mTopBarContainer.translateInAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
			mBottomBarContainer.translateInAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
			animation.startFlyAnimation(getWidth(), getHeight(), false,
					mGridView.getCurScreenIcons(), null, mGridView.getNumRows(),
					mGridView.getNumColumns());
			mFirstEnter = false;
		}
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		mBottomBarContainer.clearAnimation();
		mTopBarContainer.clearAnimation();
		if (animate) {

			if (visible) {
				setVisible(visible);
			} else {
				if (mListener != null) {
					mListener.extendFuncViewPreExit(this);
				}
				final FlyAnimation animation = new FlyAnimation();
				animation.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
				Animation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
				alphaAnim.setDuration(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
				mTopBarContainer.translateOutAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
				mBottomBarContainer
						.translateOutAnimation(GLAppDrawer.DURATION_SHOW_EXTEND_FUNC_VIEW);
				if (obj instanceof RemoveViewAnimationListener) {
					animation.startFlyAnimation(getWidth(), getHeight(), true,
							mGridView.getCurScreenIcons(), null, mGridView.getNumRows(),
							mGridView.getNumColumns());
					alphaAnim.setAnimationListener((RemoveViewAnimationListener) obj);
				} else {
					animation.startFlyAnimation(mGridView.getWidth(), mGridView.getHeight(), true,
							mGridView.getCurScreenIcons(), null, mGridView.getNumRows(),
							mGridView.getNumColumns());
					alphaAnim.setAnimationListener(this);
				}
				mGridViewContainer.showIndicator(false);
				startAnimation(alphaAnim);
			}

		} else {
			setVisible(visible);
			if (visible) {
				if (mListener != null) {
					mListener.extendFuncViewOnEnter(this);
				}
			} else {
				if (mListener != null) {
					mListener.extendFuncViewPreExit(this);
				}
			}
		}
	}
}
