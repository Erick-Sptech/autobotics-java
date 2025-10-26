package school.sptech;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

public class Alertas {

    private Map<String, List<Parametro>> parametrosPorSetor;
    private EnviarAlertas enviarAlertas;
    private JdbcTemplate jdbc;

    public Alertas(Map<String, List<Parametro>> parametrosPorSetor, EnviarAlertas enviarAlertas, JdbcTemplate jdbc) {
        this.parametrosPorSetor = parametrosPorSetor;
        this.enviarAlertas = enviarAlertas;
        this.jdbc = jdbc;
    }

    public void processarCapturas(List<Captura> capturas) {
        for (Captura c : capturas) {
            String setor = c.getSetor();
            List<Parametro> parametros = parametrosPorSetor.get(setor);
            if (parametros == null) continue;

            for (Parametro p : parametros) {
                Double valor = null;
                switch (p.getComponente().toLowerCase()) {
                    case "cpu": valor = c.getCpu(); break;
                    case "ram": valor = c.getRamUsada(); break;
                    case "disco": valor = c.getDiscoUsado(); break;
                }
                if (valor == null) continue;

                if (valor < p.getValorMin() || valor > p.getValorMax()) {
                    if (p.getCriticidade() >= 1) { // médio ou crítico (1 ou 2)
                        String mensagem = String.format(
                                "Alerta %s - %s fora do limite! Valor: %.2f, Min: %.2f, Max: %.2f",
                                c.getCodigoMaquina(), p.getComponente(), valor, p.getValorMin(), p.getValorMax()
                        );

                        // 1️⃣ Envia alerta pro Jira
                        EnviarAlertas.criarTicket(p.getComponente(), valor);

                        // 2️⃣ Busca IDs para inserir no banco
                        int idComponente = buscarIdComponente(p.getComponente(), c.getSetor());
                        if (idComponente == -1) continue;

                        int idControlador = buscarIdControlador(c.getCodigoMaquina(), idComponente);
                        if (idControlador == -1) continue;

                        // 3️⃣ Insere no banco
                        String sql = "INSERT INTO alerta (timestamp, fk_controlador, fk_componente, valor) " +
                                "VALUES (NOW(), ?, ?, ?)";
                        jdbc.update(sql, idControlador, idComponente, valor);
                    }
                }
            }
        }
    }

    private int buscarIdComponente(String componente, String setor) {
        String sql = "SELECT id_componente " +
                "FROM componente c " +
                "JOIN setor s ON c.fk_setor = s.id_setor AND c.fk_empresa = s.fk_empresa " +
                "WHERE UPPER(c.nome) = UPPER(?) AND UPPER(s.nome) = UPPER(?)";

        List<Integer> ids = jdbc.query(sql, new Object[]{componente, setor}, (rs, rowNum) -> rs.getInt("id_componente"));
        if (ids.isEmpty()) {
            System.out.printf("⚠️ Componente não encontrado: %s / Setor: %s%n", componente, setor);
            return -1;
        }
        return ids.get(0);
    }

    private int buscarIdControlador(String codigoMaquina, int idComponente) {
        String sql = "SELECT id_controlador FROM controlador WHERE numero_serial = ? AND fk_setor = " +
                "(SELECT fk_setor FROM componente WHERE id_componente = ?)";

        List<Integer> ids = jdbc.query(sql, new Object[]{codigoMaquina, idComponente}, (rs, rowNum) -> rs.getInt("id_controlador"));
        if (ids.isEmpty()) {
            System.out.printf("⚠️ Controlador não encontrado: %s / Componente ID: %d%n", codigoMaquina, idComponente);
            return -1;
        }
        return ids.get(0);
    }
}
