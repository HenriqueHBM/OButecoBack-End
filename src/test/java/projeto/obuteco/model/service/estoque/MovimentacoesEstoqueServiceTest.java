package projeto.obuteco.model.service.estoque;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.estoque.dto.EstoquesRequest;
import projeto.obuteco.controller.estoque.dto.MovimentacoesEstoqueRequest;
import projeto.obuteco.model.entity.produto.InsumosProdutoEntity;
import projeto.obuteco.model.enums.*;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.estoque.MovimentacoesEstoqueEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.repository.estoque.EstoquesRepository;
import projeto.obuteco.model.repository.estoque.MovimentacoesEstoqueRepository;
import projeto.obuteco.model.repository.produto.InsumosProdutoRepository;
import projeto.obuteco.model.service.produto.ProdutosService;
import projeto.obuteco.model.service.usuario.UsuariosService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacoesEstoqueServiceTest {
    @InjectMocks
    MovimentacoesEstoqueService movimentacoesEstoqueService;

    @Mock
    UsuariosService usuariosService;
    @Mock
    EstoquesService estoquesService;
    @Mock
    ProdutosService produtosService;

    @Mock
    MovimentacoesEstoqueRepository movimentacoesEstoqueRepository;
    @Mock
    EstoquesRepository estoquesRepository;
    @Mock // ou @MockitoBean, igual aos outros
    InsumosProdutoRepository insumosProdutoRepository;

    @Captor
    ArgumentCaptor<MovimentacoesEstoqueEntity> captorMov;
    @Captor
    ArgumentCaptor<EstoquesRequest> captorEstoque;

    ProdutosEntity produto = new ProdutosEntity(
            1L,
            "Espetinho",
            EStatus.ATIVO,
            CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(9.99),
            "Teste Prod"
    );

    ConversoesEntity conversao = new ConversoesEntity(
            1L,
            "Quilograma",
            "KG",
            BigDecimal.valueOf(1)
    );

    EstoquesEntity estoque = new  EstoquesEntity(
            1L,
            produto,
            BigDecimal.valueOf(10),
            conversao,
            "Prateleira"
    );

    UsuariosEntity usuario = new UsuariosEntity(1L, "Jonas", "jonas123", "jonas123", EStatus.ATIVO);

    MovimentacoesEstoqueEntity movimentacao = new MovimentacoesEstoqueEntity(
            estoque,
            "SAIDA",
            BigDecimal.valueOf(10.0),
            BigDecimal.valueOf(1.0),
            usuario,
            conversao,
            produto
            );

    private ProdutosEntity criarInsumoProduto(Long id, String nome) {
        return new ProdutosEntity(id, nome, EStatus.ATIVO, CategoriaEnum.NORMAL,
                GrupoEnum.COMIDA, BigDecimal.valueOf(5), "Insumo");
    }

    private InsumosProdutoEntity criarInsumo(ProdutosEntity insumoProduto, BigDecimal qtde, ConversoesEntity conv) {
        var insumo = new InsumosProdutoEntity();
        insumo.setInsumos(insumoProduto);
        insumo.setQtde(qtde);
        insumo.setConversoesEntity(conv);
        return insumo;
    }

    @SuppressWarnings("java:S107")
    private MovimentacoesEstoqueRequest criarRequestParcial(Double valorUnitario, String observacao) {
        return new MovimentacoesEstoqueRequest(
                1L,
                null,
                null,
                valorUnitario,
                1L,
                null,
                1.0,
                observacao,
                1L,
                "Prateleira"
        );
    }

    private MovimentacoesEstoqueEntity criarMovimentacaoCompleta(String tipo, BigDecimal quantidade, ConversoesEntity conv) {
        var movimentacao = new MovimentacoesEstoqueEntity();
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setQtdeConversao(quantidade);
        movimentacao.setConversoesEntity(conv);
        return movimentacao;
    }

    private MovimentacoesEstoqueRequest criarRequest(String tipo, Double qtde, Long fkIdConversao) {
        return criarRequest(tipo, qtde, fkIdConversao, 1L);
    }


    private MovimentacoesEstoqueRequest criarRequest(String tipo, Double qtde, Long fkIdConversao, Long fkIdProduto) {
        return new MovimentacoesEstoqueRequest(
                1L,
                tipo,
                qtde,
                10.0,
                1L,
                fkIdConversao,
                1.0,
                "Teste",
                fkIdProduto,
                "Prateleira"
        );
    }

    private MovimentacoesEstoqueEntity criarMovimentacao(String tipo, BigDecimal qtdeConversao) {
        var movimentacao = new MovimentacoesEstoqueEntity();
        movimentacao.setTipo(tipo);
        movimentacao.setQtdeConversao(qtdeConversao);
        return movimentacao;
    }

    //-------------------Testa buscarMovimentacaoPorId-------------------
    @Test
    @DisplayName("Deve retornar a movimentacao quando o ID existe")
    void buscar_movimentacao_por_ID_com_sucesso() {
        //Arrange
        movimentacao.setId(1L);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacao));

        //Act
        var busca = movimentacoesEstoqueService.buscarMovimentacaoPorId(1L);

        //Assert
        assertNotNull(busca);
        assertSame(movimentacao, busca);
        assertEquals(1L, busca.getId());

        verify(movimentacoesEstoqueRepository).findById(1L);
    }


    @Test
    @DisplayName("Deve retornar 404 quando a movimentacao nao existe")
    void buscar_movimentacao_por_ID_nao_encontrado() {
        //Arrange
        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var busca = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.buscarMovimentacaoPorId(1L));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, busca.getStatusCode());
        verify(movimentacoesEstoqueRepository).findById(1L);
    }

    //-------------------Testa historicoMovimentacoes-------------------
    @Test
    @DisplayName("Deve retornar lista com historico de movimentacoes")
    void listar_historico_de_movimentocoes_com_sucesso(){
        //Arrange
        when(movimentacoesEstoqueRepository.findAll()).thenReturn(List.of(movimentacao));

        //Act
        var resposta = movimentacoesEstoqueService.historicoMovimentacoes();

        //Assert
        assertNotNull(resposta);
        assertEquals(1,  resposta.size());
        assertSame(movimentacao, resposta.get(0));

        verify(movimentacoesEstoqueRepository).findAll();
    }

    //-------------------Testa historicoPorEstoque-------------------
    @Test
    @DisplayName("Deve retornar lista com historico de movimentacoes por estoque")
    void listar_historico_de_movimentacoes_por_estoque_com_sucesso(){
        //Arrange
        when(movimentacoesEstoqueRepository.findByEstoqueEntityIdOrderByDataMovimentacaoDesc(1L)).thenReturn(List.of(movimentacao));

        //Act
        var resposta = movimentacoesEstoqueService.historicoPorEstoque(1L);

        //Assert
        assertNotNull(resposta);
        assertEquals(1,  resposta.size());
        assertSame(movimentacao, resposta.get(0));

        verify(movimentacoesEstoqueRepository).findByEstoqueEntityIdOrderByDataMovimentacaoDesc(1L);
    }

    //-------------------Testa ehSaida-------------------
    @Test
    @DisplayName("Deve retornar TRUE para o tipo de saida")
    void retorna_tipo_de_saida(){
        //Arrange
        String tipo = movimentacao.getTipo();

        //Act
        var resultado = movimentacoesEstoqueService.ehSaida(tipo);

        //Assert
        assertTrue(resultado);
    }

    //-------------------Testa afetaEstoque-------------------
    @Test
    @DisplayName("Deve retornar true quando o tipo estiver preenchido")
    void deveRetornarTrueQuandoTipoPreenchido() {
        //Arrange
        var request = criarRequest("SAIDA", null, null);

        //Act
        boolean resultado = movimentacoesEstoqueService.afetaEstoque(request);

        //Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve retornar true quando a quantidade estiver preenchida")
    void deveRetornarTrueQuandoQuantidadePreenchida() {
        //Arrange
        var request = criarRequest(null, 10.0, null);

        //Act
        boolean resultado = movimentacoesEstoqueService.afetaEstoque(request);

        //Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve retornar true quando a conversão estiver preenchida")
    void deveRetornarTrueQuandoConversaoPreenchida() {
        //Arrange
        var request = criarRequest(null, null, 1L);

        //Act
        boolean resultado = movimentacoesEstoqueService.afetaEstoque(request);

        //Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve retornar false quando os campos relevantes forem nulos")
    void deveRetornarFalseQuandoCamposForemNulos() {
        //Arrange
        var request = criarRequest(null, null, null);

        //Act
        boolean resultado = movimentacoesEstoqueService.afetaEstoque(request);

        //Assert
        assertFalse(resultado);
    }

    //-------------------Testa atualizarValores-------------------
    @Test
    @DisplayName("Deve atualizar o valor unitário e recalcular o valor total")
    void atualizar_valores_com_valor_unitario_informado() {
        // Arrange
        movimentacao.setValorUnitario(new BigDecimal("10.00"));

        BigDecimal qtdeConvertida = new BigDecimal("5.00");
        Double valorUnitarioRequest = 20.0;

        // Act
        movimentacoesEstoqueService.atualizarValores(movimentacao, valorUnitarioRequest, qtdeConvertida);

        //Assert
        assertEquals(new BigDecimal("20.0"), movimentacao.getValorUnitario());
        assertEquals(new BigDecimal("100.000"), movimentacao.getValorTotal());
    }

    @Test
    @DisplayName("Deve manter o valor unitario anterior quando o request for nulo")
    void atualizar_valores_com_valor_unitario_nulo() {
        // Arrange
        movimentacao.setValorUnitario(new BigDecimal("10.00"));

        BigDecimal qtdeConvertida = new BigDecimal("5.00");
        Double valorUnitarioRequest = null;

        // Act
        movimentacoesEstoqueService.atualizarValores(movimentacao, valorUnitarioRequest, qtdeConvertida);

        // Assert
        assertEquals(new BigDecimal("10.00"), movimentacao.getValorUnitario());

        assertEquals(new BigDecimal("50.0000"), movimentacao.getValorTotal());
    }

    //-------------------Testa aplicarNoEstoque-------------------
    @Test
    @DisplayName("Deve subtrair do estoque quando a saida tem saldo suficiente")
    void aplicar_saida_com_saldo_suficiente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));

        //Act
        movimentacoesEstoqueService.aplicarNoEstoque(estoque, "SAIDA", BigDecimal.valueOf(4));

        //Assert
        assertEquals(0, BigDecimal.valueOf(6).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve permitir saida que zera o estoque")
    void aplicar_saida_igual_ao_saldo(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));

        //Act
        movimentacoesEstoqueService.aplicarNoEstoque(estoque, "SAIDA", BigDecimal.valueOf(10));

        //Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve lancar excecao e manter o saldo quando a saida excede o estoque")
    void aplicar_saida_com_saldo_insuficiente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var qtdeConvertida = BigDecimal.valueOf(11);

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.aplicarNoEstoque(estoque, "SAIDA", qtdeConvertida));

        //Assert
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        assertNotNull(chamada.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
    }

    @Test
    @DisplayName("Deve somar ao estoque quando o tipo e entrada")
    void aplicar_entrada(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));

        //Act
        movimentacoesEstoqueService.aplicarNoEstoque(estoque, "ENTRADA", BigDecimal.valueOf(5));

        //Assert
        assertEquals(0, BigDecimal.valueOf(15).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Nao deve alterar o estoque quando o tipo nao e entrada nem saida")
    void aplicar_tipo_desconhecido(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));

        //Act
        movimentacoesEstoqueService.aplicarNoEstoque(estoque, "DESCONHECIDO", BigDecimal.valueOf(5));

        //Assert
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        assertEquals("Prateleira", estoque.getLocal()); //nao alterao a entidade
        assertSame(produto, estoque.getProdutosEntity());
    }

    //-------------------Testa criarEstoqueSeProdutoInformado-------------------
    @Test
    @DisplayName("Deve criar estoque vazio quando o produto e encontrado")
    void criar_estoque_vazio(){
        //Arrange
        var request = criarRequest("ENTRADA", 5.0, 1L);
        when(estoquesService.salvarEstoque(any(EstoquesRequest.class))).thenReturn(estoque);

        //Act
        var resultado = movimentacoesEstoqueService.criarEstoqueSeProdutoInformado(request);

        //Assert
        captorEstoque = ArgumentCaptor.forClass(EstoquesRequest.class);
        verify(estoquesService).salvarEstoque(captorEstoque.capture());

        EstoquesRequest enviado = captorEstoque.getValue();
        assertEquals(1L, enviado.fk_id_produto());
        assertEquals(1L, enviado.fk_id_conversao());
        assertEquals(0.0, enviado.qtdeEstoque());
        assertEquals("Prateleira", enviado.local());
        assertSame(estoque, resultado);
    }

    @Test
    @DisplayName("Deve lancar 404 quando o produto nao e encontrado")
    void criar_estoque_sem_produto(){
        //Arrange
        var request = criarRequest("ENTRADA", 5.0, 1L, null);

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.criarEstoqueSeProdutoInformado(request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        verify(estoquesService, never()).salvarEstoque(any());
    }

    //-------------------Testa saida e movimentar-------------------
    @Test
    @DisplayName("Deve registrar saida buscando o estoque pelo id do request")
    void saida_com_sucesso(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var request = criarRequest("SAIDA", 4.0, 1L);

        when(estoquesService.buscarEstoquePorId(1L)).thenReturn(estoque);
        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);
        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(estoquesRepository.save(any(EstoquesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacoesEstoqueRepository.save(any(MovimentacoesEstoqueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = movimentacoesEstoqueService.saida(request);

        //Assert
        verify(estoquesService).buscarEstoquePorId(1L);

        captorMov = ArgumentCaptor.forClass(MovimentacoesEstoqueEntity.class);
        verify(movimentacoesEstoqueRepository).save(captorMov.capture());

        verify(estoquesRepository).save(estoque);

        MovimentacoesEstoqueEntity movSalva = captorMov.getValue();
        assertEquals("SAIDA", movSalva.getTipo());
        assertSame(estoque, movSalva.getEstoqueEntity());
        assertSame(movSalva, resultado);
        assertEquals(0, BigDecimal.valueOf(6).compareTo(estoque.getQntdEstoque()));
        }

    @Test
    @DisplayName("Deve lancar excecao e nao salvar quando o estoque nao e encontrado")
    void saida_estoque_nao_encontrado(){
        //Arrange
        var request = criarRequest("SAIDA", 4.0, 1L);
        when(estoquesService.buscarEstoquePorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.saida(request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao e manter o saldo quando a saida excede o estoque")
    void saida_saldo_insuficiente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var request = criarRequest("SAIDA", 11.0, 1L);
        when(estoquesService.buscarEstoquePorId(1L)).thenReturn(estoque);
        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.saida(request));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    //-------------------Testa entrada-------------------
    @Test
    @DisplayName("Deve registrar entrada somando ao estoque existente")
    void entrada_estoque_existente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var request = criarRequest("ENTRADA", 5.0, 1L);

        when(estoquesRepository.findById(1L)).thenReturn(Optional.of(estoque));
        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);
        when(estoquesRepository.save(any(EstoquesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacoesEstoqueRepository.save(any(MovimentacoesEstoqueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = movimentacoesEstoqueService.entrada(request);

        //Assert
        assertNotNull(resultado);
        assertEquals("ENTRADA", resultado.getTipo());
        assertEquals(0, BigDecimal.valueOf(15).compareTo(estoque.getQntdEstoque()));

        verify(estoquesRepository).findById(1L);
        verify(estoquesRepository).save(estoque);
        verify(movimentacoesEstoqueRepository).save(any(MovimentacoesEstoqueEntity.class));
        verify(estoquesService, never()).salvarEstoque(any());
    }

    @Test
    @DisplayName("Deve criar estoque vazio e registrar entrada quando o estoque nao existe e o produto e informado")
    void entrada_cria_estoque_quando_nao_existe(){
        //Arrange
        var estoqueNovo = new EstoquesEntity(2L, produto, BigDecimal.ZERO, conversao, "Prateleira");
        var request = criarRequest("ENTRADA", 5.0, 1L);

        when(estoquesRepository.findById(1L)).thenReturn(Optional.empty());
        when(estoquesService.salvarEstoque(any(EstoquesRequest.class))).thenReturn(estoqueNovo);
        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);
        when(estoquesRepository.save(any(EstoquesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacoesEstoqueRepository.save(any(MovimentacoesEstoqueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = movimentacoesEstoqueService.entrada(request);

        //Assert
        assertNotNull(resultado);
        assertEquals(0, BigDecimal.valueOf(5).compareTo(estoqueNovo.getQntdEstoque()));

        verify(estoquesService).salvarEstoque(any(EstoquesRequest.class));
        verify(estoquesRepository).save(estoqueNovo);
    }

    @Test
    @DisplayName("Deve lancar excecao e nao salvar quando o estoque nao existe e o produto nao e informado")
    void entrada_estoque_inexistente_sem_produto(){
        //Arrange
        var request = criarRequest("ENTRADA", 5.0, 1L, null);
        when(estoquesRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var excecao = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.entrada(request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(estoquesService, never()).salvarEstoque(any());
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    //-------------------Testa converterQuantidade-------------------
    @Test
    @DisplayName("Deve retornar a mesma quantidade quando origem e destino tem o mesmo id")
    void converter_quantidade_mesma_conversao(){
        //Arrange
        var origem = new ConversoesEntity(1L, "Quilograma", "KG", BigDecimal.valueOf(1000));
        var destino = new ConversoesEntity(1L, "Quilograma", "KG", BigDecimal.valueOf(1000));
        var quantidade = BigDecimal.valueOf(7);

        //Act
        var resultado = movimentacoesEstoqueService.converterQuantidade(quantidade, origem, destino);

        //Assert
        assertSame(quantidade, resultado);
    }

    @Test
    @DisplayName("Deve converter a quantidade usando os fatores base")
    void converter_quantidade_entre_unidades_diferentes(){
        //Arrange
        var origem = new ConversoesEntity(1L, "Quilograma", "KG", BigDecimal.valueOf(1000));
        var destino = new ConversoesEntity(2L, "Grama", "G", BigDecimal.valueOf(1));

        //Act
        var resultado = movimentacoesEstoqueService.converterQuantidade(BigDecimal.valueOf(2), origem, destino);

        //Assert
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(resultado));
    }

    @Test
    @DisplayName("Deve arredondar para 4 casas com HALF_UP")
    void converter_quantidade_arredonda_quatro_casas(){
        //Arrange
        var origem = new ConversoesEntity(2L, "Grama", "G", BigDecimal.valueOf(1));
        var destino = new ConversoesEntity(3L, "Libra", "LB", BigDecimal.valueOf(3));

        //Act
        var resultado = movimentacoesEstoqueService.converterQuantidade(BigDecimal.ONE, origem, destino);

        //Assert
        // 1/3 = 0.3333333333... -> 0.3333
        assertEquals(new BigDecimal("0.3333"), resultado);
    }

    @Test
    @DisplayName("Deve lancar excecao quando o fator base da origem e nulo")
    void converter_quantidade_fator_origem_nulo(){
        //Arrange
        var origem = new ConversoesEntity(1L, "Quilograma", "KG", null);
        var destino = new ConversoesEntity(2L, "Grama", "G", BigDecimal.valueOf(1));
        var quantidade = BigDecimal.TEN;

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.converterQuantidade(quantidade, origem, destino));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
    }

    @Test
    @DisplayName("Deve lancar excecao quando o fator base do destino e nulo")
    void converter_quantidade_fator_destino_nulo(){
        //Arrange
        var origem = new ConversoesEntity(1L, "Quilograma", "KG", BigDecimal.valueOf(1000));
        var destino = new ConversoesEntity(2L, "Grama", "G", null);
        var quantidade = BigDecimal.TEN;

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.converterQuantidade(quantidade, origem, destino));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
    }

    //-------------------Testa reverterMovimentacao-------------------

    @Test
    @DisplayName("Deve subtrair do estoque ao reverter uma entrada com saldo suficiente")
    void reverter_entrada_com_saldo_suficiente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoEntrada = criarMovimentacao("ENTRADA", BigDecimal.valueOf(4));

        //Act
        movimentacoesEstoqueService.reverterMovimentacao(estoque, movimentacaoEntrada);

        //Assert
        assertEquals(0, BigDecimal.valueOf(6).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve permitir reverter entrada que zera o estoque")
    void reverter_entrada_zerando_estoque(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoEntrada = criarMovimentacao("ENTRADA", BigDecimal.valueOf(10));

        //Act
        movimentacoesEstoqueService.reverterMovimentacao(estoque, movimentacaoEntrada);

        //Assert
        assertEquals(0, estoque.getQntdEstoque().signum());
    }

    @Test
    @DisplayName("Deve lancar excecao e manter o saldo ao reverter entrada com saldo insuficiente")
    void reverter_entrada_saldo_insuficiente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoEntrada = criarMovimentacao("ENTRADA", BigDecimal.valueOf(11));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.reverterMovimentacao(estoque, movimentacaoEntrada));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        assertNotNull(chamada.getReason());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve somar ao estoque ao reverter uma saida")
    void reverter_saida(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoSaida = criarMovimentacao("SAIDA", BigDecimal.valueOf(4));

        //Act
        movimentacoesEstoqueService.reverterMovimentacao(estoque, movimentacaoSaida);

        //Assert
        assertEquals(0, BigDecimal.valueOf(14).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Nao deve alterar o estoque quando o tipo da movimentacao e desconhecido")
    void reverter_tipo_desconhecido(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoDesconhecida = criarMovimentacao("OUTRO", BigDecimal.valueOf(4));

        //Act
        movimentacoesEstoqueService.reverterMovimentacao(estoque, movimentacaoDesconhecida);

        //Assert
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
    }

    //-------------------Testa reverterMovimentacao-------------------
    @Test
    @DisplayName("Deve reverter a entrada, salvar o estoque e deletar a movimentacao")
    void excluir_movimentacao_entrada_com_sucesso(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoEntrada = criarMovimentacao("ENTRADA", BigDecimal.valueOf(4));
        movimentacaoEntrada.setEstoqueEntity(estoque); // ajuste ao setter real

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoEntrada));

        //Act
        movimentacoesEstoqueService.excluirMovimentacao(1L);

        //Assert
        assertEquals(0, BigDecimal.valueOf(6).compareTo(estoque.getQntdEstoque()));
        verify(estoquesRepository).save(estoque);
        verify(movimentacoesEstoqueRepository).delete(movimentacaoEntrada);
    }

    @Test
    @DisplayName("Deve reverter a saida, salvar o estoque e deletar a movimentacao")
    void excluir_movimentacao_saida_com_sucesso(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoSaida = criarMovimentacao("SAIDA", BigDecimal.valueOf(4));
        movimentacaoSaida.setEstoqueEntity(estoque);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoSaida));

        //Act
        movimentacoesEstoqueService.excluirMovimentacao(1L);

        //Assert
        assertEquals(0, BigDecimal.valueOf(14).compareTo(estoque.getQntdEstoque()));
        verify(estoquesRepository).save(estoque);
        verify(movimentacoesEstoqueRepository).delete(movimentacaoSaida);
    }

    @Test
    @DisplayName("Deve lancar excecao e nao deletar nada quando a movimentacao nao existe")
    void excluir_movimentacao_nao_encontrada(){
        //Arrange
        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.excluirMovimentacao(1L));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lancar excecao e nao salvar nem deletar quando a reversao nao e possivel")
    void excluir_movimentacao_reversao_invalida(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(3));
        var movimentacaoEntrada = criarMovimentacao("ENTRADA", BigDecimal.valueOf(4));
        movimentacaoEntrada.setEstoqueEntity(estoque);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoEntrada));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.excluirMovimentacao(1L));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        assertEquals(0, BigDecimal.valueOf(3).compareTo(estoque.getQntdEstoque()));
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).delete(any());
    }

    //-------------------Testa reaplicarMovimentacao-------------------
    @Test
    @DisplayName("Deve manter os valores da movimentacao quando o request nao informa tipo, quantidade e conversao")
    void reaplicar_request_sem_alteracoes(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        var request = criarRequest(null, null, null);

        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);

        //Act
        movimentacoesEstoqueService.reaplicarMovimentacao(estoque, movimentacaoOriginal, request);

        //Assert
        assertEquals("ENTRADA", movimentacaoOriginal.getTipo());
        assertEquals(0, BigDecimal.valueOf(4).compareTo(movimentacaoOriginal.getQuantidade()));
        assertEquals(0, BigDecimal.valueOf(4).compareTo(movimentacaoOriginal.getQtdeConversao()));
        assertSame(conversao, movimentacaoOriginal.getConversoesEntity());
        assertEquals(0, BigDecimal.valueOf(14).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve sobrescrever a movimentacao com os valores do request")
    void reaplicar_request_com_novos_valores(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        var request = criarRequest("SAIDA", 3.0, 1L);

        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);

        //Act
        movimentacoesEstoqueService.reaplicarMovimentacao(estoque, movimentacaoOriginal, request);

        //Assert
        assertEquals("SAIDA", movimentacaoOriginal.getTipo());
        assertEquals(0, BigDecimal.valueOf(3).compareTo(movimentacaoOriginal.getQuantidade()));
        assertEquals(0, BigDecimal.valueOf(3).compareTo(movimentacaoOriginal.getQtdeConversao()));
        assertEquals(0, BigDecimal.valueOf(7).compareTo(estoque.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve converter a quantidade quando a conversao do request difere da do estoque")
    void reaplicar_com_conversao_diferente(){
        //Arrange
        var conversaoGrama = new ConversoesEntity(1L, "Grama", "G", BigDecimal.ONE);
        var conversaoQuilo = new ConversoesEntity(2L, "Quilograma", "KG", BigDecimal.valueOf(1000));
        var estoqueEmGramas = new EstoquesEntity(3L, produto, BigDecimal.valueOf(5000), conversaoGrama, "Prateleira");
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.ONE, conversaoGrama);
        var request = criarRequest("ENTRADA", 2.0, 2L);

        when(estoquesService.buscarConversaoPorId(2L)).thenReturn(conversaoQuilo);

        //Act (2 kg * 1000 / 1 = 2000 g)
        movimentacoesEstoqueService.reaplicarMovimentacao(estoqueEmGramas, movimentacaoOriginal, request);

        //Assert
        assertEquals(0, BigDecimal.valueOf(2).compareTo(movimentacaoOriginal.getQuantidade()));
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(movimentacaoOriginal.getQtdeConversao()));
        assertSame(conversaoQuilo, movimentacaoOriginal.getConversoesEntity());
        assertEquals(0, BigDecimal.valueOf(7000).compareTo(estoqueEmGramas.getQntdEstoque()));
    }

    @Test
    @DisplayName("Deve lancar excecao e nao alterar nada quando a nova saida excede o saldo")
    void reaplicar_saida_saldo_insuficiente(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        var request = criarRequest("SAIDA", 11.0, 1L);

        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.reaplicarMovimentacao(estoque, movimentacaoOriginal, request));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        assertEquals("ENTRADA", movimentacaoOriginal.getTipo());
        assertEquals(0, BigDecimal.valueOf(4).compareTo(movimentacaoOriginal.getQuantidade()));
    }

    @Test
    @DisplayName("Deve propagar o erro e nao alterar nada quando a conversao nao e encontrada")
    void reaplicar_conversao_nao_encontrada(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        var request = criarRequest("ENTRADA", 5.0, 99L);

        when(estoquesService.buscarConversaoPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.reaplicarMovimentacao(estoque, movimentacaoOriginal, request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        assertSame(conversao, movimentacaoOriginal.getConversoesEntity());
    }

    //-------------------Testa editarMovimentacao-------------------
    @Test
    @DisplayName("Deve reverter, reaplicar e salvar estoque e movimentacao quando a edicao afeta o estoque")
    void editar_movimentacao_afetando_estoque(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        movimentacaoOriginal.setEstoqueEntity(estoque);
        var request = criarRequest("SAIDA", 3.0, 1L);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoOriginal));
        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);
        when(movimentacoesEstoqueRepository.save(any(MovimentacoesEstoqueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = movimentacoesEstoqueService.editarMovimentacao(1L, request);

        //Assert (10 - 4 revertendo a entrada = 6; 6 - 3 aplicando a saida = 3)
        assertSame(movimentacaoOriginal, resultado);
        assertEquals("SAIDA", resultado.getTipo());
        assertEquals(0, BigDecimal.valueOf(3).compareTo(resultado.getQuantidade()));
        assertEquals("Teste", resultado.getObservacao());
        assertEquals(0, BigDecimal.valueOf(3).compareTo(estoque.getQntdEstoque()));

        verify(estoquesRepository).save(estoque);
        verify(movimentacoesEstoqueRepository).save(movimentacaoOriginal);
    }

    @Test
    @DisplayName("Nao deve mexer no estoque quando so o valor unitario e informado")
    void editar_movimentacao_somente_valor_unitario(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        movimentacaoOriginal.setEstoqueEntity(estoque);
        var request = criarRequestParcial(25.0, null);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoOriginal));
        when(movimentacoesEstoqueRepository.save(any(MovimentacoesEstoqueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = movimentacoesEstoqueService.editarMovimentacao(1L, request);

        //Assert
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        assertEquals("ENTRADA", resultado.getTipo());

        verify(estoquesRepository, never()).save(any());
        verify(estoquesService, never()).buscarConversaoPorId(any());
        verify(movimentacoesEstoqueRepository).save(movimentacaoOriginal);
    }

    @Test
    @DisplayName("Deve atualizar apenas a observacao quando nada mais e informado")
    void editar_movimentacao_somente_observacao(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        movimentacaoOriginal.setEstoqueEntity(estoque);
        var request = criarRequestParcial(null, "Nova observacao");

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoOriginal));
        when(movimentacoesEstoqueRepository.save(any(MovimentacoesEstoqueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        var resultado = movimentacoesEstoqueService.editarMovimentacao(1L, request);

        //Assert
        assertEquals("Nova observacao", resultado.getObservacao());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(estoque.getQntdEstoque()));
        assertEquals(0, BigDecimal.valueOf(4).compareTo(resultado.getQuantidade()));
        verify(estoquesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao e nao salvar nada quando a movimentacao nao existe")
    void editar_movimentacao_nao_encontrada(){
        //Arrange
        var request = criarRequest("ENTRADA", 5.0, 1L);
        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.empty());

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.editarMovimentacao(1L, request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao e nao salvar nada quando a reversao nao e possivel")
    void editar_movimentacao_reversao_invalida(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(3));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        movimentacaoOriginal.setEstoqueEntity(estoque);
        var request = criarRequest("ENTRADA", 5.0, 1L);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoOriginal));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.editarMovimentacao(1L, request));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        assertEquals(0, BigDecimal.valueOf(3).compareTo(estoque.getQntdEstoque()));
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao e nao salvar nada quando a reaplicacao falha")
    void editar_movimentacao_reaplicacao_invalida(){
        //Arrange
        estoque.setQntdEstoque(BigDecimal.valueOf(10));
        var movimentacaoOriginal = criarMovimentacaoCompleta("ENTRADA", BigDecimal.valueOf(4), conversao);
        movimentacaoOriginal.setEstoqueEntity(estoque);
        var request = criarRequest("SAIDA", 20.0, 1L);

        when(movimentacoesEstoqueRepository.findById(1L)).thenReturn(Optional.of(movimentacaoOriginal));
        when(estoquesService.buscarConversaoPorId(1L)).thenReturn(conversao);

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.editarMovimentacao(1L, request));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    //-------------------Testa saidaInsumo-------------------
    @Test
    @DisplayName("Deve descontar o insumo, salvar o estoque e registrar a movimentacao")
    void saida_insumo_com_sucesso(){
        //Arrange (100 por unidade x 2 = 200; 500 - 200 = 300)
        var queijo = criarInsumoProduto(2L, "Queijo");
        var insumo = criarInsumo(queijo, BigDecimal.valueOf(100), conversao);
        var estoqueQueijo = new EstoquesEntity(2L, queijo, BigDecimal.valueOf(500), conversao, "Geladeira");
        var request = criarRequest("SAIDA", 2.0, 1L);

        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(insumosProdutoRepository.findAllByProdutosEntity(produto)).thenReturn(List.of(insumo));
        when(estoquesRepository.findByProdutosEntity(queijo)).thenReturn(Optional.of(estoqueQueijo));

        //Act
        movimentacoesEstoqueService.saidaInsumo(1L, request);

        //Assert
        assertEquals(0, BigDecimal.valueOf(300).compareTo(estoqueQueijo.getQntdEstoque()));
        verify(estoquesRepository).save(estoqueQueijo);

        captorMov = ArgumentCaptor.forClass(MovimentacoesEstoqueEntity.class);
        verify(movimentacoesEstoqueRepository).save(captorMov.capture());

        MovimentacoesEstoqueEntity registrada = captorMov.getValue();
        assertSame(estoqueQueijo, registrada.getEstoqueEntity());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(registrada.getQuantidade()));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(registrada.getQtdeConversao()));
        assertEquals("Saída de insumo para: Espetinho (Quantidade do produto: 2.0) - Teste",
                registrada.getObservacao());
    }

    @Test
    @DisplayName("Deve processar todos os insumos do produto")
    void saida_insumo_varios_insumos(){
        //Arrange
        var queijo = criarInsumoProduto(2L, "Queijo");
        var molho = criarInsumoProduto(3L, "Molho");
        var insumoQueijo = criarInsumo(queijo, BigDecimal.valueOf(100), conversao);
        var insumoMolho = criarInsumo(molho, BigDecimal.valueOf(50), conversao);
        var estoqueQueijo = new EstoquesEntity(2L, queijo, BigDecimal.valueOf(500), conversao, "Geladeira");
        var estoqueMolho = new EstoquesEntity(3L, molho, BigDecimal.valueOf(500), conversao, "Geladeira");
        var request = criarRequest("SAIDA", 2.0, 1L);

        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(insumosProdutoRepository.findAllByProdutosEntity(produto))
                .thenReturn(List.of(insumoQueijo, insumoMolho));
        when(estoquesRepository.findByProdutosEntity(queijo)).thenReturn(Optional.of(estoqueQueijo));
        when(estoquesRepository.findByProdutosEntity(molho)).thenReturn(Optional.of(estoqueMolho));

        //Act
        movimentacoesEstoqueService.saidaInsumo(1L, request);

        //Assert
        assertEquals(0, BigDecimal.valueOf(300).compareTo(estoqueQueijo.getQntdEstoque()));
        assertEquals(0, BigDecimal.valueOf(400).compareTo(estoqueMolho.getQntdEstoque()));
        verify(estoquesRepository).save(estoqueQueijo);
        verify(estoquesRepository).save(estoqueMolho);
        verify(movimentacoesEstoqueRepository, times(2)).save(any(MovimentacoesEstoqueEntity.class));
    }

    @Test
    @DisplayName("Deve converter a quantidade quando o insumo e o estoque usam unidades diferentes")
    void saida_insumo_com_conversao(){
        //Arrange (0,1 kg x 2 = 0,2 kg = 200 g; 500 - 200 = 300)
        var gramas = new ConversoesEntity(1L, "Grama", "G", BigDecimal.ONE);
        var quilos = new ConversoesEntity(2L, "Quilograma", "KG", BigDecimal.valueOf(1000));
        var queijo = criarInsumoProduto(2L, "Queijo");
        var insumo = criarInsumo(queijo, new BigDecimal("0.1"), quilos);
        var estoqueQueijo = new EstoquesEntity(2L, queijo, BigDecimal.valueOf(500), gramas, "Geladeira");
        var request = criarRequest("SAIDA", 2.0, 1L);

        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(insumosProdutoRepository.findAllByProdutosEntity(produto)).thenReturn(List.of(insumo));
        when(estoquesRepository.findByProdutosEntity(queijo)).thenReturn(Optional.of(estoqueQueijo));

        //Act
        movimentacoesEstoqueService.saidaInsumo(1L, request);

        //Assert
        assertEquals(0, BigDecimal.valueOf(300).compareTo(estoqueQueijo.getQntdEstoque()));

        ArgumentCaptor<MovimentacoesEstoqueEntity> captor =
                ArgumentCaptor.forClass(MovimentacoesEstoqueEntity.class);
        verify(movimentacoesEstoqueRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("0.2").compareTo(captor.getValue().getQuantidade()));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(captor.getValue().getQtdeConversao()));
    }

    @Test
    @DisplayName("Deve lancar excecao quando o produto nao possui insumos")
    void saida_insumo_produto_sem_insumos(){
        //Arrange
        var request = criarRequest("SAIDA", 2.0, 1L);
        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(insumosProdutoRepository.findAllByProdutosEntity(produto)).thenReturn(List.of());

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.saidaInsumo(1L, request));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao quando nao existe estoque para o insumo")
    void saida_insumo_estoque_nao_encontrado(){
        //Arrange
        var queijo = criarInsumoProduto(2L, "Queijo");
        var insumo = criarInsumo(queijo, BigDecimal.valueOf(100), conversao);
        var request = criarRequest("SAIDA", 2.0, 1L);

        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(insumosProdutoRepository.findAllByProdutosEntity(produto)).thenReturn(List.of(insumo));
        when(estoquesRepository.findByProdutosEntity(queijo)).thenReturn(Optional.empty());

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.saidaInsumo(1L, request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        assertNotNull(chamada.getReason());
        assertTrue(chamada.getReason().contains("Queijo"));
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao e manter o saldo quando o insumo e insuficiente")
    void saida_insumo_saldo_insuficiente(){
        //Arrange (precisa de 200, tem 100)
        var queijo = criarInsumoProduto(2L, "Queijo");
        var insumo = criarInsumo(queijo, BigDecimal.valueOf(100), conversao);
        var estoqueQueijo = new EstoquesEntity(2L, queijo, BigDecimal.valueOf(100), conversao, "Geladeira");
        var request = criarRequest("SAIDA", 2.0, 1L);

        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);
        when(insumosProdutoRepository.findAllByProdutosEntity(produto)).thenReturn(List.of(insumo));
        when(estoquesRepository.findByProdutosEntity(queijo)).thenReturn(Optional.of(estoqueQueijo));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.saidaInsumo(1L, request));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, chamada.getStatusCode());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(estoqueQueijo.getQntdEstoque()));
        verify(estoquesRepository, never()).save(any());
        verify(movimentacoesEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve propagar o erro e nao consultar insumos quando o produto nao existe")
    void saida_insumo_produto_nao_encontrado(){
        //Arrange
        var request = criarRequest("SAIDA", 2.0, 1L);
        when(usuariosService.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(produtosService.buscarProdutoPorId(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        //Act
        var chamada = assertThrows(ResponseStatusException.class,
                () -> movimentacoesEstoqueService.saidaInsumo(1L, request));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, chamada.getStatusCode());
        verify(insumosProdutoRepository, never()).findAllByProdutosEntity(any());
    }


}
