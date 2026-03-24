package com.saborperu.api.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saborperu.api.api.dto.RecetaExternaDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class RecetaExternaService {

    private final RestTemplate restTemplate;

    @Value("${app.external.themealdb.base-url:https://www.themealdb.com/api/json/v1/1}")
    private String mealDbBaseUrl;

    public RecetaExternaService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }

    public List<RecetaExternaDTO> buscarRecetas(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String q = query.trim();
        String baseUrl = normalizeBaseUrl(mealDbBaseUrl);

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/search.php")
                .queryParam("s", q)
                .toUriString();

        try {
            TheMealDbResponse response = restTemplate.getForObject(url, TheMealDbResponse.class);
            if (response == null || response.meals == null || response.meals.isEmpty()) {
                return Collections.emptyList();
            }

            int safeLimit = Math.max(1, Math.min(limit, 20));

            return response.meals.stream()
                    .limit(safeLimit)
                    .map(this::mapToDto)
                    .toList();
        } catch (RestClientException ex) {
            log.warn("No fue posible consultar API externa TheMealDB. Se devuelve lista vacía. query={}", q, ex);
            return Collections.emptyList();
        }
    }

    private String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "https://www.themealdb.com/api/json/v1/1";
        }

        String base = raw.trim();
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private RecetaExternaDTO mapToDto(TheMealDbMeal meal) {
        String resumen = meal.strInstructions;
        if (resumen != null && resumen.length() > 220) {
            resumen = resumen.substring(0, 220);
        }

        return RecetaExternaDTO.builder()
                .proveedor("THEMEALDB")
                .idExterno(meal.idMeal)
                .titulo(meal.strMeal)
                .categoria(meal.strCategory)
                .origen(meal.strArea)
                .imagenUrl(meal.strMealThumb)
                .resumen(resumen)
                .build();
    }

    @Data
    private static class TheMealDbResponse {
        @JsonProperty("meals")
        private List<TheMealDbMeal> meals;
    }

    @Data
    private static class TheMealDbMeal {
        @JsonProperty("idMeal")
        private String idMeal;

        @JsonProperty("strMeal")
        private String strMeal;

        @JsonProperty("strCategory")
        private String strCategory;

        @JsonProperty("strArea")
        private String strArea;

        @JsonProperty("strInstructions")
        private String strInstructions;

        @JsonProperty("strMealThumb")
        private String strMealThumb;
    }
}
