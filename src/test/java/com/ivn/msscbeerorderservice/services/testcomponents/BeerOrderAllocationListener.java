package com.ivn.msscbeerorderservice.services.testcomponents;

import com.ivn.brewery.model.events.AllocateOrderRequest;
import com.ivn.brewery.model.events.AllocateOrderResult;
import com.ivn.msscbeerorderservice.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg) {
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;

        if(Objects.equals(request.getBeerOrder().getCustomerRef(), "partial-allocation")){
            pendingInventory = true;
        }

        if(Objects.equals(request.getBeerOrder().getCustomerRef(), "fail-allocation")){
            allocationError = true;
        } else if(Objects.equals(request.getBeerOrder().getCustomerRef(), "dont-allocate")){
            sendResponse = false;
        }
        boolean finalPendingInventory = pendingInventory;
        request.getBeerOrder().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (finalPendingInventory) {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() -1);
            } else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
        });

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, AllocateOrderResult.builder()
                    .beerOrder(request.getBeerOrder())
                    .pendingInventory(pendingInventory)
                    .allocationError(allocationError)
                    .build());
        }

    }
}
