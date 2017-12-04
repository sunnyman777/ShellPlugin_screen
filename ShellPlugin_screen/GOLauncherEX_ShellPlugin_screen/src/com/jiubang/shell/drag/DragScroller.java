/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.shell.drag;

import android.graphics.Rect;

/**
 * Handles scrolling while dragging
 * 
 */
public interface DragScroller {
	
	public static final int SCROLL_TYPE_HORIZONTAL = 0;
	public static final int SCROLL_TYPE_VERTICAL = 1;
	public static final int SCROLL_DELAY_HORIZONTAL = 600;
	public static final int SCROLL_DELAY_VERTICAL = 10;
	public static final int NEXT_SCROLL_DELAY_HORIZONTAL = 1000;
	public static final int NEXT_SCROLL_DELAY_VERTICAL = 17;
	void onEnterLeftScrollZone();

	void onEnterRightScrollZone();

	void onEnterTopScrollZone();

	void onEnterBottomScrollZone();
	
	void onExitScrollZone();

	void onScrollLeft();

	void onScrollRight();

	void onScrollTop();

	void onScrollBottom();

	void onPostScrollRunnable(int direction);

	Rect getScrollLeftRect();
	
	Rect getScrollRightRect();
	
	Rect getScrollTopRect();
	
	Rect getScrollBottomRect();

	int getScrollType();
	
	int getScrollDelay();
	
	int getNextScrollDelay();
}
