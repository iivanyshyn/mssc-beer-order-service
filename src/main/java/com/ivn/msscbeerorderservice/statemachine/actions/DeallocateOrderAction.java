package com.ivn.msscbeerorderservice.statemachine.actions;

import com.ivn.brewery.model.events.DeallocateOrderRequest;
import com.ivn.msscbeerorderservice.config.JmsConfig;
import com.ivn.msscbeerorderservice.domain.BeerOrder;
import com.ivn.msscbeerorderservice.domain.BeerOrderEventEnum;
import com.ivn.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.ivn.msscbeerorderservice.repositories.BeerOrderRepository;
import com.ivn.msscbeerorderservice.services.BeerOrderManagerImpl;
import com.ivn.msscbeerorderservice.web.mappers.BeerOrderMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        String beerOrderId = (String) context.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(beerOrderId));

        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE, DeallocateOrderRequest.builder()
                    .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                    .build());
            log.debug("Sent Deallocation request to queue for order id " + beerOrderId);
        }, () -> log.error("Beer Order Not Found!"));
    }
}
