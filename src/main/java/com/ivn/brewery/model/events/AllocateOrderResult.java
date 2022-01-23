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
public class AllocateOrderResult {

    private BeerOrderDto beerOrder;
    private Boolean allocationError = false;
    private Boolean pendingInventory = false;
}
