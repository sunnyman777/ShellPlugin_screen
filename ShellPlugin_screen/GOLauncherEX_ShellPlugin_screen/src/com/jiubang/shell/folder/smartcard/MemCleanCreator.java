package com.jiubang.shell.folder.smartcard;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.gau.golauncherex.plugin.shell.R;
import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;
import com.jiubang.shell.folder.smartcard.data.CardMemManager;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author guoyiqing
 * 
 */
public class MemCleanCreator implements ICardViewCreator {

	private static final int DEFAULT_MEMPERCENT_LIMIT = 50;
	private CardMemManager mMemManager;
	private long mLastCreateTime;
	private int mMemPercentLimit = DEFAULT_MEMPERCENT_LIMIT;
	private static final int CREATE_INTERVAL = 1000 * 60 * 60;

	public MemCleanCreator(Context context) {
		mMemManager = new CardMemManager(context);
	}

	
	@Override
	public List<GLAbsCardView> creat(CardBuildInfo data) {
		List<GLAbsCardView> views = new ArrayList<GLAbsCardView>();
		long current = System.currentTimeMillis();
		if (current - mLastCreateTime > CREATE_INTERVAL) {
			int pecent = mMemManager.getCurrentUsedPercent();
			if (pecent >= mMemPercentLimit) {
				mLastCreateTime = current;
				GLMemCardView view = (GLMemCardView) ShellAdmin.sShellManager
						.getLayoutInflater().inflate(
								R.layout.gl_smartcard_mem_layout, null);
				view.setMemMamager(mMemManager);
				views.add(view);
			}
		}
		return views;
	}

}
