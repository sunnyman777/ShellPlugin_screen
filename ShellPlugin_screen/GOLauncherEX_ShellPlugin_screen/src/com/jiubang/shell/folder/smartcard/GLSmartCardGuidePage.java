package com.jiubang.shell.folder.smartcard;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.widget.GLImageView;
import com.jiubang.shell.common.component.ShellTextViewWrapper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
/**
 * 
 * @author dingzijian
 *
 */
public class GLSmartCardGuidePage extends GLAbsCardView {
	private GLImageView mGuidePageImageView;
	private ShellTextViewWrapper mGuidePageTitle;
	private ShellTextViewWrapper mGuidePageSummary;
	private ShellTextViewWrapper mGuidePageOperationTips;

	public GLSmartCardGuidePage(Context context) {
		super(context);
	}
	public GLSmartCardGuidePage(Context context, AttributeSet attrs) {
		super(context, attrs);
		mOrderLevel = ICardConst.ORDER_LEVEL_GUIDE_PAGE;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mGuidePageImageView = (GLImageView) findViewById(R.id.gl_smart_card_guide_imageView);
		mGuidePageTitle = (ShellTextViewWrapper) findViewById(R.id.gl_smart_card_guide_title);
		mGuidePageSummary = (ShellTextViewWrapper) findViewById(R.id.gl_smart_card_guide_summary);
		mGuidePageOperationTips = (ShellTextViewWrapper) findViewById(R.id.gl_smart_card_guide_operation_tips);
		setTextStyle(mGuidePageOperationTips);
	}

	public void setGuidePage1() {
		mGuidePageImageView.setImageResource(R.drawable.gl_smart_card_guide_page1_icon);
		mGuidePageTitle.setText(R.string.smartcard_guide_page_1_title);
		mGuidePageSummary.setText(R.string.smartcard_guide_page_1_summary);
		mGuidePageOperationTips.setText(R.string.smartcard_guide_page_1_operation_tips);
	}
	
	public void setGuidePage2() {
		mGuidePageImageView.setImageResource(R.drawable.gl_smart_card_guide_page2_icon);
		mGuidePageTitle.setText(R.string.smartcard_guide_page_2_title);
		mGuidePageSummary.setText(R.string.smartcard_guide_page_2_summary);
		mGuidePageOperationTips.setText(R.string.smartcard_guide_page_2_operation_tips);
	}
	
	private void setTextStyle(ShellTextViewWrapper textViewWrapper) {
		textViewWrapper.getTextView().setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
	}
}
