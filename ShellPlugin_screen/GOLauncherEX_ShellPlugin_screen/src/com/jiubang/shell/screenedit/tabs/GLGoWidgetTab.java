package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.commomidentify.IGoLauncherClassName;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Scale3DAnimation;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.file.FileUtil;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLDragLayer;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screenedit.GLGoWidgetAdapter;

/**
 * 类描述:Widget一级界面
 * @author zouguiquan
 *
 */
public class GLGoWidgetTab extends GLGridTab implements BatchAnimationObserver {

	private GLView mFlyView;

	private ArrayList<Drawable> mShortCutInfoDrawables;

	private final static int ON_FLY_FINISH = 1001; 				//添加动画完成时
	public static final float DEFAULT_DENSITY = 240f;
	public static final float VELOCITY_FOR_AUTO_FLY_WIDGET = 4.0f; // 添加widget图标飞的速率，单位px/ms

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ON_FLY_FINISH :
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREEN_AUTO_FLY,
							ScreenEditConstants.ADD_GOSHORTCUTS_ID, msg.obj);

					//用post方法执行removeFlyView()，解决飞入动画最后一帧闪的问题
					post(new Runnable() {

						@Override
						public void run() {
							removeFlyView();
						}
					});
					break;
				default :
					break;
			}
		};
	};

	public GLGoWidgetTab(Context context, int tabId, int level) {
		super(context, tabId, level);
		mPreTabId = ScreenEditConstants.TAB_ID_MAIN;
	}

	@Override
	public ArrayList<Object> requestData() {
		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}
		ArrayList<Object> dataList = new ArrayList<Object>();

		ArrayList<Object> shortCutList = ScreenEditController.getInstance()
				.getWidgetTabShortCutList();
		dataList.addAll(shortCutList);

		ArrayList<Object> widgetList = ScreenEditController.getInstance().getWidgetTabList();
		dataList.addAll(widgetList);

		mShortCutInfoDrawables = ScreenEditController.getInstance().getWidgetTabShortCutDrawable();
		return dataList;
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {
		super.handleAppChanged(msgId, pkgName, showing);

		if (mShortCutInfoDrawables != null) {
			mShortCutInfoDrawables.clear();
		}

		if (showing) {
			refreshData();
		}
	}

	@Override
	public void setParam(Object[] params) {
		if (params != null && params.length > 0) {
			boolean needChangeAnim = (Boolean) params[0];
			setNeedChangeAnim(needChangeAnim);
		}
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		return new GLGoWidgetAdapter(mContext, infoList, mImageLoader);
	}

	@Override
	public Bitmap onLoadImage(int index) {

		Bitmap result = null;
		Drawable drawable = null;
		Object obj = mDataList.get(index);

		if (obj == null) {
			return null;
		}

		if (obj instanceof ShortCutInfo) {

			drawable = mShortCutInfoDrawables.get(index);
			result = mEditController.composeIconMaskBitmap(drawable, false, false);

		} else if (obj instanceof InnerWidgetInfo) {

			InnerWidgetInfo info = (InnerWidgetInfo) obj;
			if (info.getProviderInfo().icon > 0) {
				drawable = m2DContext.getResources().getDrawable(info.mIconId);
				result = mEditController.composeIconMaskBitmap(drawable, false, false);
			}

		} else if (obj instanceof GoWidgetProviderInfo) {

			GoWidgetProviderInfo info = (GoWidgetProviderInfo) obj;
			final String pkgName = info.getPkgName();

			if (pkgName.equals("")) {
				if (info.getProviderInfo().icon > 0) {
					drawable = mContext.getResources().getDrawable(info.getProviderInfo().icon);
				} else if (info.mIconPath != null && info.mIconPath.length() > 0) {
					BitmapDrawable imgDrawable = null;
					if (FileUtil.isFileExist(info.mIconPath)) {
						Bitmap bitmap = BitmapFactory.decodeFile(info.mIconPath);
						final Resources resources = mContext.getResources();
						DisplayMetrics displayMetrics = resources.getDisplayMetrics();
						float density = displayMetrics.densityDpi;
						float scale = density / DEFAULT_DENSITY;
						bitmap = Bitmap.createScaledBitmap(bitmap,
								(int) (bitmap.getWidth() * scale),
								(int) (bitmap.getHeight() * scale), false);
						imgDrawable = new BitmapDrawable(resources, bitmap);
						imgDrawable.setTargetDensity(displayMetrics);
					}
					if (imgDrawable == null) {
						imgDrawable = (BitmapDrawable) ImageExplorer.getInstance(mContext)
								.getDrawable(IGoLauncherClassName.DEFAULT_THEME_PACKAGE,
										info.mIconPath);
					}
					drawable = imgDrawable;
				}
				if (drawable != null) {
					result = mEditController.composeIconMaskBitmap(drawable, false, false);
				}

			} else if (pkgName.equals(PackageName.CLEAN_MASTER_PACKAGE)) {

				if (pkgName.equals(PackageName.CLEAN_MASTER_PACKAGE)) {
					drawable = mContext.getResources().getDrawable(
							R.drawable.gl_screenedit_clean_master_icon);
					result = mEditController.composeIconMaskBitmap(drawable, false, false);
				}

			} else {
				Resources resources = ScreenEditController.getInstance().mFinder
						.getGoWidgetResources(pkgName);
				if (resources != null) {
					drawable = resources.getDrawable(info.getProviderInfo().icon);
					result = mEditController.composeIconMaskBitmap(drawable, false, false);
				}
			}

			if (!info.mInstalled) {
				BitmapDrawable bitmapDrawable = new BitmapDrawable(result);
				bitmapDrawable.mutate();
				ColorMatrix cm = new ColorMatrix();
				cm.setSaturation(0);
				ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
				bitmapDrawable.setColorFilter(cf);
				result = drawableToBitmap(bitmapDrawable);
			}
		}

		return result;
	}

	private Bitmap drawableToBitmap(Drawable drawable) {
		int size = (int) (drawable.getIntrinsicWidth() * DrawUtils.sDensity);
		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, size, size);
		drawable.draw(canvas);
		return bitmap;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long arg3) {
		super.onItemClick(adapter, view, position, arg3);

		Object tag = view.getTag();
		if (tag == null) {
			return;
		}

		if (tag instanceof ShortCutInfo) {						//如果是GO手册  则飞入屏幕进行添加

			if (!checkScreenVacant(1, 1) || mFlyView != null) {
				return;
			}

			int[] centerXY = new int[2]; 						// 将要添加到的位置对应当前屏幕的坐标XY
			float[] translate = new float[2];
			if (checkScreenVacant(1, 1, centerXY, translate)) {
				flyToScreen(view, centerXY, translate); 	// 进行添加的飞行动画
			}
			return;

		} else if (tag instanceof GoWidgetProviderInfo) {

			GoWidgetProviderInfo info = (GoWidgetProviderInfo) tag;
			if (info.mInstalled) {
				try {
					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
							IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
							ScreenEditConstants.TAB_ID_SUB_GOWIDGET, info);
				} catch (Exception ex) {
					String pkgName = info.mGoWidgetPkgName;
					gotoMarketForAPK(pkgName);
				}
			} else {
				// 未安装的去goStore下载
				if (info != null) {
					String pkgName = info.mGoWidgetPkgName;
					String title = info.getProviderInfo().label;
					String content = "xxxxxxxxxxx";
					String[] linkArray = new String[] { pkgName, info.mDownloadUrl };
					CheckApplication.downloadAppFromMarketFTPGostore(m2DContext, content,
							linkArray, LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title,
							System.currentTimeMillis(), GoAppUtils.isCnUser(mContext),
							CheckApplication.FROM_SCREENEDIT, 0, null);
				}
			}
		} else if (tag instanceof InnerWidgetInfo) {
			try {
				MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
						IScreenEditMsgId.SCREEN_EDIT_CHANGE_TAB,
						ScreenEditConstants.TAB_ID_SUB_GOWIDGET, tag);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void clear() {
		super.clear();
		if (mShortCutInfoDrawables != null) {
			mShortCutInfoDrawables.clear();
		}
	}

	/**
	 * 下载GoWidget
	 * 
	 * @param uriString
	 *            电子市场地址
	 * @param item
	 */
	private void gotoMarketForAPK(String pkgName) {
		// 直接跳转到GO Store的该插件的详情界面 再选择下载
		if (pkgName != null) {
			AppsDetail
					.gotoDetailDirectly(m2DContext, AppsDetail.START_TYPE_APPRECOMMENDED, pkgName);
			GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_NO_WIDGET,
					m2DContext);
		}
	}

	/***
	 * 查看桌面是否还能放下指定大小的组件
	 * 
	 * @param spanX
	 *            行
	 * @param spanY
	 *            列
	 * @return
	 */
	private boolean checkScreenVacant(int spanX, int spanY, int[] centerXY, float[] translate) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_EDIT_PRE_ADD, 0, list, centerXY, translate);
	}

	/**
	 * 进行飞行的动画
	 * @param flyView
	 * @param flyType
	 * @param centerXY 长度为2
	 * @param translate 长度为2
	 */
	private void flyToScreen(GLView flyView, int[] centerXY, float[] translate) {

		int[] loc = new int[2];
		ShellAdmin.sShellManager.getShell().getContainer().getLocation(flyView, loc);
		final float scale = ScreenEditConstants.AUTO_FLY_SCALE;
		int centerX = (int) (loc[0] + flyView.getWidth() / 2 - flyView.getWidth() * (scale - 1) / 2);
		int centerY = (int) (loc[1] + flyView.getHeight() / 2 - flyView.getHeight() * (scale - 1)
				/ 2);
		mFlyView = cloneIcon(flyView, centerX, centerY);
		if (mFlyView == null) {
			return;
		}
		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		ShellAdmin.sShellManager.getShell().getDragLayer().addView(mFlyView);

		int offsetX = centerXY[0] - centerX;
		int offsetY = (int) (centerXY[1] - centerY - translate[0]);
		int duration = (int) (Math.sqrt(offsetX * offsetX + offsetY * offsetY + translate[1]
				* translate[1]) / VELOCITY_FOR_AUTO_FLY_WIDGET);
		Animation moveAnimation = new Translate3DAnimation(0, offsetX, 0, -offsetY, 0, translate[1]);
		moveAnimation.setFillEnabled(true);
		moveAnimation.setFillAfter(true);
		moveAnimation.setDuration(duration/*DropAnimation.DURATION_300*/);
		moveAnimation.setInterpolator(InterpolatorFactory
				.getInterpolator(InterpolatorFactory.QUADRATIC));
		Animation scaleAnimation = new Scale3DAnimation(scale, 1f, scale, 1f, 1f, 1f);
		scaleAnimation.setFillEnabled(true);
		scaleAnimation.setFillAfter(true);
		scaleAnimation.setDuration(duration/*DropAnimation.DURATION_300*/);
		scaleAnimation.setInterpolator(InterpolatorFactory
				.getInterpolator(InterpolatorFactory.QUADRATIC));
		AnimationSet set = new AnimationSet(false);
		set.addAnimation(moveAnimation);
		set.addAnimation(scaleAnimation);
		task.addAnimation(mFlyView, set, null);
		task.setBatchAnimationObserver(this, ScreenEditConstants.ADD_GOSHORTCUTS_ID, flyView);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_ENABLE_CELLLAYOUT_DRAWING_CACHE, 1);
		GLAnimationManager.startAnimation(task);
	}

	private GLView cloneIcon(GLView view, int centerX, int centerY) {
		GLView originalIcon = view;
		originalIcon.setVisible(true);
		GLView cloneView = ShellAdmin.sShellManager.getLayoutInflater().inflate(
				R.layout.gl_screen_edit_item, null);
		Object itemInfo = view.getTag();
		if (itemInfo == null) {
			return null;
		}
		GLImageView image = (GLImageView) cloneView.findViewById(R.id.thumb);
		ShellTextViewWrapper text = (ShellTextViewWrapper) cloneView.findViewById(R.id.title);

		if (itemInfo instanceof AppItemInfo) {
			text.setText(((AppItemInfo) itemInfo).mTitle);
			image.setImageDrawable(((AppItemInfo) itemInfo).mIcon);
		} else if (itemInfo instanceof ShortCutInfo) {
			text.setText(((ShortCutInfo) itemInfo).mTitle);
			image.setImageDrawable(((ShortCutInfo) itemInfo).mIcon);
		} else {
			return null;
		}

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
		GLDragLayer.LayoutParams lp = new GLDragLayer.LayoutParams(params);
		lp.width = originalIcon.getWidth();
		lp.height = originalIcon.getHeight();
		lp.x = centerX - lp.width / 2;
		lp.y = centerY - lp.height / 2;
		lp.customPosition = false;
		cloneView.setLayoutParams(lp);
		return cloneView;
	}

	@Override
	public void onStart(int what, Object[] params) {
	}

	@Override
	public void onFinish(int what, Object[] params) {
		switch (what) {
			case ScreenEditConstants.ADD_GOSHORTCUTS_ID :
				if (params != null && params.length > 0) {
					Message msg = new Message();
					msg.what = ON_FLY_FINISH;
					msg.obj = params[0];
					mHandler.sendMessage(msg);
				}
				break;
			default :
				break;
		}
	}

	private void removeFlyView() {
		if (mFlyView != null) {
			ShellAdmin.sShellManager.getShell().getDragLayer().removeView(mFlyView);
			mFlyView.cleanup();
			mFlyView = null;
		}
	}
	
	@Override
	public boolean onKeyBack() {
		if (mFlyView != null) {
			return true;
		}
		
		return super.onKeyBack();
	}
}