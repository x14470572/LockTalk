package com.example.markenteder.locktalk;

import android.util.Log;
import android.widget.Toast;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class SecretKey {

    public byte[] GenDHSecret() {
        KeyPairGenerator aKPG, bKPG;
        KeyPair aKP, bKP;
        KeyAgreement aKA, bKA;
        KeyFactory bKF, aKF;
        X509EncodedKeySpec x509KeySpec;
        PublicKey aPK, bPK;
        DHParameterSpec aParamDHPK;
        byte[] aSecret = new byte[0];

        {
            try {
                // Person A creates their DH Key Pair with 1024-bit key size

                aKPG = KeyPairGenerator.getInstance("DH");
                aKPG.initialize(1024);
                aKP = aKPG.generateKeyPair();

                // Person A creates and initializes their DH KeyAgreement object

                aKA = KeyAgreement.getInstance("DH");
                aKA.init(aKP.getPrivate());

                //Person A encodes their public key, to be used by Person B.

                byte[] aPubKeyEnc = aKP.getPublic().getEncoded();

                bKF = KeyFactory.getInstance("DH");
                x509KeySpec = new X509EncodedKeySpec(aPubKeyEnc);

                aPK = bKF.generatePublic(x509KeySpec);

                aParamDHPK = ((DHPublicKey) aPK).getParams();

                bKPG = KeyPairGenerator.getInstance("DH");
                bKPG.initialize(aParamDHPK);
                bKP = bKPG.generateKeyPair();

                bKA = KeyAgreement.getInstance("DH");
                bKA.init(bKP.getPrivate());

                byte[] bPubKeyEnc = bKP.getPublic().getEncoded();

                aKF = KeyFactory.getInstance("DH");
                x509KeySpec = new X509EncodedKeySpec(bPubKeyEnc);
                bPK = aKF.generatePublic(x509KeySpec);
                aKA.doPhase(bPK, true);

                bKA.doPhase(aPK, true);

                aSecret = aKA.generateSecret();
                int aLen = aSecret.length;
                byte[] bSecret = new byte[aLen];
                int bLen;

                bLen = bKA.generateSecret(bSecret, 0);

                if (!java.util.Arrays.equals(aSecret, bSecret)) {
                    throw new Exception("Shared Secret is different");
                }


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (ShortBufferException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return aSecret;
    }
}
