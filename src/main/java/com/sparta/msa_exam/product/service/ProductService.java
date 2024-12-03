package com.sparta.msa_exam.product.service;

import com.sparta.msa_exam.product.dto.ProductRequestDto;
import com.sparta.msa_exam.product.dto.ProductResponseDto;
import com.sparta.msa_exam.product.dto.ProductSearchDto;
import com.sparta.msa_exam.product.entity.Product;
import com.sparta.msa_exam.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @CachePut(cacheNames = "productCache", key = "#result.id")
    public ProductResponseDto createProduct(ProductRequestDto requestDto, String userId) {
        Product product = Product.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .supply_price(requestDto.getSupply_price())
                .quantity(requestDto.getQuantity())
                .createdBy(userId)
                .build();

        Product savedProduct = productRepository.save(product);
        return toResponseDto(savedProduct);
    }

    @Cacheable(value = "productSearchCache", keyGenerator = "customCacheKeyGenerator")
    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, Pageable pageable) {
        return productRepository.searchProducts(searchDto, pageable);
    }

    @Cacheable(cacheNames = "productCache", key = "args[0]")
    public ProductResponseDto getProductById(Long productId) {
        Product product = findProductById(productId);
        return toResponseDto(product);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "productSearchCache", allEntries = true)
    }, put = {
            @CachePut(cacheNames = "productCache", key = "args[0]")
    })
    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto, String userId) {
        Product product = findProductById(productId);

        product.updateProduct(requestDto.getName(), requestDto.getDescription(), requestDto.getSupply_price(), requestDto.getQuantity(), userId);
        Product updatedProduct = productRepository.save(product);

        return toResponseDto(updatedProduct);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "productCache", key = "#productId"),
            @CacheEvict(cacheNames = "productSearchCache", allEntries = true)
    })
    @Transactional
    public void deleteProduct(Long productId, String deletedBy) {
        Product product = findProductById(productId);

        product.deleteProduct(deletedBy);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "productCache", key = "#productId"),
            @CacheEvict(cacheNames = "productSearchCache", allEntries = true)
    })
    @Transactional
    public void reduceProductQuantity(Long productId, int quantity) {
        Product product = findProductById(productId);

        int previousQuantity = product.getQuantity();

        if (previousQuantity < quantity) {
            throw new IllegalArgumentException("Not enough quantity for product ID: " + productId);
        }

        product.reduceQuantity(quantity);
    }

    private ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSupply_price(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy()
        );
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));
    }
}
