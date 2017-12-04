package com.jiubang.shell;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import com.go.gl.view.GLView;
import com.go.gowidget.core.WidgetCallback;
import com.jiubang.ggheart.apps.desks.diy.FrameControl;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.shell.common.component.GLDragLayer;
import com.jiubang.shell.common.component.ShellContainer;
import com.jiubang.shell.drag.DragController;
import com.jiubang.shell.effect.EffectController.EffectListener;
import com.jiubang.shell.folder.BaseFolderIcon;
import com.jiubang.shell.popupwindow.PopupWindowControler;

/**
 * @author yangguanxiang
 *
 */
public interface IShell {
	public static final int STAGE_NONE = -1;
	public static final int STAGE_SCREEN = 0;
	public static final int STAGE_APP_DRAWER = 1;
	public static final int STAGE_APP_FOLDER = 2;
	public static final int STAGE_PREVIEW = 3;
	/**
	 * 获取Context
	 * @return
	 */
	public Context getContext();

	/**
	 * 获取Activity，在弹出对话框时用到
	 * @return
	 */
	public Activity getActivity();
	/**
	 * 获取DragLayer
	 * @return
	 */
	public GLDragLayer getDragLayer();
	
	/**
	 * 获取Container
	 * @return
	 */
	public ShellContainer getContainer();

	/**
	 * 获取FrameControl
	 * @return
	 */
	public FrameControl getFrameControl();
	
	/**
	 * 判断View是否可见
	 * 
	 * @param viewId
	 */
	public boolean isViewVisible(int viewId);
	
	/**
	 * 获取view
	 * 
	 * @param viewId
	 * @return
	 */
	public GLView getView(int viewId);

	/**
	 * 隐藏View
	 * 
	 * @param viewId
	 */
	public void close(int viewId, boolean animate);

	public void onCloseStart(int viewId);

	public void onCloseEnd(int viewId);

	/**
	 * 显示View
	 * 
	 * @param viewId
	 */
	public GLView show(int viewId, boolean animate, Object... objects);
	
	public void show(IView iView, boolean animate, Object... objects) throws IllegalArgumentException;
	
	public GLView hide(int viewId, boolean animate, Object... objects);
	
	public GLView remove(int viewId, boolean animate, Object... objects);
	
	public void showStage(int stage, boolean animate, Object... objects);

	public void onShowStart(int viewId);

	public void onShowEnd(int viewId);
	
	/**
	 * 启动Activity
	 * 
	 * @param intent
	 * @param forResult 是否调用startActivityForResult
	 */
	public void startActivitySafely(Intent intent);

	/**
	 * 启动activity
	 * @param intent
	 * @param requestCode
	 */
	public void startActivityForResultSafely(Intent intent, int requestCode);

	/**
	 * 把Runnable post到渲染线程上执行
	 * @param runnable
	 */
	public void postRunnableSafely(Runnable runnable);
	
	/**
	 * 显示对话框
	 * 
	 * @param dialogId
	 * @param tag
	 */
	public void showDialog(int dialogId, Object tag);

	/**
	 * 隐藏对话框
	 * 
	 * @param dialogId
	 */
	public void closeDialog(int dialogId);

//	/**
//	 * 打开文件夹的方法
//	 * @param folderIcon     点击的文件夹所对应的图标
//	 * @param folderInfo   
//	 * @param rect           点击的文件夹的全局区域
//	 * @param whereOpen   	   在哪里打开文件夹
//	 * @param isInEditState  是否处于编辑状态        
//	 * @param animation      是否显示动画      
//	 */
//	public void openFolder(FolderViewContainer folderViewContainer, PointF startPointF);
//
//	/**
//	 * 编辑文件夹
//	 */
//	public void editFolder(List<ShortcutInfo> selectedList, int iviewId, int iMessageId, AppSelectListener listener);
//
//	/**
//	 * 关闭文件夹编辑框
//	 */
//	public void closeEditFolder();
//
//	public FolderSelectAppView getEditFolder();
//
//	/**
//	 * 关闭文件夹
//	 * 
//	 * @param folder
//	 */
//	public void closeFolder();
//
//	/**
//	 * 是否有文件夹打开的判断方法 
//	 * @return
//	 */
//	public boolean isFolderOpen();
//
//	/**
//	 * 取消文件夹正在打开时的动画（这时动画还未完成），并把文件夹关闭。用于处理一些极短时间内发生事件响应的极端情况
//	 * 
//	 * @param folder
//	 * @param animation      是否显示动画      
//	 */
//	public void cancelFolder(boolean animation);
//
//	/**
//	 * 文件夹关闭后回调
//	 */
//	public void onFolderClose(int openFolderType, boolean isInEditState);

