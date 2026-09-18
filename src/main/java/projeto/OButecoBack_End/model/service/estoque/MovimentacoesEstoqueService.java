package projeto.OButecoBack_End.model.service.estoque;

import jakarta.transaction.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.estoque.dto.*;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;
import projeto.OButecoBack_End.model.entity.estoque.*;
import projeto.OButecoBack_End.model.entity.produto.*;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.repository.estoque.*;
import projeto.OButecoBack_End.model.repository.produto.*;
import projeto.OButecoBack_End.model.repository.usuario.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MovimentacoesEstoqueService {

    private final MovimentacoesEstoqueRepository movimentacoesEstoqueRepository;
    private final EstoquesRepository estoquesRepository;
    private final ProdutosRepository produtosRepository;
    private final UsuariosRepository usuariosRepository;
    private final InsumosProdutoRepository insumosProdutoRepository;
    private final ConversoesRepository conversoesRepository;
    private final EstoquesService estoquesService;

    public MovimentacoesEstoqueService(MovimentacoesEstoqueRepository movimentacoesEstoqueRepository, EstoquesRepository estoquesRepository, ProdutosRepository produtosRepository, UsuariosRepository usuariosRepository, InsumosProdutoRepository insumosProdutoRepository, ConversoesRepository conversoesRepository, EstoquesService estoquesService) {
        this.movimentacoesEstoqueRepository = movimentacoesEstoqueRepository;
        this.estoquesRepository = estoquesRepository;
        this.produtosRepository = produtosRepository;
        this.usuariosRepository = usuariosRepository;
        this.insumosProdutoRepository = insumosProdutoRepository;
        this.conversoesRepository = conversoesRepository;
        this.estoquesService = estoquesService;
    }

    @Transactional
    public MovimentacoesEstoqueEntity entrada(MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        //Validacao de usuario e estoque
        UsuariosEntity usuario = usuariosRepository.findByIdAndDeletedAtIsNull(movimentacoesEstoqueRequest.fk_id_usuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado com ID: " + movimentacoesEstoqueRequest.fk_id_usuario()
                ));

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

        ConversoesEntity conversaoEntrada = conversoesRepository.findById(movimentacoesEstoqueRequest.fk_id_conversao())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversão não encontrada"));
        ConversoesEntity conversoesEstoque = estoque.getConversoesEntity();

        if (!conversaoEntrada.getId().equals(conversoesEstoque.getId())) {
            qtdeConvertida = converterQuantidade(qtdeSemConversao, conversaoEntrada, conversoesEstoque);
        }

        //Atualiza o estoque
        BigDecimal estoqueAnterior = estoque.getQntdEstoque();
        estoque.setQntdEstoque(estoqueAnterior.add(qtdeConvertida));
        estoquesRepository.save(estoque);

        //Registra a mov
        return registrarMovimentacao(estoque, "ENTRADA", qtdeSemConversao, qtdeConvertida, usuario, movimentacoesEstoqueRequest, conversaoEntrada);
    }

    @Transactional
    public MovimentacoesEstoqueEntity saida(MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        //Validacao de usuario e estoque
        UsuariosEntity usuario = usuariosRepository.findByIdAndDeletedAtIsNull(movimentacoesEstoqueRequest.fk_id_usuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado com ID: " + movimentacoesEstoqueRequest.fk_id_usuario()
                ));

        EstoquesEntity estoque = estoquesRepository.findById(movimentacoesEstoqueRequest.fk_id_estoque())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Estoque não encontrado com ID: " + movimentacoesEstoqueRequest.fk_id_estoque()
                ));

        //Calcular quantidade com conversão
        BigDecimal qtdeSemConversao = BigDecimal.valueOf(movimentacoesEstoqueRequest.qtde());
        BigDecimal qtdeConvertida = qtdeSemConversao;

        ConversoesEntity conversaoSaida =
                conversoesRepository.findById(movimentacoesEstoqueRequest.fk_id_conversao()
                ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Conversão não encontrada"));
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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade insuficiente no estoque. Disponível: " + estoque.getQntdEstoque() +
                            ", Solicitado: " + qtdeConvertida);
        }

        //Atualiza o estoque
        estoque.setQntdEstoque(estoque.getQntdEstoque().subtract(qtdeConvertida));
        estoquesRepository.save(estoque);

        //Registra a mov
        return registrarMovimentacao(estoque, "SAÍDA", qtdeSemConversao, qtdeConvertida, usuario, movimentacoesEstoqueRequest, conversaoSaida);
    }

    @Transactional
    public void saidaInsumo(Long fk_id_produto, MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        //Valida usuario
        UsuariosEntity usuario = usuariosRepository.findByIdAndDeletedAtIsNull(movimentacoesEstoqueRequest.fk_id_usuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado com ID: " + movimentacoesEstoqueRequest.fk_id_usuario()
                ));

        //Valida produto principal
        ProdutosEntity produto = produtosRepository.findByIdAndDeletedAtIsNull(fk_id_produto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado com ID: " + fk_id_produto
                ));

        //Busca insumos do produto principal
        List<InsumosProdutoEntity> insumos = insumosProdutoRepository.findAllByProdutosEntity(produto);

        if (insumos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Produto não possui insumos cadastrados"
            );
        }

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
            movimentacao.setTipo("SAIDA_INSUMO");
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
        }
    }

    //para editar reverte a mov antiga e aplica a nova
    @Transactional
    public MovimentacoesEstoqueEntity editarMovimentacao(Long id, MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        //Encontra a movimentação antiga
        MovimentacoesEstoqueEntity movimentacaoAntiga = movimentacoesEstoqueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Movimentação não encontrada com ID: " + id));

        EstoquesEntity estoque = movimentacaoAntiga.getEstoqueEntity();

        boolean mudouCampos =
                movimentacoesEstoqueRequest.tipo() != null ||
                        movimentacoesEstoqueRequest.qtde() != null ||
                        movimentacoesEstoqueRequest.fk_id_conversao() != null;

        if (mudouCampos) {
            //Reverte a movimentação antiga
            if ("ENTRADA".equals(movimentacaoAntiga.getTipo())) {
                estoque.setQntdEstoque(estoque.getQntdEstoque()
                                .subtract(movimentacaoAntiga.getQtdeConversao()));
            } else if ("SAIDA".equals(movimentacaoAntiga.getTipo())
                    || "SAIDA_INSUMO".equals(movimentacaoAntiga.getTipo())) {
                estoque.setQntdEstoque(estoque.getQntdEstoque()
                                .add(movimentacaoAntiga.getQtdeConversao()));
            }

            //Usa valores antigos como fallback
            String novoTipo = movimentacoesEstoqueRequest.tipo() != null ? movimentacoesEstoqueRequest.tipo() : movimentacaoAntiga.getTipo();
            BigDecimal novaQtde = movimentacoesEstoqueRequest.qtde() != null ? BigDecimal.valueOf(movimentacoesEstoqueRequest.qtde()) : movimentacaoAntiga.getQuantidade();
            Long novaConversaoId = movimentacoesEstoqueRequest.fk_id_conversao() != null ?
                    movimentacoesEstoqueRequest.fk_id_conversao() : movimentacaoAntiga.getConversoesEntity().getId();

            //Busca nova conversão
            ConversoesEntity conversao = conversoesRepository.findById(novaConversaoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversão não encontrada"));
            ConversoesEntity conversaoEstoque = estoque.getConversoesEntity();

            //Calcula quantidade convertida
            BigDecimal qtdeConvertida = novaQtde;

            if (!conversao.getId().equals(conversaoEstoque.getId())) {
                qtdeConvertida = converterQuantidade(
                        novaQtde,
                        conversao,
                        conversaoEstoque);
            }

            //Valida quantidade suficiente para saída
            if ("SAÍDA".equals(novoTipo) || "SAIDA_INSUMO".equals(novoTipo)) {
                if (estoque.getQntdEstoque().compareTo(qtdeConvertida) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Quantidade insuficiente no estoque. Disponível: " + estoque.getQntdEstoque()
                                    + ", Solicitado: " + qtdeConvertida);
                }
                estoque.setQntdEstoque(estoque.getQntdEstoque().subtract(qtdeConvertida));
            } else if ("ENTRADA".equals(novoTipo)) {
                estoque.setQntdEstoque(estoque.getQntdEstoque().add(qtdeConvertida));
            }

            //Atualiza campos da movimentação
            movimentacaoAntiga.setTipo(novoTipo);
            movimentacaoAntiga.setQuantidade(novaQtde);
            movimentacaoAntiga.setQtdeConversao(qtdeConvertida);
            movimentacaoAntiga.setConversoesEntity(conversao);

            //Atualiza valor se fornecido
            if (movimentacoesEstoqueRequest.valorUnitario() != null) {
                movimentacaoAntiga.setValorUnitario(BigDecimal.valueOf(movimentacoesEstoqueRequest.valorUnitario()));
                movimentacaoAntiga.setValorTotal(
                        qtdeConvertida.multiply(BigDecimal.valueOf(movimentacoesEstoqueRequest.valorUnitario())));
            } else {
                // Se não forneceu novo valor, mantém o antigo e recalcula
                movimentacaoAntiga.setValorTotal(qtdeConvertida
                        .multiply(movimentacaoAntiga.getValorUnitario()));
            }
        } else {
            //Se só está mudando observação/valor (sem afetar estoque)
            if (movimentacoesEstoqueRequest.valorUnitario() != null) {
                movimentacaoAntiga.setValorUnitario(BigDecimal.valueOf(movimentacoesEstoqueRequest.valorUnitario()));
                movimentacaoAntiga.setValorTotal(
                        movimentacaoAntiga.getQtdeConversao()
                                .multiply(BigDecimal.valueOf(
                                        movimentacoesEstoqueRequest.valorUnitario())));
            }
        }

        //Sempre atualiza observação se fornecida
        if (movimentacoesEstoqueRequest.observacao() != null) {
            movimentacaoAntiga.setObservacao(movimentacoesEstoqueRequest.observacao());
        }

        estoquesRepository.save(estoque);
        return movimentacoesEstoqueRepository.save(movimentacaoAntiga);
    }

    @Transactional
    public void excluirMovimentacao(Long id) {
        //encontra a mov
        MovimentacoesEstoqueEntity movimentacao = movimentacoesEstoqueRepository.findById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Movimentação não encontrada com ID: " + id));

        EstoquesEntity estoque = movimentacao.getEstoqueEntity();

        //reverte a mov
        if("ENTRADA".equals(movimentacao.getTipo())) {
            BigDecimal novaQuantidade = estoque.getQntdEstoque().subtract(movimentacao.getQtdeConversao());

            if (novaQuantidade.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Não é possível excluir a movimentação, pois a quantidade atual do estoque é insuficiente.");}

            estoque.setQntdEstoque(novaQuantidade);
        } else if ("SAÍDA".equals(movimentacao.getTipo()) || "SAIDA_INSUMO".equals(movimentacao.getTipo())) {
            estoque.setQntdEstoque(estoque.getQntdEstoque().add(movimentacao.getQtdeConversao()));
        }

        estoquesRepository.save(estoque);
        movimentacoesEstoqueRepository.delete(movimentacao);

    }

    //metodos get
    public List<MovimentacoesEstoqueEntity> historicoPorEstoque(Long id) {
        return movimentacoesEstoqueRepository.findByEstoqueEntityIdOrderByDataMovimentacaoDesc(id);
    }

    public List<MovimentacoesEstoqueEntity> historicoMovimentacoes(){
        return movimentacoesEstoqueRepository.findAll();
    }

    //metodo auxiliar para conversao
    private BigDecimal converterQuantidade(
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

    //metodo auxiliar para registrar cada mov
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
}