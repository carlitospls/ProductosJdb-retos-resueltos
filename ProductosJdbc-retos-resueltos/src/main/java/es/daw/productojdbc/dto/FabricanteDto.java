package es.daw.productojdbc.dto;

import es.daw.productojdbc.model.Fabricante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // añade getter, setter, toString, equals, hashcode, contructor para atributos final
@NoArgsConstructor
public class FabricanteDto {
    private Integer codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    // SI TE PIDEN UNA COLUMNA NUEVA EN FABRICANTE (como el año de nacimiento del autor en Biblioteca):
    //   es el RETO 4 aplicado a la tabla "padre". Los 10 sitios serían: schema.sql, data.sql, Fabricante,
    //   los SELECT y mapRow de FabricanteRepositoryJdbc (no hay SELECT_BASE: hay 2 SELECT, y además el SELECT del RETO 1),
    //   fabricantes/lista.html, fabricantes/formulario.html, este DTO (campo + validación),
    //   toDto/toModelEntity de aquí abajo, y save/update. OJO: ProductoRepositoryJdbc.mapRow y ProductoDto.toModelEntity
    //   también hacen "new Fabricante(...)" y dejarían de compilar: pásales null en el hueco nuevo.
    // ------------MAPEO --------
    /** Fabricante (modelo) → formulario (vista). Se usa al editar. */
    public static FabricanteDto toDto(Fabricante f) {
        FabricanteDto fabricanteDto = new FabricanteDto();
        fabricanteDto.setCodigo(f.getCodigo());
        fabricanteDto.setNombre(f.getNombre());
        return fabricanteDto;
    }

    /** Formulario (vista) → Fabricante (modelo).*/
    public static Fabricante toModelEntity(FabricanteDto f) {

        return new Fabricante(f.getCodigo(), f.getNombre());
    }

}
