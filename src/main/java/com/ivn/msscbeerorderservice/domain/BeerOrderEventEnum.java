package com.ivn.msscbeerorderservice.domain;

public enum BeerOrderEventEnum {
    VALIDATE_ORDER, VALIDATION_PASSED, VALIDATION_FAILED, ALLOCATE_ORDER, ALLOCATION_SUCCESS, ALLOCATION_NO_INVENTORY,
    ALLOCATION_FAILED, ORDER_PICKED_UP
}
