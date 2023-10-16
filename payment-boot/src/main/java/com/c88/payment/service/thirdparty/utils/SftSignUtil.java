package com.c88.payment.service.thirdparty.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class SftSignUtil {
	public final static Charset UTF_8 = Charset.forName("UTF-8");

	public static String formatSignData(Map<String, String> signDataMap) {
		Set<String> sortedSet = new TreeSet<String>(signDataMap.keySet());
		StringBuffer sb = new StringBuffer();
		for (String key : sortedSet) {
			if ("sign".equalsIgnoreCase(key)) {
				continue;
			}

			if (signDataMap.get(key) != null) {
				String v = String.valueOf(signDataMap.get(key));
				if (StringUtils.isNotBlank(v)) {
					sb.append(key);
					sb.append("=");
					sb.append(v);
					sb.append("&");
				}
			}
		}
		String s = sb.toString();
		if (s.length() > 0) {
			s = s.substring(0, s.length() - 1);
		}
//		log.debug("To be signed data: {}", s);
		return s;
	}

	public static boolean verifySign(String signType, String signData, String sign, String authMchPublicKey) {
		if ("md5".equals(signType.toLowerCase())) {
			return md5VerifySign(signData, sign, authMchPublicKey);
		} else {
			try {
				return rsaVerifySign(signData, sign, authMchPublicKey);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
	}

	public static boolean rsaVerifySign(String signData, String sign, String authMchPublicKey) throws Exception {
		RSAPublicKey key = RSAUtils.getPublicKey(authMchPublicKey);
		return RSAUtils.VerifyRSASha256( signData,sign, key);
	}

	public static boolean md5VerifySign(String signData, String sign, String authMchPublicKey) {
		String lsign = md5Sign(signData, authMchPublicKey, "");
		return lsign.equals(sign.toLowerCase());
	}

	public static String sign(String signType, String signData, String sftPrivateKey, String source) throws Exception {
		if ("md5".equals(signType.toLowerCase())) {
			return md5Sign(signData, sftPrivateKey, source);
		} else {

			return rsaSign(signData, sftPrivateKey);
		}
	}

	public static String rsaSign(String signData, String sftPrivateKey) throws Exception  {
		RSAPrivateKey key = RSAUtils.getPrivateKey(sftPrivateKey);
		return RSAUtils.DoRSASha256( signData, key);
	}

	public static String md5Sign(String signData, String sftPrivateKey, String source) {
		String sign;
		if(source.equals("TR"))
			sign = PayMD5.MD5Encode(signData + "&key=" + sftPrivateKey);
		else
			sign = PayMD5.MD5Encode(signData + sftPrivateKey);
		return sign.toLowerCase();
	}

    public static JSONObject sortJson(JSONObject signDataJson) {
        @SuppressWarnings("unchecked")
		Set<String> sortedSet = new TreeSet<String>(signDataJson.keySet());
        JSONObject tmp = new JSONObject();
        for (String key : sortedSet) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }
            tmp.put(key, signDataJson.get(key));
        }
        return tmp;
    }


    public static String sign(Map<String, String> signDataMap, String sftPrivateKey) {
        String toBeSignedData = formatSignData(signDataMap);
        byte[] signBuf = RSAUtil.sign(RSAUtil.SignatureSuite.SHA256, toBeSignedData.getBytes(UTF_8),
                sftPrivateKey);
        return new String(Base64.getEncoder().encode(signBuf), UTF_8);
    }

}
