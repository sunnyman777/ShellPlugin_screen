package com.jiubang.shell.i18n;

import android.content.Context;

import com.jiubang.ggheart.components.DeskResources;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class ShellResourceConfiguration extends DeskResourcesConfiguration {
	private static ShellResourceConfiguration sInstance;

	public static ShellResourceConfiguration getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ShellResourceConfiguration(context);
		}
		return sInstance;
	}

	protected ShellResourceConfiguration(Context context) {
		super(context);
	}

	@Override
	protected DeskResources createDeskResources(boolean isInnerLanguage) {
		return new ShellDeskResources(ShellAdmin.sShellManager.getContext().getBaseContext()
				.getResources(), isInnerLanguage);
	}
}
