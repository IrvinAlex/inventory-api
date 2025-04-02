package com.inventory.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioDTO {
    private Integer productoId;
    private Integer cantidad;
    private Integer usuarioId;
    private String tipo;  // "ENTRADA" o "SALIDA"
}
