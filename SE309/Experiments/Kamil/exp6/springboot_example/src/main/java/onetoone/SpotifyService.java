package onetoone;


import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import org.springframework.stereotype.Service;

/*
    Library that I found online for running the API.
 */
@Service
public class SpotifyService {

    private static final String clientId = "841ec1155c594334a5d54fa6069569f8";
    private static final String clientSecret = "f4386e2e1b0548e9851cd9a026664ab4";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();

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