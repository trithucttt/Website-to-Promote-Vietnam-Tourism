package com.trithuc.service.impl;

import com.trithuc.dto.ItemSummary;
import com.trithuc.dto.OrderSummary;
import com.trithuc.model.CartItems;
import com.trithuc.model.User;
import com.trithuc.repository.CartItemsRepository;
import com.trithuc.request.OrderRequest;
import com.trithuc.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private CartItemsRepository cartItemsRepository;
    @Value("hoanglong255l@gmail.com")
    private String mailFrom;


    @Override
    public void sendDetailReceipt(OrderRequest orderRequest, User user) {

        OrderSummary orderSummary = getOrderSummary(orderRequest);
        if (orderSummary == null) {
            throw new RuntimeException("OrderSummary not found");
        }
        String emailContent = buildEmailContent(user, orderSummary);
        sendHtmlMail(user.getEmail(), emailContent);
    }

    private void sendHtmlMail(String to, String content) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("Your Order Information");
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailContent(User user, OrderSummary orderSummary) {
        StringBuilder sb = new StringBuilder();
//        sb.append("<h1>Dear ").append(user.getFullName()).append(",</h1>")
        sb.append("<style>")
                .append("table {")
                .append("font-family: Arial, sans-serif;")
                .append("border-collapse: collapse;")
                .append("width: 100%;")
                .append("}")
                .append("table th, table td {")
                .append("border: 1px solid #dddddd;")
                .append("text-align: left;")
                .append("padding: 8px;")
                .append("}")
                .append("table th {")
                .append("background-color: #f2f2f2;")
                .append("}")
                .append("</style>")
                .append("<h1>Dear Mr/Mrs").append(user.getLastname()).append(",</h1>")
                .append("<p>Thank you for your booking! Here is your booking information:</p>")
                .append("<table>")
                .append("<tr><th>Item</th><th>Quantity</th><th>Price</th></tr>");

        orderSummary.getItems().forEach(item ->
                sb.append("<tr>")
                        .append("<td>").append(item.getItemName()).append("</td>")
                        .append("<td>").append(item.getQuantity()).append("</td>")
                        .append("<td>").append(item.getPrice()).append("</td>")
                        .append("</tr>")
        );

        sb.append("</table>")
                .append("<p>Total Quantity: ").append(orderSummary.getTotalQuantity()).append("</p>")
                .append("<p>Total Amount: ").append(orderSummary.getTotalAmount()).append("</p>")
                .append("<p>Regards,</p>")
                .append("<p>Travel World</p>");

        return sb.toString();
    }

    public OrderSummary getOrderSummary(OrderRequest orderRequest) {
        List<CartItems> cartItems = cartItemsRepository.findAllById(orderRequest.getCartItemId());
        System.out.println("cartItems" + orderRequest.getCartItemId());
        if (cartItems.isEmpty()) {
            return null;
        }
        List<ItemSummary> itemSummaries = cartItems.stream().map(cartItem ->
                new ItemSummary(cartItem.getPostTour().getTour().getTitle(),
                        cartItem.getQuantity(),
                        cartItem.getPostTour().getTour().getPrice())
        ).collect(Collectors.toList());

        Double totalAmount = itemSummaries.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        int totalQuantity = itemSummaries.stream()
                .mapToInt(ItemSummary::getQuantity)
                .sum();
        System.out.println("totalAmount" + totalAmount);
        System.out.println("totalQuantity" + totalQuantity);
        System.out.println("itemSummaries" + itemSummaries);
        return new OrderSummary(itemSummaries, totalAmount, totalQuantity);
    }

}
