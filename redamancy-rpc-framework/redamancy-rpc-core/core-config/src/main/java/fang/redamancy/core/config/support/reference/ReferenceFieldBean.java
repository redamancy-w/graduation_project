package fang.redamancy.core.config.support.reference;

import fang.redamancy.core.common.annotation.FangReference;
import fang.redamancy.core.config.support.AbstractServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

/**
 * 主要功能就是将客户端中标记访问远程服务的field 替换成自己写的代理对象
 *
 * @Author redamancy
 * @Date 2023/3/26 17:34
 * @Version 1.0
 */
@Slf4j
public class ReferenceFieldBean<T> extends AbstractServiceConfig<T> implements FactoryBean, ApplicationContextAware, InitializingBean {


    public ReferenceFieldBean(FangReference reference, Class<?> interfaceClazz) {

        super(reference);
        this.interfaceClass = interfaceClazz;
        this.interfaceName = interfaceClazz.getSimpleName();

    }

    public Object get() {

        if (Objects.isNull(ref)) {
            initProxyObj();
        }
        return ref;
    }

    private void initProxyObj() {
        if (isInit()) {
            return;
        }
        setInit(true);
        if (interfaceName == null || interfaceName.length() == 0) {
            throw new IllegalStateException("<fang:reference interface=\"\" /> interface not allow null!");
        }
        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }


    @Override
    public Object getObject() throws Exception {
        return get();
    }


    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    /**
     * bean初始化时属性初始化后的处理方法，
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        fullConfig();

        if (!isInit()) {
            getObject();
        }
    }

}
