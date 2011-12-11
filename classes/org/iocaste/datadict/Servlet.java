package org.iocaste.datadict;

import org.iocaste.protocol.Message;
import org.iocaste.protocol.ServerServlet;

public class Servlet extends ServerServlet {
    private static final long serialVersionUID = -4447612067637162915L;

    /*
     * (non-Javadoc)
     * @see org.iocaste.protocol.ServerServlet#config()
     * 
     * registre as bibliotecas aqui com register()
     */
    @Override
    public void config() {
        
    }
    
    /*
     * (non-Javadoc)
     * @see org.iocaste.protocol.ServerServlet#preRun(
     *     org.iocaste.protocol.Message)
     * 
     * rotinas customizadas de pré-processamento
     */
    @Override
    public void preRun(Message message) throws Exception {
        super.preRun(message);
    }
}
