package school.sptech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GerarCsvTickets {

    public static void gerarCsv(JSONArray abertos, JSONArray resolvidos) {

        String nomeArquivo = "tickets_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv";

        try (FileWriter writer = new FileWriter(nomeArquivo)) {

            writer.write("ticketKey,componente,criticidade,percentual,status,criadoEm,resolvidoEm\n");

            // Tickets abertos
            for (int i = 0; i < abertos.length(); i++) {
                JSONObject t = abertos.getJSONObject(i);
                escreverLinha(writer, t, "ABERTO");
            }

            // Tickets resolvidos hoje
            for (int i = 0; i < resolvidos.length(); i++) {
                JSONObject t = resolvidos.getJSONObject(i);
                escreverLinha(writer, t, "RESOLVIDO HOJE");
            }

            System.out.println("CSV de tickets gerado: " + nomeArquivo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void escreverLinha(FileWriter writer, JSONObject ticket, String status) throws Exception {
        String key = ticket.getString("key");

        JSONObject fields = ticket.getJSONObject("fields");

        String resumo = fields.getString("summary");

        // Extrair dados do título que você mesmo criou
        // Exemplo: 🔴 Alerta CRÍTICO - CPU (92%)
        String componente = extrairComponente(resumo);
        String percentual = extrairPercentual(resumo);
        String criticidade = extrairCriticidade(resumo);

        String criado = fields.getString("created");
        String resolvido = fields.has("resolutiondate") ? fields.optString("resolutiondate", "") : "";

        writer.write(String.format(
                "%s,%s,%s,%s,%s,%s,%s\n",
                key, componente, criticidade, percentual, status, criado, resolvido
        ));
    }

    private static String extrairComponente(String resumo) {
        if (resumo.contains("CPU")) return "CPU";
        if (resumo.contains("RAM")) return "RAM";
        if (resumo.contains("disco") || resumo.contains("Disco")) return "Disco";
        return "Desconhecido";
    }

    private static String extrairPercentual(String resumo) {
        int ini = resumo.indexOf("(");
        int fim = resumo.indexOf("%");
        if (ini != -1 && fim != -1) {
            return resumo.substring(ini + 1, fim);
        }
        return "";
    }

    private static String extrairCriticidade(String resumo) {
        if (resumo.contains("CRÍTICO") || resumo.contains("crítico")) return "Crítico";
        if (resumo.contains("MÉDIO") || resumo.contains("médio")) return "Médio";
        return "Desconhecido";
    }
}

