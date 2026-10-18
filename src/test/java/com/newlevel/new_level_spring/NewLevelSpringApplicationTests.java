package com.newlevel.new_level_spring;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class NewLevelSpringApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void contextLoadsWithTestProfileAndInMemoryDatabase() {
	}

	@Test
	void rejectsRequestsWithoutJwt() throws Exception {
		mockMvc.perform(get("/api/task"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/event"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/user/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsRequestsWithInvalidJwt() throws Exception {
		when(jwtDecoder.decode("invalid-token"))
			.thenThrow(new BadJwtException("Invalid token"));

		mockMvc.perform(get("/api/task")
				.header("Authorization", "Bearer invalid-token"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void createsUserAndTaskThroughAuthenticatedHttpEndpoints() throws Exception {
		String auth0Id = "integration-" + UUID.randomUUID();
		mockMvc.perform(post("/api/user")
				.with(jwt().jwt(token -> token.subject(auth0Id)
					.claim("https://newlevelauth.com/name", "Integration User"))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("Integration User"));

		mockMvc.perform(post("/api/task")
				.with(jwt().jwt(token -> token.subject(auth0Id)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "Integration task",
					  "category": "STUDY",
					  "difficulty": "EASY",
					  "subtasks": [{"title": "Step one"}]
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Integration task"))
			.andExpect(jsonPath("$.subtasks[0].title").value("Step one"));
	}

}
