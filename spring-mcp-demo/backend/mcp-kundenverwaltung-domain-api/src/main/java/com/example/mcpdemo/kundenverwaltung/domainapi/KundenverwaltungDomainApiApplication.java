package com.example.mcpdemo.kundenverwaltung.domainapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Kundenverwaltung Domain API",
        description = "Fach-API der Kundenverwaltung-Domaene: Kundenstammdaten (Name, "
                + "E-Mail, Stadt).",
        version = "0.1.0"
))
public class KundenverwaltungDomainApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KundenverwaltungDomainApiApplication.class, args);
    }
}
