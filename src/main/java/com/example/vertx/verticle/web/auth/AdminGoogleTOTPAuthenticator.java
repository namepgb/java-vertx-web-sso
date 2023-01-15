package com.example.vertx.verticle.web.auth;

import com.example.vertx.verticle.launcher.LauncherConfig;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AdminGoogleTOTPAuthenticator {
    static String SHARED_SECRET_KEY = null;

    public static class HandleRegist implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext ctx)
        {
            SHARED_SECRET_KEY = generateSharedSecretKey();
            String qrURL = generateQRBarcodeURL("admin", "example.com", SHARED_SECRET_KEY);
            ctx.response().setStatusCode(200);
            ctx.response().end(new JsonObject().put("sharedScretKey", SHARED_SECRET_KEY).put("qrURL", qrURL).encode());
        }
    }

    /**
     * shared secret key를 생성
     */
    public static String generateSharedSecretKey()
    {
        // 5바이트 버퍼에 랜덤 값 입력
        byte[] buffer = new byte[5];
        new Random().nextBytes(buffer);
        // 80 비트 비밀 키 생성
        byte[] sharedSecretKey = Arrays.copyOf(buffer, 10);
        // Authenticator에서 등록을 위해서 Base32로 인코딩
        Base32 codec = new Base32();
        byte[] encrytedSharedSecretKey = codec.encode(sharedSecretKey);
        // 문자열로 출력
        return new String(encrytedSharedSecretKey);
    }

    /**
     * Google Chart API를 사용하여 QR 코드 URL을 생성
     */
    public static String generateQRBarcodeURL(String user, String host, String sharedSecretKey) {
        String format = "http://chart.apis.google.com/chart?cht=qr&chs=200x200&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s&chld=H|0";
        return String.format(format, user, host, sharedSecretKey);
    }

    /**
     * TOTP를 생성
     */
    public static String[] generateTOTP(String sharedSecretKey)
    {
        // shared secret key를 Base32로 디코딩
        Base32 codec = new Base32();
        byte[] key = codec.decode(sharedSecretKey);

        Long timeStep = LauncherConfig.Web.getTOTPTimeStep();
        Integer movingFactorOffset = LauncherConfig.Web.getTOTPMovingFactorOffset();
        if (movingFactorOffset < 0)
            movingFactorOffset = 0;
        int offset = -(movingFactorOffset);
        List<String> TOTPs = new ArrayList<>();
        do {
            long UT = new Date().getTime();
            // T0는 0으로 고정
            long T0 = 0L;
            long T = ((UT - T0) / (timeStep)) + offset;
            String TOTP = generateHOTP(key, T);
            TOTPs.add(TOTP);
        } while (offset++ < movingFactorOffset);
        return TOTPs.toArray(new String[TOTPs.size()]);
    }

    /**
     * HTOP를 생성
     */
    public static String generateHOTP(byte[] key,
                                      long T)
    {
        byte[] message = new byte[8];
        for (int i = message.length - 1; i >= 0; i--) {
            message[i] = (byte) (T & 0xff);
            T >>= 8;
        }

        byte[] hmac = HMAC(key, message);
        // 마지막 8비트(0xdd)에서 끝 4bit를 구한 값을 offset으로 설정
        byte bFinal = hmac[hmac.length - 1];
        byte bOffset = (byte) (bFinal & 0xf);

        // offset에서 4bytes를 가져오고 첫번째 부호 비트를 제거한다.
        byte[] bResult = new byte[4];
        for (int i = bOffset; i < bOffset + 4; ++i)
            bResult[i - bOffset] = hmac[i];
        bResult[0] &= 0x7f;

        // 버퍼를 6자리 decimal 문자열로 변환
        int b = new BigInteger(bResult).intValue();
        int c = b % ((int) Math.pow(10, 6));
        return String.format("%06d", c);
    }

    /**
     * HMAC 알고리즘을 실행
     */
    public static byte[] HMAC(byte[] key,
                              byte[] message)
    {
        try {
            // HmacSHA1 알고리즘을 구현하는 Mac 객체를 작성
            Mac mac;
            try {
                mac = Mac.getInstance("HmacSHA1");
            } catch (NoSuchAlgorithmException nsae) {
                mac = Mac.getInstance("HMAC-SHA-1");
            }

            // SecretKeySpec 클래스를 사용하여 키 생성
            SecretKeySpec macKey = new SecretKeySpec(key, "RAW");
            // SecretKeySpec 객체를 사용해 이 Mac 객체를 초기화
            mac.init(macKey);
            byte[] hmac = mac.doFinal(message);
            return hmac;
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
}
