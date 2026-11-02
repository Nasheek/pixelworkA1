package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ClaveActivacionDTO;
import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IClaveActivacionService;
import com.devsv.pixelworks_api.mappers.ClaveActivacionMapper;
import com.devsv.pixelworks_api.repository.ClaveActivacionRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ClaveActivacionService implements IClaveActivacionService {

    // Regla de formato: 3 bloques de 4 a 5 caracteres alfanuméricos (letras/números) separados por guion
    private static final String REGEX_CODIGO = "^[A-Z0-9]{4,5}-[A-Z0-9]{4,5}-[A-Z0-9]{4,5}$";

    private final ClaveActivacionRepository claveRepository;
    private final ProductoRepository productoRepository;
    private final ClaveActivacionMapper claveMapper;

    @Override
    @Transactional
    public ClaveActivacionDTO guardar(ClaveActivacionDTO dto) {
        if (dto == null || dto.getProductoId() == null || dto.getProductoId() <= 0) {
            throw new IllegalArgumentException("Un ID de producto válido es obligatorio.");
        }
        // Validation 1: Campo en blanco o nulo
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código de activación no puede estar vacío.");
        }

        // Formateo automático: quita espacios en extremos y convierte a mayúsculas
        String codigoLimpio = dto.getCodigo().trim().toUpperCase(Locale.ROOT);

        // Validación 2: Cumplimiento del formato Regex
        if (!codigoLimpio.matches(REGEX_CODIGO)) {
            throw new IllegalArgumentException("Formato de código inválido. Debe seguir el patrón AAAA-1111-BBBB (letras/números en bloques separados por guion).");
        }

        // Validación 3: Duplicados
        if (claveRepository.existsByCodigo(codigoLimpio)) {
            throw new IllegalArgumentException("El código de activación '" + codigoLimpio + "' ya existe en la base de datos.");
        }

        // Validación 4: Producto existente
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + dto.getProductoId()));

        // Mapeo e inyección de reglas de negocio
        ClaveActivacion clave = claveMapper.toEntity(dto, producto);
        clave.setCodigo(codigoLimpio); // Guarda la versión limpia en mayúsculas
        clave.setEstado(EstadoClave.DISPONIBLE);

        ClaveActivacion claveGuardada = claveRepository.save(clave);
        return claveMapper.toDTO(claveGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarStockDisponible(Integer productoId) {
        return claveRepository.countByProductoIdAndEstado(productoId, EstadoClave.DISPONIBLE);
    }
}
