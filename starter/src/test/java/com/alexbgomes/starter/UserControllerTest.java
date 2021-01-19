package com.alexbgomes.starter;

import com.alexbgomes.starter.data.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.hamcrest.CoreMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDataSql() throws Exception {
        final String user = "admin";
        final String pwd = "admin";

        MvcResult mvcResult = mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value(user))
                .andExpect(jsonPath("$[0].pwd").value(pwd))
                .andReturn();

        Assertions.assertEquals("application/json", mvcResult.getResponse().getContentType());
    }

    @Test
    public void testUserRegistration_badUsernameLength() throws Exception {
        final String username = "1234567";
        final String pwd = "@Bc23j3sd^3sd";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Username must be at least 8 characters and at most 30 characters.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserRegistration_badUsernameCharacters() throws Exception {
        final String username = "1234567!";
        final String pwd = "@Bc23j3sd^3sd";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Username must be alphanumeric only.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserRegistration_badPwdLength() throws Exception {
        final String username = "12345678";
        final String pwd = "@Bc23j3sd";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Password must be at least 10 characters and at most 18 characters.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserRegistration_badPwdCharacters() throws Exception {
        final String username = "12345678";
        final String pwd = "@Bc23j3sd^sd+\\";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Password must contain an uppercase, a lowercase, a number, and a special character.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserRegistration_success() throws Exception {
        final String username = "alexgomes";
        final String pwd = "P@ssword12";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("New user created.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/unregister")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(String.format("User %s removed.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserRegistration_userAlreadyExists() throws Exception {
        final String username = "alexgomes";
        final String pwd = "P@ssword12";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("New user created.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isConflict())
                .andReturn();

        Assertions.assertEquals(String.format("Username %s is already taken.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/unregister")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(String.format("User %s removed.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserLogin_UserDNE() throws Exception {
        final String username = "johndoe123";
        final String pwd = "@Bc23j3sd^3sd";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isNotFound())
                .andReturn();

        Assertions.assertEquals(String.format("User %s not found.", user.getUsername()), mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUserLogin_badPwdLength() throws Exception {
        final String username = "alexgomes";
        final String pwd = "P@ssword12";
        final User user = new User(username, pwd);
        final String loginPwd = "P@ssword";
        final User loginUser = new User(username, loginPwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("New user created.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Password must be at least 10 characters and at most 18 characters.", mvcResult.getResponse().getContentAsString());

        mvcResult = mockMvc.perform(post("/api/unregister")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(String.format("User %s removed.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserLogin_badPwdCharacters() throws Exception {
        final String username = "alexgomes";
        final String pwd = "P@ssword12";
        final User user = new User(username, pwd);
        final String loginPwd = "P@ssword12+\\";
        final User loginUser = new User(username, loginPwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("New user created.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Password must contain an uppercase, a lowercase, a number, and a special character.", mvcResult.getResponse().getContentAsString());

        mvcResult = mockMvc.perform(post("/api/unregister")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(String.format("User %s removed.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserLogin_incorrectPassword() throws Exception {
        final String username = "alexgomes";
        final String pwd = "P@ssword12";
        final User user = new User(username, pwd);
        final String loginPwd = "P@ssword122";
        final User loginUser = new User(username, loginPwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("New user created.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print()).andExpect(status().isNotFound())
                .andReturn();

        Assertions.assertEquals("Password is incorrect.", mvcResult.getResponse().getContentAsString());

        mvcResult = mockMvc.perform(post("/api/unregister")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(String.format("User %s removed.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }

    @Test
    public void testUserLogin_success() throws Exception {
        final String username = "alexgomes";
        final String pwd = "P@ssword12";
        final User user = new User(username, pwd);

        MvcResult mvcResult = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals("New user created.", mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        mvcResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString(), CoreMatchers.containsString(String.format("User %s logged in at ", user.getUsername())));

        mvcResult = mockMvc.perform(post("/api/unregister")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(String.format("User %s removed.", user.getUsername()), mvcResult.getResponse().getContentAsString());

        mockMvc.perform(get("/api/users"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();
    }
}
