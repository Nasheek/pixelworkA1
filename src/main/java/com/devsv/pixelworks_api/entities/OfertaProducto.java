package com.devsv.pixelworks_api.entities;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ofertas_productos", schema = "public")
public class OfertaProducto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private Oferta oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productos_id", nullable = false)
    private Producto producto;
}