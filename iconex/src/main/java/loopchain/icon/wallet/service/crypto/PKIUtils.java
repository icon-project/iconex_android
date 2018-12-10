package loopchain.icon.wallet.service.crypto;

import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import loopchain.icon.wallet.core.Constants;


public class PKIUtils {

    public static final String PROVIDER = "SC";
    public static final String ALGORITHM_KEY = "EC";
    public static final String ALGORITHM_PARAM = "secp256k1";
    public static final int ALGORITHM_KEY_LENGTH = 32;
    public static final String ALGORITHM_HASH = "SHA3-256";
    public static final String ETHER_ADD_ALGO = "Keccak-256";


    private static final ECParameterSpec EC_SPEC = ECNamedCurveTable.getParameterSpec(ALGORITHM_PARAM);

    public static byte[] hash(byte[] message, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest md = MessageDigest.getInstance(algorithm, PROVIDER);
        return md.digest(message);
    }

    public static String b64Encode(byte[] message) {
        return Base64.toBase64String(message);
    }

    public static byte[] b64Decode(String b64Data) {
        return Base64.decode(b64Data);
    }

    public static String hexEncode(byte[] message) {
        return Hex.toHexString(message);
    }

    public static byte[] hexDecode(String hexData) {
        return Hex.decode(hexData);
    }

