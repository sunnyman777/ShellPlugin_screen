package com.jiubang.shell.widget.resize;

import android.graphics.Rect;

/**
 * 
 * @author dengdazhong
 *
 */
public interface IResizeListener {
	public void onSizeChanging(Rect rect);
	public void onReSizeCompleted(Rect rect);
}
