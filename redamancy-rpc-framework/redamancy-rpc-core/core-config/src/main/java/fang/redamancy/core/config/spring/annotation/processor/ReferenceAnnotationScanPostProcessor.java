package fang.redamancy.core.config.spring.annotation.processor;

import fang.redamancy.core.common.annotation.FangReference;
import fang.redamancy.core.config.support.reference.ReferenceFieldBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * @Author redamancy
 * @Date 2023/3/9 19:02
 * @Version 1.0
 */
@Slf4j
public class ReferenceAnnotationScanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, ApplicationContextAware, BeanClassLoaderAware,
        DisposableBean {

    public static final String BEAN_NAME = "referenceAnnotationScanPostProcessor";
    private ApplicationContext applicationContext;

    private ClassLoader classLoader;

    private final ConcurrentMap<String, InjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<String, InjectionMetadata>(256);

    private final ConcurrentMap<String, ReferenceFieldBean<?>> referenceBeansCache =
            new ConcurrentHashMap<String, ReferenceFieldBean<?>>();

    /**
     * 完成其他定制的一些依赖注入和依赖检查等
     *
     * @param pvs      属性
     * @param bean     ioc容器中的bean
     * @param beanName beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findReferenceMetadata(beanName, bean.getClass(), pvs);

        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;

        } catch (Throwable ex) {

            throw new BeanCreationException(beanName, "Injection of @Reference dependencies failed", ex);

        }
        return pvs;

    }


    private InjectionMetadata findReferenceMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {

        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        //先从缓冲中命中
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);

        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildReferenceMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for reference metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildReferenceMetadata(Class<?> clazz) {

        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();

        elements.addAll(findFieldReferenceMetadata(clazz));

        return new InjectionMetadata(clazz, elements);
    }

    private List<InjectionMetadata.InjectedElement> findFieldReferenceMetadata(final Class<?> beanClass) {

        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                FangReference reference = getAnnotation(field, FangReference.class);

                if (reference != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (log.isWarnEnabled()) {
                            log.warn("@Reference annotation is not supported on static fields: " + field);
                        }
                        return;
                    }
                    elements.add(new ReferenceFieldElement(field, reference));
                }
            }
        });
        return elements;
    }


    private class ReferenceFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;
        private final FangReference reference;

        protected ReferenceFieldElement(Field field, FangReference reference) {
            super(field, null);
            this.field = field;
            this.reference = reference;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> referenceClass = field.getType();

            Object referenceBean = buildReferenceBean(reference, referenceClass);

            // 将一个字段设置为可读写，主要针对private字段；
            // 相当于 setAccessible
            ReflectionUtils.makeAccessible(field);
            field.set(bean, referenceBean);
        }
    }

    private Object buildReferenceBean(FangReference reference, Class<?> referenceClass) throws Exception {
        String referenceBeanCacheKey = generateReferenceBeanCacheKey(reference, referenceClass);
        ReferenceFieldBean<?> referenceBean = referenceBeansCache.get(referenceBeanCacheKey);

        if (referenceBean == null) {

            //TODO RefrenceBean的创建
            referenceBean = new ReferenceFieldBean(reference, referenceClass, classLoader, applicationContext);
            referenceBeansCache.putIfAbsent(referenceBeanCacheKey, referenceBean);

        }

        return referenceBean.get();

    }


    private static String generateReferenceBeanCacheKey(FangReference reference, Class<?> beanClass) {
        String interfaceName = resolveInterfaceName(reference, beanClass);
        String key = reference.group() + "/" + interfaceName + ":" + reference.version();
        return key;
    }

    private static String resolveInterfaceName(FangReference reference, Class<?> beanClass)
            throws IllegalStateException {

        String interfaceName;

        if (beanClass.isInterface()) {
            interfaceName = beanClass.getName();
        } else {
            throw new IllegalStateException(
                    "The @Reference undefined interfaceClass or interfaceName, and the property type "
                            + beanClass.getName() + " is not a interface.");

        }
        return interfaceName;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition rootBeanDefinition, Class<?> aClass, String s) {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
