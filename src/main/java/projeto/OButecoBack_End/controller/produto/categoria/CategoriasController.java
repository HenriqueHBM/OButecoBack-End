package projeto.OButecoBack_End.controller.produto.categoria;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.produto.categoria.dto.CategoriasRequest;
import projeto.OButecoBack_End.controller.produto.categoria.dto.CategoriasResponse;
import projeto.OButecoBack_End.model.entity.produto.CategoriasEntity;
import projeto.OButecoBack_End.model.service.produto.CategoriasService;

@RestController
@RequestMapping("produtos/categorias")
@AllArgsConstructor
public class CategoriasController {
    private final CategoriasService categoriasService;

    //post, put, patch, delete, get, getporid

    @PostMapping()
    public ResponseEntity<CategoriasResponse> salvarCategoria(@Valid @RequestBody CategoriasRequest categoriasRequest){
        CategoriasEntity categoriasEntity = this.categoriasService.salvarCategoria();
        return new ResponseEntity<>(CategoriasResponse.de(categoriasEntity), HttpStatus.CREATED);
    }
}
