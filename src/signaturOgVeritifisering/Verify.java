package signaturOgVeritifisering;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.Scanner;

public class Verify {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        System.out.println("please enter file to verify: ");
        String nameOfFile = input.nextLine();
        String pathToFile = "/home/adrian/IdeaProjects/oblig_4_datakom/" + nameOfFile;
        String pathToPublicKey = "/home/adrian/IdeaProjects/oblig_4_datakom/puplicKey";
        String pathToSignature = "/home/adrian/IdeaProjects/oblig_4_datakom/sig";

        if (nameOfFile.length() == 0) {
            System.out.println("please enter filename to verify");
        } else try {
 

            FileInputStream keyfis = new FileInputStream(pathToPublicKey);
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);

            keyfis.close();

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);

            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
 
            FileInputStream sigfis = new FileInputStream(pathToSignature);
            byte[] sigToVerify = new byte[sigfis.available()];
            sigfis.read(sigToVerify);

            sigfis.close();
 
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(pubKey);
 

            FileInputStream datafis = new FileInputStream(pathToFile);
            BufferedInputStream bufin = new BufferedInputStream(datafis);

            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                sig.update(buffer, 0, len);
            }
            ;

            bufin.close();


            boolean verifies = sig.verify(sigToVerify);

            System.out.println("signature verifies: " + verifies);


        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
        ;

    }
}