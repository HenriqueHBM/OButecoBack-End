package projeto.OButecoBack_End.controller.produto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.estoque.dto.EstoquesResponse;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoRequest;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoResponse;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.service.produto.ProdutosService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@CrossOrigin("*")
public class ProdutosController {
    private static final Logger log = LoggerFactory.getLogger(ProdutosController.class);

    private final ProdutosService produtosService;

    public ProdutosController(ProdutosService produtosService) {
        this.produtosService = produtosService;
    }

    //CREATE
    @PostMapping()
    public ResponseEntity<ProdutoResponse>salvarProduto(@Valid @RequestBody ProdutoRequest request){
        log.info("Solicitação de cadastro de produto. Nome: {}, Categoria: {}",
                request.nome(),
                request.categoriaEnum());

        ProdutosEntity produtosEntity = this.produtosService.salvarProduto(request);

        log.info("Produto cadastrado com sucesso. ID: {}, Nome: {}",
                produtosEntity.getId(),
                produtosEntity.getNome());

        return new ResponseEntity<>(ProdutoResponse.de(produtosEntity), HttpStatus.CREATED);
    }

    //UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequest request
    ){
        log.info("Solicitação de atualização de produto. ID: {}", id);

        ProdutosEntity produto = this.produtosService.atualizarProduto(id, request);

        log.info("Solicitação de atualização de produto. ID: {}", id);

        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }

    //UPDATE PARTIAL
    @PatchMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizarProdutoParcial(
            @PathVariable Long id,
            @Valid@RequestBody ProdutoRequest request
    ){
        log.info("Solicitação de atualização parcial de produto. ID: {}", id);

        ProdutosEntity produto = this.produtosService.atualizarProdutoParcial(id, request);

        log.info("Atualização parcial concluída. ID: {}", id);

        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }

    //CHANGE STATUS
    @PatchMapping("change_status/{id}")
    public ResponseEntity<ProdutoResponse> atualizarStatusProduto(
            @PathVariable Long id
    ){
        log.info("Solicitação de alteração de status do produto. ID: {}", id);

        ProdutosEntity produto = this.produtosService.atualizarStatusProduto(id);

        log.info("Status do produto alterado com sucesso. ID: {}", id);

        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ProdutoResponse> deletarProduto(
            @PathVariable Long id
    ){
        log.info("Solicitação de exclusão de produto. ID: {}", id);

        this.produtosService.deletarProduto(id);

        log.info("Produto excluído com sucesso. ID: {}", id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //READ/LIST
    @GetMapping()
    public ResponseEntity<List<ProdutoResponse>> listarProdutos(){
        log.info("Solicitação de consulta de produtos");

        List<ProdutoResponse> produtos = this.produtosService.listarProdutos()
                .stream()
                .map(ProdutoResponse::de)
                .toList();

        log.info("Produtos consultados com sucesso. Total: {}", produtos.size());

        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }


    //FIND BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId (@PathVariable Long id) {
        ProdutosEntity produto = this.produtosService.buscarProdutoPorId(id);
        return new ResponseEntity<>(ProdutoResponse.de(produto), HttpStatus.OK);
    }
}
