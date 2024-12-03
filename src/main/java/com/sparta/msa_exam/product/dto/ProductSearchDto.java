package com.sparta.msa_exam.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchDto {
    private String name;
    private String description;
    private Double minPrice;
    private Double maxPrice;
    private Integer minQuantity;
    private Integer maxQuantity;
}