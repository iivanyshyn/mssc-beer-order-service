package com.ivn.brewery.model.events;

import com.ivn.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeallocateOrderRequest {

    private BeerOrderDto beerOrder;
}
