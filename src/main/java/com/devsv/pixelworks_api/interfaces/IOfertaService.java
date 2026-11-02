package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import java.math.BigDecimal;
import java.util.List;

public interface IOfertaService {
    List<OfertaDTO> listarTodas();
    OfertaDTO obtenerPorId(Integer id);
    OfertaDTO guardar(OfertaDTO dto);
    OfertaDTO actualizar(Integer id, OfertaDTO dto);
    void eliminar(Integer id);

    BigDecimal obtenerDescuentoActivo(Integer productoId);
}