package br.com.geraldao.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;

import br.com.geraldao.annotation.Ignore;
import br.com.geraldao.exception.ANIMALTypeException;

public class QueryResultReader<T> {

	private enum DataType {
		COMPLEX_OBJECT, WRAPPER_BY_NAME, WRAPPER_BY_POSITION
	}

	private DataType dataType;
	private Class<T> clazz;

	/**
	 * 
	 * @param <T>
	 * @param clazz
	 *            Complex object data type
	 */
	public QueryResultReader(Class<T> clazz) {
		if (!isComplexClass(clazz)) {
			throw new IllegalArgumentException("");
		}
		this.clazz = clazz;
		dataType = DataType.COMPLEX_OBJECT;
	}

	public QueryResultReader(Class<T> clazz, String parameterName) {
		if (isComplexClass(clazz)) {
			throw new IllegalArgumentException("");
		}
		this.clazz = clazz;
		dataType = DataType.WRAPPER_BY_NAME;
	}

	public QueryResultReader(Class<T> clazz, int position) {
		if (isComplexClass(clazz)) {
			throw new IllegalArgumentException("");
		}
		this.clazz = clazz;
		dataType = DataType.WRAPPER_BY_POSITION;
	}

	private boolean isComplexClass(Class<T> clazz) {
		return !(Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)
				|| String.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz));
	}

	public T get(ResultSet rs) throws SQLException {
		switch (dataType) {
		case COMPLEX_OBJECT:
			return readByReflection(clazz, rs);
		case WRAPPER_BY_NAME:
			break;
		case WRAPPER_BY_POSITION:
			break;
		}
		return null;
	}

	/**
	 * 
	 * @param clazz
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @author victor.bello e yuri.campolongo
	 */
	@SuppressWarnings("unchecked")
	private T readByReflection(Class<T> clazz, ResultSet rs) throws SQLException {
		try {
			T obj = (T) Class.forName(clazz.getName()).newInstance();
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				Annotation annotation = method.getAnnotation(Column.class);
				Annotation ignoreColumn = method.getAnnotation(Ignore.class);
				if (ignoreColumn == null && annotation != null) {
					Class<Column> type = (Class<Column>) annotation.annotationType();
					Method met = type.getDeclaredMethod("name");
					Object value = convertTypes(rs.getObject(met.invoke(annotation).toString()),
							method.getReturnType());
					if (value != null) {
						Method declaredMethod = obj.getClass().getMethod(method.getName().replace("get", "set"),
								value.getClass());
						declaredMethod.invoke(obj, value);
					}
				}
			}

			return obj;

		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException e) {
			throw new ANIMALTypeException("Method not found " + e.getMessage()
					+ " please check in your mapping procedure class if the parameter type is the same as the procedure returning type");
		} catch (IllegalArgumentException | SecurityException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw new ANIMALTypeException(
					"Please make sure all your methods in procedure mapping with @colum annotation have public access");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("ForName was not able to identify your class " + clazz.getName());
		}
	}

	/**
	 * Convert database types to java types
	 * 
	 * @param value
	 *            the value returned by database
	 * @param type
	 *            expected by method
	 * @return converted object
	 */
	protected Object convertTypes(Object value, Class<?> type) {
		if (value == null) {
			return null;
		}
		if (value.getClass().isAssignableFrom(type)) {
			return value;
		}
		if (Boolean.class.isAssignableFrom(type)) {
			if (value instanceof Number && ((Number) value).intValue() != 0) {
				return true;
			} else {
				return false;
			}
		}
		if (value instanceof Timestamp) {
			if (Timestamp.class.isAssignableFrom(type)) {
				return (Timestamp) value;
			}
			return new Date(((Timestamp) value).getTime());
		}
		if (value instanceof Number) {
			if (Long.class.isAssignableFrom(type)) {
				return ((Number) value).longValue();
			}
			if (Integer.class.isAssignableFrom(type)) {
				return ((Number) value).intValue();
			}
			if (Float.class.isAssignableFrom(type)) {
				return ((Number) value).floatValue();
			}
			if (Double.class.isAssignableFrom(type)) {
				return ((Number) value).doubleValue();
			}
			if (Byte.class.isAssignableFrom(type)) {
				return ((Number) value).byteValue();
			}
			if (Short.class.isAssignableFrom(type)) {
				return ((Number) value).shortValue();
			}
		}
		return value;
	}

}
