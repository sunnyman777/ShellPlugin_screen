package com.jiubang.shell.folder.status;

import android.media.AudioManager;
import android.util.Log;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.shell.folder.GLAppFolderBaseGridView;
import com.jiubang.shell.system.setting.VolumeInfo;
/**
 * 
 * @author dingzijian
 *
 */
public class ScreenFolderSilentModeStatus extends ScreenFolderNormalStatus {
	private int mVolume = -1;
	public ScreenFolderSilentModeStatus(GLAppFolderBaseGridView gridView) {
		super(gridView);
	}
	
	@Override
	public boolean onClickUnderStatus(GLAdapterView<?> parent, GLView view, int position, long id) {
		mVolume = VolumeInfo.getCurrent(parent.getContext(), AudioManager.STREAM_MUSIC);
		boolean succeed = VolumeInfo.setCurrent(parent.getContext(), AudioManager.STREAM_MUSIC, 0,
				AudioManager.FLAG_SHOW_UI);
		Log.i("dzj", "Screen SilentMode-->" + succeed);
		return super.onClickUnderStatus(parent, view, position, id);
	}
	
	@Override
	public int getGridStatusID() {
		return FolderStatusManager.FOLDER_SILENT_MODE_STATUS;
	}
	
	@Override
	public void onResume() {
		if (mVolume != -1) {
			VolumeInfo.setCurrent(mFolderBaseGridView.getContext(), AudioManager.STREAM_MUSIC,
					mVolume, AudioManager.FLAG_SHOW_UI);
			mVolume = -1;
		}
		super.onResume();
	}
}
