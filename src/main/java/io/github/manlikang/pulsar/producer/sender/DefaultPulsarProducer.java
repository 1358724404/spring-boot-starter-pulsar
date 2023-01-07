package io.github.manlikang.pulsar.producer.sender;


import io.github.manlikang.pulsar.exception.PulsarSendFailException;
import io.github.manlikang.pulsar.message.SendFailMessage;
import io.github.manlikang.pulsar.producer.PulsarTemplate;
import io.github.manlikang.pulsar.utils.DateFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.TypedMessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author fuhan
 * @des 发送消息到Pulsar
 * @date 2022/8/5 - 14:49
 */
@Slf4j
public class DefaultPulsarProducer implements PulsarProducer {

  private final PulsarTemplate<byte[]> pulsarTemplate;

  private final MessageSendFailHandler messageSendFailHandler;

  public DefaultPulsarProducer(
      PulsarTemplate<byte[]> pulsarTemplate, MessageSendFailHandler messageSendFailHandler) {
    this.pulsarTemplate = pulsarTemplate;
    this.messageSendFailHandler = messageSendFailHandler;
  }

  /**
   * 同步发送消息至指定topic
   *
   * @param topic topic名称
   * @param msg 消息内容
   * @return messageId
   */
  @Override
  public String sendRealTimeSync(String topic, String msg) {
    log.info("Pulsar同步发送消息至Topic:{},报文内容:{}", topic, msg);
    try {
      final TypedMessageBuilder<byte[]> message =
          pulsarTemplate.createMessage(topic, msg.getBytes(StandardCharsets.UTF_8));
      final MessageId messageId = message.send();
      log.info("Pulsar消息发送成功,messageId: {}", messageId.toString());
      return messageId.toString();
    } catch (Exception e) {
      throw new PulsarSendFailException("Topic[" + topic + "]消息发送失败", e);
    }
  }

  /**
   * 异步发送消息至指定topic
   *
   * @param topic topic名称
   * @param msg 消息内容
   * @return CompletableFuture<MessageId>
   */
  @Override
  public CompletableFuture<MessageId> sendRealTimeAsync(String topic, String msg) {
    log.info("Pulsar异步发送消息至Topic:{},报文内容:{}", topic, msg);
    try {
      final TypedMessageBuilder<byte[]> message =
          pulsarTemplate.createMessage(topic, msg.getBytes(StandardCharsets.UTF_8));
      return message.sendAsync();
    } catch (Exception e) {
      throw new PulsarSendFailException("Topic[" + topic + "]消息发送失败", e);
    }
  }

  /**
   * 同步发送延时消息至指定topic
   *
   * @param topic topic名称
   * @param msg 消息内容
   * @param delay 延时时间 （多少时间之后再进行消费）
   * @param timeUnit 延时时间单位
   * @return messageId
   */
  @Override
  public String sendDelayedSync(String topic, String msg, long delay, TimeUnit timeUnit) {
    final String deliverAt =
        DateFormatUtil.format(new Date(System.currentTimeMillis() + timeUnit.toMillis(delay)));
    log.info("Pulsar同步发送延时消息至Topic:{},报文内容:{}，延时至：[{}]", topic, msg, deliverAt);
    try {
      final TypedMessageBuilder<byte[]> message =
          pulsarTemplate
              .createMessage(topic, msg.getBytes(StandardCharsets.UTF_8))
              .deliverAfter(delay, timeUnit);
      message.property("deliverAt", deliverAt);
      final MessageId messageId = message.send();
      log.info("[Pulsar]消息发送成功,messageId: {}", messageId.toString());
      return messageId.toString();
    } catch (Exception e) {
      throw new PulsarSendFailException("Topic[" + topic + "]消息发送失败", e);
    }
  }

  /**
   * 异步发送延时消息至指定topic
   *
   * @param topic topic名称
   * @param msg 消息内容
   * @param delay 延时时间 （多少时间之后再进行消费）
   * @param timeUnit 延时时间单位
   * @return messageId
   */
  @Override
  public CompletableFuture<MessageId> sendDelayedAsync(
      String topic, String msg, long delay, TimeUnit timeUnit) {
    final String deliverAt =
        DateFormatUtil.format(new Date(System.currentTimeMillis() + timeUnit.toMillis(delay)));
    log.info("Pulsar异步发送延时消息至Topic:{},报文内容:{}，延时至：[{}]", topic, msg, deliverAt);
    final TypedMessageBuilder<byte[]> message =
        pulsarTemplate
            .createMessage(topic, msg.getBytes(StandardCharsets.UTF_8))
            .deliverAfter(delay, timeUnit);
    message.property("deliverAt", deliverAt);
    return message.sendAsync();
  }

  @Override
  public void sendRealTimeAsyncWithCallbackLog(String topic, String msg) {
    sendRealTimeAsync(topic, msg)
        .whenComplete(
            (messageId, throwable) -> {
              if (throwable == null) {
                log.info("topic:{},实时消息异步发送成功：{},messageId:{}", topic, msg, messageId.toString());
              } else {
                messageSendFailHandler.handler(SendFailMessage.build(false, topic, msg, throwable));
              }
            });
  }

  @Override
  public void sendDelayedAsyncWithCallbackLog(
      String topic, String msg, long delay, TimeUnit timeUnit) {
    sendDelayedAsync(topic, msg, delay, timeUnit)
        .whenComplete(
            (messageId, throwable) -> {
              if (throwable == null) {
                log.info("topic:{},延时消息异步发送成功：{},messageId:{}", topic, msg, messageId.toString());
              } else {
                messageSendFailHandler.handler(SendFailMessage.build(true, topic, msg, throwable));
              }
            });
  }
}
