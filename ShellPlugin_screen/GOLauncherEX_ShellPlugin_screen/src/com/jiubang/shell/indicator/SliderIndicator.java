package com.jiubang.shell.indicator;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.widget.GLImageView;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.shell.utils.GLImageUtil;

/**
 * 可拖动的指示器
 *
 */
public class SliderIndicator extends Indicator {
	private GLDrawable mIndicator; // 指示器图片
	private GLDrawable mIndicatorBG; // 指示器背景

	private IndicatorBgView mBgImageView; // 指示器背景组件，为了事件传到这里才加控件

	/**
	 * 指示器背景View
	 *
	 */
	private class IndicatorBgView extends GLImageView {

		public IndicatorBgView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(GLCanvas canvas) {
			GLDrawable drawable = (GLDrawable) getDrawable();
			if (null != drawable) {
				drawable.draw(canvas);
			}
		}
	};

	public SliderIndicator(Context context) {
		super(context);

		mTotal = 1;
		mCurrent = 0;

		initBgImageView();
	}

	private void initBgImageView() {
		mBgImageView = new IndicatorBgView(getContext());
		//		mBgImageView.setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				// do nothing;
		//			}
		//		});
		addView(mBgImageView);
	}

	public SliderIndicator(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public SliderIndicator(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}

	public void setIndicator(GLDrawable indicator, GLDrawable indicatorBG) {
		if (mIndicator != null) {
			mIndicator.clear();
		}
		if (mIndicatorBG != null) {
			mIndicatorBG.clear();
		}
		mIndicator = indicator;
		mIndicatorBG = indicatorBG;
		mBgImageView.setImageDrawable(mIndicatorBG);
		requestLayout();
	}

	public void setIndicator(int indicator, int indicatorBG) {
		try {
			final Drawable drawableIndicator = getContext().getResources().getDrawable(indicator);
			final Drawable drawableIndicatorBG = getContext().getResources().getDrawable(
					indicatorBG);

			GLDrawable glDrawableIndicator = null;
			GLDrawable glDrawableIndicatorbg = null;

			//			if (drawableIndicator != null) {
			//				if (drawableIndicator instanceof BitmapDrawable) {
			//					glDrawableIndicator = new BitmapGLDrawable((BitmapDrawable) drawableIndicator);
			//				} else if (drawableIndicator instanceof NinePatchDrawable) {
			//					glDrawableIndicator = new NinePatchGLDrawable((NinePatchDrawable) drawableIndicator);
			//				}
			//			}
			glDrawableIndicator = GLImageUtil.getGLDrawable(drawableIndicator);

			//			if (drawableIndicatorBG != null) {
			//				if (drawableIndicatorBG instanceof BitmapDrawable) {
			//					glDrawableIndicatorbg = new BitmapGLDrawable((BitmapDrawable) drawableIndicatorBG);
			//				} else if (drawableIndicatorBG instanceof NinePatchDrawable) {
			//					glDrawableIndicatorbg = new NinePatchGLDrawable((NinePatchDrawable) drawableIndicatorBG);
			//				}
			//			}
			glDrawableIndicatorbg = GLImageUtil.getGLDrawable(drawableIndicatorBG);
			
			setIndicator(glDrawableIndicator, glDrawableIndicatorbg);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			setIndicator(null, null); // 设置为null,效果是不显示指示器
		}
	}

	@Override
	public void setTotal(int total) {
		if (mTotal != total) {
			mTotal = total;
			requestLayout();
		}
	}

	@Override
	public void setCurrent(int current) {
		if (current < 0) {
			return;
		}

		mCurrent = current;
		mOffset = getWidth() * mCurrent / mTotal;
		postInvalidate();
	}

	@Override
	public void setOffset(int offset) {
		if (mOffset != offset) {
			mOffset = offset;
			postInvalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (null != mIndicator) {
			mIndicator.setBounds(0, 0, getWidth() / mTotal, mIndicator.getIntrinsicHeight());

			if (null != mBgImageView) {
				Rect bounds = new Rect(0, 0, getWidth(), mIndicatorBG.getIntrinsicHeight());
				mBgImageView.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
				Drawable drawable = mBgImageView.getDrawable();
				if (null != drawable) {
					drawable.setBounds(bounds);
				}
			}

		}
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		super.onDraw(canvas);

		if (null != mIndicator) {
			canvas.translate(mOffset, 0);
			mIndicator.draw(canvas);
			canvas.translate(-mOffset, 0);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean ret = super.dispatchTouchEvent(ev);

		if (null != mListener) {
			int action = ev.getAction();

			switch (action) {
				case MotionEvent.ACTION_DOWN : {
					mMovePercent = 0.0f;

					break;
				}

				case MotionEvent.ACTION_MOVE : {
					if (mMoveDirection != Indicator.MOVE_DIRECTION_NONE) {
						float x = ev.getX();
						if (0 <= x && x <= getWidth()) {
							mMovePercent = (x * 100) / getWidth();
							mListener.sliding(mMovePercent);
						}
					}

					break;
				}

				case MotionEvent.ACTION_CANCEL :
				case MotionEvent.ACTION_UP : {
					int x = (int) ev.getX();
					if (0 <= x && x <= getWidth()) {
						int index = (int) (((float) x / (float) getWidth()) * mTotal);
						mListener.clickIndicatorItem(index);
					}

					break;
				}

				default :
					break;
			}
		}

		return ret;
	}
}
