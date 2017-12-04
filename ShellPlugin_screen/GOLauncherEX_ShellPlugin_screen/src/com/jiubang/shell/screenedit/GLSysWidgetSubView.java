package com.jiubang.shell.screenedit;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import com.gau.golauncherex.plugin.shell.R;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLRelativeLayout;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLImageView;

/**
 * 
 * @author zouguiquan
 *
 */
public class GLSysWidgetSubView extends GLLinearLayout {

	private GLViewGroup mPreviewContainer;
	private GLImageView mWidgetPreview;
	private GLImageView mWidgetIcon;
	private int mColumn;
	private int mRow;
	private int mHorMargin;
	private int mVerMargin;
	private boolean mHasPreview;

	public GLSysWidgetSubView(Context context) {
		this(context, null);
	}

	public GLSysWidgetSubView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources resources = context.getResources();

		mHorMargin = (int) resources.getDimension(R.dimen.screen_edit_syswidget_subview_hor_margin);
		mVerMargin = (int) resources.getDimension(R.dimen.screen_edit_syswidget_subview_ver_margin);
	}

	public void setColumnAndRow(int column, int row) {
		mColumn = column;
		mRow = row;
	}

	public void setHasPreview(boolean preview) {
		mHasPreview = preview;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mPreviewContainer = (GLViewGroup) findViewById(R.id.previewContainer);
		mWidgetPreview = (GLImageView) findViewById(R.id.widgetPreview);
		mWidgetIcon = (GLImageView) findViewById(R.id.widgetIcon);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (!mHasPreview) {
			int width = mPreviewContainer.getMeasuredWidth() - mHorMargin * 2;
			int height = mPreviewContainer.getMeasuredHeight() - mVerMargin * 2;

			int rect[] = calculateSize(width, height, mColumn, mRow);
			
			GLRelativeLayout.LayoutParams params = (GLRelativeLayout.LayoutParams) mWidgetPreview
					.getLayoutParams();
			params.width = rect[0];
			params.height = rect[1];
			mWidgetPreview.setLayoutParams(params);

//			Log.d("zgq", "onMeasure width= " + width + " height= " + height);
//			Log.d("zgq", "onMeasure params.width= " + params.width + " params.height= " + params.height);
		}
	}

	private int[] calculateSize(int width, int height, int column, int row) {
		int[] result = new int[2];
		
		if (column == 1 && row == 1) {
			result[1] = (int) (height * 0.5f);
			result[0] = result[1];
			return result;
		}
		
		int cellWidth = (int) (width / 4.0);
		int cellHeight = (int) (height / 4.0);
		if (column >= 5) {
			result[0] = width;
		} else {
			result[0] = cellWidth * column;
		}
		
		if (row >= 5) {
			result[1] = height;
		} else {
			result[1] = cellHeight * row;
		}

		return result;
	}
}
