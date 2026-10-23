package projeto.obuteco.model.service.estoque;

import jakarta.transaction.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.estoque.dto.*;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.estoque.*;
import projeto.obuteco.model.entity.produto.*;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.repository.estoque.*;
import projeto.obuteco.model.repository.produto.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projeto.obuteco.model.service.produto.ProdutosService;
import projeto.obuteco.model.service.usuario.UsuariosService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class MovimentacoesEstoqueService {
    private static final Logger log = LoggerFactory.getLogger(MovimentacoesEstoqueService.class);

    private static final String TIPO_ENTRADA = "ENTRADA";
    private static final String TIPO_SAIDA = "SAIDA";
    private static final String TIPO_SAIDA_INSUMO = "SAIDA_INSUMO";

    private final MovimentacoesEstoqueRepository movimentacoesEstoqueRepository;
    private final EstoquesRepository estoquesRepository;
    private final ProdutosService produtosService;
    private final InsumosProdutoRepository insumosProdutoRepository;
    private final EstoquesService estoquesService;
    private final UsuariosService usuariosService;

    public MovimentacoesEstoqueService(MovimentacoesEstoqueRepository movimentacoesEstoqueRepository, EstoquesRepository estoquesRepository, ProdutosService produtosService, InsumosProdutoRepository insumosProdutoRepository, EstoquesService estoquesService, UsuariosService usuariosService) {
        this.movimentacoesEstoqueRepository = movimentacoesEstoqueRepository;
        this.estoquesRepository = estoquesRepository;
        this.produtosService = produtosService;
        this.insumosProdutoRepository = insumosProdutoRepository;
        this.estoquesService = estoquesService;
        this.usuariosService = usuariosService;
    }

    public record DadosMovimentacao(String tipo,
                                    BigDecimal qtde,
                                    BigDecimal qtdeConvertida,
                                    UsuariosEntity usuario,
                                    BigDecimal valorUnitario,
                                    String observacao,
                                    ConversoesEntity conversao) {}

    // ===================== OPERAÇÕES PRINCIPAIS =====================

    @Transactional
    public MovimentacoesEstoqueEntity entrada(MovimentacoesEstoqueRequest request) {
        log.info("Iniciando entrada de estoque. Estoque: {}, Produto: {}, Quantidade: {}, Usuário: {}",
                request.fk_id_estoque(),
                request.fk_id_produto(),
                request.qtde(),
                request.fk_id_usuario());

        return movimentar(TIPO_ENTRADA, request, () ->
                estoquesRepository.findById(request.fk_id_estoque())
                        .orElseGet(() -> criarEstoqueSeProdutoInformado(request)));
    }

    @Transactional
    public MovimentacoesEstoqueEntity saida(MovimentacoesEstoqueRequest request) {
        log.info("Iniciando saída de estoque. Estoque: {}, Quantidade: {}, Usuário: {}",
                request.fk_id_estoque(),
                request.qtde(),
                request.fk_id_usuario());

        return movimentar(TIPO_SAIDA, request, () ->
                estoquesService.buscarEstoquePorId(request.fk_id_estoque()));
    }

    @Transactional
    public void saidaInsumo(Long idProduto, MovimentacoesEstoqueRequest request) {
        log.info("Iniciando saída de insumos. Produto: {}, Quantidade solicitada: {}, Usuário: {}",
                idProduto,
                request.qtde(),
                request.fk_id_usuario());

        UsuariosEntity usuario = usuariosService.buscarUsuarioPorId(request.fk_id_usuario());
        ProdutosEntity produto = produtosService.buscarProdutoPorId(request.fk_id_produto());

        List<InsumosProdutoEntity> insumos = insumosProdutoRepository.findAllByProdutosEntity(produto);

        if (insumos.isEmpty()) {
            log.warn("Produto não possui insumos cadastrados. Produto: {}", produto.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto não possui insumos cadastrados");
        }

        log.info("Produto {} possui {} insumo(s) cadastrado(s)", produto.getId(), insumos.size());

        for (InsumosProdutoEntity insumo : insumos) {
            EstoquesEntity estoqueInsumo = estoquesRepository.findByProdutosEntity(insumo.getInsumos())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Estoque não encontrado para insumo: " + insumo.getInsumos().getNome()));

            // Ex.: 100g de queijo por pizza × 2 pizzas = 200g
            BigDecimal quantidadeNecessaria =
                    insumo.getQtde().multiply(BigDecimal.valueOf(request.qtde()));

            BigDecimal quantidadeConvertida = converterQuantidade(
                    quantidadeNecessaria,
                    insumo.getConversoesEntity(),
                    estoqueInsumo.getConversoesEntity());

            validarSaldoSuficiente(
                    estoqueInsumo,
                    quantidadeConvertida,
                    "Quantidade insuficiente do insumo: " + insumo.getInsumos().getNome(),
                    estoqueInsumo.getConversoesEntity().getNomenclatura());

            estoqueInsumo.setQntdEstoque(estoqueInsumo.getQntdEstoque().subtract(quantidadeConvertida));
            estoquesRepository.save(estoqueInsumo);

            String observacao = "Saída de insumo para: " + produto.getNome() +
                    " (Quantidade do produto: " + request.qtde() + ")" +
                    (request.observacao() != null ? " - " + request.observacao() : "");

            registrarMovimentacao(
                    estoqueInsumo,
                    new DadosMovimentacao(
                            TIPO_SAIDA_INSUMO,
                            quantidadeNecessaria,
                            quantidadeConvertida,
                            usuario,
                            insumo.getInsumos().getPrecoVenda(),
                            observacao,
                            insumo.getConversoesEntity()));

            log.info("Saída de insumo registrada. Produto principal: {}, Insumo: {}, Quantidade: {}",
                    produto.getNome(),
                    insumo.getInsumos().getNome(),
                    quantidadeConvertida);
        }

        log.info("Saída de insumos concluída com sucesso. Produto: {}", produto.getNome());
    }

    // para editar reverte a mov antiga e aplica a nova
    @Transactional
    public MovimentacoesEstoqueEntity editarMovimentacao(Long id, MovimentacoesEstoqueRequest request) {
        log.info("Iniciando edição de movimentação. ID: {}", id);

        MovimentacoesEstoqueEntity movimentacao = buscarMovimentacaoPorId(id);
        EstoquesEntity estoque = movimentacao.getEstoqueEntity();

        if (afetaEstoque(request)) {
            reverterMovimentacao(estoque, movimentacao);
            reaplicarMovimentacao(estoque, movimentacao, request);
            estoquesRepository.save(estoque);
        } else if (request.valorUnitario() != null) {
            atualizarValores(movimentacao, request.valorUnitario(), movimentacao.getQtdeConversao());
        }

        if (request.observacao() != null) {
            movimentacao.setObservacao(request.observacao());
        }

        MovimentacoesEstoqueEntity movimentacaoEditada = movimentacoesEstoqueRepository.save(movimentacao);

        log.info("Movimentação editada com sucesso. ID: {}, Tipo: {}, Quantidade: {}",
                movimentacaoEditada.getId(),
                movimentacaoEditada.getTipo(),
                movimentacaoEditada.getQuantidade());

        return movimentacaoEditada;
    }

    @Transactional
    public void excluirMovimentacao(Long id) {
        log.info("Iniciando exclusão da movimentação. ID: {}", id);

        MovimentacoesEstoqueEntity movimentacao = buscarMovimentacaoPorId(id);
        EstoquesEntity estoque = movimentacao.getEstoqueEntity();

        log.info("Revertendo movimentação. ID: {}, Tipo: {}, Quantidade: {}",
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getQtdeConversao());

        reverterMovimentacao(estoque, movimentacao);

        estoquesRepository.save(estoque);
        movimentacoesEstoqueRepository.delete(movimentacao);

        log.info("Movimentação excluída com sucesso. ID: {}", id);
    }

    // ===================== CONSULTAS =====================

    public List<MovimentacoesEstoqueEntity> historicoPorEstoque(Long id) {
        log.info("Buscando movimentações do estoque. ID: {}", id);

        List<MovimentacoesEstoqueEntity> movimentacoes =
                movimentacoesEstoqueRepository.findByEstoqueEntityIdOrderByDataMovimentacaoDesc(id);

        log.info("Movimentações encontradas para o estoque {}: {}", id, movimentacoes.size());

        return movimentacoes;
    }

    public List<MovimentacoesEstoqueEntity> historicoMovimentacoes() {
        log.info("Buscando todas as movimentações");

        List<MovimentacoesEstoqueEntity> movimentacoes = movimentacoesEstoqueRepository.findAll();

        log.info("Total de movimentações encontradas: {}", movimentacoes.size());

        return movimentacoes;
    }

    public MovimentacoesEstoqueEntity buscarMovimentacaoPorId(Long id) {
        return movimentacoesEstoqueRepository.findById(id).orElseThrow(() -> {
            log.warn("Movimentação não encontrada. ID: {}", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Movimentação não encontrada com ID: " + id);
        });
    }

    // ===================== AUXILIARES =====================

    public BigDecimal converterQuantidade(BigDecimal quantidade, ConversoesEntity origem, ConversoesEntity destino) {
        if (origem.getId().equals(destino.getId())) {
            return quantidade;
        }

        if (origem.getFatorBase() == null || destino.getFatorBase() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fator de conversão não cadastrado");
        }

        return quantidade
                .multiply(origem.getFatorBase())
                .divide(destino.getFatorBase(), 4, RoundingMode.HALF_UP);
    }


    public MovimentacoesEstoqueEntity movimentar(String tipo,
                                                  MovimentacoesEstoqueRequest request,
                                                  Supplier<EstoquesEntity> estoqueSupplier) {
        UsuariosEntity usuario = usuariosService.buscarUsuarioPorId(request.fk_id_usuario());
        EstoquesEntity estoque = estoqueSupplier.get();

        ConversoesEntity conversao = estoquesService.buscarConversaoPorId(request.fk_id_conversao());
        BigDecimal qtde = BigDecimal.valueOf(request.qtde());
        BigDecimal qtdeConvertida = converterQuantidade(qtde, conversao, estoque.getConversoesEntity());

        BigDecimal saldoAnterior = estoque.getQntdEstoque();
        aplicarNoEstoque(estoque, tipo, qtdeConvertida);
        estoquesRepository.save(estoque);

        log.info("Estoque atualizado ({}). Estoque: {}, Saldo anterior: {}, Quantidade: {}, Novo saldo: {}",
                tipo,
                estoque.getId(),
                saldoAnterior,
                qtdeConvertida,
                estoque.getQntdEstoque());

        MovimentacoesEstoqueEntity movimentacao = registrarMovimentacao(
                estoque,
                new DadosMovimentacao(
                        tipo,
                        qtde,
                        qtdeConvertida,
                        usuario,
                        BigDecimal.valueOf(request.valorUnitario()),
                        request.observacao(),
                        conversao));

        log.info("{} registrada com sucesso. Movimentação: {}, Estoque: {}",
                tipo, movimentacao.getId(), estoque.getId());

        return movimentacao;
    }

    public EstoquesEntity criarEstoqueSeProdutoInformado(MovimentacoesEstoqueRequest request) {
        if (request.fk_id_produto() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado");
        }

        EstoquesRequest estoqueRequest = new EstoquesRequest(
                request.fk_id_produto(),
                0.0, // Começa vazio
                request.fk_id_conversao(),
                request.local());

        return estoquesService.salvarEstoque(estoqueRequest);
    }

    public MovimentacoesEstoqueEntity registrarMovimentacao(EstoquesEntity estoque, DadosMovimentacao dados) {
        MovimentacoesEstoqueEntity movimentacao = new MovimentacoesEstoqueEntity();

        movimentacao.setEstoqueEntity(estoque);
        movimentacao.setTipo(dados.tipo());
        movimentacao.setQuantidade(dados.qtde());
        movimentacao.setQtdeConversao(dados.qtdeConvertida());
        movimentacao.setValorUnitario(dados.valorUnitario());
        movimentacao.setValorTotal(dados.qtdeConvertida().multiply(dados.valorUnitario()));
        movimentacao.setUsuarioEntity(dados.usuario());
        movimentacao.setConversoesEntity(dados.conversao());
        movimentacao.setObservacao(dados.observacao());
        movimentacao.setProdutosEntity(estoque.getProdutosEntity());

        return movimentacoesEstoqueRepository.save(movimentacao);
    }

    public boolean afetaEstoque(MovimentacoesEstoqueRequest request) {
        return request.tipo() != null
                || request.qtde() != null
                || request.fk_id_conversao() != null;
    }

    public boolean ehSaida(String tipo) {
        return TIPO_SAIDA.equals(tipo) || TIPO_SAIDA_INSUMO.equals(tipo);
    }

    public void reverterMovimentacao(EstoquesEntity estoque, MovimentacoesEstoqueEntity movimentacao) {
        BigDecimal saldo = estoque.getQntdEstoque();
        BigDecimal qtde = movimentacao.getQtdeConversao();

        if (TIPO_ENTRADA.equals(movimentacao.getTipo())) {
            BigDecimal novoSaldo = saldo.subtract(qtde);

            if (novoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Não é possível reverter a movimentação, pois a quantidade atual do estoque é insuficiente.");
            }

            estoque.setQntdEstoque(novoSaldo);
        } else if (ehSaida(movimentacao.getTipo())) {
            estoque.setQntdEstoque(saldo.add(qtde));
        }
    }

    public void reaplicarMovimentacao(EstoquesEntity estoque,
                                       MovimentacoesEstoqueEntity movimentacao,
                                       MovimentacoesEstoqueRequest request) {
        String novoTipo = Optional.ofNullable(request.tipo())
                .orElse(movimentacao.getTipo());

        BigDecimal novaQtde = Optional.ofNullable(request.qtde())
                .map(BigDecimal::valueOf)
                .orElse(movimentacao.getQuantidade());

        Long novaConversaoId = Optional.ofNullable(request.fk_id_conversao())
                .orElse(movimentacao.getConversoesEntity().getId());

        ConversoesEntity conversao = estoquesService.buscarConversaoPorId(novaConversaoId);
        BigDecimal qtdeConvertida = converterQuantidade(novaQtde, conversao, estoque.getConversoesEntity());

        aplicarNoEstoque(estoque, novoTipo, qtdeConvertida);

        movimentacao.setTipo(novoTipo);
        movimentacao.setQuantidade(novaQtde);
        movimentacao.setQtdeConversao(qtdeConvertida);
        movimentacao.setConversoesEntity(conversao);

        atualizarValores(movimentacao, request.valorUnitario(), qtdeConvertida);
    }

    public void aplicarNoEstoque(EstoquesEntity estoque, String tipo, BigDecimal qtdeConvertida) {
        if (ehSaida(tipo)) {
            validarSaldoSuficiente(estoque, qtdeConvertida);
            estoque.setQntdEstoque(estoque.getQntdEstoque().subtract(qtdeConvertida));
        } else if (TIPO_ENTRADA.equals(tipo)) {
            estoque.setQntdEstoque(estoque.getQntdEstoque().add(qtdeConvertida));
        }
    }

    public void validarSaldoSuficiente(EstoquesEntity estoque, BigDecimal qtdeConvertida) {
        validarSaldoSuficiente(estoque, qtdeConvertida, "Quantidade insuficiente no estoque", null);
    }

    public void validarSaldoSuficiente(EstoquesEntity estoque,
                                        BigDecimal qtdeConvertida,
                                        String mensagem,
                                        String unidade) {
        if (estoque.getQntdEstoque().compareTo(qtdeConvertida) < 0) {
            String sufixo = unidade != null ? " " + unidade : "";

            log.warn("Estoque insuficiente. Estoque: {}, Disponível: {}, Solicitado: {}",
                    estoque.getId(),
                    estoque.getQntdEstoque(),
                    qtdeConvertida);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    mensagem + ". Disponível: " + estoque.getQntdEstoque() + sufixo
                            + ", Solicitado: " + qtdeConvertida + sufixo);
        }
    }

    public void atualizarValores(MovimentacoesEstoqueEntity movimentacao,
                                  Double valorUnitarioRequest,
                                  BigDecimal qtdeConvertida) {

        BigDecimal valorUnitario = Optional.ofNullable(valorUnitarioRequest)
                .map(BigDecimal::valueOf)
                .orElse(movimentacao.getValorUnitario());

        movimentacao.setValorUnitario(valorUnitario);
        movimentacao.setValorTotal(qtdeConvertida.multiply(valorUnitario));
    }
}