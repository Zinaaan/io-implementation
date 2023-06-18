# io-implementation
The Implementation of Blocking IO, Non-blocking IO and Netty
## BIO-V1
The server only use one thread to receive and handle client sockets. If current socket is still in processing, the subsequent socket will be blocked.

## BIO-V2
### New-1:
The server will open a separate thread to process each new socket so that the multiple socket could process concurrently.
### New-2:
The server will create a thread pool to reduce the overhead of thread creation and destruction.
    
## NIO-V1
Add each socket events to array, and continuously loop to check and process it even if this socket didn't send any message

## NIO-V2
Initialize a Selector to receive the socket events. The selector will start processing only when there are some socket registered.
