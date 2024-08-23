package com.alexapp.footballShop.services;

import com.alexapp.footballShop.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepo extends JpaRepository<Product ,Long> {
}
