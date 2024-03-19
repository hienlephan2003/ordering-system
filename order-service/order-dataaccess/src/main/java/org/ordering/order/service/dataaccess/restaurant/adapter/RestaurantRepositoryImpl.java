package org.ordering.order.service.dataaccess.restaurant.adapter;

import org.ordering.dataaccess.restaurant.entity.RestaurantEntity;
import org.ordering.dataaccess.restaurant.entity.RestaurantEntityId;
import org.ordering.dataaccess.restaurant.repository.RestaurantJpaRepository;
import org.ordering.domain.valueobject.RestaurantId;
import org.ordering.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import org.ordering.order.service.domain.entity.Restaurant;
import org.ordering.order.service.domain.repository.RestaurantRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RestaurantRepositoryImpl implements RestaurantRepository {
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository, RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> restaurantProducts =
                restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);

        Optional<List<RestaurantEntity>> restaurantEntities = restaurantJpaRepository
                .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(),
                        restaurantProducts);
        return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
    }
}
