package test.com.accessibility.util.conf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.util.Base64;
import android.util.Log;

public class Crypt {

    private static final String tag = Crypt.class.getSimpleName();

    private static final String characterEncoding = "UTF-8";
    private static final String cipherTransformation = "AES/CBC/PKCS5Padding";
    private static final String aesEncryptionAlgorithm = "AES";
    private static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    private static byte[] keyBytes;

    private static Crypt instance = null;

    private String key;
    private String Tag;

    /* ****** Example Usage:

    String filename = "config.enc";
    String imei = "990000862471854";
    Crypt in = Crypt.getInstance(imei);
    String newfile = in.decrypt_file(context.getExternalCacheDir().toString(), filename);

    */


    Crypt(String inputKey)
    {
        key = inputKey;
        SecureRandom random = new SecureRandom();
        Crypt.ivBytes = new byte[16];
        random.nextBytes(Crypt.ivBytes);
        this.Tag = "Crypt";
    }

    public static Crypt getInstance(String inputKey) {
        if(instance == null){
            instance = new Crypt(inputKey);
        }

        return instance;
    }

    public String encrypt_string(final String plain) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException
    {
        return Base64.encodeToString(encrypt(plain.getBytes()), Base64.DEFAULT);
    }

    public String decrypt_string(final String plain) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException
    {
        byte[] encryptedBytes = decrypt(Base64.decode(plain, 0));
        return new String(encryptedBytes);
    }

    public String decrypt_file(String pathName, String filename)
    {
        String oldFile = pathName + File.separator + filename;
        try{
            FileInputStream inputStream = new FileInputStream(new File(oldFile));

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String receiveString = "";

            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            return decrypt_string(stringBuilder.toString());

        } catch (FileNotFoundException e) {
                Log.e(this.Tag, "File not found: " + e.toString());
        } catch (Exception e) {
                Log.e(this.Tag, e.toString());
        }

        return "";
    }


    public  byte[] encrypt(byte[] mes)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException, IOException {

        keyBytes = key.getBytes("UTF-8");

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(keyBytes);
        keyBytes = md.digest();

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, aesEncryptionAlgorithm);
        Cipher cipher = null;
        cipher = Cipher.getInstance(cipherTransformation);

        SecureRandom random = new SecureRandom();
        Crypt.ivBytes = new byte[16];
        random.nextBytes(Crypt.ivBytes);

        cipher.init(Cipher.ENCRYPT_MODE, newKey, random);
        byte[] destination = new byte[ivBytes.length + mes.length];
        System.arraycopy(ivBytes, 0, destination, 0, ivBytes.length);
        System.arraycopy(mes, 0, destination, ivBytes.length, mes.length);
        return  cipher.doFinal(destination);

    }

    public   byte[] decrypt(byte[] bytes)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException, IOException, ClassNotFoundException {

        keyBytes = key.getBytes("UTF-8");
        Log.d(tag,"Long KEY: "+keyBytes.length);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(keyBytes);
        keyBytes = md.digest();
        Log.d(tag,"Long KEY: "+keyBytes.length);

        byte[] ivB = Arrays.copyOfRange(bytes,0,16);
        Log.d(tag, "IV: "+new String(ivB));
        byte[] codB = Arrays.copyOfRange(bytes,16,bytes.length);


        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivB);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, aesEncryptionAlgorithm);
        Cipher cipher = Cipher.getInstance(cipherTransformation);
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        byte[] res = cipher.doFinal(codB);
        return  res;
    }

}

