package com.inventory.inventario.controller;

import com.inventory.inventario.model.Historial;
import com.inventory.inventario.service.HistorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historial")
public class HistorialController {

    @Autowired
    private HistorialService historialService;

    @GetMapping
    public List<Historial> listarHistorial() {
        return historialService.obtenerTodos();
    }

    @GetMapping("/tipo/{tipo}")
    public List<Historial> filtrarPorTipo(@PathVariable String tipo) {
        return historialService.obtenerPorTipo(tipo);
    }

    @PostMapping
    public ResponseEntity<Historial> registrarMovimiento(@RequestBody Historial historial) {
        return ResponseEntity.ok(historialService.guardarMovimiento(historial));
    }
}