package com.trithuc.dto;


import com.trithuc.model.ChatMessage;
import com.trithuc.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {

    private Long chatRoomId;
    private List<UserDTO> userDTOS;

}
