/**
 * 
 */
package com.jiubang.shell.popupwindow.component.ggmenu;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.animation.Animation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLImageView.ScaleType;
import com.go.gl.widget.GLTextViewWrapper;
import com.go.proxy.ApplicationProxy;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageManager;
import com.jiubang.shell.common.component.GLModel3DMultiView;
import com.jiubang.shell.common.component.GLModel3DView;
import com.jiubang.shell.common.component.IconView;
import com.jiubang.shell.ggheart.plugin.ShellAdmin;
import com.jiubang.shell.model.IModelItemType;
import com.jiubang.shell.theme.GLAppDrawerThemeControler;

/**
 * @author liuxinyang
 *
 */
public class GLGGMenuIcon extends IconView<GLGGMenuItemInfo> {

	private GLModel3DMultiView mMultiView;
	private GLModel3DView mItemView;
	private GLTextViewWrapper mTextView;
	
	private GLImageView mImageView;
	
	public GLGGMenuIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public GLGGMenuIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mMultiView = (GLModel3DMultiView) findViewById(R.id.multmodel);
		mItemView = (GLModel3DView) mMultiView.findViewById(R.id.model);
		mImageView = (GLImageView) mMultiView.findViewById(R.id.imge);
		mTextView = (GLTextViewWrapper) findViewById(R.id.app_name);

		mItemView.setModelItem(IModelItemType.GENERAL_ICON);
		mImageView.setVisibility(View.GONE);
	}

	@Override
	public void setTitle(CharSequence title) {
		if (title != null) {
			mTextView.setText(title);
		}
	}

	@Override
	public void setIcon(BitmapDrawable drawable) {
		// TODO Auto-generated method stub
		if (drawable != null) {
			mItemView.changeTexture(drawable.getBitmap());
		}
	}

	@Override
	public void refreshIcon() {
		// TODO Auto-generated method stub
		if (mInfo != null) {
			mItemView.setTexture((BitmapDrawable) mInfo.getIcon());
			mTextView.setText(mInfo.getTitle());
			post(new Runnable() {

				@Override
				public void run() {
					invalidate();
				}
			});
		}
	}

	@Override
	public void onIconRemoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reloadResource() {
		// TODO Auto-generated method stub
	}

	@Override
	public void checkSingleIconNormalStatus() {
		// TODO Auto-generated method stub
		if (mInfo != null) {
			int type = GGMenuControler.getInstance().checkMenuItemStatus(
					mInfo.getId());
			switch (type) {
			case GGMenuControler.GGMENU_ITEM_STATUS_SHOW_COUNT:
				int cnt = MessageManager.getMessageManager(
						ApplicationProxy.getContext()).getUnreadedCnt();
				if (cnt > 0) {
					Drawable d = getCounterImage(cnt);
					mImageView.setVisible(true);
					mImageView.setImageDrawable(d);
					startRefreshStateAnimation();
				}
				break;
			case GGMenuControler.GGMENU_ITEM_STATUS_SHOW_NEW_LOGO: {
				Drawable d = getNewImage();
				mImageView.setVisible(true);
				mImageView.setImageDrawable(d);
				startRefreshStateAnimation();
			}
				break;
			case GGMenuControler.GGMENU_ITEM_STATUS_UNSHOW_NEW_LOGO:
				mImageView.setVisible(false);
				mImageView.setImageDrawable(null);
				break;
			default:
				break;
			}
		}
	}
	
	// 菜单项继承于桌面图标，因此会与桌面图标共用同一套数字标识 
	// 这里就把数字标识图的构造方法抽取出来，专门用于菜单图标
	private Drawable getCounterImage(int count) {
		Context context = ShellAdmin.sShellManager.getContext();
		int fontSize = context.getResources().getDimensionPixelSize(
				R.dimen.menu_notify_no_nine_font_size);
		String countString = String.valueOf(count);
		int drawableRes = R.drawable.gl_menu_stat_notify_no_nine;
		// 数字长，字体越小
		if (countString != null && countString.length() > 2) {
			fontSize = context.getResources().getDimensionPixelSize(
					R.dimen.menu_notify_font_size);
		}
		int padding = context.getResources().getDimensionPixelSize(
				R.dimen.gl_notify_padding);
		Drawable drawable = BitmapUtility.composeDrawableTextExpend(
				ShellAdmin.sShellManager.getActivity(), context.getResources()
						.getDrawable(drawableRes), countString, fontSize,
				padding);
		if (drawable != null) {
			drawable.setBounds(0, 0, mImageView.getWidth(),
					mImageView.getHeight());
			mImageView.setScaleType(ScaleType.FIT_XY);
		}
		return drawable;
	}
	
	private Drawable getNewImage() {
			GLAppDrawerThemeControler themeCtrl = GLAppDrawerThemeControler.getInstance(ShellAdmin.sShellManager.getActivity());
			Drawable d = themeCtrl.getDrawable(themeCtrl.getThemeBean().mAppIconBean.mNewApp, false, R.drawable.gl_menu_new);
			return d;
	}
	
	private void startRefreshStateAnimation() {
		ScaleAnimation inAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		inAnimation.setDuration(300);
		mImageView.startAnimation(inAnimation);
	}
}
