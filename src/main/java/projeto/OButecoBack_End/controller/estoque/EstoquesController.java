package projeto.OButecoBack_End.controller.estoque;

import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.estoque.dto.EstoquesRequest;
import projeto.OButecoBack_End.controller.estoque.dto.EstoquesResponse;
import projeto.OButecoBack_End.controller.estoque.dto.MovimentacoesEstoqueRequest;
import projeto.OButecoBack_End.controller.estoque.dto.MovimentacoesEstoqueResponse;
import projeto.OButecoBack_End.model.service.estoque.*;

import java.util.List;


@RestController
@RequestMapping("/estoques")
public class EstoquesController {

    private final EstoquesService estoqueService;
    private final MovimentacoesEstoqueService movimentacoesEstoqueService;

    public EstoquesController(EstoquesService estoqueService, MovimentacoesEstoqueService movimentacoesEstoqueService) {
        this.estoqueService = estoqueService;
        this.movimentacoesEstoqueService = movimentacoesEstoqueService;
    }

    //--------------------------------------Estoque--------------------------------------//
    @GetMapping
    public ResponseEntity<List<EstoquesResponse>> listarEstoques() {
        List<EstoquesResponse> estoques = this.estoqueService.listarEstoques()
                .stream()
                .map(EstoquesResponse::de)
                .toList();
        return new ResponseEntity<>(estoques, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<EstoquesResponse> salvarEstoque(@Valid @RequestBody EstoquesRequest estoquesRequest) {
        var criacao = estoqueService.salvarEstoque(estoquesRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EstoquesResponse.de(criacao));
    }

    //------------------------------------Movimentacoes------------------------------------//
    @PostMapping("/movimentacoes/entrada")
    public ResponseEntity<MovimentacoesEstoqueResponse> entrada(
            @Valid @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest){
        var entrada = movimentacoesEstoqueService.entrada(movimentacoesEstoqueRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacoesEstoqueResponse.de(entrada));
    }

    @PostMapping("/movimentacoes/saida")
    public ResponseEntity<MovimentacoesEstoqueResponse> saida(
            @Valid @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        var saida = movimentacoesEstoqueService.saida(movimentacoesEstoqueRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacoesEstoqueResponse.de(saida));
    }

    @PostMapping("/movimentacoes/saida-insumo/{idProduto}")
    public ResponseEntity<Void> saidaInsumo(
            @PathVariable Long idProduto,
            @Valid @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        movimentacoesEstoqueService.saidaInsumo(idProduto, movimentacoesEstoqueRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/movimentacoes/{idMovimentacao}")
    public ResponseEntity<MovimentacoesEstoqueResponse> editarMovimentacao(
            @PathVariable Long idMovimentacao,
            @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        var editado = movimentacoesEstoqueService.editarMovimentacao(idMovimentacao, movimentacoesEstoqueRequest);
        return ResponseEntity.status(HttpStatus.OK).body(MovimentacoesEstoqueResponse.de(editado));
    }

    @DeleteMapping("/movimentacoes/{idMovimentacao}")
    public ResponseEntity<Void> deletarMovimentacao(@PathVariable Long idMovimentacao){
        movimentacoesEstoqueService.excluirMovimentacao(idMovimentacao);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/movimentacoes/historico")
    public ResponseEntity<List<MovimentacoesEstoqueResponse>> historicoMovimentacoes(){
        List<MovimentacoesEstoqueResponse> movimentacoes = this.movimentacoesEstoqueService.historicoMovimentacoes()
                .stream()
                .map(MovimentacoesEstoqueResponse::de)
                .toList();
        return new ResponseEntity<>(movimentacoes, HttpStatus.OK);
    }

    @GetMapping("/movimentacoes/historico/{idEstoque}")
    public ResponseEntity<List<MovimentacoesEstoqueResponse>> historicoMovimentacoesPorEstoque(@PathVariable Long idEstoque){
        List<MovimentacoesEstoqueResponse> movimentacoes = this.movimentacoesEstoqueService.historicoPorEstoque(idEstoque)
                .stream()
                .map(MovimentacoesEstoqueResponse::de)
                .toList();
        return new ResponseEntity<>(movimentacoes, HttpStatus.OK);
    }
}
