package com.jiubang.shell.screen;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;

import android.os.Message;
import android.os.Process;

import com.go.proxy.GoLauncherActivityProxy;
import com.go.proxy.MsgMgrProxy;
import com.go.util.AsyncHandler;
import com.go.util.DeferredHandler;
import com.golauncher.message.IDiyFrameIds;
import com.golauncher.message.IFolderMsgId;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.shell.folder.GLAppFolder;
import com.jiubang.shell.folder.GLScreenFolderIcon;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author
 * @date [2012-10-18]
 */
class GLDesktopBinder {
	private BindAsyncHandler mAsyncHandler;
	private BindDeferredHandler mDeferredHandler;

	/** 15秒内没有收到异步请求，则退出异步线程 */
	private final static int QUIT_DELAY = 15000;

	private static final int MSG_QUIT_THREAD = -1;
	private static final int MSG_BIND_DESKTOP_ITEMS = 0x1;
	private static final int MSG_ASYNC_LOAD_SHORTCUT_INFO = 0x2;
	private static final int MSG_POST_LOAD_SHORTCUT_INFO = 0x3;

	private static final int MSG_ASYNC_UPDATE_FOLDER = 0x4;
	private static final int MSG_POST_UPDATE_FOLDER = 0x5;

	private static final int MSG_SYNCH_FOLDER_CONTENT = 0x6;
	private static final int MSG_POST_RELOAD_FOLDER_CONTENT = 0x7;

	private static final int MSG_REMOVE_FOLDER_CONTENT = 0x8;
	private static final int MSG_POST_DELETE_FOLDER = 0x9;

	private static final int MSG_ASYNC_LOAD_FINISH = 0x10;
	private static final int MSG_POST_LOAD_FINISH = 0x11;

	private static final int MSG_SET_COUNTER = 0x12;

	public static final int MSG_NO_CHECK = 0;
	public static final int MSG_CHECK_DELETE = 1;

	/**
	 * 
	 * <br>
	 * 类描述: 用于同步桌面文件夹 <br>
	 * 功能详细描述:
	 * 
	 * @author
	 * @date [2012-10-18]
	 */
	private static class SyncFolderInfo {
		boolean mReload = false;
		ArrayList<ItemInfo> mItems; // 新增的内容,如果为null的话,mItems里面的每个元素直接从mFolderInfo.getChildInfo(int)获取
		UserFolderInfo mFolderInfo; // 编辑的文件夹
	}

	// 每次加载图标个数
	static final int ITEMS_COUNT = 4;
	private final WeakReference<GLScreen> mScreen;
	private LinkedList<ItemInfo> mShortcuts = null;
	private boolean mTerminate = false;
	private Object mLock = new Object();

	GLDesktopBinder(GLScreen screen,
			LinkedList<ItemInfo> shortcuts) {
		// init handler
		mDeferredHandler = new BindDeferredHandler();
		mScreen = new WeakReference<GLScreen>(screen);
		mShortcuts = shortcuts;
	}

	public void cancel() {
		// Log.i("luoph", "cancel DesktopBinder");
		synchronized (mLock) {
			mDeferredHandler.cancel();
			if (mAsyncHandler != null) {
				mAsyncHandler.cancel();
			}
		}
	}

	private boolean scheduleNextLocked() {
		if (mTerminate) {
			return false;
		}

		if (mShortcuts != null && !mShortcuts.isEmpty()) {
			mDeferredHandler.sendEmptyMessage(MSG_BIND_DESKTOP_ITEMS);
			return true;
		} else {
			notifyLoadFinish();
			// final ScreenFrame screenFrame = mScreen.get();
			// if (screenFrame != null && screenFrame.isLoading())
			// {
			// screenFrame.setLoading(false);
			// }
		}
		return false;
	}

	public synchronized void startBinding() {
		if (mTerminate) {
			return;
		}

		scheduleNextLocked();

		// 设置系统home键可触发
		GoLauncherActivityProxy.setSystemHomeKeyAct(true);
	}

