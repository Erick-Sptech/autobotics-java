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
                Double valor = switch (p.getComponente().toLowerCase()) {
                    case "cpu" -> c.getCpu();
                    case "ram" -> c.getRamUsada();
                    case "disco" -> c.getDiscoUsado();
                    default -> null;
                };

                if (valor == null) continue;

                // Verifica se valor está fora dos limites
                if (valor > p.getValorMin()) {

                    int criticidade = p.getCriticidade();

                    // Envia apenas se for médio (1) ou crítico (2)
                    if (criticidade == 1 || criticidade == 2) {

                        String tipoAlerta = (criticidade == 1) ? "médio" : "crítico";

                        String mensagem = String.format(
                                "⚠️ Alerta %s - %s fora do limite!\n" +
                                        "Setor: %s\nMáquina: %s\nValor atual: %.2f\nMínimo: %.2f",
                                tipoAlerta.toUpperCase(), p.getComponente(), setor,
                                c.getCodigoMaquina(), valor, p.getValorMin()
                        );

                        System.out.println(mensagem);

                        // 📨 Envia alerta ao Jira com criticidade e mensagem detalhada
                        EnviarAlertas.criarTicket(p.getComponente(), valor, tipoAlerta, mensagem);

                        // 🔍 Busca IDs e insere no banco
                        int idComponente = buscarIdComponente(p.getComponente(), setor);
                        if (idComponente == -1) continue;

                        int idControlador = buscarIdControlador(c.getCodigoMaquina(), idComponente);
                        if (idControlador == -1) continue;

                        String sql = "INSERT INTO alerta (timestamp, fk_controlador, fk_componente, valor, criticidade) " +
                                "VALUES (NOW(), ?, ?, ?, ?)";
                        jdbc.update(sql, idControlador, idComponente, valor, criticidade);
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
