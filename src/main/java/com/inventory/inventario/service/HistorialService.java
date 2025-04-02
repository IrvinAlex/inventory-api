package com.inventory.inventario.service;

import com.inventory.inventario.model.Historial;
import com.inventory.inventario.repository.HistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistorialService {

    @Autowired
    private HistorialRepository historialRepository;

    public List<Historial> obtenerTodos() {
        return historialRepository.findAll();
    }

    public List<Historial> obtenerPorTipo(String tipo) {
        return historialRepository.findByTipo(tipo);
    }

    public Historial guardarMovimiento(Historial historial) {
        return historialRepository.save(historial);
    }
}