package fang.redamancy.core.config.spring.annotation.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author redamancy
 * @Date 2023/2/27 14:36
 * @Version 1.0
 */
@Slf4j
public class FangComponentScanRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, Ordered {

    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {


        AnnotationAttributes rpcScanAnnotationAttributes =
                AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(FangComponentScan.class.getName()));
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);

        if (rpcScanAnnotationAttributes != null) {
            // get the value of the basePackage property
            packagesToScan =
                    Arrays.stream(rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME)).collect(Collectors.toSet());
        }

        // Scan the RpcService annotation
//        CustomScanner rpcServiceScanner = new CustomScanner(registry, FangService.class);

//        if (resourceLoader != null) {
//            rpcServiceScanner.setResourceLoader(resourceLoader);
//        }
//
//        packagesToScan.forEach(rpcServiceScanner::scan);

    }


    /**
     * 返回需要扫描的包的全名，或手动添加的类的全限类名
     *
     * @param metadata
     * @return
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(FangComponentScan.class.getName()));

        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");
        // Appends value array attributes
        Set<String> packagesToScan = new LinkedHashSet<String>(Arrays.asList(value));

        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packagesToScan.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packagesToScan;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}

