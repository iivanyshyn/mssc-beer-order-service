package com.ivn.msscbeerorderservice.statemachine.actions;

import com.ivn.brewery.model.events.ValidateOrderRequest;
import com.ivn.msscbeerorderservice.config.JmsConfig;
import com.ivn.msscbeerorderservice.domain.BeerOrder;
import com.ivn.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.ivn.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.ivn.msscbeerorderservice.repositories.BeerOrderRepository;
import com.ivn.msscbeerorderservice.services.BeerOrderManagerImpl;
import com.ivn.msscbeerorderservice.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, ValidateOrderRequest.builder()
                    .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                    .build());
            log.debug("Sent Validation request to queue for order id " + beerOrderId);
        }, () -> log.error("Order Not Found. Id: " + beerOrderId));

    }
}
