package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.OfertaProductoDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import com.devsv.pixelworks_api.entities.OfertaProducto;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IOfertaProductoService;
import com.devsv.pixelworks_api.mappers.OfertaProductoMapper;
import com.devsv.pixelworks_api.repository.OfertaProductoRepository;
import com.devsv.pixelworks_api.repository.OfertaRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfertaProductoService implements IOfertaProductoService {

    private final OfertaProductoRepository ofertaProductoRepository;
    private final OfertaRepository ofertaRepository;
    private final ProductoRepository productoRepository;
    private final OfertaProductoMapper ofertaProductoMapper;

    @Override
    public List<OfertaProductoDTO> listarTodos() {
        return ofertaProductoRepository.findAll().stream()
                .map(ofertaProductoMapper::toDTO)
                .toList();
    }

    @Override
    public OfertaProductoDTO obtenerPorId(Integer id) {
        OfertaProducto ofertaProducto = ofertaProductoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relación Oferta-Producto no encontrada con el ID: " + id));
        return ofertaProductoMapper.toDTO(ofertaProducto);
    }

    @Override
    @Transactional
    public OfertaProductoDTO guardar(OfertaProductoDTO dto) {
        if (dto == null || dto.getOfertaId() == null || dto.getOfertaId() <= 0
                || dto.getProductoId() == null || dto.getProductoId() <= 0) {
            throw new IllegalArgumentException("La oferta y el producto son obligatorios.");
        }
        Oferta oferta = ofertaRepository.findById(dto.getOfertaId())
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada con el ID: " + dto.getOfertaId()));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + dto.getProductoId()));

        if (ofertaProductoRepository.existsByOfertaIdAndProductoId(dto.getOfertaId(), dto.getProductoId())) {
            throw new IllegalArgumentException("Este producto ya está asignado a la oferta seleccionada.");
        }

        OfertaProducto entidad = ofertaProductoMapper.toEntity(dto, oferta, producto);
        return ofertaProductoMapper.toDTO(ofertaProductoRepository.save(entidad));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!ofertaProductoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relación Oferta-Producto no encontrada con el ID: " + id);
        }
        ofertaProductoRepository.deleteById(id);
    }
}
