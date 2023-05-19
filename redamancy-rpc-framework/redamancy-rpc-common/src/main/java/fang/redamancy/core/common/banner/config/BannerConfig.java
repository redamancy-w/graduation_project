package fang.redamancy.core.common.banner.config;

import fang.redamancy.core.common.banner.BannerApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author redamancy
 * @Date 2023/4/27 15:00
 * @Version 1.0
 */
@Configuration
public class BannerConfig {

    @Bean
    public BannerApplicationRunner bannerApplicationRunner() {
        return new BannerApplicationRunner();
    }
}
