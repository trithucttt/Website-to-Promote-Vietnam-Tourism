package com.trithuc.service.impl;

import com.trithuc.dto.*;
import com.trithuc.model.*;
import com.trithuc.repository.*;
import com.trithuc.request.AddToCartRequest;
import com.trithuc.request.OrderRequest;
import com.trithuc.request.ZaloPayRequest;
import com.trithuc.response.*;
import com.trithuc.service.ConfigPaymenntService;
import com.trithuc.service.MailService;
import com.trithuc.service.UserService;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ConfigPaymentServiceImpl implements ConfigPaymenntService {

    private final StringRedisTemplate redisTemplate;
    private static final String tmnCode = "ALK2F9GM";
    private static final String version = "2.1.0";
    private static final String command = "pay";
    private static final String orderType = "other";
    private static final String ipAddress = "127.0.0.1";
    private static final String currCode = "VND";
    private static final String paymentDomain = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String location = "vn";
    private static final String returnUrl = "http://localhost:3000/payment";
    private static final String CHARACTERS = "0123456789";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemsRepository cartItemsRepository;
    @Autowired
    private YourBookingRepository yourBookingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private WardRepository wardRepository;
    @Autowired
    private tourbooking_itemRepository orderItemsRepository;

    @Autowired
    private tourbooking_itemRepository tourBookingItemRepository;

    public ConfigPaymentServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String initTxRef() {

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code + createDate;
    }

    private String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }
