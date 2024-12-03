package com.sparta.msa_exam.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Integer supply_price;
    private Integer quantity;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateProduct(String name, String description, Integer price, Integer quantity, String updatedBy) {
        this.name = name;
        this.description = description;
        this.supply_price = price;
        this.quantity = quantity;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public void deleteProduct(String deletedBy) {
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }

    public void reduceQuantity(int i) {
        this.quantity = this.quantity - i;
    }
}