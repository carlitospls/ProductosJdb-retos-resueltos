package es.daw.productojdbc.dto;

import es.daw.productojdbc.model.Fabricante;
import es.daw.productojdbc.model.Producto;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto del formulario de producto (lo que viaja entre la vista y el controlador).
 *
 * ¿Por qué no usamos Producto directamente?
 * El <select> solo envía el código del fabricante (un número), no un Fabricante completo.
 * Aquí lo recogemos como Integer y lo validamos; después lo convertimos a Producto.
 */
@Data // añade getter, setter, toString, equals, hashcode, contructor para atributos final
@NoArgsConstructor
public class ProductoDto {

    private Integer codigo;            // null = alta, con valor = edición

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que 0")
    private Double precio;

    @NotNull(message = "Selecciona un fabricante")
    private Integer codigoFabricante;


    // >>> RETO 4 [COLUMNA NUEVA] · sitio 8/10 · El campo en el DTO del formulario, con sus validaciones.
    //     - Tipo ENVOLTORIO (Integer, no int): un campo vacío llega como null y salta @NotNull con TU mensaje.
    //       (En clase quedó como "int": con int, el campo vacío da un error de conversión feo, no tu mensaje.)
    //     - Las anotaciones dicen lo que SE PERMITE: "no negativo" = @PositiveOrZero · "mayor que 0" = @Positive
    //       · rango = @Min(1) @Max(100). En clase la profe puso un rango de ejemplo:
    //           @Positive @Min(1) @Max(value=100, message="El valor máximo de stock es 100")
    //       Aquí se permite el 0 porque en data.sql hay productos con stock 0 (si no, no se podrían editar).
    //     - Pon SIEMPRE message = "..." con el texto literal del enunciado.
    //     - El nombre del campo tiene que ser el mismo que el th:field="*{stock}" del formulario (sitio 7).
    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;



    // ----------- MAPEO ----------
    /** Producto (modelo) → formulario (vista). Se usa al editar. */
    public static ProductoDto toDto(Producto p) {
        ProductoDto form = new ProductoDto();
        form.setCodigo(p.getCodigo());
        form.setNombre(p.getNombre());
        form.setPrecio(p.getPrecio());
        // >>> RETO 4 [COLUMNA NUEVA] · sitio 9/10 (a) · LA SILENCIOSA. Sin esta línea NO hay ningún error:
        //     simplemente, al pulsar Editar, el campo sale vacío. Solo se detecta probando a editar.
        form.setStock(p.getStock());
        form.setCodigoFabricante(p.getFabricante().getCodigo());
        return form;
    }

    /** Formulario (vista) → Producto (modelo). Del fabricante solo tenemos el código. */
    public Producto toModelEntity() {
        Fabricante fabricante = new Fabricante(codigoFabricante, null);
        // >>> RETO 4 [COLUMNA NUEVA] · sitio 9/10 (b) · El valor nuevo en el constructor, en el orden de la clase Producto.
        //     (Esta línea es la que deja de compilar al añadir el campo al modelo.)
        return new Producto(codigo, nombre, precio, stock, fabricante);
    }
}