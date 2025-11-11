package com.example.apitest.service;

import com.example.apitest.DTO.GenerateWebhookRequest;
import com.example.apitest.DTO.GenerateWebhookResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookService {

    private static final String URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public void generateWebhook() {
        RestTemplate restTemplate = new RestTemplate();

        GenerateWebhookRequest request = new GenerateWebhookRequest(
                "Vinay Dupad",
                "U25UV22T029078",
                "vinay.dupad@campusuvce.in"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(request, headers);

        // --- Call the generateWebhook API ---
        ResponseEntity<GenerateWebhookResponse> response =
                restTemplate.exchange(URL, HttpMethod.POST, entity, GenerateWebhookResponse.class);

        if (response.getBody() == null) {
            System.out.println("❌ No response from API");
            return;
        }

        String webhookUrl = response.getBody().getWebhook();
        String accessToken = response.getBody().getAccessToken();

        System.out.println("✅ Webhook URL: " + webhookUrl);
        System.out.println("✅ Access Token: " + accessToken);

        // --- Now send your SQL query ---
        sendFinalSql(restTemplate, webhookUrl, accessToken);
    }

    private void sendFinalSql(RestTemplate restTemplate, String webhookUrl, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken); // token goes directly here

        // 👉 Replace this SQL with your actual answer
        String finalSql = """
WITH ranked AS (
    SELECT 
        e.EMP_ID,
        e.FIRST_NAME,
        e.LAST_NAME,
        e.DOB,
        d.DEPARTMENT_NAME,
        RANK() OVER (PARTITION BY e.DEPARTMENT ORDER BY e.DOB DESC) AS age_rank_desc
    FROM EMPLOYEE e
    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
)
SELECT 
    EMP_ID,
    FIRST_NAME,
    LAST_NAME,
    DEPARTMENT_NAME,
    (age_rank_desc - 1) AS YOUNGER_EMPLOYEES_COUNT
FROM ranked
ORDER BY EMP_ID DESC;
""";

        Map<String, String> body = Map.of("finalQuery", finalSql);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
        System.out.println("✅ Submission Response: " + response.getBody());
    }
}
