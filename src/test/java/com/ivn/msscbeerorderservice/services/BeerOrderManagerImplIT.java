package com.ivn.msscbeerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.ivn.brewery.model.BeerDto;
import com.ivn.msscbeerorderservice.domain.BeerOrder;
import com.ivn.msscbeerorderservice.domain.BeerOrderLine;
import com.ivn.msscbeerorderservice.domain.BeerOrderStatusEnum;
import com.ivn.msscbeerorderservice.domain.Customer;
import com.ivn.msscbeerorderservice.repositories.BeerOrderRepository;
import com.ivn.msscbeerorderservice.repositories.CustomerRepository;
import com.ivn.msscbeerorderservice.services.beer.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(WireMockExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;
    @Autowired
    BeerOrderRepository beerOrderRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    WireMockServer wireMockServer;
    @Autowired
    ObjectMapper objectMapper;

    Customer testCustomer;
    UUID beerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test customer")
                .build());
    }

    @Test
    @Order(1)
    void testNewToAllocated() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder("12345");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();

            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            BeerOrderLine line = foundOrder.getBeerOrderLines().iterator().next();
            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(savedBeerOrder2);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder2.getOrderStatus());
        savedBeerOrder2.getBeerOrderLines()
                .forEach(line -> assertEquals(line.getOrderQuantity(), line.getQuantityAllocated()));
    }

    @Test
    @Order(2)
    void testFailedValidation() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("123457").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "123457")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder("123457");
        beerOrder.setCustomerRef("fail-validation");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();

            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    @Order(3)
    void testNewToPickUp() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("123456").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "123456")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder("123456");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
        });

        BeerOrder pickedUpOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertEquals(BeerOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus());
    }

    @Test
    @Order(4)
    void testAllocationFailure() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("123456").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "123456")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder("123456");
        beerOrder.setCustomerRef("fail-allocation");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    @Order(5)
    void testPartialAllocation() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("123456").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH_V1 + "123456")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        BeerOrder beerOrder = createBeerOrder("123456");
        beerOrder.setCustomerRef("partial-allocation");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus());
        });
    }

    public BeerOrder createBeerOrder(String upc) {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();
        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .upc(upc)
                .orderQuantity(1)
                .beerOrder(beerOrder)
                .build());

        beerOrder.setBeerOrderLines(lines);
        return beerOrder;
    }

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8085));
            server.start();
            return server;
        }
    }
}
