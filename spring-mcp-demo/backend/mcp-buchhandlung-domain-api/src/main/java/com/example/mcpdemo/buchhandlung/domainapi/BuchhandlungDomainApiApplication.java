package com.example.mcpdemo.buchhandlung.domainapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Buchhandlung Domain API",
        description = "Fach-API der Buchhandlung-Domaene: Buecher, Bestellungen und die "
                + "serverseitige Summenberechnung (Menge x Preis, optional nach Kunde und "
                + "Zeitraum gefiltert).",
        version = "0.1.0"
))
public class BuchhandlungDomainApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuchhandlungDomainApiApplication.class, args);
    }
}
