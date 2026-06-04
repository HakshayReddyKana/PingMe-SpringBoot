package com.hakshay.chat.service;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class KeyService {
        public static KeyPair generateRsaKey() {
            KeyPair keyPair;
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                keyPair = keyPairGenerator.generateKeyPair();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
            String key = Base64.getEncoder().encodeToString(pub.getEncoded());

            System.out.println("------------------------------------------------");
            System.out.println("PUBLIC KEY TO VERIFY JWT:");
            System.out.println("-----BEGIN PUBLIC KEY-----");
            System.out.println(key);
            System.out.println("-----END PUBLIC KEY-----");
            System.out.println("------------------------------------------------");
            return keyPair;
        }
}
