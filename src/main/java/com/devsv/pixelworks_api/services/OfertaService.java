package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import com.devsv.pixelworks_api.entities.OfertaProducto;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import com.devsv.pixelworks_api.mappers.OfertaMapper;
import com.devsv.pixelworks_api.repository.OfertaProductoRepository;
import com.devsv.pixelworks_api.repository.OfertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfertaService implements IOfertaService {

    private final OfertaRepository ofertaRepository;
    private final OfertaProductoRepository ofertaProductoRepository;
    private final OfertaMapper ofertaMapper;

    @Override
    public List<OfertaDTO> listarTodas() {
        return ofertaRepository.findAll().stream()
                .map(ofertaMapper::toDTO)
                .toList();
    }

    @Override
    public OfertaDTO obtenerPorId(Integer id) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Oferta no encontrada con el ID: " + id
                        )
                );

        return ofertaMapper.toDTO(oferta);
    }

    @Override
    @Transactional
    public OfertaDTO guardar(OfertaDTO dto) {
        validarOferta(dto);

        Oferta oferta = ofertaMapper.toEntity(dto);

        return ofertaMapper.toDTO(
                ofertaRepository.save(oferta)
        );
    }

    @Override
    @Transactional
    public OfertaDTO actualizar(Integer id, OfertaDTO dto) {

        validarOferta(dto);

        Oferta ofertaExistente = ofertaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Oferta no encontrada con el ID: " + id
                        )
                );

        ofertaExistente.setNombre(dto.getNombre());
        ofertaExistente.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        ofertaExistente.setFechaInicio(dto.getFechaInicio());
        ofertaExistente.setFechaFin(dto.getFechaFin());

        return ofertaMapper.toDTO(
                ofertaRepository.save(ofertaExistente)
        );
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {

        if (!ofertaRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Oferta no encontrada con el ID: " + id
            );
        }

        // Las asignaciones dependen de la oferta y deben eliminarse primero.
        ofertaProductoRepository.deleteAll(ofertaProductoRepository.findByOfertaId(id));
        ofertaProductoRepository.flush();
        ofertaRepository.deleteById(id);
    }

    private void validarOferta(OfertaDTO dto) {
        if (dto == null || dto.getNombre() == null || dto.getNombre().isBlank()
                || dto.getNombre().length() > 50) {
            throw new IllegalArgumentException("El nombre de la oferta es obligatorio y admite hasta 50 caracteres.");
        }
        BigDecimal descuento = dto.getPorcentajeDescuento();
        if (descuento == null || descuento.signum() < 0
                || descuento.compareTo(BigDecimal.valueOf(100)) > 0
                || descuento.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100 y tener como máximo dos decimales.");
        }
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    @Override
    public BigDecimal obtenerDescuentoActivo(Integer productoId) {

        List<OfertaProducto> ofertasActivas =
                ofertaProductoRepository.obtenerOfertasActivasPorProductos(
                        List.of(productoId)
                );

        if (ofertasActivas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        OfertaProducto ofertaProducto = ofertasActivas.get(0);

        if (ofertaProducto.getOferta() == null
                || ofertaProducto.getOferta().getPorcentajeDescuento() == null) {
            return BigDecimal.ZERO;
        }

        return ofertaProducto.getOferta().getPorcentajeDescuento();
    }
}
