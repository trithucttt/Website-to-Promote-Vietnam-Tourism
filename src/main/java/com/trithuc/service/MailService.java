package com.trithuc.service;

import com.trithuc.model.User;
import com.trithuc.request.OrderRequest;

public interface MailService {
    void sendDetailReceipt(OrderRequest orderRequest, User user);

    void sendOtpEmail(String email, String otpCode);

    void sendNewPassword(String email, String newPassword);
}
