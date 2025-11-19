package school.sptech;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Paths;

public class Gerenciador {
    public Gerenciador() {

    }

    // RAW
    private static String buscarUltimoCsv(String nomeBucket) {
        // cria cliente s3 para requisitar a lista de objetos do bucket
        S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();

        // cria objetos request e response para buscar aws
        ListObjectsV2Request requisicao = ListObjectsV2Request.builder().bucket(nomeBucket).build();
        ListObjectsV2Response resultado = s3.listObjectsV2(requisicao);

        for (S3Object objetos : resultado.contents()) {
            System.out.println(objetos.key());
        }

        return  resultado.contents().get(resultado.contents().size()-1).key();
    }

    private static String buscarUrlBucket(String nomeBucket, String nomeRegiao) {
        return String.format("https://%s.s3.%s.amazonaws.com", nomeBucket, nomeRegiao);
    }

    public static List<Captura> tratarDados(List<Captura> listaAntiga) {
        List<Captura> listaNova = new ArrayList<>();
        for (Captura c : listaAntiga) {
            c.setRamTotal((c.getRamTotal() / Math.pow(1024.0, 3.0)));
            c.setDiscoTotal((c.getDiscoTotal() / Math.pow(1024.0, 3.0)));
            listaNova.add(c);
        }
        return listaNova;
    }