// Payment with VNPay
    @Override
    @Cacheable(value = "TxRef", key = "#username")
    public ResponseEntity<MessageResponse> createUrlPayment(String username, Double totalPrice) throws UnsupportedEncodingException {

        int amount = totalPrice.intValue() * 100;
        String oderInfo = "Booking tour";
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("vnp_Version", version);
        urlParams.put("vnp_Command", command);
        urlParams.put("vnp_TmnCode", tmnCode);
        urlParams.put("vnp_Amount", String.valueOf(amount));
        urlParams.put("vnp_CurrCode", currCode);

        // add code TxnRef to redis
        String codeTxRef = initTxRef();
        urlParams.put("vnp_TxnRef", codeTxRef);
        redisTemplate.opsForValue().set(username, codeTxRef, 20, TimeUnit.MINUTES);

        urlParams.put("vnp_OrderInfo", oderInfo);
        urlParams.put("vnp_OrderType", orderType);
        urlParams.put("vnp_Locale", location);
        urlParams.put("vnp_ReturnUrl", returnUrl);
        urlParams.put("vnp_IpAddr", ipAddress);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        urlParams.put("vnp_CreateDate", createDate);
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        urlParams.put("vnp_ExpireDate", expireDate);

        List<String> fieldNames = new ArrayList<>(urlParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hasData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = urlParams.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hasData.append(fieldName);
                hasData.append('=');
                hasData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hasData.append('&');
                }

            }
        }
        String queryUrl = query.toString();
        String secureHas = hmacSHA512("FOYAUJQ53LRUY186CI1I9SVR0VXT0UYW", hasData.toString());
        queryUrl += "&vnp_SecureHash=" + secureHas;
        String paymentUrl = paymentDomain + "?" + queryUrl;
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("success");
        messageResponse.setResponseCode("200");
        messageResponse.setData(paymentUrl);
        return ResponseEntity.ok(messageResponse);
    }

    @Override
    public ResponseEntity<?> handlePaymentResult(VnpPaymentDTO requestData, String token) {
        String username = userService.Authentication(token);
        StringBuilder hashData = new StringBuilder();
        String vnp_SecureHash = hmacSHA512("WMSXYGCIRXCTNIBMUMWGDCFBVZMNZEPW", hashData.toString());
        String vnpSecureHash = requestData.getVnp_SecureHash();
        String orderInfo = requestData.getVnp_OrderInfo();
        String vnpResponseCode = requestData.getVnp_ResponseCode();
        String vnpTxnRefResponse = requestData.getVnp_TxnRef();
        String vnpTmnCode = requestData.getVnp_TmnCode();
        String vnpBankTranNo = requestData.getVnp_BankTranNo();
        String vpnBankCode = requestData.getVnp_BankCode();
        String vnp_PayDate = requestData.getVnp_PayDate();
        String totalPriceString = requestData.getVnp_Amount();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // Chuyển đổi chuỗi sang LocalDateTime
        LocalDateTime payDayFormat = LocalDateTime.parse(vnp_PayDate, formatter);

        //  xử lý múi giờ,  chuyển đổi LocalDateTime sang ZonedDateTime
        ZoneId zoneId = ZoneId.of("Etc/GMT-7");
        ZonedDateTime payDayZonedDateTime = payDayFormat.atZone(zoneId);

//        System.out.println("LocalDateTime: " + localDateTime);
//        System.out.println("ZonedDateTime: " + zonedDateTime);

        if (vnpResponseCode.equals("00")) {
            if (!vnpTmnCode.equals(tmnCode)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_TmnCode incorrect");
            }
            if (vnpBankTranNo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_BankTranNo is null");
            }
            if (vpnBankCode.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_BankCode is null");
            }
            // CHECK TxnRef
            String codeTxnRefFromRedis = redisTemplate.opsForValue().get(username);
            if (vnpTxnRefResponse != null && vnpTxnRefResponse.equals(codeTxnRefFromRedis)) {
                PaymentInfoResponse paymentInfoResponse = new PaymentInfoResponse();
                paymentInfoResponse.setOrderInfo(orderInfo);
                paymentInfoResponse.setPaymentCode(vnpTxnRefResponse);
                paymentInfoResponse.setBankTranNo(vnpBankTranNo);
                paymentInfoResponse.setPayDay(payDayFormat);
                Double totalPrice = Double.parseDouble(totalPriceString);
                paymentInfoResponse.setTotalPrice(totalPrice / 100);
                paymentInfoResponse.setMessage("Valid successfully");
                paymentInfoResponse.setResCode("200");
//                redisTemplate.delete(username);
                return ResponseEntity.status(HttpStatus.OK).body(paymentInfoResponse);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("vnp_TxnRef code is incorrect");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment Failed");
        }
    }


//     Payment with ZaloPay
    @Override
    public MessageResponse createOrder(ZaloPayRequest request) throws Exception {
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + UUID.randomUUID();
        long appTime = System.currentTimeMillis();
        Map<String, String> embedData = Map.of("redirecturl", returnUrl);

        Map[] item = {
                new HashMap(){{
                    put("itemid", "knb");
                    put("itemname", "kim nguyen bao");
                    put("itemprice", 198400);
                    put("itemquantity", 1);
                }}
        };
        Map<String, Object> order = new HashMap<>() {{
            put("appid", "554");
            put("apptransid", appTransId);
            put("apptime", appTime);
            put("appuser", request.getAppUser());
            put("amount", request.getAmount());
            put("description", request.getDescription());
            put("bankcode", "zalopayapp");
//            put("bankcode", "JCB");
            put("item", new JSONObject(item).toString());
            put("embeddata", new JSONObject(embedData).toString());
        }};

        // Tính MAC
        String data = order.get("appid") + "|" + order.get("apptransid") + "|" + order.get("appuser") + "|" +
                order.get("amount") + "|" + order.get("apptime") + "|" + order.get("embeddata") + "|" + order.get("item");
        order.put("mac", calculateMac("8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn", data));

        // Gửi HTTP POST
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://sandbox.zalopay.com.vn/v001/tpe/createorder");
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, Object> e : order.entrySet()) {
            params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
        }
        post.setEntity(new UrlEncodedFormEntity(params));
        CloseableHttpResponse response = client.execute(post);

        // Đọc phản hồi
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        JSONObject jsonResponse = new JSONObject(result.toString());
//        System.out.println("Response: " + jsonResponse);
//        System.out.println("Order Data: " + order);
//        System.out.println("Data for MAC: " + data);
//        System.out.println("MAC: " + calculateMac("8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn", data));
//        System.out.println("Item JSON: " + new JSONObject(List.of(Map.of(
//                "itemid", "knb",
//                "itemname", "kim nguyen bao",
//                "itemprice", request.getAmount(),
//                "itemquantity", 1
//        ))).toString());

        return new MessageResponse("200","success",jsonResponse.toMap());
    }

    private String calculateMac(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }


    @Override
    public ResponseEntity<MessageResponse> addToCart(AddToCartRequest addToCartRequest, String token) {
        MessageResponse messageResponse = new MessageResponse();
        String username = userService.Authentication(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User not found");
            return ResponseEntity.ok(messageResponse);
        }
        Optional<Tour> tour = tourRepository.findById(addToCartRequest.getTourId());
//        Optional<PostTour> postTourOptional = postTourRepository.findByPostIdAndTourId(addToCartRequest.getPostId(), addToCartRequest.getTourId());
        if (tour.isEmpty() || tour.get().getQuantity() <= 0) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("Tour not found or out off stock");
            return ResponseEntity.ok(messageResponse);
        }
        CartItems existingItems = cartItemsRepository.findByTourIdAndUserId(tour.get().getId(), userOptional.get().getId());
        if (existingItems != null) {
            existingItems.setQuantity(existingItems.getQuantity() + addToCartRequest.getQuantity());
            cartItemsRepository.save(existingItems);
            messageResponse.setMessage("Add To Cart Successfully!!");
            messageResponse.setResponseCode("200");
            return ResponseEntity.ok(messageResponse);
        } else {
            CartItems cartItems = new CartItems();
            cartItems.setQuantity(addToCartRequest.getQuantity());
            cartItems.setUser(userOptional.get());
            cartItems.setTour(tour.get());
            cartItemsRepository.save(cartItems);
            messageResponse.setMessage("Add To Cart Successfully");
            messageResponse.setResponseCode("200");
            return ResponseEntity.ok(messageResponse);
        }
    }

    @Override
    public ResponseEntity<MessageResponse> getInfoCart(String token) {
        MessageResponse messageResponse = new MessageResponse();
        String username = userService.Authentication(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User Not Found");
            return ResponseEntity.ok(messageResponse);
        }
        List<CartItems> cartItemsList = cartItemsRepository.findByUserId(userOptional.get().getId());
        if (cartItemsList.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("Cart in null please Add before view cart");
            return ResponseEntity.ok(messageResponse);
        }
        List<InfoCart> infoCartList = cartItemsList.stream().map(cartItem -> {
            InfoCart infoCart = new InfoCart();
            infoCart.setCartItemId(cartItem.getId());
            infoCart.setQuantity(cartItem.getQuantity());

            Tour tour = cartItem.getTour();
            if (tour == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found for cart item id: " + cartItem.getId());
            }
            ListTourInCart tourInCart = new ListTourInCart();
            tourInCart.setPostTourId(tour.getId());
            tourInCart.setPrice(tour.getPrice());
            tourInCart.setTourName(tour.getTitle());
            List<String> imageUrl = tour.getImages().stream()
                    .map(Image::getImageUrl) // Tạo đối tượng ImageDto
                    .toList();
            tourInCart.setImageTourUrl(imageUrl);
            tourInCart.setStartTime(tour.getStartTimeTour());
            tourInCart.setEndTime(tour.getEndTimeTour());
            tourInCart.setFullNameSupplier(tour.getManager().getLastname() + " " + tour.getManager().getFirstname());
            infoCart.setListTourInCart(tourInCart);
            return infoCart;
        }).toList();
        messageResponse.setMessage("success");
        messageResponse.setResponseCode("200");
        messageResponse.setData(infoCartList);
        return ResponseEntity.ok(messageResponse);
    }

    @Override
    public ResponseEntity<MessageResponse> incrementQuantityCart(Long cartItemId) {
        MessageResponse messageResponse = new MessageResponse();
        Optional<CartItems> cartItemsOptional = cartItemsRepository.findById(cartItemId);
        if (cartItemsOptional.isPresent()) {
            CartItems cartItems = cartItemsOptional.get();
            cartItems.setQuantity(cartItems.getQuantity() + 1);
            cartItemsRepository.save(cartItems);
            messageResponse.setMessage("Tăng số lượng chuyến đi thành công");
            messageResponse.setResponseCode("200");
            return ResponseEntity.ok(messageResponse);
        }
        messageResponse.setMessage("Không tìm thấy chuyến đi nào như vậy trong giỏ hàng");
        messageResponse.setResponseCode("404");
        return ResponseEntity.ok(messageResponse);
    }

    @Override
    public ResponseEntity<MessageResponse> DecrementQuantityCart(Long cartItemId) {
        MessageResponse messageResponse = new MessageResponse();
        Optional<CartItems> cartItemsOptional = cartItemsRepository.findById(cartItemId);
        if (cartItemsOptional.isPresent()) {
            CartItems cartItems = cartItemsOptional.get();
            if (cartItems.getQuantity() > 1) {
                cartItems.setQuantity(cartItems.getQuantity() - 1);
                cartItemsRepository.save(cartItems);
                messageResponse.setMessage("Giảm số lượng thành công");
                messageResponse.setResponseCode("200");
                return ResponseEntity.ok(messageResponse);
            } else {
                cartItemsRepository.deleteById(cartItemId);
                messageResponse.setMessage("Đã xóa chuyến đi khỏi giỏ hàng!");
                messageResponse.setResponseCode("200");
                return ResponseEntity.ok(messageResponse);
            }

        } else {
            messageResponse.setMessage("Không tìm thấy chuyến đi nào như vậy trong giỏ hàng");
            messageResponse.setResponseCode("404");
            return ResponseEntity.ok(messageResponse);
        }
    }

    @Override
    public ResponseEntity<MessageResponse> deleteItem(Long cartItemId) {
        MessageResponse messageResponse = new MessageResponse();
        Optional<CartItems> cartItems = cartItemsRepository.findById(cartItemId);
        if (cartItems.isEmpty()){
            messageResponse.setMessage("Xóa chuyến đi thất bại!");
            messageResponse.setResponseCode("400");
            return ResponseEntity.ok(messageResponse);
        }
        cartItemsRepository.deleteById(cartItemId);
        messageResponse.setMessage("Đã xóa chuyến đi khỏi giỏ hàng");
        messageResponse.setResponseCode("200");
        return ResponseEntity.ok(messageResponse);
    }

    @Transactional
    public String checkOut(OrderRequest orderRequest, User user) {
        MessageResponse messageResponse = new MessageResponse();
        List<CartItems> cartItemsList = cartItemsRepository.findAllById(orderRequest.getCartItemId());
        if (cartItemsList.isEmpty()) {
            return "404";
        }
        YourBooking yourBooking = new YourBooking();
//        yourBooking.setStatus("process");
        yourBooking.setUser(user);
        List<tourbooking_item> itemList = new ArrayList<>();
        for (CartItems cartItems : cartItemsList) {
            tourbooking_item item = new tourbooking_item();
            item.setQuantity(cartItems.getQuantity());
            item.setStatus("PROCESS");
            item.setPrice(cartItems.getTour().getPrice());
            item.setYourbooking(yourBooking);

            item.setTour(cartItems.getTour());
            Tour tour = cartItems.getTour();
            int quantity = tour.getQuantity() - cartItems.getQuantity();
            if (quantity < 0) {
                return "300";
            }
            tour.setQuantity(quantity);
            tourRepository.save(tour);
            itemList.add(item);
        }
        yourBooking.setTourbooking_items(itemList);
        YourBooking saveYourBooking = yourBookingRepository.save(yourBooking);
        Payment payment = new Payment();
        payment.setCode(orderRequest.getBankCode());
        payment.setTotal(orderRequest.getTotalPrice());
        payment.setDate(orderRequest.getBankDate());
        payment.setYourBooking(saveYourBooking);
        paymentRepository.save(payment);

        return "200";
    }

    @Override
    @Transactional
    public ResponseEntity<MessageResponse> sendReceipt(OrderRequest orderRequest, String token) {
        String username = userService.Authentication(token);
        MessageResponse messageResponse = new MessageResponse();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User not found");
            return ResponseEntity.ok(messageResponse);
        }
        String ResCode = checkOut(orderRequest, userOptional.get());
        System.out.println(ResCode);
        if (ResCode == "200") {
            //send receipt;
            mailService.sendDetailReceipt(orderRequest, userOptional.get());
            List<CartItems> cartItemsList = cartItemsRepository.findAllById(orderRequest.getCartItemId());
            cartItemsRepository.deleteAll(cartItemsList);
            messageResponse.setResponseCode("200");
            messageResponse.setMessage("Xử lý đơn hàng của bạn thành công");
            return ResponseEntity.ok(messageResponse);
        } else {
            messageResponse.setResponseCode("400");
            messageResponse.setMessage("Quá trình thanh toán thất bại");
            return ResponseEntity.ok(messageResponse);
        }

    }


    @Override
    public ResponseEntity<MessageResponse> oderHistory(String token) {
        MessageResponse messageResponse = new MessageResponse();
        String username = userService.Authentication(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User not found");
            return ResponseEntity.ok(messageResponse);
        }
        User user = userOptional.get();
        List<YourBooking> bookings = yourBookingRepository.findByUser(user);
        if (bookings.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("Order in null please book tour before view order");
            return ResponseEntity.ok(messageResponse);
        }
        List<HistoryOrderResponse> orderResponseList = bookings.stream().map(booking -> {
            HistoryOrderResponse orderResponse = new HistoryOrderResponse();
            orderResponse.setBookingId(booking.getId());
            orderResponse.setBankCode(booking.getPayment().getCode());
            orderResponse.setTotalBooking(booking.getPayment().getTotal());
            orderResponse.setBookingDate(booking.getPayment().getDate());
            List<tourbooking_item> orderItemList = orderItemsRepository.findByYourbooking(booking);
            List<InfoTourItems> infoTourItemsList = orderItemList.stream().map(item -> {
                InfoTourItems infoTourItem = new InfoTourItems();
                infoTourItem.setItemId(item.getId());
                infoTourItem.setStatus(item.getStatus());
                infoTourItem.setDiscountItem(item.getTour().getDiscount());
                infoTourItem.setQuantityItem(item.getQuantity());
                infoTourItem.setPrice(item.getPrice());
                infoTourItem.setItemName(item.getTour().getTitle());
                infoTourItem.setStartTimeItem(item.getTour().getStartTimeTour());
                infoTourItem.setEndTimeItem(item.getTour().getEndTimeTour());
                return infoTourItem;
            }).toList();
            orderResponse.setInfoTourItems(infoTourItemsList);
            return orderResponse;
        }).toList();
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("successfully");
        messageResponse.setData(orderResponseList);
        return ResponseEntity.ok(messageResponse);
    }

    /** Approve */
    @Override
    public ResponseEntity<MessageResponse> approveOrder(Long id) {
        MessageResponse messageResponse = new MessageResponse();
        Optional<YourBooking> yourBooking = yourBookingRepository.findById(id);
        if (yourBooking.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("Order not Found");
            return ResponseEntity.ok(messageResponse);
        }
//        yourBooking.get().setStatus("approved");
        yourBookingRepository.save(yourBooking.get());
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("Approve Order Successfully");
        return ResponseEntity.ok(messageResponse);
    }

    @Override
    public ResponseEntity<MessageResponse> GetOrder() {
        MessageResponse messageResponse = new MessageResponse();
        List<YourBooking> yourBooking = yourBookingRepository.findAll();
        if (yourBooking.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("Order not Found");
            return ResponseEntity.ok(messageResponse);
        }

        List<ApproveOrder> approveOrderList = yourBooking.stream().map(item -> {
            ApproveOrder approveOrder = new ApproveOrder();
            approveOrder.setId(item.getId());
            approveOrder.setUserOrder(item.getUser().getFirstname());
//            approveOrder.setStatus(item.getStatus());
            approveOrder.setOrderDate(item.getPayment().getDate());
            approveOrder.setBankCodeOrder(item.getPayment().getCode());
            approveOrder.setTotalTourOrder(item.getTourbooking_items().stream().count());
            return approveOrder;
        }).toList();
        messageResponse.setData(approveOrderList);
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("Approve Order Successfully");
        return ResponseEntity.ok(messageResponse);
    }

    @Override
    public List<Long> GetTourBookingItemIdsWithStatusByBusiness(String username, String status) {
        return orderItemsRepository.findTourBookingItemIdWithStatusByUsernameBusiness(username, status);
    }


    @Override
    public List<TourBookingItemDTO> getBookingsByManagerId(Long managerId) {
        List<Tour> tours = tourRepository.findByManagerId(managerId); // Lấy danh sách tour theo managerId
        List<tourbooking_item> bookings = new ArrayList<>();

        for (Tour tour : tours) {
            bookings.addAll(tourBookingItemRepository.findByTourId(tour.getId()));
        }

        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void approveBooking(Long bookingId) {
        tourbooking_item booking = tourBookingItemRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
        booking.setStatus("CONFIRM");
        tourBookingItemRepository.save(booking);
    }

    @Override
    public void rejectBooking(Long bookingId) {
        tourbooking_item booking = tourBookingItemRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + bookingId));
        booking.setStatus("REJECT");
        tourBookingItemRepository.save(booking);
    }

    private TourBookingItemDTO convertToDTO(tourbooking_item booking) {
        return new TourBookingItemDTO(
                booking.getId(),
                booking.getTour().getTitle(),
                booking.getYourbooking().getUser().getFirstname()+ " " + booking.getYourbooking().getUser().getLastname(), // lấy tên khách hàng
                booking.getQuantity(),
                booking.getPrice(),
                booking.getStatus()
        );
    }


    @Override
    public List<TourBookingStatsDTO> getTotalBookedTours(Long businessId) {
        //  Lấy danh sách tour của doanh nghiệp
        List<Tour> tours = tourRepository.findByManagerId(businessId); // Giả định rằng `businessId` tương đương với `managerId`.

      List<TourBookingStatsDTO> bookingStatsDTO = new ArrayList<>();

      for (Tour tour : tours){
          List<tourbooking_item> bookingItems = tourBookingItemRepository.findByTourId(tour.getId());
          long bookedQuantity = bookingItems.stream().mapToLong(tourbooking_item::getQuantity).sum();
          TourBookingStatsDTO statsDTO = new TourBookingStatsDTO();
          statsDTO.setId(tour.getId());
          statsDTO.setTitle(tour.getTitle());
          statsDTO.setPrice(tour.getPrice());
          statsDTO.setQuantity(tour.getQuantity());
          statsDTO.setBookedQuantity(bookedQuantity);

          bookingStatsDTO.add(statsDTO);
      }
      return bookingStatsDTO;
    }

    @Override
    public Double getTotalRevenueTours(Long businessId) {
        List<Tour> tours = tourRepository.findByManagerId(businessId);
        // Bước 2: Khởi tạo tổng doanh thu
        double totalRevenue = 0.0;

        // Bước 3: Duyệt qua danh sách tour
        for (Tour tour : tours) {
            // Lấy danh sách booking items cho tour
            List<tourbooking_item> bookings = tourBookingItemRepository.findByTourId(tour.getId());

            // Bước 4: Tính doanh thu cho từng booking item
            for (tourbooking_item booking : bookings) {
                totalRevenue += booking.getPrice() * booking.getQuantity(); // Giá * Số lượng
            }
        }

        return  totalRevenue;

    }

    @Override
    public Long getUniqueCustomers(Long businessId) {
        return yourBookingRepository.countUniqueCustomersByBusiness(businessId);
    }
    @Override
    public Long getCompletedTours(Long businessId) {
        return yourBookingRepository.countCompletedToursByBusiness(businessId);
    }
    @Override
    public Long getCanceledTours(Long businessId) {
        return yourBookingRepository.countCanceledToursByBusiness(businessId);
    }

    @Override
    public List<RevenueStatisticsDTO> getRevenueStatistics(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE) : null;

        return tourBookingItemRepository.findRevenueStatistics(start, end)
                .stream()
                .map(objects -> new RevenueStatisticsDTO(
                        (String) objects[0],
                        (Long) objects[1],
                        (Double) objects[2]
                ))
                .collect(Collectors.toList());
    }
}
