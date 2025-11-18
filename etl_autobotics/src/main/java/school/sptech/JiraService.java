package school.sptech;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class JiraService {

    private static final String EMAIL = "autobotics.sptech@gmail.com";
    private static final String API_TOKEN = "";
    private static final String JIRA_SEARCH_URL = "https://autoboticssptech.atlassian.net/rest/api/3/search";

    private static String authHeader() {
        String auth = EMAIL + ":" + API_TOKEN;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }

    // Buscar tickets abertos
    public static JSONArray buscarTicketsAbertos() {
        String jql = "status != Done AND project = AL";
        return executarBusca(jql);
    }

    // Buscar tickets encerrados HOJE
    public static JSONArray buscarTicketsResolvidosHoje() {
        String jql = "status = Done AND resolved >= startOfDay() AND project = AL";
        return executarBusca(jql);
    }

    private static JSONArray executarBusca(String jql) {
        try {
            String urlStr = JIRA_SEARCH_URL + "?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
            URL url = new URL(urlStr);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", authHeader());
            con.setRequestProperty("Accept", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), StandardCharsets.UTF_8
            ));

            StringBuilder json = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) json.append(line);

            JSONObject response = new JSONObject(json.toString());
            return response.getJSONArray("issues");

        } catch (Exception e) {
            System.err.println("Erro ao consultar Jira:");
            e.printStackTrace();
            return new JSONArray();
        }
    }
}

