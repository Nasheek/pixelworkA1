package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.OfertaProductoDTO;
import java.util.List;

public interface IOfertaProductoService {
    List<OfertaProductoDTO> listarTodos();
    OfertaProductoDTO obtenerPorId(Integer id);
    OfertaProductoDTO guardar(OfertaProductoDTO dto);
    void eliminar(Integer id);
}