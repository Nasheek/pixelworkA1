package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.DetalleReporteDTO;
import com.devsv.pixelworks_api.dto.ResumenFinancieroDTO;
import com.devsv.pixelworks_api.dto.ProductoAlertaDTO;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.interfaces.IReporteService;
import com.devsv.pixelworks_api.repository.CompraRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService implements IReporteService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;

    @Override
    public ResumenFinancieroDTO obtenerFinanzas(LocalDateTime inicio, LocalDateTime fin, List<Integer> productoIds) {

        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        // Fechas seguras para evitar excepciones de SQL
        LocalDateTime fechaInicio = (inicio != null) ? inicio : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fechaFin = (fin != null) ? fin : LocalDateTime.of(2100, 1, 1, 0, 0);

        // Seguridad para listas de Ids nulas
        boolean hasProductos = (productoIds != null && !productoIds.isEmpty());
        List<Integer> idsSeguros = hasProductos ? productoIds : List.of(-1);

        List<Object[]> resultados = compraRepository.obtenerDesgloseVentas(fechaInicio, fechaFin, idsSeguros, hasProductos);

        BigDecimal ingresosTotales = BigDecimal.ZERO;
        long cantidadVentas = 0;
        List<DetalleReporteDTO> detalles = new ArrayList<>();

        for (Object[] fila : resultados) {
            String nombre = (String) fila[0];
            long cantidad = ((Number) fila[1]).longValue();
            BigDecimal ingresos = new BigDecimal(fila[2].toString());

            ingresosTotales = ingresosTotales.add(ingresos);
            cantidadVentas += cantidad;

            detalles.add(new DetalleReporteDTO(nombre, cantidad, ingresos));
        }

        ResumenFinancieroDTO dto = new ResumenFinancieroDTO();
        dto.setIngresosTotales(ingresosTotales);
        dto.setCantidadVentas(cantidadVentas);
        dto.setDetalles(detalles); // Incluye el desglose que React pinta en el PDF

        return dto;
    }

    @Override
    public List<ProductoAlertaDTO> obtenerJuegosSinStock() {
        List<Producto> agotados = productoRepository.findProductosSinStock();

        return agotados.stream().map(p -> {
            ProductoAlertaDTO dto = new ProductoAlertaDTO();
            dto.setId(p.getId());
            dto.setNombre(p.getNombre());
            return dto;
        }).toList();
    }
}
