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
    private static final String API_TOKEN = "";

    // IDs válidos obtidos da API
    private static final String SERVICE_DESK_ID = "1";      // Alertas
    private static final String REQUEST_TYPE_ID = "6";      // Report a bug

    public static void criarTicket(String componente, double valor) {
        try {
            URL url = new URL(JIRA_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            // Autenticação básica com token do Jira
            String auth = EMAIL + ":" + API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            con.setRequestProperty("Authorization", "Basic " + encodedAuth);
            con.setDoOutput(true);

            // Corpo da requisição
            String jsonInputString = String.format(
                    "{ \"serviceDeskId\": \"%s\", \"requestTypeId\": \"%s\", " +
                            "\"requestFieldValues\": { \"summary\": \"Alerta - %s em %.2f%%\", " +
                            "\"description\": \"O componente %s atingiu %.2f%% de uso.\" } }",
                    SERVICE_DESK_ID, REQUEST_TYPE_ID, componente, valor, componente, valor
            );

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Ler a resposta
            int code = con.getResponseCode();
            BufferedReader br;
            if (code >= 200 && code < 300) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println("Código HTTP: " + code);
            System.out.println("Resposta do Jira: " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
