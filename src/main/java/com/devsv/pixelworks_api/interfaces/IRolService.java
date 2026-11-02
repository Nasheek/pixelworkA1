package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.RolDTO;
import java.util.List;

public interface IRolService {
    List<RolDTO> listarTodos();
    RolDTO obtenerPorId(Integer id);
    RolDTO guardar(RolDTO dto);
    RolDTO actualizar(Integer id, RolDTO dto);
    void eliminar(Integer id);
}