package school.sptech;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String nomeArqLocal = "dados_tratados";
        String nomeBucketRaw = "raw-1d4a3f130793f4b0dfc576791dd86b32";
        String nomeBucketTrusted = "trusted-1d4a3f130793f4b0dfc576791dd86b32";

        // Ler do bucket RAW
        List<Captura> lista = Gerenciador.leCsvBucketRaw(nomeBucketRaw);

        // Criar CSV local tratado
        Gerenciador.criaCsv(lista, nomeArqLocal);

        // Exibir no console
        Gerenciador.exibeListaCapturas(lista);

        // Criar conexão com banco
        Connection conn = new Connection();
        JdbcTemplate jdbc = new JdbcTemplate(conn.getDataSource());

        // Buscar parâmetros por setor usando ParametroService
        ParametroService parametroService = new ParametroService(jdbc);
        Map<String, List<Parametro>> parametrosPorSetor = parametroService.buscarParametrosPorSetor();

        // Criar instância do alerta
        EnviarAlertas enviar = new EnviarAlertas();
        Alertas alertas = new Alertas(parametrosPorSetor, enviar, jdbc);

        // Processar as capturas e enviar alertas + registrar no banco
        alertas.processarCapturas(lista);

        // Enviar CSV final pro bucket TRUSTED
        Gerenciador.enviaCsvParaBucketTrusted(nomeArqLocal, nomeBucketTrusted);

        System.out.println("Fluxo completo finalizado com sucesso ✅");
    }
}
