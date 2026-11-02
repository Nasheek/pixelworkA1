package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.RealizarCompraDTO;
import com.devsv.pixelworks_api.dto.DetalleItemDTO;
import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import com.devsv.pixelworks_api.dto.DetalleCompraResponseDTO;
import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.ICompraService;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import com.devsv.pixelworks_api.mappers.CompraMapper;
import com.devsv.pixelworks_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CompraService implements ICompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProductoRepository productoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClaveActivacionRepository claveActivacionRepository;
    private final IOfertaService ofertaService;
    private final CompraMapper compraMapper;

    @Override
    @Transactional
    public CompraResponseDTO procesarCompra(RealizarCompraDTO dto, Integer usuarioId) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la compra son obligatorios.");
        }
        if (dto.getMetodoPagoId() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un producto.");
        }
        // Agrupar antes de reservar stock y adquirir los bloqueos siempre en el mismo orden.
        Map<Integer, Integer> cantidadesPorProducto = new TreeMap<>();
        for (DetalleItemDTO item : dto.getItems()) {
            if (item == null || item.getProductoId() == null
                    || item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener un ID y una cantidad mayor que cero.");
            }
            try {
                cantidadesPorProducto.merge(item.getProductoId(), item.getCantidad(), Math::addExact);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("La cantidad solicitada para un producto es demasiado grande.", ex);
            }
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no válido."));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setMetodoPago(metodoPago);
        compra.setFechaVenta(LocalDateTime.now());
        compra.setTotal(BigDecimal.ZERO);

        Compra compraGuardada = compraRepository.save(compra);

        BigDecimal totalCompra = BigDecimal.ZERO;
        List<DetalleCompraResponseDTO> listaDetallesDTO = new ArrayList<>();

        for (Map.Entry<Integer, Integer> item : cantidadesPorProducto.entrySet()) {
            Integer productoId = item.getKey();
            int cantidad = item.getValue();
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + productoId + " no existe."));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new IllegalArgumentException("El juego " + producto.getNombre() + " ya no está disponible.");
            }

            List<ClaveActivacion> clavesDisponibles = claveActivacionRepository
                    .findByProductoIdAndEstado(producto.getId(), EstadoClave.DISPONIBLE, PageRequest.of(0, cantidad));

            if (clavesDisponibles.size() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para el juego: " + producto.getNombre());
            }

            BigDecimal precioEfectivo = calcularPrecioEfectivo(producto);
            BigDecimal subTotal = precioEfectivo.multiply(BigDecimal.valueOf(cantidad));
            totalCompra = totalCompra.add(subTotal);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compraGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioEfectivo);
            detalle.setSubTotal(subTotal);

            DetalleCompra detalleGuardado = detalleCompraRepository.save(detalle);

            List<String> codigosEntregados = new ArrayList<>();
            for (ClaveActivacion clave : clavesDisponibles) {
                clave.setEstado(EstadoClave.VENDIDA);
                clave.setDetalleCompra(detalleGuardado);
                claveActivacionRepository.save(clave);
                codigosEntregados.add(clave.getCodigo());
            }

            DetalleCompraResponseDTO detalleDTO = compraMapper.toDetalleResponseDTO(detalleGuardado);
            detalleDTO.setClaves(codigosEntregados);
            listaDetallesDTO.add(detalleDTO);
        }

        compraGuardada.setTotal(totalCompra);
        compraRepository.save(compraGuardada);

        CompraResponseDTO response = compraMapper.toResponseDTO(compraGuardada);
        response.setDetalles(listaDetallesDTO);

        return response;
    }

    private BigDecimal calcularPrecioEfectivo(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio().signum() < 0) {
            throw new IllegalArgumentException("El producto no tiene un precio válido.");
        }

        // Si falla la consulta de ofertas, la transacción debe cancelarse para no cobrar de más.
        BigDecimal descuento = ofertaService.obtenerDescuentoActivo(producto.getId());
        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }
        if (descuento.signum() < 0 || descuento.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100.");
        }
        BigDecimal porcentaje = descuento.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return producto.getPrecio().multiply(BigDecimal.ONE.subtract(porcentaje))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerMisCompras(Integer usuarioId) {
        List<Compra> misCompras = compraRepository.findByUsuarioId(usuarioId);

        return misCompras.stream().map(compra -> {
            CompraResponseDTO compraDTO = compraMapper.toResponseDTO(compra);

            List<DetalleCompra> detallesDeLaCompra = detalleCompraRepository.findByCompraId(compra.getId());

            List<DetalleCompraResponseDTO> detallesDTO = detallesDeLaCompra.stream().map(detalle -> {
                DetalleCompraResponseDTO detalleDTO = compraMapper.toDetalleResponseDTO(detalle);

                List<String> codigos = claveActivacionRepository.findByDetalleCompraId(detalle.getId())
                        .stream()
                        .map(ClaveActivacion::getCodigo)
                        .toList();

                detalleDTO.setClaves(codigos);
                return detalleDTO;
            }).toList();

            compraDTO.setDetalles(detallesDTO);
            return compraDTO;
        }).toList();
    }
}
