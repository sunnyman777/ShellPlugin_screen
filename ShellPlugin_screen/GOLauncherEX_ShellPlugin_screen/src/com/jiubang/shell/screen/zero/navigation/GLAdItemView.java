package com.jiubang.shell.screen.zero.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.view.GLView.OnClickListener;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.zeroscreen.navigation.bean.ZeroScreenAdInfo;
import com.jiubang.ggheart.zeroscreen.navigation.data.ToolUtil;
import com.jiubang.shell.common.component.ShellTextViewWrapper;
import com.jiubang.shell.screen.zero.navigation.data.GLSuggestImageCache;

/**
 * 
 * @author zhujian
 * 
 */
public class GLAdItemView extends GLFrameLayout implements OnClickListener {

	private ZeroScreenAdInfo mAdBean;

	private GLImageView mLogoImage;

	private GLImageView mDelImage;

	private OnClickListener mOnClickListener;

	private Context mContext;

	private ShellTextViewWrapper mTextLogoView;

	public static final int LOAD_FRONT = 0;

	public GLAdItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public GLAdItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public GLAdItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		init();

	}

	private void init() {
		mLogoImage = (GLImageView) findViewById(R.id.logo);
		mDelImage = (GLImageView) findViewById(R.id.kill);
		mDelImage.setOnClickListener(this);
		mTextLogoView = (ShellTextViewWrapper) findViewById(R.id.text_logo);
	}

	public GLImageView getDelImg() {

		return mDelImage;
	}

	public void setPlusDrawable() {
		mTextLogoView.setText(null);
		mLogoImage.setTag(null);
		mTextLogoView.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.gl_zero_ad_add_click_selector));
		mLogoImage.setImageDrawable(getResources().getDrawable(
				R.drawable.gl_zero_screen_ad_add));
	}

	public void setContex(Context context) {
		mContext = context;
	}

	public void setAdBean(ZeroScreenAdInfo adBean) {
		mAdBean = adBean;
		if (!mAdBean.mIsPlus) {
			if (mAdBean.mIsRecommend) {
				if (mAdBean.mUrl
						.equals(GLNavigationView.ZERO_SCREEN_POSITION_AD_ONE_WEBURL)) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_aitaobao));
				} else if (mAdBean.mUrl
						.equals(GLNavigationView.ZERO_SCREEN_POSITION_AD_TWO_WEBURL)) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_yamxun));
				} else if (mAdBean.mUrl
						.equals(GLNavigationView.ZERO_SCREEN_POSITION_AD_SIX_WEBURL)) {
					mAdBean.mUrl = GLNavigationView.ZERO_SCREEN_POSITION_AD_SIX_WEBURL;
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_baidu));
				} else if (mAdBean.mUrl
						.equals(GLNavigationView.ZERO_SCREEN_POSITION_AD_THREE_WEBURL)) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_3g));
				} else if (mAdBean.mUrl
						.equals(GLNavigationView.ZERO_SCREEN_POSITION_AD_FIVE_WEBURL)) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_youyuan));
				} else if (mAdBean.mUrl
						.equals(GLNavigationView.ZERO_SCREEN_POSITION_AD_FOUR_WEBURL)) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_taobao));
				}
			} else {
				mTextLogoView.setText(mAdBean.mTitle);
				mLogoImage.setTag(adBean.mUrl);
				mLogoImage.setImageDrawable(null);
				GLSuggestImageCache.getInstance(mContext).asynView(mLogoImage,
						mTextLogoView, GLSuggestImageCache.TYPE_FRONT);
				// 默认数据
				if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[0]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_suggest_tengxun_big));
					mTextLogoView.setText(null);
				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[1]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_suggest_sina_big));
					mTextLogoView.setText(null);

				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[2]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_3g));
					mTextLogoView.setText(null);

				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[3]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_screen_ad_baidu));
					mTextLogoView.setText(null);

				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[4]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_suggest_hao123_big));
					mTextLogoView.setText(null);

				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[5]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_suggest_wangyi_big));
					mTextLogoView.setText(null);

				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[6]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_suggest_souhu_big));
					mTextLogoView.setText(null);

				} else if (mAdBean.mUrl.equals(ToolUtil.HTTPHEAD
						.concat(SuggestSiteDefaultData.WEB_CATEGORY_URL[7]))) {
					mLogoImage.setImageDrawable(getResources().getDrawable(
							R.drawable.gl_zero_suggest_fenghuang_big));
					mTextLogoView.setText(null);

				}
			}
			mTextLogoView.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.gl_zero_ad_click_selector));
		} else {
			mTextLogoView.setText(null);
			mLogoImage.setTag(null);
			mTextLogoView.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.gl_zero_ad_add_click_selector));
			mLogoImage.setImageDrawable(getResources().getDrawable(
					R.drawable.gl_zero_screen_ad_add));
		}
		isShowKillDrawable(mAdBean.mIsShowDel);
	}

	public ZeroScreenAdInfo getAdBean() {
		return mAdBean;
	}

	public void isShowKillDrawable(boolean show) {
		if (show) {
			if (!mAdBean.mIsPlus) {
				mDelImage.setVisibility(View.VISIBLE);
				mAdBean.mIsShowDel = true;
			} else {
				mDelImage.setVisibility(View.GONE);
				mAdBean.mIsShowDel = false;
			}
		} else {
			mDelImage.setVisibility(View.GONE);
			mAdBean.mIsShowDel = false;
		}
		invalidate();
	}

	public void setOnClick(OnClickListener listener) {
		mOnClickListener = listener;
	}

	@Override
	public void onClick(GLView v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.kill:
			if (mOnClickListener != null) {
				mOnClickListener.onClick(this);
			}
			break;
		default:
			break;
		}
	}

}
