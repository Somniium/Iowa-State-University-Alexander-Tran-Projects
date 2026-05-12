package onetoone.Albums;


import jakarta.annotation.PostConstruct;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
    Library that I found online for running the API.
 */
@Service
public class SpotifyService {

    // Credentials are injected from application.properties — never hardcode secrets in source.
    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }

    public void authenticate() {
        try {
            ClientCredentials credentials = spotifyApi.clientCredentials().build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());
            System.out.println("Authenticated! Token expires in: " + credentials.getExpiresIn());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public SpotifyApi getApi() {
        return spotifyApi;
    }
}