package com.jiubang.shell.appdrawer.slidemenu.slot;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.jiubang.shell.appdrawer.adapter.GLGridBaseAdapter;
import com.jiubang.shell.appdrawer.slidemenu.slot.SlideMenuSlotItemIcon.IClickEffectListener;

/**
 * 侧边栏内几个功能场景入口适配器
 * @author wuziyi
 *
 */
public class SlideMenuSlotAdapter extends GLGridBaseAdapter<ISlideMenuViewSlot> {

	public SlideMenuSlotAdapter(Context context,
			List<ISlideMenuViewSlot> infoList) {
		super(context, infoList);
	}

	@Override
	public GLView getView(int position, GLView convertView, GLViewGroup parent) {
		ISlideMenuViewSlot slot = mInfoList.get(position);
		convertView = getViewByItem(slot);
		if (convertView == null) {
			convertView = createView(slot);
			SlideMenuSlotItemIcon view = (SlideMenuSlotItemIcon) convertView;
			if (parent instanceof IClickEffectListener) {
				view.setClickEffectListener((IClickEffectListener) parent);
			}
			mViewHolder.put(String.valueOf(slot.getViewId()), convertView);
		}
		return convertView;
	}

	@Override
	public GLView getViewByItem(ISlideMenuViewSlot t) {
		// TODO Auto-generated method stub
		return mViewHolder.get(String.valueOf(t.getViewId()));
	}

	@Override
	public GLView removeViewByItem(ISlideMenuViewSlot t) {
		// TODO Auto-generated method stub
		return mViewHolder.remove(String.valueOf(t.getViewId()));
	}

	@Override
	protected GLView createView(ISlideMenuViewSlot slot) {
		int iconId = slot.getIconResId();
		Drawable drawable = mContext.getResources().getDrawable(iconId);
		GLView convertView = mInflater.inflate(R.layout.gl_appdrawer_slide_menu_slot_icon, null);
		SlideMenuSlotItemIcon view = (SlideMenuSlotItemIcon) convertView;
		view.setBackgroundResource(slot.getBackgroundResId());
		view.setIconDrawable(drawable);
		view.setText(mContext.getString(slot.getFuntionNameResId()));
		view.setTag(slot);
		return convertView;
	}

}
