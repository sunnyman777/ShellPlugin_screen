package com.jiubang.shell.folder.smartcard;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.util.graphics.DrawUtils;
import com.jiubang.shell.indicator.DesktopIndicator;

/**
 * 
 * @author guoyiqing
 * 
 */
public class GLSmartCardLayout extends GLLinearLayout implements
		OnCardClickListener {

	private DesktopIndicator mIndicator;
	private GLLinearLayout mIndicatorLayout;
	private GLScrollCardGroup mScrollCardGroup;

	public GLSmartCardLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLSmartCardLayout(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mIndicator = (DesktopIndicator) findViewById(R.id.smart_card_indicator);
		mIndicator.setCurrent(0);
		mIndicator.setDefaultDotsIndicatorImage(
				R.drawable.gl_smartcard_indicator_selected,
				R.drawable.gl_smartcard_indicator_unselected);
		mIndicatorLayout = (GLLinearLayout) findViewById(R.id.smart_card_indicator_layout);
		mScrollCardGroup = (GLScrollCardGroup) findViewById(R.id.smart_card_scroll_layout);
		mScrollCardGroup.setIndicator(mIndicator);
	}

	public void setCardViews(int typeId, int currentCard,
			List<GLAbsCardView> views) {
		if (views != null && !views.isEmpty()) {
			mScrollCardGroup.setContainerId(typeId);
			mScrollCardGroup.setCardsViews(views);
			currentCard %= views.size();
			mIndicator.setDotIndicatorItemWidth(DrawUtils.dip2px(12));
			mIndicator.setTotal(views.size());
			mIndicator.setCurrent(currentCard);
			mIndicatorLayout.getLayoutParams().width = DrawUtils.dip2px(12)
					* views.size();
			mScrollCardGroup.setCurrent(currentCard);
			for (GLAbsCardView glAbsCardView : views) {
				glAbsCardView.setCardClickListener(this);
			}
		}
	}

	@Override
	public void onDismissClick(GLAbsCardView view) {
		mScrollCardGroup.removeView(view);
		mIndicator.setTotal(mScrollCardGroup.getChildCount());
		if (mScrollCardGroup.getChildCount() == 0) {
			setVisibility(GONE);
		}
	}

	public void setCurrentCardTypeMap(SparseArray<Integer> map) {
		if (mScrollCardGroup != null) {
			mScrollCardGroup.setCurrentCardTypeMap(map);
		}
	}

}
