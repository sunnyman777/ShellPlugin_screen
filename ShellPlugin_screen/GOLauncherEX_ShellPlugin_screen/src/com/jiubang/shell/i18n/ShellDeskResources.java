package com.jiubang.shell.i18n;

import android.content.Context;
import android.content.res.Resources;

import com.jiubang.ggheart.components.DeskResources;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class ShellDeskResources extends DeskResources {

	public ShellDeskResources(Resources resources, boolean isInnerLanguage) {
		super(resources, isInnerLanguage);
	}

	@Override
	public CharSequence getText(int id) throws NotFoundException {
		if (null != mLanguageResources || mIsInnerLanguage) {
			String resName = getResourceEntryName(id);
			CharSequence ret = getLanguageText(resName);
			if (null != ret) {
				return ret;
			}
		}
		return super.getText(id);
	}

	@Override
	public CharSequence[] getTextArray(int id) throws NotFoundException {
		CharSequence[] textArray = null;
		if (mIsInnerLanguage) {
			String resName = getResourceEntryName(id);
			textArray = getLanguageTextArray(resName);
		} else {
			textArray = super.getTextArray(id);
		}
		return textArray;
	}

	@Override
	public String[] getStringArray(int id) throws NotFoundException {
		String[] stringArray = null;
		if (mIsInnerLanguage) {
			String resName = getResourceEntryName(id);
			stringArray = getLanguageStringArray(resName);
		} else {
			stringArray = super.getStringArray(id);
		}
		return stringArray;
	}

	@Override
	protected CharSequence getLanguageText(String resName) {
		CharSequence ret = null;
		if (mLanguageResources != null) {
			try {
				int remoteResId = mLanguageResources.getIdentifier(resName, "string",
						mLanguagePackage);
				ret = mLanguageResources.getText(remoteResId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (mIsInnerLanguage) {
			try {
				Context context = ShellAdmin.sShellManager.getActivity();
				Resources res = context.getResources();
				ret = res.getText(res.getIdentifier(resName, "string", context.getPackageName()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private CharSequence[] getLanguageTextArray(String resName) {
		CharSequence[] textArray = null;
		try {
			Context context = ShellAdmin.sShellManager.getActivity();
			Resources res = context.getResources();
			textArray = res.getTextArray(res.getIdentifier(resName, "array",
					context.getPackageName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textArray;
	}

	private String[] getLanguageStringArray(String resName) {
		String[] stringArray = null;
		try {
			Context context = ShellAdmin.sShellManager.getActivity();
			Resources res = context.getResources();
			stringArray = res.getStringArray(res.getIdentifier(resName, "array",
					context.getPackageName()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringArray;
	}
}
