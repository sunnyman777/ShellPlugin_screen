package com.jiubang.shell.appdrawer.slidemenu;

import android.content.Context;
import android.util.AttributeSet;

import com.go.gl.view.GLView;
import com.go.gl.widget.GLAdapterView;
import com.go.gl.widget.GLAdapterView.OnItemClickListener;
import com.go.gl.widget.GLAdapterView.OnItemLongClickListener;
import com.jiubang.shell.appdrawer.slidemenu.slot.ISlideMenuViewSlot;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuSlotItemIcon;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuSlotItemIcon.IClickEffectListener;

/**
 * 功能表侧边栏功能模块grid
 * @author wuziyi
 *
 */
public class SlideMenuSlotGrid extends CannotScrollGrid implements OnItemClickListener, OnItemLongClickListener, IClickEffectListener {

	private boolean mIsDelayClick;

	public SlideMenuSlotGrid(Context context) {
		super(context);
		init();
	}
	
	public SlideMenuSlotGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public SlideMenuSlotGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
	}

	
	@Override
	public void onItemClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		if (view instanceof SlideMenuSlotItemIcon) {
			SlideMenuSlotItemIcon icon = (SlideMenuSlotItemIcon) view;
			if (icon.isDrawingAnimation()) {
				// 等于延时响应点击
				mIsDelayClick = true;
			} else {
				ISlideMenuViewSlot slot = (ISlideMenuViewSlot) view.getTag();
				slot.showExtendFunctionView(view, true);
			}
		}
	}

	@Override
	public void onClickEffectStart(SlideMenuSlotItemIcon icon) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickEffectEnd(SlideMenuSlotItemIcon icon) {
		if (mIsDelayClick) {
			ISlideMenuViewSlot slot = (ISlideMenuViewSlot) icon.getTag();
			slot.showExtendFunctionView(icon, true);
			mIsDelayClick = false;
		}
		
	}

	@Override
	public boolean onItemLongClick(GLAdapterView<?> parent, GLView view, int position, long id) {
		// TODO Auto-generated method stub
		return false;
	}

}
