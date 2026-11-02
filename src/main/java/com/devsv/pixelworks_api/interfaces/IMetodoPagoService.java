package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.MetodoPagoDTO;
import java.util.List;

public interface IMetodoPagoService {
    List<MetodoPagoDTO> listarTodos();
    MetodoPagoDTO obtenerPorId(Integer id);
    MetodoPagoDTO guardar(MetodoPagoDTO dto);
    MetodoPagoDTO actualizar(Integer id, MetodoPagoDTO dto);
    void eliminar(Integer id);
}