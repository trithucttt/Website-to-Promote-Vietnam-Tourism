package com.trithuc.service;


import com.trithuc.model.User;
import com.trithuc.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void testFindUserById() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("trithuc");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        User result = userService.findUserById(1L);
        assertEquals("trithuc", result.getUsername());
    }


}

