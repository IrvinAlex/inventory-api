package com.inventory.inventario.repository;

import com.inventory.inventario.model.Historial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Integer> {
    List<Historial> findByTipo(String tipo);
    List<Historial> findByTipoAndFechaBetween(String tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<Historial> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<Historial> findByUsuarioId(Integer usuarioId);
    List<Historial> findByProductoId(Integer productoId);
}
