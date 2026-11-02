package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.MetodoPagoDTO;
import com.devsv.pixelworks_api.entities.MetodoPago;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IMetodoPagoService;
import com.devsv.pixelworks_api.mappers.MetodoPagoMapper;
import com.devsv.pixelworks_api.repository.MetodoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetodoPagoService implements IMetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final MetodoPagoMapper metodoPagoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoDTO> listarTodos() {
        return metodoPagoRepository.findAll().stream()
                .map(metodoPagoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MetodoPagoDTO obtenerPorId(Integer id) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con el ID: " + id));
        return metodoPagoMapper.toDTO(metodoPago);
    }

    @Override
    @Transactional
    public MetodoPagoDTO guardar(MetodoPagoDTO dto) {
        if (metodoPagoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un método de pago con ese nombre.");
        }
        MetodoPago metodoPago = metodoPagoMapper.toEntity(dto);
        MetodoPago metodoGuardado = metodoPagoRepository.save(metodoPago);
        return metodoPagoMapper.toDTO(metodoGuardado);
    }

    @Override
    @Transactional
    public MetodoPagoDTO actualizar(Integer id, MetodoPagoDTO dto) {
        MetodoPago metodoExistente = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con el ID: " + id));

        if (metodoPagoRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe un método de pago con ese nombre.");
        }

        // Actualización manual por seguridad
        metodoExistente.setNombre(dto.getNombre());

        MetodoPago metodoActualizado = metodoPagoRepository.save(metodoExistente);
        return metodoPagoMapper.toDTO(metodoActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!metodoPagoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Método de pago no encontrado con el ID: " + id);
        }
        metodoPagoRepository.deleteById(id);
    }
}
