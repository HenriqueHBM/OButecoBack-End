package projeto.obuteco.controller.estoque;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import projeto.obuteco.controller.estoque.dto.EstoquesRequest;
import projeto.obuteco.controller.estoque.dto.EstoquesResponse;
import projeto.obuteco.controller.estoque.dto.MovimentacoesEstoqueRequest;
import projeto.obuteco.controller.estoque.dto.MovimentacoesEstoqueResponse;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.service.estoque.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@RestController
@RequestMapping("/estoques")
public class EstoquesController {

    private static final Logger log = LoggerFactory.getLogger(EstoquesController.class);

    private final EstoquesService estoqueService;
    private final MovimentacoesEstoqueService movimentacoesEstoqueService;

    public EstoquesController(EstoquesService estoqueService, MovimentacoesEstoqueService movimentacoesEstoqueService) {
        this.estoqueService = estoqueService;
        this.movimentacoesEstoqueService = movimentacoesEstoqueService;
    }

    //--------------------------------------Estoque--------------------------------------//
    @GetMapping
    @PreAuthorize("hasRole('ESTAGIARIO')")
    public ResponseEntity<List<EstoquesResponse>> listarEstoques() {
        log.info("Listar estoques");

        List<EstoquesResponse> estoques = this.estoqueService.listarEstoques()
                .stream()
                .map(EstoquesResponse::de)
                .toList();

        log.info("Consulta de estoques concluida. Total de estoques : {}", estoques.size());

        return new ResponseEntity<>(estoques, HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EstoquesResponse> salvarEstoque(@Valid @RequestBody EstoquesRequest estoquesRequest) {
        log.info("Iniciando cadastro de estoques. Produto: {}, Quantidade: {}",
                estoquesRequest.fk_id_produto(),
                estoquesRequest.qtdeEstoque() );

        var criacao = estoqueService.salvarEstoque(estoquesRequest);

        log.info("Estoque cadastrado com sucesso. ID: {}, Produto: {}, Quantidade: {}",
                criacao.getId(),
                criacao.getProdutosEntity().getId(),
                criacao.getQntdEstoque());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EstoquesResponse.de(criacao));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ESTAGIARIO')")
    public ResponseEntity<EstoquesResponse> buscarPorId (@PathVariable Long id) {
        EstoquesEntity estoque = this.estoqueService.buscarEstoquePorId(id);
        return new ResponseEntity<>(EstoquesResponse.de(estoque), HttpStatus.OK);
    }

    //------------------------------------Movimentacoes------------------------------------//
    @PostMapping("/movimentacoes/entrada")
    @PreAuthorize("hasRole('AUXILIAR')")
    public ResponseEntity<MovimentacoesEstoqueResponse> entrada(
            @Valid @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest,
            @AuthenticationPrincipal UsuariosEntity usuarioLogado){
        log.info("Solicitação de entrada de estoque. Estoque: {}, Quantidade: {}, Usuário: {}",
                movimentacoesEstoqueRequest.fk_id_estoque(),
                movimentacoesEstoqueRequest.qtde(),
                usuarioLogado.getId());

        var entrada = movimentacoesEstoqueService.entrada(movimentacoesEstoqueRequest, usuarioLogado.getId());

        log.info("Entrada de estoque realizada com sucesso. Movimentação: {}", entrada.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacoesEstoqueResponse.de(entrada));
    }

    @PostMapping("/movimentacoes/saida")
    @PreAuthorize("hasRole('AUXILIAR')")
    public ResponseEntity<MovimentacoesEstoqueResponse> saida(
            @Valid @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest,
            @AuthenticationPrincipal UsuariosEntity usuarioLogado) {
        log.info("Solicitação de saída de estoque. Estoque: {}, Quantidade: {}, Usuário: {}",
                movimentacoesEstoqueRequest.fk_id_estoque(),
                movimentacoesEstoqueRequest.qtde(),
                usuarioLogado.getId());

        var saida = movimentacoesEstoqueService.saida(movimentacoesEstoqueRequest, usuarioLogado.getId());

        log.info("Saída de estoque realizada com sucesso. Movimentação: {}", saida.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimentacoesEstoqueResponse.de(saida));
    }

    @PostMapping("/movimentacoes/saida-insumo/{idProduto}")
    @PreAuthorize("hasRole('AUXILIAR')")
    public ResponseEntity<Void> saidaInsumo(
            @PathVariable Long idProduto,
            @Valid @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest,
            @AuthenticationPrincipal UsuariosEntity usuarioLogado) {
        log.info("Solicitação de saída de insumos. Produto: {}, Quantidade: {}, Usuário: {}",
                idProduto,
                movimentacoesEstoqueRequest.qtde(),
                usuarioLogado.getId());

        movimentacoesEstoqueService.saidaInsumo(idProduto, movimentacoesEstoqueRequest, usuarioLogado.getId());

        log.info("Saída de insumos realizada com sucesso. Produto: {}", idProduto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/movimentacoes/{idMovimentacao}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<MovimentacoesEstoqueResponse> editarMovimentacao(
            @PathVariable Long idMovimentacao,
            @RequestBody MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        log.info("Solicitação de edição de movimentação. ID: {}", idMovimentacao);

        var editado = movimentacoesEstoqueService.editarMovimentacao(idMovimentacao, movimentacoesEstoqueRequest);

        log.info("Movimentação editada com sucesso. ID: {}", editado.getId());

        return ResponseEntity.status(HttpStatus.OK).body(MovimentacoesEstoqueResponse.de(editado));
    }

    @DeleteMapping("/movimentacoes/{idMovimentacao}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> deletarMovimentacao(@PathVariable Long idMovimentacao){
        log.info("Solicitação de exclusão de movimentação. ID: {}", idMovimentacao);

        movimentacoesEstoqueService.excluirMovimentacao(idMovimentacao);

        log.info("Movimentação excluída com sucesso. ID: {}", idMovimentacao);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/movimentacoes/historico")
    @PreAuthorize("hasRole('ESTAGIARIO')")
    public ResponseEntity<List<MovimentacoesEstoqueResponse>> historicoMovimentacoes(){
        log.info("Solicitação de consulta do histórico de movimentações");

        List<MovimentacoesEstoqueResponse> movimentacoes = this.movimentacoesEstoqueService.historicoMovimentacoes()
                .stream()
                .map(MovimentacoesEstoqueResponse::de)
                .toList();

        log.info("Histórico de movimentações consultado. Total: {}", movimentacoes.size());

        return new ResponseEntity<>(movimentacoes, HttpStatus.OK);
    }

    @GetMapping("/movimentacoes/historico/{idEstoque}")
    @PreAuthorize("hasRole('ESTAGIARIO')")
    public ResponseEntity<List<MovimentacoesEstoqueResponse>> historicoMovimentacoesPorEstoque(@PathVariable Long idEstoque){
        log.info("Solicitação de consulta do histórico. Estoque: {}", idEstoque);

        List<MovimentacoesEstoqueResponse> movimentacoes = this.movimentacoesEstoqueService.historicoPorEstoque(idEstoque)
                .stream()
                .map(MovimentacoesEstoqueResponse::de)
                .toList();

        log.info("Histórico consultado. Estoque: {}, Total: {}", idEstoque, movimentacoes.size());

        return new ResponseEntity<>(movimentacoes, HttpStatus.OK);
    }
}
