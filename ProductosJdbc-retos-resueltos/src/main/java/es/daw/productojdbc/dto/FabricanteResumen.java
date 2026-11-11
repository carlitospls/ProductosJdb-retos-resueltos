package es.daw.productojdbc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// >>> RETO 1 [CONTAR CON JOIN] · paso 1/6 · Un DTO nuevo para el listado: los datos del fabricante + el número calculado.
//     ¿Por qué una clase nueva? El modelo Fabricante representa una FILA de la tabla y no tiene numProductos.
//     El orden de los campos es el orden del constructor: new FabricanteResumen(codigo, nombre, numProductos).
//     La vista lo lee con ${f.numProductos} (getter que genera @Data).

@Data //solo mete constructor de finals
@AllArgsConstructor
//@NoArgsConstructor
public class FabricanteResumen {

    private Integer codigo;
    private String nombre;
    private int numProductos;
}