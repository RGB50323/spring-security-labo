package com.server.app.services;

import com.server.app.dto.prestamos.AbonoDto;
import com.server.app.dto.prestamos.PrestamoDto;
import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.*;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final AbonoRepository abonoRepository;

    public Pagination<Prestamo> findByUsuario(Integer usuarioId, int page, int size) {
        Page<Prestamo> p = prestamoRepository.findByUsuarioId(usuarioId, PageRequest.of(page - 1, size));
        return new Pagination<>(p.getContent(), new PaginationMeta(page, size, p.getTotalPages(), p.getTotalElements()));
    }

    @Transactional
    public Prestamo create(PrestamoDto dto, User usuario) {
        Prestamo prestamo = Prestamo.builder()
                .capitalSolicitado(dto.getCapitalSolicitado())
                .tasaInteresAnual(dto.getTasaInteresAnual())
                .plazoMeses(dto.getPlazoMeses())
                .estado(Prestamo.EstadoPrestamo.APROBADO)
                .usuario(usuario)
                .build();

        prestamo = prestamoRepository.save(prestamo);
        generarAmortizacion(prestamo);
        return prestamo;
    }

    private void generarAmortizacion(Prestamo prestamo) {
        BigDecimal capital = prestamo.getCapitalSolicitado();
        BigDecimal tasaMensual = prestamo.getTasaInteresAnual()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        int meses = prestamo.getPlazoMeses();

        BigDecimal factor = tasaMensual.add(BigDecimal.ONE).pow(meses);
        BigDecimal cuota = capital.multiply(tasaMensual.multiply(factor))
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        BigDecimal saldo = capital;
        List<PlanPago> planes = new ArrayList<>();

        for (int i = 1; i <= meses; i++) {
            BigDecimal interes = saldo.multiply(tasaMensual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capitalCuota = cuota.subtract(interes).setScale(2, RoundingMode.HALF_UP);

            PlanPago plan = PlanPago.builder()
                    .numeroCuota(i)
                    .montoInteres(interes)
                    .montoCapital(capitalCuota)
                    .fechaVencimiento(LocalDate.now().plusMonths(i))
                    .estado(PlanPago.EstadoCuota.PENDIENTE)
                    .prestamo(prestamo)
                    .build();

            planes.add(plan);
            saldo = saldo.subtract(capitalCuota);
        }

        planPagoRepository.saveAll(planes);
    }

    public List<PlanPago> findPlanes(Integer prestamoId) {
        prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado"));
        return planPagoRepository.findByPrestamoId(prestamoId);
    }

    @Transactional
    public Abono registrarAbono(AbonoDto dto) {
        PlanPago plan = planPagoRepository.findById(dto.getPlanPagoId())
                .orElseThrow(() -> new NotFoundException("Plan de pago no encontrado"));

        if (plan.getEstado() == PlanPago.EstadoCuota.PAGADO) {
            throw new BadRequestException("Esta cuota ya fue pagada");
        }

        BigDecimal mora = BigDecimal.ZERO;
        if (LocalDate.now().isAfter(plan.getFechaVencimiento())) {
            mora = plan.getMontoCapital().add(plan.getMontoInteres())
                    .multiply(BigDecimal.valueOf(0.05))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Abono abono = Abono.builder()
                .monto(dto.getMonto())
                .fechaPago(LocalDateTime.now())
                .recargoMora(mora)
                .planPago(plan)
                .build();

        plan.setEstado(PlanPago.EstadoCuota.PAGADO);
        planPagoRepository.save(plan);

        verificarPrestamoPagado(plan.getPrestamo().getId());

        return abonoRepository.save(abono);
    }

    private void verificarPrestamoPagado(Integer prestamoId) {
        List<PlanPago> planes = planPagoRepository.findByPrestamoId(prestamoId);
        boolean todoPagado = planes.stream().allMatch(p -> p.getEstado() == PlanPago.EstadoCuota.PAGADO);
        if (todoPagado) {
            Prestamo prestamo = prestamoRepository.findById(prestamoId).orElseThrow();
            prestamo.setEstado(Prestamo.EstadoPrestamo.PAGADO);
            prestamoRepository.save(prestamo);
        }
    }

    public Map<String, Object> resumenCredito(Integer usuarioId) {
        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuarioId, PageRequest.of(0, 1000)).getContent();

        BigDecimal totalDeuda = BigDecimal.ZERO;
        int cuotasPendientes = 0;

        for (Prestamo p : prestamos) {
            List<PlanPago> planes = planPagoRepository.findByPrestamoId(p.getId());
            for (PlanPago plan : planes) {
                if (plan.getEstado() == PlanPago.EstadoCuota.PENDIENTE) {
                    totalDeuda = totalDeuda.add(plan.getMontoCapital()).add(plan.getMontoInteres());
                    cuotasPendientes++;
                }
            }
        }

        return Map.of(
                "totalPrestamos", prestamos.size(),
                "totalDeudaPendiente", totalDeuda,
                "cuotasPendientes", cuotasPendientes
        );
    }
}