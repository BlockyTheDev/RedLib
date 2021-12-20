package redempt.redlib.config.instantiation;

import redempt.redlib.config.ConfigManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * An Instantiator which uses an empty constructor, then populates fields and invokes a post-init method
 * @author Redempt
 */
public class EmptyInstantiator implements Instantiator {
	
	/**
	 * Creates an instance of a class if it has a no-args constructor
	 * @param clazz The class to instantiate
	 * @param <T> The type of the class
	 * @return The instance
	 */
	public static <T> T instantiate(Class<T> clazz) {
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return (T) constructor.newInstance();
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Class must have a no-arg constructor", e);
		}
	}
	
	/**
	 * Instantiates an object, loads values into its fields, and calls the post-init method
	 * @param manager The ConfigManager handling the config data
	 * @param target The object to load to, or null if creating a new one
	 * @param clazz The class whose fields are being worked with
	 * @param fields The fields being worked with
	 * @param values The values for the fields
	 * @param path The path in config
	 * @param info Extra info used for post-instantiation steps
	 * @param <T> The type
	 * @return The instantiated object, or the input object with its fields modified
	 */
	@Override
	public <T> T instantiate(ConfigManager manager, Object target, Class<T> clazz, List<Field> fields, List<Object> values, String path, InstantiationInfo info) {
		try {
			T t = target == null ? instantiate(clazz) : (T) target;
			for (int i = 0; i < fields.size(); i++) {
				if (values.get(i) == null) {
					continue;
				}
				fields.get(i).set(t, values.get(i));
			}
			if (info.getConfigPath() != null) {
				Object pathValue = info.getConfigPathConverter().fromString(path);
				info.getConfigPath().set(t, pathValue);
			}
			if (info.getPostInit() != null) {
				info.getPostInit().invoke(t);
			}
			return t;
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
