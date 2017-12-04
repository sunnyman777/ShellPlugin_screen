package com.jiubang.shell.common.component;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.ViewDebug;
/**
 * 关于GLView额外的图形图像相关信息
 * 根据需要来设置（可以不设置）
 * @author jiangxuwen
 *
 */
public class TransformationInfo {

	 /**
     * The transform matrix for the View. This transform is calculated internally
     * based on the rotation, scaleX, and scaleY properties. The identity matrix
     * is used by default. Do *not* use this variable directly; instead call
     * getMatrix(), which will automatically recalculate the matrix if necessary
     * to get the correct matrix based on the latest rotation and scale properties.
     */
    public final Matrix mMatrix = new Matrix();

    /**
     * The transform matrix for the View. This transform is calculated internally
     * based on the rotation, scaleX, and scaleY properties. The identity matrix
     * is used by default. Do *not* use this variable directly; instead call
     * getInverseMatrix(), which will automatically recalculate the matrix if necessary
     * to get the correct matrix based on the latest rotation and scale properties.
     */
    public Matrix mInverseMatrix;

    /**
     * An internal variable that tracks whether we need to recalculate the
     * transform matrix, based on whether the rotation or scaleX/Y properties
     * have changed since the matrix was last calculated.
     */
    public boolean mMatrixDirty = false;

    /**
     * An internal variable that tracks whether we need to recalculate the
     * transform matrix, based on whether the rotation or scaleX/Y properties
     * have changed since the matrix was last calculated.
     */
    public boolean mInverseMatrixDirty = true;

    /**
     * A variable that tracks whether we need to recalculate the
     * transform matrix, based on whether the rotation or scaleX/Y properties
     * have changed since the matrix was last calculated. This variable
     * is only valid after a call to updateMatrix() or to a function that
     * calls it such as getMatrix(), hasIdentityMatrix() and getInverseMatrix().
     */
    public boolean mMatrixIsIdentity = true;

    /**
     * The Camera object is used to compute a 3D matrix when rotationX or rotationY are set.
     */
    public Camera mCamera = null;

    /**
     * This matrix is used when computing the matrix for 3D rotations.
     */
    public Matrix matrix3D = null;

    /**
     * These prev values are used to recalculate a centered pivot point when necessary. The
     * pivot point is only used in matrix operations (when rotation, scale, or translation are
     * set), so thes values are only used then as well.
     */
    public int mPrevWidth = -1;
    public int mPrevHeight = -1;

    /**
     * The degrees rotation around the vertical axis through the pivot point.
     */
    @ViewDebug.ExportedProperty
    public float mRotationY = 0f;

    /**
     * The degrees rotation around the horizontal axis through the pivot point.
     */
    @ViewDebug.ExportedProperty
    public float mRotationX = 0f;

    /**
     * The degrees rotation around the pivot point.
     */
    @ViewDebug.ExportedProperty
    public float mRotation = 0f;

    /**
     * The amount of translation of the object away from its left property (post-layout).
     */
    @ViewDebug.ExportedProperty
    public float mTranslationX = 0f;

    /**
     * The amount of translation of the object away from its top property (post-layout).
     */
    @ViewDebug.ExportedProperty
    public float mTranslationY = 0f;

    /**
     * The amount of scale in the x direction around the pivot point. A
     * value of 1 means no scaling is applied.
     */
    @ViewDebug.ExportedProperty
    public float mScaleX = 1f;

    /**
     * The amount of scale in the y direction around the pivot point. A
     * value of 1 means no scaling is applied.
     */
    @ViewDebug.ExportedProperty
    public float mScaleY = 1f;

    /**
     * The x location of the point around which the view is rotated and scaled.
     */
    @ViewDebug.ExportedProperty
    public float mPivotX = 0f;

    /**
     * The y location of the point around which the view is rotated and scaled.
     */
    @ViewDebug.ExportedProperty
    public float mPivotY = 0f;

    /**
     * The opacity of the View. This is a value from 0 to 1, where 0 means
     * completely transparent and 1 means completely opaque.
     */
    @ViewDebug.ExportedProperty
    public float mAlpha = 1f;
}
