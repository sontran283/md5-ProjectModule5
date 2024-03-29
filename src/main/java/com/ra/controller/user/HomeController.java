package com.ra.controller.user;

import com.ra.exception.CustomException;
import com.ra.model.dto.request.ChangePasswordRequestDTO;
import com.ra.model.dto.request.UserRequestDTO;
import com.ra.model.dto.response.OrderResponseDTO;
import com.ra.model.dto.response.ProductResponseDTO;
import com.ra.model.dto.response.UserResponseAllDTO;
import com.ra.model.entity.User;
import com.ra.repository.UserRepository;
import com.ra.security.user_principle.UserDetailService;
import com.ra.service.OrdersService;
import com.ra.service.ProductService;
import com.ra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user")
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailService userDetailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private ProductService productService;

    // index
    @GetMapping("/getListProductIndex")
    public ResponseEntity<?> getListProductIndex() {
        List<ProductResponseDTO> productList = productService.findAll();
        List<ProductResponseDTO> filteredProducts = productList.stream().filter(ProductResponseDTO::getStatus).toList();
        return new ResponseEntity<>(filteredProducts, HttpStatus.OK);
    }


    // order history
    @GetMapping("/history")
    public ResponseEntity<?> history(Authentication authentication) {
        Long userId = userDetailService.getUserIdFromAuthentication(authentication);
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<OrderResponseDTO> orderResponseDTO = ordersService.getListOrderByUser(user);
            return new ResponseEntity<>(orderResponseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }


    // get userById
    @GetMapping("/userById")
    public ResponseEntity<?> userById(Authentication authentication) throws CustomException {
        Long userId = userDetailService.getUserIdFromAuthentication(authentication);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            UserResponseAllDTO userResponseAllDTO = userService.findByIdd(user.getId());
            if (userResponseAllDTO != null) {
                return new ResponseEntity<>(userResponseAllDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User details not found", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }


    // changePass
    @PatchMapping("/changePass")
    public ResponseEntity<?> changePass(@RequestBody ChangePasswordRequestDTO changePasswordRequestDTO, Authentication authentication) throws CustomException {
        Long userId = userDetailService.getUserIdFromAuthentication(authentication);
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            if (!userService.validateOldPassword(user, changePasswordRequestDTO.getOldPassword())) {
                return new ResponseEntity<>("Old password is incorrect", HttpStatus.BAD_REQUEST);
            }

            userService.changePassword(user, changePasswordRequestDTO.getNewPassword());

            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("NOT_FOUND", HttpStatus.NOT_FOUND);
        }
    }

    // change profile
    @PutMapping("/changeProfile")
    public ResponseEntity<?> changeProfile(@ModelAttribute UserRequestDTO userRequestDTO, Authentication authentication) throws CustomException {
        Long userId = userDetailService.getUserIdFromAuthentication(authentication);
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            UserResponseAllDTO userResponseDTO = userService.changeProfile(userId, userRequestDTO);
            return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>("NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
