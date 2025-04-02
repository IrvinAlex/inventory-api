package com.inventory.inventario.service;

import com.inventory.inventario.model.Producto;
import com.inventory.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerActivos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> obtenerInactivos() {
        return productoRepository.findByActivoFalse();
    }

    public Optional<Producto> obtenerPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {
        // Cuando se crea un nuevo producto, se establece cantidad en 0 si no viene especificada
        if (producto.getId() == null && producto.getCantidad() == null) {
            producto.setCantidad(0);
        }
        
        // Asegurar que el producto se guarde como activo si es nuevo
        if (producto.getId() == null && producto.getActivo() == null) {
            producto.setActivo(true);
        }
        
        return productoRepository.save(producto);
    }

    public Producto actualizarStock(Integer id, int cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (!productoOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        
        Producto producto = productoOpt.get();
        
        // Verificar que no se intente reducir el inventario
        if (cantidad < 0) {
            throw new RuntimeException("No se permite disminuir el inventario desde este método");
        }
        
        producto.setCantidad(producto.getCantidad() + cantidad);
        return productoRepository.save(producto);
    }

    public Producto cambiarEstatus(Integer id, boolean activar) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (!productoOpt.isPresent()) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        
        Producto producto = productoOpt.get();
        producto.setActivo(activar);
        return productoRepository.save(producto);
    }

    public Producto darDeBaja(Integer id) {
        return cambiarEstatus(id, false);
    }
    
    public Producto activarProducto(Integer id) {
        return cambiarEstatus(id, true);
    }
}
