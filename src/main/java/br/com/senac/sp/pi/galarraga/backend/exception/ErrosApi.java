package br.com.senac.sp.pi.galarraga.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrosApi {
    public static ResponseStatusException invalido(String motivo) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, motivo); }
    public static ResponseStatusException naoAutorizado(String motivo) { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, motivo); }
    public static ResponseStatusException naoEncontrado(String motivo) { return new ResponseStatusException(HttpStatus.NOT_FOUND, motivo); }
    public static ResponseStatusException conflito(String motivo) { return new ResponseStatusException(HttpStatus.CONFLICT, motivo); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException erro) {
        Map<String, String> campos = new LinkedHashMap<>();
        erro.getBindingResult().getFieldErrors().forEach(campo ->
                campos.putIfAbsent(campo.getField(), campo.getDefaultMessage()));
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Confira os campos informados");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflitoDeDados() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O registro já existe ou está vinculado a outro cadastro");
    }
}
