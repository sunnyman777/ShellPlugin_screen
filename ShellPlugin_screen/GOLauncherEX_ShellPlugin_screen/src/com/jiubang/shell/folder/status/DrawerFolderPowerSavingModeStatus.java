package com.jiubang.shell.folder.status;

import android.util.Log;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.system.setting.ScreenInfo;
/**
 * 
 * @author dingzijian
 *
 */
public class DrawerFolderPowerSavingModeStatus extends DrawerFolderSilentModeStatus {
	private int mBrightness = -1;
	private boolean mIsAutoBrightness;
	public DrawerFolderPowerSavingModeStatus(GLAppFolderBaseGridView gridView) {
		super(gridView);
	}
	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		mBrightness = ScreenInfo.getBrightness(parent.getContext());
		if (ScreenInfo.isAutoBrightness(parent.getContext())) {
			mIsAutoBrightness = true;
			ScreenInfo.setAutoBrightness(mFolderBaseGridView.getContext(), false);
		}
		ScreenInfo.setBrightness(parent.getContext(), ShellAdmin.sShellManager.getActivity()
				.getWindow(), 38);
		Log.i("dzj", "AppDrawer PowerSaving");
		return super.onClickUnderStatus(parent, view, position, id);
	}
	
	@Override
	public int getGridStatusID() {
		return FolderStatusManager.FOLDER_POWER_SAVING_MODE_STATUS;
	}
	
	@Override
	public void onResume() {
		switch (mBrightness) {
			case -1 :
				ScreenInfo.setBrightnessWithSystem(ShellAdmin.sShellManager.getActivity()
						.getWindow());
				break;
			default :
				ScreenInfo.setBrightness(mFolderBaseGridView.getContext(), ShellAdmin.sShellManager
						.getActivity().getWindow(), mBrightness);
				break;
		}
		if (mIsAutoBrightness) {
			ScreenInfo.setAutoBrightness(mFolderBaseGridView.getContext(), true);
			mIsAutoBrightness = false;
		}
		mBrightness = -1;
		super.onResume();
	}
}