    public static KeyPair generateKey() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM_KEY, PROVIDER);
        kpg.initialize(EC_SPEC, new SecureRandom());
        return kpg.generateKeyPair();
    }

    public static boolean checkPrivateKey(byte[] input) {
        BigInteger n = EC_SPEC.getN();

        BigInteger d = new BigInteger(1, input);
        if (d.compareTo(BigInteger.valueOf(2)) < 0 || (d.compareTo(n) >= 0))
            return false;
        else
            return true;
    }

    public static String makeAddressFromPrivateKey(byte[] privateKey, String coinType) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] publicKey = getPublicKeyFromPrivateKey(privateKey, false);
        if (coinType.equals(Constants.KS_COINTYPE_ICX))
            return makeAddress(publicKey);
        else
            return makeEtherAddress(publicKey);
    }

    /**
     * ICX address
     *
     * @param publicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static String makeAddress(byte[] publicKey) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM_HASH, PROVIDER);
        byte[] hash = null;
        if (publicKey.length > 64) {
            md.update(publicKey, 1, 64);
            hash = md.digest();
        } else
            hash = md.digest(publicKey);

        return "hx" + Hex.toHexString(hash, hash.length - 20, 20);
    }

    /**
     * Ether address
     *
     * @param publicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static String makeEtherAddress(byte[] publicKey) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest md = MessageDigest.getInstance(ETHER_ADD_ALGO, PROVIDER);
        byte[] hash = null;
        if (publicKey.length > 64) {
            md.update(publicKey, 1, 64);
            hash = md.digest();
        } else
            hash = md.digest(publicKey);

        return Hex.toHexString(hash, hash.length - 20, 20);
    }

    public static byte[] getPublicKeyFromPrivateKey(byte[] privBytes, boolean addPrefix) {
        ECPoint pointQ = EC_SPEC.getG().multiply(new BigInteger(1, privBytes));
        if (addPrefix)
            return pointQ.getEncoded(false);
        else
            return ecpoint2bytes(pointQ);
    }

    public static PrivateKey bytes2ECPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM_KEY, PROVIDER);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    public static PublicKey bytes2ECPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM_KEY, PROVIDER);
        return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    //UnsignedByte
    public static byte[] ecPrivateKey2Bytes(PrivateKey privKey) {
        if (privKey instanceof ECPrivateKey) {
            BigInteger d = ((ECPrivateKey) privKey).getD();
            return asUnsignedByteArray(ALGORITHM_KEY_LENGTH, d);
        } else {
            return null;
        }
    }

    //UnsignedByte, Uncompress, UnPrefix
    public static byte[] ecPublicKey2Bytes(PublicKey pubKey, boolean addPrefix) {
        if (pubKey instanceof ECPublicKey) {
            ECPoint q = ((ECPublicKey) pubKey).getQ().normalize();
            if (addPrefix)
                return q.getEncoded(false);
            else
                return ecpoint2bytes(q);
        } else {
            return null;
        }
    }

    public static BigInteger[] sign(byte[] message, byte[] privKey) {
        ECDomainParameters domain = new ECDomainParameters(EC_SPEC.getCurve(), EC_SPEC.getG(), EC_SPEC.getN());

        BigInteger d = new BigInteger(1, privKey);
        ECPrivateKeyParameters privateKeyParms = new ECPrivateKeyParameters(d, domain);

        ECDSASigner ecdsaSigner = new ECDSASigner();
        ecdsaSigner.init(true, privateKeyParms);

        return ecdsaSigner.generateSignature(message);
    }

    public static String sign(byte[] hashedTBS, String hexPrivKey) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] privateKey = Hex.decode(hexPrivKey);
        BigInteger[] sign = PKIUtils.sign(hashedTBS, privateKey);
        byte[] publicKey = PKIUtils.getPublicKeyFromPrivateKey(privateKey, true);

        byte recoveryId = PKIUtils.getRecoveryId(sign, hashedTBS, publicKey);
        System.out.println("KS recoveryID=" + recoveryId);
        byte[] signData = PKIUtils.getSignature(sign[0], sign[1], new byte[]{recoveryId});
        return PKIUtils.b64Encode(signData);
    }

    public static byte[] verify(byte[] hashedTBS, String b64Signature) {
        byte[] signature = PKIUtils.b64Decode(b64Signature);
        return PKIUtils.recoverPublicKey(signature, hashedTBS);
    }

    public static byte[] getSignature(BigInteger R, BigInteger S, byte[] recoveryId) {
        byte[] r = asUnsignedByteArray(ALGORITHM_KEY_LENGTH, R);
        byte[] s = asUnsignedByteArray(ALGORITHM_KEY_LENGTH, S);
        byte[] signature = new byte[r.length + s.length + 1];

        System.arraycopy(r, 0, signature, 0, r.length);
        System.arraycopy(s, 0, signature, r.length, s.length);
        System.arraycopy(recoveryId, 0, signature, r.length + s.length, 1);
        return signature;
    }

    public static byte[][] fromSignature(byte[] signature) {
        byte[][] result = new byte[3][];
        result[0] = new byte[32];
        result[1] = new byte[32];
        result[2] = new byte[1];
        System.arraycopy(signature, 0, result[0], 0, 32);
        System.arraycopy(signature, 32, result[1], 0, 32);
        System.arraycopy(signature, 64, result[2], 0, 1);

        return result;
    }

    public static byte[] recoverPublicKey(byte[] signature, byte[] message) {
        byte[] sigR = new byte[32];
        byte[] sigS = new byte[32];
        byte[] sigV = new byte[1];
        System.arraycopy(signature, 0, sigR, 0, 32);
        System.arraycopy(signature, 32, sigS, 0, 32);
        System.arraycopy(signature, 64, sigV, 0, 1);

        BigInteger pointN = EC_SPEC.getN();

        BigInteger pointX = new BigInteger(1, sigR);

        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(pointX, 1 + x9.getByteLength(EC_SPEC.getCurve()));
        compEnc[0] = (byte) ((sigV[0] & 1) == 1 ? 0x03 : 0x02);
        ECPoint pointR = EC_SPEC.getCurve().decodePoint(compEnc);
        if (!pointR.multiply(pointN).isInfinity()) {
            return new byte[0];
        }

        BigInteger pointE = new BigInteger(1, message);
        BigInteger pointEInv = BigInteger.ZERO.subtract(pointE).mod(pointN);
        BigInteger pointRInv = new BigInteger(1, sigR).modInverse(pointN);
        BigInteger srInv = pointRInv.multiply(new BigInteger(1, sigS)).mod(pointN);
        BigInteger pointEInvRInv = pointRInv.multiply(pointEInv).mod(pointN);
        ECPoint pointQ = ECAlgorithms.sumOfTwoMultiplies(EC_SPEC.getG(), pointEInvRInv, pointR, srInv);
        return pointQ.getEncoded(false);
    }

    public static byte[] recoverPublicKey(byte[] sigR, byte[] sigS, byte[] sigV, byte[] message) {
        BigInteger pointN = EC_SPEC.getN();

        BigInteger pointX = new BigInteger(1, sigR);

        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(pointX, 1 + x9.getByteLength(EC_SPEC.getCurve()));
        compEnc[0] = (byte) ((sigV[0] & 1) == 1 ? 0x03 : 0x02);
        ECPoint pointR = EC_SPEC.getCurve().decodePoint(compEnc);
        if (!pointR.multiply(pointN).isInfinity()) {
            return new byte[0];
        }

        BigInteger pointE = new BigInteger(1, message);
        BigInteger pointEInv = BigInteger.ZERO.subtract(pointE).mod(pointN);
        BigInteger pointRInv = new BigInteger(1, sigR).modInverse(pointN);
        BigInteger srInv = pointRInv.multiply(new BigInteger(1, sigS)).mod(pointN);
        BigInteger pointEInvRInv = pointRInv.multiply(pointEInv).mod(pointN);
        ECPoint pointQ = ECAlgorithms.sumOfTwoMultiplies(EC_SPEC.getG(), pointEInvRInv, pointR, srInv);
        return pointQ.getEncoded(false);
    }

    public static boolean verify(byte[] message, byte[] pubKey, byte[] signR, byte[] signS) {
        ECDomainParameters domain = new ECDomainParameters(EC_SPEC.getCurve(), EC_SPEC.getG(), EC_SPEC.getN());

        ECDSASigner ecdsaSigner = new ECDSASigner();
        ECPublicKeyParameters publicKeyParms = new ECPublicKeyParameters(EC_SPEC.getCurve().decodePoint(pubKey), domain);

        ecdsaSigner.init(false, publicKeyParms);
        return ecdsaSigner.verifySignature(message, new BigInteger(1, signR), new BigInteger(1, signS));
    }

    public static byte getRecoveryId(BigInteger[] sign, byte[] message, byte[] publicKey) {
        BigInteger pointN = EC_SPEC.getN();

        for (int recoveryId = 0; recoveryId < 2; recoveryId++) {
            BigInteger pointX = sign[0];

            X9IntegerConverter x9 = new X9IntegerConverter();
            byte[] compEnc = x9.integerToBytes(pointX, 1 + x9.getByteLength(EC_SPEC.getCurve()));
            compEnc[0] = (byte) ((recoveryId & 1) == 1 ? 0x03 : 0x02);
            ECPoint pointR = EC_SPEC.getCurve().decodePoint(compEnc);
            if (!pointR.multiply(pointN).isInfinity()) {
                continue;
            }

            BigInteger pointE = new BigInteger(1, message);
            BigInteger pointEInv = BigInteger.ZERO.subtract(pointE).mod(pointN);
            BigInteger pointRInv = sign[0].modInverse(pointN);
            BigInteger srInv = pointRInv.multiply(sign[1]).mod(pointN);
            BigInteger pointEInvRInv = pointRInv.multiply(pointEInv).mod(pointN);
            ECPoint pointQ = ECAlgorithms.sumOfTwoMultiplies(EC_SPEC.getG(), pointEInvRInv, pointR, srInv);
            byte[] pointQBytes = pointQ.getEncoded(false);

            boolean matchedKeys = true;
            for (int j = 0; j < publicKey.length; j++) {
                if (pointQBytes[j] != publicKey[j]) {
                    matchedKeys = false;
                    break;
                }
            }
            if (!matchedKeys) {
                continue;
            }
            return (byte) (0xFF & recoveryId);
        }
        return (byte) 0xFF;
    }


//	public static byte[][] pbkdfEncrypt(char[] pw, byte[] data, int dkLen, int count, byte[] iv, byte[] salt) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
//	    byte [] passBytes = PKCS5S2ParametersGenerator.PKCS5PasswordToUTF8Bytes(pw);
//	    
//	    PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest()); 
//	    gen.init(passBytes ,salt, count); 
//	    KeyParameter parameter = (KeyParameter)gen.generateDerivedParameters(256); 
//	    byte [] output = parameter.getKey(); 
//	    System.out.println("Generate : " + Hex.toHexString(output));
//	    
//	    byte[] dKey = new byte[16];
//	    System.arraycopy(output, 0, dKey, 0, dKey.length);
//	    System.out.println("Key1     : " + Hex.toHexString(dKey));
//	    
//	    byte[] mKey = new byte[16];
//	    System.arraycopy(output, dKey.length, mKey, 0, mKey.length);
//	    System.out.println("Key2     : " + Hex.toHexString(mKey));
//	    
//	    Key keybc = new SecretKeySpec(dKey, "AES");
//	    IvParameterSpec ivParam = new IvParameterSpec(iv);
//	    
//	    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", PROVIDER);
//	    cipher.init(Cipher.ENCRYPT_MODE, keybc, ivParam);
//	    byte[] enc = cipher.doFinal(data);
//	    System.out.println("ENC      : " + Hex.toHexString(enc));
//	    
//	    byte[] checkMac = new byte[mKey.length + enc.length];
//	    System.arraycopy(mKey, 0, checkMac, 0, mKey.length);
//	    System.arraycopy(enc, 0, checkMac, mKey.length, enc.length);
//	    System.out.println("Mac Input: " + Hex.toHexString(checkMac));
//	    
//	    MessageDigest md = MessageDigest.getInstance("Keccak-256", PROVIDER);
//	    byte[] digest = md.digest(checkMac);
//	    
//	    byte[][] eData = new byte[2][];
//	    eData[0] = enc;
//	    eData[1] = digest;
//	    
//	    return eData;
//	}

    //	public static byte[][] pbkdfDecrypt(char[] pw, byte[] enc, int dkLen, int count, byte[] iv, byte[] salt) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException  {
//	    byte [] passBytes = PKCS5S2ParametersGenerator.PKCS5PasswordToUTF8Bytes(pw); 
//
//	    PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest()); 
//	    gen.init(passBytes ,salt, count); 
//	    KeyParameter parameter = (KeyParameter)gen.generateDerivedParameters(256); 
//	    byte [] output = parameter.getKey(); 
//	    System.out.println("Generate : " + Hex.toHexString(output));
//	    
//	    byte[] dKey = new byte[16];
//	    System.arraycopy(output, 0, dKey, 0, dKey.length);
//	    System.out.println("Key1     : " + Hex.toHexString(dKey));
//	    
//	    byte[] mKey = new byte[16];
//	    System.arraycopy(output, dKey.length, mKey, 0, mKey.length);
//	    System.out.println("Key2     : " + Hex.toHexString(mKey));
//	    
//	    Key keybc = new SecretKeySpec(dKey, "AES");
//		
//	    IvParameterSpec ivParam = new IvParameterSpec(iv);
//	    
//	    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", PROVIDER);
//	    cipher.init(Cipher.DECRYPT_MODE, keybc, ivParam);
//	    byte[] dec = cipher.doFinal(enc);
//	    System.out.println("DEC      : " + Hex.toHexString(dec));
//	    
//	    byte[] checkMac = new byte[mKey.length + enc.length];
//	    System.arraycopy(mKey, 0, checkMac, 0, mKey.length);
//	    System.arraycopy(enc, 0, checkMac, mKey.length, enc.length);
//	    System.out.println("Mac Input: " + Hex.toHexString(checkMac));
//	    
//	    MessageDigest md = MessageDigest.getInstance("Keccak-256", PROVIDER);
//	    byte[] digest = md.digest(checkMac);
//	    
//	    byte[][] eData = new byte[2][];
//	    eData[0] = dec;
//	    eData[1] = digest;
//	    
//	    return eData;
//	}
//	
    public static byte[] ecpoint2bytes(ECPoint p) {
        ECPoint q = p.normalize();
        byte[] x = q.getXCoord().getEncoded();
        byte[] y = q.getYCoord().getEncoded();

        byte[] out = new byte[x.length + y.length];
        System.arraycopy(x, 0, out, 0, x.length);
        System.arraycopy(y, 0, out, x.length, y.length);
        return out;
    }

    public static byte[] asUnsignedByteArray(int length, BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }

        int start = bytes[0] == 0 ? 1 : 0;
        int count = bytes.length - start;

        if (count > length) {
            throw new IllegalArgumentException("standard length exceeded for value");
        }

        byte[] tmp = new byte[length];
        System.arraycopy(bytes, start, tmp, tmp.length - count, count);
        return tmp;
    }
}
