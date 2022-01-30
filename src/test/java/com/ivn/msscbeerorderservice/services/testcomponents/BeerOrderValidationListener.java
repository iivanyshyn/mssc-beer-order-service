package com.ivn.msscbeerorderservice.services.testcomponents;

import com.ivn.brewery.model.events.ValidateOrderRequest;
import com.ivn.brewery.model.events.ValidateOrderResult;
import com.ivn.msscbeerorderservice.config.JmsConfig;
import com.ivn.msscbeerorderservice.domain.BeerOrder;
import com.ivn.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.ivn.msscbeerorderservice.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<ValidateOrderRequest> msg) {
        boolean isValid = true;
        boolean sendResponse = true;
        ValidateOrderRequest request = msg.getPayload();

        if (Objects.equals(request.getBeerOrder().getCustomerRef(), "fail-validation")) {
            isValid = false;
        } else if(Objects.equals(request.getBeerOrder().getCustomerRef(), "dont-validate")){
            sendResponse = false;
        }

        await().untilAsserted(() -> {
            BeerOrder beerOrder = beerOrderRepository.findById(request.getBeerOrder().getId()).get();
            assertEquals(beerOrder.getOrderStatus(), BeerOrderStatusEnum.VALIDATION_PENDING);
        });

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder()
                            .isValid(isValid)
                            .orderId(request.getBeerOrder().getId())
                            .build());
        }

    }
}
