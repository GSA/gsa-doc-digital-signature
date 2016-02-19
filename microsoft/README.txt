Intro
-----
This Document Signing Tool application will digitally sign documents and files
using digital certificates. It will store the document
and signature together in a single PKCS7 enveloped file.  The
signed file will appear in the same directory as the original file,
with the same filename with an added extension of ".p7m".


Quick start:
------------
1. Start the program from your Desktop Shortcut or from Start -> ICAM -> PKCS7 Signing Tool
2. Click the Open button to select a file to sign
3. Select a file and click Open
3. Enter your PIN
4. Click the Sign button

Your file should now be signed.


Change Log:
-----------
v2.0.0 - new GUI improvements
       - included 508 compliance
       - fixed certificate selection issue by relying on PKCS#11 instead of using the MSCAPI key store
       - added check for certificate expiration
       - added check for certificate revocation status
       - updated Java to version 1.8.0_25
       - updated Bouncy Castle libraries to 1.51
       - removed PKCS7Unpack.exe
v1.5.0 - no file association for signing, just unpack
v1.4.0 - fixes PKCS7Unpack.exe error
v1.3.0 - fixes problem on certain service packs of WinXP where the
            PKCS7 envelope contains a reference to an incorrect 
            certificate
v1.2.0 - displays certificate selection dialog on Win XP even if only
            one available signing certificate
v1.1.0 - repairs issue with selecting wrong signing cert


Certificate Validation
----------------------
By default, the tool will check the revocation status of the certificate used to sign the file.


Security
--------
All files will be signed using SHA256.


Compatibility
-------------
This tool was tested and successfully signed files using Windows 8.1 / Windows 7 / Windows XP.
