
package accesscheck;

import java.util.Base64;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import configuration.*;

public class AccessCheck {

  ConfigurationReader configReader = new ConfigurationReader();
  Config authHtpasswd;

  public AccessCheck() {
    this.authHtpasswd = configReader.getConfig("AUTH_HTPASSWD");
  }

  public AccessCheck(int test) {
    this.authHtpasswd = configReader.getConfig("AUTH_HTPASSWD");
    this.testAccesCheck();
  }

  public boolean isAuthorized(String authInfo) {
    String credentials = new String(
      Base64.getDecoder().decode(authInfo),
      Charset.forName("UTF-8")
    );

    String[] tokens = credentials.split(":");

    if (tokens.length == 2) {
      return verifyPassword(tokens[0], tokens[1]);
    }

    return false;
  }

  private boolean verifyPassword(String username, String password) {
    String givenPassword = encryptClearPassword(password);
    String storedPassword;

    try {
      storedPassword = authHtpasswd.lookUp(username, "PASSWORD");

    } catch (NullPointerException e) {
      return false;
    }

    if (password == storedPassword) {
      return true;
    }

    return false;
  }

  private String encryptClearPassword(String password) {
    try {
      MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
      byte[] result = mDigest.digest(password.getBytes());

     return Base64.getEncoder().encodeToString(result);
    } catch(Exception e) {
      return "";
    }
  }

  private void testAccesCheck() {
    String username = "jrob";
    String password = authHtpasswd.lookUp(username, "PASSWORD");

    final String HR = "-------------";
    System.out.printf("%13s%-33s%13s\n", HR, "  Initializing AccessCheck Test  ", HR);
    System.out.printf("%10s%-45s\n%10s%-45s\n", "Username: ", username, "Password: ", password);

    Boolean testPassed = verifyPassword("jrob", password);
    System.out.printf("%10s%-45s\n", "Match?: ", testPassed);
    if (testPassed) {
      System.out.printf("%13s%-33s%13s\n", HR, "     AccessCheck Test Passed     ", HR);
    } else {
      System.out.printf("%13s%-33s%13s\n", HR, "     AccessCheck Test Failed     ", HR);
    }
  }

  public static void main(String[] args) {
    AccessCheck accessCheck = new AccessCheck(-1);
  }
}