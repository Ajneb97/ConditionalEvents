package ce.ajneb97.managers;

import ce.ajneb97.model.internal.UpdateCheckerResult;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckerManager {

    private String version;
    private String latestVersion;

    public UpdateCheckerManager(String version){
        this.version = version;
    }

    public UpdateCheckerResult check(){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=82271").openConnection();
            int timed_out = 1500;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (latestVersion.length() <= 7) {
                if(!version.equals(latestVersion)){
                    return UpdateCheckerResult.noErrors(latestVersion);
                }
            }
            return UpdateCheckerResult.noErrors(null);
        } catch (Exception ex) {
            return UpdateCheckerResult.error();
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
