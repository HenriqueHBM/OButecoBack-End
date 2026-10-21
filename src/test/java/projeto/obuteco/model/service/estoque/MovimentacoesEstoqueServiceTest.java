package projeto.obuteco.model.service.estoque;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.estoque.dto.MovimentacoesEstoqueRequest;
import projeto.obuteco.model.enums.*;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.estoque.MovimentacoesEstoqueEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.repository.estoque.MovimentacoesEstoqueRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacoesEstoqueServiceTest {
    @InjectMocks
    MovimentacoesEstoqueService movimentacoesEstoqueService;

    @Mock
    MovimentacoesEstoqueRepository movimentacoesEstoqueRepository;

    @Captor
    ArgumentCaptor<MovimentacoesEstoqueEntity> captorMov;

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

    private MovimentacoesEstoqueRequest criarRequest(String tipo, Double qtde, Long fkIdConversao)
    { return new MovimentacoesEstoqueRequest(
                1L,          // fk_id_estoque
                tipo,
                qtde,
                10.0,        // valorUnitario
                1L,          // fk_id_usuario
                fkIdConversao,
                1.0,         // taxaConversao
                "Teste",     // observacao
                1L,          // fk_id_produto
                "Prateleira" // local
        );
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

}
