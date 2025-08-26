package com.BeatUp.BackEnd.Chat.ChatMessage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(min = 1, max = 200, message = "메시지는 1~2000자여야 합니다")
    private String content;

    public ChatMessageRequest(){}

    public ChatMessageRequest(String content){
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
