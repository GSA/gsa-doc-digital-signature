package com.gsa.signingtool.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.GeneralName;
import static com.gsa.signingtool.app.Gui.dateFormat;
import sun.security.provider.certpath.OCSP;

public class CheckRevocationStatus {
    
    public static X509Certificate fetchIssuerCert(X509Certificate cert) {
        AuthorityInformationAccess authInfoAcc = null;
        AuthorityInformationAccess access = null;
        URI responderURI = null;
        X509Certificate issuerCert = null;
        String serviceAddr = null;
        ArrayList<X509Certificate> issuingCerts = new ArrayList<X509Certificate>();
        X509Certificate exactIssuerCert = null;
        boolean issuerMatch = false;
        File issuerChain = null;
        
        //Get issuer certificate from the path to use for OCSP and 
        //validating the end entity
        try {
            Gui.logger.info("Trying to create new file to store "
                    + "issuer p7c file");
            String issuerChainPath = new File("").getAbsolutePath() 
                    + "\\logs\\issuer.p7b";
            issuerChain = new File(issuerChainPath);
            Gui.logger.info("Successfully created issuer file: "
                    + issuerChain.getAbsolutePath());
        } catch (Exception e) {
            Gui.logger.severe("Error creating issuer p7b file");
            Gui.logger.log(Level.FINEST, e.getMessage(), e);
        }
        
        //Get AiA instance from end-entity certificate
        try {
            Gui.logger.info("Trying to get AiA from end-entity"
                    + " certificate");
            byte[] extensionValue = 
                    ((X509Certificate)cert).getExtensionValue("1.3.6.1.5.5.7.1.1");
            DEROctetString string = 
                    (DEROctetString) (new ASN1InputStream
                    (new ByteArrayInputStream(extensionValue)).readObject()); 
            ASN1Sequence seq = 
                    ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(
                            DEROctetString.getInstance(string).getOctets()));
            access = AuthorityInformationAccess.getInstance(seq);
            Gui.logger.info("Successfully retrieved AiA instances");
        } catch (Exception e) {
            Gui.logger.severe("Issue getting AiA instances");
            Gui.logger.log(Level.FINEST, e.getMessage(), e);
        }

        try {
            Gui.logger.info("Setting the accessDescriptions");
            AccessDescription[] accessDescriptions = 
                    access.getAccessDescriptions();
            Gui.logger.info("Looping through the accessDescriptions "
                    + "to get the different end-points");
            for (AccessDescription accessDescription : accessDescriptions) {
                GeneralName gn = 
                        accessDescription.getAccessLocation();
                if(gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                    DERIA5String str = 
                            DERIA5String.getInstance(gn.getName());
                    String accessLocation = str.getString();
                    serviceAddr = accessLocation;
                    if (serviceAddr.toLowerCase().startsWith("http") 
                            && serviceAddr.toLowerCase().endsWith(".p7c") 
                            || serviceAddr.toLowerCase().endsWith(".p7b")) {
                        try {          
                            Gui.logger.info("Setting the p7c URL");
                            URL url = new URL(serviceAddr);
                            Gui.logger.info("Downloading the issuer "
                                    + "cert p7c file");
                            org.apache.commons.io.FileUtils.copyURLToFile(
                                    url, issuerChain);
                            Gui.logger.info("Saving the issuer "
                                    + "cert p7c file");
                            FileInputStream fis = new FileInputStream(issuerChain);
                            Gui.logger.info("File location: " 
                                    + issuerChain.getAbsolutePath());
                            Gui.logger.info("Generating a "
                                    + "new certificate to store issuer cert ");
                            CertificateFactory cf = 
                                    CertificateFactory.getInstance("x.509");
                            Iterator iter = 
                                    cf.generateCertificates(fis).iterator();
                            Gui.logger.info("Iterating through the p7c file");
                            while (iter.hasNext()) {
                                issuerCert = (X509Certificate)iter.next();
                                issuingCerts.add(issuerCert);
                                Gui.logger.info("Verifying if their is a match "
                                        + "between the end-entity issuerDN "
                                        + "and certificates in p7c file subjectDN");
                                if (issuerCert.getSubjectDN().equals(cert.getIssuerDN())) {
                                    issuerMatch = true;
                                    Gui.logger.info("Found a match! The "
                                            + "issuer of the end-entity cert's serial "
                                            + "number is: " 
                                            + issuerCert.getSerialNumber());
                                    exactIssuerCert = issuerCert;
                                }
                            }
                            if (issuerMatch = false) {
                                Gui.logger.severe("Unable to check "
                                        + "revocation status. Issue "
                                        + "finding issuer cert");
                                return null;
                            }
                        } catch (Exception e) {
                            Gui.logger.log(Level.FINEST, e.getMessage(), e);
                        }
                    }
                }
                Gui.logger.info("AiA length: " + accessDescriptions.length);
            }
        } catch (Exception e) {
            Gui.logger.severe("Issue with obtaining issuer cert");
            Gui.logger.log(Level.FINEST, e.getMessage(), e);
        }    
        Gui.logger.info("Returning the cert with the following serial #: " 
                + exactIssuerCert.getSerialNumber());
        return exactIssuerCert;
    }
    
    public static String checkRevocation(X509Certificate end, X509Certificate issuer) {
        String revocationStatus = null;
        try {
            Gui.logger.info("Checking the revocation status");
            revocationStatus = OCSP.check(end, issuer).getCertStatus().toString();
            Gui.logger.info("Revocation Status is: " + revocationStatus);
        } catch (Exception ex) {
            if (SigningTool.expired == true) {
                Gui.status.append(dateFormat.format(new Date()) + " - Unable to "
                        + "sign. Revocation Status returned an error.\n" 
                        + dateFormat.format(new Date()) 
                        + " - The signing certificate is expired.\n"
                        + dateFormat.format(new Date()) 
                        + " - Expiration Date: " 
                        + end.getNotAfter().toString() 
                        + "\n");
                System.out.println("Unable to sign. The signing certificate "
                        + "is expired. Also, unable revocation status "
                        + "returned an error.");
                Gui.logger.log(Level.FINEST, ex.getMessage(), ex);
                return null;
            } else {
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - Unable to get revocation status.\n ");
                System.out.println("Unable to sign. The revocation "
                        + "check for your signing cert failed. If you "
                        + "believe this is in err, disable revocation "
                        + "checking in the menu.");
                Gui.logger.log(Level.FINEST, ex.getMessage(), ex);
                return null;
            }
            
        }
        Gui.logger.finest("Returning the following revocation status: " 
                + revocationStatus);
        return revocationStatus;
    }
}
