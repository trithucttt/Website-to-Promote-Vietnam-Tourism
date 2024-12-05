package com.trithuc.service.impl;

import com.trithuc.dto.DestinationSummary;
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
        sb.append("<style>")
                .append("body {")
                .append("font-family: Arial, sans-serif;")
                .append("}")
                .append(".card {")
                .append("box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);")
                .append("margin: 8px;")
                .append("padding: 16px;")
                .append("background-color: #f9f9f9;")
                .append("border-radius: 5px;")
                .append("}")
                .append(".container {")
                .append("padding: 2px 16px;")
                .append("}")
                .append("</style>")
                .append("<h1>Dear Mr/Mrs ").append(user.getLastname()).append(",</h1>")
                .append("<p>Thank you for your booking! Here is your booking information:</p>");

        orderSummary.getItems().forEach(item -> {
            sb.append("<div class='card'>")
                    .append("<h2>").append(item.getItemName()).append("</h2>")
                    .append("<p>Quantity: ").append(item.getQuantity()).append("</p>")
                    .append("<p>Price: $").append(String.format("%.2f", item.getPrice())).append("</p>")
                    .append("<p>Destinations:</p>")
                    .append("<ul>");

            sb.append("</ul>")
                    .append("</div>");
        });

        sb.append("<p>Total Quantity: ").append(orderSummary.getTotalQuantity()).append("</p>")
                .append("<p>Total Amount: $").append(String.format("%.2f", orderSummary.getTotalAmount())).append("</p>")
                .append("<p>Regards,</p>")
                .append("<p>Wake Travel</p>");

        return sb.toString();
    }



    public OrderSummary getOrderSummary(OrderRequest orderRequest) {
        List<CartItems> cartItems = cartItemsRepository.findAllById(orderRequest.getCartItemId());
        System.out.println("cartItems" + orderRequest.getCartItemId());
        if (cartItems.isEmpty()) {
            return null;
        }
        List<ItemSummary> itemSummaries = cartItems.stream().map(cartItem -> {

            List<DestinationSummary> destinations = cartItem.getTour().getDestination().stream()
                    .map(destination -> new DestinationSummary(destination.getName(), destination.getAddress()))
                    .collect(Collectors.toList());

            return new ItemSummary(cartItem.getTour().getTitle(),
                    cartItem.getQuantity(),
                    cartItem.getTour().getPrice(),
                    destinations);
        }).collect(Collectors.toList());

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


    private void sendHtmlEmail(String to, String subject, String body) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates HTML content
            javaMailSender.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }


    @Override
    public void sendOtpEmail(String email, String otpCode) {
        String htmlContent = "<html><body style=\"font-family: Arial, sans-serif;\">"
                + "<h2>Confirmation Code</h2>"
                + "<p>Your one-time confirmation code is: <strong>" + otpCode + "</strong>.</p>"
                + "<p>This code will expire in 15 minutes.</p>"
                + "</body></html>";

        sendHtmlEmail(email, "OTP Confirmation Code", htmlContent);
    }

    @Override
    public void sendNewPassword(String email, String newPassword) {
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>New Password Issued</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "h2 { color: #333; }" +
                "p { color: #555; }" +
                "strong { color: #007bff; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<h2>New password is issued</h2>" +
                "<p>Your new Password: <strong>" + newPassword + "</strong>.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(email, "New Password", htmlContent);
    }

}
