package com.formen.service;

import com.formen.entity.ConfiguracionTienda;
import com.formen.entity.Producto;
import com.formen.repository.ConfiguracionTiendaRepository;
import com.formen.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TiendaService {
    private final ProductoRepository productoRepository;
    private final ConfiguracionTiendaRepository configuracionRepository;

    public TiendaService(ProductoRepository productoRepository, ConfiguracionTiendaRepository configuracionRepository) {
        this.productoRepository = productoRepository;
        this.configuracionRepository = configuracionRepository;
    }

    public ConfiguracionTienda config() {
        return configuracionRepository.findById(1L).orElseGet(() -> configuracionRepository.save(new ConfiguracionTienda()));
    }

    public List<Producto> productosPublicos() {
        if (config().isOcultarSinStock()) {
            return productoRepository.findByActivoTrueAndCategoriaActivoTrueAndStockGreaterThanOrderByIdAsc(0);
        }
        return productoRepository.findByActivoTrueAndCategoriaActivoTrueOrderByIdAsc();
    }

    public List<Producto> filtrar(String buscar, BigDecimal min, BigDecimal max) {
        return productosPublicos().stream()
                .filter(p -> buscar == null || buscar.isBlank() || p.getNombre().toLowerCase().contains(buscar.toLowerCase()))
                .filter(p -> min == null || p.getPrecio().compareTo(min) >= 0)
                .filter(p -> max == null || p.getPrecio().compareTo(max) <= 0)
                .toList();
    }
}
