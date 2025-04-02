package com.inventory.inventario.repository;

import com.inventory.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByActivoTrue(); // Buscar solo productos activos
    List<Producto> findByActivoFalse(); // Buscar productos inactivos
}
