package com.jiubang.shell.folder.status;

import java.util.Random;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.proxy.ApplicationProxy;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogStatusObserver;
import com.jiubang.ggheart.apps.desks.diy.pref.PrefConst;
import com.jiubang.ggheart.apps.desks.diy.pref.PrivatePreference;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderInfo;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.appdrawer.component.GLExtrusionGridView;
import com.jiubang.shell.appdrawer.controler.Status;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public abstract class FolderStatus extends Status {
	protected float[] mDragTransInfo = new float[5];
	protected FolderStatusManager mFolderStatusManager;
	protected GLAppFolderBaseGridView<?> mFolderBaseGridView;
	protected GLAppFolderController mFolderController;
	public FolderStatus(GLAppFolderBaseGridView<?> gridView) {
		mFolderStatusManager = FolderStatusManager.getInstance();
		mStatusManager = mFolderStatusManager;
		mFolderBaseGridView = gridView;
		mFolderController = GLAppFolderController.getInstance();
	}

	@Override
	public void setGridView(GLExtrusionGridView gridView) {
		if (gridView instanceof GLAppFolderBaseGridView) {
			mFolderBaseGridView = (GLAppFolderBaseGridView<?>) gridView;
		}

	}

	@Override
	public GLAppFolderBaseGridView<?> getGridView() {

		return mFolderBaseGridView;
	}

	public void onGridViewHide() {
		mDragControler.removeDragListener(mFolderBaseGridView);
		mDragControler.removeDropTarget(mFolderBaseGridView);
		mStatusManager.changeStatus(FolderStatusManager.FOLDER_HIDE_STATUS,
				FolderStatusManager.FOLDER_HIDE_STATUS);
	}

	public void onGridViewShow() {
		mDragControler.addDropTarget(mFolderBaseGridView, mFolderBaseGridView.getTopViewId());
		mDragControler.addDragListener(mFolderBaseGridView);
	}

	public boolean onHomeAction(GestureSettingInfo info) {
		DialogStatusObserver observer = DialogStatusObserver.getInstance();
		if (observer.isDialogShowing()) {
			observer.dismissDialog();
		}
		mFolderStatusManager.changeGridStatus(FolderStatusManager.GRID_NORMAL_STATUS);
		return false;
	}
	
	public boolean onTouchMoveUnderStatus() {
		return false;
	};

	public boolean onTouchUpUnderStatus() {
		return false;
	};

	public boolean onTouchDownUnderStatus() {
		return false;

	};
	
	public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {

	}
	
	public boolean gameFolderAcc() {
		switch (mFolderBaseGridView.getFolderIcon().getFolderInfo().folderType) {
			case GLAppFolderInfo.TYPE_RECOMMAND_GAME :
				boolean gameAcc = PrivatePreference.getPreference(ApplicationProxy.getContext())
						.getBoolean(PrefConst.KEY_GAME_FOLDER_ACCELERATE_SWITCH, true);
				if (gameAcc) {
					AppDrawerControler appDrawerControler = AppDrawerControler
							.getInstance(ApplicationProxy.getContext());
					appDrawerControler.terminateAllProManageTask(appDrawerControler
							.getProManageFunAppItems());
					final WindowManager manager = (WindowManager) ApplicationProxy.getContext()
							.getSystemService(Context.WINDOW_SERVICE);
					LayoutParams layoutParams = new LayoutParams(DrawUtils.dip2px(328),
							DrawUtils.dip2px(70), WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
							LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE,
							PixelFormat.RGBA_8888);
					final FrameLayout frameLayout = new FrameLayout(
							ShellAdmin.sShellManager.getContext());
					frameLayout.setBackgroundResource(R.drawable.gl_game_folder_acc_box_bg);
					layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
					layoutParams.y = DrawUtils.dip2px(123);
					final RelativeLayout relativeLayout = new RelativeLayout(
							ShellAdmin.sShellManager.getContext());
					frameLayout.addView(relativeLayout,
							new android.widget.FrameLayout.LayoutParams(
									android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
									android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
					final ImageView rocketView = new ImageView(ShellAdmin.sShellManager.getContext());
					final TextView textView = new TextView(ShellAdmin.sShellManager.getContext());
					Random random = new Random();
					int accNum = random.nextInt(4) + 21; 
					String gameAccTips = mFolderBaseGridView.getContext().getString(R.string.app_folder_game_acc_tips);
					SpannableString spannableString = new SpannableString(gameAccTips + " " + accNum
							+ "%");
					spannableString.setSpan(new ForegroundColorSpan(0xFF99cc00),
							spannableString.length() - 3, spannableString.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					textView.setText(spannableString);
					
					textView.setVisibility(View.INVISIBLE);
					textView.setGravity(Gravity.CENTER);
					textView.setTextSize(16);
					
					rocketView.setImageResource(R.drawable.gl_game_folder_rocket);
					rocketView.setScaleType(ScaleType.CENTER_INSIDE);
					
					android.widget.RelativeLayout.LayoutParams textViewLayoutParams = new android.widget.RelativeLayout.LayoutParams(
							android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
							android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
					textViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,
							RelativeLayout.TRUE);
//					textViewLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
//							RelativeLayout.TRUE);

					android.widget.RelativeLayout.LayoutParams imageViewLayoutParams = new android.widget.RelativeLayout.LayoutParams(
							android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
							android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
					imageViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
							RelativeLayout.TRUE);
					
					relativeLayout.addView(textView, textViewLayoutParams);
					relativeLayout.addView(rocketView, imageViewLayoutParams);

					final TranslateAnimation translateAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_PARENT, 1.0f,
							Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
//					AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator(1.5f);
//					translateAnimation.setInterpolator(accelerateInterpolator);
					
					final TranslateAnimation textTranslateAnimation = new TranslateAnimation(
							Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
							Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
					
					textTranslateAnimation.setDuration(1500);
					translateAnimation.setDuration(2000);
					
					manager.addView(frameLayout, layoutParams);
					
					frameLayout.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							rocketView.startAnimation(translateAnimation);
							
						}
					}, 1000);
					
					frameLayout.postDelayed(new Runnable() {

						@Override
						public void run() {
							textView.startAnimation(textTranslateAnimation);

						}
					}, 1500);
					translateAnimation.setFillAfter(true);
					textTranslateAnimation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(android.view.animation.Animation animation) {

						}

						@Override
						public void onAnimationRepeat(android.view.animation.Animation animation) {

						}

						@Override
						public void onAnimationEnd(android.view.animation.Animation animation) {
							textView.setVisibility(View.VISIBLE);
							frameLayout.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									try {
										manager.removeViewImmediate(frameLayout);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}, 600);
								}
							});
						}

				break;
			default :
				break;
		}
		return false;
	}
	public void onResume() {
		
	}
}
