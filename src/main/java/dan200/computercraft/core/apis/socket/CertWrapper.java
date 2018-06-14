/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import javax.net.ssl.*;
import java.security.cert.Certificate;
import java.io.*;
import java.net.*;
import java.util.*;

public class CertWrapper {
    public boolean hasCertificates = false;
    public Certificate[] certificates;
    
    public CertWrapper(SSLSocket sock){
        try {
		    sock.startHandshake();
		    SSLSession sslsock = sock.getSession();
		    this.certificates = sslsock.getPeerCertificates();
		    this.hasCertificates = true; 
	    } catch (Exception e){
			System.out.println("Failed to initialize SSL connection!\nException reached: " + e.getMessage());
	    }
    }
    public CertWrapper(){}
}