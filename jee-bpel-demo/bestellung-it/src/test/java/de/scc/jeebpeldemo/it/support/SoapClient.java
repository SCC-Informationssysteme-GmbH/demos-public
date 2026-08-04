package de.scc.jeebpeldemo.it.support;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class SoapClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private SoapClient() {
    }

    public static String post(String url, String soapBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "text/xml")
                .header("SOAPAction", "\"\"")
                .POST(HttpRequest.BodyPublishers.ofString(soapBody, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static String extractElementText(String xml, String localName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList nodes = document.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            throw new IllegalStateException("Element <" + localName + "> nicht gefunden in: " + xml);
        }
        return nodes.item(0).getTextContent();
    }
}
