package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.CategoriaDTO;
import java.util.List;

public interface ICategoriaService {
    List<CategoriaDTO> listarTodas();
    CategoriaDTO obtenerPorId(Integer id);
    CategoriaDTO guardar(CategoriaDTO dto);
    CategoriaDTO actualizar(Integer id, CategoriaDTO dto);
    void eliminar(Integer id);
}