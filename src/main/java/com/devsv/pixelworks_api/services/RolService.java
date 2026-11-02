package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.RolDTO;
import com.devsv.pixelworks_api.entities.Rol;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IRolService;
import com.devsv.pixelworks_api.mappers.RolMapper;
import com.devsv.pixelworks_api.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolService implements IRolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listarTodos() {
        return rolRepository.findAll().stream()
                .map(rolMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolDTO obtenerPorId(Integer id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con el ID: " + id));
        return rolMapper.toDTO(rol);
    }

    @Override
    @Transactional
    public RolDTO guardar(RolDTO dto) {
        if (rolRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
        }
        Rol rol = rolMapper.toEntity(dto);
        Rol rolGuardado = rolRepository.save(rol);
        return rolMapper.toDTO(rolGuardado);
    }

    @Override
    @Transactional
    public RolDTO actualizar(Integer id, RolDTO dto) {
        Rol rolExistente = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con el ID: " + id));

        if (rolRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
        }

        // Actualización manual por seguridad
        rolExistente.setNombre(dto.getNombre());

        Rol rolActualizado = rolRepository.save(rolExistente);
        return rolMapper.toDTO(rolActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!rolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol no encontrado con el ID: " + id);
        }
        rolRepository.deleteById(id);
    }
}
