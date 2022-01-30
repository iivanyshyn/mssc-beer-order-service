package com.ivn.msscbeerorderservice.services.testcomponents;

import com.ivn.brewery.model.events.ValidateOrderRequest;
import com.ivn.brewery.model.events.ValidateOrderResult;
import com.ivn.msscbeerorderservice.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<ValidateOrderRequest> msg) {
        boolean isValid = true;
        ValidateOrderRequest request = msg.getPayload();

        if(Objects.equals(request.getBeerOrder().getCustomerRef(), "fail-validation")){
            isValid = false;
        }
        await().during(50, TimeUnit.MILLISECONDS);

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                ValidateOrderResult.builder()
                        .isValid(isValid)
                        .orderId(request.getBeerOrder().getId())
                        .build());

    }
}
