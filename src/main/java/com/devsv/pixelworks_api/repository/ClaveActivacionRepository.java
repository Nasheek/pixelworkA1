package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.enums.EstadoClave;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaveActivacionRepository extends JpaRepository<ClaveActivacion, Integer> {

    boolean existsByCodigo(String codigo);

    long countByProductoIdAndEstado(
            Integer productoId,
            EstadoClave estado
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ClaveActivacion c WHERE c.producto.id = :productoId AND c.estado = :estado ORDER BY c.id")
    List<ClaveActivacion> findByProductoIdAndEstado(
            @Param("productoId") Integer productoId,
            @Param("estado") EstadoClave estado,
            Pageable pageable
    );

    List<ClaveActivacion> findByDetalleCompraId(
            Integer detalleCompraId
    );

    @Query("""
        SELECT
            c.producto.id AS productoId,
            COUNT(c.id) AS stock
        FROM ClaveActivacion c
        WHERE c.producto.id IN :productoIds
          AND c.estado = :estado
        GROUP BY c.producto.id
        """)
    List<StockDisponibleProjection> obtenerStockPorProductos(
            @Param("productoIds") List<Integer> productoIds,
            @Param("estado") EstadoClave estado
    );

    interface StockDisponibleProjection {

        Integer getProductoId();

        Long getStock();
    }
}
