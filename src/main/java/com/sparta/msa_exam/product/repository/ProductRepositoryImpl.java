package com.sparta.msa_exam.product.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.msa_exam.product.dto.ProductResponseDto;
import com.sparta.msa_exam.product.dto.ProductSearchDto;
import com.sparta.msa_exam.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.msa_exam.product.entity.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, Pageable pageable) {
        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

        QueryResults<Product> results = queryFactory
                .selectFrom(product)
                .where(
                        nameContains(searchDto.getName()),
                        descriptionContains(searchDto.getDescription()),
                        priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()),
                        quantityBetween(searchDto.getMinQuantity(), searchDto.getMaxQuantity())
                )
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<ProductResponseDto> content = results.getResults().stream()
                .map(product -> new ProductResponseDto(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getSupply_price(),
                        product.getQuantity(),
                        product.getCreatedAt(),
                        product.getCreatedBy(),
                        product.getUpdatedAt(),
                        product.getUpdatedBy()
                ))
                .collect(Collectors.toList());

        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression descriptionContains(String description) {
        return description != null ? product.description.containsIgnoreCase(description) : null;
    }

    private BooleanExpression priceBetween(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return product.supply_price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return product.supply_price.goe(minPrice);
        } else if (maxPrice != null) {
            return product.supply_price.loe(maxPrice);
        } else {
            return null;
        }
    }

    private BooleanExpression quantityBetween(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null) {
            return product.quantity.between(minQuantity, maxQuantity);
        } else if (minQuantity != null) {
            return product.quantity.goe(minQuantity);
        } else if (maxQuantity != null) {
            return product.quantity.loe(maxQuantity);
        } else {
            return null;
        }
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort() != null) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                com.querydsl.core.types.Order direction = sortOrder.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
                switch (sortOrder.getProperty()) {
                    case "createdAt":
                        orders.add(new OrderSpecifier<>(direction, product.createdAt));
                        break;
                    case "price":
                        orders.add(new OrderSpecifier<>(direction, product.supply_price));
                        break;
                    case "quantity":
                        orders.add(new OrderSpecifier<>(direction, product.quantity));
                        break;
                    default:
                        break;
                }
            }
        }

        return orders;
    }
}
