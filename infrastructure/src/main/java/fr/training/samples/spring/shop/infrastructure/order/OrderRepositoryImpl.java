package fr.training.samples.spring.shop.infrastructure.order;

import org.springframework.stereotype.Repository;

import fr.training.samples.spring.shop.domain.common.exception.NotFoundException;
import fr.training.samples.spring.shop.domain.order.OrderRepository;
import fr.training.samples.spring.shop.domain.order.Orders;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

	private final OrderJpaRepository orderJpaRepository;

	public OrderRepositoryImpl(final OrderJpaRepository orderJpaRepository) {
		this.orderJpaRepository = orderJpaRepository;
	}

	@Override
	public Orders findById(final String orderId) {
		return orderJpaRepository.findById(orderId).orElseThrow(() -> new NotFoundException());
	}

	@Override
	public void save(final Orders order) {
		orderJpaRepository.save(order);
	}

}