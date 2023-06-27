package it.polimi.ingsw.server.network.RmiServerServices;

import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiInterface;
import it.polimi.ingsw.network.ClientHandler.RmiServices.RmiService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

/**
 * Class: RmiServerService
 * @author Paolo Ceffa
 * Implementation of RmiServerInterface, which allow a Rmi client to establish a connection with the server
 */
public class RmiServerService extends UnicastRemoteObject implements RmiServerInterface {

    RmiInterface rmiService;

    /**
     * Default constructor
     * @throws RemoteException if a network issue occurred
     */
    public RmiServerService() throws RemoteException {
        this.rmiService = null;
    }

    /**
     * Implementation of the getRmiClientService of the RmiInterface
     */
    @Override
    public synchronized RmiInterface getRmiClientService() throws RemoteException {
        RmiInterface clientSideService = new RmiService();
        RmiInterface serverSideService = new RmiService(clientSideService);
        clientSideService.setClient(serverSideService);
        while(this.rmiService != null){   //we wait that the establishRmiConnectionKernel manges the previous client
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
        this.rmiService = serverSideService;
        return clientSideService;
    }

    /**
     * Method to allow the server to obtain the RmiInterface object of one connected Rmi client
     * @return the Rmi service object of the client
     */
    public RmiInterface getServerService(){
        RmiInterface result = this.rmiService;
        this.rmiService = null;
        return result;
    }

    /**
     * Method used server-side if there is an RMI waiting client
     * @return True if there is an RMI waiting client False otherwise
     */
    public boolean isThereAClient(){
        return (this.rmiService != null);
    }
}
