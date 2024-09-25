package com.trithuc.service;

import com.trithuc.model.User;
import com.trithuc.request.OrderRequest;

public interface MailService {
    void sendDetailReceipt(OrderRequest orderRequest, User user);
}
