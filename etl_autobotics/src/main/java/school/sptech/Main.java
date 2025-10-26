package school.sptech;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String nomeArqLocal = "dados_tratados";
        String nomeBucketRaw = "raw-1d4a3f130793f4b0dfc576791dd86b32";
        String nomeBucketTrusted = "trusted-1d4a3f130793f4b0dfc576791dd86b32";

        // 1️⃣ Ler do bucket RAW
        List<Captura> lista = Gerenciador.leCsvBucketRaw(nomeBucketRaw);

        // 2️⃣ Criar CSV local tratado
        Gerenciador.criaCsv(lista, nomeArqLocal);

        // 3️⃣ Exibir no console
        Gerenciador.exibeListaCapturas(lista);

        // 4️⃣ Criar conexão com banco
        Connection conn = new Connection();
        JdbcTemplate jdbc = new JdbcTemplate(conn.getDataSource());

        // 5️⃣ Buscar parâmetros por setor usando ParametroService
        ParametroService parametroService = new ParametroService(jdbc);
        Map<String, List<Parametro>> parametrosPorSetor = parametroService.buscarParametrosPorSetor();

        // 6️⃣ Criar instância do alerta
        EnviarAlertas enviar = new EnviarAlertas();
        Alertas alertas = new Alertas(parametrosPorSetor, enviar, jdbc);

        // 7️⃣ Processar as capturas e enviar alertas + registrar no banco
        alertas.processarCapturas(lista);

        // 8️⃣ Enviar CSV final pro bucket TRUSTED
        Gerenciador.enviaCsvParaBucketTrusted(nomeArqLocal, nomeBucketTrusted);

        System.out.println("Fluxo completo finalizado com sucesso ✅");
    }
}
