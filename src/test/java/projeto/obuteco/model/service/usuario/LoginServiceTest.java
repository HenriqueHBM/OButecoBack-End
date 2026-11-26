package projeto.obuteco.model.service.usuario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.login.dto.LoginRequest;
import projeto.obuteco.controller.login.dto.LoginResponse;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.infra.spring.security.JwtService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @InjectMocks
    LoginService loginService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    UsuariosEntity usuario = new UsuariosEntity(
            1L, "Ana", "Ana123", "Ana123", EStatus.ATIVO
    );

    @Test
    @DisplayName("autenticacao usuario com sucesso")
    void autententicacaoUsuario(){
        LoginRequest request = new LoginRequest("Ana123", "Ana123");
        var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(autenticacao);
        when(jwtService.gerarToken(usuario)).thenReturn("token-jwt");

        LoginResponse resultado = loginService.autenticaoDoLogin(request);

        assertEquals("token-jwt", resultado.token());
        assertEquals(1L, resultado.id());
        assertEquals("Ana", resultado.nome());
        verify(jwtService).gerarToken(usuario);
    }

    @Test
    @DisplayName("autenticação do usuario com a senha errada")
    void autenticacaoUsuarioSenhaIncorreta(){
        LoginRequest request = new LoginRequest("Ana123", "Ana");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.autenticaoDoLogin(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("autenticação do usuario com usuario invalido")
    void autenticacaoUsuarioUsusarioInvalido(){
        LoginRequest request = new LoginRequest("Luis1", "Ana123");

        // o DaoAuthenticationProvider converte "usuario nao encontrado" em BadCredentialsException
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> loginService.autenticaoDoLogin(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(jwtService);
    }
}