	/**
	 * 振动
	 */
	public void vibrate();

//	public void setTmpCell(int[] cell, int screen);
//
//	/**
//	 * 横竖屏时恢复功能表的打开文件夹状态的方法
//	 */
//	public void restoreAppDrawerFolderStatus();
//
//	/**
//	 * 横竖屏时恢复功能表的打开GO Widget样式选择面板状态的方法
//	 */
//	public void restoreAppDrawerGoWidgetStylePanel();

	/**
	 * 获取GOWidget管理类
	 * @return
	 */
	public GoWidgetManager getGoWidgetManager();

	/**
	 * 获取AppWidgetHost
	 * @return
	 */
	public AppWidgetHost getAppWidgetHost();

	/**
	 * 获取拖拽控制
	 * @return
	 */
	public DragController getDragController();
	
//	/**
//	 * 获取任务栏当前高度
//	 * @return
//	 */
//	public int getStatusBarHeight();
//	
//	/**
//	 * 屏幕预览缓存当前特效种类
//	 */
//	public void switchPreviewEffetor(int effectorKind);
	
	/**
	 * 获取2D兼容层
	 * @return
	 */
	public ViewGroup getOverlayedViewGroup();
	
	/**
	 * 显示全屏进度条
	 */
	public void showProgressBar(boolean show);
	
	/**
	 * 获取当前最上层而且可见的View
	 * @return
	 */
	public GLView getTopView();
	
	/**
	 * 判断对应viewId的View是否处于最上层
	 * @param viewId
	 * @return
	 */
	public boolean isTopView(int viewId);
	
	/**
	 * 文件夹打开后回调
	 */
	public void onFolderOpened();
	
	/**
	 * 文件夹关闭后回调
	 */
	public void onFolderClosed(boolean needReopen, BaseFolderIcon<?> folderIcon, int status);
	
	/**
	 * 获取当前舞台
	 * @return
	 */
	public int getCurrentStage();

	/**
	 * 获取Widget回调对象
	 * @return
	 */
	public WidgetCallback getWidgetCallback();
	
	/**
	 * 显示罩子层
	 * @param show
	 */
	public void showCoverFrame(boolean show);
	
	/**
	 * 获取PopupWindowControler
	 * @return
	 */
	public PopupWindowControler getPopupWindowControler();
	
	/**
	 * 
	 * @param viewId
	 * @param centerX
	 * @param centerY
	 * @param duration
	 * @param waveSize
	 * @param damping
	 * @param listener
	 */
	public void wave(int viewId, int centerX, int centerY, long duration,
			int waveSize, int waveDepth, int damping, EffectListener listener, long delay);
	
	/**
	 * 设置是否支持多点触控
	 * @param enable
	 */
	public void setMultiTouchable(boolean enable);
	
	/**
	 * 切换GLContentView为16位或32位颜色格式
	 * @param translucent
	 */
	public void changePixelFormat(boolean translucent);
	/**
	 * 监听文件夹被关闭后的回调接口
	 * @author yangguanxiang
	 *
	 */
	public static interface IFolderClosedCallback {
		public void callback();
	}
}

