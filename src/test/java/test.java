import com.project_christopher.libraries.srp.Client;
import com.project_christopher.libraries.srp.Components.IVerifierAndSalt;
import com.project_christopher.libraries.srp.Components.M1AndA;
import com.project_christopher.libraries.srp.Exceptions.BadClientCredentials;
import com.project_christopher.libraries.srp.Exceptions.BadServerCredentials;
import com.project_christopher.libraries.srp.Modules.Parameters;
import com.project_christopher.libraries.srp.Modules.Routines;
import com.project_christopher.libraries.srp.Modules.Utils;
import com.project_christopher.libraries.srp.Server;
import jdk.nashorn.internal.parser.JSONParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class test {

    private static final int TIMEOUT = 15 * 1000;

    public static void main(String[] args) throws Exception {
        new test().test_exceptions();
    }

    @Test
    public void test_exceptions() throws Exception {
        System.out.println("Start of test file.");

        ArrayList<Document> db = new ArrayList<>();
        register(db);
        login(db);

        new Thread(() -> {
            Assertions.assertDoesNotThrow(() -> Thread.sleep(TIMEOUT));
            System.out.println("Timeout was reached.");
            System.exit(1);
        }).start();
    }

    public void register(ArrayList<Document> db) {
        // Client
        final String username = "projectChristopher";
        final String password = "password";

        Routines routines = new Routines(new Parameters());
        IVerifierAndSalt verifierAndSalt = Utils.generateVerifierAndSalt(routines, username, password);
        String salt = verifierAndSalt.salt;
        String verifier = verifierAndSalt.verifier;
        /* sendToServer(username, salt, verifier) */

        // Server
        /* storeToDatabase(username, salt, verifier) */
        db.add(new Document(username, salt, verifier));
    }

    public void login(ArrayList<Document> db) throws BadServerCredentials, BadClientCredentials {
        final String username = "projectChristopher";
        String password = "password";

        // Client
        Client client = new Client(new Routines(new Parameters()));
        client.step1(username, password);
        password = ""; // No longer needed.
        /* sendToServer(username) */

        // Server
        Server server = new Server(new Routines(new Parameters()));
        Document document = Document.find(db, username); // Search in db
        if(document == null) {
            // Send random data to avoid if user exists
            /* respondToClient(randomB, randomSalt) */
            return;
        }

        String salt = document.salt;
        String B = server.step1(username, salt, document.verifier); // Generate server's public key
        /* respondToClient(B, salt) */

        // Client
        M1AndA m1AndA = client.step2(salt, B);
        String A = m1AndA.A;
        String M1 = m1AndA.M1;
        /* sendToServer(A, M1) */

        // Server
        String M2 = server.step2(A, M1);
        /* respondToClient(M2) */

        // Client
        client.step3(M2);

        System.out.println("End of test file.");
        System.exit(0);
    }
}
