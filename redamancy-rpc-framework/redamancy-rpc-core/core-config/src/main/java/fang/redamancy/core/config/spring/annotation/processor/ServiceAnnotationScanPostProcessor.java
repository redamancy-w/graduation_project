package fang.redamancy.core.config.spring.annotation.processor;

import fang.redamancy.core.common.annotation.FangService;
import fang.redamancy.core.config.spring.annotation.server.CustomScanner;
import fang.redamancy.core.config.support.service.ApplicationListenerRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * 处理Service注解的后置处理器
 *
 * @Author redamancy
 * @Date 2023/2/27 17:09
 * @Version 1.0
 */
@Slf4j
public class ServiceAnnotationScanPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware,
        ResourceLoaderAware, BeanClassLoaderAware, Ordered {


    private Environment environment;

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    private final Set<String> packagesToScan;

    public ServiceAnnotationScanPostProcessor(String... packagesToScan) {
        this(Arrays.asList(packagesToScan));
    }

    public ServiceAnnotationScanPostProcessor(Collection<String> packagesToScan) {
        this(new LinkedHashSet<String>(packagesToScan));
    }

    public ServiceAnnotationScanPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }


    /**
     * 所有的bean定义信息，即将要被加载到IOC容器中(其实还没有被加载到容器中)，
     * bean实例还没有被初始化时，BeanDefinitionRegistryPostProcessor被调用。
     *
     * @throws BeansException
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Set<String> resolvedPackagesToScan = resolvePackagesToScan(packagesToScan);

        if (!CollectionUtils.isEmpty(resolvedPackagesToScan)) {
            registerServiceBeans(resolvedPackagesToScan, registry);
        } else {
            log.warn("packagesToScans is empty");
        }

    }

    /**
     * 进行扫描和解析
     *
     * @param resolvedPackagesToScan
     * @param registry
     */
    private void registerServiceBeans(Set<String> resolvedPackagesToScan, BeanDefinitionRegistry registry) {
        CustomScanner scanner = new CustomScanner(registry, false, FangService.class, environment, resourceLoader);

        // beanName解析器
        BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

        scanner.setBeanNameGenerator(beanNameGenerator);


        //扫描正式开始，遍历包
        for (String packageToScan : packagesToScan) {

            // 先注册带有@FangService的bean
            scanner.scan(packageToScan);

            // 开始查找添加了@FangService注解的类
            Set<BeanDefinitionHolder> beanDefinitionHolders =
                    findServiceBeanDefinitionHolders(scanner, packageToScan, registry, beanNameGenerator);

            if (!CollectionUtils.isEmpty(beanDefinitionHolders)) {

                for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {

                    //注册ApplicationListenerRegistrar
                    registerApplicationListenerRegistrar(beanDefinitionHolder, registry, scanner);
                }

            } else {

                log.warn("在配置的包路径下没有发现带有@FangService的类["
                        + packageToScan + "]");

            }

        }
    }

    private void registerApplicationListenerRegistrar(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, CustomScanner scanner) {

        //服务接口类对象
        Class<?> beanClass = resolveClass(beanDefinitionHolder);

        //找到@service注解
        FangService service = findAnnotation(beanClass, FangService.class);

        //接口服务的实现类对象
        Class<?> interfaceClass = resolveServiceInterfaceClass(beanClass, service);

        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();

        // 构建ApplicationListenerRegistrar对象的BeanDefinition,通过Service注解对象，以及接口服务的实现类生成
        AbstractBeanDefinition serviceBeanDefinition =
                buildApplicationListenerRegistrarDefinition(service, interfaceClass, annotatedServiceBeanName);

        // 构建ApplicationListenerRegistrar的名称
        String beanName = generateServiceBeanName(interfaceClass, annotatedServiceBeanName);

        //校验Bean是否重复
        if (scanner.checkCandidate(beanName, serviceBeanDefinition)) {
            registry.registerBeanDefinition(beanName, serviceBeanDefinition);

            log.warn("这 BeanDefinition[" + serviceBeanDefinition +
                    "] 这个名字已经被注册 : " + beanName);

        } else {

            log.warn("这 BeanDefinition[" + serviceBeanDefinition +
                    "] 类名[ bean name : " + beanName +
                    "] 已经注册 , 检测包路径是否被多次扫描?");

        }
    }

    private String generateServiceBeanName(Class<?> interfaceClass, String annotatedServiceBeanName) {

        return "ServiceBean@" + interfaceClass.getName() + "#" + annotatedServiceBeanName;

    }


    private AbstractBeanDefinition buildApplicationListenerRegistrarDefinition(FangService service, Class<?> interfaceClass, String annotatedServiceBeanName) {

        BeanDefinitionBuilder builder = rootBeanDefinition(ApplicationListenerRegistrar.class)
                .addConstructorArgValue(service)
                //将ref属性引用到带有@FangService注解的bean上
                .addPropertyReference("ref", annotatedServiceBeanName).addPropertyValue("interface", interfaceClass.getName());

        return builder.getBeanDefinition();
    }

    /**
     * 返回注解下的类所实习接口类的class
     *
     * @param beanClass
     * @param service
     * @return
     */
    private Class<?> resolveServiceInterfaceClass(Class<?> beanClass, FangService service) {

        Class<?> interfaceClass = null;

        Class<?>[] allInterfaces = beanClass.getInterfaces();

        if (allInterfaces.length > 0) {
            interfaceClass = allInterfaces[0];
        }

        Assert.notNull(interfaceClass,
                "@FangService 所注释的类必须实现接口");

        return interfaceClass;

    }


    /**
     * 服务接口类对象
     *
     * @param beanDefinitionHolder
     * @return
     */
    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {

        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();

        return resolveClass(beanDefinition);

    }

    private Class<?> resolveClass(BeanDefinition beanDefinition) {

        String beanClassName = beanDefinition.getBeanClassName();

        return resolveClassName(beanClassName, classLoader);
    }

    /**
     * 查找注释了@FangService的 {@link BeanDefinitionHolder}的Set集合
     *
     * @param scanner
     * @param packageToScan
     * @param registry
     * @param beanNameGenerator
     * @return
     */
    private Set<BeanDefinitionHolder> findServiceBeanDefinitionHolders(CustomScanner scanner, String packageToScan,
                                                                       BeanDefinitionRegistry registry,
                                                                       BeanNameGenerator beanNameGenerator) {


        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);

        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<BeanDefinitionHolder>(beanDefinitions.size());

        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);
        }
        return beanDefinitionHolders;

    }

    /**
     * 使用BeanNameGenerator来生成实例 {@link BeanNameGenerator}
     *
     * @param registry
     * @return
     */
    private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {

        BeanNameGenerator beanNameGenerator = null;

        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry singletonBeanRegistry = SingletonBeanRegistry.class.cast(registry);
            beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
        }

        if (beanNameGenerator == null) {

            log.info("BeanNameGenerator bean can't be found in BeanFactory with name ["
                    + CONFIGURATION_BEAN_NAME_GENERATOR + "]");

            beanNameGenerator = new AnnotationBeanNameGenerator();

        }

        return beanNameGenerator;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {

    }

    private Set<String> resolvePackagesToScan(Set<String> packagesToScan) {
        Set<String> resolvedPackagesToScan = new LinkedHashSet<String>(packagesToScan.size());
        for (String packageToScan : packagesToScan) {
            if (StringUtils.hasText(packageToScan)) {
                String resolvedPackageToScan = environment.resolvePlaceholders(packageToScan.trim());
                resolvedPackagesToScan.add(resolvedPackageToScan);
            }
        }
        return resolvedPackagesToScan;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @Override
    public int getOrder() {

        return LOWEST_PRECEDENCE;
    }
}
