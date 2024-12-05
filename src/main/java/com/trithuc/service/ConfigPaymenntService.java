package com.trithuc.service;

import com.trithuc.dto.RevenueStatisticsDTO;
import com.trithuc.dto.TourBookingItemDTO;
import com.trithuc.dto.TourBookingStatsDTO;
import com.trithuc.dto.VnpPaymentDTO;
import com.trithuc.request.AddToCartRequest;
import com.trithuc.request.OrderRequest;
import com.trithuc.request.ZaloPayRequest;
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

    MessageResponse createOrder(ZaloPayRequest request) throws Exception;

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

    List<TourBookingItemDTO> getBookingsByManagerId(Long managerId);

    void approveBooking(Long bookingId);

    void rejectBooking(Long bookingId);

    Long getCanceledTours(Long businessId);

    Long getCompletedTours(Long businessId);

    Long getUniqueCustomers(Long businessId);

    List<TourBookingStatsDTO> getTotalBookedTours(Long businessId);

    Double getTotalRevenueTours(Long businessId);

    List<RevenueStatisticsDTO> getRevenueStatistics(String startDate, String endDate);
}
