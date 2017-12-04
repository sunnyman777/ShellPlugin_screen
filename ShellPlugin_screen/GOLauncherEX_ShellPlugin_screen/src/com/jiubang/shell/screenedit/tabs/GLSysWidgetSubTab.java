package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.FrameLayout;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.AnimationSet;
import com.go.gl.animation.InterpolatorFactory;
import com.go.gl.animation.Translate3DAnimation;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLRelativeLayout.LayoutParams;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.graphics.DrawUtils;
import com.golauncher.message.IAppCoreMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenEditMsgId;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConfig;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditConstants;
import com.jiubang.ggheart.apps.systemwidget.SysSubWidgetInfo;
import com.jiubang.ggheart.apps.systemwidget.SystemWidgetLoader;
import com.jiubang.shell.common.component.GLDragLayer;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.common.management.GLAnimationManager;
import com.jiubang.shell.common.management.GLAnimationManager.AnimationTask;
import com.jiubang.shell.common.management.GLAnimationManager.BatchAnimationObserver;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.screenedit.GLSysWidgetSubView;
import com.jiubang.shell.screenedit.TabFactory;

/**
 * 添加系统小部件二级界面
 * @author zouguiquan
 *
 */
public class GLSysWidgetSubTab extends GLWidgetPrevTab
		implements
			BatchAnimationObserver,
			GLView.OnClickListener {

	private PackageManager mPackageManager;
	private GLView mFlyView;

	private String mWidgetTitle;
	private String mWidgetPkgName;
	private ArrayList<Object> mWidgetAndShortCut;

	private final static int ON_FLY_FINISH = 4000;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ON_FLY_FINISH :

					GLView view = (GLView) msg.obj;
					Object tag = view.getTag();

					if (tag instanceof SysSubWidgetInfo) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
								IScreenFrameMsgId.SCREEN_AUTO_FLY,
								ScreenEditConstants.ADD_SYSTEM_WIDGET_ID, msg.obj);
					} else if (tag instanceof ResolveInfo) {
						MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IScreenEditMsgId.SCREEN_EDIT_ADD_SYSTEM_SHORTCUT, -1, tag);
					}

					// 用post方法执行removeFlyView()，解决飞入动画最后一帧闪的问题
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

	public GLSysWidgetSubTab(Context context, int tabId, int level) {
		super(context, tabId, level);

		mPreTabId = ScreenEditConstants.TAB_ID_SYSTEMWIDGET;
		mImageLoader.setDefaultResource(R.drawable.transparent);

		mPackageManager = context.getPackageManager();
	}

	@Override
	public ArrayList<Object> requestData() {

		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}

		if (mWidgetAndShortCut == null || mWidgetAndShortCut.size() <= 0) {
			mWidgetAndShortCut = SystemWidgetLoader.getSortedWidgetsAndShortcuts(m2DContext);
		}

		return SystemWidgetLoader.getSysWidgetSubList(mWidgetAndShortCut, m2DContext,
				mWidgetPkgName, GLCellLayout.sColumns, GLCellLayout.sRows);
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName, boolean showing) {

		if (mWidgetPkgName.equals(pkgName)) {
			super.handleAppChanged(msgId, pkgName, showing);

			if (mWidgetAndShortCut != null) {
				mWidgetAndShortCut.clear();
			}

			if (showing) {
				if (msgId == IAppCoreMsgId.EVENT_INSTALL_APP
						|| msgId == IAppCoreMsgId.EVENT_INSTALL_PACKAGE) {

					refreshData();

				} else if (msgId == IAppCoreMsgId.EVENT_UNINSTALL_APP
						|| msgId == IAppCoreMsgId.EVENT_UNINSTALL_PACKAGE) {

					GLBaseTab preTab = TabFactory.getInstance(mContext).getTab(mPreTabId);
					preTab.handleAppChanged(msgId, pkgName, false);

					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN_EDIT,
							IScreenEditMsgId.SCREEN_EDIT_HANDLE_KEY_BACK, -1);
				}
			}
		}
	}

	@Override
	public GLView getView(int position) {
		if (mDataList == null) {
			return null;
		}

		Object obj = mDataList.get(position);
		if (obj == null) {
			return null;
		}

		GLSysWidgetSubView sysSubView = (GLSysWidgetSubView) mGlInflater.inflate(
				R.layout.gl_screen_edit_syswidget_subview, null);

		GLImageView widgetPreview = (GLImageView) sysSubView.findViewById(R.id.widgetPreview);
		ShellTextViewWrapper titleView = (ShellTextViewWrapper) sysSubView
				.findViewById(R.id.widgetstyletitle);
		ShellTextViewWrapper formatView = (ShellTextViewWrapper) sysSubView
				.findViewById(R.id.widgetstyleformat);

		if (obj instanceof SysSubWidgetInfo) {
			SysSubWidgetInfo info = (SysSubWidgetInfo) obj;
			if (info.getProviderInfo() == null) {
				return null;
			}

			if (info.getPreviewImage() > 0) {
				sysSubView.setHasPreview(true);
			} else {
				widgetPreview.setBackgroundColor(Color.parseColor("#aa000000"));
				GLImageView widgetIcon = (GLImageView) sysSubView.findViewById(R.id.widgetIcon);
				sysSubView.setColumnAndRow(info.getCellWidth(), info.getCellHeight());
				sysSubView.setHasPreview(false);
				widgetIcon.setVisibility(GLView.VISIBLE);

				Drawable drawable = SystemWidgetLoader.getSysWidgetSubIcon(mContext,
						info.getPkgName(), info.getIconId());
				widgetIcon.setImageDrawable(drawable);
			}

			titleView.setText(info.getLabel());
			formatView.setText(String.format("%dx%d", info.getCellWidth(), info.getCellHeight()));
			info.setPosition(position);

		} else if (obj instanceof ResolveInfo) {
			widgetPreview.setBackgroundColor(Color.parseColor("#aa000000"));
			GLImageView widgetIcon = (GLImageView) sysSubView.findViewById(R.id.widgetIcon);
			sysSubView.setColumnAndRow(1, 1);
			sysSubView.setHasPreview(false);
			widgetIcon.setVisibility(GLView.VISIBLE);

			ResolveInfo info = (ResolveInfo) obj;
			titleView.setText(info.activityInfo.loadLabel(mPackageManager));
			formatView.setText(String.format("%dx%d", 1, 1));

			Drawable drawable = info.activityInfo.loadIcon(mPackageManager);
			widgetIcon.setImageDrawable(drawable);
		}

		mImageLoader.loadImage(widgetPreview, position);

		widgetPreview.setOnClickListener(this);
		widgetPreview.setTag(position);
		sysSubView.setTag(obj);
		return sysSubView;
	}

	@Override
	public Bitmap onLoadImage(int index) {
		
		if (index < 0 || mDataList == null || index >= mDataList.size()) {
			return null;
		}

		Object obj = mDataList.get(index);
		if (obj == null) {
			return null;
		}

		if (obj instanceof SysSubWidgetInfo) {

			SysSubWidgetInfo info = (SysSubWidgetInfo) obj;
			if (info.getProviderInfo() == null) {
				return null;
			}

			AppWidgetProviderInfo providerInfo = info.getProviderInfo();

			if (providerInfo.previewImage > 0) {
				Drawable drawable = SystemWidgetLoader.getWidgetPreviewById(m2DContext,
						providerInfo.provider.getPackageName(), providerInfo.previewImage);
				if (drawable != null) {
					return ((BitmapDrawable) drawable).getBitmap();
				}

			} else {
				Drawable d = SystemWidgetLoader.getSysWidgetSubIcon(mContext, info.getPkgName(),
						info.getIconId());
				return createWidgetPreview((BitmapDrawable) d);
			}

		} else if (obj instanceof ResolveInfo) {
			ResolveInfo info = (ResolveInfo) obj;
			Drawable drawable = info.activityInfo.loadIcon(mPackageManager);
			return createWidgetPreview((BitmapDrawable) drawable);
		}

		return null;
	}

	private Bitmap createWidgetPreview(Drawable drawable) {
		Bitmap oriBitmap = ((BitmapDrawable) drawable).getBitmap();

		Paint paint = new Paint();
		paint.setAlpha(80);
		int width = oriBitmap.getWidth();
		int height = oriBitmap.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height / 2, Config.ARGB_8888);
		Matrix matrix = new Matrix();
		Canvas canvas = new Canvas(bitmap);
		canvas.translate(0, -height / 2);
		canvas.drawBitmap(oriBitmap, matrix, paint);

		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		bitmapDrawable.mutate();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
		bitmapDrawable.setColorFilter(cf);
		return drawableToBitmap(bitmapDrawable);
	}

	private Bitmap drawableToBitmap(Drawable drawable) {
		int width = (int) (drawable.getIntrinsicWidth() * DrawUtils.sDensity);
		int height = (int) (drawable.getIntrinsicHeight() * DrawUtils.sDensity);

		Log.d("zgq", "drawableToBitmap width= " + width + " height= " + height);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setParam(Object[] params) {
		if (params == null) {
			return;
		}
		if (params.length > 0) {
			mWidgetAndShortCut = (ArrayList<Object>) params[0];
		}
		if (params.length > 1) {
			mWidgetPkgName = (String) params[1];
		}
		if (params.length > 2) {
			mWidgetTitle = (String) params[2];
		}
	}

	public String getWidgetTitle() {
		return mWidgetTitle;
	}

	public void onClick(GLView v) {
		if (mFlyView != null) {
			return;
		}

		Object obj = v.getTag();
		if (obj == null || !(obj instanceof Integer)) {
			return;
		}

		int position = (Integer) obj;
		GLView view = getChildAt(position);

		Object tag = view.getTag();
		if (tag == null) {
			return;
		}

		int cellWidth = 1;
		int cellHeight = 1;

		if (tag instanceof SysSubWidgetInfo) {
			SysSubWidgetInfo info = (SysSubWidgetInfo) tag;
			cellWidth = info.getCellWidth();
			cellHeight = info.getCellHeight();
		} else if (tag instanceof ResolveInfo) {
			cellWidth = 1;
			cellHeight = 1;
		}

		int[] centerXY = new int[2]; 								// 将要添加到的位置对应当前屏幕的坐标XY
		float[] translate = new float[2];
		if (!checkScreenVacant(cellWidth, cellHeight, centerXY, translate)) {
			return;
		}

		flyToScreen(view, centerXY, translate, position);	// 进行添加的飞行动画
	}

	private void flyToScreen(GLView flyView, int[] centerXY, float[] translate, int position) {

		int[] loc = new int[2];
		ShellAdmin.sShellManager.getShell().getContainer().getLocation(flyView, loc);
		int centerX = loc[0] + flyView.getWidth() / 2;
		int centerY = loc[1] + flyView.getHeight() / 2;
		mFlyView = cloneIcon(flyView, centerX, centerY, 0.7f, position);

		if (mFlyView == null) {
			return;
		}

		AnimationTask task = new AnimationTask(true, AnimationTask.PARALLEL);
		ShellAdmin.sShellManager.getShell().getDragLayer().addView(mFlyView);
		mFlyView.requestLayout();

		int offsetX = centerXY[0] - centerX;
		int offsetY = (int) (centerXY[1] - centerY - translate[0]);
		int duration = (int) (Math.sqrt(offsetX * offsetX + offsetY * offsetY + translate[1]
				* translate[1]) / GLGoWidgetTab.VELOCITY_FOR_AUTO_FLY_WIDGET);
		Animation moveAnimation = new Translate3DAnimation(0, offsetX, 0, -offsetY, 0, translate[1]);
		moveAnimation.setFillEnabled(true);
		moveAnimation.setFillAfter(true);
		moveAnimation.setDuration(duration);
		moveAnimation.setInterpolator(InterpolatorFactory
				.getInterpolator(InterpolatorFactory.EASE_OUT));

		AnimationSet set = new AnimationSet(false);
		set.addAnimation(moveAnimation);
		task.addAnimation(mFlyView, set, null);
		task.setBatchAnimationObserver(this, -1, flyView);
		MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN, IScreenFrameMsgId.LOCK_SCREEN_TO_SCROLL,
				-1);
		GLAnimationManager.startAnimation(task);
	}

	/**
	 * 
	 * @param view
	 * @param centerX
	 * @param centerY
	 * @param screenScale
	 * @param position
	 * @return
	 */
	private GLView cloneIcon(GLView view, int centerX, int centerY, float screenScale, int position) {

		view.setVisible(true);
		GLSysWidgetSubView cloneView = null;
		Object itemInfo = view.getTag();

		if (itemInfo == null || mGlInflater == null) {
			return null;
		}

		GLSysWidgetSubView originView = (GLSysWidgetSubView) view;
		GLImageView originPreview = (GLImageView) originView.findViewById(R.id.widgetPreview);
		int originPreviewWidth = originPreview.getLayoutParams().width;
		int originPreviewHeight = originPreview.getLayoutParams().height;

		cloneView = (GLSysWidgetSubView) mGlInflater.inflate(
				R.layout.gl_screen_edit_syswidget_subview, null);

		GLImageView widgetPreview = (GLImageView) cloneView.findViewById(R.id.widgetPreview);
		GLRelativeLayout.LayoutParams paramss = (LayoutParams) widgetPreview.getLayoutParams();
		paramss.width = originPreviewWidth;
		paramss.height = originPreviewHeight;
		cloneView.findViewById(R.id.widgetstyletitle).setVisibility(GLView.GONE);
		cloneView.findViewById(R.id.widgetstyleformat).setVisibility(GLView.GONE);

		if (itemInfo instanceof SysSubWidgetInfo) {

			SysSubWidgetInfo info = (SysSubWidgetInfo) itemInfo;
			if (info.getProviderInfo() == null) {
				return null;
			}

			if (info.getPreviewImage() > 0) {
				cloneView.setHasPreview(true);
			} else {
				GLImageView widgetIcon = (GLImageView) cloneView.findViewById(R.id.widgetIcon);
				widgetIcon.setVisibility(GLView.VISIBLE);
				cloneView.setColumnAndRow(info.getCellWidth(), info.getCellHeight());
				cloneView.setHasPreview(false);

				Drawable drawable = SystemWidgetLoader.getSysWidgetSubIcon(mContext,
						info.getPkgName(), info.getIconId());
				widgetIcon.setImageDrawable(drawable);
			}

		} else if (itemInfo instanceof ResolveInfo) {

			//			GLImageView widgetIcon = (GLImageView) cloneView.findViewById(R.id.widgetIcon);
			//			widgetIcon.setVisibility(GLView.VISIBLE);
			cloneView.setColumnAndRow(1, 1);
			cloneView.setHasPreview(false);

			//			ResolveInfo info = (ResolveInfo) itemInfo;
			//			Drawable drawable = info.activityInfo.loadIcon(mPackageManager);
			//			widgetIcon.setImageDrawable(drawable);
		}

		mImageLoader.loadImage(widgetPreview, position);
		cloneView.setTag(itemInfo);

		if (cloneView != null) {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
			GLDragLayer.LayoutParams lp = new GLDragLayer.LayoutParams(params);
			lp.width = view.getWidth();
			lp.height = view.getHeight();
			lp.x = centerX - lp.width / 2;
			lp.y = centerY - lp.height / 2;
			lp.customPosition = false;
			cloneView.setLayoutParams(lp);
		}

		return cloneView;
	}

	/***
	 * 查看桌面是否还能放下指定大小的组件
	 * @param spanX 行
	 * @param spanY 列
	 * @return
	 */
	public boolean checkScreenVacant(int spanX, int spanY, int[] centerXY, float[] translate) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);
		return MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_EDIT_PRE_ADD, 0, list, centerXY, translate);
	}

	@Override
	public void onStart(int what, Object[] params) {
	}

	@Override
	public void onFinish(int what, Object[] params) {
		if (params != null && params.length > 0) {
			Message msg = new Message();
			msg.what = ON_FLY_FINISH;
			msg.obj = params[0];
			mHandler.sendMessage(msg);
		}
	}

	private void removeFlyView() {
		if (mFlyView != null) {
			mFlyView.cleanup();
			ShellAdmin.sShellManager.getShell().getDragLayer().removeView(mFlyView);
			mFlyView = null;
		}
	}

	@Override
	public boolean onBackExit() {
		if (ScreenEditConfig.sEXTERNAL_FROM_ID == ScreenEditConstants.EXTERNAL_ID_APPDRAWER_SLIDEMENU) {
			return true;
		}
		return super.onBackExit();
	}

	@Override
	public boolean onKeyBack() {
		if (mFlyView != null) {
			return true;
		}
		return super.onKeyBack();
	}
}