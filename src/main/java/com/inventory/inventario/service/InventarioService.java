package com.inventory.inventario.service;

import com.inventory.inventario.dto.InventarioDTO;
import com.inventory.inventario.model.Historial;
import com.inventory.inventario.model.Producto;
import com.inventory.inventario.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
public class InventarioService {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private HistorialService historialService;
    
    @Transactional
    public Producto registrarEntrada(InventarioDTO inventarioDTO) {
        // Validar que la cantidad sea positiva
        if (inventarioDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        // Obtener el producto
        Optional<Producto> productoOpt = productoService.obtenerPorId(inventarioDTO.getProductoId());
        if (!productoOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }
        
        Producto producto = productoOpt.get();
        
        // Validar que el producto esté activo
        if (!producto.getActivo()) {
            throw new IllegalStateException("No se puede agregar inventario a un producto inactivo");
        }
        
        // Actualizar el stock
        producto = productoService.actualizarStock(inventarioDTO.getProductoId(), inventarioDTO.getCantidad());
        
        // Registrar en historial
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(inventarioDTO.getUsuarioId());
        if (!usuarioOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        
        Historial historial = new Historial();
        historial.setTipo("ENTRADA");
        historial.setCantidad(inventarioDTO.getCantidad());
        historial.setFecha(LocalDateTime.now());
        historial.setProducto(producto);
        historial.setUsuario(usuarioOpt.get());
        
        historialService.guardarMovimiento(historial);
        
        return producto;
    }
    
    @Transactional
    public Producto registrarSalida(InventarioDTO inventarioDTO) {
        // Validar que la cantidad sea positiva
        if (inventarioDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        // Obtener el producto
        Optional<Producto> productoOpt = productoService.obtenerPorId(inventarioDTO.getProductoId());
        if (!productoOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado");
        }
        
        Producto producto = productoOpt.get();
        
        // Validar que el producto esté activo
        if (!producto.getActivo()) {
            throw new IllegalStateException("No se puede retirar inventario de un producto inactivo");
        }
        
        // Validar que haya suficiente stock
        if (producto.getCantidad() < inventarioDTO.getCantidad()) {
            throw new IllegalStateException("No hay suficiente inventario disponible. Stock actual: " + producto.getCantidad());
        }
        
        // Actualizar el stock (restar la cantidad)
        producto.setCantidad(producto.getCantidad() - inventarioDTO.getCantidad());
        producto = productoService.guardarProducto(producto);
        
        // Registrar en historial
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(inventarioDTO.getUsuarioId());
        if (!usuarioOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        
        Historial historial = new Historial();
        historial.setTipo("SALIDA");
        historial.setCantidad(inventarioDTO.getCantidad());
        historial.setFecha(LocalDateTime.now());
        historial.setProducto(producto);
        historial.setUsuario(usuarioOpt.get());
        
        historialService.guardarMovimiento(historial);
        
        return producto;
    }
}
