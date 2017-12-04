package com.jiubang.shell.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.gau.go.launcherex.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.proxy.ApplicationProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.proxy.SettingProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.go.util.SortUtils;
import com.go.util.graphics.effector.united.IEffectorIds;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.plugin.shell.folder.GLAppFolderController;
import com.jiubang.shell.animation.DropAnimation.DropAnimationInfo;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.drag.DragSource;
import com.jiubang.shell.drag.DragView;
import com.jiubang.shell.drag.DropTarget;
import com.jiubang.shell.folder.adapter.GLScreenFolderAdapter;
import com.jiubang.shell.folder.status.ScreenFolderNormalStatus;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-3-11]
 */
public class GLScreenFolderGridView extends GLAppFolderBaseGridView<GLScreenFolderIcon>
		implements
			BroadCasterObserver {
	protected DesktopSettingInfo mDeskTopSetting;
	protected ScreenSettingInfo mScreenSetting;
	private EffectSettingInfo mEffectSettingInfo;

	public GLScreenFolderGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLScreenFolderGridView(Context context) {
		super(context);
	}
	@Override
	protected void init() {
		super.init();
		mDeskTopSetting = SettingProxy.getDesktopSettingInfo();
		mScreenSetting = SettingProxy.getScreenSettingInfo();
		mEffectSettingInfo = SettingProxy.getEffectSettingInfo();
		SettingProxy.getInstance(mContext).registerObserver(this);
		handleRowColumnSetting(false);
		handleScrollerSetting();
	}
	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new GLScreenFolderAdapter(context, infoList);
	}

	@Override
	protected void handleScrollerSetting() {
		super.handleScrollerSetting();
		int effectType = mEffectSettingInfo.mEffectorType;
		if (effectType == IEffectorIds.EFFECTOR_TYPE_RANDOM_CUSTOM) {
			int[] effects = mEffectSettingInfo.mEffectCustomRandomEffects;
			((HorScrollableGridViewHandler) mScrollableHandler).setEffectType(effects);
		} else {
			if (effectType == IEffectorIds.EFFECTOR_TYPE_CLOTH) {
				effectType = IEffectorIds.EFFECTOR_TYPE_DEFAULT;
			}
			// 一般特效
			((HorScrollableGridViewHandler) mScrollableHandler).setEffectType(effectType);
		}
		mScrollableHandler.setCycleScreenMode(mScreenSetting.mScreenLooping);
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		setGridRowsAndColumns();
		super.handleRowColumnSetting(updateDB);
	}
