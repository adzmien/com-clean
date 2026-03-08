package com.clean.jpa.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for converting a filter POJO into a JPA {@link Specification}.
 *
 * <p>This class is stateless. It is a pure static utility.
 */
@Slf4j
public final class FilterSpecificationUtil {

    /** Cached property descriptors per filter class — avoids repeated introspection. */
    private static final ConcurrentHashMap<Class<?>, PropertyDescriptor[]> DESCRIPTOR_CACHE =
            new ConcurrentHashMap<>();

    private FilterSpecificationUtil() {
        // utility class — no instantiation
    }

    /**
     * Converts a filter POJO to a {@link Specification} where all field types use exact equality.
     *
     * @param filterDto any POJO whose non-null JavaBean properties drive the filter;
     *                  must not be {@code null}
     * @param <E>       the JPA entity type
     * @return a {@link Specification} that ANDs all non-null property predicates
     * @throws IllegalArgumentException if {@code filterDto} is {@code null}
     */
    public static <E> Specification<E> toSpecification(Object filterDto) {
        if (filterDto == null) {
            throw new IllegalArgumentException("filterDto must not be null");
        }
        return (root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(filterDto, root, cb, false);
            if (predicates.isEmpty()) {
                log.debug("FilterSpecificationUtil: no non-null properties on [{}]; returning conjunction",
                        filterDto.getClass().getSimpleName());
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Converts a filter POJO to a {@link Specification} where {@code String} fields use
     * case-insensitive LIKE ({@code %value%}) and all other types use exact equality.
     *
     * @param filterDto any POJO whose non-null JavaBean properties drive the filter;
     *                  must not be {@code null}
     * @param <E>       the JPA entity type
     * @return a {@link Specification} that ANDs all non-null property predicates
     * @throws IllegalArgumentException if {@code filterDto} is {@code null}
     */
    public static <E> Specification<E> toSearchSpecification(Object filterDto) {
        if (filterDto == null) {
            throw new IllegalArgumentException("filterDto must not be null");
        }
        return (root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(filterDto, root, cb, true);
            if (predicates.isEmpty()) {
                log.debug("FilterSpecificationUtil: no non-null properties on [{}]; returning conjunction",
                        filterDto.getClass().getSimpleName());
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * @param likeForStrings {@code true} → String fields use LIKE; {@code false} → exact equality for all types
     */
    private static <E> List<Predicate> buildPredicates(
            Object filterDto, Root<E> root, CriteriaBuilder cb, boolean likeForStrings) {

        List<Predicate> predicates = new ArrayList<>();
        PropertyDescriptor[] descriptors = DESCRIPTOR_CACHE.computeIfAbsent(
                filterDto.getClass(), FilterSpecificationUtil::introspect);
        if (descriptors.length == 0) {
            return predicates;
        }

        for (PropertyDescriptor pd : descriptors) {
            Method readMethod = pd.getReadMethod();
            if (readMethod == null) {
                log.trace("FilterSpecificationUtil: skipping write-only property [{}] on [{}]",
                        pd.getName(), filterDto.getClass().getSimpleName());
                continue;
            }

            Object value = invokeReadMethod(filterDto, readMethod, pd.getName());
            if (value == null) continue;

            try {
                Predicate predicate = buildPredicate(root, cb, pd.getName(), value, likeForStrings);
                if (predicate != null) predicates.add(predicate);
            } catch (IllegalArgumentException e) {
                log.warn("FilterSpecificationUtil: property [{}] from [{}] has no matching entity attribute on [{}]; skipping. Cause: {}",
                        pd.getName(), filterDto.getClass().getSimpleName(),
                        root.getJavaType().getSimpleName(), e.getMessage());
            }
        }
        return predicates;
    }

    private static <E> Predicate buildPredicate(
            Root<E> root, CriteriaBuilder cb, String propertyName, Object value, boolean likeForStrings) {

        if (likeForStrings && value instanceof String strValue) {
            if (strValue.isBlank()) return null; // blank treated as "not provided"
            return cb.like(cb.lower(root.get(propertyName)),
                    "%" + strValue.trim().toLowerCase() + "%");
        }
        return cb.equal(root.get(propertyName), value);
    }

    private static Object invokeReadMethod(Object filterDto, Method readMethod, String propertyName) {
        try {
            return readMethod.invoke(filterDto);
        } catch (InvocationTargetException e) {
            log.warn("FilterSpecificationUtil: getter for [{}] threw; skipping. Cause: {}",
                    propertyName, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return null;
        } catch (IllegalAccessException e) {
            log.warn("FilterSpecificationUtil: getter for [{}] not accessible; skipping. Cause: {}",
                    propertyName, e.getMessage());
            return null;
        }
    }

    private static PropertyDescriptor[] introspect(Class<?> clazz) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
            return beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            log.warn("FilterSpecificationUtil: could not introspect [{}]; returning empty descriptor array. Cause: {}",
                    clazz.getName(), e.getMessage());
            return new PropertyDescriptor[0];
        }
    }
}
