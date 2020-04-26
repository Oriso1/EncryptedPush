package encryptedpush.com.encryptedpush.cipher;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import static java.security.spec.RSAKeyGenParameterSpec.F4;

public class KeyStoreHelper {

    private static final String TAG = "KeyStoreHelper";

    /**
     * Creates a public and private key and stores it using the Android Key
     * Store, so that only this application will be able to access the keys.
     */
    public static void createKeys(String alias){
        if (!isSigningKey(alias)) {
                createKeys(alias, false);
        }
    }

    public static void createKeysForSigning(String alias){
        if (!isSigningKey(alias)) {
            createKeysForSigning(alias, false);
        }
    }

    private static void createKeys(String alias, boolean requireAuth) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(alias,
                            KeyProperties.PURPOSE_ENCRYPT |
                                    KeyProperties.PURPOSE_DECRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .setUserAuthenticationRequired(requireAuth)
                            .build());

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Log.d(TAG, "Public Key is: " + keyPair.getPublic().toString());

        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createKeysForSigning(String alias, boolean requireAuth) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            alias,
                            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                            .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(1024, F4))
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            // Only permit the private key to be used if the user authenticated
                            // within the last five minutes.
                            .setUserAuthenticationRequired(requireAuth)
                            .build());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Log.d(TAG, "Public Key is: " + keyPair.getPublic().toString());

        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If Key with the default alias exists, returns true, else false.
     */
    private static boolean isSigningKey(String alias) {
            try {
                KeyStore keyStore = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
                keyStore.load(null);
                return keyStore.containsAlias(alias);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return false;
            }

    }

    private static Certificate getCertificate(String alias)
    {
        try {
            KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);
            return ks.getCertificate(alias);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private static PrivateKey getPrivateKeyEntry(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            ks.load(null);

            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, null);
            return privateKey;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public static String encrypt(String alias, String plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        if(plaintext == null)
            return "";

        PublicKey publicKey = getCertificate(alias).getPublicKey();
        PublicKey unrestrictedPublicKey = KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                new X509EncodedKeySpec(publicKey.getEncoded()));
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, unrestrictedPublicKey, spec);
        return Base64.encodeToString(cipher.doFinal(plaintext.getBytes()), Base64.NO_WRAP);

    }

    public static String decrypt(String alias, String ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        PrivateKey key  = getPrivateKeyEntry(alias);
        Cipher cipher = getCipher();
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)));
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(
                String.format("%s/%s/%s",
                        SecurityConstants.TYPE_RSA,
                        SecurityConstants.BLOCKING_MODE,
                        SecurityConstants.PADDING_TYPE));
    }

    public static String signData(String alias , String inputStr) throws KeyStoreException,
            NoSuchAlgorithmException,
            InvalidKeyException, SignatureException, IOException,
            CertificateException {
        byte[] data = inputStr.getBytes();

        KeyStore ks = KeyStore.getInstance(SecurityConstants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);

        ks.load(null);

        PrivateKey privateKey = getPrivateKeyEntry(alias);

        // This class doesn't actually represent the signature,
        // just the engine for creating/verifying signatures, using
        // the specified algorithm.
        Signature s = Signature.getInstance(SecurityConstants.SIGNATURE_SHA256withRSA);

        // Initialize Signature using specified private key
        s.initSign(privateKey);

        // Sign the data, store the result as a Base64 encoded String.
        s.update(data);
        byte[] signature = s.sign();
        String result;
        result = Base64.encodeToString(signature, Base64.DEFAULT);

        return result;
    }

    /**
     * Given some data and a signature, uses the key pair stored in the Android
     * Key Store to verify that the data was signed by this application, using
     * that key pair.
     *
     * @param input
     *            The data to be verified.
     * @param signatureStr
     *            The signature provided for the data.
     * @return A boolean value telling you whether the signature is valid or
     *         not.
     */
    public static boolean verifyData(String alias,String input, String signatureStr)
            throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException, UnrecoverableEntryException,
            InvalidKeyException, SignatureException {
        byte[] data = input.getBytes();
        byte[] signature;

        // Make sure the signature string exists. If not, bail out, nothing to
        // do.

        if (signatureStr == null) {
            Log.w(TAG, "Invalid signature.");
            Log.w(TAG, "Exiting verifyData()...");
            return false;
        }

        try {
            // The signature is going to be examined as a byte array,
            // not as a base64 encoded string.
            signature = Base64.decode(signatureStr, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            // signatureStr wasn't null, but might not have been encoded
            // properly.
            // It's not a valid Base64 string.
            return false;
        }

        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");

        ks.load(null);

        Certificate certificate = getCertificate(alias);

        // This class doesn't actually represent the signature,
        // just the engine for creating/verifying signatures, using
        // the specified algorithm.
        Signature s = Signature.getInstance(SecurityConstants.SIGNATURE_SHA256withRSA);

        // Verify the data.
        s.initVerify(certificate);
        s.update(data);
        boolean valid = s.verify(signature);
        return valid;

    }


    public interface SecurityConstants
    {
        String KEYSTORE_PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore";
        String TYPE_RSA = KeyProperties.KEY_ALGORITHM_RSA;
        String PADDING_TYPE = "OAEPWithSHA-256AndMGF1Padding";
        String BLOCKING_MODE = KeyProperties.BLOCK_MODE_ECB;

        String SIGNATURE_SHA256withRSA = "SHA256withRSA";
    }

}