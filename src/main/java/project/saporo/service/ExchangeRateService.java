package project.saporo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final RestTemplate restTemplate;

    private BigDecimal getKrwToJpyRate() {
        String url = "https://open.er-api.com/v6/latest/KRW";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("환율 API 호출 실패");
        }

        Map<String, Object> body = response.getBody();
        Map<String, Object> rates = (Map<String, Object>) body.get("rates");
        if (rates == null || rates.get("JPY") == null) {
            throw new IllegalStateException("JPY 환율이 없습니다.");
        }

        return new BigDecimal(rates.get("JPY").toString());
    }

    public BigDecimal getJpyToKrwRate() {
        BigDecimal jpyPerKrw = getKrwToJpyRate();
        return BigDecimal.ONE.divide(jpyPerKrw, 2, RoundingMode.HALF_UP);
    }
}