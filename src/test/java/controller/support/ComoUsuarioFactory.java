package controller.support;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.EStatus;

public class ComoUsuarioFactory implements WithSecurityContextFactory<ComoUsuario> {
    @Override
    public SecurityContext createSecurityContext(ComoUsuario anotacao) {
        var usuario = new UsuariosEntity(anotacao.id(), "Jonas", "jonas123", "senha", EStatus.ATIVO);
        usuario.setCargo(anotacao.cargo());

        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        return contexto;
    }
}
