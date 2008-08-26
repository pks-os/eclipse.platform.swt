package org.eclipse.swt.tools.internal;

public class ReflectParameter extends ReflectItem implements JNIParameter {
	ReflectMethod method;
	int parameter;

public ReflectParameter(ReflectMethod method, int parameter) {
	this.method = method;
	this.parameter = parameter;
}

public String getMetaData() {
	String className = method.getDeclaringClass().getSimpleName();
	String key = className + "_" + JNIGenerator.getFunctionName(method) + "_" + parameter;
	MetaData metaData = method.declaringClass.metaData;
	String value = metaData.getMetaData(key, null);
	if (value == null) {
		key = className + "_" + method.getName() + "_" + parameter;
		value = metaData.getMetaData(key, null);
	}
	/*
	* Support for 64 bit port.
	*/
	if (value == null) {
		JNIClass[] paramTypes = method.getParameterTypes();
		if (ReflectItem.convertTo32Bit(paramTypes, true)) {
			key = className + "_" + JNIGenerator.getFunctionName(method, paramTypes) + "_" + parameter;
			value = metaData.getMetaData(key, null);
		}
		if (value == null) {
			paramTypes = method.getParameterTypes();
			if (ReflectItem.convertTo32Bit(paramTypes, false)) {
				key = className + "_" + JNIGenerator.getFunctionName(method, paramTypes) + "_" + parameter;
				value = metaData.getMetaData(key, null);
			}
		}
	}
	/*
	* Support for lock.
	*/
	if (value == null && method.getName().startsWith("_")) {
		key = className + "_" + JNIGenerator.getFunctionName(method).substring(2) + "_" + parameter;
		value = metaData.getMetaData(key, null);
		if (value == null) {
			key = className + "_" + method.getName().substring(1) + "_" + parameter;
			value = metaData.getMetaData(key, null);
		}
	}
	if (value == null) value = "";	
	return value;
}

public String getCast() {
	String cast = ((String)getParam("cast")).trim();
	if (cast.length() > 0) {
		if (!cast.startsWith("(")) cast = "(" + cast;
		if (!cast.endsWith(")")) cast = cast + ")";
	}
	return cast;
}

public JNIMethod getMethod() {
	return method;
}

public JNIClass getType() {
	return method.getParameterTypes()[parameter];
}

public int getParameter() {
	return parameter;
}

public void setCast(String str) {
	setParam("cast", str);
}

public void setMetaData(String value) {
	String key;
	String className = method.getDeclaringClass().getSimpleName();
	if (JNIGenerator.isNativeUnique(method)) {
		key = className + "_" + method.getName () + "_" + parameter;
	} else {
		key = className + "_" + JNIGenerator.getFunctionName(method) + "_" + parameter;
	}
	method.declaringClass.metaData.setMetaData(key, value);
}
}
