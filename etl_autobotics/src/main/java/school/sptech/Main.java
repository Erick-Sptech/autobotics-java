package school.sptech;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Captura> listaCaptura = leCsv("dados_hardware");
        exibeListaCapturas(listaCaptura);
    }

    public static List<Captura> leCsv(String nomeArq) {
        Conecction connection = new Conecction();
        JdbcTemplate con = new JdbcTemplate(connection.getDataSource());
        List<Captura> listaCaptura = new ArrayList<>();
        Captura captura;

        FileReader arq = null;
        Scanner entrada = null;
        Boolean erroGravar = false;
        nomeArq += ".csv";

        try {
            System.out.println("Diretório atual: " + System.getProperty("user.dir"));
            System.out.println("Arquivo a ser aberto: " + nomeArq);
            arq = new FileReader("etl_autobotics/" + nomeArq);

            entrada = new Scanner(arq).useDelimiter(",|\\n");
        }
        catch (FileNotFoundException erro) {
            System.out.println("Arquivo inexistente!");
            System.exit(1);
        }

        try {
            Boolean cabecalho = true;
            while (entrada.hasNextLine()) {
                // le a linha inteira do csv
                String linha = entrada.nextLine();
                // divide essa linha em vários campos usando a virgula
                // campos = 0 - timestamp, 1 - cpu, 2 - ramTotal, 3 - ramUsada, 4 - discoTotal, 5 - discoUsado
                // 6 - numProcessos, 7 - codigoMaquina, 8 - top5Processos (sendo tratado como uma String inteira)

                String[] campos = linha.split(",", 9);

                if (cabecalho) {

                    for (int i = 0; i < campos.length; i++) {
                        campos[i] = campos[i].replaceAll("[^a-zA-Z0-9]", "");
                    }
//                    System.out.printf("%-25s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-20s %-20s %-10s\n",
//                            campos[0], campos[1], campos[2], campos[3], campos[4], campos[5],
//                            campos[6], campos[7], "empresa", "setor", campos[8]);
                    cabecalho = false;
                } else {
                    String timestamp = campos[0].replaceAll("\"", "");
                    Double cpu = Double.parseDouble(campos[1]);
                    Double ramTotal = Double.parseDouble(campos[2]);
                    Double ramUsada = Double.parseDouble(campos[3]);
                    Double discoTotal = Double.parseDouble(campos[4]);
                    Double discoUsado = Double.parseDouble(campos[5]);
                    Integer numProcessos = Integer.parseInt(campos[6]);
                    String codigoMaquina = campos[7].replaceAll("\"", "");
                    String top5Processos = campos[8];
                    
                    String nomeEmpresa = "";
                    String nomeSetor = "";

                    String sql = """
                    SELECT e.nome AS nome_empresa, s.nome AS nome_setor
                    FROM controlador c
                    JOIN empresa e ON c.fk_empresa = e.id_empresa
                    JOIN setor s ON c.fk_setor = s.id_setor AND c.fk_empresa = s.fk_empresa
                    WHERE c.numero_serial = ?;
                    """;

                    List<Map<String, Object>> resultadoBanco = con.queryForList(sql, codigoMaquina);

                    if (!resultadoBanco.isEmpty()) {
                        Map<String, Object> linhaBanco = resultadoBanco.get(0);
                        nomeEmpresa = linhaBanco.get("nome_empresa").toString();
                        nomeSetor = linhaBanco.get("nome_setor").toString();
                    } else {
                        nomeEmpresa = "N/A";
                        nomeSetor = "N/A";
                    }

                    captura = new Captura(timestamp, cpu, ramTotal, ramUsada, discoTotal, discoUsado, numProcessos, codigoMaquina, nomeEmpresa, nomeSetor, top5Processos);
                    listaCaptura.add(captura);

//                    System.out.printf(
//                            "%-25s %-10.2f %-10.2f %-10.2f %-10.2f %-10.2f %-15d %-15s %-20s %-20s %-10s\n",
//                            timestamp, cpu, ramTotal, ramUsada, discoTotal, discoUsado, numProcessos,
//                            codigoMaquina, nomeEmpresa, nomeSetor, top5Processos
//                    );
                }
            }
        }
        catch (NoSuchElementException erro) {
            System.out.println("Arquivo com problemas!");
            erro.printStackTrace();
            erroGravar = true;
        }
        catch (IllegalStateException erro) {
            System.out.println("Erro na leitura do arquivo!");
            erro.printStackTrace();
            erroGravar = true;
        }
        finally {
            try {
                entrada.close();
                arq.close();
            }
            catch (IOException erro) {
                System.out.println("Erro ao fechar o arquivo");
                erroGravar = true;
            }
            if (erroGravar) {
                System.exit(1);
            }
        }
        return listaCaptura;
    }

    public static void exibeListaCapturas(List<Captura> lista) {
        System.out.printf("%-25s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-20s %-20s %-10s\n",
                            "timestamp", "cpu", "ramTotal", "ramUsada", "discoTotal", "discoUsado",
                            "numProcessos", "codigoMaquina", "empresa", "setor", "top5Processos");
        for (Captura c : lista) {
            System.out.printf(
                            "%-25s %-10.2f %-10.2f %-10.2f %-10.2f %-10.2f %-15d %-15s %-20s %-20s %-10s\n",
                            c.getTimestamp(), c.getCpu(), c.getRamTotal(), c.getRamUsada(), c.getDiscoTotal(), c.getDiscoUsado(), c.getNumProcessos(),
                            c.getCodigoMaquina(), c.getEmpresa(), c.getSetor(), c.getTop5Processos()
                    );
        }
    }

    public static void enviaCsvParaBucket(String nomeArq){

    }
}