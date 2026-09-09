//ai服务接口
package com.macro.mall.ai.service;

import java.io.IOException;

/**
 * DeepSeek AI服务接口
 */
public interface DeepSeekService {
    String askQuestion(String question) throws IOException;
}
