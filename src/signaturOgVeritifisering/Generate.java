package signaturOgVeritifisering;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.util.Scanner;

class Generate {

    public static void main(String[] args) {

        Scanner scann = new Scanner(System.in);
        System.out.print("please enter file that you want to have signed: ");
        String nameOfFile = scann.nextLine();

        if (nameOfFile.length() == 0) {
            System.out.println("Usage: GenSig nameOfFileToSign");
        }
        else try {
            String pathToFile = "/home/adrian/IdeaProjects/oblig_4_datakom/src/signaturOgVeritifisering/" + nameOfFile;
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();

            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();
            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(priv);
            FileInputStream fis = new FileInputStream(pathToFile);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            }
            bufin.close();
            byte[] realSig = dsa.sign();

            FileOutputStream sigfos = new FileOutputStream("sig");
            sigfos.write(realSig);
            sigfos.close();

            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = new FileOutputStream("puplicKey");
            keyfos.write(key);
            keyfos.close();

            System.out.println("signature generated");


        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
    }
}