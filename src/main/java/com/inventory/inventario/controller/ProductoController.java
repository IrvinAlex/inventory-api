package com.inventory.inventario.controller;

import com.inventory.inventario.dto.InventarioDTO;
import com.inventory.inventario.model.Historial;
import com.inventory.inventario.model.Producto;
import com.inventory.inventario.model.Usuario;
import com.inventory.inventario.service.HistorialService;
import com.inventory.inventario.service.ProductoService;
import com.inventory.inventario.service.UsuarioService;
import com.inventory.inventario.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private HistorialService historialService;
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        // Log para depuración
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Usuario autenticado: " + (auth != null ? auth.getName() : "ninguno"));
        System.out.println("Autoridades: " + (auth != null ? auth.getAuthorities() : "ninguna"));
        
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> listarProductosActivos() {
        return ResponseEntity.ok(productoService.obtenerActivos());
    }
    
    @GetMapping("/inactivos")
    public ResponseEntity<List<Producto>> listarProductosInactivos() {
        return ResponseEntity.ok(productoService.obtenerInactivos());
    }

    @GetMapping("/{id}")
     public ResponseEntity<Producto> obtenerProducto(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.obtenerPorId(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        try {
            // Asegurar que el nuevo producto tenga cantidad 0
            producto.setCantidad(0);
            producto.setActivo(true);
            
            Producto nuevoProducto = productoService.guardarProducto(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Error al crear el producto: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody Producto producto) {
        try {
            Optional<Producto> productoExistente = productoService.obtenerPorId(id);
            if (!productoExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Mantener la cantidad y estatus actual
            producto.setId(id);
            producto.setCantidad(productoExistente.get().getCantidad());
            producto.setActivo(productoExistente.get().getActivo());
            
            Producto productoActualizado = productoService.guardarProducto(producto);
            return ResponseEntity.ok(productoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Error al actualizar el producto: " + e.getMessage()));
        }
    }

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody InventarioDTO inventarioDTO) {
        try {
            // Validar que la cantidad sea positiva
            if (inventarioDTO.getCantidad() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La cantidad debe ser mayor a cero"));
            }
            
            // Obtener el producto
            Optional<Producto> productoOpt = productoService.obtenerPorId(inventarioDTO.getProductoId());
            if (!productoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Producto no encontrado"));
            }
            
            Producto producto = productoOpt.get();
            
            // Validar que el producto esté activo
            if (!producto.getActivo()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "No se puede agregar inventario a un producto inactivo"));
            }
            
            // Actualizar el stock
            producto = productoService.actualizarStock(inventarioDTO.getProductoId(), inventarioDTO.getCantidad());
            
            // Registrar en historial
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(inventarioDTO.getUsuarioId());
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Usuario no encontrado"));
            }
            
            Historial historial = new Historial();
            historial.setTipo("ENTRADA");
            historial.setCantidad(inventarioDTO.getCantidad());
            historial.setFecha(LocalDateTime.now());
            historial.setProducto(producto);
            historial.setUsuario(usuarioOpt.get());
            
            historialService.guardarMovimiento(historial);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Inventario actualizado correctamente",
                "producto", producto
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Error al registrar entrada: " + e.getMessage()));
        }
    }

    @PostMapping("/salida")
    public ResponseEntity<?> registrarSalida(@RequestBody InventarioDTO inventarioDTO) {
        try {
            // Validar que la cantidad sea positiva
            if (inventarioDTO.getCantidad() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "La cantidad debe ser mayor a cero"));
            }
            
            Producto producto = inventarioService.registrarSalida(inventarioDTO);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Salida de inventario registrada correctamente",
                "producto", producto
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Error al registrar salida: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarProducto(@PathVariable Integer id) {
        try {
            Producto producto = productoService.activarProducto(id);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Producto activado correctamente",
                "producto", producto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Error al activar el producto: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/baja")
    public ResponseEntity<?> darDeBaja(@PathVariable Integer id) {
        try {
            Producto producto = productoService.darDeBaja(id);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Producto dado de baja correctamente",
                "producto", producto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Error al dar de baja el producto: " + e.getMessage()));
        }
    }
}