package com.jiubang.shell.common.listener;

import android.content.res.Resources;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.proxy.MsgMgrProxy;
import com.go.util.SingleThreadProxy;
import com.golauncher.message.IAppDrawerMsgId;
import com.golauncher.message.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.common.component.GLModel3DMultiView.OnSelectClickListener;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 功能表文件夹解除监听器
 * @author wuziyi
 *
 */
public class UnPakageFolderListener implements OnSelectClickListener {
	private FunFolderItemInfo mFolderInfo;
	GLAppFolderController mFolderControler;

	public UnPakageFolderListener(FunFolderItemInfo folderInfo) {
		super();
		mFolderInfo = folderInfo;
		mFolderControler = GLAppFolderController.getInstance();
	}
	
	@Override
	public void onClick(GLView v) {
		DialogConfirm dialogConfirm = new DialogConfirm(ShellAdmin.sShellManager.getActivity());
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		dialogConfirm.show();
		dialogConfirm.setTitle(res.getString(R.string.dlg_deleteFolder));
		dialogConfirm.setMessage(res.getString(R.string.dlg_deleteFolderContent));
		dialogConfirm.setPositiveButton(res.getString(R.string.ok),
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ShellAdmin.sShellManager.getShell().showProgressBar(true);
						SingleThreadProxy.postRunable(new Runnable() {
							
							@Override
							public void run() {
								MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_DRAWER_ALL_APP_GRID_VIEW,
										IAppDrawerMsgId.APPDRAWER_ALL_APP_REMOVE_ICON, -1, mFolderInfo);
								mFolderControler.removeAppDrawerFolder(mFolderInfo);
								ShellAdmin.sShellManager.getShell().showProgressBar(false);
							}
						});
						
					}

				});
		dialogConfirm.setNegativeButton(res.getString(R.string.cancel), null);
	}

}
