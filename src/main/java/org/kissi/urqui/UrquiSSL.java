package org.kissi.urqui;


/* 
 *
 * @author Kinesis Identity Security System Inc. 
 */
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

class UrquiSSL {

    private final SSLContext sc = SSLContext.getInstance("SSL");

    private static X509Certificate loadCA(String certFile) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(UrquiSSL.class
                .getResourceAsStream(certFile));
    }

    UrquiSSL(String certFile) throws NoSuchAlgorithmException, KeyManagementException, CertificateException {

        final X509Certificate URQUiCA = loadCA(certFile);
        
        TrustManager[] trustURQUiCert = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{URQUiCA};
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
                    } catch (NoSuchProviderException | SignatureException e) {
                        throw new CertificateException("NoSuchProviderException");
                    }
                }
            }
        };

        sc.init(null, trustURQUiCert, new java.security.SecureRandom());
    }

    SSLSocketFactory mySocketFactory() {
        return sc.getSocketFactory();
    }
}
