package com.vineyard.mqttbroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class MqttErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttErrorHandler.class);

    /**
     * Captura qualquer exceção que ocorra no fluxo MQTT após a recepção da mensagem.
     * A exceção é encapsulada em uma MessagingException.
     */
    @ServiceActivator(inputChannel = "mqttErrorChannel")
    public void handleMqttError(Message<MessagingException> message) {
        MessagingException exception = message.getPayload();
        Message<?> failedMessage = exception.getFailedMessage();
        Throwable cause = exception.getCause();

        log.error(
            "!!! ERRO AO PROCESSAR MENSAGEM MQTT. Causa: {}",
            cause != null ? cause.getMessage() : "Causa desconhecida",
            exception
        );

        log.error("Mensagem com falha: {}", failedMessage);
    }
}
