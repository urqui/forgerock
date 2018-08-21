package org.kissi.urqui;


/* 
 *
 * @author Kinesis Identity Security System Inc. 
 */
import java.io.IOException;
import java.io.InputStream;

import java.net.UnknownHostException;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class UrquiSSL {

    private final SSLContext sc = SSLContext.getInstance("SSL");

    private static X509Certificate loadCA(String certfile) {
        CertificateFactory certFactory;
        X509Certificate cert = null;
        try {
            certFactory = CertificateFactory.getInstance("X.509");

            InputStream is = UrquiSSL.class.getResourceAsStream(certfile);

            cert = (X509Certificate) certFactory.generateCertificate(is);
        } catch (CertificateException e) {
            e.printStackTrace();

        }
        return cert;
    }

    public UrquiSSL(String certfile) throws NoSuchAlgorithmException, KeyManagementException, UnknownHostException, IOException {

        final X509Certificate URQUiCA = loadCA(certfile);
        
        TrustManager[] trustURQUiCert = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{URQUiCA};
                    //				return new X509Certificate[] {};
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
                    if (certs.length != 1) {
                        throw new CertificateException("InvalidCertCount");
                    }
                    try {
                        certs[0].verify(URQUiCA.getPublicKey());
                    } catch (InvalidKeyException e) {
                        throw new CertificateException("InvalidKeyException");
                    } catch (NoSuchAlgorithmException e) {
                        throw new CertificateException("NoSuchAlgorithmException");
                    } catch (NoSuchProviderException e) {
                        throw new CertificateException("NoSuchProviderException");
                    } catch (SignatureException e) {
                        throw new CertificateException("NoSuchProviderException");
                    }
                }
            }
        };

        sc.init(null, trustURQUiCert, new java.security.SecureRandom());
    }

    public SSLSocketFactory mySocketFactory() {
        return (SSLSocketFactory) sc.getSocketFactory();
    }
}