	public void loadShortcutAsync(ShortCutInfo itemInfo) {
		// Log.i("luoph", "loadShortcutAsync");
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
				mAsyncHandler.initHandler();
			}
			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			mAsyncHandler.obtainMessage(MSG_ASYNC_LOAD_SHORTCUT_INFO, itemInfo)
					.sendToTarget();
		}
	}

	public void notifyLoadFinish() {
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
				mAsyncHandler.initHandler();
			}
			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			mAsyncHandler.removeMessages(MSG_ASYNC_LOAD_FINISH);
			mAsyncHandler.obtainMessage(MSG_ASYNC_LOAD_FINISH).sendToTarget();

		}
	}

	// private FolderIcon mFolderIcon;
	/**
	 * 刷新文件夹图标
	 * 
	 * @param folderIcon
	 *            文件图标View
	 * @param reloadIfNeed
	 *            是否检查需要更新内容
	 */
	public void updateFolderIconAsync(GLScreenFolderIcon folderIcon, boolean checkDel) {
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
				mAsyncHandler.initHandler();
			}
			
			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			Message message = new Message();
			message.what = MSG_ASYNC_UPDATE_FOLDER;
			message.obj = folderIcon;
			message.arg1 = checkDel ? MSG_CHECK_DELETE : MSG_NO_CHECK;
			mAsyncHandler.sendMessage(message);
		}
	}
	 
	/**
	 * 与功能功表文件夹同步
	 */
	
	public void synchFolderFromDrawer(UserFolderInfo folderInfo, ArrayList<ItemInfo> items,
			boolean reloadContent) {
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
				mAsyncHandler.initHandler();
			}

			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			Message message = new Message();
			message.what = MSG_SYNCH_FOLDER_CONTENT;
			SyncFolderInfo syncFolderInfo = new SyncFolderInfo();
			syncFolderInfo.mReload = reloadContent;
			syncFolderInfo.mFolderInfo = folderInfo;
			syncFolderInfo.mItems = items;
			message.obj = syncFolderInfo;
			mAsyncHandler.sendMessage(message);
		}
	}
	 

	/**
	 * 删除文件夹内的items
	 * 
	 * @param folderInfo
	 * @param items
	 */
	public void removeFolderContent(UserFolderInfo folderInfo,
			ArrayList<ItemInfo> items, boolean checkDeleteFolder) {
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
				mAsyncHandler.initHandler();
			}

			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			Message message = new Message();
			message.what = MSG_REMOVE_FOLDER_CONTENT;
			SyncFolderInfo syncFolderInfo = new SyncFolderInfo();
			syncFolderInfo.mReload = true;
			syncFolderInfo.mFolderInfo = folderInfo;
			syncFolderInfo.mItems = items;
			message.obj = syncFolderInfo;
			message.arg1 = checkDeleteFolder ? 1 : 0;
			mAsyncHandler.sendMessage(message);
		}
	}

	private void requestQuit() {
		// 发出终止线程请求
		synchronized (mLock) {
			if (mAsyncHandler != null) {
				mAsyncHandler
						.sendEmptyMessageDelay(MSG_QUIT_THREAD, QUIT_DELAY);
			}
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author
	 * @date [2012-10-18]
	 */
	private class BindAsyncHandler extends AsyncHandler {
		public BindAsyncHandler() {
			super(ThreadName.SCREEN_DESKTOP_BINDER,
					Process.THREAD_PRIORITY_BACKGROUND);
		}

		@Override
		public void handleAsyncMessage(Message msg) {
			switch (msg.what) {
			case MSG_ASYNC_LOAD_SHORTCUT_INFO: {
				if (msg.obj != null && msg.obj instanceof ShortCutInfo) {
					final GLScreen screenFrame = mScreen.get();
					if (screenFrame != null) {
						final ShortCutInfo shortcutInfo = (ShortCutInfo) msg.obj;
						// Log.i("luoph", "+++ " +
						// AppUtils.getPackage(shortcutInfo.mIntent));
						screenFrame.loadCompleteInfo(shortcutInfo);
						// 同步到UI线程上
						Message message = new Message();
						message.obj = shortcutInfo;
						message.what = MSG_POST_LOAD_SHORTCUT_INFO;
						mDeferredHandler.sendMessage(message);
					}
				}

				// 发出终止线程请求
				requestQuit();
			}
				break;

			
			  case MSG_ASYNC_UPDATE_FOLDER :
				  if (msg.obj != null && msg.obj instanceof GLScreenFolderIcon) {
					final GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) msg.obj;
					final UserFolderInfo folderInfo = folderIcon.getInfo();
					final GLScreen screen = mScreen.get();
					if (msg.arg2 == 1) { //表示reload == true
						folderInfo.mContentsInit = false;
					}

					if (folderInfo != null && screen != null) {
						// 需要重新加载文件夹内容
							if (!folderInfo.mContentsInit) {
								final ArrayList<ItemInfo> contents = screen
										.getFolderContentFromDB(folderInfo);
								folderInfo.clear();
								if (contents != null) {
									folderInfo.addAll(contents);
								}
								folderInfo.mContentsInit = true;

								// 注册图标更新事件
								final int size = folderInfo.getChildCount();

								for (int i = 0; i < GLScreenFolderIcon.INNER_ICON_SIZE && i < size; i++) {
									final ShortCutInfo itemInfo = folderInfo.getChildInfo(i);
									if (itemInfo != null) {
										//3D应该用不到了
//										// 清除之前注册的观察者
//										itemInfo.clearAllObserver();
//										// 注册观望者
//										itemInfo.registerObserver(folderIcon);
									}
								}
							}

						// 更新到UI
						Message uiMessage = new Message();
						uiMessage.what = MSG_POST_UPDATE_FOLDER;
						uiMessage.obj = folderIcon;
						uiMessage.arg1 = msg.arg1;
						mDeferredHandler.sendMessage(uiMessage);
						// 这里加postInvalidate()因为在合并文件夹时，如果这里不刷新一次
						// mDeferredHandler发出去的消息不会立即执行，在滑屏后才执行，
						// 因为mDeferredHandler.queueIdle()不会立即被执行，原因不明
						folderIcon.postInvalidate();
					}

				// 发出终止线程请求
				requestQuit();
				break;
			}
			 

			case MSG_SYNCH_FOLDER_CONTENT: {
				final GLScreen screen = mScreen.get();
				if (screen != null && msg.obj != null && msg.obj instanceof SyncFolderInfo) {
					final SyncFolderInfo syncFolderInfo = (SyncFolderInfo) msg.obj;
					final UserFolderInfo folderInfo = syncFolderInfo.mFolderInfo;

					if (folderInfo != null) {
						screen.addUserFolderContent(folderInfo.mInScreenId, folderInfo,
								syncFolderInfo.mItems, true);

						// 在3D的逻辑，UI已经先更新，所以这里注掉UI的请求刷新
//						if (syncFolderInfo.mReload) {
//							Message uiMessage = new Message();
//							uiMessage.what = MSG_POST_RELOAD_FOLDER_CONTENT;
//							uiMessage.obj = folderInfo;
//							uiMessage.arg1 = 0;
//							mDeferredHandler.sendMessage(uiMessage);
//						}
					}
				}
				// 发出终止线程请求
				requestQuit();
				break;
			}

			/*
			 * case MSG_REMOVE_FOLDER_CONTENT : { final GLScreenframe
			 * screenFrame = mScreen.get(); if (screenFrame != null && msg.obj
			 * != null && msg.obj instanceof SyncFolderInfo) { final
			 * SyncFolderInfo syncFolderInfo = (SyncFolderInfo) msg.obj; final
			 * UserFolderInfo folderInfo = syncFolderInfo.mFolderInfo;
			 * 
			 * if (folderInfo != null) {
			 * screenFrame.removeUserFolderConent(folderInfo.mInScreenId,
			 * syncFolderInfo.mItems, true);
			 * 
			 * ArrayList<ItemInfo> items = screenFrame
			 * .getFolderContentFromDB(folderInfo); boolean checkDeleteFolder =
			 * msg.arg1 == 1;
			 * 
			 * if (checkDeleteFolder && (null != items && items.size() <= 0)) {
			 * // 更新到UI删除文件夹 Message uiMessage = new Message(); uiMessage.what =
			 * MSG_POST_DELETE_FOLDER; uiMessage.obj = folderInfo;
			 * mDeferredHandler.sendMessage(uiMessage); } else if
			 * (syncFolderInfo.mReload) { // 更新到UI刷新图标 Message uiMessage = new
			 * Message(); uiMessage.what = MSG_POST_RELOAD_FOLDER_CONTENT;
			 * uiMessage.obj = folderInfo; uiMessage.arg1 = 1;
			 * mDeferredHandler.sendMessage(uiMessage); }
			 * 
			 * } }
			 * 
			 * // 发出终止线程请求 requestQuit(); break; }
			 */

			case MSG_ASYNC_LOAD_FINISH: {
				mDeferredHandler.sendEmptyMessage(MSG_POST_LOAD_FINISH);

				/**
				 * load图标消息以这种方式添加到消息队列中MessageQueue.addIdleHandler(
				 * MessageQueue .IdleHandler), 优先级最低，要等其他所有线程都完成任务才会执行此消息
				 * 加一个postInvalidate()到UI消息队列尾，可以提高此时处理UI消息队列优先级
				 */
				GLScreen screenFrame = mScreen.get();
				if (null != screenFrame) {
					screenFrame.postInvalidate();
				}

				// 发出终止线程请求
				requestQuit();
				break;
			}

			case MSG_QUIT_THREAD: {
				synchronized (mLock) {
					if (mAsyncHandler != null) {
						mAsyncHandler.cancel();
						mAsyncHandler = null;
					}
				}
				break;
			}

			default:
				break;
			}
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author
	 * @date [2012-10-18]
	 */
	private class BindDeferredHandler extends DeferredHandler {
		@Override
		public void handleIdleMessage(Message msg) {
			GLScreen screenFrame = mScreen.get();
			if (screenFrame != null) {
				switch (msg.what) {
				case MSG_BIND_DESKTOP_ITEMS: {
					screenFrame.bindShortcut(mShortcuts);
					scheduleNextLocked();

					break;
				}

				case MSG_POST_LOAD_SHORTCUT_INFO: {
					if (msg.obj != null && msg.obj instanceof ShortCutInfo) {
						screenFrame.postLoadShortcut((ShortCutInfo) msg.obj);
					}
					break;
				}

				case MSG_POST_UPDATE_FOLDER:
						if (msg.obj != null && msg.obj instanceof GLScreenFolderIcon) {
							final GLScreenFolderIcon folderIcon = (GLScreenFolderIcon) msg.obj;
							if (null == folderIcon.getInfo()) {
								return;
							}
							if (folderIcon.getInfo().getChildCount() == 0
									&& GLWorkspace.sLayoutScale >= 1.0f) {
								msg.arg1 = MSG_CHECK_DELETE;
							} else {
								folderIcon.refreshIcon();
								if (GLAppFolder.getInstance().isFolderOpened(
										folderIcon.getFolderInfo().folderId)) {
									MsgMgrProxy.sendMessage(this, IDiyFrameIds.APP_FOLDER,
											IFolderMsgId.FOLDER_RELAYOUT, -1, folderIcon);
								}
							}
							if (msg.arg1 == MSG_CHECK_DELETE) {
								// 检查删除桌面的空文件夹，添加界面的操作会用到
								long folderid = folderIcon.getInfo().mInScreenId;
								// MsgMgrProxy.sendMessage(this, IDiyFrameIds.SCREEN,
								// IDiyMsgIds.CHECK_FOLDER_NEED_DELETE, -1, folderid, null);
							}
						}
					break;
				 

				/*
				 * case MSG_POST_RELOAD_FOLDER_CONTENT : { if (msg.obj != null
				 * && msg.obj instanceof UserFolderInfo) { boolean checkDel =
				 * (msg.arg1 == 1) ? true : false;
				 * screenFrame.reloadFolderContent((UserFolderInfo) msg.obj,
				 * checkDel); } break; }
				 */

				/*
				 * case MSG_POST_DELETE_FOLDER : { if (msg.obj != null &&
				 * msg.obj instanceof UserFolderInfo) { UserFolderInfo
				 * userFolderInfo = (UserFolderInfo) msg.obj;
				 * screenFrame.deleteItem(userFolderInfo, -1); } break; }
				 */

				case MSG_POST_LOAD_FINISH: {
					screenFrame.loadFinish();
				}
					break;

				/*
				 * case MSG_SET_COUNTER : if (msg.obj != null && msg.obj
				 * instanceof FolderIcon) { final FolderIcon folderIcon =
				 * (FolderIcon) msg.obj; folderIcon.setCounter(msg.arg1); }
				 * break;
				 */
				default:
					break;
				}
			}
		}
	}

}