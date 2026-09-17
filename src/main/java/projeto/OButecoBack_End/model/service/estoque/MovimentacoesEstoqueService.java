package projeto.OButecoBack_End.model.service.estoque;

import jakarta.transaction.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.estoque.dto.EstoquesRequest;
import projeto.OButecoBack_End.controller.estoque.dto.MovimentacoesEstoqueRequest;
import projeto.OButecoBack_End.model.entity.conversao.ConversoesEntity;
import projeto.OButecoBack_End.model.entity.estoque.EstoquesEntity;
import projeto.OButecoBack_End.model.entity.estoque.MovimentacoesEstoqueEntity;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.repository.estoque.*;
import projeto.OButecoBack_End.model.repository.produto.*;
import projeto.OButecoBack_End.model.repository.usuario.*;

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
        double estoqueAnterior = estoque.getQtdeEstoque();
        estoque.setQtdeEstoque(estoqueAnterior + qtdeConvertida);
        estoquesRepository.save(estoque);

        //Registra a mov
        return registrarMovimentacao(estoque,"ENTRADA", qtdeSemConversao, usuario, movimentacoesEstoqueRequest, conversaoEntrada);
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
        if (estoque.getQtdeEstoque() < qtdeConvertida) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantidade insuficiente no estoque. Disponível: " +
                            estoque.getQtdeEstoque() + ", Solicitado: " + qtdeConvertida
            );
        }

        //Atualiza o estoque
        estoque.setQtdeEstoque(estoque.getQtdeEstoque() - qtdeConvertida);
        estoquesRepository.save(estoque);

        //Registra a mov
        return registrarMovimentacao(estoque,"SAÍDA", qtdeSemConversao, usuario, movimentacoesEstoqueRequest , conversaoSaida);
    }

    @Transactional






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
        movimentacoesEstoqueEntity.setQtde(qtde);
        movimentacoesEstoqueEntity.setValorUnitario(request.valorUnitario());
        movimentacoesEstoqueEntity.setValorTotal(qtde * request.valorUnitario()); //valor unitario tem que estar expresso na mesma unidade da quantidade convertida. Conferir depois
        movimentacoesEstoqueEntity.setUsuarioEntity(usuario);
        movimentacoesEstoqueEntity.setConversoesEntity(conversao);
        movimentacoesEstoqueEntity.setObservacao(request.observacao());
        movimentacoesEstoqueEntity.setProdutosEntity(estoque.getProdutosEntity());

        return movimentacoesEstoqueRepository.save(movimentacoesEstoqueEntity);
    }
}

