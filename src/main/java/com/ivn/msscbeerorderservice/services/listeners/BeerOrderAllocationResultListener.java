package com.ivn.msscbeerorderservice.services.listeners;

import com.ivn.brewery.model.events.AllocateOrderResult;
import com.ivn.msscbeerorderservice.config.JmsConfig;
import com.ivn.msscbeerorderservice.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderAllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult result) {
        if (!result.getAllocationError() && !result.getPendingInventory()) {
            // allocated without issues
            beerOrderManager.beerOrderAllocationPassed(result.getBeerOrder());
        } else if (!result.getAllocationError() && result.getPendingInventory()) {
            // pending inventory
            beerOrderManager.beerOrderAllocationPendingInventory(result.getBeerOrder());
        } else if (result.getAllocationError()) {
            // allocation error
            beerOrderManager.beerOrderAllocationFailed(result.getBeerOrder());
        }
    }
}
