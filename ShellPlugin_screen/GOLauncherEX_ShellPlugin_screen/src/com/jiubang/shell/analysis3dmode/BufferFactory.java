package com.jiubang.shell.analysis3dmode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Buffer工厂。 提供创建Buffer的方法。 注意OPhone中要传入gl*Pointer()函数的Buffer对象必须要为direct模式申请的，
 * 这样可以确保缓存对象放置在Native的堆中，以免受到Java端的垃圾回收机制的影响。
 * 对于FloatBuffer,ShortBuffer,IntBuffer等多字节的缓存对象，它们的字节顺序 必须设置为nativeOrder。
 * 
 * @author Yong
 * 
 */
public class BufferFactory {
	private static final int INT_SIZE = 4;
	/**
	 * 创建新的FloatBuffer对象
	 * 
	 * @param numElements
	 *            - float元素的个数
	 * @return
	 */
	public static FloatBuffer newFloatBuffer(int numElements) {
		ByteBuffer bb = ByteBuffer.allocateDirect(numElements * INT_SIZE);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.position(0);
		return fb;
	}

	public static ShortBuffer newShortBuffer(int numElements) {
		ByteBuffer bb = ByteBuffer.allocateDirect(numElements * 2);
		bb.order(ByteOrder.nativeOrder());
		ShortBuffer sb = bb.asShortBuffer();
		sb.position(0);
		return sb;
	}
}
