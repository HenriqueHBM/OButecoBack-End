package projeto.obuteco.controller.login.dto;
import projeto.obuteco.model.Enum.CargoEnum;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
public record LoginResponse(
        Long id,
        String nome,
        String usuario,
        CargoEnum cargo
) {


    public static LoginResponse de(UsuariosEntity usuariosEntity) {
        return new LoginResponse(
                usuariosEntity.getId(),
                usuariosEntity.getNome(),
                usuariosEntity.getUsuario(),
                usuariosEntity.getCargo()
        );
    }
}
