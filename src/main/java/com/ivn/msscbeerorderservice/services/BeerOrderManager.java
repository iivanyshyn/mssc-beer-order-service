package com.ivn.msscbeerorderservice.services;

import com.ivn.msscbeerorderservice.domain.BeerOrder;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
}
