package projeto.obuteco.model.infra.spring.security;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.CargoEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.repository.usuario.UsuariosRepository;

import org.slf4j.Logger;

@Component
public class AdminInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String usuario;
    private final String senha;

    public AdminInicializador(UsuariosRepository usuariosRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.admin.nome:Administrador}") String nome,
                              @Value("${app.admin.usuario:admin}") String usuario,
                              @Value("${app.admin.senha:}") String senha) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuario)
                .ifPresentOrElse(this::garantirAdminAtivo, this::criarAdmin);
    }

    private void garantirAdminAtivo(UsuariosEntity admin) {
        boolean alterado = false;

        if (admin.getCargo() != CargoEnum.ADMIN) { admin.setCargo(CargoEnum.ADMIN); alterado = true; }
        if (admin.getStatus() != EStatus.ATIVO)  { admin.setStatus(EStatus.ATIVO);  alterado = true; }

        if (!senhaEhBcrypt(admin.getSenha())) {
            if (senha.isBlank()) {
                log.warn("Admin padrão '{}' tem senha fora do formato BCrypt e app.admin.senha não foi definida. O login não vai funcionar.", usuario);
            } else {
                admin.setSenha(passwordEncoder.encode(senha));
                alterado = true;
                log.warn("Senha do admin padrão '{}' não estava em formato BCrypt e foi redefinida a partir de app.admin.senha.", usuario);
            }
        }

        if (alterado) {
            usuariosRepository.save(admin);
            log.warn("Admin padrão '{}' foi corrigido (cargo, status ou senha).", usuario);
        }
    }

    private boolean senhaEhBcrypt(String hash) {
        return hash != null && hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    private void criarAdmin() {
        if (senha.isBlank()) {
            log.warn("Admin padrão '{}' não existe e app.admin.senha não foi definida. Nenhum admin criado.", usuario);
            return;
        }
        var admin = new UsuariosEntity();
        admin.setNome(nome);
        admin.setUsuario(usuario);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setStatus(EStatus.ATIVO);
        admin.setCargo(CargoEnum.ADMIN);
        usuariosRepository.save(admin);
        log.info("Admin padrão '{}' criado.", usuario);
    }
}