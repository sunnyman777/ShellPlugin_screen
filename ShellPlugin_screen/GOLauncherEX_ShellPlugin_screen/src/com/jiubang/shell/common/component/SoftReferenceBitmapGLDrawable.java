package com.jiubang.shell.common.component;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.go.gl.ICleanup;
import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.TextureListener;
import com.go.gl.graphics.TextureManager;
import com.go.gl.view.GLContentView;

/**
 * 软引用专用GLDrawable
 * @author yejijiong
 *
 */
public class SoftReferenceBitmapGLDrawable extends BitmapGLDrawable implements ICleanup {

	public SoftReferenceBitmapGLDrawable() {
		super();
	}

	public SoftReferenceBitmapGLDrawable(BitmapDrawable drawable) {
		super(drawable);
	}

	public SoftReferenceBitmapGLDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		GLContentView.requestCleanUp(this);
	}

	@Override
	public void register() {
		
	}

	@Override
	public void unregister() {
		
	}
	
	public void registerTextureListener(TextureListener listener) {
		TextureManager.getInstance().registerTextureListener(listener);
	}

	@Override
	public void cleanup() {
		clear();
	}
}
