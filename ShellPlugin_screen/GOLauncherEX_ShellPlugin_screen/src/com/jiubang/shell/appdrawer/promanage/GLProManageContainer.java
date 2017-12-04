package com.jiubang.shell.appdrawer.promanage;

import java.util.ArrayList;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.AlphaAnimation;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationListenerAdapter;
import com.go.gl.view.GLViewGroup;
import com.go.proxy.MsgMgrProxy;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.GLAppDrawer;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.animation.FlyAnimation;
import com.jiubang.shell.appdrawer.component.GLAbsSandwichContainer;
import com.jiubang.shell.appdrawer.promanage.actionbar.GLProManageActionBar;
import com.jiubang.shell.appdrawer.promanage.actionbar.GLProManageEditActionBar;
import com.jiubang.shell.appdrawer.promanage.actionbar.GLProManageNormalActionBar;
import com.jiubang.shell.common.listener.RemoveViewAnimationListener;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLProManageContainer extends GLAbsSandwichContainer {

	private DragController mDragController;
	private GLProManageEditActionBar mTopEditActionBar;
	private GLProManageNormalActionBar mTopNormalActionBar;
	private GLProManageActionBar mBottomActionBar;
	private GLProManageGridView mGridView;
	private boolean mFirstEnter = true;

	public GLProManageContainer(Context context) {
		super(context, context.getResources().getDimensionPixelSize(
				R.dimen.promanage_top_bar_container_height_v), context.getResources()
				.getDimensionPixelSize(R.dimen.promanage_bottom_bar_container_height_v),
				context.getResources().getDimensionPixelSize(
						R.dimen.promanage_top_bar_container_height_h), context.getResources()
						.getDimensionPixelSize(R.dimen.promanage_bottom_bar_container_height_h));
		setHasPixelOverlayed(false);
		setBackgroundColor(getResources().getColor(R.color.full_layer_background_color));
		initViews();
	}

	private void initViews() {
		mTopNormalActionBar = (GLProManageNormalActionBar) ShellAdmin.sShellManager
				.getLayoutInflater().inflate(R.layout.gl_appdrawer_promanage_top_action_bar, null);
		mTopEditActionBar = new GLProManageEditActionBar(mContext);
		mBottomActionBar = (GLProManageActionBar) ShellAdmin.sShellManager.getLayoutInflater()
				.inflate(R.layout.gl_appdrawer_promanage_action_bar, null);
		mGridView = new GLProManageGridView(mContext);
	}

	@Override
	public int getViewId() {
		return IViewId.PRO_MANAGE;
	}

	@Override
	public void onAdd(GLViewGroup parent) {
		super.onAdd(parent);
		MsgMgrProxy.registMsgHandler(mBottomActionBar);

		mDragController.addDragListener(mTopBarContainer);
		mGridView.setDragController(mDragController);
		mGridViewContainer.setGridView(mGridView);
		mGridViewContainer.showIndicator(false);

		ArrayList<IActionBar> bottomBarList = new ArrayList<IActionBar>(1);
		bottomBarList.add(mBottomActionBar);
		mBottomBarContainer.setBarViewGroup(bottomBarList);

		ArrayList<IActionBar> topBarList = new ArrayList<IActionBar>(2);
		topBarList.add(mTopNormalActionBar);
		topBarList.add(mTopEditActionBar);
		mTopBarContainer.setBarViewGroup(topBarList);
		mDragController.addDropTarget(mGridView, mGridView.getTopViewId());
		mDragController.addDragListener(mGridView);
		mDragController.addDropTarget(mTopEditActionBar, mTopEditActionBar.getTopViewId());
		notifyGridDataSetChange();
		mGridView.onAdd();
	}

	@Override
	public void onRemove() {
		mGridView.onRemove();
		MsgMgrProxy.unRegistMsgHandler(mBottomActionBar);
		if (mDragController != null) {
			mDragController.removeDragListener(mTopBarContainer);
			mDragController.removeDragListener(mGridView);
			mDragController.removeDropTarget(mGridView);
			mDragController.removeDropTarget(mTopEditActionBar);
			mDragController = null;
		}
		//添加小白云
		//		GuideControler cloudView = GuideControler.getInstance(mContext);
		//		cloudView.showSlideMenuGuide();

		super.onRemove();
	}

	@Override
	public void setShell(IShell shell) {
		super.setShell(shell);
		mDragController = shell.getDragController();
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
						mListener.extendFuncViewOnEnter(GLProManageContainer.this);
					}
					setDrawingCacheEnabled(false);
					mGridViewContainer.showIndicator(true);
				}
			});
			GLProManageContainer.this.startAnimation(alphaAnim);
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
