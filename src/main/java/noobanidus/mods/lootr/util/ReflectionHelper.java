package noobanidus.mods.lootr.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// Stolen in its entirety from Up
public class ReflectionHelper {
  public static <T> Supplier<T> staticGetter(Class<?> ownerClass, String fieldName, Class<T> fieldType) {
    return staticGetter(ownerClass, fieldName, fieldType, MethodHandles.lookup());
  }

  public static <T> Supplier<T> staticGetter(Class<?> clazz, String fieldName, Class<T> fieldType, MethodHandles.Lookup callerContext) {
    try {
      MethodHandle getter = MethodHandles.privateLookupIn(clazz, callerContext)
          .findStaticGetter(clazz, fieldName, fieldType);
      var cachedErrorMessage = "Reflection error: Unable to get static field %s#%s".formatted(clazz.getName(), fieldName);
      return () -> {
        try {
          return fieldType.cast(getter.invoke());
        } catch (Throwable e) {
          throw new RuntimeException(cachedErrorMessage, e);
        }
      };
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Unable to look up static field %s for class %s".formatted(fieldName, clazz.getName()), e);
    }
  }

  public static <P, T> Function<P, T> getter(Class<P> ownerClass, String fieldName, Class<T> fieldType) {
    return getter(ownerClass, fieldName, fieldType, MethodHandles.lookup());
  }

  public static <P, T> Function<P, T> getter(Class<P> clazz, String fieldName, Class<T> fieldType, MethodHandles.Lookup callerContext) {
    try {
      MethodHandle getter = MethodHandles.privateLookupIn(clazz, callerContext).findGetter(clazz, fieldName, fieldType);
      var cachedErrorMessage = "Reflection error: Unable to get field %s#%s".formatted(clazz.getName(), fieldName);
      return parent -> {
        try {
          return fieldType.cast(getter.invoke(clazz.cast(parent)));
        } catch (Throwable e) {
          throw new RuntimeException(cachedErrorMessage, e);
        }
      };
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Unable to look up field %s for class %s".formatted(fieldName, clazz.getName()), e);
    }
  }

  public static <T> Consumer<T> staticSetter(Class<?> ownerClass, String fieldName, Class<T> fieldType) {
    return staticSetter(ownerClass, fieldName, fieldType, MethodHandles.lookup());
  }

  public static <T> Consumer<T> staticSetter(Class<?> clazz, String fieldName, Class<T> fieldType, MethodHandles.Lookup callerContext) {
    try {
      MethodHandle setter = MethodHandles.privateLookupIn(clazz, callerContext)
          .findStaticSetter(clazz, fieldName, fieldType);
      var cachedErrorMessage = "Reflection error: Unable to set static field %s#%s".formatted(clazz.getName(), fieldName);
      return value -> {
        try {
          setter.invoke(fieldType.cast(value));
        } catch (Throwable e) {
          throw new RuntimeException(cachedErrorMessage, e);
        }
      };
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Unable to look up static field %s for class %s".formatted(fieldName, clazz.getName()), e);
    }
  }

  public static <P, T> BiConsumer<P, T> setter(Class<P> ownerClass, String fieldName, Class<T> fieldType) {
    return setter(ownerClass, fieldName, fieldType, MethodHandles.lookup());
  }

  public static <P, T> BiConsumer<P, T> setter(Class<P> clazz, String fieldName, Class<T> fieldType, MethodHandles.Lookup callerContext) {
    try {
      MethodHandle setter = MethodHandles.privateLookupIn(clazz, callerContext).findSetter(clazz, fieldName, fieldType);
      var cachedErrorMessage = "Reflection error: Unable to set field %s#%s".formatted(clazz.getName(), fieldName);
      return (parent, value) -> {
        try {
          setter.invoke(clazz.cast(parent), fieldType.cast(value));
        } catch (Throwable e) {
          throw new RuntimeException(cachedErrorMessage, e);
        }
      };
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Unable to look up field %s for class %s".formatted(fieldName, clazz.getName()), e);
    }
  }
}