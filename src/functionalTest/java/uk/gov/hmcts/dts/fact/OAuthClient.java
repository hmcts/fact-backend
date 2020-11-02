package uk.gov.hmcts.dts.fact;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static io.restassured.RestAssured.given;

@Component
public class OAuthClient {

    @Value("${OAUTH_CLIENT_ID:fact_admin}")
    private String clientId;

    @Value("${OAUTH_SECRET:fact_admin_secret}")
    private String clientSecret;

    @Value("${OAUTH_REDIRECT:http://localhost:3300/oauth2/callback}")
    private String redirectUri;

    @Value("${OAUTH_PROVIDER_URL:https://idam-api.aat.platform.hmcts.net}")
    private String providerUrl;

    @Value("${OAUTH_USER:hmcts.fact@gmail.com}")
    private String username;

    @Value("${OAUTH_USER_PASSWORD:Pa55word11}")
    private String password;


    public String getToken() {
        return generateClientToken(username, password);
    }

    public String generateClientToken(String userName, String password) {
        String code = generateClientCode(userName, password);
        String token = given()
            .relaxedHTTPSValidation()
            .baseUri(providerUrl)
            .post("/oauth2/token" + "?code=" + code + "&client_secret=" + clientSecret + "&client_id=" + clientId + "&redirect_uri=" + redirectUri + "&grant_type=authorization_code")
            .body()
            .jsonPath()
            .get("id_token");

        if (token == null) {
            throw new AuthException(
                String.format(
                    "Unable to get token with %s %s %s %s %s",
                    providerUrl,
                    clientId,
                    clientSecret,
                    redirectUri,
                    code
                )
            );
        }

        return token;
    }


    private String generateClientCode(String userName, String password) {
        final String encoded = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());

        String code = given()
            .relaxedHTTPSValidation()
            .baseUri(providerUrl)
            .header("Authorization", "Basic " + encoded)
            .post("/oauth2/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri)
            .body()
            .jsonPath()
            .get("code");

        if (code == null) {
            throw new AuthException(
                String.format(
                    "Unable to get auth code with %s %s %s %s %s",
                    providerUrl,
                    clientId,
                    redirectUri,
                    userName,
                    password
                )
            );
        }

        return code;
    }
}
