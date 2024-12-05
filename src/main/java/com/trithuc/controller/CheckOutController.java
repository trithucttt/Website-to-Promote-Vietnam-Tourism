package com.trithuc.controller;

import com.trithuc.dto.RevenueStatisticsDTO;
import com.trithuc.dto.TourBookingItemDTO;
import com.trithuc.dto.TourBookingStatsDTO;
import com.trithuc.dto.VnpPaymentDTO;
import com.trithuc.repository.PaymentRepository;
import com.trithuc.repository.YourBookingRepository;
import com.trithuc.request.AddToCartRequest;
import com.trithuc.request.OrderRequest;
import com.trithuc.request.ZaloPayRequest;
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


    @PostMapping( "cart/add")
    public ResponseEntity<MessageResponse> addToCart(@RequestHeader(name = "Authorization") String token,
                                                     @RequestBody AddToCartRequest cartItem){
        return configPaymenntService.addToCart(cartItem,token);
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
        return configPaymenntService.deleteItem(cartItemId);
    }

    @GetMapping("cart/payment/createUrl")
    public ResponseEntity<MessageResponse> getUrlPaymentWithVNPay(@RequestParam("username") String username,
                                                         @RequestParam("totalPrice") Double totalPrice) throws UnsupportedEncodingException {
        return configPaymenntService.createUrlPayment(username,totalPrice);
    }
    @PostMapping("cart/payment/process")
    public ResponseEntity<?> processPaymentResultWithVNPay(@RequestHeader(name = "Authorization") String token,
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

    @Autowired
    private YourBookingRepository yourBookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("business/revenue/{businessId}")
    public ResponseEntity<?> getRevenue(@PathVariable Long businessId) {
        Double totalRevenue = paymentRepository.calculateTotalRevenueByBusiness(businessId);
        Long totalToursBooked = yourBookingRepository.countToursBookedByBusiness(businessId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalToursBooked", totalToursBooked);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("manager/{managerId}/bookings")
    public ResponseEntity<List<TourBookingItemDTO>> getBookingsByManagerId(@PathVariable Long managerId) {
        List<TourBookingItemDTO> bookings = configPaymenntService.getBookingsByManagerId(managerId);
        return ResponseEntity.ok(bookings);
    }

    // API duyệt đơn hàng
    @PostMapping("bookings/{bookingId}/approve")
    public ResponseEntity<Void> approveBooking(@PathVariable Long bookingId) {
        configPaymenntService.approveBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    // API từ chối đơn hàng
    @PostMapping("bookings/{bookingId}/reject")
    public ResponseEntity<Void> rejectBooking(@PathVariable Long bookingId) {
        configPaymenntService.rejectBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    // API lấy tổng số tour đã được đặt
    @GetMapping("bookings/total-booked-tours")
    public List<TourBookingStatsDTO> getTotalBookedTours(@RequestParam Long businessId) {
        return configPaymenntService.getTotalBookedTours(businessId);
    }

    @GetMapping("bookings/total-revenue-tours")
    public Double getTotalRevenueTours(@RequestParam Long businessId) {
        return configPaymenntService.getTotalRevenueTours(businessId);
    }

    // API lấy số lượng khách hàng độc đáo
    @GetMapping("bookings/unique-customers")
    public Long getUniqueCustomers(@RequestParam Long businessId) {
        return configPaymenntService.getUniqueCustomers(businessId);
    }

    // API lấy số lượng tour đã hoàn thành
    @GetMapping("bookings/completed-tours")
    public Long getCompletedTours(@RequestParam Long businessId) {
        return configPaymenntService.getCompletedTours(businessId);
    }

    // API lấy số lượng tour đã bị hủy
    @GetMapping("bookings/canceled-tours")
    public Long getCanceledTours(@RequestParam Long businessId) {
        return configPaymenntService.getCanceledTours(businessId);
    }
    // API lấy doanh thu
    @GetMapping("/revenue-statistics")
    public List<RevenueStatisticsDTO> getRevenueStatistics(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        return configPaymenntService.getRevenueStatistics(startDate, endDate);
    }

    // test create url payment with zalopay
    @PostMapping("payment/zalopay/create-order")
    public ResponseEntity<MessageResponse> createOrder(@RequestBody ZaloPayRequest request) {
        try {
            return ResponseEntity.ok(configPaymenntService.createOrder(request));
        } catch (Exception e) {
//            e.printStackTrace();
            return ResponseEntity.ok(new MessageResponse("400","Error creating order: " + e.getMessage(),null));
        }
    }



}
