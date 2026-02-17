package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        if (instances.containsKey(interfaceClass)) {
            return instances.get(interfaceClass);
        }
        Class<?> implClass = findImplementation(interfaceClass);
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    + " is not annotated with @Component");
        }
        Object implInstance;
        try {
            implInstance = implClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
        instances.put(interfaceClass, implInstance);
        injectFields(implInstance, implClass);
        return implInstance;
    }

    private void injectFields(Object instance, Class<?> implClazz) {
        Field[] fields = implClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject dependency into field "
                            + field.getName() + " of " + implClazz.getName(), e);
                }
            }
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {

        if (interfaceClazz.equals(FileReaderService.class)) {
            return FileReaderServiceImpl.class;
        }
        if (interfaceClazz.equals(ProductParser.class)) {
            return ProductParserImpl.class;
        }
        if (interfaceClazz.equals(ProductService.class)) {
            return ProductServiceImpl.class;
        }
        throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
    }
}
