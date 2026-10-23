package projeto.obuteco.model.service.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.controller.login.dto.LoginResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.repository.usuario.UsuariosRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class LoginServiceTest {
    @InjectMocks
    LoginService loginService;

    @Mock
    private UsuariosRepository usuariosRepository;

    UsuariosEntity usuario = new UsuariosEntity(
            1L, "Ana", "Ana123", "Ana123", EStatus.ATIVO
    );

    @Test
    @DisplayName("autenticacao usuario com sucesso")
    void autententicacaoUsuario(){
        LoginRequest request = new LoginRequest("Ana123", "Ana123");

        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull("Ana123"))
                .thenReturn(Optional.of(usuario));

        UsuariosEntity resultado = loginService.autenticaoDoLogin(request);

        assertEquals(usuario, resultado);
        verify(usuariosRepository).findByUsuarioAndDeletedAtIsNull("Ana123");
    }

    @Test
    @DisplayName("autenticação do usuario com a senha errada")
    void autenticacaoUsuarioSenhaIncorreta(){
        LoginRequest request = new LoginRequest("Ana123", "Ana");

        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull("Ana123"))
                .thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.autenticaoDoLogin(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    @DisplayName("autenticação do usuario com usuario invalido")
    void autenticacaoUsuarioUsusarioInvalido(){
        LoginRequest request = new LoginRequest("Luis1", "Ana123");

        when(usuariosRepository.findByUsuarioAndDeletedAtIsNull("Luis1"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.autenticaoDoLogin(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
