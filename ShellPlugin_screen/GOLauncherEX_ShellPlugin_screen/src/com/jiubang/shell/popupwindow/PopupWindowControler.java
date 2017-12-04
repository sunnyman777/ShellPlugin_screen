package com.jiubang.shell.popupwindow;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.folder.GLAppFolderGridVIewContainer;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.popupwindow.component.GLPopupWindowLayer;
import com.jiubang.shell.popupwindow.component.actionmenu.GLMenuTextView;
import com.jiubang.shell.popupwindow.component.actionmenu.GLQuickActionMenu;
import com.jiubang.shell.popupwindow.component.ggmenu.GLGGMenu;
import com.jiubang.shell.popupwindow.component.listmenu.appdrawer.GLAllAppMenu;
import com.jiubang.shell.popupwindow.component.listmenu.folder.GLAppFolderGameModeMenu;
import com.jiubang.shell.popupwindow.component.listmenu.folder.GLAppFolderMenu;

/**
 * 弹出菜单控制器
 * @author yangguanxiang
 *
 */
public class PopupWindowControler {

	/**
	 * 
	 * @author zouguiquan
	 *
	 */
	public interface ActionListener {
		void onActionClick(int action, Object target);
	}

	private GLLayoutInflater mGLInflater;
	private IShell mShell;
	private Context mContext;
	private GLPopupWindowLayer mMainLayer;
	private GLQuickActionMenu mQuickActionMenu;
	private GLGGMenu mGGMenu;
	private GLAllAppMenu mAllAppMenu;
	
	private ActionListener mListener;
	private Object mCallbackFlag;
	private int mScreenWidth;
	private int mScreenHeight;

	public PopupWindowControler(IShell shell, GLPopupWindowLayer layer) {
		mShell = shell;
		mContext = shell.getContext();
		mMainLayer = layer;
		mMainLayer.setPopupMenuController(this);
		mGLInflater = ShellAdmin.sShellManager.getLayoutInflater();

		mScreenWidth = StatusBarHandler.getDisplayWidth();
		mScreenHeight = StatusBarHandler.getDisplayHeight();
	}

	public void onConfigurationChanged(int orientation, Configuration newConfig) {
		mScreenWidth = StatusBarHandler.getDisplayWidth();
		mScreenHeight = StatusBarHandler.getDisplayHeight();
	}

	/**
	 * 弹出GGMenu
	 */
	public void showGGMenu(boolean animate) {
		if (isShowing()) {
			return;
		}
		if (mGGMenu == null) {
			mGGMenu = (GLGGMenu) mGLInflater.inflate(R.layout.gl_ggmenu, null);
		}
		mMainLayer.setPopupWindow(mGGMenu);
		mMainLayer.setVisible(true);
		mMainLayer.getMiddleView().setVisible(true);
		mGGMenu.setParentLayer(mMainLayer);
		mMainLayer.enter(animate);
	}

	private void addQuickAcitonMenu() {
		if (mMainLayer != null) {
			mQuickActionMenu = (GLQuickActionMenu) mGLInflater.inflate(R.layout.gl_quickactionmenu,
					null);
			mMainLayer.setPopupWindow(mQuickActionMenu);
			mQuickActionMenu.setParentLayer(mMainLayer);
		}
	}
	
	/**
	 * 弹出功能表所有程序菜单
	 * @param animate
	 */
	public void showAllAppMenu(boolean animate) {
		if (isShowing()) {
			return;
		}
		if (mAllAppMenu == null) {
			mAllAppMenu = new GLAllAppMenu();
		}
		mMainLayer.setPopupWindow(mAllAppMenu);
		mMainLayer.setVisible(true);
		mMainLayer.enter(animate);
	}
	
	/**
	 * 弹出文件夹菜单
	 * @param animate
	 */
	public void showAppFolderMenu(boolean animate, GLAppFolderGridVIewContainer container) {
		if (isShowing()) {
			return;
		}
//		if (mFolderMenu == null) {
			GLAppFolderMenu folderMenu = new GLAppFolderMenu(container);
//		}
		mMainLayer.setPopupWindow(folderMenu);
		mMainLayer.setVisibility(GLView.VISIBLE);
		mMainLayer.enter(animate);
	}
	
