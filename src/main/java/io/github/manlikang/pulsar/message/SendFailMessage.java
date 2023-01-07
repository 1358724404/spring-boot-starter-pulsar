package io.github.manlikang.pulsar.message;

import lombok.Data;

/**
 * @author fuhan
 * @date 2022/12/1 - 17:55
 */
@Data
public class SendFailMessage {

  private boolean delay;

  private boolean async;

  private Throwable throwable;

  private String msg;

  private String topic;

  public static SendFailMessage build(
      boolean delay, String topic, String msg, Throwable throwable) {
    SendFailMessage sendFailMessage = new SendFailMessage();
    sendFailMessage.setAsync(true);
    sendFailMessage.setDelay(delay);
    sendFailMessage.setThrowable(throwable);
    sendFailMessage.setTopic(topic);
    sendFailMessage.setMsg(msg);
    return sendFailMessage;
  }
}
