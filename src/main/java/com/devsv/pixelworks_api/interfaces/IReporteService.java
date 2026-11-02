package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.ResumenFinancieroDTO;
import com.devsv.pixelworks_api.dto.ProductoAlertaDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface IReporteService {
    ResumenFinancieroDTO obtenerFinanzas(LocalDateTime inicio, LocalDateTime fin, List<Integer> productoIds);
    List<ProductoAlertaDTO> obtenerJuegosSinStock();
}