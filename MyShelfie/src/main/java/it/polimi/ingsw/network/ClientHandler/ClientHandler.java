package it.polimi.ingsw.network.ClientHandler;

import it.polimi.ingsw.network.message.Message;
import it.polimi.ingsw.network.message.MessageCode;
import it.polimi.ingsw.network.message.PingMessage;
import it.polimi.ingsw.server.WebServer;
import it.polimi.ingsw.utils.ClientDisconnectedException;
import it.polimi.ingsw.utils.ErrorReceiveException;
import it.polimi.ingsw.utils.ErrorSendException;
import it.polimi.ingsw.utils.NoMessageToReadException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;


/**
 * Abstract class to manage the communication  server-client and client-server
 *
 */
abstract public class ClientHandler {

    protected Integer pingMessageCounter; // number of ping messages to read

    private static final long  PING_FREQUENCY = 500; // time interval beetween a ping message and the next one, in millsecs

    protected ConcurrentLinkedQueue<Message> messageQueue; // asynchronous message queue

    protected Boolean connectionStatus; //connection status of the receiver

    /**
     * Raw method to receive a message from the client
     *
     * @return the received Message object
     * @throws ErrorReceiveException if an exception occurs trying to receive the message
     */
    protected abstract Message getMessageFromNetwork() throws ErrorReceiveException;

    /**
     * Raw method to send a message to the client
     *
     * @param message message to put on the network
     * @throws ErrorSendException if an exception occurs trying to send the message
     */
    protected abstract void putMessageInNetwork(Message message) throws ErrorSendException;


    /**
     * Method to get the first Message to read in the messageQueue, if any
     *
     * @return the first Message to read
     * @throws NoMessageToReadException if there is no message to read and the client is still connected
     * @throws ClientDisconnectedException if there is no message to read and the client is no more connected
     */
    public Message receive() throws NoMessageToReadException, ClientDisconnectedException {
        if(!this.messageQueue.isEmpty()){
            return this.messageQueue.poll();
        }
        else{
            if(isConnected()) {
                throw new NoMessageToReadException();
            }
            else{
                throw new ClientDisconnectedException();
            }
        }
    }

    /** Method to get a message with retry logic. If the messageQueue is empty we wait for a certain time,
     * then we look again into the messageQueue and check if any message arrived.
     *
     * @param attempts max number of attempts to perform
     * @param waitingTime the waiting time between one attempt and the  next noe
     * @return the first message found in the messageQueue, if any it found during the several attempts
     * @throws NoMessageToReadException if no message found after the max number of attempts,
     * and the client is still connected
     * @throws ClientDisconnectedException if there is no message to read and the client is no more connected
     */
    public Message receivingWithRetry(int attempts, int waitingTime)  throws NoMessageToReadException,
            ClientDisconnectedException {
        int count = 0;
        Message message = null;
        while(count < attempts){
            try {
                message = this.receive();
                break;
            }catch(NoMessageToReadException e){
                if(count == attempts-1) throw new NoMessageToReadException();
                try{
                    TimeUnit.SECONDS.sleep(waitingTime);
                }catch(InterruptedException err){
                    WebServer.LOG.warning(err.getMessage());
                }
                count += 1;
            }

        }
        return message;
    }


    /**
     * method to send a message to the client
     *
     * @param message message to send
     * @return a boolean value about the status of the sending: true: message has been correctly send,
     * false: an issue occurred trying to send the message
     * @throws ClientDisconnectedException if the client is no more connected
     */

    public boolean send(Message message) throws ClientDisconnectedException {
        try{
            if(! this.isConnected()){
                throw new ClientDisconnectedException();
            }
            else{
                this.putMessageInNetwork(message);
                return true;
            }
        }catch(ErrorSendException e){
            return false;
        }
    }

    /**
     * Method to send a message to client with retry logic
     *
     * @param message message to send
     * @param attempts max number of sending attempts to perform
     * @param waitingTime waiting time between one attempt and the next one
     * @return a boolean value about the status of the sending: true: message has been correctly send,
     * false: an issue occurred several times trying to send the message
     * @throws ClientDisconnectedException if the client is no more connected
     */
    public boolean sendingWithRetry(Message message, int attempts, int waitingTime) throws ClientDisconnectedException{
        int count = 0;
        boolean status = false;
        while(count < attempts){
            status = this.send(message);
            if(! status){
                try{
                    TimeUnit.SECONDS.sleep(waitingTime);
                }catch(InterruptedException e){
                    WebServer.LOG.warning(e.getMessage());
                }
                count += 1;
            }
            else{
                count = attempts;
            }

        }
        return status;
    }

    /**
     * Method to manage all incoming messages in a dedicated thread, storing them in a suitable data structure.
     *
     */
    public void receivingKernel(){
        new Thread(()-> {
            boolean kernelFlag = true;
            while (kernelFlag) {
                try {
                    Message message = this.getMessageFromNetwork();
                    if (message.getMessageType().equals(MessageCode.PING_MESSAGE)) {
                        synchronized (this.pingMessageCounter) {
                            this.pingMessageCounter += 1;
                        }
                    } else {
                        this.messageQueue.add(message);
                    }

                } catch (ErrorReceiveException e) {
                    // just ignore it, we can get an exception due to synchronization
                    // time when the pingKernel disconnects the client and at the same time,
                    // we are trying to read a message from the network.
                }
                catch(ArrayStoreException e1){}
                synchronized (this.connectionStatus) {
                    kernelFlag = this.connectionStatus;
                }
            }
        }).start();
    }

    /**
     * method to manage the ping process client-server in a dedicated thread, exchanging a ping message with a certain frequency
     * in order to establish if the client is still connected by server side, and to establish if the server
     * consider the client still connected by client side
     *
     */
    public void pingKernel(){
        new Thread(()-> {
            boolean kernelFlag = true;
            while (kernelFlag) {
                try {
                    TimeUnit.MILLISECONDS.sleep(PING_FREQUENCY);
                } catch (InterruptedException e) {
                    WebServer.LOG.warning(e.getMessage());
                }
                synchronized (this.pingMessageCounter) {
                    if (this.pingMessageCounter == 0) {
                        synchronized (this.connectionStatus) { //we consider the client disconnected from now on
                            this.connectionStatus = false;
                        }
                    } else {
                        try {
                            this.putMessageInNetwork(new PingMessage());
                        } catch (ErrorSendException ignored) {
                            // just ignore it, we can get an exception due to synchronization
                            // time when we close the connection with a client and at the same time,
                            //we are trying to send a ping message.
                        }
                    }
                    this.pingMessageCounter -= 1;
                }
                synchronized (this.connectionStatus) {
                    kernelFlag = this.connectionStatus;
                }
            }
        }).start();
    }

    /**
     * Raw method to get the status of the connection
     *
     * @return a boolean value which represents the connection status
     */
    public boolean isConnected(){
        synchronized (this.connectionStatus){
            if(this.connectionStatus){
                return true;
            }
            else return false;
        }
    }

    /**
     * Raw method to correctly stop the connection
     *
     */
    public void stopConnection(){
        synchronized (this.connectionStatus) {
            this.connectionStatus = false;
        }
    }

}
