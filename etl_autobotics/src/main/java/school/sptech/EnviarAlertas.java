package school.sptech;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EnviarAlertas {

    private static final String JIRA_URL = "https://autoboticssptech.atlassian.net/rest/servicedeskapi/request";
    private static final String EMAIL = "autobotics.sptech@gmail.com";
    private static final String API_TOKEN =""; // coloque seu token aqui

    private static final String SERVICE_DESK_ID = "1"; // Alertas
    private static final String REQUEST_TYPE_ID = "6"; // Report a bug

    public static void criarTicket(String componente, double valor, String tipoAlerta, String descricaoDetalhada) {
        try {
            URL url = new URL(JIRA_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);

            // Autenticação básica
            String auth = EMAIL + ":" + API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            con.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Emoji no título
            String emoji = tipoAlerta.equalsIgnoreCase("crítico") ? "🔴" : "🟠";
            String titulo = String.format("%s Alerta %s - %s (%.2f%%)", emoji, tipoAlerta.toUpperCase(), componente, valor);

            // Monta o JSON com escape seguro
            String jsonInputString = String.format(
                    "{ \"serviceDeskId\": \"%s\", \"requestTypeId\": \"%s\", " +
                            "\"requestFieldValues\": { \"summary\": \"%s\", \"description\": \"%s\" } }",
                    SERVICE_DESK_ID, REQUEST_TYPE_ID,
                    escapeJson(titulo),
                    escapeJson(descricaoDetalhada)
            );

            // Envia
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lê resposta
            int code = con.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream(),
                    StandardCharsets.UTF_8
            ));

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.printf("Jira retorno (%d): %s%n", code, response);

        } catch (Exception e) {
            System.err.println("Erro ao enviar alerta ao Jira:");
            e.printStackTrace();
        }
    }

    // Escapa aspas, barras e quebras de linha
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