    public static List<Captura> leCsvBucketRaw(String nomeBucket) {
        String nomeArq = buscarUltimoCsv(nomeBucket);
        String urlBucket = buscarUrlBucket(nomeBucket, "us-east-1");
        Connection connection = new Connection();
        JdbcTemplate con = new JdbcTemplate(connection.getDataSource());
        List<Captura> listaCaptura = new ArrayList<>();
        Captura captura;

        InputStream arq = null;
        Scanner entrada = null;
        Boolean erroGravar = false;

        try {
            System.out.println("Arquivo a ser aberto: " + nomeArq);
            System.out.println("Link do csv: " + urlBucket+"/"+nomeArq);
            // cria um objeto URL do CSV no Bucket
            URL url = new URL(urlBucket+"/"+nomeArq);
            // openStream faz requisicao HTTPS e retorna um InputStream
            arq = url.openStream();

            entrada = new Scanner(arq).useDelimiter(",|\\n");
        }
        catch (IOException erro) {
            System.out.println("Erro na URL Bucket");
            erro.printStackTrace();
            System.exit(1);
        }

        try {
            Boolean cabecalho = true;
            while (entrada.hasNextLine()) {
                // le a linha inteira do csv
                String linha = entrada.nextLine();
                // divide essa linha em vários campos usando a virgula
                // campos: 0 - timestamp, 1 - cpu, 2 - ramTotal, 3 - ramUsada, 4 - discoTotal, 5 - discoUsado
                // 6 - numProcessos, 7 - codigoMaquina, 8 - top5Processos (sendo tratado como uma String inteira)

                String[] campos = linha.split(";", 9);

                if (cabecalho) {

                    for (int i = 0; i < campos.length; i++) {
                        campos[i] = campos[i].replaceAll("[^a-zA-Z0-9]", "");
                    }
//                    System.out.printf("%-25s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-20s %-20s %-10s\n",
//                            campos[0], campos[1], campos[2], campos[3], campos[4], campos[5],
//                            campos[6], campos[7], "empresa", "setor", campos[8]);
                    cabecalho = false;
                } else {
                    String timestamp = (!campos[0].isEmpty()) ? campos[0].replaceAll("\"", ""): "N/A";
                    String codigoMaquina = (!campos[1].isEmpty()) ? campos[1].replaceAll("\"", "") : "N/A";
                    Double cpu = (!campos[2].isEmpty()) ? Double.parseDouble(campos[2]) : null;
                    Double ramTotal = (!campos[3].isEmpty()) ? Double.parseDouble(campos[3]) : null;
                    Double ramUsada = (!campos[4].isEmpty()) ? Double.parseDouble(campos[4]) : null;
                    Double discoTotal = (!campos[5].isEmpty()) ? Double.parseDouble(campos[5]) : null;
                    Double discoUsado = (!campos[6].isEmpty()) ? Double.parseDouble(campos[6]) : null;
                    Integer numProcessos = (!campos[7].isEmpty()) ? Integer.parseInt(campos[7]) : null;
                    String top5Processos = (!campos[8].isEmpty()) ? campos[8] : "N/A";

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
        return tratarDados(listaCaptura);
    }

    public static List<Captura> leCsvLocal(String nomeArq) {
        FileReader arq = null;
        Scanner entrada = null;
        Boolean erroGravar = false;
        //nomeArq += ".csv";
        if (!nomeArq.startsWith("/tmp/")) {
            nomeArq = "/tmp/" + nomeArq + ".csv";
        } else {
            nomeArq += ".csv";
        }
        List<Captura> listaCaptura = new ArrayList<>();
        Captura captura;

        try {
            System.out.println("Lendo CSV local...");
            arq = new FileReader(nomeArq);  // Abre o arquivo para leitura
            // Instancia a classe Scanner e especifica que deve usar delimitador ; ou \n
            entrada = new Scanner(arq).useDelimiter(";|\\n");
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
                System.out.println(linha);
                // divide essa linha em vários campos usando a virgula
                // campos: 0 - timestamp, 1 - cpu, 2 - ramTotal, 3 - ramUsada, 4 - discoTotal, 5 - discoUsado
                // 6 - numProcessos, 7 - codigoMaquina, 8 - top5Processos (sendo tratado como uma String inteira)

                String[] campos = linha.split(";", 11);

                if (cabecalho) {

                    for (int i = 0; i < campos.length; i++) {
                        campos[i] = campos[i].replaceAll("[^a-zA-Z0-9]", "");
                    }
    //                    System.out.printf("%-25s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-20s %-20s %-10s\n",
    //                            campos[0], campos[1], campos[2], campos[3], campos[4], campos[5],
    //                            campos[6], campos[7], "empresa", "setor", campos[8]);
                    cabecalho = false;
                } else {
                    String timestamp = (!campos[0].isEmpty()) ? campos[0].replaceAll("\"", ""): "N/A";
                    Double cpu = (!campos[1].isEmpty()) ? Double.parseDouble(campos[1]) : null;
                    Double ramTotal = (!campos[2].isEmpty()) ? Double.parseDouble(campos[2]) : null;
                    Double ramUsada = (!campos[3].isEmpty()) ? Double.parseDouble(campos[3]) : null;
                    Double discoTotal = (!campos[4].isEmpty()) ? Double.parseDouble(campos[4]) : null;
                    Double discoUsado = (!campos[5].isEmpty()) ? Double.parseDouble(campos[5]) : null;
                    Integer numProcessos = (!campos[6].isEmpty()) ? Integer.parseInt(campos[6]) : null;
                    String codigoMaquina = (!campos[7].isEmpty()) ? campos[7].replaceAll("\"", "") : "N/A";
                    String nomeEmpresa = (!campos[8].isEmpty()) ? campos[8].replaceAll("\"", "") : "N/A";;
                    String nomeSetor = (!campos[9].isEmpty()) ? campos[9].replaceAll("\"", "") : "N/A";;
                    String top5Processos = (!campos[10].isEmpty()) ? campos[10] : "N/A";


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

    public static void criaCsv(List<Captura> lista, String nomeArq) {
        OutputStreamWriter saida = null;
        Boolean erroGravar = false;
        //nomeArq += ".csv";
        if (!nomeArq.startsWith("/tmp/")) {
            nomeArq = "/tmp/" + nomeArq + ".csv";
        } else {
            nomeArq += ".csv";
        }

        try {
            saida = new OutputStreamWriter(new FileOutputStream(nomeArq), StandardCharsets.UTF_8);
        }
        catch (FileNotFoundException erro) {
            System.out.println("Erro ao criar arquivo CSV");
            erro.printStackTrace();
            System.exit(1);
        }
        try {
            System.out.println("Criando csv '" + nomeArq + "'...");
            saida.append("timestamp;cpu;ramTotal;ramUsada;discoTotal;discoUsado;numProcessos;codigoMaquina;empresa;setor;top5Processos\n");
            for (Captura c : lista) {
                saida.write(String.format(Locale.US,"%s;%f;%f;%f;%f;%f;%d;%s;%s;%s;%s\n",
                        c.getTimestamp(), c.getCpu(), c.getRamTotal(), c.getRamUsada(), c.getDiscoTotal(), c.getDiscoUsado(), c.getNumProcessos(),
                        c.getCodigoMaquina(), c.getEmpresa(), c.getSetor(), c.getTop5Processos()));
            }
        }
        catch (IOException erro) {
            System.out.println("Erro ao adicionar dados no CSV");
            erro.printStackTrace();
            erroGravar = true;
        }
        finally {
            try {
                saida.close();
            }
            catch (IOException erro) {
                System.out.println("Erro ao fechar");
                erroGravar = true;
            }
            if (erroGravar) {
                System.exit(1);
            }
        }
    }

    // TRUSTED

    public static List<Captura> leCsvBucketTrusted(String nomeBucket) {
        String nomeArq = "main.csv";
        String urlBucket = buscarUrlBucket(nomeBucket, "us-east-1");
        Connection connection = new Connection();
        JdbcTemplate con = new JdbcTemplate(connection.getDataSource());
        List<Captura> listaCaptura = new ArrayList<>();
        Captura captura;

        InputStream arq = null;
        Scanner entrada = null;
        Boolean erroGravar = false;

        try {
            System.out.println("Link do csv: " + urlBucket+"/"+nomeArq);
            // cria um objeto URL do CSV no Bucket
            URL url = new URL(urlBucket+"/"+nomeArq);
            // openStream faz requisicao HTTPS e retorna um InputStream
            arq = url.openStream();

            entrada = new Scanner(arq).useDelimiter(",|\\n");
        }
        catch (IOException erro) {
            System.out.println("Erro na URL Bucket");
            erro.printStackTrace();
            System.exit(1);
        }

        try {
            Boolean cabecalho = true;
            while (entrada.hasNextLine()) {
                // le a linha inteira do csv
                String linha = entrada.nextLine();
                // divide essa linha em vários campos usando a virgula
                // campos: 0 - timestamp, 1 - cpu, 2 - ramTotal, 3 - ramUsada, 4 - discoTotal, 5 - discoUsado
                // 6 - numProcessos, 7 - codigoMaquina, 8 - top5Processos (sendo tratado como uma String inteira)

                String[] campos = linha.split(";", 11);

                if (cabecalho) {

                    for (int i = 0; i < campos.length; i++) {
                        campos[i] = campos[i].replaceAll("[^a-zA-Z0-9]", "");
                    }
//                    System.out.printf("%-25s %-10s %-10s %-10s %-10s %-10s %-15s %-15s %-20s %-20s %-10s\n",
//                            campos[0], campos[1], campos[2], campos[3], campos[4], campos[5],
//                            campos[6], campos[7], "empresa", "setor", campos[8]);
                    cabecalho = false;
                }
                else {
                    String timestamp = (!campos[0].isEmpty()) ? campos[0].replaceAll("\"", "") : "N/A";
                    Double cpu = (!campos[1].isEmpty()) ? Double.parseDouble(campos[1]) : null;
                    Double ramTotal = (!campos[2].isEmpty()) ? Double.parseDouble(campos[2]) : null;
                    Double ramUsada = (!campos[3].isEmpty()) ? Double.parseDouble(campos[3]) : null;
                    Double discoTotal = (!campos[4].isEmpty()) ? Double.parseDouble(campos[4]) : null;
                    Double discoUsado = (!campos[5].isEmpty()) ? Double.parseDouble(campos[5]) : null;
                    Integer numProcessos = (!campos[6].isEmpty()) ? Integer.parseInt(campos[6]) : null;
                    String codigoMaquina = (!campos[7].isEmpty()) ? campos[7].replaceAll("\"", "") : "N/A";
                    String nomeEmpresa = (!campos[8].isEmpty()) ? campos[8].replaceAll("\"", "") : "N/A";
                    ;
                    String nomeSetor = (!campos[9].isEmpty()) ? campos[9].replaceAll("\"", "") : "N/A";
                    ;
                    String top5Processos = (!campos[10].isEmpty()) ? campos[10] : "N/A";

//
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

    public static void deletarCsvMain() throws AwsServiceException {
        System.out.println("Excluindo o main.csv anterior(Bucket S3)...");

        // cria um cliente S3 que sera usado para fazer as acoes no bucket
        S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket("trusted-1d4a3f130793f4b0dfc576791dd86b32")
                .key("main.csv")
                .build();

        s3.deleteObject(deleteObjectRequest);
        System.out.println("Excluido com sucesso.");
    }

    public static void enviaCsvParaBucketTrusted(String nomeArqLocal, String nomeBucket){
        System.out.println("Enviando '" + nomeArqLocal + "' para o main.csv do Bucket '" + nomeBucket + "'...");

        List<Captura> listaCapturaLocal = new ArrayList<>(leCsvLocal(nomeArqLocal));
        System.out.println("Csv local ...");
        exibeListaCapturas(listaCapturaLocal);

        List<Captura> listaCapturaBucket = new ArrayList<>(leCsvBucketTrusted(nomeBucket));
        System.out.println("Csv que está no Trusted ...");
        exibeListaCapturas(listaCapturaBucket);

        List<Captura> listaFinal = new ArrayList<>();
        listaFinal.addAll(listaCapturaBucket);
        listaFinal.addAll(listaCapturaLocal);

        System.out.println("Csv final (Trusted + Local)");
        exibeListaCapturas(listaFinal);
        criaCsv(listaFinal, "mainEnviar");

        try {
            deletarCsvMain();
            // puxa as credencias setadas no 'aws configure'
            ProfileCredentialsProvider credenciais = ProfileCredentialsProvider.create();
            credenciais.resolveCredentials();

            // cria um cliente S3 que sera usado para fazer as acoes no bucket
            S3Client s3 = S3Client.builder().region(Region.US_EAST_1).credentialsProvider(credenciais).build();

            // cria um objeto requisicao para adicionar um novo objeto num Bucket S3
            PutObjectRequest requisicao = PutObjectRequest.builder().bucket(nomeBucket).key("main.csv").build();

            // usa esse objeto no cliente s3
            //s3.putObject(requisicao, Paths.get("mainEnviar.csv"));
            s3.putObject(requisicao, Paths.get("/tmp/mainEnviar.csv"));
            System.out.println("Upload concluído.");
        }
        catch (AwsServiceException erro) {
            System.out.println("Erro ao conectar nos serviços AWS.");
            System.out.println(erro.awsErrorDetails());
            System.exit(1);
        }
    }
}
