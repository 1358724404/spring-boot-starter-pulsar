package com.github.manlikang.pulsar.consumer.handler;


import com.github.manlikang.pulsar.error.FailedMessage;

/**
 * @author fuhan
 * @date 2022/9/9
 */
public interface ConsumerExceptionHandler {

  /**
   * 消费报错处理器
   *
   * @param failedMessage 错误消息
   */
  void handler(FailedMessage failedMessage);
}
