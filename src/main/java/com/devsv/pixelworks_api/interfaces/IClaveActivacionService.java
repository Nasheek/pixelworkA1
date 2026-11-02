package com.devsv.pixelworks_api.interfaces;

import com.devsv.pixelworks_api.dto.ClaveActivacionDTO;

public interface IClaveActivacionService {
    ClaveActivacionDTO guardar(ClaveActivacionDTO dto);
    long contarStockDisponible(Integer productoId);
}