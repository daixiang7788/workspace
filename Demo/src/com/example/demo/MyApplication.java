package com.example.demo;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		String dexPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/patch_dex.jar");
		File file = new File(dexPath);
		if (file.exists()) {
			inject(dexPath);
		}
	}

	private void inject(String dexPath) {
		try {
			Class<?> cl = Class.forName("dalvik.system.BaseDexClassLoader");
			Object pathList = getField(cl, "pathList", getClassLoader());
			Object oldElements = getField(pathList.getClass(), "dexElements", pathList);
			
			String dexopt = this.getDir("dexopt", Context.MODE_PRIVATE).getAbsolutePath();
			DexClassLoader mDexClassLoader = new DexClassLoader(dexPath, dexopt, dexopt, this.getClassLoader());
			Object pastList2 = getField(cl, "pathList", mDexClassLoader);
			Object newElements = getField(pastList2.getClass(), "dexElements", pastList2);
			
			Object newArrays = combineArray(oldElements, newElements);
			setField(pathList.getClass(), "dexElements", pathList, newArrays);
			Object dexElements = getField(pathList.getClass(), "dexElements", pathList);
			Log.e("DX", "dexElements length is " + Array.getLength(dexElements));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Object getField(Class<?> cl, String fieldName, Object obj)throws Exception {
		Field field = cl.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(obj);
	}
	
	private void setField(Class<?> cl, String fieldName, Object obj, Object value)throws Exception{
		Field field = cl.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(obj,value);
	}
	
	private Object combineArray(Object oldElements, Object newElements) {
		int oldLength = Array.getLength(oldElements);
		int newLength = Array.getLength(newElements);
		int length = oldLength + newLength;
		
		Class<?> componentType = newElements.getClass().getComponentType();
		Object newArrays = Array.newInstance(componentType, length);
		for (int i = 0; i < length; i++) {
			if(i < newLength){
				Array.set(newArrays, i, Array.get(newElements, i));
			}else {
				Array.set(newArrays, i, Array.get(oldElements, i - newLength));
			}
		}
		return newArrays;
	}
}