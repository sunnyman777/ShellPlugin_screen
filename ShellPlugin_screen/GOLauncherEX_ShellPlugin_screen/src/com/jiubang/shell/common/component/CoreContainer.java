package com.jiubang.shell.common.component;

import android.content.Context;
import android.widget.FrameLayout.LayoutParams;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.jiubang.ggheart.plugin.shell.IViewId;
import com.jiubang.shell.IShell;
import com.jiubang.shell.IView;
import com.jiubang.shell.appdrawer.GLAppDrawer;
import com.jiubang.shell.dock.GLDock;
import com.jiubang.shell.folder.GLAppFolderMainView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.screen.GLScreen;
import com.jiubang.shell.screen.back.GLBackWorkspace;

/**
 * 重要业务容器（方便全屏高斯模糊处理）
 * @author yangguanxiang
 *
 */
public class CoreContainer extends GLFrameLayout implements IView {

	public CoreContainer(Context context) {
		super(context);
		initViews(context);
	}
	
	private void initViews(Context context) {
		GLLayoutInflater inflater = ShellAdmin.sShellManager.getLayoutInflater();
		//初始化屏幕背景绘制层
		GLBackWorkspace backWorkspace = new GLBackWorkspace(context);
		backWorkspace.setId(IViewId.BACK_WORKSPACE);
		addView(backWorkspace, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		
		//初始化屏幕层
//		inflater.inflate(R.layout.gl_screen, this, true);
//		GLScreen screen = (GLScreen) findViewById(R.id.screenlayout);
		GLScreen screen = new GLScreen(context);
		screen.setId(R.id.screenlayout);
		addView(screen);
		
		//初始化Dock
		GLDock dock = new GLDock(context);
		dock.setId(IViewId.DOCK);
		dock.setVisibility(GLView.GONE);
		addView(dock, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		//初始化功能表
		GLAppDrawer appdrawer = new GLAppDrawer(context);
		appdrawer.setId(IViewId.APP_DRAWER);
		appdrawer.setVisibility(GLView.GONE);
		addView(appdrawer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		//初始化文件夹层
		inflater.inflate(R.layout.gl_folder_main_view_layout, this, true);
		GLAppFolderMainView folderMainView = (GLAppFolderMainView) findViewById(R.id.folder_main_view);
		folderMainView.setVisibility(GLView.GONE);
	}

	@Override
	public void setVisible(boolean visible, boolean animate, Object obj) {
		setVisible(visible);
	}

	@Override
	public void setShell(IShell shell) {

	}

	@Override
	public int getViewId() {
		return IViewId.CORE_CONTAINER;
	}

	@Override
	public void onAdd(GLViewGroup parent) {

	}

	@Override
	public void onRemove() {

	}

}
