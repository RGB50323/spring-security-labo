package com.server.app.repositories;

import com.server.app.entities.PlanPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanPagoRepository extends JpaRepository<PlanPago, Integer> {
    List<PlanPago> findByPrestamoId(Integer prestamoId);
}