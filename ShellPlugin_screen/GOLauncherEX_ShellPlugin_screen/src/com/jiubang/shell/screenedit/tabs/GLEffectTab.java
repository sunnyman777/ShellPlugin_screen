package com.jiubang.shell.screenedit.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.graphics.effector.united.EffectorControler;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IScreenFrameMsgId;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingUtils;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogMultiChoice;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditController;
import com.jiubang.ggheart.apps.desks.purchase.FunctionPurchaseManager;
import com.jiubang.ggheart.apps.gowidget.ScreenEditItemInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.screenedit.GLEffectAdapter;

/**
 * 特效设置
 * @author zouguiquan
 *
 */
public class GLEffectTab extends GLGridTab {

	private String[] mItemsRes;
	private int[] mItemNumValue;
	private Drawable[] mItemDrawables;
	private EffectSettingInfo mEffectInfo;
	private GLEffectAdapter mAdapter;

	private BroadcastReceiver mRefreshReceiver;

	private boolean[] mDeskCustomRandomCheckStatus;
	private int mOldsetting = 0;
	private int mOldselect = 0; 

	private boolean mSuccessRandom = false;
	int mItemsCount; //总页数

	private EffectorControler mEffectorControler;
	private ScreenEditController mScreenEditControler;

	public GLEffectTab(Context context, int tabId, int level) {
		super(context, tabId, level);

		mNeedAsyncLoadImage = false;
		mLoadDataDelay = 200;

		mEffectorControler = EffectorControler.getInstance();
		mScreenEditControler = ScreenEditController.getInstance();
		registeRefreshReceiver(m2DContext);
	}

	@Override
	public void onResume() {
		// 获取当前选择特效
		mEffectInfo = SettingProxy.getEffectSettingInfo();
		mOldsetting = mEffectInfo.mEffectorType;
	}

	@Override
	public ArrayList<Object> requestData() {
		
		if (mDataList != null && mDataList.size() > 0) {
			return mDataList;
		}

		Object[] objs = mScreenEditControler.getAllEffectors(false);
		mItemsRes = (String[]) objs[0]; // 特效名
		mItemNumValue = (int[]) objs[1]; // 特效ID
		mItemDrawables = (Drawable[]) objs[2]; // 特效图片

		// 根据已选特效 判断第几个
		for (int i = 0; i < mItemNumValue.length; i++) {
			if (mItemNumValue[i] == mOldsetting) {
				mOldselect = i;
			}
		}

		ArrayList<Object> list = new ArrayList<Object>();
		for (int i = 0; i < mItemsRes.length; i++) {
			ScreenEditItemInfo info = new ScreenEditItemInfo();
			info.setTitle(mItemsRes[i]);
			info.setId(mItemNumValue[i]);
			info.setIcon(mItemDrawables[i]);
			list.add(info);
		}

		return list;
	}

	@Override
	public ShellBaseAdapter<Object> createAdapter(Context mContext, List<Object> infoList) {
		mAdapter = new GLEffectAdapter(mContext, infoList, mOldselect);
		return mAdapter;
	}

	@Override
	public void onItemClick(GLAdapterView<?> adapter, GLView view, int position, long id) {
		super.onItemClick(adapter, view, position, id);

		boolean isShowingEffect = MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
				IScreenFrameMsgId.SCREEN_SHOWING_AUTO_EFFECT, 0);

		if (isShowingEffect) {
			return;
		}
		
		ScreenEditItemInfo info = (ScreenEditItemInfo) view.getTag();
		if (info == null) {
			return;
		}
		
