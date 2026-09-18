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
        double qtdeSemConversao = movimentacoesEstoqueRequest.qtde();
        double qtdeConvertida = qtdeSemConversao;

        ConversoesEntity conversaoEntrada = conversoesRepository.findById(movimentacoesEstoqueRequest.fk_id_conversao())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversão não encontrada"));
        ConversoesEntity conversoesEstoque = estoque.getConversoesEntity();

        if (!conversaoEntrada.getId().equals(conversoesEstoque.getId())) {
            if (movimentacoesEstoqueRequest.taxaConversao() == null
                    || movimentacoesEstoqueRequest.taxaConversao() <= 0) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Taxa de conversão inválida");
            }

            qtdeConvertida = movimentacoesEstoqueRequest.qtde() * movimentacoesEstoqueRequest.taxaConversao();
        }

        //Atualiza o estoque
        BigDecimal estoqueAnterior = estoque.getQntdEstoque();
        estoque.setQntdEstoque(estoqueAnterior.add(new BigDecimal(qtdeConvertida)));
        estoquesRepository.save(estoque);

        //Registra a mov
        return registrarMovimentacao(estoque, "ENTRADA", qtdeSemConversao, usuario, movimentacoesEstoqueRequest, conversaoEntrada);
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
        double qtdeSemConversao = movimentacoesEstoqueRequest.qtde();
        double qtdeConvertida = qtdeSemConversao;

        ConversoesEntity conversaoSaida = conversoesRepository.findById(movimentacoesEstoqueRequest.fk_id_conversao())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversão não encontrada"));
        ConversoesEntity conversoesEstoque = estoque.getConversoesEntity();

        if (!conversaoSaida.getId().equals(conversoesEstoque.getId())) {
            if (movimentacoesEstoqueRequest.taxaConversao() == null
                    || movimentacoesEstoqueRequest.taxaConversao() <= 0) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Taxa de conversão inválida");
            }

            qtdeConvertida = qtdeSemConversao * movimentacoesEstoqueRequest.taxaConversao();
        }

        //Valida se tem a quantidade suficiente
        if (estoque.getQntdEstoque().compareTo(new BigDecimal(qtdeConvertida)) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade insuficiente no estoque. Disponível: " +
                            estoque.getQntdEstoque() + ", Solicitado: " + qtdeConvertida
            );
        }

        //Atualiza o estoque
        estoque.setQntdEstoque(estoque.getQntdEstoque().subtract(new BigDecimal(qtdeConvertida)));
        estoquesRepository.save(estoque);

        //Registra a mov
        return registrarMovimentacao(estoque, "SAÍDA", qtdeSemConversao, usuario, movimentacoesEstoqueRequest, conversaoSaida);
    }

    @Transactional
    public void saidaInsumo(Long fk_id_produto, MovimentacoesEstoqueRequest movimentacoesEstoqueRequest) {
        System.out.println("1 - entrou na service");

        //Valida usuario
        UsuariosEntity usuario = usuariosRepository.findByIdAndDeletedAtIsNull(movimentacoesEstoqueRequest.fk_id_usuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado com ID: " + movimentacoesEstoqueRequest.fk_id_usuario()
                ));

        System.out.println("2 - usuário encontrado: " + usuario.getId());


        //Valida produto principal
        ProdutosEntity produto = produtosRepository.findByIdAndDeletedAtIsNull(fk_id_produto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado com ID: " + fk_id_produto
                ));

        System.out.println("3 - produto encontrado: " + produto.getId());


        //Busca insumos do produto principal
        List<InsumosProdutoEntity> insumos = insumosProdutoRepository.findAllByProdutosEntity(produto);

        System.out.println("4 - quantidade de insumos: " + insumos.size());


        if (insumos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Produto não possui insumos cadastrados"
            );
        }

        // Para cada insumo realiza a saída
        for (InsumosProdutoEntity insumo : insumos) {

            System.out.println(
                    "5 - procurando estoque do insumo: "
                            + insumo.getInsumos().getNome()
            );

            EstoquesEntity estoqueInsumo =
                    estoquesRepository.findByProdutosEntity(insumo.getInsumos())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Estoque não encontrado para insumo: "
                                            + insumo.getInsumos().getNome()
                            ));

            System.out.println(
                    "6 - estoque encontrado: "
                            + estoqueInsumo.getId()
            );

            // Quantidade necessária do insumo para produzir a quantidade solicitada
            // Ex.: 100g de queijo por pizza × 2 pizzas = 200g
            BigDecimal quantidadeNecessaria = insumo.getQtde().multiply(BigDecimal.valueOf(movimentacoesEstoqueRequest.qtde()));

            //Valida a disponibilidade
            if (estoqueInsumo.getQntdEstoque()
                    .compareTo(quantidadeNecessaria) < 0) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quantidade insuficiente do insumo: " + insumo.getInsumos().getNome()
                                + ". Disponível: " + estoqueInsumo.getQntdEstoque()
                                + ", Necessário: " + quantidadeNecessaria);
            }

            //Retira a quantidade do estoque
            estoqueInsumo.setQntdEstoque(
                    estoqueInsumo.getQntdEstoque()
                            .subtract(quantidadeNecessaria));

            estoquesRepository.save(estoqueInsumo);

            //Registra a mov
            MovimentacoesEstoqueEntity movimentacao = new MovimentacoesEstoqueEntity();
            movimentacao.setEstoqueEntity(estoqueInsumo);
            movimentacao.setTipo("SAIDA_INSUMO");
            movimentacao.setQuantidade(quantidadeNecessaria);
            BigDecimal valorUnitario = insumo.getInsumos().getPrecoVenda();
            BigDecimal valorTotal = quantidadeNecessaria.multiply(valorUnitario);


            movimentacao.setValorUnitario(valorUnitario);
            movimentacao.setValorTotal(valorTotal);

            movimentacao.setConversoesEntity(estoqueInsumo.getConversoesEntity());
            movimentacao.setProdutosEntity(insumo.getInsumos());
            movimentacao.setObservacao(
                    "Saída de insumo para: " + produto.getNome() +
                            " (Quantidade do produto: " + movimentacoesEstoqueRequest.qtde() + ")" +
                            (movimentacoesEstoqueRequest.observacao() != null ? " - " + movimentacoesEstoqueRequest.observacao() : ""));

            movimentacoesEstoqueRepository.save(movimentacao);
            System.out.println("7 - terminou a service");
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
                        .subtract(movimentacaoAntiga.getQuantidade()));
            } else if ("SAIDA".equals(movimentacaoAntiga.getTipo()) || "SAIDA_INSUMO".equals(movimentacaoAntiga.getTipo())) {
                estoque.setQntdEstoque(estoque.getQntdEstoque()
                        .add((movimentacaoAntiga.getQuantidade())));
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
                if (movimentacoesEstoqueRequest.taxaConversao() == null
                        || movimentacoesEstoqueRequest.taxaConversao() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Taxa de conversão inválida");
                }

                qtdeConvertida = novaQtde
                        .multiply(BigDecimal.valueOf(movimentacoesEstoqueRequest.taxaConversao()));
            }

            //Valida quantidade suficiente para saída
            if ("SAIDA".equals(novoTipo) || "SAIDA_INSUMO".equals(novoTipo)) {
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
                        movimentacaoAntiga.getQuantidade()
                                .multiply(BigDecimal.valueOf(movimentacoesEstoqueRequest.valorUnitario())));
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
            BigDecimal novaQuantidade = estoque.getQntdEstoque().subtract(movimentacao.getQuantidade());

            if (novaQuantidade.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Não é possível excluir a movimentação, pois a quantidade atual do estoque é insuficiente.");}

            estoque.setQntdEstoque(novaQuantidade);
        } else if ("SAIDA".equals(movimentacao.getTipo()) || "SAIDA_INSUMO".equals(movimentacao.getTipo())) {
            estoque.setQntdEstoque(estoque.getQntdEstoque().add(movimentacao.getQuantidade()));
        }

        estoquesRepository.save(estoque);
        movimentacoesEstoqueRepository.delete(movimentacao);

    }

    public List<MovimentacoesEstoqueEntity> historicoEstoque(Long id) {
        return movimentacoesEstoqueRepository.findByEstoqueEntityIdOrderByDataMovimentacaoDesc(id);
    }


    //metodo auxiliar para registrar cada mov
    private MovimentacoesEstoqueEntity registrarMovimentacao(
            EstoquesEntity estoque,
            String tipo,
            double qtde,
            UsuariosEntity usuario,
            MovimentacoesEstoqueRequest request,
            ConversoesEntity conversao
    ) {

        MovimentacoesEstoqueEntity movimentacoesEstoqueEntity = new MovimentacoesEstoqueEntity();

        movimentacoesEstoqueEntity.setEstoqueEntity(estoque);
        movimentacoesEstoqueEntity.setTipo(tipo);
        movimentacoesEstoqueEntity.setQuantidade(BigDecimal.valueOf(qtde));
        movimentacoesEstoqueEntity.setValorUnitario(BigDecimal.valueOf(request.valorUnitario()));
        movimentacoesEstoqueEntity.setValorTotal(BigDecimal.valueOf(qtde * request.valorUnitario())); //valor unitario tem que estar expresso na mesma unidade da quantidade convertida. Conferir depois
        movimentacoesEstoqueEntity.setUsuarioEntity(usuario);
        movimentacoesEstoqueEntity.setConversoesEntity(conversao);
        movimentacoesEstoqueEntity.setObservacao(request.observacao());
        movimentacoesEstoqueEntity.setProdutosEntity(estoque.getProdutosEntity());

        return movimentacoesEstoqueRepository.save(movimentacoesEstoqueEntity);
    }
}

