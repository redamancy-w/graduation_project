package fang.redamancy.core.common.banner;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author redamancy
 * @Date 2023/4/27 14:48
 * @Version 1.0
 */
@Slf4j
@Component
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {

        ThreadUtil.execute(() -> {
            // 延迟 1 秒，保证输出到结尾
            ThreadUtil.sleep(1, TimeUnit.SECONDS);
            log.info("starting success!");
        });
    }
}

