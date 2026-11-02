package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.RealizarCompraDTO;
import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import java.util.List;

public interface ICompraService {

    CompraResponseDTO procesarCompra(RealizarCompraDTO dto, Integer usuarioId);

    List<CompraResponseDTO> obtenerMisCompras(Integer usuarioId);
}