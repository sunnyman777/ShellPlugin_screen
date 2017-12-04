package com.jiubang.shell.screen.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.GoLauncherLogicProxy;
import com.go.util.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.plugin.ISecurityPoxy;
import com.jiubang.shell.animation.ValueAnimation;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.GLModel3DView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.model.CommonImageManager;
import com.jiubang.shell.model.IModelItemType;
import com.jiubang.shell.model.IModelState;
import com.jiubang.shell.screen.GLCellLayout;
import com.jiubang.shell.utils.IconUtils;

/**
 * 
 * @author jiangchao
 * 
 */
public class GLScreenShortCutIcon extends IconView<ShortCutInfo> implements BroadCasterObserver {
	
	private final static long BACK_DURATION = 300;
	private final static int STATE_NORMAL = 0;
	private final static int STATE_BACK_ANIMATE = 1;
	private GLModel3DMultiView mMultiView;
	private GLModel3DView mItemView;
	private GLTextViewWrapper mTextView;
	private GLImageView mImageView;
	private ValueAnimation mValueAnimation;
	private Averages mAverages;
	private int mState = STATE_NORMAL;
	private TitleChangeListener mTitleChangeListener;
	
	public GLScreenShortCutIcon(Context context) {
		this(context, null);
	}

	public GLScreenShortCutIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
//		CommonImageManager.getInstance().registerObserver(this);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMultiView = (GLModel3DMultiView) findViewById(R.id.multmodel);

		mItemView = (GLModel3DView) mMultiView.findViewById(R.id.model);
		mImageView = (GLImageView) mMultiView.findViewById(R.id.imge);
		mTextView = (GLTextViewWrapper) findViewById(R.id.app_name);

		mItemView.setModelItem(IModelItemType.GENERAL_ICON);
		//		mTextView.setMaxLines(DEFAULT_TEXT_MAX_LINES);
		//		mTextView.setMinLines(DEFAULT_TEXT_MAX_LINES);
		mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
				CommonImageManager.RES_FOLDER_BG));
		initIconFromSetting(true);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
