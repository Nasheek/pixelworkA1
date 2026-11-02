package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import java.util.List;

public interface IProductoService {
    List<ProductoDTO> listarTodos();
    ProductoDTO obtenerPorId(Integer id);
    ProductoDTO guardar(ProductoDTO dto);
    ProductoDTO actualizar(Integer id, ProductoDTO dto);
    void eliminar(Integer id);
}