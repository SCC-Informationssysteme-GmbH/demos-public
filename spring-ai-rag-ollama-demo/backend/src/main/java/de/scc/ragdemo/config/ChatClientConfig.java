package de.scc.ragdemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Baut den zentralen {@link ChatClient} und haengt standardmaessig den
 * {@link QuestionAnswerAdvisor} ein: jede Anfrage wird zunaechst gegen den
 * Qdrant-Vektorspeicher ausgefuehrt, die Treffer werden als Kontext in den
 * Prompt eingefuegt (Retrieval-Augmented-Generation).
 */
@Configuration
public class ChatClientConfig {

    /**
     * Von {@link de.scc.ragdemo.controller.ChatController} wiederverwendet, um
     * fuer die Persona "Nur Kontext (streng)" selbst zu pruefen, ob ueberhaupt
     * passender Kontext existiert - siehe dort.
     */
    public static final int RETRIEVAL_TOP_K = 4;
    public static final double RETRIEVAL_SIMILARITY_THRESHOLD = 0.5d;

    /**
     * Der Standard-Prompt-Template von {@link QuestionAnswerAdvisor} haengt an
     * JEDE Frage fest die Anweisung "...and not prior knowledge... inform the
     * user that you can't answer" an (als User-Message, nicht als System-Prompt) -
     * das widerspricht Personas wie "Kontext + Allgemeinwissen", die bewusst auf
     * eigenes Wissen zurueckfallen sollen, und ueberstimmt deren System-Prompt.
     * Deshalb hier ein neutrales Template ohne eingebaute Verweigerungs-Klausel;
     * das gewuenschte Verhalten steuern stattdessen ausschliesslich die Personas
     * (system-prompts.json) sowie fuer "streng" zusaetzlich der Code-Check in
     * ChatController.
     */
    private static final PromptTemplate CONTEXT_PROMPT_TEMPLATE = new PromptTemplate("""
            {query}

            Kontext aus den indexierten Dokumenten:
            ---------------------
            {question_answer_context}
            ---------------------
            """);

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore, SystemPromptCatalog promptCatalog) {
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(RETRIEVAL_TOP_K)
                        .similarityThreshold(RETRIEVAL_SIMILARITY_THRESHOLD)
                        .build())
                .promptTemplate(CONTEXT_PROMPT_TEMPLATE)
                .build();

        return builder
                .defaultSystem(promptCatalog.defaultPrompt())
                .defaultAdvisors(questionAnswerAdvisor)
                .build();
    }
}
