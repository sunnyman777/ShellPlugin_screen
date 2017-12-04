package com.jiubang.shell.appdrawer.promanage.actionbar;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.view.GLLinearLayout;
import com.go.proxy.VersionControl;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.golauncher.utils.GoAppUtils;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.advert.AdvertDialogCenter;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.shell.appdrawer.component.GLMemoryCleanButton;
import com.jiubang.shell.appdrawer.component.GLCleanView.OnCleanButtonClickListener;
import com.jiubang.shell.appdrawer.component.GLMemoryCleanButton.OnRefreshAnimationListener;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;

/**
 * 
 * @author yangguanxiang
 *
 */
public class GLMemoryCleanButtonComponent extends GLLinearLayout
		implements
			OnCleanButtonClickListener,
			OnRefreshAnimationListener {

	private GLMemoryCleanButton mBtnClean;
	private ShellTextViewWrapper mTxtMemory;

	private AppDrawerControler mAppDrawerControler;
	private StringBuilder mInfoBuilder = new StringBuilder();
	private String mMemoryInfo;

	public GLMemoryCleanButtonComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mAppDrawerControler = AppDrawerControler
				.getInstance(ShellAdmin.sShellManager.getActivity());
		mMemoryInfo = mContext.getString(R.string.btns_memory);
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mBtnClean = (GLMemoryCleanButton) findViewById(R.id.button_clean);
		mBtnClean.setOnCleanButtonClickListener(this);
		mBtnClean.setOnRefreshAnimationListener(this);
		mTxtMemory = (ShellTextViewWrapper) findViewById(R.id.text_memory_info);
		mTxtMemory.showTextShadow();
		mBtnClean.refresh(false);
	}

	/**
	 * 更新内存文字
	 */
	private void updateMemoryProgressText(long usedMemory, long totalMemory) {
		mInfoBuilder.delete(0, mInfoBuilder.length());
		mInfoBuilder.append(mMemoryInfo);
		mInfoBuilder.append(usedMemory).append("M");
		mInfoBuilder.append("/");
		mInfoBuilder.append(totalMemory).append("M");
		mTxtMemory.setText(mInfoBuilder.toString());
	}

	@Override
	public void onClick() {
		GuiThemeStatistics.sideOpStaticData("-1", "rg_clear_all", 1, "-1");
		mAppDrawerControler.terminateAllProManageTask();
		checkShow360Recommend();
	}

	private void checkShow360Recommend() {

		//判断是否存在电子市场，如果存在电子市场就推。不存在就不推。
		if (!GoAppUtils.isMarketExist(mContext)) {
			return;
		}

		PreferencesManager ps = new PreferencesManager(mContext);
		int clickCount = ps.getInt(IPreferencesIds.PREFERENCES_TASK_KILL_NUM, 1);

		String curCountry = Machine.getCountry(mContext);
		boolean isNewUser = VersionControl.isNewUser();

		boolean isAllowCounry = isNewUser || (!isNewUser && Machine.isAllowCountry(curCountry));

		boolean isNeedRecommend = ps.getBoolean(
				IPreferencesIds.PREFERENCES_TASK_KILL_IS_NEED_DISPLAY_RECOMMENDED, true);

		if (isAllowCounry && DeskSettingUtils.isPrimeAd(mContext)
				&& !AppUtils.isAppExist(mContext, PackageName.SECURITY_GUARDS_PACKAGE)) {

			if (Statistics.is200ChannelUid(mContext) && clickCount == 3 && isNeedRecommend) {
				GuiThemeStatistics.guiStaticDataFor360Mathon(40, "5371909", "f000", 1, "-1", "-1",
						"-1", "-1", "570");
				AdvertDialogCenter.show360RecommendDialog(ShellAdmin.sShellManager.getActivity());
				ps.putBoolean(IPreferencesIds.PREFERENCES_TASK_KILL_IS_NEED_DISPLAY_RECOMMENDED,
						false);
				ps.commit();
			}

		} else {
			//剩余的老用户国家推荐安卓优化大师
			if (DeskSettingUtils.isPrimeAd(mContext) && Statistics.is200ChannelUid(mContext)
					&& clickCount == 3
					&& !AppUtils.isAppExist(mContext, PackageName.DU_SPEED_BOOSTER)
					&& isNeedRecommend) {
				// 如果已经安装了CM，则推荐下载安卓优化大师
				String mapId = "5377557";
				String aId = "618";
				GuiThemeStatistics.guiStaticData(40, mapId, "f000", 1, "-1", "-1", "-1", "-1");
				AdvertDialogCenter.showDuSpeedBoosterDialog(ShellAdmin.sShellManager.getActivity(),
						mapId, aId, LauncherEnv.Url.DU_SPEED_BOOSTER_URL);

				ps.putBoolean(IPreferencesIds.PREFERENCES_TASK_KILL_IS_NEED_DISPLAY_RECOMMENDED,
						false);
				ps.commit();

			}
		}

		ps.putInt(IPreferencesIds.PREFERENCES_TASK_KILL_NUM, ++clickCount);
		ps.commit();
	}

	public void handleStateChanged(boolean changeToEdit) {
		Animation btnAnim = null;
		if (changeToEdit) {
			btnAnim = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, mBtnClean.getWidth() / 2,
					mBtnClean.getHeight());
		} else {
			btnAnim = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, mBtnClean.getWidth() / 2,
					mBtnClean.getHeight());
		}
		btnAnim.setFillAfter(true);
		btnAnim.setDuration(GLProManageActionBar.ANIMATION_DURATION);
		mBtnClean.startAnimation(btnAnim);
	}

	@Override
	public void onRefreshStart(long usedMemory, long totalMemory, float memoryPercent) {
	}

	@Override
	public void onRefreshing(long usedMemory, long totalMemory, float memoryPercent) {
		updateMemoryProgressText(usedMemory, totalMemory);
	}

	@Override
	public void onRefreshEnd(long usedMemory, long totalMemory, float memoryPercent) {
		updateMemoryProgressText(usedMemory, totalMemory);
	}

	public void refresh(boolean animate) {
		mBtnClean.refresh(animate);
	}
}
