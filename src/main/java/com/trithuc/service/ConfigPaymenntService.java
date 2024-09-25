package com.trithuc.service;

import com.trithuc.dto.VnpPaymentDTO;
import com.trithuc.request.AddToCartRequest;
import com.trithuc.request.OrderRequest;
import com.trithuc.response.MessageResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ConfigPaymenntService {

    @Cacheable(value = "TxRef", key = "#username")
    ResponseEntity<MessageResponse> createUrlPayment(String username, Double totalPrice) throws UnsupportedEncodingException;

    ResponseEntity<?> handlePaymentResult(VnpPaymentDTO requestData, String token);

    ResponseEntity<MessageResponse> addToCart(AddToCartRequest addToCartRequest, String token);

    ResponseEntity<MessageResponse> getInfoCart(String token);

    ResponseEntity<MessageResponse> incrementQuantityCart(Long cartItemId);

    ResponseEntity<MessageResponse> DecrementQuantityCart(Long cartItemId);

    ResponseEntity<MessageResponse> deleteItem(Long cartItemId);

    @Transactional
    ResponseEntity<MessageResponse> sendReceipt(OrderRequest orderRequest, String token);

    ResponseEntity<MessageResponse> oderHistory(String token);

    ResponseEntity<MessageResponse> approveOrder(Long id);

    ResponseEntity<MessageResponse> GetOrder();

    List<Long> GetTourBookingItemIdsWithStatusByBusiness(String username, String status);
}