		int effectId = info.getId();
		//检查是否为收费特效
		if (FunctionPurchaseManager.getInstance(mContext.getApplicationContext())
				.getPayFunctionState(FunctionPurchaseManager.PURCHASE_ITEM_EFFECT) == FunctionPurchaseManager.STATE_VISABLE
				&& mEffectorControler.checkEffectorIsPrime(effectId)) {
			int entranceId = 0;
			// 先进行预览，再弹出购买的框
			if (effectId == IEffectorIds.EFFECTOR_TYPE_CROSSFADE) {
				entranceId = 416;
			} else if (effectId == IEffectorIds.EFFECTOR_TYPE_FLYIN) {
				entranceId = 414;
			} else if (effectId == IEffectorIds.EFFECTOR_TYPE_PAGETURN) {
				entranceId = 415;
			} else if (effectId == IEffectorIds.EFFECTOR_TYPE_CURVE) {
				entranceId = 413;
			} else if (effectId == IEffectorIds.EFFECTOR_TYPE_CRYSTAL) {
				entranceId = 421;
			} else if (effectId == IEffectorIds.EFFECTOR_TYPE_CLOTH) {
				entranceId = 425;
			} else if (effectId == IEffectorIds.EFFECTOR_TYPE_SNAKE) {
				entranceId = 428;
			}

			// 进行预览
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IScreenFrameMsgId.SCREENEDIT_SHOW_TAB_EFFECT_SETTING, effectId, entranceId,
					mEffectInfo.mEffectorType);
			return;
		}
		
		mOldselect = position;

		if (IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM == effectId) { // -2为自定义特效的ID
			alertChooseDialog();
			return;
		} else {
			mAdapter.setSelectedPos(mOldselect);
			mBaseGrid.requestLayout();
			applyEfftect(effectId);
		}
	}

	@Override
	public void clear() {
		super.clear();
		unRegisterRefreshReceiver(m2DContext);
	}

	/**
	 * 什么都没选就设置回当前特效
	 */
	private void applyEfftect(int type) {
		// EffectSettingInfo effectSettingInfo = null;
		if (null != mEffectInfo) {
			boolean bChanged = false;
			if (mEffectInfo.mEffectorType != type) {
				mEffectInfo.mEffectorType = type;
				bChanged = true;
			}
			if (bChanged) {
				SettingProxy.updateEffectSettingInfo(mEffectInfo);
			}
			// 进行预览
			//			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
			//					IDiyMsgIds.SCREENEDIT_SHOW_TAB_EFFECT_SETTING, mEffectInfo.mEffectorType, null,
			//					null);
			MsgMgrProxy
					.sendMessage(this, IDiyFrameIds.SCREEN,
							IScreenFrameMsgId.SCREENEDIT_SHOW_TAB_EFFECT_SETTING,
							mEffectInfo.mEffectorType);
		}
	}

	private void getDeskCustomRandomCheckStatus(int[] itemValue) {
		int[] checkedValue = mEffectInfo.mEffectCustomRandomEffects;
		mDeskCustomRandomCheckStatus = new boolean[itemValue.length];
		for (int i = 0; i < mDeskCustomRandomCheckStatus.length; i++) {
			mDeskCustomRandomCheckStatus[i] = false;
		}
		int i = 0;
		for (int j = 0; j < checkedValue.length; j++) {
			{
				i = 0;
				for (int c = 0; c < itemValue.length; c++) {
					if (itemValue[c] == checkedValue[j]) {
						mDeskCustomRandomCheckStatus[i] = true;
						break;
					}
					i++;
				}

			}
		}
	}

	public boolean alertChooseDialog() {
		// 获取用户自定义特效
		Object[] objs = mScreenEditControler.getAllEffectors(true);
		String[] itemsNames = (String[]) objs[0]; // 选择框中显示的特效名数组
		final int[] itemValues = (int[]) objs[1]; // 选择框中显示的特效ID数组
		getDeskCustomRandomCheckStatus(itemValues);
		DialogMultiChoice dialog = new DialogMultiChoice(m2DContext);
		dialog.show();
		dialog.setTitle(mContext.getResources().getString(R.string.dialog_title_custom_effect));
		dialog.setItemData(itemsNames, mDeskCustomRandomCheckStatus,
				new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						mDeskCustomRandomCheckStatus[which] = isChecked;
					}
				});
		dialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int[] items = itemValues;
				ArrayList<Integer> arrayItems = new ArrayList<Integer>();
				int j = 0;
				for (int i = 0; i < items.length; i++) {
					if (mDeskCustomRandomCheckStatus[j++]) {
						arrayItems.add(items[i]);
					}
				}
				if (arrayItems.isEmpty()) {

					//					Toast.makeText(mContext, R.string.toast_msg_noeffect_select, Toast.LENGTH_SHORT).show();

					mEffectInfo = SettingProxy.getEffectSettingInfo();
					mOldsetting = mEffectInfo.mEffectorType;
					for (int i = 0; i < mItemNumValue.length; i++) {
						if (mItemNumValue[i] == mOldsetting) {
							mOldselect = i;
						}
					}

					return;
				}
				int[] effects = new int[arrayItems.size()];
				for (int i = 0; i < effects.length; i++) {
					effects[i] = arrayItems.get(i);
				}

				mSuccessRandom = true;
				mEffectInfo.mEffectCustomRandomEffects = effects;
				mEffectInfo.mEffectorType = IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM;
				SettingProxy.updateEffectSettingInfo(mEffectInfo);

				arrayItems.clear();
				arrayItems = null;
				// 进行预览
				if (mSuccessRandom) {
					//					MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					//							IDiyMsgIds.SCREENEDIT_SHOW_TAB_EFFECT_SETTING,
					//							mEffectInfo.mEffectorType, null, null);
				}
				mAdapter.setSelectedPos(mOldselect);
				mBaseGrid.requestLayout();
			}
		});
		dialog.setNegativeButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mEffectInfo = SettingProxy.getEffectSettingInfo();
				mOldsetting = mEffectInfo.mEffectorType;
				for (int i = 0; i < mItemNumValue.length; i++) {
					if (mItemNumValue[i] == mOldsetting) {
						mOldselect = i;
					}
				}
			}
		});
		return mSuccessRandom;
	}

	private void registeRefreshReceiver(final Context context) {
		mRefreshReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context2, Intent intent) {
				if (intent != null) {
					if (FunctionPurchaseManager.getInstance(context2.getApplicationContext())
							.isItemCanUse(FunctionPurchaseManager.PURCHASE_ITEM_FULL)) {
						refreshData();
					}
				}

			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(DeskSettingUtils.ACTION_HAD_PAY_REFRESH);
		filter.setPriority(Integer.MAX_VALUE);
		context.registerReceiver(mRefreshReceiver, filter);
	}

	private void unRegisterRefreshReceiver(Context context) {
		try {
			context.unregisterReceiver(mRefreshReceiver);
		} catch (Exception e) {
		}

	}
}
