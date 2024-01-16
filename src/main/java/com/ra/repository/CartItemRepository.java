package com.ra.repository;

import com.ra.model.entity.Cart;
import com.ra.model.entity.Cart_item;
import com.ra.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface CartItemRepository extends JpaRepository<Cart_item, Long> {
    Cart_item findByCart(Cart cart);

    Cart_item findAllByCartAndProduct(Cart cart, Product product);

    Boolean existsCart_itemByCartAndProduct(Cart cart, Product product);

    Cart_item findByCartAndProduct(Cart cart, Product product);

    Cart_item findCart_itemById(Long id);

    List<Cart_item> findAllByCart_Id(Long id);

    @Modifying
    @Query("SELECT ci.product FROM Cart_item ci WHERE ci.cart.user.id = :userId")
    List<Cart_item> getCartItems(@Param("userId") Long userId);
}