//		initIconFromSetting(true);
		setTextViewBg(GoLauncherLogicProxy.getIsShowAppTitleBg());
		if (getGLParent() instanceof GLCellLayout && mTextView != null && mTextView.isVisible()) {
			IconUtils.sScreenIconTextHeight = mTextView.getHeight();
		}
	}
	
	@Override
	public void setTitle(CharSequence text) {
		if (text != null) {
			mTextView.setText(text);
		}
	}

	@Override
	public void setIcon(BitmapDrawable icon) {
		if (icon != null) {
			mItemView.changeTexture(icon.getBitmap());
		}
		if (mIconRefreshObserver != null) {
			mIconRefreshObserver.onIconRefresh();
		}
	}

	@Override
	public void setInfo(ShortCutInfo info) {
		ShortCutInfo oldInfo = mInfo;
		if (oldInfo != null) {
			oldInfo.unRegisterObserver(this);
		}
		super.setInfo(info);
		if (mInfo != null) {
			mInfo.registerObserver(this);
		}
		if (mIconRefreshObserver != null) {
			mIconRefreshObserver.onIconRefresh();
		}
	}

	@Override
	public Object getTag() {
		if (getInfo() != null) {
			return getInfo();
		}
		return super.getTag();
	}

	@Override
	public void onBCChange(int msgId, int param, Object ...object) {
		switch (msgId) {
			case AppItemInfo.INCONCHANGE :
			case AppItemInfo.TITLECHANGE : {
				//				if (object != null && object instanceof Drawable) {
				//					final Object tag = getTag();
				//					final Drawable icon = (Drawable) object;
				//					post(new Runnable() {
				//						@Override
				//						public void run() {
				//							if (tag != null && tag instanceof ShortCutInfo) {
				//								final Drawable newIconDrawable = mInfo.mIcon;
				//								setIcon(newIconDrawable);
				//							} else {
				//								setIcon(icon);
				//							}
				//						}
				//					});
				//				}
				if (mInfo != null) {
					post(new Runnable() {

						@Override
						public void run() {
							refreshIcon();

						}
					});
					if (mTitleChangeListener != null && mInfo.mTitle != null) {
						mTitleChangeListener.onTitleChange(mInfo.mTitle.toString());
					}
				}
				break;
			}

			//			case AppItemInfo.TITLECHANGE : {
			//				final Object tag = getTag();
			//				if (tag != null && tag instanceof ShortCutInfo) {
			//					final ShortCutInfo info = (ShortCutInfo) tag;
			//					if (object != null && object instanceof String) {
			//						final CharSequence title = info.mTitle;
			//						final boolean showTitle = GoSettingControler.getInstance(ApplicationProxy.getContext())
			//								.getDesktopSettingInfo().isShowTitle();
			//
			//						post(new Runnable() {
			//							@Override
			//							public void run() {
			//								if (showTitle) {
			//									if (!info.mIsUserTitle) {
			//										mTextView.setText(title);
			//									}
			//								} else {
			//									mTextView.setText(null);
			//								}
			//							}
			//						});
			//					}
			//				}
			//				break;
			//			}
			case AppItemInfo.IS_NEW_APP_CHANGE : 			
			case AppItemInfo.UNREADCHANGE : {
				post(new Runnable() {
					
					@Override
					public void run() {
						checkSingleIconNormalStatus();
						invalidate();
					}
				});
				
				break;
			}
			/**
			 * 用于CN包的代码
			 */
			case ISecurityPoxy.DOCK_SECURITY_STATE_CHANGED :
				post(new Runnable() {

					@Override
					public void run() {
						checkSingleIconNormalStatus();
						invalidate();
					}
				});
				break;
//			case IScreenFrameMsgId.COMMON_IMAGE_CHANGED : {
//				mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
//						CommonImageManager.RES_FOLDER_BG));
//				break;
//			}
			default :
				break;
		}
	}

	@Override
	public void refreshIcon() {
		if (mInfo != null) {
			mItemView.setTexture((BitmapDrawable) mInfo.mIcon);
			mTextView.setText(mInfo.mTitle);
			mTextView.setTextSize(GoLauncherLogicProxy.getAppFontSize());
			checkSingleIconNormalStatus();
			post(new Runnable() {

				@Override
				public void run() {
					invalidate();
				}
			});
		}
		if (mIconRefreshObserver != null) {
			mIconRefreshObserver.onIconRefresh();
		}
	}
	 
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
//		if (mAutoFit) {
//			canvas.clipRect(0, 0, mWidth, mHeight);
//		}
		
		if (mState == STATE_NORMAL) {
			super.dispatchDraw(canvas);
		} else if (mState == STATE_BACK_ANIMATE) {
			if (mAverages != null && mValueAnimation != null && mTFInfo != null) {
				if (mValueAnimation.animate()) {
					//    				final int saveCount = canvas.save();
					final float value = mValueAnimation.getValue();
					final float scaleX = mAverages.mStartSx + mAverages.mAScaleX * value;
					final float scaleY = mAverages.mStartSy + mAverages.mAScaleY * value;
					final float translateX = mAverages.mStartTx + mAverages.mATransX * value;
					final float translateY = mAverages.mStartTy + mAverages.mATransY * value;
					setScaleXY(scaleX, scaleY);
					setTranslateXY(translateX, translateY);
					//    				canvas.scale(scaleX, scaleY, mTFInfo.mPivotX, mTFInfo.mPivotY);
					//    				canvas.translate(translateX, translateY);
					super.dispatchDraw(canvas);
					//    	    		canvas.restoreToCount(saveCount);
					invalidate();
				} else {
					mState = STATE_NORMAL;
					mAverages = null;
					mValueAnimation = null;
					mTFInfo = null;
					super.dispatchDraw(canvas);
				}
			}
		}
	}

	@Override
	public void animateToSolution() {
		if (mTFInfo != null) {
			if (mValueAnimation == null) {
				mValueAnimation = new ValueAnimation(0);
			}
			mAverages = new Averages();
			mAverages.mStartSx = mTFInfo.mScaleX;
			mAverages.mStartSy = mTFInfo.mScaleY;
			mAverages.mStartTx = mTFInfo.mTranslationX;
			mAverages.mStartTy = mTFInfo.mTranslationY;
			mAverages.mATransX = -mTFInfo.mTranslationX;
			mAverages.mATransY = -mTFInfo.mTranslationY;
			mAverages.mAScaleX = 1.0f - mTFInfo.mScaleX;
			mAverages.mAScaleY = 1.0f - mTFInfo.mScaleY;

			mValueAnimation.start(1.0f, BACK_DURATION);
			mValueAnimation.animate();
			mState = STATE_BACK_ANIMATE;
			invalidate();
		}
	} // end animateToSolution

	/**
	 * 返回当前icon的图标
	 * @return
	 */
	public Bitmap getIcon() {
		// TODO：返回适当的图片
		if (mItemView != null) {
			return mItemView.getTexture();
		}
		return null;
	}

	@Override
	public void cleanup() {
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
		super.cleanup();
//		CommonImageManager.getInstance().unRegisterObserver(this);
	}

	@Override
	public void onIconRemoved() {
//		CommonImageManager.getInstance().unRegisterObserver(this);
	}

	/**
	 * 平均变化值
	 * @author jiangxuwen
	 *
	 */
	class Averages {

		float mStartTx;
		float mStartTy;

		float mATransX;
		float mATransY;

		float mStartSx;
		float mStartSy;

		float mAScaleX;
		float mAScaleY;
	}
	/**
	 * 
	 * <br>类描述:app文字改变回调的接口
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2013-4-9]
	 */
	public interface TitleChangeListener {
		public void onTitleChange(String newTitle);
	}

	public void setTitleChangeListener(TitleChangeListener changeListener) {
		mTitleChangeListener = changeListener;
	}
	
	
	@Override
	public void reloadResource() {
		if (mMultiView != null) {
			mMultiView.setBgImageDrawable(CommonImageManager.getInstance().getDrawable(
					CommonImageManager.RES_FOLDER_BG));
		}
	}
	
	@Override
	public void checkSingleIconNormalStatus() {
		if (mInfo != null && mInfo.getRelativeItemInfo() != null) {
//			int dangerLevel = ISecurityPoxy.DANGER_LEVEL_NONE;
//			try {
//				if (mInfo.getRelativeItemInfo() != null) {
//					dangerLevel = SecurityPoxyFactory.getSecurityPoxy().getDangerLevel(
//							mInfo.getRelativeItemInfo());
//				}
//			} catch (UnsupportSecurityPoxyException e) {
//			}
//			if (dangerLevel > ISecurityPoxy.DANGER_LEVEL_UNKNOW) {
//				if (dangerLevel == ISecurityPoxy.DANGER_LEVEL_SAFE) { // 扫描结果安全
//					mMultiView.setCurrenState(IModelState.SAFE_STATE, null);
//				} else if (dangerLevel == ISecurityPoxy.DANGER_LEVEL_UNSAFE) { // 扫描结果危险
//					mMultiView.setCurrenState(IModelState.DANGER_STATE, null);
//				}
////				mMultiView.setOnSelectClickListener(null);
//			} else 
			if (mInfo.getRelativeItemInfo().isNew()) { // 显示New标识
				mMultiView.setCurrenState(IModelState.NEW_STATE);
//				mMultiView.setOnSelectClickListener(null);
			} else if (mInfo.getUnreadCount() > 0) { // 通讯统计的未读数字
				mMultiView.setCurrenState(IModelState.STATE_COUNT, mInfo.getUnreadCount());
//				mMultiView.setOnSelectClickListener(null);
			} else { // 没有任何状态
				mMultiView.setCurrenState(IModelState.NO_STATE);
//				mMultiView.setOnSelectClickListener(null);
			}
		}
	};
}
