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

    @Transactional
    public MovimentacoesEstoqueEntity entrada(MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        log.info("Iniciando entrada de estoque. Estoque: {}, Produto: {}, Quantidade: {}, Usuário: {}",
                movimentacoesEstoqueRequest.fk_id_estoque(),
                movimentacoesEstoqueRequest.fk_id_produto(),
                movimentacoesEstoqueRequest.qtde(),
                movimentacoesEstoqueRequest.fk_id_usuario());

        //Validacao de usuario e estoque
        UsuariosEntity usuario = usuariosService.buscarUsuarioPorId(movimentacoesEstoqueRequest.fk_id_usuario());

        //Caso nao exista estoque cria um novo
        EstoquesEntity estoque = estoquesRepository.findById(movimentacoesEstoqueRequest.fk_id_estoque())
                .orElseGet(() -> {
                    if (movimentacoesEstoqueRequest.fk_id_produto() != null) {
                        EstoquesRequest estoqueRequest = new EstoquesRequest(
                                movimentacoesEstoqueRequest.fk_id_produto(),
                                0.0,  // Começa vazio
                                movimentacoesEstoqueRequest.fk_id_conversao(),
                                movimentacoesEstoqueRequest.local()
                        );
                        return estoquesService.salvarEstoque(estoqueRequest);
                    }
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado");
                });

        //Calcular quantidade com conversão
        BigDecimal qtdeSemConversao = BigDecimal.valueOf(movimentacoesEstoqueRequest.qtde());
        BigDecimal qtdeConvertida = qtdeSemConversao;

        ConversoesEntity conversaoEntrada = estoquesService.buscarConversaoPorId(movimentacoesEstoqueRequest.fk_id_conversao());
        ConversoesEntity conversoesEstoque = estoque.getConversoesEntity();

        if (!conversaoEntrada.getId().equals(conversoesEstoque.getId())) {
            qtdeConvertida = converterQuantidade(qtdeSemConversao, conversaoEntrada, conversoesEstoque);
        }

        //Atualiza o estoque
        BigDecimal estoqueAnterior = estoque.getQntdEstoque();
        estoque.setQntdEstoque(estoqueAnterior.add(qtdeConvertida));
        estoquesRepository.save(estoque);

        log.info("Estoque atualizado após entrada. Estoque: {}, Quantidade anterior: {}, Quantidade adicionada: {}, Nova quantidade: {}",
                estoque.getId(),
                estoqueAnterior,
                qtdeConvertida,
                estoque.getQntdEstoque());

        //Registra a mov
        MovimentacoesEstoqueEntity movimentacao = registrarMovimentacao(
                estoque,
                TIPO_ENTRADA,
                qtdeSemConversao,
                qtdeConvertida,
                usuario,
                movimentacoesEstoqueRequest,
                conversaoEntrada
        );

        log.info("Entrada registrada com sucesso. Movimentação: {}, Estoque: {}",
                movimentacao.getId(),
                estoque.getId());

        return movimentacao;
    }

    @Transactional
    public MovimentacoesEstoqueEntity saida(MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        log.info("Iniciando saída de estoque. Estoque: {}, Quantidade: {}, Usuário: {}",
                movimentacoesEstoqueRequest.fk_id_estoque(),
                movimentacoesEstoqueRequest.qtde(),
                movimentacoesEstoqueRequest.fk_id_usuario());

        //Validacao de usuario e estoque
        UsuariosEntity usuario = usuariosService.buscarUsuarioPorId(movimentacoesEstoqueRequest.fk_id_usuario());

        EstoquesEntity estoque = estoquesService.buscarEstoquePorId(movimentacoesEstoqueRequest.fk_id_estoque());

        //Calcular quantidade com conversão
        BigDecimal qtdeSemConversao = BigDecimal.valueOf(movimentacoesEstoqueRequest.qtde());
        BigDecimal qtdeConvertida = qtdeSemConversao;

        ConversoesEntity conversaoSaida = estoquesService.buscarConversaoPorId(movimentacoesEstoqueRequest.fk_id_conversao());
        ConversoesEntity conversoesEstoque = estoque.getConversoesEntity();

        if (!conversaoSaida.getId().equals(conversoesEstoque.getId())) {
            qtdeConvertida = converterQuantidade(
                    qtdeSemConversao,
                    conversaoSaida,
                    conversoesEstoque
            );
        }

        //Valida se tem a quantidade suficiente
        if (estoque.getQntdEstoque().compareTo(qtdeConvertida) < 0) {
            log.warn("Estoque insuficiente. Estoque: {}, Disponível: {}, Solicitado: {}",
                    estoque.getId(),
                    estoque.getQntdEstoque(),
                    qtdeConvertida);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade insuficiente no estoque. Disponível: " + estoque.getQntdEstoque() +
                            ", Solicitado: " + qtdeConvertida);
        }

        //Atualiza o estoque
        estoque.setQntdEstoque(estoque.getQntdEstoque().subtract(qtdeConvertida));
        estoquesRepository.save(estoque);

        log.info("Estoque atualizado após saída. Estoque: {}, Quantidade retirada: {}, Saldo: {}",
                estoque.getId(),
                qtdeConvertida,
                estoque.getQntdEstoque());

        //Registra a mov
        MovimentacoesEstoqueEntity movimentacao = registrarMovimentacao(
                estoque,
                TIPO_SAIDA,
                qtdeSemConversao,
                qtdeConvertida,
                usuario,
                movimentacoesEstoqueRequest,
                conversaoSaida);

        log.info(
                "Saída registrada com sucesso. Movimentação: {}, Estoque: {}",
                movimentacao.getId(),
                estoque.getId()
        );

        return movimentacao;
    }

    @Transactional
    public void saidaInsumo(Long idProduto, MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        log.info("Iniciando saída de insumos. Produto: {}, Quantidade solicitada: {}, Usuário: {}",
                idProduto,
                movimentacoesEstoqueRequest.qtde(),
                movimentacoesEstoqueRequest.fk_id_usuario());

        //Valida usuario
        UsuariosEntity usuario = usuariosService.buscarUsuarioPorId(movimentacoesEstoqueRequest.fk_id_usuario());

        //Valida produto principal
        ProdutosEntity produto = produtosService.buscarProdutoPorId(movimentacoesEstoqueRequest.fk_id_produto());

        //Busca insumos do produto principal
        List<InsumosProdutoEntity> insumos = insumosProdutoRepository.findAllByProdutosEntity(produto);

        if (insumos.isEmpty()) {
            log.warn("Produto não possui insumos cadastrados. Produto: {}",
                    produto.getId());

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Produto não possui insumos cadastrados"
            );
        }

        log.info("Produto {} possui {} insumo(s) cadastrado(s)",
                produto.getId(),
                insumos.size());

        // Para cada insumo realiza a saída
        for (InsumosProdutoEntity insumo : insumos) {
            EstoquesEntity estoqueInsumo =
                    estoquesRepository.findByProdutosEntity(insumo.getInsumos())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Estoque não encontrado para insumo: "
                                            + insumo.getInsumos().getNome()
                            ));

            // Quantidade necessária do insumo para produzir a quantidade solicitada
            // Ex.: 100g de queijo por pizza × 2 pizzas = 200g
            BigDecimal quantidadeNecessaria =
                    insumo.getQtde().multiply(BigDecimal.valueOf(movimentacoesEstoqueRequest.qtde()));

            BigDecimal quantidadeConvertida = converterQuantidade(
                    quantidadeNecessaria,
                    insumo.getConversoesEntity(),
                    estoqueInsumo.getConversoesEntity()
            );

            //Valida a disponibilidade
            if (estoqueInsumo.getQntdEstoque().compareTo(quantidadeConvertida) < 0) {
                log.warn("Estoque insuficiente para o insumo {}. Disponível: {}, Necessário: {}",
                        insumo.getInsumos().getNome(),
                        estoqueInsumo.getQntdEstoque(),
                        quantidadeConvertida);

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Quantidade insuficiente do insumo: " + insumo.getInsumos().getNome()
                                + ". Disponível: " + estoqueInsumo.getQntdEstoque() + " " + estoqueInsumo.getConversoesEntity().getNomenclatura()
                                + ", Necessário: " + quantidadeConvertida + " " + estoqueInsumo.getConversoesEntity().getNomenclatura());
            }

            //Retira a quantidade do estoque
            estoqueInsumo.setQntdEstoque(
                    estoqueInsumo.getQntdEstoque().subtract(quantidadeConvertida));

            estoquesRepository.save(estoqueInsumo);

            //Registra a mov
            MovimentacoesEstoqueEntity movimentacao = new MovimentacoesEstoqueEntity();
            movimentacao.setEstoqueEntity(estoqueInsumo);
            movimentacao.setUsuarioEntity(usuario);
            movimentacao.setTipo(TIPO_SAIDA_INSUMO);
            movimentacao.setQuantidade(quantidadeNecessaria);
            movimentacao.setQtdeConversao(quantidadeConvertida);

            BigDecimal valorUnitario = insumo.getInsumos().getPrecoVenda();
            BigDecimal valorTotal = quantidadeConvertida.multiply(valorUnitario);

            movimentacao.setValorUnitario(valorUnitario);
            movimentacao.setValorTotal(valorTotal);

            movimentacao.setConversoesEntity(insumo.getConversoesEntity());
            movimentacao.setProdutosEntity(insumo.getInsumos());
            movimentacao.setObservacao(
                    "Saída de insumo para: " + produto.getNome() +
                            " (Quantidade do produto: " + movimentacoesEstoqueRequest.qtde() + ")" +
                            (movimentacoesEstoqueRequest.observacao() != null ? " - " + movimentacoesEstoqueRequest.observacao() : ""));

            movimentacoesEstoqueRepository.save(movimentacao);

            log.info("Saída de insumo registrada. Produto principal: {}, Insumo: {}, Quantidade: {}",
                    produto.getNome(),
                    insumo.getInsumos().getNome(),
                    quantidadeConvertida);
        }

        log.info("Saída de insumos concluída com sucesso. Produto: {}",
                produto.getNome());
    }

    //para editar reverte a mov antiga e aplica a nova
    @Transactional
    public MovimentacoesEstoqueEntity editarMovimentacao(Long id, MovimentacoesEstoqueRequest request) {
        log.info("Iniciando edição de movimentação. ID: {}", id);

        MovimentacoesEstoqueEntity movimentacao = buscarMovimentacaoPorId(id);
        EstoquesEntity estoque = movimentacao.getEstoqueEntity();

        if (afetaEstoque(request)) {
            reverterMovimentacao(estoque, movimentacao);
            reaplicarMovimentacao(estoque, movimentacao, request);
        } else if (request.valorUnitario() != null) {
            atualizarValores(movimentacao, request.valorUnitario(), movimentacao.getQtdeConversao());
        }

        if (request.observacao() != null) {
            movimentacao.setObservacao(request.observacao());
        }

        estoquesRepository.save(estoque);
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

    //metodos get
    public List<MovimentacoesEstoqueEntity> historicoPorEstoque(Long id) {
        log.info("Buscando movimentações do estoque. ID: {}", id);

        List<MovimentacoesEstoqueEntity> movimentacoes =
                movimentacoesEstoqueRepository.findByEstoqueEntityIdOrderByDataMovimentacaoDesc(id);

        log.info("Movimentações encontradas para o estoque {}: {}", id, movimentacoes.size());

        return movimentacoes;
    }

    public List<MovimentacoesEstoqueEntity> historicoMovimentacoes(){
        log.info("Buscando todas as movimentações");

        List<MovimentacoesEstoqueEntity> movimentacoes = movimentacoesEstoqueRepository.findAll();

        log.info("Total de movimentações encontradas: {}", movimentacoes.size());

        return movimentacoes;
    }

    //metodo auxiliares
    public BigDecimal converterQuantidade(
            BigDecimal quantidade,
            ConversoesEntity origem,
            ConversoesEntity destino) {

        if (origem.getId().equals(destino.getId())) {
            return quantidade;
        }

        if (origem.getFatorBase() == null || destino.getFatorBase() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fator de conversão não cadastrado"
            );
        }

        return quantidade
                .multiply(origem.getFatorBase())
                .divide(destino.getFatorBase(), 4, RoundingMode.HALF_UP);
    }

    private MovimentacoesEstoqueEntity registrarMovimentacao(
            EstoquesEntity estoque,
            String tipo,
            BigDecimal qtde,
            BigDecimal qtdeConvertida,
            UsuariosEntity usuario,
            MovimentacoesEstoqueRequest request,
            ConversoesEntity conversao
    ) {

        MovimentacoesEstoqueEntity movimentacoesEstoqueEntity = new MovimentacoesEstoqueEntity();

        movimentacoesEstoqueEntity.setEstoqueEntity(estoque);
        movimentacoesEstoqueEntity.setTipo(tipo);
        movimentacoesEstoqueEntity.setQuantidade(qtde);
        movimentacoesEstoqueEntity.setQtdeConversao(qtdeConvertida);

        BigDecimal valorUnitario = BigDecimal.valueOf(request.valorUnitario());
        BigDecimal valorTotal = qtdeConvertida.multiply(valorUnitario);
        movimentacoesEstoqueEntity.setValorUnitario(valorUnitario);
        movimentacoesEstoqueEntity.setValorTotal(valorTotal);

        movimentacoesEstoqueEntity.setUsuarioEntity(usuario);
        movimentacoesEstoqueEntity.setConversoesEntity(conversao);
        movimentacoesEstoqueEntity.setObservacao(request.observacao());
        movimentacoesEstoqueEntity.setProdutosEntity(estoque.getProdutosEntity());

        return movimentacoesEstoqueRepository.save(movimentacoesEstoqueEntity);
    }

    public MovimentacoesEstoqueEntity buscarMovimentacaoPorId(Long id) {
        return movimentacoesEstoqueRepository.findById(id).orElseThrow
                (() -> {
                    log.warn("Movimentação não encontrada para edição. ID: {}", id);

                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Movimentação não encontrada com ID: " + id);
        });
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
        BigDecimal qtdeConvertida = converterParaConversaoDoEstoque(
                novaQtde, conversao, estoque.getConversoesEntity());

        aplicarNoEstoque(estoque, novoTipo, qtdeConvertida);

        movimentacao.setTipo(novoTipo);
        movimentacao.setQuantidade(novaQtde);
        movimentacao.setQtdeConversao(qtdeConvertida);
        movimentacao.setConversoesEntity(conversao);

        atualizarValores(movimentacao, request.valorUnitario(), qtdeConvertida);
    }

    public BigDecimal converterParaConversaoDoEstoque(BigDecimal qtde,
                                                       ConversoesEntity conversao,
                                                       ConversoesEntity conversaoEstoque) {
        if (conversao.getId().equals(conversaoEstoque.getId())) {
            return qtde;
        }
        return converterQuantidade(qtde, conversao, conversaoEstoque);
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
        if (estoque.getQntdEstoque().compareTo(qtdeConvertida) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantidade insuficiente no estoque. Disponível: " + estoque.getQntdEstoque()
                            + ", Solicitado: " + qtdeConvertida);
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