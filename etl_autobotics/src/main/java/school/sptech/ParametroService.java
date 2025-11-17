package school.sptech;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

public class ParametroService {

    private JdbcTemplate con;

    public ParametroService(JdbcTemplate con) {
        this.con = con;
    }

    public Map<String, List<Parametro>> buscarParametrosPorSetor() {
        String sql = """
                SELECT s.nome AS nome_setor, c.nome AS nome_componente, p.valor, p.criticidade
                FROM parametro p
                JOIN componente c ON c.id_componente = p.fk_componente
                JOIN setor s ON s.id_setor = c.fk_setor
                WHERE p.criticidade IN (1, 2);
            """;

        Map<String, List<Parametro>> mapa = new HashMap<>();

        con.query(sql, rs -> {
            String setor = rs.getString("nome_setor");
            String componente = rs.getString("nome_componente");
            double min = rs.getDouble("valor");
            int criticidade = rs.getInt("criticidade");

            Parametro p = new Parametro(componente, min, criticidade);
            String chave = setor;

            mapa.computeIfAbsent(chave, k -> new ArrayList<>()).add(p);
        });

        return mapa;
    }
}
