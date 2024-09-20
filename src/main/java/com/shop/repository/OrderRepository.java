package com.shop.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.domain.Order;
import com.shop.domain.OrderStatus;
import com.shop.domain.QMember;
import com.shop.domain.QOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

@Slf4j
@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        log.debug("Executing findAll with orderSearch: {}", orderSearch);

        if (query == null) {
            log.error("JPAQueryFactory is not initialized");
            throw new IllegalStateException("JPAQueryFactory is not initialized");
        }

        if (orderSearch == null) {
            log.warn("OrderSearch is null, returning empty list");
            return List.of();
        }

        QOrder order = QOrder.order;
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        BooleanExpression statusCondition = statusEq(orderSearch.getOrderStatus());
        if (statusCondition != null) {
            builder.and(statusCondition);
        }

        BooleanExpression nameCondition = nameLike(orderSearch.getMemberName());
        if (nameCondition != null) {
            builder.and(nameCondition);
        }

        List<Order> result = query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(builder)
                .limit(1000)
                .fetch();

        log.debug("Found {} orders", result.size());
        return result;
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        return statusCond != null ? QOrder.order.status.eq(statusCond) : null;
    }

    private BooleanExpression nameLike(String nameCond) {
        return StringUtils.hasText(nameCond) ? QMember.member.name.like("%" + nameCond + "%") : null;
    }
}