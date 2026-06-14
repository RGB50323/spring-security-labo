package com.server.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDto {

    @NotBlank(message = "La contraseña actual no puede estar vacía")
    private String oldpassword;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&._-]).+$",
            message = "La contraseña debe incluir mayúscula, minúscula, número y carácter especial")
    private String newpassword;

    @NotBlank(message = "La confirmación no puede estar vacía")
    private String confirmpassword;
}