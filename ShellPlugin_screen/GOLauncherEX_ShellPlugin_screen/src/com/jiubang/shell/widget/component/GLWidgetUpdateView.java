package com.jiubang.shell.widget.component;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author dengdazhong
 *
 */
public class GLWidgetUpdateView extends GLLinearLayout implements GLView.OnClickListener {
	private String mPackageName; //gowidget的package
	private String mTitle;
	private OnLongClickListener mOnLongClickListener;
	public GLWidgetUpdateView(Context context) {
		super(context);
	}

	public GLWidgetUpdateView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setPackageName(String packageName) {
		mPackageName = packageName;
		PackageManager pm = ShellAdmin.sShellManager.getActivity().getPackageManager();
		try {
			mTitle = pm.getPackageInfo(mPackageName, 0).applicationInfo.loadLabel(pm).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mTitle = getContext().getResources().getString(R.string.widget_update_to_3d, mTitle);
	}
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		init();
	}

	public void init() {
		this.setOnClickListener(this);
		GLImageView view = (GLImageView) findViewById(R.id.gl_widget_update_button);
		if (view != null) {
			view.setOnClickListener(this);
		}
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mOnLongClickListener = l;
		GLImageView view = (GLImageView) findViewById(R.id.gl_widget_update_button);
		if (view != null) {
			view.setOnLongClickListener(mOnLongClickListener);
		}
		super.setOnLongClickListener(mOnLongClickListener);
	}
	
	@Override
	public void onClick(GLView v) {
		DialogConfirm dialog = new DialogConfirm(ShellAdmin.sShellManager.getActivity());
		dialog.show();
		Resources res = ShellAdmin.sShellManager.getContext().getResources();
		dialog.setTitle(res.getString(R.string.widget_update_tips));
		dialog.setMessage(mTitle);
		dialog.setNegativeButton(res.getString(R.string.tip_upgrade), new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckApplication.downloadAppFromMarketGostoreDetail(
						ShellAdmin.sShellManager.getActivity(), mPackageName, null);
			}

		});
		dialog.setPositiveButton(res.getString(R.string.tip_not_now), null);
		dialog.show();

	}

}
