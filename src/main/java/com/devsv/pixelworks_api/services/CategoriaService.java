package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.CategoriaDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.ICategoriaService;
import com.devsv.pixelworks_api.mappers.CategoriaMapper;
import com.devsv.pixelworks_api.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + id));
        return categoriaMapper.toDTO(categoria);
    }

    @Override
    @Transactional
    public CategoriaDTO guardar(CategoriaDTO dto) {
        if (categoriaRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        Categoria categoria = categoriaMapper.toEntity(dto);
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoriaGuardada);
    }

    @Override
    @Transactional
    public CategoriaDTO actualizar(Integer id, CategoriaDTO dto) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con el ID: " + id));

        if (categoriaRepository.existsByNombreAndIdNot(dto.getNombre(), id)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }

        categoriaExistente.setNombre(dto.getNombre());
        categoriaExistente.setDescripcion(dto.getDescripcion());

        Categoria categoriaActualizada = categoriaRepository.save(categoriaExistente);
        return categoriaMapper.toDTO(categoriaActualizada);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con el ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }
}
