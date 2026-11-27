package br.com.senac.sp.pi.galarraga.backend.config;

import br.com.senac.sp.pi.galarraga.backend.dto.RegisterDTO;
import br.com.senac.sp.pi.galarraga.backend.repository.UsuarioRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class AdminLocal {
    private static final String EMAIL_PADRAO = "admin@galarraga.example";

    private AdminLocal() {}

    static RegisterDTO carregarOuGerar(Path arquivo, UsuarioRepository usuarios) throws IOException {
        if (Files.exists(arquivo)) {
            var dados = new Properties();
            try (var leitor = Files.newBufferedReader(arquivo)) {
                dados.load(leitor);
            }
            return new RegisterDTO("Administrador", dados.getProperty("email"), dados.getProperty("senha"),
                    dados.getProperty("cpf"), null);
        }

        String email = usuarios.existsByEmail(EMAIL_PADRAO)
                ? "admin-" + UUID.randomUUID() + "@galarraga.example"
                : EMAIL_PADRAO;
        String cpf;
        do {
            cpf = Long.toString(ThreadLocalRandom.current().nextLong(10000000000L, 100000000000L));
        } while (usuarios.existsByCpf(cpf));

        String senha = UUID.randomUUID().toString();
        Files.createDirectories(arquivo.getParent());
        // Guarda as credenciais locais antes de cadastrar a conta, para que o primeiro login seja possível.
        Files.writeString(arquivo, "email=" + email + "\nsenha=" + senha + "\ncpf=" + cpf + "\n",
                StandardOpenOption.CREATE_NEW);
        return new RegisterDTO("Administrador", email, senha, cpf, null);
    }
}
