package com.jiubang.shell.appdrawer.slidemenu;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.jiubang.ggheart.components.sidemenuadvert.SideAdvertControl;
import com.jiubang.ggheart.components.sidemenuadvert.tools.SideToolsDataInfo;
import com.jiubang.ggheart.components.sidemenuadvert.tools.SideToolsInfo;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuSmallToolAdapter;
import com.jiubang.shell.common.adapter.ShellBaseAdapter;
import com.jiubang.shell.common.component.GLScrollableBaseGrid;
import com.jiubang.shell.common.component.HorScrollableGridViewHandler;
import com.jiubang.shell.scroller.effector.CoupleScreenEffector;

/**
 * a grid view for extending from GLScrollableBaseGrid, which can slove a bug found in GLGridVIiew
 * @author hanson
 * 2014-04-24
 */
public class GLSlideMenuSmallToolGridView extends GLScrollableBaseGrid {
	
	private List<SideToolsInfo> mToolsSlots;
	
	public GLSlideMenuSmallToolGridView(Context context) {
		super(context);
		init();
	}
	public GLSlideMenuSmallToolGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLSlideMenuSmallToolGridView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		handleRowColumnSetting(false);
		handleScrollerSetting();
	}

	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view,
			int position, long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void callBackToChild(GLView view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshGridView() {
		mToolsSlots = SideAdvertControl.getAdvertControlInstance(mContext).getGoToolsInfo();
		
		//add the addmore button
		if (mToolsSlots != null) {
			SideToolsDataInfo addmore = new SideToolsDataInfo();
			addmore.setAddMore(true);
			addmore.setTitle(mContext.getString(R.string.tabs_smalltools_addmore));
			addmore.setIcon(mContext.getResources().getDrawable(R.drawable.gl_appdrawer_slide_menu_addmore));
			mToolsSlots.add(addmore);
		}
		setData(mToolsSlots);
	}

	@Override
	public ShellBaseAdapter createAdapter(Context context, List infoList) {
		return new SlideMenuSmallToolAdapter(context, infoList);
	}

	@Override
	protected void onScrollStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onScrollFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onScreenChange(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleScrollerSetting() {
		mScrollableHandler = new HorScrollableGridViewHandler(mContext, this,
				CoupleScreenEffector.PLACE_NONE, false, false) {

					@Override
					public void onEnterLeftScrollZone() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onEnterRightScrollZone() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onEnterTopScrollZone() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onEnterBottomScrollZone() {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onExitScrollZone() {
						// TODO Auto-generated method stub
						
					}
			
		};
		
	}

	@Override
	protected void handleRowColumnSetting(boolean updateDB) {
		mNumColumns = 4;
		mNumRows = 3;
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		handleScrollerSetting();
		super.onLayout(changed, left, top, right, bottom);
	}
}