		/**
	 * 弹出功能表所有程序菜单
	 * @param animate
	 */
	public void showAppFolderGameModeMenu(boolean animate, GLAppFolderGridVIewContainer container) {
		if (isShowing()) {
			return;
		}
//		if (mGameModeMenu == null) {
			GLAppFolderGameModeMenu gameModeMenu = new GLAppFolderGameModeMenu(container);
//		}
		mMainLayer.setPopupWindow(gameModeMenu);
		mMainLayer.setVisible(true);
		mMainLayer.enter(animate);
	}
	
	/**
	 * 添加点击弹出菜单项
	 * 
	 * @param id 菜单项对应的Id
	 * @param iconRes 图标id
	 * @param txtRes 文本id
	 */
	public void addQuickActionMenuItem(int id, Drawable iconRes, String txtRes) {
		if (mQuickActionMenu == null) {
			addQuickAcitonMenu();
		}

		GLViewGroup itemGroup = mQuickActionMenu.getItemGroup();

		try {
			if (itemGroup.getChildCount() != 0) {
				// 分割线
				GLImageView line = new GLImageView(mContext);
				line.setImageDrawable(mContext.getResources().getDrawable(
						R.drawable.gl_quickaction_line));
				int height = mContext.getResources().getDimensionPixelSize(
						R.dimen.quick_action_menu_heigth);
				line.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, height));
				itemGroup.addView(line);
			}
			GLView itemView = (GLView) mGLInflater.inflate(R.layout.gl_quickactionitem, itemGroup,
					false);
			GLMenuTextView textView = (GLMenuTextView) itemView.findViewById(R.id.quickmenu_txt);
			itemView.setTag(new Integer(id));
			itemView.setFocusable(true);
			GLImageView imageView = (GLImageView) itemView.findViewById(R.id.quickmenu_pic);
			imageView.setImageDrawable(iconRes);

			textView.setText(txtRes);
			itemView.setOnClickListener(mQuickActionMenu);
			itemView.setBackgroundResource(R.drawable.gl_qa_background_change);
			itemGroup.addView(itemView);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}

	/**
	 * 弹出图标的操作菜单
	 * @param locateRect
	 * @param callbackFlag
	 * @param listener
	 * @param view
	 */
	public void showQuickActionMenu(Rect locateRect, GLView targetView, Object callbackFlag,
			ActionListener listener, Object view) {
		if (isShowing() || mQuickActionMenu == null) {
			return;
		}
		mMainLayer.setVisible(true);

		mCallbackFlag = callbackFlag;
		mListener = listener;
		mQuickActionMenu.setActionListener(mListener);
		mQuickActionMenu.setCallbackFlag(callbackFlag);
		mQuickActionMenu.computePostion(locateRect);
		mQuickActionMenu.setTargetView(targetView);
		mMainLayer.enter(true);
	}

	public void dismiss(boolean animate) {
		if (!isShowing()) {
			return;
		}

		mMainLayer.exit(animate);
	}

	public boolean isShowing() {
		if (mMainLayer != null && mMainLayer.isVisible()) {
			return true;
		}
		return false;
	}

	/**
	 * 取消显示，调用此方法会有一个回调，事件id为IQuickActionId.CANCEL 如果不需要回调可以直接调用dismiss接口
	 */
	public void cancel(boolean animate) {
		if (!isShowing()) {
			return;
		}
		if (mListener != null) {
			mListener.onActionClick(IQuickActionId.CANCEL, mCallbackFlag);
		}
		dismiss(animate);
	}

	public void releaseReference() {
		if (mListener != null) {
			mListener = null;
		}
		if (mQuickActionMenu != null) {
			mQuickActionMenu = null;
		}
		if (mGGMenu != null) {
			mGGMenu = null;
		}
		if (mAllAppMenu != null) {
			mAllAppMenu = null;
		}
	}

}
