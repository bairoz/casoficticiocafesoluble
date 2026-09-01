package com.cafesoluble.catalogo.controller;

import com.cafesoluble.catalogo.model.Producto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final List<Producto> productos = new ArrayList<>();
    private final AtomicLong siguienteId = new AtomicLong(9);

    public ProductoController() {
        productos.add(new Producto(1L, "Café Instantáneo Clásico", "50 g", "Café soluble", true));
        productos.add(new Producto(2L, "Café Instantáneo Fuerte", "100 g", "Café soluble", true));
        productos.add(new Producto(3L, "Café Descafeinado", "100 g", "Café soluble", false));
        productos.add(new Producto(4L, "Café con Leche Instantáneo", "200 g", "Mezclas", true));
        productos.add(new Producto(5L, "Café Vainilla", "150 g", "Café saborizado", true));
        productos.add(new Producto(6L, "Café Canela", "150 g", "Café saborizado", true));
        productos.add(new Producto(7L, "Café Premium Liofilizado", "100 g", "Café premium", true));
        productos.add(new Producto(8L, "Café Instantáneo Suave", "50 g", "Café soluble", false));
    }

    @GetMapping
    public List<Producto> obtenerTodos() {
        return productos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productos.stream()
                .filter(producto -> producto.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> registrar(@RequestBody Producto producto) {
        producto.setId(siguienteId.getAndIncrement());
        productos.add(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }
}
