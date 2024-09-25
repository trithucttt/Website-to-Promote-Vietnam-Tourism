package com.trithuc.controller;

import com.trithuc.dto.VnpPaymentDTO;
import com.trithuc.request.AddToCartRequest;
import com.trithuc.request.OrderRequest;
import com.trithuc.response.MessageResponse;
import com.trithuc.service.ConfigPaymenntService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/api/")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class CheckOutController {


    @Autowired
    private ConfigPaymenntService configPaymenntService;

    @PostMapping("create")
    public ResponseEntity<MessageResponse> createUrlPayment() throws UnsupportedEncodingException{
        return null;
        // return configPaymenntService.createUrlPayment();
    }


    @PostMapping( "cart/add")
    public ResponseEntity<MessageResponse> addToCart(@RequestHeader(name = "Authorization") String token,
                                                     @RequestBody AddToCartRequest addToCartRequest){
        return configPaymenntService.addToCart(addToCartRequest,token);
    }

    @GetMapping("cart/info")
    public ResponseEntity<MessageResponse> getInfoCart(@RequestHeader(name = "Authorization") String token){
        return configPaymenntService.getInfoCart(token);
    }

    @PutMapping("cart/increase/{cartItemId}")
    public ResponseEntity<MessageResponse> incrementQuantityCart(@PathVariable Long cartItemId){
        return configPaymenntService.incrementQuantityCart(cartItemId);
    }
    @PutMapping("cart/decrease/{cartItemId}")
    public ResponseEntity<MessageResponse> DecrementQuantityCart(@PathVariable Long cartItemId){
        return configPaymenntService.DecrementQuantityCart(cartItemId);
    }

    @DeleteMapping("cart/delete/{cartItemId}")
    public ResponseEntity<MessageResponse> deleteCartItem(@PathVariable Long cartItemId){
        return configPaymenntService.DecrementQuantityCart(cartItemId);
    }

    @GetMapping("cart/payment/createUrl")
    public ResponseEntity<MessageResponse> getUrlPayment(@RequestParam("username") String username,
                                                         @RequestParam("totalPrice") Double totalPrice) throws UnsupportedEncodingException {
        return configPaymenntService.createUrlPayment(username,totalPrice);
    }
    @PostMapping("cart/payment/process")
    public ResponseEntity<?> processPaymentResult(@RequestHeader(name = "Authorization") String token,
                                                  @RequestBody VnpPaymentDTO requestData) {
        return configPaymenntService.handlePaymentResult(requestData,token);
    }

    @PostMapping("cart/payment/save")
    public ResponseEntity<MessageResponse> checkOutAndSendReceipt(@RequestBody OrderRequest orderRequest,
                                                                  @RequestHeader(name = "Authorization") String token){
        return configPaymenntService.sendReceipt(orderRequest,token);
    }

    @GetMapping("order/info")
    public ResponseEntity<MessageResponse> getOrderHistory(@RequestHeader(name = "Authorization") String token){
        return configPaymenntService.oderHistory(token);
    }

    @PostMapping("order/approve")
    public ResponseEntity<MessageResponse> ApprovedOrder(@RequestParam("id") Long id){
        return configPaymenntService.approveOrder(id);
    }
    @GetMapping("order/approve/info")
    public ResponseEntity<MessageResponse> GetApprovedOrder(){
        return configPaymenntService.GetOrder();
    }

    @GetMapping("order/business/paid")
    public ResponseEntity<MessageResponse> GetTourBookingItemIdsWithStatusByBusiness(@RequestParam("business") String username){
       MessageResponse messageResponse = new MessageResponse();
       messageResponse.setMessage("Success");
       messageResponse.setResponseCode("200");
       List<Long> tourBookingItemIds = configPaymenntService.GetTourBookingItemIdsWithStatusByBusiness(username,"process");
       messageResponse.setData(tourBookingItemIds);

        return ResponseEntity.ok(messageResponse);
    }
}
