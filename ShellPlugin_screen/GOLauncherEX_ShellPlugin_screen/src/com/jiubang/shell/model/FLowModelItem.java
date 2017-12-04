package com.jiubang.shell.model;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.go.gl.view.GLView;
import com.jiubang.shell.analysis3dmode.MS3DFlowModel;

/**
 * FLowModelItem
 */
public class FLowModelItem extends ModelItem {
	private MS3DFlowModel mFlowModel;
	
	public FLowModelItem(GLView view, String fileName, boolean recyleImgAfterLoaded) {
		super(view, fileName, recyleImgAfterLoaded);
		mFlowModel = (MS3DFlowModel) mModel;
	}

	public FLowModelItem(GLView view, String fileName, int[] resIds, boolean recyleImgAfterLoaded) {
		super(view, fileName, resIds, recyleImgAfterLoaded);
		mFlowModel = (MS3DFlowModel) mModel;
	}

	public void changeUV(float u, float v) {

		mFlowModel.changeUV(u, v);
	}

	protected MS3DFlowModel loadModle(Context context, final String fileName, int[] resIds, boolean recyleImgAfterLoaded) {
		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		MS3DFlowModel model = null;
		try {
			Bitmap[] imgs = new Bitmap[resIds.length];
			for (int i = 0; i < resIds.length; i++) {
				imgs[i] = BitmapFactory.decodeResource(context.getResources(), resIds[i]);
			}
			//Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
			model = new MS3DFlowModel(imgs, recyleImgAfterLoaded);
			model.loadModel(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return model;
	}

	protected MS3DFlowModel loadModle(Context context, String fileName, boolean recyleImgAfterLoaded) {

		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		MS3DFlowModel model = new MS3DFlowModel(recyleImgAfterLoaded);

		try {
			model.loadModel(is);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return model;
	}
}
