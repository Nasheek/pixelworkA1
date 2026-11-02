package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.DesarrolladorDTO;
import com.devsv.pixelworks_api.entities.Desarrollador;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IDesarrolladorService;
import com.devsv.pixelworks_api.mappers.DesarrolladorMapper;
import com.devsv.pixelworks_api.repository.DesarrolladorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesarrolladorService implements IDesarrolladorService {

    private final DesarrolladorRepository desarrolladorRepository;
    private final DesarrolladorMapper desarrolladorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DesarrolladorDTO> listarTodos() {
        return desarrolladorRepository.findAll().stream()
                .map(desarrolladorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DesarrolladorDTO obtenerPorId(Integer id) {
        Desarrollador desarrollador = desarrolladorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desarrollador no encontrado con el ID: " + id));
        return desarrolladorMapper.toDTO(desarrollador);
    }

    @Override
    @Transactional
    public DesarrolladorDTO guardar(DesarrolladorDTO dto) {
        if (desarrolladorRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un desarrollador con ese nombre.");
        }
        Desarrollador desarrollador = desarrolladorMapper.toEntity(dto);
        Desarrollador guardado = desarrolladorRepository.save(desarrollador);
        return desarrolladorMapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public DesarrolladorDTO actualizar(Integer id, DesarrolladorDTO dto) {
        Desarrollador existente = desarrolladorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desarrollador no encontrado con el ID: " + id));

        if (desarrolladorRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe un desarrollador con ese nombre.");
        }

        // Actualización manual por seguridad
        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setTipo(dto.getTipo());
        existente.setPais(dto.getPais());

        Desarrollador actualizado = desarrolladorRepository.save(existente);
        return desarrolladorMapper.toDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!desarrolladorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Desarrollador no encontrado con el ID: " + id);
        }
        desarrolladorRepository.deleteById(id);
    }
}
