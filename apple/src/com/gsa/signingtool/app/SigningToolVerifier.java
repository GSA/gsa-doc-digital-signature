/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsa.signingtool.app;

import static com.gsa.signingtool.app.Gui.logger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import static com.gsa.signingtool.app.Gui.dateFormat;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author jorpac01
 */
public class SigningToolVerifier {
    protected SigningToolVerifier(File file) {
        byte[] fileBytes = null;
        
        if (file != null) {
            fileBytes = getFileContentBytes(file);
        }
        System.out.println(fileBytes.length);
        
        //Object selectFile = unpackP7(fileBytes, file.toString());
        System.out.println(unpackP7(fileBytes, file.toString()));
        Gui.progress.setValue(100);
    }
    
    public byte[] getFileContentBytes(File file) {
        byte[] fileBytes = null;
        
        try {
                FileInputStream fos = new FileInputStream(file);
                fileBytes = new byte[(int)file.length()];
                fos.read(fileBytes);
                fos.close();
            } catch (Exception x) {
                System.out.println("Unable to open " + file);
            }
        return fileBytes;
    }
    
    public String unpackP7(byte[] bytesToUnpack, String filename)
    {
        String str = null;
        StringBuffer response = new StringBuffer();
        boolean verified = false;
        int lastPeriodPos = filename.lastIndexOf('.');
        int flag = 0;
        String ext = "";
        try {
            ext = FilenameUtils.getExtension(filename);
            System.out.println("File Type is: " + ext);
            if (ext.equals("p7m")) {
                System.out.println("Valid .p7m file.");
                CMSSignedData signedData = new CMSSignedData(bytesToUnpack);
                CMSProcessable signedContent = signedData.getSignedContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                signedContent.write(baos);
            
                byte[] fileBytes = baos.toByteArray();
                str = filename.substring(0, lastPeriodPos);
                writeBytes(fileBytes, str);
                response.append("\n\nUnpacked to " + str + "\n");
                Gui.status.append(dateFormat.format(new Date()) + " - " + str + "\n");
            } else {
                flag = 1;
                throw new Exception();
            }           
        } catch (Exception e) {
            response.append("Unable to unpack " + filename + ": " + e + "\n");
            if (flag == 1) {
                logger.severe("File selected does not have the appropriate file " 
                        + "extension.\n File should end in a .p7m file extension.");
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - File selected does not have the appropriate file extension (.p7m).\n");
                Gui.errors = true;
                Gui.progress.setValue(100);    
            } else {
                logger.severe(e.getMessage());
                Gui.status.append(dateFormat.format(new Date()) 
                        + " - Issue unpacking the signed file. See log file for more details.\n");
                Gui.errors = true;
                Gui.progress.setValue(100);    
            }
        }
        return response.toString();
    }
    
    public Boolean errMessage (String filename) {
        int lastPeriodPos = filename.lastIndexOf('.');
        if (!filename.substring(0, lastPeriodPos).equals(".p7m")) {
            String errMesg = "Not a valid .p7m file.";  
        }
        return true;
    }
    public String writeBytes(byte[] bytesToWrite, String paramString)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(paramString);
            fos.write(bytesToWrite);
            fos.close();
            return null;
        } catch (Exception x) {
            x.printStackTrace();
            return "Error Writing File " + paramString + ": " + x.toString();
        }
   }
}
