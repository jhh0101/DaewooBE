package com.example.daewoo.common.toss.service;

import com.example.daewoo.common.toss.dto.TossPaymentDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Service
public class TossPaymentsService {

    @Value("${toss.payment.secretKey}")
    private String secretKey;

    private final RestTemplate restTemplate;
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";


    public TossPaymentsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonNode confirmPayment(TossPaymentDto tossPaymentDto) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<TossPaymentDto> entity = new HttpEntity<>(tossPaymentDto, headers);

        try {
            return restTemplate.postForObject(TOSS_API_URL, entity, JsonNode.class);
        } catch (HttpClientErrorException e) {
            // Toss API에서 에러 응답이 온 경우
            throw new Exception("Toss Payments API Error: " + e.getResponseBodyAsString());
        }
    }
}