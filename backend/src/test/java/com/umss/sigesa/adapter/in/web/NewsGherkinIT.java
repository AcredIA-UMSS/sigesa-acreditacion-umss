package com.umss.sigesa.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.umss.sigesa.config.AuthDataLoader;
import com.umss.sigesa.config.NewsSeedDataLoader;
import com.umss.sigesa.domain.exception.PublicResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NewsGherkinIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Tag("PRD-US-027")
    @Tag("FSD-UC-035")
    @Tag("TC-NEWS")
    @DisplayName("Borrador no visible para visitante anónimo")
    void draftNotVisibleToAnonymous() throws Exception {
        String listBody = mockMvc.perform(get("/api/v1/public/news"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listBody).doesNotContain(NewsSeedDataLoader.SLUG_DRAFT);
        assertThat(listBody).doesNotContain("Borrador interno DUEA");

        mockMvc.perform(get("/api/v1/public/news/" + NewsSeedDataLoader.SLUG_DRAFT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));

        mockMvc.perform(get("/api/v1/public/news/slug-inexistente-news"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));

        String featured = mockMvc.perform(get("/api/v1/public/news/featured"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(featured).doesNotContain(NewsSeedDataLoader.SLUG_DRAFT);
    }

    @Test
    @Tag("PRD-US-028")
    @Tag("FSD-UC-035")
    @Tag("TC-NEWS")
    @DisplayName("[JD] publica una noticia y aparece en la home y en el listado")
    void publishAppearsInPublicListAndHome() throws Exception {
        String token = obtainSeedJdToken();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String id = createDraft(token, "Publicación visible " + suffix, "Cuerpo listo para publicar.");

        mockMvc.perform(post("/api/v1/admin/news/" + id + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLICADA"));

        String slug = "publicacion-visible-" + suffix;
        mockMvc.perform(get("/api/v1/public/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.slug=='" + slug + "')]").isNotEmpty());

        mockMvc.perform(get("/api/v1/public/news/" + slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Publicación visible " + suffix));
    }

    @Test
    @Tag("PRD-US-028")
    @Tag("FSD-UC-035")
    @Tag("TC-NEWS")
    @DisplayName("[JD] despublica y deja de aparecer")
    void unpublishRemovesFromPublicAndClearsFeatured() throws Exception {
        String token = obtainSeedJdToken();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String id = createDraft(token, "Temporal destacada " + suffix, "Cuerpo publicado.");

        mockMvc.perform(post("/api/v1/admin/news/" + id + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/news/" + id + "/feature")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(true));

        mockMvc.perform(post("/api/v1/admin/news/" + id + "/unpublish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BORRADOR"))
                .andExpect(jsonPath("$.featured").value(false));

        String slug = "temporal-destacada-" + suffix;
        String listBody = mockMvc.perform(get("/api/v1/public/news"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(listBody).doesNotContain(slug);

        mockMvc.perform(get("/api/v1/public/news/" + slug))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PublicResourceNotFoundException.CLIENT_MESSAGE));
    }

    @Test
    @Tag("PRD-US-029")
    @Tag("FSD-UC-035")
    @Tag("TC-NEWS")
    @DisplayName("Solo una destacada a la vez")
    void onlyOneFeaturedAtATime() throws Exception {
        String token = obtainSeedJdToken();
        String firstId = createDraft(token, "Primera destacada " + UUID.randomUUID().toString().substring(0, 8),
                "Cuerpo A");
        String secondId = createDraft(token, "Segunda destacada " + UUID.randomUUID().toString().substring(0, 8),
                "Cuerpo B");

        mockMvc.perform(post("/api/v1/admin/news/" + firstId + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/news/" + secondId + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/news/" + firstId + "/feature")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/news/" + secondId + "/feature")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(true));

        mockMvc.perform(get("/api/v1/admin/news/" + firstId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(false));

        String secondSlug = mockMvc.perform(get("/api/v1/admin/news/" + secondId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String slug = JsonPath.read(secondSlug, "$.slug");

        mockMvc.perform(get("/api/v1/public/news/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.featured").value(true));
    }

    @Test
    @Tag("PRD-US-028")
    @Tag("FSD-UC-035")
    @Tag("TC-NEWS")
    @DisplayName("Un usuario sin rol [JD] no puede crear ni editar")
    void nonJdCannotMutateNews() throws Exception {
        String before = mockMvc.perform(get("/api/v1/public/news?size=50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(post("/api/v1/admin/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsJson("Anónimo no crea", "EVENTO", "Resumen", "Cuerpo")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        String ccToken = obtainSeedCcToken();
        mockMvc.perform(post("/api/v1/admin/news")
                        .header("Authorization", "Bearer " + ccToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsJson("CC no crea", "EVENTO", "Resumen", "Cuerpo")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));

        String after = mockMvc.perform(get("/api/v1/public/news?size=50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(after).isEqualTo(before);
    }

    @Test
    @Tag("PRD-US-027")
    @Tag("FSD-UC-035")
    @Tag("TC-NEWS")
    @DisplayName("Sin destacada, la home muestra la más reciente")
    void featuredFallsBackToLatestPublished() throws Exception {
        String token = obtainSeedJdToken();
        String adminList = mockMvc.perform(get("/api/v1/admin/news?size=100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<String> featuredIds = JsonPath.read(adminList, "$.content[?(@.featured==true)].id");
        for (String featuredId : featuredIds) {
            mockMvc.perform(post("/api/v1/admin/news/" + featuredId + "/unpublish")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/v1/admin/news/" + featuredId + "/publish")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        String olderId = createDraft(token, "Antigua publicada " + UUID.randomUUID().toString().substring(0, 8),
                "Cuerpo antiguo");
        mockMvc.perform(post("/api/v1/admin/news/" + olderId + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        String newerTitle = "Reciente publicada " + UUID.randomUUID().toString().substring(0, 8);
        String newerId = createDraft(token, newerTitle, "Cuerpo reciente");
        String newerJson = mockMvc.perform(post("/api/v1/admin/news/" + newerId + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String newerSlug = JsonPath.read(newerJson, "$.slug");

        mockMvc.perform(get("/api/v1/public/news/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(newerSlug))
                .andExpect(jsonPath("$.featured").value(false));

        String listBody = mockMvc.perform(get("/api/v1/public/news?size=50"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(listBody).contains(newerSlug);
    }

    private String createDraft(String token, String title, String body) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/news")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsJson(title, "EVENTO", "Resumen de " + title, body)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
    }

    private static String newsJson(String title, String category, String summary, String body) {
        return """
                {"title":"%s","category":"%s","summary":"%s","body":"%s"}
                """.formatted(title, category, summary, body);
    }

    private String obtainSeedJdToken() throws Exception {
        return login(AuthDataLoader.SEED_JD_EMAIL, AuthDataLoader.SEED_JD_PASSWORD);
    }

    private String obtainSeedCcToken() throws Exception {
        return login(AuthDataLoader.SEED_CC_EMAIL, AuthDataLoader.SEED_CC_PASSWORD);
    }

    private String login(String email, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}
