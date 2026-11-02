package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.DesarrolladorDTO;
import java.util.List;

public interface IDesarrolladorService {
    List<DesarrolladorDTO> listarTodos();
    DesarrolladorDTO obtenerPorId(Integer id);
    DesarrolladorDTO guardar(DesarrolladorDTO dto);
    DesarrolladorDTO actualizar(Integer id, DesarrolladorDTO dto);
    void eliminar(Integer id);
}