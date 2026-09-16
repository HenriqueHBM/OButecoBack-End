package projeto.OButecoBack_End.controller.produto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoRequest;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoResponse;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.service.produto.ProdutosService;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProdutosController {
    private final ProdutosService produtosService;

    @PostMapping()
    public ResponseEntity<ProdutoResponse>salvarProduto(@Valid @RequestBody ProdutoRequest request){
        ProdutosEntity produtosEntity = this.produtosService.salvarProduto(request);
        return new ResponseEntity<>(ProdutoResponse.de(produtosEntity), HttpStatus.CREATED);
    }

}
