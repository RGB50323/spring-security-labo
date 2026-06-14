package com.server.app.controllers;

import com.server.app.dto.prestamos.AbonoDto;
import com.server.app.dto.prestamos.PrestamoDto;
import com.server.app.dto.response.Pagination;
import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.services.PrestamoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finanzas")
@AllArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @GetMapping("/prestamos")
    public ResponseEntity<Pagination<Prestamo>> listar(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(prestamoService.findByUsuario(user.getId(), page, size));
    }

    @PostMapping("/prestamos")
    public ResponseEntity<Prestamo> crear(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PrestamoDto dto) {
        return ResponseEntity.ok(prestamoService.create(dto, user));
    }

    @GetMapping("/prestamos/{id}/planes-pago")
    public ResponseEntity<List<PlanPago>> planes(@PathVariable Integer id) {
        return ResponseEntity.ok(prestamoService.findPlanes(id));
    }

    @PostMapping("/abonos")
    public ResponseEntity<Abono> abonar(@Valid @RequestBody AbonoDto dto) {
        return ResponseEntity.ok(prestamoService.registrarAbono(dto));
    }

    @GetMapping("/resumen-credito")
    public ResponseEntity<Map<String, Object>> resumen(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(prestamoService.resumenCredito(user.getId()));
    }
}