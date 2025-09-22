package com.dtsx.astra.cli.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Function;

@UtilityClass
public class HttpUtils {
    @SneakyThrows
    public static HttpResponse<String> GET(String url, Function<HttpClient.Builder, HttpClient.Builder> clientBuilderFn, Function<HttpRequest.Builder, HttpRequest.Builder> reqBuilderFn) {
        try {
            @Cleanup val client = clientBuilderFn.apply(
                HttpClient.newBuilder()
                    .followRedirects(Redirect.NORMAL)
                    .version(Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(20))
            ).build();

            val request = reqBuilderFn.apply(
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .GET()
            ).build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
