/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.apis.socket;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.core.apis.socket.AsyncAction;
import dan200.computercraft.core.apis.socket.IAsyncObject;
import dan200.computercraft.core.apis.socket.CertWrapper;
import dan200.computercraft.core.apis.IAPIEnvironment;
import javax.annotation.Nonnull;
import javax.net.ssl.*;
import java.security.cert.Certificate;
import java.io.*;
import java.net.*;
import java.util.*;
import java.math.BigInteger;

import static dan200.computercraft.core.apis.ArgumentHelper.*;


public class SocketWrapper {

    final static String[] methods = new String[] {
        "write",
        "read",
        "close",
        "isClosed",
        "getCertificates",
		"readLine",
		"readAll"
    };

    public static ILuaObject wrapSocket(final Socket sock, final CertWrapper certs, IAPIEnvironment m_apiEnvironment, AsyncAction m_queue) {
        class WrappedSocket implements ILuaObject, IAsyncObject {

			
            @Nonnull
            @Override
            public String[] getMethodNames() {
                return methods;
            }

            @Override
            public Object[] callMethod(@Nonnull ILuaContext context, int method, @Nonnull Object[] args) throws LuaException, InterruptedException {
                switch (method) {

                    default: {
						int ID = m_queue.runAsyncAction(
							this, m_apiEnvironment, context, method, args
						);

						return new Object[] { ID };
                    }

                }
            }

            public Object[] callAsyncMeth(@Nonnull ILuaContext context, int method, @Nonnull Object[] args) {
                try {
                    if ((method != 3) && sock.isClosed()) {
                        throw new Exception("Attempt to interact with closed socket");
                    };

                    switch (method) {
                        case 0:
                            {
                                // write
                                String arg = getString(args, 0);
                                byte[] bytes = arg.getBytes("UTF-8");
                                sock.getOutputStream().write(bytes);

                                return new Object[] { methods[method], true };
                            }
                        case 1:
                            {
                                // read
                                int times = optInt(args, 0, 1);

								String res = "";
								int lastbyte = 0;
								try {
									for (int i = 0; i < times; i++) {
										byte[] bytes = {(byte) sock.getInputStream().read()};
										res = res + new String(bytes, "UTF-8");
									}
								} catch (SocketTimeoutException e) {
									res = null;
								}
                                return new Object[] { methods[method], true, res };
                            }
                        case 2:
                            {
                                // close
                                sock.close();

                                return new Object[] { methods[method], true };
                            }
                        case 3:
                            {
                                // isClosed

                                return new Object[] { methods[method], true, sock.isClosed() };
                            }
                        case 4:
                            {
                                //getCertificates
                                if (!certs.hasCertificates) {
                                    return new Object[] {
                                        methods[method], false, "The socket is not secure"
                                    };
                                }

                                Object[] rtn = new Object[2 + certs.certificates.length];

                                System.out.println(certs.certificates.length);
                                for (int i = 0; i < certs.certificates.length; i++) {
                                    rtn[i + 2] = certs.certificates[i].getType();
                                }
								rtn[0] = methods[method];
                                rtn[1] = true;

                                return rtn;
                            }
							
						case 5:
						{
							//readLine
							String res = null;
							try {
								res = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8")).readLine();
							} catch (SocketTimeoutException e) {}
                            return new Object[] { methods[method], true, res };
						}
						
						case 6:
						{
							//readAll
							String res = "";
							String resadd = "";
							BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
							while (true) {
								try {
									resadd = br.readLine();
								} catch (SocketTimeoutException e) {
									resadd = null;
								}
								if (resadd == null) {
									break;
								}
								res = res + resadd;
							}
							
							return new Object[] { methods[method], true, res };
						}

                        default:
                            {
                                return new Object[] {
                                    "?", false, "Unknown method of \"Sock\" class called"
                                };
                            }
                    }
                } catch (Exception e) {
                    return new Object[] {
                        methods[method], false, e.getMessage()
                    };
                }
            }

        }
        return new WrappedSocket();
    }

}