package com.macro.mall.dto;

import lombok.Data;
import java.util.List;

/**
 * 用户行为分析DTO
 */
@Data
public class UserBehaviorDto {

    /**
     * 漏斗数据
     */
    private List<FunnelData> funnelData;

    @Data
    public static class FunnelData {
        /**
         * 行为阶段
         */
        private String stage;

        /**
         * 用户数量
         */
        private Long value;

        // 手动添加setter方法解决编译问题
        public void setStage(String stage) {
            this.stage = stage;
        }

        public void setValue(Long value) {
            this.value = value;
        }
    }

    // 手动添加setter方法解决编译问题
    public void setFunnelData(List<FunnelData> funnelData) {
        this.funnelData = funnelData;
    }
}