//	@Override
//	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//		if (changed) {
//			handleRowColumnSetting(false);
//		}
//		super.onLayout(changed, left, top, right, bottom);
//	}
	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		mStatus.onClickUnderStatus(parent, view, position, id);
	}
	public void setGridRowsAndColumns() {
		mNumColumns = mDeskTopSetting.getColumns();
	}
	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		super.onItemLongClick(parent, view, position, id);
		mStatus.onLongClickUnderStatus(parent, view, position, id);
		return true;
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		return mStatus.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return mStatus.onKeyDown(keyCode, event);
	}

	@Override
	protected void onVisibilityChanged(GLView changedView, int visibility) {
		if (changedView instanceof GLAppFolderMainView) {
		super.onVisibilityChanged(changedView, visibility);
		UserFolderInfo info = mFolderIcon.getInfo();
		switch (visibility) {
			case GLView.INVISIBLE :
			case GLView.GONE :
				if (mPositionChange && info != null && info.getChildCount() > 0) {
					GLAppFolderController controller = GLAppFolderController.getInstance();
					controller.moveScreenFolderInnerItem(info);
					mPositionChange = false;
				}
				break;
			default :
				break;
		}
	}
	}
	
	//	
	@Override
	public void onDropCompleted(DropTarget target, Object dragInfo, boolean success,
			DropAnimationInfo resetInfo) {
		super.onDropCompleted(target, dragInfo, success, resetInfo);
		if (target != this) {
			MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
					IFolderMsgId.ON_FOLDER_DROP_COMPELETE, -1, target, dragInfo, success,
					mFolderIcon);
		}
	}
	@Override
	public void onDragMove(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo) {
		mStatus.onTouchMoveUnderStatus();
		super.onDragMove(source, x, y, xOffset, yOffset, dragView, dragInfo);
	}
	@Override
	public void onFolderNameChange(String name) {
		mFolderIcon.getInfo().mTitle = name;
		mFolderIcon.getInfo().setFeatureTitle(name);
		mFolderIcon.refreshIcon();
		mController.updateScreenFolderItem(mFolderIcon.getInfo());
	}
	@Override
	public void onGirdStatusChange(int gridStatusId) {

	}
	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		mStatus.onTouchDownUnderStatus();
		super.onDragStart(source, info, dragAction);
	}
	@Override
	public boolean onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
			DragView dragView, Object dragInfo, DropAnimationInfo resetInfo) {
		mStatus.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo, resetInfo);
		return super.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo, resetInfo);
	}
	@Override
	public void showSortDialog() {
		DialogSingleChoice sortDialog = createSortDialog();
		sortDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				mPositionChange = true;
				List<?> folderContent = mFolderIcon.getInfo().getContents();
				switch (item) {
					case SORTTYPE_LETTER :
						// 进行排序
//						Collections.sort(folderContent, new Comparator<Object>() {
//							@Override
//							public int compare(Object object1, Object object2) {
//								int result = 0;
//								CharSequence chars1 = ((ShortCutInfo) object1).mTitle;
//								CharSequence chars2 = ((ShortCutInfo) object2).mTitle;
//								if (chars1 == null || chars2 == null) {
//									return result;
//								}
//								// 按字符串类型比较
//								String str1 = chars1.toString();
//								String str2 = chars2.toString();
//								Collator collator = Collator.getInstance(Locale.ENGLISH);
//								if (collator == null) {
//									collator = Collator.getInstance(Locale.getDefault());
//								}
//								result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
//								return result;
//							}
//						});
						String sortMethod = "getTitle";
						try {
							SortUtils.sortFolderApps(ApplicationProxy.getContext(), folderContent, 
									sortMethod, null, null, "ASC", R.raw.unicode2pinyin);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case SORTTYPE_TIMENEAR :
						// 进行排序
						Collections.sort(folderContent, new Comparator<Object>() {
							@Override
							public int compare(Object object1, Object object2) {
								int result = 0;
								// 按int类型比较
								long value1 = ((ShortCutInfo) object1).mTimeInFolder;
								long value2 = ((ShortCutInfo) object2).mTimeInFolder;
								if (value1 == value2) {
									return result;
								}
								int temInt = value2 > value1 ? 1 : -1;
								result = temInt;
								return result;
							}
						});
						break;
					case SORTTYPE_TIMEREMOTE :
						// 进行排序
						Collections.sort(folderContent, new Comparator<Object>() {
							@Override
							public int compare(Object object1, Object object2) {
								int result = 0;
								// 按int类型比较
								long value1 = ((ShortCutInfo) object1).mTimeInFolder;
								long value2 = ((ShortCutInfo) object2).mTimeInFolder;
								if (value1 == value2) {
									return result;
								}
								int temInt = value1 > value2 ? 1 : -1;
								result = temInt;
								return result;
							}
						});
						break;
					default :
						break;
				}
				setData(folderContent);
				mFolderIcon.refreshIcon();
			}
		});

	}

	//	@Override
	//	public void refreshGridView() {
	//		setData(mFolderIcon.getInfo().getContents());
	//		mFolderIcon.refreshIcon();
	//	}

	//	@Override
	//	public int getContentSize() {
	//		return mFolderIcon.getInfo().getContents().size();
	//	}
	@Override
	public void onFolderContentIconChange(Bundle bundle) {
		ScreenFolderNormalStatus normalStatus = (ScreenFolderNormalStatus) mStatus;
		normalStatus.actionChangeIcon(bundle);
		super.onFolderContentIconChange(bundle);
	}
	@Override
	public void onFolderContentNameChange(String name, long itemId) {
		ScreenFolderNormalStatus normalStatus = (ScreenFolderNormalStatus) mStatus;
		normalStatus.actionChangeAppName(name, itemId);
		super.onFolderContentNameChange(name, itemId);
	}
	
	@Override
	public void onFolderContentUninstall(ArrayList<AppItemInfo> uninstallapps) {
		ScreenFolderNormalStatus normalStatus = (ScreenFolderNormalStatus) mStatus;
		normalStatus.actionUninstallApp(uninstallapps);
	}

	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (param) {
			case DataType.DATATYPE_SCREENSETTING :
			case DataType.DATATYPE_EFFECTSETTING :
				if (object[0] instanceof ScreenSettingInfo) {
					mScreenSetting = (ScreenSettingInfo) object[0];
				}
				if (object[0] instanceof EffectSettingInfo) {
					mEffectSettingInfo = (EffectSettingInfo) object[0];
				}
				handleScrollerSetting();
				break;
			case DataType.DATATYPE_DESKTOPSETING :
				if (object[0] instanceof DesktopSettingInfo) {
					mDeskTopSetting = (DesktopSettingInfo) object[0];
				}
				handleRowColumnSetting(false);
			default :
				break;
		}
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		
	}
}
