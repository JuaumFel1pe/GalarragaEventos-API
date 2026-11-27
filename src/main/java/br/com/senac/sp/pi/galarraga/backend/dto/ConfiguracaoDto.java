package br.com.senac.sp.pi.galarraga.backend.dto;

import br.com.senac.sp.pi.galarraga.backend.model.Configuracao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfiguracaoDto(
        @NotBlank @Size(max = 300) String endereco,
        @NotBlank @Size(max = 20) String telefone,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 255) @Pattern(regexp = "(?:https?://[^\\s]+)?") String facebook,
        @Size(max = 255) @Pattern(regexp = "(?:https?://[^\\s]+)?") String instagram,
        @Size(max = 255) @Pattern(regexp = "(?:https?://[^\\s]+)?") String linkedin,
        @Size(max = 255) @Pattern(regexp = "(?:https?://[^\\s]+)?") String twitter
) {
    public static ConfiguracaoDto de(Configuracao config) {
        return new ConfiguracaoDto(config.getEndereco(), config.getTelefone(), config.getEmail(),
                config.getFacebook(), config.getInstagram(), config.getLinkedin(), config.getTwitter());
    }
}
