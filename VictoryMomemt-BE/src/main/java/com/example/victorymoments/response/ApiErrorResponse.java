package com.example.victorymoments.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Thông tin lỗi trả về từ API"
)
public class ApiErrorResponse {

    @Schema(
            description = "Mã lỗi định danh loại lỗi",
            example = "USER_NOT_FOUND"
    )
    private String errorCode;

    @Schema(
            description = "Thông báo mô tả lỗi",
            example = "User with provided ID does not exist"
    )
    private String message;

    @Schema(
            description = "Danh sách các lỗi chi tiết theo trường (nếu có)",
            example = "{\"email\":\"Email format is invalid\"}"
    )
    private Map<String, String> errors;
}
