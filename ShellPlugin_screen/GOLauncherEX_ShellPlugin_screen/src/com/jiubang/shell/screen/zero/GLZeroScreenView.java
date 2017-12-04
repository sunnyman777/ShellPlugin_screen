package com.jiubang.shell.screen.zero;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLLinearLayout;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.zeroscreen.StatisticsUtils;
import com.jiubang.ggheart.zeroscreen.navigation.bean.ZeroScreenAdInfo;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.utils.ToastUtils;

/**
 * 
 */
public class GLZeroScreenView extends GLLinearLayout {

	protected GLLayoutInflater mInflater = null;

	public GLZeroScreenView(Context context) {
		super(context);
		initView(context);
	}

	public GLZeroScreenView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public void initView(Context context) {
		mContext = context;
		mInflater = ShellAdmin.sShellManager.getLayoutInflater();
//		mInflater.inflate(R.layout.gl_zero_view, this);
		changeConfiguration(false);
	}
	
	public void changeConfiguration(boolean hideSoftInputWindow) {
	}
	
	/**
	 * <br>功能简述:点击桌面返回按钮
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onKeyBack() {
	};

	/**
	 * <br>功能简述:进入0屏
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void enterToZeroScreen() {
		GuiThemeStatistics.getInstance(getContext()).guiStaticData(57, "",
				StatisticsUtils.G001, 1, "0", "0", "", "");
		// 判断是否要弹toast提示
		int count = GoAppUtils.getShowZeroScreenTimes(ShellAdmin.sShellManager.getActivity());
		if (count < 3) {
			count++;
			GoAppUtils.setShowZeroScreenTimes(ShellAdmin.sShellManager.getActivity(), count);
			try {
				ToastUtils.showToast(R.string.zero_screen_setting_toast, Toast.LENGTH_LONG);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * <br>功能简述:离开0屏
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void leaveToZeroScreen() {
	};

	/**
	 * <br>功能简述:销毁view
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onDestory() {
	};

	public void destroyChildrenDrawingCache() {
	};

	public void buildChildrenDrawingCache() {
	};

	public void showOrHideTabLayout(boolean show, boolean where) {
	};

	public void cleanSearchResult() {
	};

	public boolean isShowOrHideNavigationView() {
		return false;
	};


	public boolean isShowOrHide() {
		return false;
	}

	public void addNewWeb(Object t) {
	};

	public ArrayList<ZeroScreenAdInfo> getZeroScreenAdInfos() {
		return null;
	}

	public int getClickPosition() {
		return -1;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int barHeight = 0;
		if (!StatusBarHandler.isHide()) {
			barHeight = StatusBarHandler.getStatusbarHeight();
			this.setPadding(0, barHeight, 0, 0);
		} else {
			this.setPadding(0, 0, 0, 0);
		}

		super.onLayout(changed, l, t, r, b);
	}
}
