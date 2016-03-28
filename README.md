### Description
This repo contains the General Services Administration (GSA) Document Signing tools used for digitally signing documents and files with Personal Identification Validation (PIV) cards. The main purpose of the tool is for Federal agencies to use for digitally signing documents to be submitted to the Office of the Federal Register's (OFR) document submission web portal. The tool was developed using Java 8. It relies on Bouncy Castle libraries for the signing operations, Apache Commons for file operations, and OpenSC for PIV operations.

### How does it work?
The GSA Document Signing Tool includes a simple, 508-compliant Graphical User Interface (GUI) to assist in the signing of your desired document. The tool allows the user to select the desired file they'd like signed, a field to enter the user's PIV application PIN , and a "Sign" button to initiate the signing. The tool automatically detects any smart card readers and PIV cards connected to the system. The tool utilizes PKCS#11 to interact with the reader and PIV card. The signed file is formatted in an enveloped PKCS#7 (.p7m) file, which is the required format for the OFR's document submission portal. In order for the tool to sign correctly, the PIV card MUST contain a valid digital signing certificate (i.e. Key usage of Digital Signature and Non-repudiation). If it doesn't contain a valid digital signing certificate, the application will provide a descriptive error messages.

### Installation/Usage Instructions

The GSA Document Signing Tool includes a Windows 32-bit, 64-bit, and MacOS version. Verify your system's operating system before installing. Use the following User Guides below for guiding you through the installation process:

* Windows Installation Guide - https://github.com/GSA/gsa-doc-digital-signature/blob/platforms/microsoft/GSA_Signing_Tool_User_Guide-64bit.pdf
* MacOS Installation Guides - https://github.com/GSA/gsa-doc-digital-signature/blob/platforms/apple/GSA_Signing_Tool_User_Guide-Mac.pdf

### Developer Install Instructions

For developers, the source code, 3rd party libraries, and drivers can be imported into a Java IDE (i.e. Eclipse, NetBeans).

**NOTE:** This tool will only compile correctly using Java 8. Lower Java versions are not compatible with the development of this tool.

Initial setup:
* Import initial source code in the "/src" directory to your Java IDE project source code directory.
* Add the specific opensc driver for the system type you will be developing for (i.e. if you are developing on a 64-bit system, copy the opensc driver in the 64-bit directory, and paste the driver in the "lib" directory with the Bouncy Castle libraries.
* Import "lib" directory to your Java IDE project.
* Use the "GUI" class, as your main executable class for executing the tool.

### Licenses

The GSA Document Signing Tool leverages 3rd party open source libraries including:
* [Java] http://www.oracle.com/
* [Bouncy Castle] https://www.bouncycastle.org/
* [opensc] https://github.com/OpenSC/OpenSC
* [commons-io] http://commons.apache.org/proper/commons-io/

On Windows, the installer was built using [inno setup] http://www.jrsoftware.org/

### Contact Information

Please contact icam@gsa.gov for any questions or issues related to the tool.