package com.ivn.msscbeerorderservice.statemachine.actions;

import com.ivn.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.ivn.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.ivn.msscbeerorderservice.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ValidateFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        log.error("Compensating Transaction.... Validation failed: " + beerOrderId);
    }
}
