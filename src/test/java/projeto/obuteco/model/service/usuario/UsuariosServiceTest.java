package projeto.obuteco.model.service.usuario;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.usuario.dto.UsuariosRequest;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.CargoEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.repository.usuario.UsuariosRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuariosServiceTest {
    @InjectMocks
    private UsuariosService usuariosService;

    @Captor
    private ArgumentCaptor<UsuariosEntity> usuarioCaptor;

    @Mock
    private UsuariosRepository usuariosRepository;

    UsuariosEntity usuario = new UsuariosEntity(
            1L,
            "Ana",
            "Ana123",
            "Ana123",
            EStatus.ATIVO
    );

    UsuariosRequest userRequest = new UsuariosRequest(
            1L,
            "Ana",
            "Ana123",
            "Ana123",
            CargoEnum.ADMIN,
            null,
            null,
            null
    );
    @Test
    @DisplayName("verifica se retorna usuario")
    void buscarUsuariosPorId() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        assertEquals(usuario, usuariosService.buscarUsuarioPorId(1L));
        verify(usuariosRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("verifica caso nao acha usuario")
    void buscarUsuarioPorIdNaoEncontrado() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuariosService.buscarUsuarioPorId(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Usuario nao encontrado com id", exception.getReason());
    }

    @Test
    void buscarPorNome(){
        when(usuariosRepository.findByNome("Ana"))
                .thenReturn(List.of(usuario));
        assertEquals(List.of(usuario), usuariosService.buscarPorNome("Ana"));
    }

    @Test
    void listarUsuarios(){
        when(usuariosRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(usuario));

        assertEquals(List.of(usuario), usuariosService.listarUsuarios());
    }

    @Test
    void deletarUsuarioPorId(){
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        long time_antes = System.currentTimeMillis();
        usuariosService.deletarUsuarioPorId(1L);
        long time_depois = System.currentTimeMillis();

        verify(usuariosRepository).save(usuarioCaptor.capture());
        var salvo = usuarioCaptor.getValue();

        assertEquals(EStatus.INATIVO, salvo.getStatus());
        assertNotNull(salvo.getDeletedAt());

        assertTrue(salvo.getDeletedAt().getTime() >= time_antes
        && salvo.getDeletedAt().getTime() <= time_depois);
    }

    @Test
    @DisplayName("atualiza staus to ususario encontrado e ativo")
    void atualizarStatusUsuarioAtivo(){
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        when(usuariosRepository.save(usuario))
                .thenReturn(usuario);

        usuariosService.atualizarStatusUsuario(1L);

        assertEquals(EStatus.INATIVO, usuario.getStatus());
        verify(usuariosRepository).save(usuario);
    }


    @Test
    @DisplayName("Atualiza o status do usuário encontrado e inativo")
    void atualizarStatusUsuarioInativo() {
        usuario.setStatus(EStatus.INATIVO);
        usuario.setDeletedAt(null);

        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        when(usuariosRepository.save(usuario))
                .thenReturn(usuario);

        usuariosService.atualizarStatusUsuario(1L);

        assertEquals(EStatus.ATIVO, usuario.getStatus());
        assertEquals(null, usuario.getDeletedAt());

        verify(usuariosRepository).save(usuario);
    }

    @Test
    @DisplayName("salva usuario")
    void salvarUsusario(){
        when(usuariosRepository.save(any(UsuariosEntity.class)))
                .thenAnswer(i -> {
                    UsuariosEntity p = i.getArgument(0);
                   p.setId(1L);
                   return p;
                });

        var newUser = usuariosService.salvarUsuario(userRequest);
        verify(usuariosRepository).save(usuarioCaptor.capture());

        var salvo = usuarioCaptor.getValue();

        assertEquals("Ana", salvo.getNome());
        assertEquals("Ana123", salvo.getUsuario());
        assertEquals("Ana123", salvo.getSenha());
        assertEquals(CargoEnum.ADMIN, salvo.getCargo());
        assertEquals(EStatus.ATIVO, salvo.getStatus());

        assertEquals(1L, newUser.getId());
    }

    @Test
    @DisplayName("verifica se o usuario ja existe")
    void salvarUsuarioExistente() {
        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull(userRequest.usuario()))
                .thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuariosService.salvarUsuario(userRequest)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuariosRepository, never()).save(any(UsuariosEntity.class));
    }

    @Test
    void atualizarUsuario(){

        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(usuario));
        when(usuariosRepository.save(usuario)).thenReturn(usuario);

        var atualizado = usuariosService.atualizarUsuario(1L, userRequest);

        assertSame(usuario, atualizado);
        assertEquals("Ana", atualizado.getNome());
        assertEquals("Ana123", atualizado.getUsuario());
        assertEquals("Ana123", atualizado.getSenha());
        assertEquals(CargoEnum.ADMIN, atualizado.getCargo());
        assertEquals(EStatus.ATIVO, atualizado.getStatus());
        verify(usuariosRepository, never()).findById(any());
    }

    @Test
    void atualizarUsuarioExistente() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        UsuariosRequest request = new UsuariosRequest(
                1L, "Ana", "Carlos123", "Senha123",
                CargoEnum.ADMIN, null, null, null
        );

        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull("Carlos123"))
                .thenReturn(Optional.of(new UsuariosEntity()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuariosService.atualizarUsuario(1L, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuariosRepository, never()).save(any(UsuariosEntity.class));
    }

    @Test
    @DisplayName("atualiza no nome do usuario")
    void atualizarUsuarioParcialNome() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        when(usuariosRepository.save(any(UsuariosEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuariosRequest request = new UsuariosRequest(
                null, "Ana Maria", null, null,
                null, null, null, null
        );

        UsuariosEntity atualizado = usuariosService.atualizarUsuarioParcial(1L, request);

        assertEquals("Ana Maria", atualizado.getNome());
        assertEquals("Ana123", atualizado.getUsuario());
        assertEquals("Ana123", atualizado.getSenha());
        verify(usuariosRepository).save(usuario);
    }

    @Test
    @DisplayName("atualiza a senha do usuario")
    void atualizarUsuarioParcialSenha() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        when(usuariosRepository.save(any(UsuariosEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuariosRequest request = new UsuariosRequest(
                null, null, null, "NovaSenha123",
                null, null, null, null
        );

        UsuariosEntity atualizado = usuariosService.atualizarUsuarioParcial(1L, request);

        assertEquals("NovaSenha123", atualizado.getSenha());
        assertEquals("Ana", atualizado.getNome());
        assertEquals("Ana123", atualizado.getUsuario());
        verify(usuariosRepository).save(usuario);
    }

    @Test
    @DisplayName("atualiza o cargo do usuario")
    void atualizarUsuarioParcialCargo() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));

        when(usuariosRepository.save(any(UsuariosEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuariosRequest request = new UsuariosRequest(
                null, null, null, null,
                CargoEnum.GERENTE, null, null, null
        );

        UsuariosEntity atualizado = usuariosService.atualizarUsuarioParcial(1L, request);

        assertEquals(CargoEnum.GERENTE, atualizado.getCargo());
        assertEquals("Ana", atualizado.getNome());
        assertEquals("Ana123", atualizado.getUsuario());
        verify(usuariosRepository).save(usuario);
    }

    @Test
    @DisplayName("Atualizar o usuario parcialmente para outro usuario")
    void atualizarUsuarioParcialUsuarioDiferente() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));
        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull("Carlos123"))
                .thenReturn(Optional.empty());
        when(usuariosRepository.save(any(UsuariosEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        UsuariosRequest request = new UsuariosRequest(
                null, null, "Carlos123", null, null, null, null, null
        );

        var atualizado = usuariosService.atualizarUsuarioParcial(1L, request);

        assertEquals("Carlos123", atualizado.getUsuario());
        verify(usuariosRepository).findByUsuarioAndDeletedAtIsNull("Carlos123");
    }

    @Test
    void atualizarUsuarioParcialUsuarioJaExiste() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));
        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull("Carlos123"))
                .thenReturn(Optional.of(new UsuariosEntity()));

        UsuariosRequest request = new UsuariosRequest(
                null, null, "Carlos123", null, null, null, null, null
        );

        var exception = assertThrows(ResponseStatusException.class,
                () -> usuariosService.atualizarUsuarioParcial(1L, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(usuariosRepository, never()).save(any(UsuariosEntity.class));
    }

    @Test
    @DisplayName("Deve manter o nome de usuário quando ele for igual ao atual")
    void atualizarUsuarioParcialMesmoUsuario() {
        when(usuariosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(usuario));
        when(usuariosRepository.save(any(UsuariosEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        UsuariosRequest request = new UsuariosRequest(
                null, null, usuario.getUsuario(), null, null, null, null, null
        );

        var atualizado = usuariosService.atualizarUsuarioParcial(1L, request);

        assertEquals(usuario.getUsuario(), atualizado.getUsuario());
        verify(usuariosRepository, never())
                .findByUsuarioAndDeletedAtIsNull(any());
        verify(usuariosRepository).save(usuario);
    }
}
