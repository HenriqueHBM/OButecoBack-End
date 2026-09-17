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

import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProdutosController {
    private final ProdutosService produtosService;

    //CREATE
    @PostMapping()
    public ResponseEntity<ProdutoResponse>salvarProduto(@Valid @RequestBody ProdutoRequest request){
        ProdutosEntity produtosEntity = this.produtosService.salvarProduto(request);
        return new ResponseEntity<>(ProdutoResponse.de(produtosEntity), HttpStatus.CREATED);
    }

    //UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request
    ){
        ProdutosEntity produto = this.produtosService.atualizarProduto(id, request);
        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }

    //UPDATE PARTIAL
    @PatchMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizarProdutoParcial(
            @PathVariable Long id,
            @Valid@RequestBody ProdutoRequest request
    ){
        ProdutosEntity produto = this.produtosService.atualizarProdutoParcial(id, request);
        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }

    //CHANGE STATUS
    @PatchMapping("change_status/{id}")
    public ResponseEntity<ProdutoResponse> atualizarStatusProduto(
            @PathVariable Long id
    ){
        ProdutosEntity produto = this.produtosService.atualizarStatusProduto(id);
        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ProdutoResponse> deletarProduto(
            @PathVariable Long id
    ){
        this.produtosService.deletarProduto(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //READ/LIST
    @GetMapping()
    public ResponseEntity<List<ProdutoResponse>> listarProdutos(){
        List<ProdutoResponse> produtos = this.produtosService.listarProdutos()
                .stream()
                .map(ProdutoResponse::de)
                .toList();
        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }
}
