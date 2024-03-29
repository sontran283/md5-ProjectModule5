package com.ra.service;

import com.ra.exception.CustomException;
import com.ra.exception.ProductNotFoundException;
import com.ra.exception.UserNotFoundException;
import com.ra.model.dto.request.AddtoCartRequestDTO;
import com.ra.model.dto.request.UpdateCartItemRequestDTO;
import com.ra.model.entity.Cart;
import com.ra.model.entity.Cart_item;
import com.ra.model.entity.Product;
import com.ra.model.entity.User;
import com.ra.repository.CartItemRepository;
import com.ra.repository.CartRepository;
import com.ra.repository.ProductRepository;
import com.ra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart getUserCart(User user) {
        return cartRepository.findByUser(user);
    }

    @Override
    public List<Cart_item> findAll() {
        List<Cart_item> cartList = cartItemRepository.findAll();
        return cartList.stream().map(Cart_item::new).toList();
    }

    @Override
    public void addToCart(Long userId, AddtoCartRequestDTO addtoCartRequestDTO) throws UserNotFoundException, ProductNotFoundException {
        User user1 = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User id not found"));
        Cart cart = cartRepository.findByUser(user1);
        Product product = productRepository.findById(addtoCartRequestDTO.getProductId()).orElseThrow(() -> new ProductNotFoundException("product id not found"));

        if (!product.getStatus()) {
            throw new ProductNotFoundException("This product is currently off, you can add to cart");
        }

        if (cart == null) {
            Cart cart1 = new Cart();
            cart1.setUser(user1);
            cartRepository.save(cart1);
        }

        if (cartItemRepository.existsCart_itemByCartAndProduct(cart, product)) {
            Cart_item cartItem = cartItemRepository.findByCartAndProduct(cart, product);
            cartItem.setQuantity(cartItem.getQuantity() + addtoCartRequestDTO.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            Cart_item cartItem1 = new Cart_item();
            cartItem1.setCart(cart);
            cartItem1.setProduct(product);
            cartItem1.setQuantity(addtoCartRequestDTO.getQuantity());
            cartItem1.setPrice(product.getPrice());
            cartItemRepository.save(cartItem1);
        }
    }


    @Override
    public List<Product> getCartItems(User user) {
        return cartRepository.getCartItems(user.getId());
    }

    @Override
    public void removeFromCart(Long id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    public void clearCart(Long id) {
        User user = userRepository.findById(id).orElse(null);
        Cart cart = cartRepository.findByUser(user);
        if (cart != null) {
            cartItemRepository.deleteCartItemByCartId(cart.getId());
        }
    }

    @Override
    public double cartTotal(User user) {
        List<Product> cartItems = getCartItems(user);
        return cartItems.stream().mapToDouble(Product::getPrice).sum();
    }

    @Override
    public void updateCartItem(Long userId, UpdateCartItemRequestDTO updateCartItemRequestDTO) throws CustomException, UserNotFoundException {
        // Kiểm tra sự tồn tại của người dùng và sản phẩm
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        Cart_item cartItem = cartItemRepository.findById(updateCartItemRequestDTO.getCartItemId())
                .orElseThrow(() -> new CustomException("Cart item not found"));

        // Kiểm tra sự hợp lệ của số lượng
        if (updateCartItemRequestDTO.getQuantity() > 0) {
            // Cập nhật số lượng và lưu lại vào cơ sở dữ liệu
            cartItem.setQuantity(updateCartItemRequestDTO.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            throw new CustomException("Invalid quantity. Please enter a valid quantity.");
        }
    }
}
