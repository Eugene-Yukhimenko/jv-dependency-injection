package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClazz = findImplementation(interfaceClazz);

        if (!implClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Missing @Component annotation on class: "
                    + implClazz.getName());
        }

        Object instance = createInstance(implClazz);
        injectDependencies(instance, implClazz);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.equals(mate.academy.service.ProductParser.class)) {
            return mate.academy.service.impl.ProductParserImpl.class;
        } else if (interfaceClazz.equals(mate.academy.service.ProductService.class)) {
            return mate.academy.service.impl.ProductServiceImpl.class;
        } else if (interfaceClazz.equals(mate.academy.service.FileReaderService.class)) {
            return mate.academy.service.impl.FileReaderServiceImpl.class;
        } else {
            throw new RuntimeException("No implementation found for: " + interfaceClazz.getName());
        }
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of: " + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency: "
                            + fieldType.getName(), e);
                }
            }
        }
    }
}
