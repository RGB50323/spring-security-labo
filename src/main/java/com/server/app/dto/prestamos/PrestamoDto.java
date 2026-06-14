package com.server.app.dto.prestamos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PrestamoDto {
    @NotNull(message = "El capital solicitado es requerido")
    private BigDecimal capitalSolicitado;

    @NotNull(message = "La tasa de interés anual es requerida")
    private BigDecimal tasaInteresAnual;

    @NotNull(message = "El plazo en meses es requerido")
    @Min(value = 1, message = "El plazo mínimo es 1 mes")
    private Integer plazoMeses;
}