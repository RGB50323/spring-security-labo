package com.server.app.dto.prestamos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AbonoDto {
    @NotNull(message = "El monto es requerido")
    private BigDecimal monto;

    @NotNull(message = "El ID del plan de pago es requerido")
    private Integer planPagoId;
}