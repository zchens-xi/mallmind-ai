package com.macro.mall.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EtlService {
    // ... 注入所有需要的源和目标Repository ...

    @Scheduled(cron = "0 0 3 * * ?")
    public void syncProducts() {

        // ... 同步商品的逻辑 ...
    }

    @Scheduled(cron = "0 15 3 * * ?")
    public void syncUsers() {
        // ... 同步用户的逻辑 ...
    }
}
