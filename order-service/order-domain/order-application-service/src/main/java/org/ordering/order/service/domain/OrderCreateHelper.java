package org.ordering.order.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.ordering.order.service.domain.dto.create.CreateOrderCommand;
import org.ordering.order.service.domain.entity.Order;
import org.ordering.order.service.domain.entity.Restaurant;
import org.ordering.order.service.domain.event.OrderCreatedEvent;
import org.ordering.order.service.domain.exception.OrderDomainException;
import org.ordering.order.service.domain.mapper.OrderDataMapper;
import org.ordering.order.service.domain.repository.OrderRepository;
import org.ordering.order.service.domain.repository.RestaurantRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
//do mapper and actual things
@Slf4j
@Component
public class OrderCreateHelper {
    private final OrderDataMapper orderDataMapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    public OrderCreateHelper(OrderDataMapper orderDataMapper, RestaurantRepository restaurantRepository, OrderDomainService orderDomainService, OrderRepository orderRepository) {
        this.orderDataMapper = orderDataMapper;
        this.restaurantRepository = restaurantRepository;
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
    }
    public OrderCreatedEvent persistOrder(CreateOrderCommand createOrderCommand){
        //check if restaurant exist in database
        Restaurant restaurant = checkRestaurant(createOrderCommand);
        //create new order
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitializeOrder(order, restaurant);
        saveOrder(order);
        log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
        return orderCreatedEvent;
    }
    //check if restaurant exist in database
    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findRestaurantInformation(restaurant);
        if (optionalRestaurant.isEmpty()) {
            log.warn("Could not find restaurant with restaurant id: {}", createOrderCommand.getRestaurantId());
            throw new OrderDomainException("Could not find restaurant with restaurant id: " +
                    createOrderCommand.getRestaurantId());
        }
        return optionalRestaurant.get();
    }
    //save order to database
    private void saveOrder(Order order) {
        Order orderResult = orderRepository.save(order);
        if (orderResult == null) {
            log.error("Could not save order!");
            throw new OrderDomainException("Could not save order!");
        }
        log.info("Order is saved with id: {}", orderResult.getId().getValue());
    }
}
