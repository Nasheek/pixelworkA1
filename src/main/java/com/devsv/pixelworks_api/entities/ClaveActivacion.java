package com.devsv.pixelworks_api.entities;

import com.devsv.pixelworks_api.enums.EstadoClave;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "claves_activaciones", schema = "public")
public class ClaveActivacion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "codigo", nullable = false, length = 100, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoClave estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_compra_id")
    private DetalleCompra detalleCompra;
}