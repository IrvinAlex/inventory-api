package com.inventory.inventario.controller;

import com.inventory.inventario.model.Rol;
import com.inventory.inventario.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles") // Cambiado a /api/roles para coincidir con la configuración de seguridad
public class RolController {

    @Autowired
    private RolService rolService;

    // Endpoint para obtener todos los roles
    @GetMapping
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(rolService.obtenerTodos());
    }
}
