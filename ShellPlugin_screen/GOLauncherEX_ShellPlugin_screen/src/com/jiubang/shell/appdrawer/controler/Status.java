package com.jiubang.shell.appdrawer.controler;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseListMenu;
import com.jiubang.shell.IShell;
import com.jiubang.shell.appdrawer.IActionBar;
import com.jiubang.shell.appdrawer.component.GLExtrusionGridView;
import com.jiubang.shell.drag.DragController;

/**
 * 状态基类
 * @author wuziyi
 *
 */
public abstract class Status {
	protected AbsStatusManager mStatusManager;
	protected IShell mShell;
	protected BaseListMenu mPopupMenu;
	protected DragController mDragControler;
	protected Handler mHandler = new Handler(Looper.getMainLooper());
	
//	public Status() {
//		mStatusManager = StatusManager.getInstance();
//	}
	
	public void setShell(IShell shell) {
		mShell = shell;
	}
	
	public void setDragControler(DragController controler) {
		mDragControler = controler;
	}

	public abstract boolean onKeyDown(int keyCode, KeyEvent event);

	public abstract boolean onKeyUp(int keyCode, KeyEvent event);

	public abstract void popupMenu();

	public abstract void dismissMenu();
	
	public abstract ArrayList<IActionBar> getBottomBarViewGroup();
	
	public abstract ArrayList<IActionBar> getTopBarViewGroup();
	
	public abstract IActionBar getBottomBarViewByGridSatus();

	public abstract IActionBar getTopBarViewByGridSatus();

	public abstract IActionBar getBottomBarViewByOrder();

	public abstract IActionBar getTopBarViewByOrder();
	
	public abstract void setGridView(GLExtrusionGridView gridView);
	
	public abstract GLExtrusionGridView getGridView();

	public abstract int getTabStatusID();
	
	public abstract int getGridStatusID();
	
	/**
	 * 
	 * 当前grid状态下的操作事件处理
	 * @param parent 当前的gird对象
	 * @param view 被点击的view对象
	 * @param position 被点击所在的列表位置
	 * @param id 被点击的行数
	 */
	public abstract boolean onClickUnderStatus(GLAdapterView<?> parent, GLView view,
			int position, long id);
	
	/**
	 * 
	 * 当前grid状态下的操作事件处理
	 * @param parent 当前的gird对象
	 * @param view 被点击的view对象
	 * @param position 被点击所在的列表位置
	 * @param id 被点击的行数
	 */
	public abstract boolean onLongClickUnderStatus(GLAdapterView<?> parent, GLView view,
			int position, long id);
	
	public abstract boolean onTouchMoveUnderStatus();
	
	public abstract boolean onTouchUpUnderStatus();
	
	public abstract boolean onTouchDownUnderStatus();
	
}
